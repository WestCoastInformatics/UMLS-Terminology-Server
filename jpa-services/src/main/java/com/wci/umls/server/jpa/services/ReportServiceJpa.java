/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.services.handlers.RrfComputePreferredNameHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
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
 */
public class ReportServiceJpa extends HistoryServiceJpa
    implements ReportService {

  /** The line end. */
  private String lineEnd = "\r\n";

  /**
   * Instantiates an empty {@link ReportServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ReportServiceJpa() throws Exception {
    super();

  }

  /**
   * Helper function for actually generating a report.
   *
   * @param project the project
   * @param comp the concept
   * @param list the list
   * @param decorate the decorate
   * @return the report helper
   * @throws Exception the exception
   */
  private String getReportHelper(Project project, AtomClass comp,
    PrecedenceList list, boolean decorate) throws Exception {

    final StringBuilder sb = new StringBuilder();
    Tree parent = null;
    String indent = "";

    // For concept-specific things, cast it if so
    final Concept concept = (comp instanceof Concept) ? ((Concept) comp) : null;

    //
    // Handle validation/integrity checks
    //
    if (project != null && concept != null) {
      final ValidationResult validationResult =
          validateConcept(project, concept);
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
    sb.append(comp.getId()).append(" ");
    sb.append(comp.getName()).append(lineEnd);

    // get all concept terminology ids associated with the atoms in this concept
    final List<String> conceptTerminologyIds = new ArrayList<>();
    for (final Atom atom : comp.getAtoms()) {
      final String conceptTerminologyId =
          atom.getConceptTerminologyIds().get(comp.getTerminology());
      if (conceptTerminologyId != null && !conceptTerminologyId.equals("")
          && !conceptTerminologyIds.contains(conceptTerminologyId)) {
        conceptTerminologyIds.add(conceptTerminologyId);
      }
    }
    Collections.sort(conceptTerminologyIds);
    conceptTerminologyIds.remove(comp.getTerminologyId());

    sb.append(getOpenStyleTag(comp.getWorkflowStatus(), comp.isPublishable(),
        comp.isObsolete(), false, decorate));
    sb.append("CUI ");
    sb.append(comp.getTerminologyId()).append("\t");
    sb.append("Concept Status is ")
        .append(getStatusChar(comp.getWorkflowStatus())).append("\r\n");
    sb.append(getCloseStyleTag(comp.getWorkflowStatus(), comp.isPublishable(),
        comp.isObsolete(), false, decorate));
    for (final String id : conceptTerminologyIds) {
      sb.append(id).append(lineEnd);
    }

    //
    // Semantic Types
    //
    if (concept != null) {
      sb.append("STY ");
      boolean first = true;
      for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
        sb.append(getOpenStyleTag(sty.getWorkflowStatus(), sty.isPublishable(),
            sty.isObsolete(), false, decorate));
        if (!first) {
          sb.append("    ");
        }
        first = false;
        sb.append(sty.getSemanticType()).append("\t");
        sb.append(getStatusChar(sty.getWorkflowStatus())).append(lineEnd);
        sb.append(getCloseStyleTag(sty.getWorkflowStatus(), false, false, false,
            decorate));
      }
    }
    //
    // Definitions
    //
    for (final Atom atom : comp.getAtoms()) {
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
    final StringBuffer sosBuffer = new StringBuffer();
    final String sosLabel = "SOS";
    sosBuffer.append(sosLabel);
    for (final Atom atom : comp.getAtoms()) {
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

    // Determine ambiguous atoms
    final Set<Long> ambiguousAtomIds = concept == null ? new HashSet<>()
        : new HashSet<>(getAmbiguousAtomIds(concept));

    sb.append("ATOMS").append(lineEnd);

    String prev_lui = "";
    String prev_sui = "";

    final List<Atom> sortedAtoms = new ArrayList<>(comp.getAtoms());
    if (concept != null) {
      Collections.sort(sortedAtoms, new ReportsAtomComparator(concept, list));
    }

    for (final Atom atom : sortedAtoms) {

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

      sb.append(getOpenStyleTag(atom.getWorkflowStatus(), atom.isPublishable(),
          atom.isObsolete(), isBaseRxnormAmbiguous, decorate));

      if (atom.getWorkflowStatus() == WorkflowStatus.DEMOTION) {
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
          comp.getTerminology(), comp.getVersion()).isSuppressible()) {
        sb.append("Y");
      } else if (atom.isSuppressible() && !getTermType(atom.getTermType(),
          comp.getTerminology(), comp.getVersion()).isSuppressible()) {
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
      if (ambiguousAtomIds.contains(atom.getId())) {
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

      // Write the SCUI
      if (atom.getConceptId() != null) {
        sb.append(" ");
        sb.append(atom.getConceptId());
      }

      // Write RXCUI
      final Attribute att = atom.getAttributeByName("RXCUI");
      if (att != null) {
        sb.append(" ");
        sb.append(att.getValue());
      }

      // Write UMLSCUI
      final Attribute umlscui = atom.getAttributeByName("UMLSCUI");
      if (umlscui != null) {
        sb.append(" ");
        sb.append(umlscui.getValue());
      }

      if (!atom.isPublishable()) {
        sb.append("}");
      }

      sb.append(getCloseStyleTag(atom.getWorkflowStatus(), atom.isPublishable(),
          atom.isObsolete(), isBaseRxnormAmbiguous, decorate));
      sb.append(lineEnd);

      prev_lui = atom.getLexicalClassId();
      prev_sui = atom.getStringClassId();

    }
    sb.append(lineEnd);

    //
    // RELATIONSHIPS
    //

    List<Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relList =
        new ArrayList<>(0);
    // Handle concept rels
    if (concept != null) {
      relList = findConceptDeepRelationships(concept.getTerminologyId(),
          concept.getTerminology(), concept.getVersion(), Branch.ROOT, null,
          false, true, true, false, new PfsParameterJpa()).getObjects();
    }

    // Handle descriptor rels
    if (comp instanceof Descriptor) {
      relList = findDescriptorRelationships(comp.getTerminologyId(),
          comp.getTerminology(), comp.getVersion(), Branch.ROOT, null, false,
          null).getObjects();
    }

    // Handle code rels
    if (comp instanceof Code) {
      relList =
          findCodeRelationships(comp.getTerminologyId(), comp.getTerminology(),
              comp.getVersion(), Branch.ROOT, null, false, null).getObjects();
    }

    // Lexical Relationships
    final List<AtomRelationship> lexicalRelationships = new ArrayList<>();
    // double for loop over atoms and then each atom's relationships
    // additional relation types ends with form_of
    for (final Atom atom : comp.getAtoms()) {
      for (final AtomRelationship atomRel : atom.getRelationships()) {
        if (atomRel.getAdditionalRelationshipType().endsWith("form_of")) {
          lexicalRelationships.add(atomRel);
        }
      }
    }
    if (lexicalRelationships.size() > 0) {
      sb.append("LEXICAL RELATIONSHIP(S)").append(lineEnd);
      for (final AtomRelationship rel : lexicalRelationships) {
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
      sb.append(lineEnd);
    }

    // Demoted Related Concepts
    final List<String> usedToIds = new ArrayList<>();
    final List<ConceptRelationship> demotionRelationships = new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB"))) {
        usedToIds.add(rel.getTo().getTerminologyId());
        demotionRelationships.add(rel);
      }
    }
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.DEMOTION
          && usedToIds.contains(rel.getTo().getTerminologyId())) {
        usedToIds.add(rel.getTo().getTerminologyId());
        demotionRelationships.add(rel);
      }
    }
    if (demotionRelationships.size() > 0) {
      sb.append("DEMOTED RELATED CONCEPT(S)").append(lineEnd);
      sb.append(getOpenStyleTag(WorkflowStatus.DEMOTION, false, false, false,
          decorate));
      sb.append(getRelationshipsReport(demotionRelationships));
      sb.append(getCloseStyleTag(WorkflowStatus.DEMOTION, false, false, false,
          decorate));
    }

    // Needs Review Related Concepts
    final List<ConceptRelationship> needsReviewRelationships =
        new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW
          && !usedToIds.contains(rel.getTo().getTerminologyId())) {
        usedToIds.add(rel.getTo().getTerminologyId());
        needsReviewRelationships.add(rel);
      }
    }
    if (needsReviewRelationships.size() > 0) {
      sb.append("NEEDS REVIEW RELATED CONCEPT(S)").append(lineEnd);
      sb.append(getOpenStyleTag(WorkflowStatus.NEEDS_REVIEW, false, false,
          false, decorate));
      sb.append(getRelationshipsReport(needsReviewRelationships));
      sb.append(getCloseStyleTag(WorkflowStatus.NEEDS_REVIEW, false, false,
          false, decorate));
    }

    // XR(S) and Corresponding Relationships
    final List<ConceptRelationship> xrCorrespondingRelationships =
        new ArrayList<>();
    final List<String> xrRelsToIds = new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.NEEDS_REVIEW
          && rel.getRelationshipType().equals("XR")
          && !usedToIds.contains(rel.getTo().getTerminologyId())) {
        // usedToIds.add(rel.getTo().getTerminologyId());
        xrRelsToIds.add(rel.getTo().getTerminologyId());
        xrCorrespondingRelationships.add(rel);
      }
    }
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
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
    final List<ConceptRelationship> reviewedRelatedConcepts = new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      int ct = 0;
      if ((rel.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
          || rel.getWorkflowStatus() == WorkflowStatus.PUBLISHED)
          && !usedToIds.contains(rel.getTo().getTerminologyId()) && ct < 20
          // Exclude these rel types
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB")
              || rel.getRelationshipType().equals("AQ")
              || rel.getRelationshipType().equals("QB"))) {
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
    final List<ConceptRelationship> contextRelationships = new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
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
    final Set<String> uniqueSet = new HashSet<>();
    final List<TreePosition<?>> treePositionList = new ArrayList<>();
    // collect all unique terminology, version, terminologyId, type combos from
    // atoms in concept - ATOMS are in order - just pick first 10
    int ct = 0;
    for (final Atom atom : comp.getAtoms()) {

      final Terminology fullTerminology =
          getTerminology(atom.getTerminology(), atom.getVersion());
      final IdType type = fullTerminology.getOrganizingClassType();
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
      final String entry = type + ":" + atom.getTerminology() + ":"
          + atom.getVersion() + ":" + terminologyId;
      // If new entry
      if (!uniqueSet.contains(entry)) {
        // Break if we've reached the limit
        if (ct >= 10) {
          break;
        }

        // See if there is a tree position
        final TreePosition<?> treePos = getTreePosition(type, terminologyId,
            atom.getTerminology(), atom.getVersion());
        // Increment if so
        if (treePos != null) {
          ++ct;
          treePositionList.add(treePos);
        }

        // Get tree position
      }
      uniqueSet.add(entry);
    }

    // Sort tree positions by terminology
    Collections.sort(treePositionList,
        (t1, t2) -> t1.getTerminology().compareTo(t2.getTerminology()));

    // display context for each tree position
    for (final TreePosition<?> treePos : treePositionList) {

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

      final Tree tree = getTreeForTreePosition(treePos);

      // ancestors
      indent = "";
      sb.append(tree.getNodeName()).append(lineEnd);
      indent += "  ";
      indent = printAncestors(sb, tree, null, indent);
      // "parent" is the tree position above the bottom one
      parent = tree;
      while (parent.getChildren().size() > 0) {
        if (parent.getChildren().get(0).getChildren().size() > 0) {
          parent = parent.getChildren().get(0);
        } else {
          break;
        }
      }

      // children
      final Terminology fullTerminology = getTerminology(
          treePos.getNode().getTerminology(), treePos.getNode().getVersion());
      final IdType type = fullTerminology.getOrganizingClassType();

      TreePositionList siblings = null;
      TreePositionList children = null;
      final PfsParameter childPfs = new PfsParameterJpa();
      childPfs.setStartIndex(0);
      childPfs.setMaxResults(10);
      if (type == IdType.CONCEPT) {
        if (treePos.getChildCt() > 0) {
          children = findConceptTreePositionChildren(
              treePos.getNode().getTerminologyId(),
              treePos.getNode().getTerminology(),
              treePos.getNode().getVersion(), Branch.ROOT, childPfs);
        } else {
          children = new TreePositionListJpa();
        }
        siblings = findConceptTreePositionChildren(
            parent.getNodeTerminologyId(), parent.getTerminology(),
            parent.getVersion(), Branch.ROOT, new PfsParameterJpa());
      } else if (type == IdType.CODE) {
        if (treePos.getChildCt() > 0) {
          children =
              findCodeTreePositionChildren(treePos.getNode().getTerminologyId(),
                  treePos.getNode().getTerminology(),
                  treePos.getNode().getVersion(), Branch.ROOT, childPfs);
        } else {
          children = new TreePositionListJpa();
        }
        siblings = findCodeTreePositionChildren(parent.getNodeTerminologyId(),
            parent.getTerminology(), parent.getVersion(), Branch.ROOT,
            new PfsParameterJpa());
      } else if (type == IdType.DESCRIPTOR) {
        if (treePos.getChildCt() > 0) {
          children = findDescriptorTreePositionChildren(
              treePos.getNode().getTerminologyId(),
              treePos.getNode().getTerminology(),
              treePos.getNode().getVersion(), Branch.ROOT, childPfs);
        } else {
          children = new TreePositionListJpa();
        }
        siblings = findDescriptorTreePositionChildren(
            parent.getNodeTerminologyId(), parent.getTerminology(),
            parent.getVersion(), Branch.ROOT, new PfsParameterJpa());
      } else {
        throw new Exception("Unexpected it type - " + type);
      }

      // siblings & self node
      indent = indent.substring(0, indent.length() - 2);
      Collections.sort(siblings.getObjects(),
          (t1, t2) -> t1.getNode().getName().compareTo(t2.getNode().getName()));

      indent += "  ";
      for (TreePosition<?> siblingPosition : siblings.getObjects()) {
        sb.append(indent);
        if (siblingPosition.getNode().getName()
            .equals(treePos.getNode().getName())) {
          sb.append("<b>");
        }
        sb.append(siblingPosition.getNode().getName());
        if (siblingPosition.getNode().getName()
            .equals(treePos.getNode().getName())) {
          sb.append("</b>").append(lineEnd);

          // children
          indent += "  ";
          printChildren(sb, treePos, children, indent);
          if (children.getTotalCount() > 10) {
            sb.append(indent)
                .append("..." + (children.getTotalCount() - 10) + " more ...");
          }
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
   * Prints the children.
   *
   * @param sb the sb
   * @param treePos the tree pos
   * @param children the children
   * @param indent the indent
   */
  private void printChildren(StringBuilder sb, TreePosition<?> treePos,
    TreePositionList children, String indent) {
    Collections.sort(children.getObjects(),
        (t1, t2) -> t1.getNode().getName().compareTo(t2.getNode().getName()));
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
   * Returns the tree position.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the tree position
   * @throws Exception the exception
   */
  private TreePosition<?> getTreePosition(IdType type, String terminologyId,
    String terminology, String version) throws Exception {
    // for each unique entry, get all tree positions
    final PfsParameter singleResultPfs = new PfsParameterJpa();
    singleResultPfs.setStartIndex(0);
    singleResultPfs.setMaxResults(1);

    TreePositionList list = null;
    if (type == IdType.CONCEPT) {
      list = findConceptTreePositions(terminologyId, terminology, version, null,
          null, singleResultPfs);
    } else if (type == IdType.DESCRIPTOR) {
      list = findDescriptorTreePositions(terminologyId, terminology, version,
          null, null, singleResultPfs);
    } else if (type == IdType.CODE) {
      list = findConceptTreePositions(terminologyId, terminology, version, null,
          null, singleResultPfs);
    }
    if (list.size() > 0) {
      return list.getObjects().get(0);
    }
    return null;

  }

  /**
   * Prints the ancestors.
   *
   * @param sb the sb
   * @param tree the tree
   * @param parent the parent
   * @param indent the indent
   * @return the string
   */
  private String printAncestors(StringBuilder sb, Tree tree, Tree parent,
    String indent) {
    Tree ancestor = parent;
    for (Tree child : tree.getChildren()) {

      ancestor = tree;
      if (child.getChildren().size() != 0) {
        sb.append(indent);
        sb.append(child.getNodeName());
        sb.append(lineEnd);
      } else {
        return indent;
      }

      return printAncestors(sb, child, ancestor, indent + "  ");

    }
    return indent;
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
      return "N";
    } else {
      return "R";
    }
  }

  /* see superclass */
  @Override
  public String getConceptReport(Project project, Concept concept,
    PrecedenceList list, boolean decorate) throws Exception {
    return getReportHelper(project, concept, list, decorate);
  }

  /**
   * Returns the descriptor report.
   *
   * @param project the project
   * @param descriptor the descriptor
   * @param list the list
   * @param decorate the decorate
   * @return the descriptor report
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public String getDescriptorReport(Project project, Descriptor descriptor,
    PrecedenceList list, boolean decorate) throws Exception {
    return getReportHelper(project, descriptor, list, decorate);
  }

  /* see superclass */
  @Override
  public String getCodeReport(Project project, Code code, PrecedenceList list,
    boolean decorate) throws Exception {
    return getReportHelper(project, code, list, decorate);
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
       * getVersion()) .append("/"); // TODO termType - only ifneeded
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
      if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION) {
        sb.append(" P");
      } else if (rel.getTerminology().equals(rel.getFrom().getTerminology())) {
        sb.append(" C");
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

  /**
   * Returns the open style tag.
   *
   * @param status the status
   * @param publishable the publishable
   * @param obsolete the obsolete
   * @param orangeFlag the orange flag
   * @param decorate the decorate
   * @return the open style tag
   */
  @SuppressWarnings("static-method")
  public String getOpenStyleTag(WorkflowStatus status, boolean publishable,
    boolean obsolete, boolean orangeFlag, boolean decorate) {
    if (!decorate) {
      return "";
    }
    if (status == WorkflowStatus.DEMOTION) {
      return "<span class=\"DEMOTION\">";
    } else if (status == WorkflowStatus.NEEDS_REVIEW) {
      return "<span class=\"NEEDS_REVIEW\">";
    } else if (!publishable) {
      return "<span class=\"UNRELEASABLE\">";
    } else if (obsolete) {
      return "<span class=\"OBSOLETE\">";
    } else if (orangeFlag) {
      return "<span class=\"RXNORM\">";
    }
    return "";
  }

  /**
   * Returns the close style tag.
   *
   * @param status the status
   * @param publishable the publishable
   * @param obsolete the obsolete
   * @param orangeFlag the orange flag
   * @param decorate the decorate
   * @return the close style tag
   */
  @SuppressWarnings("static-method")
  public String getCloseStyleTag(WorkflowStatus status, boolean publishable,
    boolean obsolete, boolean orangeFlag, boolean decorate) {
    if (!decorate) {
      return "";
    }
    if (status == WorkflowStatus.DEMOTION) {
      return "</span>";
    } else if (status == WorkflowStatus.NEEDS_REVIEW) {
      return "</span>";
    } else if (!publishable) {
      return "</span>";
    } else if (obsolete) {
      return "</span>";
    } else if (orangeFlag) {
      return "</span>";
    }
    return "";
  }

  /**
   * LexicalClass/StringClass comparator for report.
   */
  class ReportsAtomComparator implements Comparator<Atom> {

    /** The lui ranks. */
    private Map<String, String> luiRanks = new HashMap<>();

    /** The sui ranks. */
    private Map<String, String> suiRanks = new HashMap<>();

    /** The atom ranks. */
    private Map<Long, String> atomRanks = new HashMap<>();

    /**
     * Instantiates a {@link ReportsAtomComparator} from the specified
     * parameters.
     *
     * @param concept the concept
     * @param list the list
     * @throws Exception the exception
     */
    public ReportsAtomComparator(Concept concept, PrecedenceList list)
        throws Exception {

      // Set up vars
      String lui = null;
      String sui = null;
      String rank = null;
      String luiRank = null;
      String suiRank = null;

      // Configure rank handler
      RrfComputePreferredNameHandler handler =
          new RrfComputePreferredNameHandler();
      handler.cacheList(list);

      // Get default atom ordering
      final List<Atom> atoms = concept.getAtoms();

      // Iterate through atoms, maintaning the luiRanks and suiRanks maps
      for (final Atom atom : atoms) {

        // Get initial values
        lui = atom.getLexicalClassId();
        sui = atom.getStringClassId();
        rank = handler.getRank(atom, list);
        atomRanks.put(atom.getId(), rank);

        // Look up that atom's lui
        luiRank = luiRanks.get(lui);
        if (luiRank == null) {
          // Add the current atom's lui and rank to the hashmap.
          luiRanks.put(lui, rank);
        }

        // Compare the rank returned with the current rank
        // and determine which rank is higher.
        else if (rank.compareTo(luiRank) > 0) {
          // if the current atom's rank is higher than the one in the hashmap,
          // then replace it.
          luiRanks.put(lui, rank);

          // Look up that atom's sui in suiRanks
        }
        suiRank = suiRanks.get(sui);
        if (suiRank == null) {
          // Add the current atom's sui and rank to the hashmap.
          suiRanks.put(sui, rank);
        }

        // Compare the rank returned with the current rank
        // and determine which rank is higher.
        else if (rank.compareTo(suiRank) > 0) {
          // if the current atom's rank is higher than the one in the hashmap,
          // then replace it.
          suiRanks.put(sui, rank);
        }

      } // end for
    }

    /* see superclass */
    @Override
    public int compare(Atom a1, Atom a2) {

      // Reverse sort -> return the higher value first

      // Compare LUI ranks first
      if (!a1.getLexicalClassId().equals(a2.getLexicalClassId())) {
        String l2 = luiRanks.get(a2.getLexicalClassId());
        return l2.compareTo(luiRanks.get(a1.getLexicalClassId()));
      }

      // Compare SUI ranks second
      if (!a1.getStringClassId().equals(a2.getStringClassId())) {
        String s2 = suiRanks.get(a2.getStringClassId());
        return s2.compareTo(suiRanks.get(a1.getStringClassId()));
      }

      // If things are STILL equal, compare the ranks
      return atomRanks.get(a2.getId()).compareTo(atomRanks.get(a1.getId()));
    }
  }

}