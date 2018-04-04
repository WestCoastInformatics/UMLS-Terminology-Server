/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasTerminology;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.report.ReportJpa;
import com.wci.umls.server.jpa.report.ReportListJpa;
import com.wci.umls.server.jpa.report.ReportResultItemJpa;
import com.wci.umls.server.jpa.report.ReportResultJpa;
import com.wci.umls.server.jpa.services.helper.ReportsAtomComparator;
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
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportList;
import com.wci.umls.server.model.report.ReportResult;
import com.wci.umls.server.model.report.ReportResultItem;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.ReportService;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * JPA and JAXB enabled implementation of {@link HistoryService}.
 */
public class ReportServiceJpa extends HistoryServiceJpa
    implements ReportService {

  /** The line end. */
  private final String lineEnd = "\r\n";

  /** The concept contexts cache. */
  private static final Map<Long, String> conceptContextsCache =
      Collections.synchronizedMap(new LinkedHashMap<Long, String>(50) {
        private static final long serialVersionUID = 2546245625L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
          return size() > 50;
        }
      });

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
          validateConcept(project.getValidationChecks(), concept);
      sb.append("As of ");
      sb.append(new Date());
      if (validationResult.getWarnings().size() > 0
          || validationResult.getErrors().size() > 0) {
        sb.append(", this entry has the following problems/issues: \n");
        for (String warning : validationResult.getWarnings()) {
          sb.append(warning).append("\n");
        }
        for (String error : validationResult.getErrors()) {
          sb.append(error).append("\n");
        }
      } else {
        sb.append(", this entry had no problems/issues.");
      }
      sb.append("\r\n");
    }

    sb.append(
        "...............................................................................");

    //
    // Component information
    //
    sb.append(lineEnd).append("CN# ");
    sb.append(comp.getId()).append(" ");
    sb.append(handleHtmlSymbols(comp.getName())).append(lineEnd);

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
    sb.append(comp.getTerminologyId().equals(comp.getId().toString())
        ? "        " : comp.getTerminologyId()).append("\t");
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
    // Sort atoms
    //
    final List<Atom> sortedAtoms = new ArrayList<>(comp.getAtoms());
    if (concept != null) {
      Collections.sort(sortedAtoms, new ReportsAtomComparator(concept, list));
    }

    //
    // Definitions
    // TODO: need to look for definitions on descriptor,scui, etc.
    //
    for (final Atom atom : sortedAtoms) {
      for (final Definition def : atom.getDefinitions()) {
        sb.append("DEF ");
        sb.append(def.isPublishable() ? "[Release] " : "[Do Not Release] ");
        sb.append(getTerminologyAndVersion(def)).append(lineEnd);
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
    final StringBuilder sosBuffer = new StringBuilder();
    final String sosLabel = "SOS";
    sosBuffer.append(sosLabel);
    for (final Atom atom : sortedAtoms) {
      for (final Attribute att : atom.getAttributes()) {
        if (att.getName().equals("SOS")) {
          sosBuffer.append(
              att.isPublishable() ? " [Release] " : " [Do Not Release] ");
          sosBuffer.append(getTerminologyAndVersion(att)).append(lineEnd);
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

    sb.append(lineEnd).append("ATOMS").append(lineEnd);

    String prev_lui = "";
    String prev_sui = "";

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
      sb.append(handleHtmlSymbols(atom.getName())).append(" [");
      sb.append(getTerminologyAndVersion(atom)).append("/");
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
    // Notes
    //
    StringBuilder notesBuffer = new StringBuilder();
    String notesLabel = "CONCEPT NOTE(S)";
    notesBuffer.append(notesLabel);
    for (final Note note : concept.getNotes()) {
      notesBuffer.append(lineEnd)
          .append(
              WordUtils.wrap(
                  "  - " + note.getLastModifiedBy() + "/"
                      + note.getLastModified() + "  " + note.getNote(),
                  65, "\r\n    ", true));
    }
    if (notesBuffer.toString().length() > notesLabel.length()) {
      sb.append(notesBuffer.toString());
      sb.append(lineEnd);
    }

    notesBuffer = new StringBuilder();
    notesLabel = "ATOM NOTE(S)";
    notesBuffer.append(notesLabel);
    for (final Atom atom : concept.getAtoms()) {
      for (final Note note : atom.getNotes()) {
        notesBuffer.append(lineEnd)
            .append(
                WordUtils.wrap(
                    "  - " + note.getLastModifiedBy() + "/"
                        + note.getLastModified() + "  " + note.getNote(),
                    65, "\r\n    ", true));
      }
    }
    if (notesBuffer.toString().length() > notesLabel.length()) {
      sb.append(notesBuffer.toString());
      sb.append(lineEnd);
    }
    sb.append(lineEnd);

    //
    // RELATIONSHIPS
    //

    List<Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relList =
        new ArrayList<>(0);
    // Handle concept rels
    if (concept != null) {
      // For concept relationships, sort by relationshipType
      // This is a bit of a hack to ensure that Bequeathal rels take precedence over other rel types
      PfsParameter pfs = new PfsParameterJpa();
      pfs.setAscending(true);
      pfs.setSortField("relationshipType");
      relList = findConceptDeepRelationships(concept.getTerminologyId(),
          concept.getTerminology(), concept.getVersion(), Branch.ROOT, null,
          true, true, false, false, pfs).getObjects();
    }

    // Handle descriptor rels
    if (comp instanceof Descriptor) {
      relList = findDescriptorRelationships(comp.getTerminologyId(),
          comp.getTerminology(), comp.getVersion(), Branch.ROOT, null, true,
          null).getObjects();
    }

    // Handle code rels
    if (comp instanceof Code) {
      relList =
          findCodeRelationships(comp.getTerminologyId(), comp.getTerminology(),
              comp.getVersion(), Branch.ROOT, null, true, null).getObjects();
    }

    // Lexical Relationships
    final List<AtomRelationship> lexicalRelationships = new ArrayList<>();
    // double for loop over atoms and then each atom's relationships
    // additional relation types ends with form_of
    for (final Atom atom : comp.getAtoms()) {
      for (final AtomRelationship atomRel : atom.getRelationships()) {
        if (atomRel.isShortFormLongForm()) {
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
        sb.append(handleHtmlSymbols(rel.getFrom().getName()))
            .append("[SFO]/[LFO]")
            .append(handleHtmlSymbols(rel.getTo().getName()));
        sb.append("[").append(getTerminologyAndVersion(rel)).append("]")
            .append(lineEnd);
        if (!rel.isPublishable()) {
          sb.append("}");
        }
      }
      sb.append(lineEnd);
    }

    // Demoted Related Concepts
    final List<String> usedFromIds = new ArrayList<>();
    final List<ConceptRelationship> demotionRelationships = new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB"))) {
        usedFromIds.add(rel.getFrom().getTerminologyId());
        demotionRelationships.add(rel);
      }
    }
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.DEMOTION
          && usedFromIds.contains(rel.getFrom().getTerminologyId())) {
        usedFromIds.add(rel.getFrom().getTerminologyId());
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
          && !usedFromIds.contains(rel.getFrom().getTerminologyId())) {
        usedFromIds.add(rel.getFrom().getTerminologyId());
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
    final List<String> xrRelsFromIds = new ArrayList<>();
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.NEEDS_REVIEW
          && rel.getRelationshipType().equals("XR")
          && !usedFromIds.contains(rel.getFrom().getTerminologyId())) {
        // usedToIds.add(rel.getTo().getTerminologyId());
        xrRelsFromIds.add(rel.getFrom().getTerminologyId());
        xrCorrespondingRelationships.add(rel);
      }
    }
    for (final Relationship<?, ?> relationship : relList) {
      final ConceptRelationship rel = (ConceptRelationship) relationship;
      if (!rel.getRelationshipType().equals("XR")
          && !usedFromIds.contains(rel.getFrom().getTerminologyId())
          && xrRelsFromIds.contains(rel.getFrom().getTerminologyId())) {
        usedFromIds.add(rel.getFrom().getTerminologyId());
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
          && !usedFromIds.contains(rel.getFrom().getTerminologyId()) && ct < 20
          // Exclude these rel types
          && !(rel.getRelationshipType().equals("PAR")
              || rel.getRelationshipType().equals("CHD")
              || rel.getRelationshipType().equals("SIB")
              || rel.getRelationshipType().equals("XR")
              || rel.getRelationshipType().equals("AQ")
              || rel.getRelationshipType().equals("QB"))) {
        usedFromIds.add(rel.getFrom().getTerminologyId());
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

    // Check cache
    final String contexts = getCachedContexts(concept.getId());
    if (contexts != null) {
      sb.append(contexts);
    }

    else {

      final StringBuilder cxtBuilder = new StringBuilder();
      boolean firstContext = true;

      final TreePositionList treePositionList = findConceptDeepTreePositions(
          concept.getTerminologyId(), concept.getTerminology(),
          concept.getVersion(), Branch.ROOT, null, new PfsParameterJpa());

      // display context for each tree position
      for (final TreePosition<?> treePos : treePositionList.getObjects()) {

        // Write header
        if (firstContext) {
          cxtBuilder.append("CONTEXTS").append(lineEnd);
          firstContext = false;
        }

        cxtBuilder.append(getTerminologyAndVersion(treePos.getNode()));
        cxtBuilder.append("/");

        if (treePos.getNode() instanceof Atom) {
          cxtBuilder.append(((Atom) treePos.getNode()).getCodeId());
        } else {
          cxtBuilder.append(treePos.getNode().getTerminologyId());
        }
        cxtBuilder.append(lineEnd);

        final Tree tree = getTreeForTreePosition(treePos);

        // ancestors
        indent = "";
        cxtBuilder.append(tree.getNodeName()).append(lineEnd);
        indent += "  ";
        indent = printAncestors(cxtBuilder, tree, null, indent);
        // "parent" is the tree position above the bottom one
        parent = tree;
        while (parent.getChildren().size() > 0) {
          if (parent.getChildren().get(0).getChildren().size() > 0) {
            parent = parent.getChildren().get(0);
          } else {
            break;
          }
        }

        TreePositionList siblings = null;
        TreePositionList children = null;
        final PfsParameter childPfs = new PfsParameterJpa();
        childPfs.setStartIndex(0);
        childPfs.setMaxResults(10);

        if (treePos.getChildCt() > 0) {
          children = findTreePositionChildren(treePos.getNode().getId(), null,
              null, null, Branch.ROOT, treePos.getClass(), childPfs);
        } else {
          children = new TreePositionListJpa();
        }

        siblings = findTreePositionChildren(parent.getNodeId(), null, null,
            null, Branch.ROOT, treePos.getClass(), new PfsParameterJpa());

        // siblings & self node
        indent = indent.substring(0, indent.length() - 2);
        Collections.sort(siblings.getObjects(), (t1, t2) -> t1.getNode()
            .getName().compareTo(t2.getNode().getName()));

        indent += "  ";
        for (TreePosition<?> siblingPosition : siblings.getObjects()) {
          cxtBuilder.append(indent);
          if (siblingPosition.getNode().getName()
              .equals(treePos.getNode().getName())) {
            cxtBuilder.append("<b>");
          }
          cxtBuilder.append(siblingPosition.getNode().getName());
          if (siblingPosition.getNode().getName()
              .equals(treePos.getNode().getName())) {
            cxtBuilder.append("</b>").append(lineEnd);

            // children
            indent += "  ";
            printChildren(cxtBuilder, treePos, children, indent);
            if (children.getTotalCount() > 10) {
              cxtBuilder.append(indent).append(
                  "..." + (children.getTotalCount() - 10) + " more ...");
            }
            indent = indent.substring(0, indent.length() - 2);
          } else if (siblingPosition.getChildCt() > 0) {
            cxtBuilder.append(" +").append(lineEnd);
          } else {
            cxtBuilder.append(lineEnd);
          }
        }
        cxtBuilder.append(lineEnd);
      }
      cacheContexts(concept.getId(), cxtBuilder.toString());
      sb.append(cxtBuilder.toString());
    }

    if (comp.getLastModified() != null && comp.getLastModifiedBy() != null) {
      if (comp instanceof Concept) {
        sb.append("Concept");
      } else if (comp instanceof Descriptor) {
        sb.append("Descriptor");
      } else if (comp instanceof Code) {
        sb.append("Code");
      }
      sb.append(" was last touched on ").append(comp.getLastModified())
          .append(" by ").append(comp.getLastModifiedBy()).append(".")
          .append(lineEnd);
    }
    if (concept != null) {
      if (concept.getLastApproved() != null
          && concept.getLastApprovedBy() != null) {
        sb.append("Concept was last approved on ")
            .append(concept.getLastApproved()).append(" by ")
            .append(concept.getLastApprovedBy()).append(".").append(lineEnd);
      }
    }

    return sb.toString();
  }

  private Object handleHtmlSymbols(String name) {
    final String name2 = name.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
        .replaceAll("'", "&apos;");
    return name2;
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
    StringBuilder sb = new StringBuilder();
    for (ConceptRelationship rel : rels) {
      // relationship type
      sb.append("[").append(rel.getRelationshipType()).append("]  ");

      // Released?
      if (!rel.isPublishable()) {
        sb.append("{");
      }

      // Name/termgroup/code
      sb.append(rel.getFrom().getName().replaceAll("<", "&lt;")
          .replaceAll(">", "&gt;").replaceAll("'", "&apos;")).append(" [");
      /*
       * TODO NE-143 sb.append(" ["); sb.append(getVsab(rel) .append("/"); //
       * TODO termType - only ifneeded
       */
      sb.append("|");
      sb.append(rel.getAdditionalRelationshipType());
      sb.append("|");
      sb.append(getTerminologyAndVersion(rel));
      sb.append("|");
      sb.append(rel.getLastModifiedBy());
      sb.append("]");

      sb.append(" {");
      sb.append(rel.getFrom().getId());
      sb.append("}");

      // Print relationship_level
      if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION) {
        sb.append(" P");
      } else if (rel.getTerminology().equals(rel.getTo().getTerminology())) {
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
   * Returns the vsab.
   *
   * @param t the t
   * @return the vsab
   */
  @SuppressWarnings("static-method")
  private String getTerminologyAndVersion(HasTerminology t) {
    return t.getTerminology()
        + (t.getVersion().equals("latest") ? "" : ("_" + t.getVersion()));
  }

  /**
   * Returns the cached contexts.
   *
   * @param conceptId the concept id
   * @return the cached contexts
   */
  @SuppressWarnings("static-method")
  private String getCachedContexts(Long conceptId) {
    synchronized (conceptContextsCache) {
      if (conceptContextsCache.containsKey(conceptId)) {
        return conceptContextsCache.get(conceptId);
      }
    }
    return null;
  }

  /**
   * Cache contexts.
   *
   * @param conceptId the concept id
   * @param contexts the contexts
   */
  @SuppressWarnings("static-method")
  private void cacheContexts(Long conceptId, String contexts) {
    synchronized (conceptContextsCache) {
      conceptContextsCache.put(conceptId, contexts);
    }
  }

  /**
   * Clear cache contexts for concept.
   *
   * @param conceptId the concept id
   */
  @SuppressWarnings("static-method")
  public static void clearCachedContextsForConcept(Long conceptId) {
    synchronized (conceptContextsCache) {
      conceptContextsCache.remove(conceptId);
    }
  }

  /* see superclass */
  @Override
  public Report getReport(Long reportId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get report " + reportId);
    Report report = getHasLastModified(reportId, ReportJpa.class);
    return report;
  }

  /* see superclass */
  @Override
  public Report addReport(Report report) throws Exception {
    // Cascades the add
    return addHasLastModified(report);
  }

  /* see superclass */
  @Override
  public void removeReport(Long reportId) throws Exception {
    // Cascaded delete
    this.removeHasLastModified(reportId, ReportJpa.class);

  }

  /* see superclass */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  @Override
  public ReportList findReports(Project projectId, String query,
    PfsParameter pfs) throws Exception {
    final SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);
    final int[] totalCt = new int[1];
    final ReportList list = new ReportListJpa();
    list.setObjects((List) searchHandler.getQueryResults(null, null,
        Branch.ROOT, query, null, ReportJpa.class, pfs, totalCt, manager));
    list.setTotalCount(totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public Report generateReport(Project project, String name, String query,
    QueryType queryType, IdType resultType) throws Exception {

    final List<Object[]> list = executeReportQuery(query, queryType,
        getDefaultQueryParams(project), false);

    final Report report = new ReportJpa();
    report.setProject(project);
    report.setName(name);
    report.setQuery(query);
    report.setQueryType(queryType);
    report.setResultType(resultType.toString());
    report.setAutoGenerated(false);
    report.setDiffReport(false);
    report.setReport1Id(null);
    report.setReport2Id(null);
    report.setLastModifiedBy(this.getLastModifiedBy());
    report.setLastModified(new Date());
    report.setTimestamp(new Date());
    final Map<String, ReportResult> map = new HashMap<>();
    for (final Object[] result : list) {
      final String itemId = result[0].toString();
      final String itemName = result[1].toString();
      final String value = result[2].toString();
      ReportResult rr = map.get(value);
      if (!map.containsKey(value)) {
        rr = new ReportResultJpa();
        rr.setValue(value);
        rr.setCt(1);
        rr.setReport(report);
        rr.setLastModified(new Date());
        rr.setLastModifiedBy(this.getLastModifiedBy());
        rr.setTimestamp(new Date());
        report.getResults().add(rr);
        map.put(value, rr);
      }
      final long ct = rr.getCt() + 1;
      rr.setCt(ct);
      final ReportResultItem item = new ReportResultItemJpa();
      item.setResult(rr);
      item.setItemId(itemId);
      item.setItemName(itemName);
      item.setLastModified(new Date());
      item.setLastModifiedBy(this.getLastModifiedBy());
      item.setTimestamp(new Date());
      rr.getResultItems().add(item);
    }
    // Return the report
    return addReport(report);
  }

  /**
   * Handle lazy initialization.
   *
   * @param report the report
   */
  @Override
  public void handleLazyInit(Report report) {
    if (report == null) {
      return;
    }
    report.getResults().size();
    for (ReportResult rr : report.getResults()) {
      rr.getCt();
    }
  }
}