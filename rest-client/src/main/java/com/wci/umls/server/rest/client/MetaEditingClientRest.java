/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;

/**
 * A client for connecting to a content REST service.
 */
public class MetaEditingClientRest extends RootClientRest
    implements MetaEditingServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link MetaEditingClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public MetaEditingClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public ValidationResult addSemanticType(Long projectId, Long conceptId,
    String activityId, Long lastModified, String semanticType,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - add semantic type to concept" + projectId
            + ", " + conceptId + ", " + semanticType.toString() + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/sty/add?projectId=" + projectId + "&conceptId=" + conceptId
        + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified + "&semanticType=" + semanticType
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult removeSemanticType(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long semanticTypeComponentId,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - remove semantic type from concept "
            + projectId + ", " + conceptId + ", " + semanticTypeComponentId
            + ", " + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/sty/remove/" + semanticTypeComponentId + "?projectId="
        + projectId + "&conceptId=" + conceptId
        + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);

  }

  /* see superclass */
  @Override
  public ValidationResult addAttribute(Long projectId, Long conceptId,
    String activityId, Long lastModified, AttributeJpa attribute,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - add attribute to concept " + projectId
            + ", " + conceptId + ", " + attribute.toString() + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/attribute/add?projectId=" + projectId + "&conceptId="
        + conceptId + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final String attributeString = ConfigUtility
        .getJsonForGraph(attribute == null ? new AttributeJpa() : attribute);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(attributeString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);

  }

  /* see superclass */
  @Override
  public ValidationResult removeAttribute(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long attributeId,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - remove attribute from concept " + projectId
            + ", " + conceptId + ", " + attributeId + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/meta/attribute/remove/" + attributeId
            + "?projectId=" + projectId + "&conceptId=" + conceptId
            + (activityId == null ? "" : "&activityId=" + activityId)
            + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult addAtom(Long projectId, Long conceptId,
    String activityId, Long lastModified, AtomJpa atom,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - add atom to concept " + projectId + ", "
            + conceptId + ", " + atom.toString() + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/atom/add?projectId=" + projectId + "&conceptId=" + conceptId
        + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final String atomString =
        ConfigUtility.getJsonForGraph(atom == null ? new AtomJpa() : atom);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(atomString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult removeAtom(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long atomId, boolean overrideWarnings,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - remove atom from concept " + projectId
            + ", " + conceptId + ", " + atomId + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/meta/atom/remove/"
            + atomId + "?projectId=" + projectId + "&conceptId=" + conceptId
            + (activityId == null ? "" : "&activityId=" + activityId)
            + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult updateAtom(Long projectId, Long conceptId,
    String activityId, Long lastModified, AtomJpa atom,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - update atom on concept " + projectId + ", "
            + conceptId + ", " + atom.toString() + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/atom/update?projectId=" + projectId + "&conceptId=" + conceptId
        + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final String atomString =
        ConfigUtility.getJsonForGraph(atom == null ? new AtomJpa() : atom);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(atomString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult addRelationship(Long projectId, Long conceptId,
    String activityId, Long lastModified, ConceptRelationshipJpa relationship,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - add relationship to concept " + projectId
            + ", " + conceptId + ", " + relationship.toString() + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/relationship/add?projectId=" + projectId + "&conceptId="
        + conceptId + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    String relString = ConfigUtility.getJsonForGraph(
        relationship == null ? new ConceptRelationshipJpa() : relationship);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(relString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult removeRelationship(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long relationshipId,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - remove relationship from concept "
            + projectId + ", " + conceptId + ", " + relationshipId + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/relationship/remove/" + relationshipId + "?projectId="
        + projectId + "&conceptId=" + conceptId
        + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult addDemotion(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long conceptId2,
    AtomRelationshipJpa demotion, boolean overrideWarnings, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - add demotion " + projectId + ", "
            + conceptId + ", " + demotion.toString() + ", " + lastModified
            + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    validateNotEmpty(conceptId2, "conceptId2");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/demotion/add?projectId=" + projectId + "&conceptId="
        + conceptId + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified + "&conceptId2=" + conceptId2
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    String relString = ConfigUtility.getJsonForGraph(
        demotion == null ? new AtomRelationshipJpa() : demotion);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(relString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult mergeConcepts(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long conceptId2,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - merge concept " + conceptId
            + " with concept " + conceptId2 + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    validateNotEmpty(conceptId2, "conceptId2");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/concept/merge?projectId=" + projectId + "&conceptId="
        + conceptId + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified + "&conceptId2=" + conceptId2
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(null));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult moveAtoms(Long projectId, Long conceptId,
    String activityId, Long lastModified, Long conceptId2, List<Long> atomIds,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - move atoms " + atomIds + " from concept "
            + conceptId + " to concept " + conceptId2 + ", " + lastModified
            + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    validateNotEmpty(conceptId2, "conceptId2");
    validateNotEmpty(atomIds.toString(), "atoms");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/atom/move?projectId=" + projectId + "&conceptId=" + conceptId
        + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified + "&conceptId2=" + conceptId2
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    String atomIdsString = ConfigUtility
        .getJsonForGraph(atomIds == null ? new ArrayList<Long>() : atomIds);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(atomIdsString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult splitConcept(Long projectId, Long conceptId,
    String activityId, Long lastModified, List<Long> atomIds,
    boolean overrideWarnings, boolean copyRelationships,
    boolean copySemanticTypes, String relationshipType, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - splitting " + atomIds
            + " atoms out of concept " + conceptId + " into new concept, "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/concept/split?projectId=" + projectId + "&conceptId="
        + conceptId + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : "")
        + (copyRelationships ? "&copyRelationships=true" : "")
        + (copySemanticTypes ? "&copySemanticTypes=true" : "")
        + "&relationshipType=" + relationshipType);

    String atomIdsString = ConfigUtility
        .getJsonForGraph(atomIds == null ? new ArrayList<Long>() : atomIds);

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(atomIdsString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  /* see superclass */
  @Override
  public ValidationResult approveConcept(Long projectId, Long conceptId,
    String activityId, Long lastModified, boolean overrideWarnings,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("MetaEditing Client - approve concept " + conceptId + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/meta/concept/approve?projectId=" + projectId + "&conceptId="
        + conceptId + (activityId == null ? "" : "&activityId=" + activityId)
        + "&lastModified=" + lastModified
        + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(null));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  @Override
  public ValidationResult undoAction(Long projectId, Long molecularActionId,
    String activityId, boolean force, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("MetaEditing Client - undo action "
        + molecularActionId + ", " + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(molecularActionId, "molecularActionId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(config.getProperty("base.url") + "/meta/action/undo?projectId="
            + projectId + "&molecularActionId=" + molecularActionId
            + (activityId == null ? "" : "&activityId=" + activityId)
            + (force ? "&force=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(null));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

  @Override
  public ValidationResult redoAction(Long projectId, Long molecularActionId,
    String activityId, boolean force, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("MetaEditing Client - redo action "
        + molecularActionId + ", " + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(molecularActionId, "molecularActionId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(config.getProperty("base.url") + "/meta/action/redo?projectId="
            + projectId + "&molecularActionId=" + molecularActionId
            + (activityId == null ? "" : "&activityId=" + activityId)
            + (force ? "&force=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(null));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ValidationResultJpa.class);
  }

}