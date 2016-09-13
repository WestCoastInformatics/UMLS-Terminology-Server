/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

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
}
