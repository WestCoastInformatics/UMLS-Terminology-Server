/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

/**
 * The Interface ReportService.
 */
public interface ReportService extends HistoryService {

  /**
   * Returns the concept report.
   *
   * @param project the project
   * @param concept the concept
   * @param list the list
   * @param decorate the decorate
   * @return the concept report
   * @throws Exception the exception
   */
  public String getConceptReport(Project project, Concept concept,
    PrecedenceList list, boolean decorate) throws Exception;

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
  public String getDescriptorReport(Project project, Descriptor descriptor,
    PrecedenceList list, boolean decorate) throws Exception;

  /**
   * Returns the concept report.
   *
   * @param project the project
   * @param code the code
   * @param list the list
   * @param decorate the decorate
   * @return the concept report
   * @throws Exception the exception
   */
  public String getCodeReport(Project project, Code code, PrecedenceList list,
    boolean decorate) throws Exception;
}
