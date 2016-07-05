/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.jpa.services.ReportServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ReportServiceRest;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ReportService;
import com.wci.umls.server.services.SecurityService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link ReportServiceRest}.
 */
@Path("/report")
@Api(value = "/report", description = "Operations to retrieve reports.")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ReportServiceRestImpl extends RootServiceRestImpl implements
    ReportServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link ReportServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ReportServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/concept")
  @ApiOperation(value = "Get concept report", notes = "Gets a concept report")
  public String getConceptReport(
    @ApiParam(value = "Project id, e.g. 5", required = true) @QueryParam("projectId") Long  projectId,
    @ApiParam(value = "Concept id, e.g. UMLS", required = true) @QueryParam("conceptId") Long  conceptId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Report): /report");
    
    ReportService reportService = new ReportServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get concept report",
          UserRole.VIEWER);

      Project project = reportService.getProject(projectId);
      Concept concept = reportService.getConcept(conceptId);
      return reportService.getConceptReport(project, concept);

    } catch (Exception e) {
      handleException(e, "trying to get concept report");
      return null;
    } finally {
      reportService.close();
      securityService.close();
    }
  }
}
