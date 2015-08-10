/**
 * Copyright 2015 West Coast Informatics, LLC
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

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.helpers.ReleaseInfoListJpa;
import com.wci.umls.server.jpa.services.rest.HistoryServiceRest;

/**
 * A client for connecting to a history REST service.
 */
public class HistoryClientRest implements HistoryServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public HistoryClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public ReleaseInfoList getReleaseHistory(String terminology, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/releases/"
            + terminology);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(this.getClass()).debug(
          resultString.substring(0, Math.min(resultString.length(), 3999)));
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoListJpa c =
        (ReleaseInfoListJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoListJpa.class);
    return c;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getCurrentReleaseInfo(String terminology, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/release/"
            + terminology + "/current");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(this.getClass()).debug(
          resultString.substring(0, Math.min(resultString.length(), 3999)));
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoJpa info =
        (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoJpa.class);
    return info;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getPreviousReleaseInfo(String terminology, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/release/"
            + terminology + "/previous");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(this.getClass()).debug(
          resultString.substring(0, Math.min(resultString.length(), 3999)));
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoJpa info =
        (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoJpa.class);
    return info;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getPlannedReleaseInfo(String terminology, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/release/"
            + terminology + "/planned");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(this.getClass()).debug(
          resultString.substring(0, Math.min(resultString.length(), 3999)));
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoJpa info =
        (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoJpa.class);
    return info;
  }

  /* see superclass */
  @Override
  public ReleaseInfo getReleaseInfo(String terminology, String name,
    String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/release/"
            + terminology + "/" + name);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(this.getClass()).debug(
          resultString.substring(0, Math.min(resultString.length(), 3999)));
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoJpa info =
        (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoJpa.class);
    return info;
  }

  /* see superclass */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfoJpa releaseInfo, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/history/release/add");
    String riString =
        ConfigUtility.getStringForGraph(releaseInfo == null
            ? new ReleaseInfoJpa() : releaseInfo);
    Logger.getLogger(this.getClass()).debug(riString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.xml(riString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(this.getClass()).debug(
          resultString.substring(0, Math.min(resultString.length(), 3999)));
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReleaseInfoJpa info =
        (ReleaseInfoJpa) ConfigUtility.getGraphForString(resultString,
            ReleaseInfoJpa.class);

    return info;
  }

  /* see superclass */
  @Override
  public void updateReleaseInfo(ReleaseInfoJpa releaseInfo, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/history/release/update");
    String riString =
        ConfigUtility.getStringForGraph(releaseInfo == null
            ? new ReleaseInfoJpa() : releaseInfo);
    Logger.getLogger(this.getClass()).debug(riString);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.xml(riString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void removeReleaseInfo(Long id, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/history/release/remove/" + id);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void startEditingCycle(String releaseVersion, String terminology,
    String version, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/history/release/startEditingCycle/" + releaseVersion + "/"
            + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.text(""));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }
  }
}
