/*
 *    Copyright 2016 West Coast Informatics, LLC
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
  public String getConceptReport(Long projectId, Long conceptId,
    String authToken) throws Exception;
}
