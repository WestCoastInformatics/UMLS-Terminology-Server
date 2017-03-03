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
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.services.rest.SimpleEditServiceRest;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * A client for connecting to a simple edit REST service.
 */
public class SimpleEditClientRest extends RootClientRest
    implements SimpleEditServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link SimpleEditClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public SimpleEditClientRest(Properties config) {
    this.config = config;
  }

  /**
   * Adds the atom.
   *
   * @param projectId the project id
   * @param conceptId the concept id
   * @param atom the atom
   * @param authToken the auth token
   * @return the atom
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public Atom addAtomToConcept(Long projectId, Long conceptId, AtomJpa atom,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Simple Edit Client - add atom "
        + projectId + ", " + conceptId + ", " + atom);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/simple/atom?projectId=" + projectId + "&conceptId=" + conceptId);
    String atomString =
        ConfigUtility.getStringForGraph(atom == null ? new AtomJpa() : atom);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).put(Entity.xml(atomString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, AtomJpa.class);
  }

  /* see superclass */
  @Override
  public void updateAtom(Long projectId, Long conceptId, AtomJpa atom,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Simple Edit Client - update atom " + projectId + ", " + atom);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/simple/atom?projectId=" + projectId + "&conceptId=" + conceptId);
    String atomString =
        ConfigUtility.getStringForGraph(atom == null ? new AtomJpa() : atom);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(atomString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  /* see superclass */
  @Override
  public void removeAtom(Long projectId, Long conceptId, Long atomId,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Simple Edit Client - remove atom "
        + projectId + ", " + conceptId + ", " + atomId);
    validateNotEmpty(projectId, "projectId");
    validateNotEmpty(conceptId, "conceptId");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/simple/atom/" + atomId
            + "?projectId=" + projectId + "&conceptId=" + conceptId);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public SemanticTypeComponent addSemanticTypeToConcept(Long projectId,
    Long conceptId, SemanticTypeJpa semanticType, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeSemanticType(Long projectId, Long conceptId,
    Long semanticTypeId, String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Concept addConcept(Long projectId, ConceptJpa concept,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateConcept(Long projectId, ConceptJpa concept,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeConcept(Long projectId, Long conceptId, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeConcepts(Long projectId, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    
  }
}
