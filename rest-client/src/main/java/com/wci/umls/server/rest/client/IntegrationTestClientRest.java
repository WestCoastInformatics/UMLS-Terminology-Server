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
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.jpa.services.rest.IntegrationTestServiceRest;
import com.wci.umls.server.jpa.workflow.WorklistJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * A client for connecting to an integration test REST service.
 */
public class IntegrationTestClientRest extends RootClientRest
    implements IntegrationTestServiceRest {

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
    Logger.getLogger(getClass())
        .debug("Integration Test Client - add concept" + concept);

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/test/concept/add");

    final String conceptString = ConfigUtility
        .getStringForGraph(concept == null ? new ConceptJpa() : concept);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(conceptString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, ConceptJpa.class);

  }

  /* see superclass */
  @Override
  public void updateConcept(ConceptJpa concept, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - update concept" + concept);

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/test/concept/update");

    final String conceptString = ConfigUtility
        .getStringForGraph(concept == null ? new ConceptJpa() : concept);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(conceptString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

  }

  /* see superclass */
  @Override
  public void removeConcept(Long id, boolean cascade, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - remove concept " + id);
    validateNotEmpty(id, "id");
    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(config.getProperty("base.url")
        + "/test/concept/remove/" + id + (cascade ? "?cascade=true" : ""));

    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public void updateAtom(AtomJpa atom, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - update atom" + atom);

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/test/atom/update");

    final String atomString =
        ConfigUtility.getStringForGraph(atom == null ? new AtomJpa() : atom);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(atomString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

  }

  /* see superclass */
  @Override
  public ConceptRelationship addRelationship(
    ConceptRelationshipJpa relationship, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - add relationship" + relationship);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/test/concept/relationship/add");

    final String relString = ConfigUtility.getStringForGraph(
        relationship == null ? new ConceptRelationshipJpa() : relationship);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(relString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ConceptRelationshipJpa.class);
  }

  /* see superclass */
  @Override
  public AtomRelationship addRelationship(AtomRelationshipJpa relationship,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - add relationship" + relationship);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(config.getProperty("base.url") + "/test/atom/relationship/add");

    final String relString = ConfigUtility.getStringForGraph(
        relationship == null ? new AtomRelationshipJpa() : relationship);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(relString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        AtomRelationshipJpa.class);
  }

  /* see superclass */
  @Override
  public void updateRelationship(ConceptRelationshipJpa relationship,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - update relationship" + relationship);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/test/concept/relationship/update");

    final String relString = ConfigUtility.getStringForGraph(
        relationship == null ? new ConceptRelationshipJpa() : relationship);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(relString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

  }

  /* see superclass */
  @Override
  public Worklist getWorklist(Long worklistId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - get worklist: " + worklistId);

    validateNotEmpty(worklistId, "worklistId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/test/worklist/" + worklistId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, WorklistJpa.class);
  }

  @Override
  public Atom getAtom(Long atomId, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - get atom: " + atomId);

    validateNotEmpty(atomId, "atomId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/test/atom/" + atomId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, AtomJpa.class);
  }

  @Override
  public SemanticTypeComponent getSemanticTypeComponent(Long styId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Integration Test Client - get semantic type component: " + styId);

    validateNotEmpty(styId, "styId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target =
        client.target(config.getProperty("base.url") + "/test/sty/" + styId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        SemanticTypeComponentJpa.class);
  }

  @Override
  public ConceptRelationship getConceptRelationship(Long relId,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - get concept relationship : " + relId);

    validateNotEmpty(relId, "relId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/test/concept/relationship/" + relId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString,
        ConceptRelationshipJpa.class);
  }

  @Override
  public Attribute getAttribute(Long attributeId, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - get attribute : " + attributeId);

    validateNotEmpty(attributeId, "attributeId");

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client.target(
        config.getProperty("base.url") + "/test/attribute/" + attributeId);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    if (response.getStatus() == 204) {
      return null;
    }

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, AttributeJpa.class);
  }

  /* see superclass */
  @Override
  public TypeKeyValue addTypeKeyValue(TypeKeyValueJpa typeKeyValue,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Integration Test Client - add typeKeyValue" + typeKeyValue);

    final Client client = ClientBuilder.newClient();
    final WebTarget target = client
        .target(config.getProperty("base.url") + "/test/typeKeyValue/add");

    final String typeKeyValueString = ConfigUtility.getStringForGraph(
        typeKeyValue == null ? new ConceptJpa() : typeKeyValue);
    final Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(typeKeyValueString));

    final String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, TypeKeyValue.class);
  }

}
