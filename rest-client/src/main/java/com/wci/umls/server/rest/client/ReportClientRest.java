package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import com.wci.umls.server.jpa.services.rest.ReportServiceRest;

/**
 * A client for connecting to a history REST service.
 */
public class ReportClientRest extends RootClientRest
    implements ReportServiceRest {

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
  public String getConceptReport(Long conceptId, String authToken)
    throws Exception {

    validateNotEmpty(conceptId, "conceptId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/report/concept/" + conceptId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    return resultString;
  }

  /* see superclass */
  @Override
  public String getDescriptorReport(Long descriptorId, String authToken)
    throws Exception {

    validateNotEmpty(descriptorId, "descriptorId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/report/descriptor/" + descriptorId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    return resultString;
  }

  /* see superclass */
  @Override
  public String getCodeReport(Long codeId, String authToken) throws Exception {

    validateNotEmpty(codeId, "codeId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/report/code/" + codeId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
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