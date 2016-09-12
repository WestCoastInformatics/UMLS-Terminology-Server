/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import com.wci.umls.server.Project;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.SemanticTypeComponent;
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
  public String getConceptReport(Project project, Concept concept)
    throws Exception {

    //
    //Options
    //
    int max_relationship_count = 20;
    int max_cxt_rel_count = 20;
    boolean include_siblings = false;
    
    //
    // Handle validation/integrity checks
    //
    
    
    
    //
    // Concept information
    //
    final StringBuilder sb = new StringBuilder();
    sb.append("\r\n").append("CN# ");
    sb.append(concept.getId()).append(" ");
    sb.append(concept.getName()).append("\r\n");

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
      sb.append(id).append("\r\n");
    }

    //
    // Semantic Types
    //
    sb.append("STY ");
    for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
      sb.append(sty.getSemanticType()).append("\t");
      sb.append(getStatusChar(sty.getWorkflowStatus())).append("\r\n");
    }

    //
    // Definitions
    //
    sb.append("DEF ");
    for (final Atom atom : concept.getAtoms()) {
      for (final Definition def : atom.getDefinitions()) {
        sb.append(def.isPublishable() ? "[Release] " : "[Do Not Release] ");
        sb.append(def.getTerminology()).append("_").append(def.getVersion()).append("\r\n");
        sb.append("  -").append(atom.getTerminology()).append("/").append(atom.getTermType());
        sb.append("|").append(WordUtils.wrap(def.getValue(), 65, "\r\n", false)).append("\r\n");
        
      }
    }
    
    //
    // SOS
    //
    sb.append("SOS ");
    for (final Atom atom : concept.getAtoms()) {
      for (final Attribute att : atom.getAttributes()) {
        if (att.getName().equals("SOS")) {
          sb.append(att.isPublishable() ? "[Release] " : "[Do Not Release] ");
          sb.append(att.getTerminology()).append("_").append(att.getVersion()).append("\r\n");
          sb.append("  -").append(atom.getTerminology()).append("/").append(atom.getTermType());
          sb.append("|").append(WordUtils.wrap(att.getValue(), 65, "\r\n", false)).append("\r\n");
          
        }
      }
    }
    
    //
    // Atoms
    //
    sb.append("ATOMS").append("\r\n");

    for (final Atom atom : concept.getAtoms()) {
      sb.append("   ");
      sb.append(getStatusChar(atom.getWorkflowStatus())).append(" ");
      // TODO [] what are these brackets for?
      sb.append(atom.getName()).append(" [");
      sb.append(atom.getTerminology()).append("_").append(atom.getVersion())
          .append("/"); // TODO how to append terminology/version
      sb.append(atom.getTermType()).append("/");
      sb.append(atom.getCodeId()).append("]").append("\r\n");
    }
    sb.append("\r\n");

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
}