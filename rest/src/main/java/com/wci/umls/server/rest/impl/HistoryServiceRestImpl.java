/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.algo.StartEditingCycleAlgorithm;
import com.wci.umls.server.jpa.helpers.ReleaseInfoListJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.HistoryServiceRest;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.SecurityService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link HistoryServiceRest}.
 */
@Path("/history")
@Api(value = "/history", description = "Operations to retrieve historical RF2 content for a terminology")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class HistoryServiceRestImpl extends RootServiceRestImpl implements
    HistoryServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link HistoryServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public HistoryServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/releases/{terminology}")
  @ApiOperation(value = "Get release history", notes = "Gets all release info objects", response = ReleaseInfoListJpa.class)
  public ReleaseInfoList getReleaseHistory(
    @ApiParam(value = "Release info terminology , e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/history/");

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get release history",
          UserRole.VIEWER);

      ReleaseInfoList result = historyService.getReleaseHistory(terminology);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to get release history");
      return null;
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/release/{terminology}/current")
  @ApiOperation(value = "Get current release info", notes = "Gets release info for current release", response = ReleaseInfoJpa.class)
  public ReleaseInfo getCurrentReleaseInfo(
    @ApiParam(value = "Release info terminology , e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/current/");

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get current release info",
          UserRole.VIEWER);

      ReleaseInfo result = historyService.getCurrentReleaseInfo(terminology);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to get current release info");
      return null;
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/release/{terminology}/previous")
  @ApiOperation(value = "Get previous release info", notes = "Gets release info for previous release", response = ReleaseInfoJpa.class)
  public ReleaseInfo getPreviousReleaseInfo(
    @ApiParam(value = "Release info terminology , e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/previous/");

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get previous release info",
          UserRole.VIEWER);

      ReleaseInfo result = historyService.getPreviousReleaseInfo(terminology);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to get previous release info");
      return null;
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/release/{terminology}/planned")
  @ApiOperation(value = "Get planned release info", notes = "Gets release info for planned release", response = ReleaseInfoJpa.class)
  public ReleaseInfo getPlannedReleaseInfo(
    @ApiParam(value = "Release info terminology , e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/planned/");

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get planned release info",
          UserRole.VIEWER);

      ReleaseInfo result = historyService.getPlannedReleaseInfo(terminology);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to get planned release info");
      return null;
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/release/{terminology}/{name}")
  @ApiOperation(value = "Get release info", notes = "Gets release info for specified release name and terminology", response = ReleaseInfoJpa.class)
  public ReleaseInfo getReleaseInfo(
    @ApiParam(value = "Release info terminology , e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Release version info, e.g. 'latest'", required = true) @PathParam("name") String name,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/" + name);

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get release info for " + name,
          UserRole.VIEWER);

      ReleaseInfo result = historyService.getReleaseInfo(terminology, name);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to get release info for " + name);
      return null;
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @PUT
  @Path("/release/add")
  @ApiOperation(value = "Add release info", notes = "Adds the specified release info", response = ReleaseInfoJpa.class)
  public ReleaseInfo addReleaseInfo(
    @ApiParam(value = "Release info object, e.g. see output of /release/current", required = true) ReleaseInfoJpa releaseInfo,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/add " + releaseInfo.getName());

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "add release info",
          UserRole.ADMINISTRATOR);

      releaseInfo.setLastModifiedBy(securityService
          .getUsernameForToken(authToken));
      ReleaseInfo result = historyService.addReleaseInfo(releaseInfo);
      return result;

    } catch (Exception e) {
      handleException(e, "trying to add release info");
      return null;
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/release/update")
  @ApiOperation(value = "Update release info", notes = "Updatess the specified release info")
  public void updateReleaseInfo(
    @ApiParam(value = "Release info object, e.g. see output of /release/current", required = true) ReleaseInfoJpa releaseInfo,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/update " + releaseInfo.getName());

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "update release info",
          UserRole.ADMINISTRATOR);

      releaseInfo.setLastModifiedBy(securityService
          .getUsernameForToken(authToken));
      historyService.updateReleaseInfo(releaseInfo);
    } catch (Exception e) {
      handleException(e, "trying to update release info");
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @DELETE
  @Path("/release/remove/{id}")
  @ApiOperation(value = "Remove release info", notes = "Removes the release info for the specified id")
  public void removeReleaseInfo(
    @ApiParam(value = "Release info object id, e.g. 2", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/remove/" + id);

    HistoryService historyService = new HistoryServiceJpa();
    try {
      authorizeApp(securityService, authToken, "remove release info",
          UserRole.ADMINISTRATOR);

      historyService.removeReleaseInfo(id);
    } catch (Exception e) {
      handleException(e, "trying to remove release info");
    } finally {
      historyService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @POST
  @Path("/release/startEditingCycle/{releaseVersion}/{terminology}/{version}")
  @ApiOperation(value = "Start the editing cycle", notes = "Marks the start of the editing cycle for the specified release for the specified terminology/version")
  public void startEditingCycle(
    @ApiParam(value = "Release version, e.g. 20150131 or 2015AA", required = true) @PathParam("releaseVersion") String releaseVersion,
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (History): /release/startEditingCycle/" + releaseVersion
            + "/" + terminology + "/" + version);
    // Perform operations
    StartEditingCycleAlgorithm algorithm =
        new StartEditingCycleAlgorithm(releaseVersion, terminology, version);
    try {
      authorizeApp(securityService, authToken, "start editing cycle",
          UserRole.ADMINISTRATOR);
      algorithm.setUser(securityService.getUsernameForToken(authToken));
      algorithm.compute();
    } catch (Exception e) {
      algorithm.compute();
      handleException(e, "start editing cycle");
    } finally {
      algorithm.close();
      securityService.close();
    }
  }

}
