/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.TerminologyListJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.SecurityService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link MetadataServiceRest}.
 */
@Path("/metadata")
@Api(value = "/metadata", description = "Operations providing terminology metadata")
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class MetadataServiceRestImpl extends RootServiceRestImpl
    implements MetadataServiceRest {

  /** The security service. */
  private SecurityService securityService;

  /**
   * Instantiates an empty {@link MetadataServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public MetadataServiceRestImpl() throws Exception {
    securityService = new SecurityServiceJpa();
  }

  /* see superclass */
  @Override
  @GET
  @Path("/terminology/{terminology}/{version}")
  @ApiOperation(value = "Get terminology", notes = "Gets the terminology for the specified parameters", response = TerminologyJpa.class)
  public Terminology getTerminology(
    @ApiParam(value = "Terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Metadata): /terminology/" + terminology + "/" + version);

    MetadataService metadataService = new MetadataServiceJpa();
    try {

      // authorize call
      authorizeApp(securityService, authToken, "get terminology",
          UserRole.VIEWER);

      Terminology termInfo =
          metadataService.getTerminology(terminology, version);
      if (termInfo == null) {
        return new TerminologyJpa();
      }
      metadataService.getGraphResolutionHandler(terminology).resolve(termInfo);

      return termInfo;

    } catch (Exception e) {

      handleException(e, "trying to retrieve the metadata");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/all/terminology/{terminology}/{version}")
  @ApiOperation(value = "Get metadata for terminology and version", notes = "Gets the key-value pairs representing all metadata for a particular terminology and version", response = KeyValuePairLists.class)
  public KeyValuePairLists getAllMetadata(
    @ApiParam(value = "Terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /all/terminology/" + terminology + "/"
            + version);

    MetadataService metadataService = new MetadataServiceJpa();
    try {
      // authorize call
      authorizeApp(securityService, authToken, "retrieve metadata",
          UserRole.VIEWER);

      KeyValuePairLists keyValuePairList =
          getMetadataHelper(terminology, version);

      return keyValuePairList;

    } catch (Exception e) {
      handleException(e, "trying to retrieve the metadata");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /**
   * Gets the metadata helper.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the metadata helper
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private KeyValuePairLists getMetadataHelper(String terminology,
    String version) throws Exception {
    MetadataService metadataService = new MetadataServiceJpa();
    try {

      RootTerminology rootTerminology = null;
      for (final RootTerminology root : metadataService.getRootTerminologies()
          .getObjects()) {
        if (root.getTerminology().equals(terminology)) {
          rootTerminology = root;
          break;
        }
      }
      if (rootTerminology == null) {
        // terminology does not exist, throw 204 (No Content)
        throw new WebApplicationException(Response.status(204)
            .entity("No terminology " + terminology + " is loaded").build());
      }

      Terminology term = null;
      for (final Terminology t : metadataService.getVersions(terminology)
          .getObjects()) {
        if (t.getVersion().equals(version)) {
          term = t;
          break;
        }
      }
      if (term == null) {
        throw new WebApplicationException(
            Response.status(204).entity("No version " + version
                + " is loaded for terminology " + terminology).build());
      }

      // call jpa service and get complex map return type
      final Map<String, Map<String, String>> mapOfMaps =
          metadataService.getAllMetadata(terminology, version);

      // convert complex map to KeyValuePair objects for easy transformation to
      // XML/JSON
      final KeyValuePairLists keyValuePairLists = new KeyValuePairLists();
      for (final Map.Entry<String, Map<String, String>> entry : mapOfMaps
          .entrySet()) {
        final String metadataType = entry.getKey();
        final Map<String, String> metadataPairs = entry.getValue();
        final KeyValuePairList keyValuePairList = new KeyValuePairList();
        keyValuePairList.setName(metadataType);
        for (final Map.Entry<String, String> pairEntry : metadataPairs
            .entrySet()) {
          final KeyValuePair keyValuePair = new KeyValuePair(
              pairEntry.getKey().toString(), pairEntry.getValue());
          keyValuePairList.addKeyValuePair(keyValuePair);
        }
        keyValuePairLists.addKeyValuePairList(keyValuePairList);
      }
      keyValuePairLists.sort();
      return keyValuePairLists;
    } catch (Exception e) {
      throw e;
    } finally {
      metadataService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/terminology/terminologies/latest")
  @ApiOperation(value = "Get all terminologies and their latest versions", notes = "Gets the list of terminologies and their latest versions", response = TerminologyListJpa.class)
  public TerminologyList getAllTerminologiesLatestVersions(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /terminologies/latest/");

    MetadataService metadataService = new MetadataServiceJpa();

    try {
      // authorize call
      authorizeApp(securityService, authToken,
          "get latest versions of all terminologies", UserRole.VIEWER);

      final TerminologyList results = metadataService.getTerminologies();
      for (final Terminology terminology : results.getObjects()) {
        metadataService.getGraphResolutionHandler(terminology.getTerminology())
            .resolve(terminology);
      }
      return results;

    } catch (Exception e) {
      handleException(e,
          "trying to retrieve the latest versions of all terminologies");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/terminology/terminologies")
  @ApiOperation(value = "Get all terminologies and all their versions", notes = "Gets the list of all terminologies and all of their versions", response = TerminologyListJpa.class)
  public TerminologyList getTerminologies(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /terminologies");

    MetadataService metadataService = new MetadataServiceJpa();

    try {

      // authorize call
      authorizeApp(securityService, authToken, "get terminologies",
          UserRole.VIEWER);

      final TerminologyList results = metadataService.getTerminologies();
      for (final Terminology terminology : results.getObjects()) {
        metadataService.getGraphResolutionHandler(terminology.getTerminology())
            .resolve(terminology);
      }
      return results;

    } catch (Exception e) {
      handleException(e, "trying to retrieve all terminologies");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/precedence/{terminology}/{version}")
  @ApiOperation(value = "Get default precedence list", notes = "Gets the default precedence list ranking for the specified parameters", response = PrecedenceListJpa.class)
  public PrecedenceList getDefaultPrecedenceList(
    @ApiParam(value = "Terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass()).info(
        "RESTful call (Metadata): /precedence/" + terminology + "/" + version);

    MetadataService metadataService = new MetadataServiceJpa();
    try {

      // authorize call
      authorizeApp(securityService, authToken, "get precedence list",
          UserRole.VIEWER);

      PrecedenceList precedenceList =
          metadataService.getDefaultPrecedenceList(terminology, version);

      return precedenceList;

    } catch (Exception e) {

      handleException(e, "trying to retrieve the metadata");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  @Override
  @POST
  @Path("/precedence/add")
  @ApiOperation(value = "Add a precedence list", notes = "Add a precedence list", response = PrecedenceListJpa.class)
  public PrecedenceList addPrecedenceList(
    @ApiParam(value = "Precedence list to add", required = true) PrecedenceListJpa precedenceList,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /precedence/add");

    MetadataService metadataService = new MetadataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "add precedence list",
          UserRole.USER);

      return metadataService.addPrecedenceList(precedenceList);
    } catch (Exception e) {
      handleException(e, "trying to add precedence list");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }

  }

  @Override
  @POST
  @Path("/precedence/update")
  @ApiOperation(value = "Update a precedence list", notes = "Update a precedence list", response = PrecedenceListJpa.class)
  public void updatePrecedenceList(
    @ApiParam(value = "Precedence list to add", required = true) PrecedenceListJpa precedenceList,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /precedence/add");

    MetadataService metadataService = new MetadataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "update precedence list",
          UserRole.USER);

      metadataService.updatePrecedenceList(precedenceList);
    } catch (Exception e) {
      handleException(e, "trying to update precedence list");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }

  @Override
  @DELETE
  @Path("/precedence/{id}/remove")
  @ApiOperation(value = "Remove a precedence list", notes = "Remove a precedence list")
  public void removePrecedenceList(
    @ApiParam(value = "Precedence list id, e.g. 1", required = true) @PathParam("id") Long id,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /precedence/add");

    MetadataService metadataService = new MetadataServiceJpa();
    try {

      authorizeApp(securityService, authToken, "remove precedence list",
          UserRole.USER);

      metadataService.removePrecedenceList(id);
    } catch (Exception e) {

      handleException(e, "trying to retrieve the metadata");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }

}
