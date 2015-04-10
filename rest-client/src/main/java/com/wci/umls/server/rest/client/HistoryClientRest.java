package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#getReleaseHistory(java.lang.String
   * , java.lang.String)
   */
  @Override
  public ReleaseInfoList getReleaseHistory(String terminology, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/history/releases/"
            + terminology);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.getEntity(String.class);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#getCurrentReleaseInfo(java.lang
   * .String)
   */
  @Override
  public ReleaseInfo getCurrentReleaseInfo(String terminology, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/history/release/"
            + terminology + "/current");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.getEntity(String.class);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#getPreviousReleaseInfo(java.lang
   * .String, java.lang.String)
   */
  @Override
  public ReleaseInfo getPreviousReleaseInfo(String terminology, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/history/release/"
            + terminology + "/previous");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.getEntity(String.class);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#getPlannedReleaseInfo(java.lang
   * .String, java.lang.String)
   */
  @Override
  public ReleaseInfo getPlannedReleaseInfo(String terminology, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/history/release/"
            + terminology + "/planned");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.getEntity(String.class);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#getReleaseInfo(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public ReleaseInfo getReleaseInfo(String terminology, String name,
    String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/history/release/"
            + terminology + "/" + name);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.getEntity(String.class);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#addReleaseInfo(org.ihtsdo.otf
   * .ts.helpers.ReleaseInfo, java.lang.String)
   */
  @Override
  public ReleaseInfo addReleaseInfo(ReleaseInfoJpa releaseInfo, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client
            .resource(config.getProperty("base.url") + "/history/release/add");
    String riString =
        ConfigUtility.getStringForGraph(releaseInfo == null
            ? new ReleaseInfoJpa() : releaseInfo);
    Logger.getLogger(this.getClass()).debug(riString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, riString);

    String resultString = response.getEntity(String.class);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#updateReleaseInfo(org.ihtsdo.
   * otf.ts.helpers.ReleaseInfo, java.lang.String)
   */
  @Override
  public void updateReleaseInfo(ReleaseInfoJpa releaseInfo, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/history/release/update");
    String riString =
        ConfigUtility.getStringForGraph(releaseInfo == null
            ? new ReleaseInfoJpa() : releaseInfo);
    Logger.getLogger(this.getClass()).debug(riString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, riString);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.HistoryServiceRest#removeReleaseInfo(java.lang.Long,
   * java.lang.String)
   */
  @Override
  public void removeReleaseInfo(Long id, String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/history/release/remove/" + id);

    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete(ClientResponse.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

}
