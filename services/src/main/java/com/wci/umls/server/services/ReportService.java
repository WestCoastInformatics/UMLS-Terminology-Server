/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
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
   * @return the concept report
   * @throws Exception the exception
   */
  public String getConceptReport(Project project, Concept concept) throws Exception;

  /**
   * Returns the descriptor report.
   *
   * @param project the project
   * @param descriptor the descriptor
   * @return the descriptor report
   * @throws Exception the exception
   */
  public String getDescriptorReport(Project project, Descriptor descriptor) throws Exception;

  /**
   * Returns the concept report.
   *
   * @param project the project
   * @param code the code
   * @return the concept report
   * @throws Exception the exception
   */
  public String getCodeReport(Project project, Code code) throws Exception;
}
