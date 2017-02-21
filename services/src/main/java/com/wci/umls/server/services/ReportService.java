/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportList;

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

  /**
   * Returns the report.
   *
   * @param reportId the report id
   * @return the report
   * @throws Exception the exception
   */
  public Report getReport(Long reportId) throws Exception;

  /**
   * Adds the report (CASCADE).
   *
   * @param report the report
   * @return the report
   * @throws Exception the exception
   */
  public Report addReport(Report report) throws Exception;

  // NO updateReport

  /**
   * Removes the report (CASCADE).
   *
   * @param reportId the report id
   * @throws Exception the exception
   */
  public void removeReport(Long reportId) throws Exception;

  /**
   * Find reports.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @return the report list
   * @throws Exception the exception
   */
  public ReportList findReports(Project projectId, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Generate report.
   *
   * @param project the project
   * @param name the name
   * @param query the query
   * @param queryType the query type
   * @param resultType the result type
   * @return the report
   * @throws Exception the exception
   */
  public Report generateReport(Project project, String name, String query,
    QueryType queryType, Class<? extends Component> resultType)
    throws Exception;

}
