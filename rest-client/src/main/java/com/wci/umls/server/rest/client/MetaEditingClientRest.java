/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

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
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;

/**
 * A client for connecting to a content REST service.
 */
public class MetaEditingClientRest extends RootClientRest implements
    MetaEditingServiceRest {

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

  @Override
  public ValidationResult addSemanticType(Long projectId, Long conceptId,
    Long lastModified, SemanticTypeComponentJpa semanticTypeComponent,
    boolean overrideWarnings, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "MetaEditing Client - add semantic type to concept" + projectId + ", "
            + conceptId + ", " + semanticTypeComponent.toString() + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url")
            + "/meta/sty/add?projectId=" + projectId + "&conceptId="
            + conceptId + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.json(semanticTypeComponent));

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
  public ValidationResult removeSemanticType(Long projectId, Long conceptId,
    Long lastModified, Long semanticTypeComponentId, boolean overrideWarnings,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "MetaEditing Client - remove semantic type from concept " + projectId
            + ", " + conceptId + ", " + semanticTypeComponentId + ", "
            + lastModified + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/meta/sty/remove/"
            + semanticTypeComponentId + "?projectId=" + projectId
            + "&conceptId=" + conceptId + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response =
        target.request(MediaType.APPLICATION_XML)
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
    Long lastModified, AttributeJpa attribute, boolean overrideWarnings,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "MetaEditing Client - add attribute to concept " + projectId + ", "
            + conceptId + ", " + attribute.toString() + ", " + lastModified
            + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url")
            + "/meta/attribute/add?projectId=" + projectId + "&conceptId="
            + conceptId + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.json(attribute));

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
  public ValidationResult removeAttribute(Long projectId, Long conceptId,
    Long lastModified, Long attributeId, boolean overrideWarnings,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "MetaEditing Client - remove attribute from concept " + projectId
            + ", " + conceptId + ", " + attributeId + ", " + lastModified
            + ", " + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client
            .target(config.getProperty("base.url") + "/meta/attribute/remove/"
                + attributeId + "?projectId=" + projectId + "&conceptId="
                + conceptId + "&lastModified=" + lastModified
                + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response =
        target.request(MediaType.APPLICATION_XML)
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
    Long lastModified, AtomJpa atom, boolean overrideWarnings, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "MetaEditing Client - add atom to concept " + projectId + ", "
            + conceptId + ", " + atom.toString() + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url")
            + "/meta/atom/add?projectId=" + projectId + "&conceptId="
            + conceptId + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.json(atom));

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
  public ValidationResult removeAtom(Long projectId, Long conceptId,
    Long lastModified, Long atomId, boolean overrideWarnings, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "MetaEditing Client - remove atom from concept " + projectId + ", "
            + conceptId + ", " + atomId + ", " + lastModified + ", "
            + overrideWarnings + ", " + authToken);

    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/meta/atom/remove/"
            + atomId + "?projectId=" + projectId + "&conceptId=" + conceptId
            + "&lastModified=" + lastModified
            + (overrideWarnings ? "&overrideWarnings=true" : ""));

    final Response response =
        target.request(MediaType.APPLICATION_XML)
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

}