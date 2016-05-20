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

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.services.rest.ValidationServiceRest;

/**
 * A client for connecting to a validation REST service.
 */
public class ValidationClientRest implements ValidationServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ValidationClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public ValidationResult validateConcept(ConceptJpa c, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validation/concept");

    String conceptString =
        (c != null ? ConfigUtility.getStringForGraph(c) : null);
    Logger.getLogger(getClass()).info(conceptString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(conceptString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        ConfigUtility
            .getGraphForString(resultString, ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateAtom(AtomJpa atom, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validation/aui");

    String atomString =
        (atom != null ? ConfigUtility.getStringForGraph(atom) : null);
    Logger.getLogger(getClass()).info(atomString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(atomString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        ConfigUtility
            .getGraphForString(resultString, ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateDescriptor(DescriptorJpa descriptor,
    String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validation/descriptor");

    String descriptorString =
        (descriptor != null ? ConfigUtility.getStringForGraph(descriptor)
            : null);
    Logger.getLogger(getClass()).info(descriptorString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.xml(descriptorString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        ConfigUtility
            .getGraphForString(resultString, ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateCode(CodeJpa code, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validation/code");

    String codeString =
        (code != null ? ConfigUtility.getStringForGraph(code) : null);
    Logger.getLogger(getClass()).info(codeString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(codeString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        ConfigUtility
            .getGraphForString(resultString, ValidationResultJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateMerge(String terminology, String version,
    String cui1, String cui2, String authToken) throws Exception {

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/validate/concept/merge/"
            + terminology + "/" + version + "/" + cui1 + "/" + cui2);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        ConfigUtility
            .getGraphForString(resultString, ValidationResultJpa.class);
    return result;
  }

  @Override
  public KeyValuePairList getValidationChecks(String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/checks");

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    KeyValuePairList result =
        ConfigUtility.getGraphForString(resultString, KeyValuePairList.class);
    return result;
  }

}
