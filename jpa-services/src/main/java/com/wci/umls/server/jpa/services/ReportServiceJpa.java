/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.WordUtils;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
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
  public String getConceptReport(Project project, Concept concept) throws Exception {

    final StringBuilder sb = new StringBuilder();
    
    String lineEnd = "\r\n";
    //
    //Options
    //
    int max_relationship_count = 20;
    int max_cxt_rel_count = 20;
    boolean include_siblings = false;
    
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
    
    sb.append("...............................................................................");
    
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
        sb.append(def.getTerminology()).append("_").append(def.getVersion()).append("\r\n");
        sb.append("  - ").append(atom.getTerminology()).append("/").append(atom.getTermType());
        sb.append("|").append(WordUtils.wrap(def.getValue(), 65, "\r\n    ", false)).append(lineEnd);
        
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
          sosBuffer.append(att.isPublishable() ? " [Release] " : " [Do Not Release] ");
          sosBuffer.append(att.getTerminology()).append("_").append(att.getVersion()).append(lineEnd);
          sosBuffer.append("  - ").append(atom.getTerminology()).append("/").append(atom.getTermType());
          sosBuffer.append("|").append(WordUtils.wrap(att.getValue(), 65, "\r\n    ", false)).append(lineEnd);          
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
      if (true /*atom.getId() == atom_id*/) {
        //report.append(sep_begin);

        //
        // Determine flags
        //
      }
      sb.append(" ");
      if (getStatusChar(atom.getWorkflowStatus()).equals("D")) {
        sb.append("D");
      }
      else {
        sb.append(" ");
      }

      if (atom.getLastModifiedBy().startsWith("ENG-")) {
        sb.append("M");
      }
      else {
        sb.append(" ");
      }

      if (atom.isObsolete()) {
        sb.append("O");
      } else if (atom.isSuppressible() && getTermType(atom.getTermType(), concept.getTerminology(),
          concept.getVersion()).isSuppressible())  {
        sb.append("Y");
      } else if (atom.isSuppressible() && !getTermType(atom.getTermType(), concept.getTerminology(),
          concept.getVersion()).isSuppressible()) {
        sb.append("E");
      } else {
        sb.append(" ");
      }

      // Depict flag "B" for RxNORM Base Ambiguity Atom.
      // does the atom have a releasable RXNORM indicate ABIGUITITY_FLAG=Base?
      boolean isBaseRxnormAmbiguous = atom.getAttributes().stream().filter(
          a -> a.getName().equals("AMBIGUITY_FLAG") && 
          a.getValue().equals("Base") && a.getTerminology().equals("RXNORM")
          && a.isPublishable()).collect(Collectors.toList()). size() > 0;
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
      SearchResultList results = findConcepts(concept.getTerminology(), concept.getVersion(), null, 
          "atoms.lowerNameHash:" + atom.getLowerNameHash()
          + " AND NOT id:" + concept.getId(), pfs);
      if (results.getTotalCount()>0) {
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
          }
          else {
            sb.append("  ");
          }
        }
        else {
          sb.append(" []  ");
        }

      // Released?
      if (!atom.isPublished()) {
        sb.append("{");
      }
      
      // Name/termgroup/code
      sb.append(atom.getName()).append(" [");
      sb.append(atom.getTerminology()).append("_").append(atom.getVersion())
          .append("/"); 
      sb.append(atom.getTermType()).append("/");
      sb.append(atom.getCodeId()).append("]");
      
      // Write MUI if ( MSH (or MSH translation) or NCI (or NCI subsources)).
      if (("MSH".equals(atom.getTerminology()) &&
          atom.getConceptId() != null) ||
          ("NCI".equals(atom.getTerminology()) &&
          atom.getConceptId() != null)) {
        sb.append(" ");
        sb.append(atom.getConceptId());
      }            
      
      // Write RXCUI
      Attribute att = atom.getAttributeByName("RXCUI");
      if (att != null) {            
        sb.append(" ");
        sb.append(att.getValue());
      }
      
      if (!atom.isPublished()) {
        sb.append("}");
      }
      sb.append(lineEnd);
      
      prev_lui = atom.getLexicalClassId();
      prev_sui = atom.getStringClassId();

    }
    sb.append(lineEnd);
    
    //
    // RELATIONSHIPS
    //
    sb.append("REVIEWED RELATED CONCEPT(S)").append(lineEnd);
    
    // TODO for smaller default pfs
    for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship : this.findConceptDeepRelationships(concept.getTerminologyId(), 
        concept.getTerminology(), concept.getVersion(), Branch.ROOT, null, false, new PfsParameterJpa()).getObjects()) {
      ConceptRelationship rel = (ConceptRelationship)relationship;
      if (rel.getWorkflowStatus() != WorkflowStatus.READY_FOR_PUBLICATION && 
          rel.getWorkflowStatus() != WorkflowStatus.PUBLISHED) {
        continue;
      }
      
      //relationship type
      sb.append("[").append(rel.getRelationshipType()).append("]  ");
      
      // Released?
      if (!rel.isPublishable()) {
        sb.append("{");
      }

      // Name/termgroup/code
      sb.append(rel.getTo().getName());
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
      if (rel.getTerminology().equals(concept.getTerminology())) {
        sb.append(" C");
      }
      else if (rel.getWorkflowStatus() == WorkflowStatus.DEMOTION) {
        sb.append(" P");
      }
      else {
        sb.append(" S");
      }
      
      if (!rel.isPublishable()) {
        sb.append("}");
      }
      /* OUT OF SCOPE
        if (rels.isWeaklyUnreleasable()) {
          sb.append(" n");
        }
        else if (rels.isUnreleasable()) {
          sb.append(" NEVER");
        }
      }*/
      sb.append(lineEnd).append(lineEnd);
    }

    //
    // CONTEXTS
    //
    sb.append("CONTEXTS").append(lineEnd);

    for (Atom atom : concept.getAtoms()) {
      Terminology fullTerminology =
          getTerminology(atom.getTerminology(), atom.getVersion());
      IdType type = fullTerminology.getOrganizingClassType();
      String terminologyId = null;
      List<TreePosition<? extends ComponentHasAttributesAndName>> treePosList =
          null;
      if (type == IdType.CODE) {
        terminologyId = atom.getCodeId();
        treePosList =
            findCodeTreePositions(terminologyId, atom.getTerminology(),
                atom.getVersion(), null, null, new PfsParameterJpa())
                    .getObjects();
      } else if (type == IdType.CONCEPT) {
        terminologyId = atom.getConceptId();
        treePosList =
            findConceptTreePositions(terminologyId, atom.getTerminology(),
                atom.getVersion(), null, null, new PfsParameterJpa())
                    .getObjects();

      } else if (type == IdType.DESCRIPTOR) {
        terminologyId = atom.getDescriptorId();
        treePosList =
            findDescriptorTreePositions(terminologyId, atom.getTerminology(),
                atom.getVersion(), null, null, new PfsParameterJpa())
                    .getObjects();
      } else {
        continue;
      }

      // TODO: need to unique the list of terminologyId, termgroup, and organizing class type
      // TODO: pfs with max of 1
      
      ConceptList ancestors = null;
      TreePositionList children = null;
      for (TreePosition<?> treePos : treePosList) {
        sb.append(treePos.getTerminology()).append("_").append(treePos.getVersion());
        sb.append("/").append(treePos.getTerminologyId()).append(lineEnd);
        Tree tree = getTreeForTreePosition(treePos);
        if (type == IdType.CONCEPT) {
          ancestors = findAncestorConcepts(terminologyId, atom.getTerminology(),
            atom.getVersion(), false, null, new PfsParameterJpa());
          children = findConceptTreePositionChildren(terminologyId,
            atom.getTerminology(), atom.getVersion(), null,
            new PfsParameterJpa());
          
        }
        // TODO: add other types
        
        // ancestors
        for (Component ancestor : ancestors.getObjects()) {
          sb.append(ancestor.getName()).append(lineEnd);
        }
        
        // self
        sb.append("<").append(treePos.getNode().getName()).append(">")
            .append(lineEnd);

        // children       
        for (TreePosition<?> child : children.getObjects()) {
          sb.append(child.getNode().getName()).append(lineEnd);
        }

        // siblings
        // TODO
        
        
        sb.append(lineEnd);
      }
    }

    return sb.toString();
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
  public String getDescriptorReport(Project project, Descriptor descriptor) throws Exception {
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
}