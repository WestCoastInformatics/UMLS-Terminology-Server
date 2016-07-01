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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.services.rest.IntegrationTestServiceRest;
import com.wci.umls.server.jpa.worfklow.WorklistJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * A client for connecting to an integration test REST service.
 */
public class IntegrationTestClientRest extends RootClientRest implements
    IntegrationTestServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link IntegrationTestClientRest} from the specified
   * parameters.
   *
   * @param config the config
   */
  public IntegrationTestClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public Concept addConcept(ConceptJpa concept, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Integration Test Client - add concept" + concept);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/test/concept/add");

    String conceptString =
        ConfigUtility.getStringForGraph(concept == null ? new ConceptJpa()
            : concept);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(conceptString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ConceptJpa result =
        ConfigUtility.getGraphForString(resultString, ConceptJpa.class);

    return result;
  }

  /* see superclass */
  @Override
  public void removeConcept(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Integration Test Client - remove concept " + id);
    validateNotEmpty(id, "id");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/test/concept/remove/"
            + id);

    if (id == null)
      return;

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public Worklist addWorklist(WorklistJpa worklist, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Integration Test Client - add worklist" + worklist.toString() + ", "
            + authToken);

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/test/worklist/add");

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.json(worklist));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    Worklist v =
        ConfigUtility.getGraphForString(resultString, WorklistJpa.class);
    return v;
  }

  /* see superclass */
  @Override
  public void removeWorklist(Long worklistId, boolean cascade, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Integration Test Client - remove worklist " + worklistId + ", "
            + authToken);

    validateNotEmpty(worklistId, "worklistId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/test/worklist/" + worklistId + "/remove?cascade=" + cascade);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  /* see superclass */
  @Override
  public Worklist getWorklist(Long worklistId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Integration Test Client - get worklist: " + worklistId);

    validateNotEmpty(worklistId, "worklistId");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/test/worklist/"
            /*+ "?worklistId="*/ + worklistId);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, WorklistJpa.class);
  }

}
