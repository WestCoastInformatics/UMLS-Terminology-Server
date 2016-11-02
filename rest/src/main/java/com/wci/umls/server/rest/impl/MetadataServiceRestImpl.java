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
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
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

      handleException(e, "trying to get the terminology");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }
  }
  
  /* see superclass */
  @Override
  @GET
  @Path("/rootTerminology/{terminology}")
  @ApiOperation(value = "Get root terminology", notes = "Gets the root terminology for the specified parameters", response = TerminologyJpa.class)
  public RootTerminology getRootTerminology(
    @ApiParam(value = "Terminology name, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "RESTful call (Metadata): /rootTerminology/" + terminology );

    final MetadataService metadataService = new MetadataServiceJpa();
    try {

      // authorize call
      authorizeApp(securityService, authToken, "get root terminology",
          UserRole.VIEWER);

      final RootTerminology termInfo =
          metadataService.getRootTerminology(terminology);
      if (termInfo == null) {
        return new RootTerminologyJpa();
      }
      metadataService.getGraphResolutionHandler(terminology).resolve(termInfo);

      return termInfo;

    } catch (Exception e) {

      handleException(e, "trying to get the root terminology");
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
    @ApiParam(value = "Precedence list to update", required = true) PrecedenceListJpa precedenceList,
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
    @DELETE
    @Path("/termType/{type}/remove/{terminology}/{version}")
    @ApiOperation(value = "Remove a term type", notes = "Remove a term type")
    public void removeTermType(
      @ApiParam(value = "Term type, e.g. AB", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /termType/" + type + "/remove");

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "remove term type ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        TermType tty = metadataService.getTermType(type, terminology, version);
        metadataService.removeTermType(tty.getId());
      } catch (Exception e) {

        handleException(e, "trying to remove the term type");
      } finally {
        metadataService.close();
        securityService.close();
      }

  }

    /* see superclass */
    @Override
    @GET
    @Path("/termType/{type}/{terminology}/{version}")
    @ApiOperation(value = "Retrieve a term type", notes = "Retrieve a term type", response = TermTypeJpa.class)
    public TermType getTermType(
      @ApiParam(value = "Term type, e.g. AB", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /termType/" + type);

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "get term type ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        return metadataService.getTermType(type, terminology, version);
      } catch (Exception e) {

        handleException(e, "trying to retrieve the term type");
        return null;
      } finally {
        metadataService.close();
        securityService.close();
      }

  }
    /* see superclass */
    @Override
    @DELETE
    @Path("/attributeName/{type}/remove/{terminology}/{version}")
    @ApiOperation(value = "Remove a attribute name", notes = "Remove a attribute name")
    public void removeAttributeName(
      @ApiParam(value = "Attribute name, e.g. AMT", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /attributeName/" + type + "/remove");

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "remove attribute name ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        AttributeName atn = metadataService.getAttributeName(type, terminology, version);
        metadataService.removeAttributeName(atn.getId());
      } catch (Exception e) {

        handleException(e, "trying to remove the attribute name");
      } finally {
        metadataService.close();
        securityService.close();
      }

  }

    /* see superclass */
    @Override
    @GET
    @Path("/attributeName/{type}/{terminology}/{version}")
    @ApiOperation(value = "Retrieve a attribute name", notes = "Retrieve a attribute name", response = AttributeNameJpa.class)
    public AttributeName getAttributeName(
      @ApiParam(value = "Attribute name, e.g. AMT", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /attributeName/" + type);

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "get attribute name ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        return metadataService.getAttributeName(type, terminology, version);
      } catch (Exception e) {

        handleException(e, "trying to retrieve the attribute name");
        return null;
      } finally {
        metadataService.close();
        securityService.close();
      }

  }
    /* see superclass */
    @Override
    @DELETE
    @Path("/addRelType/{type}/remove/{terminology}/{version}")
    @ApiOperation(value = "Remove a add relationship type", notes = "Remove a additional relationship type")
    public void removeAdditionalRelationshipType(
      @ApiParam(value = "Additional Relationship type, e.g. RB", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /addRelType/" + type + "/remove");

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "remove add relationship type ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        AdditionalRelationshipType relType = metadataService.getAdditionalRelationshipType(type, terminology, version);
        metadataService.removeAdditionalRelationshipType(relType.getId());
      } catch (Exception e) {

        handleException(e, "trying to remove the add relationship type");
      } finally {
        metadataService.close();
        securityService.close();
      }

  }

    /* see superclass */
    @Override
    @GET
    @Path("/addRelType/{type}/{terminology}/{version}")
    @ApiOperation(value = "Retrieve a additional relationship type", notes = "Retrieve a additional relationship type", response = AdditionalRelationshipTypeJpa.class)
    public AdditionalRelationshipType getAdditionalRelationshipType(
      @ApiParam(value = "Relationship type, e.g. RN", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /addRelType/" + type);

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "get additional relationship type ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        return metadataService.getAdditionalRelationshipType(type, terminology, version);
      } catch (Exception e) {

        handleException(e, "trying to retrieve the additional relationship type");
        return null;
      } finally {
        metadataService.close();
        securityService.close();
      }

  }
        
    /* see superclass */
    @Override
    @DELETE
    @Path("/relationshipType/{type}/remove/{terminology}/{version}")
    @ApiOperation(value = "Remove a rel type", notes = "Remove a rel type")
    public void removeRelationshipType(
      @ApiParam(value = "Relationship type, e.g. AB", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /relationshipType/" + type + "/remove");

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "remove rel type ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        RelationshipType relType = metadataService.getRelationshipType(type, terminology, version);
        metadataService.removeRelationshipType(relType.getId());
      } catch (Exception e) {

        handleException(e, "trying to remove the rel type");
      } finally {
        metadataService.close();
        securityService.close();
      }

  }

    /* see superclass */
    @Override
    @GET
    @Path("/relationshipType/{type}/{terminology}/{version}")
    @ApiOperation(value = "Retrieve a relationship type", notes = "Retrieve a relationship type", response = RelationshipTypeJpa.class)
    public RelationshipType getRelationshipType(
      @ApiParam(value = "Relationship type, e.g. RN", required = true) @PathParam("type") String type,
      @ApiParam(value = "Terminology, e.g. UMLS", required = true) @PathParam("terminology") String terminology,
      @ApiParam(value = "Version, e.g. latest", required = true) @PathParam("version") String version,
      @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
      throws Exception {
      Logger.getLogger(getClass())
          .info("RESTful call (Metadata): /relationshipType/" + type);

      final MetadataService metadataService = new MetadataServiceJpa();
      try {

        final String userName = authorizeApp(securityService, authToken,
            "get relationship type ", UserRole.USER);
        metadataService.setLastModifiedBy(userName);

        return metadataService.getRelationshipType(type, terminology, version);
      } catch (Exception e) {

        handleException(e, "trying to retrieve the relationship type");
        return null;
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

  /* see superclass */
  @Override
  @POST
  @Path("/termType/update")
  @ApiOperation(value = "Update a term type", notes = "Update a term type", response = TermTypeJpa.class)
  public void updateTermType(
    @ApiParam(value = "Term type to update", required = true) TermTypeJpa termType,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /termType/update");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update term type", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updateTermType(termType);
    } catch (Exception e) {
      handleException(e, "trying to update term type");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/attributeName/update")
  @ApiOperation(value = "Update an attribute name", notes = "Update an attribute name", response = AttributeNameJpa.class)
  public void updateAttributeName(
    @ApiParam(value = "Attribute name to update", required = true) AttributeNameJpa attributeName,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /attributeName/update");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update attribute name", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updateAttributeName(attributeName);
    } catch (Exception e) {
      handleException(e, "trying to update attribute name");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/relationshipType/update")
  @ApiOperation(value = "Update a relationship type", notes = "Update a relationship type", response = RelationshipTypeJpa.class)
  public void updateRelationshipType(
    @ApiParam(value = "Relationship type to update", required = true) RelationshipTypeJpa relType,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /relationshipType/update");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update rel type", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updateRelationshipType(relType);
    } catch (Exception e) {
      handleException(e, "trying to update rel type");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  
  /* see superclass */
  @Override
  @POST
  @Path("/rootTerminology/update")
  @ApiOperation(value = "Update a root terminology", notes = "Update a root terminology", response = RootTerminologyJpa.class)
  public void updateRootTerminology(
    @ApiParam(value = "Root terminology to update", required = true) RootTerminologyJpa rootTerminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /rootTerminology/update");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update root terminology", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updateRootTerminology(rootTerminology);
    } catch (Exception e) {
      handleException(e, "trying to update root terminology");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/terminology/update")
  @ApiOperation(value = "Update a terminology", notes = "Update a terminology", response = TerminologyJpa.class)
  public void updateTerminology(
    @ApiParam(value = "Terminology to update", required = true) TerminologyJpa terminology,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /terminology/update");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update terminology", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updateTerminology(terminology);
    } catch (Exception e) {
      handleException(e, "trying to update terminology");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/addRelType/update")
  @ApiOperation(value = "Update a relationship type", notes = "Update a relationship type", response = AdditionalRelationshipTypeJpa.class)
  public void updateAdditionalRelationshipType(
    @ApiParam(value = "AdditionalRelationship type to update", required = true) AdditionalRelationshipTypeJpa relType,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /addRelType/update");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "update add rel type", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      metadataService.updateAdditionalRelationshipType(relType);
    } catch (Exception e) {
      handleException(e, "trying to update add rel type");
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/termType/add")
  @ApiOperation(value = "Add a term type", notes = "Add a term type", response = TermTypeJpa.class)
  public TermType addTermType(
    @ApiParam(value = "Term type to add", required = true) TermTypeJpa termType,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /termType/add");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add term type", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      return metadataService.addTermType(termType);
    } catch (Exception e) {
      handleException(e, "trying to add term type");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/attributeName/add")
  @ApiOperation(value = "Add an attribute name", notes = "Add an attribute name", response = AttributeNameJpa.class)
  public AttributeName addAttributeName(
    @ApiParam(value = "Attribute name to add", required = true) AttributeNameJpa attributeName,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /attributeName/add");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add attribute name", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      return metadataService.addAttributeName(attributeName);
    } catch (Exception e) {
      handleException(e, "trying to add attribute name");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/relationshipType/add")
  @ApiOperation(value = "Add a relationship type", notes = "Add a relationship type", response = RelationshipTypeJpa.class)
  public RelationshipType addRelationshipType(
    @ApiParam(value = "Relationship type to add", required = true) RelationshipTypeJpa relationshipType,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /relationshipType/add");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add relationship type", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      return metadataService.addRelationshipType(relationshipType);
    } catch (Exception e) {
      handleException(e, "trying to add relationship type");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
  
  /* see superclass */
  @Override
  @POST
  @Path("/addRelType/add")
  @ApiOperation(value = "Add a term type", notes = "Add a term type", response = AdditionalRelationshipTypeJpa.class)
  public AdditionalRelationshipType addAdditionalRelationshipType(
    @ApiParam(value = "AdditionalRelationship type to add", required = true) AdditionalRelationshipTypeJpa addRelType,
    @ApiParam(value = "Authorization token, e.g. 'guest'", required = true) @HeaderParam("Authorization") String authToken)
    throws Exception {

    Logger.getLogger(getClass())
        .info("RESTful call (Metadata): /addRelType/add");

    final MetadataService metadataService = new MetadataServiceJpa();
    try {
      final String userName = authorizeApp(securityService, authToken,
          "add term type", UserRole.USER);
      metadataService.setLastModifiedBy(userName);

      return metadataService.addAdditionalRelationshipType(addRelType);
    } catch (Exception e) {
      handleException(e, "trying to add term type");
      return null;
    } finally {
      metadataService.close();
      securityService.close();
    }

  }
}
