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
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TerminologyListJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.SecurityService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;

/**
 * REST implementation for {@link MetadataServiceRest}.
 */
@Path("/metadata")
@Api(value = "/metadata")
@SwaggerDefinition(info = @Info(description = "Operations providing terminology metadata.", title = "Metadata API", version = "1.0.1"))
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

    final MetadataService metadataService = new MetadataServiceJpa();
    try {

      // authorize call
      authorizeApp(securityService, authToken, "get terminology",
          UserRole.VIEWER);

      final Terminology termInfo =
          metadataService.getTerminology(terminology, version);
      if (termInfo == null) {
        return new TerminologyJpa();
      }
      metadataService.getGraphResolutionHandler(terminology).resolve(termInfo);

      return termInfo;

    } catch (Exception e) {

      handleException(e, "trying to get the metadata");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/all/{terminology}/{version}")
  @ApiOperation(value = "Get metadata for terminology and version", notes = "Gets the key-value pairs representing all metadata for a particular terminology and version", response = KeyValuePairLists.class)
  public KeyValuePairLists getAllMetadata(
    @ApiParam(value = "Terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /all/" + terminology + "/" + version);

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      // authorize call
      authorizeApp(securityService, authToken, "get all metadata",
          UserRole.VIEWER);

      final KeyValuePairLists keyValuePairList =
          getMetadataHelper(terminology, version);

      return keyValuePairList;

    } catch (Exception e) {
      handleException(e, "trying to get the metadata");
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
    final MetadataService metadataService = new MetadataServiceJpa();
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
        throw new LocalException(
            "Unexpected missing terminology - " + terminology);
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
        throw new LocalException("Unexpected missing terminology/version - "
            + terminology + ", " + version);

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
  @Path("/terminology/current")
  @ApiOperation(value = "Get current terminologies", notes = "Gets the list of current terminologies", response = TerminologyListJpa.class)
  public TerminologyList getCurrentTerminologies(
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /terminology/current");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {

      // authorize call
      authorizeApp(securityService, authToken, "get terminologies",
          UserRole.VIEWER);

      final TerminologyList results = metadataService.getCurrentTerminologies();
      for (final Terminology terminology : results.getObjects()) {
        metadataService.getGraphResolutionHandler(terminology.getTerminology())
            .resolve(terminology);
      }
      return results;

    } catch (Exception e) {
      handleException(e, "trying to get all terminologies");
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

    final MetadataService metadataService = new MetadataServiceJpa();
    try {

      // authorize call
      authorizeApp(securityService, authToken, "get precedence list",
          UserRole.VIEWER);

      final PrecedenceList precedenceList =
          metadataService.getPrecedenceList(terminology, version);
      // Lazy initialize
      if (precedenceList != null) {
        precedenceList.getPrecedence().getKeyValuePairs().size();
      }
      return precedenceList;

    } catch (Exception e) {

      handleException(e, "trying to get the metadata");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /* see superclass */
  @Override
  @GET
  @Path("/precedence/{id}")
  @ApiOperation(value = "Gets a precedence list", notes = "Gets a precedence list", response = PrecedenceListJpa.class)
  public PrecedenceList getPrecedenceList(
    @ApiParam(value = "Precedence list id, e.g. 1", required = true) @PathParam("id") Long precedenceListId,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /precedence/" + precedenceListId);

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get precedence list",
          UserRole.VIEWER);

      final PrecedenceList list =
          metadataService.getPrecedenceList(precedenceListId);
      if (list == null) {
        return null;
      }
      // lazy initialize
      list.getPrecedence().getKeyValuePairs().size();
      list.getTermTypeRankMap().size();
      return list;
    } catch (Exception e) {
      handleException(e, "trying to get precedence list");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }

  /* see superclass */
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

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add precedence list", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      return metadataService.addPrecedenceList(precedenceList);
    } catch (Exception e) {
      handleException(e, "trying to add precedence list");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update precedence list", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updatePrecedenceList(precedenceList);
    } catch (Exception e) {
      handleException(e, "trying to update precedence list");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }

  /* see superclass */
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

    final MetadataService metadataService = new MetadataServiceJpa();
    try {

      final String userName = authorizeApp(securityService, authToken,
          "remove precedence list", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.removePrecedenceList(id);
    } catch (Exception e) {

      handleException(e, "trying to get the metadata");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }

  /* see superclass */
  @Override
  @GET
  @Path("sty/{terminology}/{version}")
  @ApiOperation(value = "Get semantic types", notes = "Get semantic types for the specified parameters", response = SemanticTypeListJpa.class)
  public SemanticTypeList getSemanticTypes(
    @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /sty/" + terminology + "/" + version);

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      authorizeApp(securityService, authToken, "get semantic types",
          UserRole.USER);

      return metadataService.getSemanticTypes(terminology, version);
    } catch (Exception e) {
      handleException(e, "trying to get semantic types");
    } finally {
      metadataService.close();
      securityService.close();
    }
    return null;

  }

}
