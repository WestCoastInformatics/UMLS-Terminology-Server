/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.services.ReportServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ReportServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.services.ReportService;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * REST implementation for {@link ReportServiceRest}.
 */
@Path("/report")
@Api(value = "/report")
@SwaggerDefinition(info = @Info(description = "Operations for reporting.", title = "Report API", version = "1.0.1"))
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ReportServiceRestImpl extends RootServiceRestImpl
    implements ReportServiceRest {

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
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/concept/{id}")
  @ApiOperation(value = "Get concept report", notes = "Gets a concept report", response = String.class)
  public String getConceptReport(
    @ApiParam(value = "Project id, e.g. UMLS", required = false) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Concept id, e.g. UMLS", required = true) @PathParam("id") Long conceptId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Report): /report");

    final ReportService reportService = new ReportServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "get concept report", UserRole.VIEWER);

      final Concept concept = reportService.getConcept(conceptId);
      final Project project = reportService.getProject(projectId);

      // Sort atoms
      if (concept != null) {
        reportService.getGraphResolutionHandler(concept.getTerminology())
            .resolve(concept);
        final PrecedenceList list = sortAtoms(securityService, reportService,
            userName, concept, project);
        return reportService.getConceptReport(project, concept, list);
      }
      return "MISSING CONCEPT";

    } catch (Exception e) {
      handleException(e, "trying to get concept report");
      return null;
    } finally {
      reportService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/descriptor/{id}")
  @ApiOperation(value = "Get descriptor report", notes = "Gets a descriptor report", response = String.class)
  public String getDescriptorReport(
    @ApiParam(value = "Project id, e.g. UMLS", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Descriptor id, e.g. UMLS", required = true) @PathParam("id") Long descriptorId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Report): /report");

    final ReportService reportService = new ReportServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "get descriptor report", UserRole.VIEWER);

      final Descriptor descriptor = reportService.getDescriptor(descriptorId);
      final Project project = reportService.getProject(projectId);
      // Sort atoms
      if (descriptor != null) {
        reportService.getGraphResolutionHandler(descriptor.getTerminology())
            .resolve(descriptor);
        final PrecedenceList list = sortAtoms(securityService, reportService,
            userName, descriptor, project);
        return reportService.getDescriptorReport(project, descriptor, list);
      }
      return "MISSING DESCRIPTOR";

    } catch (Exception e) {
      handleException(e, "trying to get descriptor report");
      return null;
    } finally {
      reportService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Produces(MediaType.TEXT_PLAIN)
  @Path("/code/{id}")
  @ApiOperation(value = "Get code report", notes = "Gets a code report", response = String.class)
  public String getCodeReport(
    @ApiParam(value = "Project id, e.g. UMLS", required = true) @QueryParam("projectId") Long projectId,
    @ApiParam(value = "Code id, e.g. UMLS", required = true) @PathParam("id") Long codeId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info("RESTful call (Report): /report");

    final ReportService reportService = new ReportServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "get code report", UserRole.VIEWER);

      final Code code = reportService.getCode(codeId);
      final Project project = reportService.getProject(projectId);
      // Sort atoms
      if (code != null) {
        reportService.getGraphResolutionHandler(code.getTerminology())
            .resolve(code);
        final PrecedenceList list =
            sortAtoms(securityService, reportService, userName, code, project);
        return reportService.getCodeReport(project, code, list);
      }
      return "MISSING CODE";

    } catch (Exception e) {
      handleException(e, "trying to get code report");
      return null;
    } finally {
      reportService.close();
      securityService.close();
    }
  }
}
