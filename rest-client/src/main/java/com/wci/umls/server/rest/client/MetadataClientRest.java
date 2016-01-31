/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.model.meta.Terminology;

/**
 * A client for connecting to a metadata REST service.
 */
public class MetadataClientRest extends RootClientRest implements
    MetadataServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public MetadataClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public KeyValuePairLists getAllMetadata(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Client - get all metadata " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/metadata/all/terminology/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuePairLists result =
        ConfigUtility.getGraphForString(resultString, KeyValuePairLists.class);
    return result;
  }

  /* see superclass */
  @Override
  public TerminologyList getAllTerminologiesLatestVersions(String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Client - get all terminologies latest versions");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/metadata/terminology/terminologies/latest");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TerminologyList result =
        ConfigUtility.getGraphForString(resultString, TerminologyList.class);
    return result;
  }

  /* see superclass */
  @Override
  public TerminologyList getTerminologies(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Client - get all terminologyies versions");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/metadata/terminology/terminologies");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TerminologyList result =
        ConfigUtility.getGraphForString(resultString, TerminologyList.class);
    return result;
  }

  /* see superclass */
  @Override
  public Terminology getTerminology(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Client - get terminology " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/terminology/"
            + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    Terminology result =
        ConfigUtility.getGraphForString(resultString, TerminologyJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Client - get default precedence list " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/precedence/"
            + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    PrecedenceList result =
        ConfigUtility.getGraphForString(resultString, PrecedenceListJpa.class);
    return result;
  }
}
