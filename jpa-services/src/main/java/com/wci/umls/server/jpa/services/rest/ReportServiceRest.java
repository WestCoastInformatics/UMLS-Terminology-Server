/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.WorkflowBinDefinitionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportList;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;

/**
 * The Interface ReportServiceRest.
 */
public interface ReportServiceRest {

  /**
   * Returns the concept report.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param authToken the auth token
   * @return the concept report
   * @throws Exception the exception
   */
  public String getConceptReport(Long projectId, Long conceptId, String authToken)
    throws Exception;

  /**
   * Returns the code report.
   *
   * @param projectId the project id
   * @param codeId the code id
   * @param authToken the auth token
   * @return the code report
   * @throws Exception the exception
   */
  public String getCodeReport(Long projectId, Long codeId, String authToken) throws Exception;

  /**
   * Returns the descriptor report.
   *
   * @param projectId the project id
   * @param descriptorId the descriptor id
   * @param authToken the auth token
   * @return the descriptor report
   * @throws Exception the exception
   */
  public String getDescriptorReport(Long projectId, Long descriptorId, String authToken)
    throws Exception;

  /**
   * Find report definitions.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the workflow bin definition list
   * @throws Exception the exception
   */
  public WorkflowBinDefinitionList findReportDefinitions(Long projectId, 
    String authToken) throws Exception;

  /**
   * Find reports.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the report list
   * @throws Exception the exception
   */
  public ReportList findReports(Long projectId, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Gets the report.
   *
   * @param id the id
   * @param authToken the auth token
   * @return the report
   * @throws Exception the exception
   */
  public Report getReport(Long id, String authToken) throws Exception;

  /**
   * Generate report.
   *
   * @param id the id
   * @param name the name
   * @param query the query
   * @param queryType the query type
   * @param resultType the result type
   * @param authToken the auth token
   * @return the report
   * @throws Exception the exception
   */
  public Report generateReport(Long id, String name,
    String query, QueryType queryType,
    IdType resultType, String authToken) throws Exception;
}
