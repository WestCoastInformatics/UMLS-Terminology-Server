package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.jpa.services.rest.ReportServiceRest;

/**
 * A client for connecting to a history REST service.
 */
public class ReportClientRest implements ReportServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ReportClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ReportClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public String getConceptReport(Long projectId, Long conceptId, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/report/concept" +
         "?projectId=" + projectId + "&conceptId=" + conceptId);
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

    
    return resultString;
  }

}