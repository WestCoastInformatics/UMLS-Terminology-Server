/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.ReportService;

/**
 * JPA and JAXB enabled implementation of {@link HistoryService}.
 * 
 * TODO: factor out the style calculation into a separate method so it can be
 * reused in other parts of the rpt TODO: if the style is "default" then don't
 * write span tags at all TODO: put span tags around the first CUI written out
 * TODO: put span tags around the STYs TODO: put span tags around the
 * relationships in the DEMOTED RELATIONSHIPS section TODO: put span tags around
 * the relationships in the NEEDS REVIW RELATIONSHIPS section.
 */
public class ReportServiceJpa extends HistoryServiceJpa
    implements ReportService {

  /** The line end. */
  private String lineEnd = "\r\n";

  /** The parent. */
  private Tree parent = null;

  /** The indent. */
  private String indent = "";

  /**
   * Instantiates an empty {@link ReportServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ReportServiceJpa() throws Exception {
    super();

  }

  /* see superclass */
  @Override
  public String getConceptReport(Project project, Concept concept)
    throws Exception {

    final StringBuilder sb = new StringBuilder();

    //
    // Options
    //

    //
    // Handle validation/integrity checks
    //
    if (project != null) {
      ValidationResult validationResult = validateConcept(project, concept);
      sb.append("As of ");
      sb.append(new Date());
      if (validationResult.getWarnings().size() > 0
          || validationResult.getErrors().size() > 0) {
        sb.append(", this entry has the following problems/issues: ");
        for (String warning : validationResult.getWarnings()) {
          sb.append(warning);
        }
        for (String error : validationResult.getErrors()) {
          sb.append(error);
        }
      } else {
        sb.append(", this entry had no problems/issues.");
      }
      sb.append("\r\n");
    }

    sb.append(
        "...............................................................................");

    //
    // Concept information
    //
    sb.append(lineEnd).append("CN# ");
    sb.append(concept.getId()).append(" ");
    sb.append(concept.getName()).append(lineEnd);

    // get all concept terminology ids associated with the atoms in this concept
    final List<String> conceptTerminologyIds = new ArrayList<>();
    for (final Atom atom : concept.getAtoms()) {
      final String conceptTerminologyId =
          atom.getConceptTerminologyIds().get(concept.getTerminology());
      if (conceptTerminologyId != null && !conceptTerminologyId.equals("")
          && !conceptTerminologyIds.contains(conceptTerminologyId)) {
        conceptTerminologyIds.add(conceptTerminologyId);
      }
    }
    Collections.sort(conceptTerminologyIds);
    conceptTerminologyIds.remove(concept.getTerminologyId());

    sb.append("CUI ");
    sb.append(concept.getTerminologyId()).append("\t");
    sb.append("Concept Status is ")
        .append(getStatusChar(concept.getWorkflowStatus())).append("\r\n");
    for (final String id : conceptTerminologyIds) {
      sb.append(id).append(lineEnd);
    }

    //
    // Semantic Types
    //
    sb.append("STY ");
    for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
      sb.append(sty.getSemanticType()).append("\t");
      sb.append(getStatusChar(sty.getWorkflowStatus())).append(lineEnd);
    }

    //
    // Definitions
    //
    for (final Atom atom : concept.getAtoms()) {
      for (final Definition def : atom.getDefinitions()) {
        sb.append("DEF ");
        sb.append(def.isPublishable() ? "[Release] " : "[Do Not Release] ");
        sb.append(def.getTerminology()).append("_").append(def.getVersion())
            .append(lineEnd);
        sb.append("  - ").append(atom.getTerminology()).append("/")
            .append(atom.getTermType());
        sb.append("|")
            .append(WordUtils.wrap(def.getValue(), 65, "\r\n    ", false))
            .append(lineEnd);

      }
    }

    //
    // SOS
    //
    StringBuffer sosBuffer = new StringBuffer();
    String sosLabel = "SOS";
    sosBuffer.append(sosLabel);
    for (final Atom atom : concept.getAtoms()) {
      for (final Attribute att : atom.getAttributes()) {
        if (att.getName().equals("SOS")) {
          sosBuffer.append(
              att.isPublishable() ? " [Release] " : " [Do Not Release] ");
          sosBuffer.append(att.getTerminology()).append("_")
              .append(att.getVersion()).append(lineEnd);
          sosBuffer.append("  - ").append(atom.getTerminology()).append("/")
              .append(atom.getTermType());
          sosBuffer.append("|")
              .append(WordUtils.wrap(att.getValue(), 65, "\r\n    ", false))
              .append(lineEnd);
        }
      }
    }
    if (sosBuffer.toString().length() > sosLabel.length()) {
      sb.append(sosBuffer.toString());
    }

    //
    // Atoms
    //
    sb.append("ATOMS").append(lineEnd);

    String prev_lui = "";
    String prev_sui = "";

    for (final Atom atom : concept.getAtoms()) {

      //
      // Determine flags
      //
      // Depict flag "B" for RxNORM Base Ambiguity Atom.
      // does the atom have a releasable RXNORM indicate ABIGUITITY_FLAG=Base?
      boolean isBaseRxnormAmbiguous = atom.getAttributes().stream()
          .filter(a -> a.getName().equals("AMBIGUITY_FLAG")
              && a.getValue().equals("Base")
              && a.getTerminology().equals("RXNORM") && a.isPublishable())
          .collect(Collectors.toList()).size() > 0;

      sb.append(" ");

      if (atom.getWorkflowStatus() == WorkflowStatus.DEMOTION) {
        sb.append("<span class=\"DEMOTION\">");
      } else if (atom.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
        sb.append("<span class=\"NEEDS_REVIEW\">");
      } else if (!atom.isPublishable()) {
        sb.append("<span class=\"UNRELEASABLE\">");
      } else if (atom.isObsolete()) {
        sb.append("<span class=\"OBSOLETE\">");
      } else if (isBaseRxnormAmbiguous) {
        sb.append("<span class=\"RXNORM\">");
      } else {
        sb.append("<span>");
      }

      if (getStatusChar(atom.getWorkflowStatus()).equals("D")) {
        sb.append("D");
      } else {
        sb.append(" ");
      }

      if (atom.getLastModifiedBy().startsWith("ENG-")) {
        sb.append("M");
      } else {
        sb.append(" ");
      }

      if (atom.isObsolete()) {
        sb.append("O");
      } else if (atom.isSuppressible() && getTermType(atom.getTermType(),
          concept.getTerminology(), concept.getVersion()).isSuppressible()) {
        sb.append("Y");
      } else if (atom.isSuppressible() && !getTermType(atom.getTermType(),
          concept.getTerminology(), concept.getVersion()).isSuppressible()) {
        sb.append("E");
      } else {
        sb.append(" ");
      }

      // Depict flag "B" for RxNORM Base Ambiguity Atom.
      // does the atom have a releasable RXNORM indicate ABIGUITITY_FLAG=Base?
      if (isBaseRxnormAmbiguous) {
        sb.append("B");
      } else {
        sb.append(" ");
      }

      // Name ambiguous?
      PfsParameter pfs = new PfsParameterJpa();
      pfs.setStartIndex(0);
      pfs.setMaxResults(1);
      // NOTE: this may not be 100% accurate because of use of hash
      SearchResultList results = findConcepts(concept.getTerminology(),
          concept.getVersion(), Branch.ROOT, "atoms.lowerNameHash:"
              + atom.getLowerNameHash() + " AND NOT id:" + concept.getId(),
          pfs);
      if (results.getTotalCount() > 0) {
        sb.append("A");
      } else {
        sb.append(" ");
      }

      // Determine atom status
      sb.append(getStatusChar(atom.getWorkflowStatus())).append(" ");

      // Determine indentation level and new LUI tag ([])
      if (prev_lui.toString().equals(atom.getLexicalClassId())) {
        sb.append("    ");
        if (prev_sui.toString().equals(atom.getStringClassId())) {
          sb.append("    ");
        } else {
          sb.append("  ");
        }
      } else {
        sb.append(" []  ");
      }

      // Released?
      if (!atom.isPublishable()) {
        sb.append("{");
      }

      // Name/termgroup/code
      sb.append(atom.getName()).append(" [");
      sb.append(atom.getTerminology()).append("_").append(atom.getVersion())
          .append("/");
      sb.append(atom.getTermType()).append("/");
      sb.append(atom.getCodeId()).append("]");

      // Write MUI if ( MSH (or MSH translation) or NCI (or NCI subsources)).
      if (("MSH".equals(atom.getTerminology()) && atom.getConceptId() != null)
          || ("NCI".equals(atom.getTerminology())
              && atom.getConceptId() != null)) {
        sb.append(" ");
        sb.append(atom.getConceptId());
      }

      // Write RXCUI
      Attribute att = atom.getAttributeByName("RXCUI");
      if (att != null) {
        sb.append(" ");
        sb.append(att.getValue());
      }

      if (!atom.isPublishable()) {
        sb.append("}");
      }

      sb.append("</span>");
      sb.append(lineEnd);

      prev_lui = atom.getLexicalClassId();
      prev_sui = atom.getStringClassId();

    }
    sb.append(lineEnd);

    //
    // RELATIONSHIPS
    //

    List<Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relList =
        this.findConceptDeepRelationships(concept.getTerminologyId(),
            concept.getTerminology(), concept.getVersion(), Branch.ROOT, null,
            false, true, true, false, new PfsParameterJpa()).getObjects();

    // Lexical Relationships
    List<AtomRelationship> lexicalRelationships = new ArrayList<>();
    // double for loop over atoms and then each atom's relationships
    // additional relation types ends with form_of
    for (Atom atom : concept.getAtoms()) {
      for (AtomRelationship atomRel : atom.getRelationships()) {
        if (atomRel.getAdditionalRelationshipType().endsWith("form_of")) {
          lexicalRelationships.add(atomRel);
        }
      }
    }
    if (lexicalRelationships.size() > 0) {
      sb.append("LEXICAL RELATIONSHIP(S)").append(lineEnd);
      for (AtomRelationship rel : lexicalRelationships) {
        if (!rel.isPublishable()) {
          sb.append("{");
        }
        sb.append(rel.getFrom().getName()).append("[SFO]/[LFO]")
            .append(rel.getTo().getName());
        sb.append("[").append(rel.getTerminology()).append("_")
            .append(rel.getVersion()).append("]").append(lineEnd);
        if (!rel.isPublishable()) {
          sb.append("}");
        }
      }
    }

    // Demoted Related Concepts
    List<String> usedToIds = new ArrayList<>();
    List<ConceptRelationship> demotionRelationships = new ArrayList<>();
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB"))) {
        usedToIds.add(rel.getTo().getTerminologyId());
        demotionRelationships.add(rel);
      }
    }
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.DEMOTION
          && usedToIds.contains(rel.getTo().getTerminologyId())) {
        usedToIds.add(rel.getTo().getTerminologyId());
        demotionRelationships.add(rel);
      }
    }
    if (demotionRelationships.size() > 0) {
      sb.append("DEMOTED RELATED CONCEPT(S)").append(lineEnd);
      sb.append(getRelationshipsReport(demotionRelationships));
    }

    // Needs Review Related Concepts
    List<ConceptRelationship> needsReviewRelationships = new ArrayList<>();
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW
          && !usedToIds.contains(rel.getTo().getTerminologyId())) {
        usedToIds.add(rel.getTo().getTerminologyId());
        needsReviewRelationships.add(rel);
      }
    }
    if (needsReviewRelationships.size() > 0) {
      sb.append("NEEDS REVIEW RELATED CONCEPT(S)").append(lineEnd);
      sb.append(getRelationshipsReport(needsReviewRelationships));
    }

    // XR(S) and Corresponding Relationships
    List<ConceptRelationship> xrCorrespondingRelationships = new ArrayList<>();
    List<String> xrRelsToIds = new ArrayList<>();
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.NEEDS_REVIEW
          && rel.getRelationshipType().equals("XR")
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB"))
          && !usedToIds.contains(rel.getTo().getTerminologyId())) {
        // usedToIds.add(rel.getTo().getTerminologyId());
        xrRelsToIds.add(rel.getTo().getTerminologyId());
        xrCorrespondingRelationships.add(rel);
      }
    }
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      if (!rel.getRelationshipType().equals("XR")
          && !usedToIds.contains(rel.getTo().getTerminologyId())
          && xrRelsToIds.contains(rel.getTo().getTerminologyId())) {
        usedToIds.add(rel.getTo().getTerminologyId());
        xrCorrespondingRelationships.add(rel);
      }
    }
    if (xrCorrespondingRelationships.size() > 0) {
      sb.append("XR(S) AND CORRESPONDING RELATIONSHIP(S)").append(lineEnd);
      sb.append(getRelationshipsReport(xrCorrespondingRelationships));
    }

    // Reviewed Related Concepts
    List<ConceptRelationship> reviewedRelatedConcepts = new ArrayList<>();
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      int ct = 0;
      if ((rel.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
          || rel.getWorkflowStatus() == WorkflowStatus.PUBLISHED)
          && !usedToIds.contains(rel.getTo().getTerminologyId()) && ct < 20
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB"))) {
        usedToIds.add(rel.getTo().getTerminologyId());
        reviewedRelatedConcepts.add(rel);
        ct++;
      }
    }
    if (reviewedRelatedConcepts.size() > 0) {
      sb.append("REVIEWED RELATED CONCEPT(S)").append(lineEnd);
      sb.append(getRelationshipsReport(reviewedRelatedConcepts));
    }

    // Context Relationships
    List<ConceptRelationship> contextRelationships = new ArrayList<>();
    for (Relationship<?, ?> relationship : relList) {
      ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getRelationshipType().equals("PAR")
          || rel.getRelationshipType().equals("CHD")
          || rel.getRelationshipType().equals("SIB")) {
        contextRelationships.add(rel);
      }
    }
    if (contextRelationships.size() > 0) {
      sb.append("CONTEXT RELATIONSHIP(S)").append(lineEnd);
      sb.append(getRelationshipsReport(contextRelationships));
    }
    //
    // CONTEXTS
    //
    // TODO : deal with atom tree positions
    boolean firstContext = true;
    Set<String> uniqueSet = new HashSet<>();
    // collect all unique terminology, version, terminologyId, type combos from
    // atoms in concept
    for (Atom atom : concept.getAtoms()) {

      Terminology fullTerminology =
          getTerminology(atom.getTerminology(), atom.getVersion());
      IdType type = fullTerminology.getOrganizingClassType();
      String terminologyId = null;

      if (type == IdType.CODE) {
        terminologyId = atom.getCodeId();
      } else if (type == IdType.CONCEPT) {
        terminologyId = atom.getConceptId();
      } else if (type == IdType.DESCRIPTOR) {
        terminologyId = atom.getDescriptorId();
      } else {
        continue;
      }
      uniqueSet.add(type + ":" + atom.getTerminology() + ":" + atom.getVersion()
          + ":" + terminologyId);
    }

    // for each unique entry, get all tree positions
    List<TreePosition<?>> treePositionList = new ArrayList<>();
    PfsParameter singleResultPfs = new PfsParameterJpa();
    singleResultPfs.setStartIndex(0);
    singleResultPfs.setMaxResults(1);
    for (String entry : uniqueSet) {
      String[] fields = FieldedStringTokenizer.split(entry, ":");
      String type = fields[0];
      String terminology = fields[1];
      String version = fields[2];
      String terminologyId = fields[3];

      if (IdType.valueOf(type) == IdType.CONCEPT) {
        List<TreePosition<? extends ComponentHasAttributesAndName>> localPositions =
            findConceptTreePositions(terminologyId, terminology, version, null,
                null, singleResultPfs).getObjects();
        treePositionList.addAll(localPositions);
      } else if (IdType.valueOf(type) == IdType.DESCRIPTOR) {
        treePositionList.addAll(findDescriptorTreePositions(terminologyId,
            terminology, version, null, null, singleResultPfs).getObjects());
      } else if (IdType.valueOf(type) == IdType.CODE) {
        treePositionList.addAll(findConceptTreePositions(terminologyId,
            terminology, version, null, null, singleResultPfs).getObjects());
      }

    }

    // display context for each tree position
    for (TreePosition<?> treePos : treePositionList) {

      if (treePos.getAncestorPath().equals(""))
        continue;

      if (firstContext) {
        sb.append("CONTEXTS").append(lineEnd);
        firstContext = false;
      }

      sb.append(treePos.getNode().getTerminology()).append("_")
          .append(treePos.getNode().getVersion());
      sb.append("/").append(treePos.getNode().getTerminologyId())
          .append(lineEnd);

      Tree tree = getTreeForTreePosition(treePos);

      // ancestors
      indent = "";
      sb.append(tree.getNodeName()).append(lineEnd);
      indent += "  ";
      printAncestors(sb, tree);

      // children
      Terminology fullTerminology = getTerminology(
          treePos.getNode().getTerminology(), treePos.getNode().getVersion());
      IdType type = fullTerminology.getOrganizingClassType();

      TreePositionList children = null;
      TreePositionList siblings = null;
      PfsParameter childPfs = new PfsParameterJpa();
      childPfs.setStartIndex(0);
      childPfs.setMaxResults(10);
      if (type == IdType.CONCEPT) {
        children = this.findConceptTreePositionChildren(
            treePos.getNode().getTerminologyId(),
            treePos.getNode().getTerminology(), treePos.getNode().getVersion(),
            Branch.ROOT, childPfs);
        siblings = this.findConceptTreePositionChildren(
            parent.getNodeTerminologyId(), parent.getTerminology(),
            parent.getVersion(), Branch.ROOT, new PfsParameterJpa());
      } else if (type == IdType.CODE) {
        children = this.findCodeTreePositionChildren(
            treePos.getNode().getTerminologyId(),
            treePos.getNode().getTerminology(), treePos.getNode().getVersion(),
            Branch.ROOT, childPfs);
        siblings = this.findCodeTreePositionChildren(
            parent.getNodeTerminologyId(), parent.getTerminology(),
            parent.getVersion(), Branch.ROOT, new PfsParameterJpa());
      } else if (type == IdType.DESCRIPTOR) {
        children = this.findDescriptorTreePositionChildren(
            treePos.getNode().getTerminologyId(),
            treePos.getNode().getTerminology(), treePos.getNode().getVersion(),
            Branch.ROOT, childPfs);
        siblings = this.findDescriptorTreePositionChildren(
            parent.getNodeTerminologyId(), parent.getTerminology(),
            parent.getVersion(), Branch.ROOT, new PfsParameterJpa());
      }

      // siblings & self node
      indent = indent.substring(0, indent.length() - 2);
      Collections.sort(siblings.getObjects(), new TreePositionComparator());

      indent += "  ";
      for (TreePosition<?> siblingPosition : siblings.getObjects()) {
        sb.append(indent);
        if (siblingPosition.getNode().getName()
            .equals(treePos.getNode().getName())) {
          sb.append("*");
        }
        sb.append(siblingPosition.getNode().getName());
        if (siblingPosition.getNode().getName()
            .equals(treePos.getNode().getName())) {
          sb.append("*").append(lineEnd);

          // children
          indent += "  ";
          printChildren(sb, treePos, children);
          indent = indent.substring(0, indent.length() - 2);
        } else if (siblingPosition.getChildCt() > 0) {
          sb.append(" +").append(lineEnd);
        } else {
          sb.append(lineEnd);
        }
      }
      sb.append(lineEnd);
    }

    return sb.toString();
  }

  /**
   * Comparator for sorting tree positions alphabetically by name
   */
  class TreePositionComparator implements Comparator<TreePosition<?>> {
    /* see superclass */
    @Override
    public int compare(TreePosition<?> object1, TreePosition<?> object2) {
      return object1.getNode().getName().compareTo(object2.getNode().getName());
    }
  }

  /**
   * Prints the children.
   *
   * @param sb the sb
   * @param treePos the tree pos
   * @param children the children
   */
  private void printChildren(StringBuilder sb, TreePosition<?> treePos,
    TreePositionList children) {
    Collections.sort(children.getObjects(), new TreePositionComparator());
    for (TreePosition<? extends ComponentHasAttributesAndName> childPosition : children
        .getObjects()) {
      sb.append(indent).append(childPosition.getNode().getName());
      if (childPosition.getChildCt() > 0) {
        sb.append(" +");
      }
      sb.append(lineEnd);
    }
    if (treePos.getChildCt() > 10) {
      sb.append(indent).append("more...").append(lineEnd);
    }
  }

  /**
   * @param sb
   * @param tree
   */
  private void printAncestors(StringBuilder sb, Tree tree) {
    for (Tree child : tree.getChildren()) {

      parent = tree;
      if (child.getChildren().size() != 0) {
        sb.append(indent);
        sb.append(child.getNodeName());
        sb.append(lineEnd);
      } else {
        return;
      }

      indent += "  ";
      printAncestors(sb, child);

    }
  }

  /**
   * Returns the status char.
   *
   * @param status the status
   * @return the status char
   */
  @SuppressWarnings("static-method")
  private String getStatusChar(WorkflowStatus status) {
    if (status == WorkflowStatus.NEEDS_REVIEW) {
      return "N";
    } else if (status == WorkflowStatus.DEMOTION) {
      return "D";
    } else {
      return "R";
    }
  }

  @Override
  public String getDescriptorReport(Project project, Descriptor descriptor)
    throws Exception {
    // TODO: factor out getconceptReport into getComponentReport, have it take
    // an AtomClass and do most of what it does, except with slightly different
    // behavior for concepts (e.g. "get deep relationships", etc).
    return "TBD";
  }

  @Override
  public String getCodeReport(Project project, Code code) throws Exception {
    // TODO:
    return "TBD";
  }

  /**
   * Returns the relationships report.
   *
   * @param rels the rels
   * @return the relationships report
   */
  private String getRelationshipsReport(List<ConceptRelationship> rels) {
    StringBuffer sb = new StringBuffer();
    for (ConceptRelationship rel : rels) {
      // relationship type
      sb.append("[").append(rel.getRelationshipType()).append("]  ");

      // Released?
      if (!rel.isPublishable()) {
        sb.append("{");
      }

      // Name/termgroup/code
      sb.append(rel.getTo().getName()).append(" [");
      /*
       * TODO NE-143 sb.append(" [");
       * sb.append(rel.getTo().getTerminology()).append("_").append(rel.
       * getVersion()) .append("/"); // TODO termType
       */
      sb.append("|");
      sb.append(rel.getAdditionalRelationshipType());
      sb.append("|");
      sb.append(rel.getTerminology()).append("_").append(rel.getVersion());
      sb.append("|");
      sb.append(rel.getLastModifiedBy());
      sb.append("]");

      sb.append(" {");
      sb.append(rel.getTo().getId());
      sb.append("}");

      // Print relationship_level
      if (rel.getTerminology().equals(rel.getFrom().getTerminology())) {
        sb.append(" C");
      } else if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION) {
        sb.append(" P");
      } else {
        sb.append(" S");
      }

      if (!rel.isPublishable()) {
        sb.append("}");
      }
      /*
       * OUT OF SCOPE if (rels.isWeaklyUnreleasable()) { sb.append(" n"); } else
       * if (rels.isUnreleasable()) { sb.append(" NEVER"); } }
       */
      sb.append(lineEnd);
    }
    return sb.append(lineEnd).toString();
  }
}