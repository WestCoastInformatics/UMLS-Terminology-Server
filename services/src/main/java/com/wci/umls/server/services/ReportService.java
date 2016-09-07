/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
import com.wci.umls.server.model.content.Concept;

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
}
