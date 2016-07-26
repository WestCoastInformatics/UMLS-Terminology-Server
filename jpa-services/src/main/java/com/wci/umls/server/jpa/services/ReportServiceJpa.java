/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wci.umls.server.Project;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
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

    final StringBuilder sb = new StringBuilder();
    sb.append("\n").append("CN# ");
    sb.append(concept.getId()).append(" ");
    sb.append(concept.getName()).append("\n");

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
    sb.append("Concept Status is "
        + concept.getWorkflowStatus().toString().substring(0, 1)).append("\n");
    for (final String id : conceptTerminologyIds) {
      sb.append(id).append("\n");
    }

    sb.append("STY ");
    for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
      sb.append(sty.getSemanticType()).append("\t");
      sb.append(sty.getWorkflowStatus().toString().substring(0, 1))
          .append("\n");
    }

    sb.append("ATOMS").append("\n");

    for (final Atom atom : concept.getAtoms()) {
      sb.append("   ");
      sb.append(atom.getWorkflowStatus().toString().substring(0, 1))
          .append(" ");
      // TODO [] what are these brackets for?
      sb.append(atom.getName()).append(" [");
      sb.append(atom.getTerminology()).append("_").append(atom.getVersion())
          .append("/"); // TODO how to append terminology/version
      sb.append(atom.getTermType()).append("/");
      sb.append(atom.getCodeId()).append("]").append("\n");
    }
    sb.append("\n");

    return sb.toString();
  }

}