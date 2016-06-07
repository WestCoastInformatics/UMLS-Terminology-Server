/**
 * Copyright 2016 West Coast Informatics, LLC
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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.services.rest.MetaEditingServiceRest;
import com.wci.umls.server.model.content.Concept;

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


  @Override
  public Concept addSemanticType(Long projectId, Long conceptId,
    SemanticTypeComponentJpa semanticTypeComponent, String authToken)
      throws Exception {
    Logger.getLogger(getClass()).debug("MetaEditing Client - add semantic type to concept"
        + projectId + ", " + conceptId + ", " + semanticTypeComponent.toString() + ", " + authToken);

    validateNotEmpty( projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/meta/sty/add?projectId=" + projectId + "&conceptId=" + conceptId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.json(semanticTypeComponent));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    Concept c = ConfigUtility.getGraphForString(resultString,
        ConceptJpa.class);
    return c;
  }

  @Override
  public Concept removeSemanticType(Long projectId, Long conceptId,
    Long semanticTypeComponentId, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("MetaEditing Client - remove semantic type from concept "
        + projectId + ", " + conceptId + ", " + semanticTypeComponentId + ", " + authToken);

    validateNotEmpty( projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/meta/sty/remove/" + semanticTypeComponentId + "?projectId=" + projectId + "&conceptId=" + conceptId);

    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(null);

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    Concept c = ConfigUtility.getGraphForString(resultString,
        ConceptJpa.class);
    return c;
  }

}