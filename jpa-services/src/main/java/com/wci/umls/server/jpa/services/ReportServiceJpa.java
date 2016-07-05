/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.EnumSet;

import com.wci.umls.server.Project;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.ReportService;

/**
 * JPA and JAXB enabled implementation of {@link HistoryService}.
 */
public class ReportServiceJpa extends HistoryServiceJpa implements
    ReportService {

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
    
    StringBuffer sb = new StringBuffer();
    sb.append("CN# ");
    sb.append(concept.getId()).append(" ");  // TODO ?
    sb.append(concept.getName()).append("\n");
    
    sb.append("CUI ");
    sb.append(concept.getTerminologyId()).append("\t");
    sb.append(EnumSet.of(WorkflowStatus.REVIEW_DONE, // TODO ? alternate text?
        WorkflowStatus.READY_FOR_PUBLICATION, WorkflowStatus.PUBLISHED).contains(
        concept.getWorkflowStatus()) ? "Concept Status is Reviewed" : "").append("\n");
  
    sb.append("STY ");
    sb.append(concept.getSemanticTypes().get(0).getSemanticType());  // TODO ? more than one?
    sb.append(" R").append("\n");  // TODO ? what is R
    
    sb.append("ATOMS").append("\n");
    
    for (Atom atom : concept.getAtoms()) {
      sb.append("\t").append("\t");
      // TODO R []  
      sb.append(atom.getName()).append(" [");
      sb.append(atom.getTerminology()).append(atom.getVersion()).append("/"); // TODO how to append terminology/version
      sb.append(atom.getTermType()).append("/");
      sb.append(atom.getCodeId()).append("]");     
    }
    
    return sb.toString();
  }
  
}