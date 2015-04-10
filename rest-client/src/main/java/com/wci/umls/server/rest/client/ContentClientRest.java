/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;

/**
 * A client for connecting to a content REST service.
 */
public class ContentClientRest implements ContentServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ContentClientRest(Properties config) {
    this.config = config;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#loadTerminologyRrf(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void loadTerminologyRrf(String terminology, String version,
    String inputDir, String authToken) throws Exception {

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/terminology/load/rf2/snapshot" + terminology + "/" + version);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, inputDir);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#computeTransitiveClosure(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void computeTransitiveClosure(String terminology, String version,
    String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/terminology/closure/compute/" + terminology + "/"
            + version);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#luceneReindex(java.lang.String, java.lang.String)
   */
  @Override
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception {
    Client client = Client.create();

    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/reindex");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.TEXT_PLAIN)
            .post(ClientResponse.class, indexedObjects);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      if (response.getStatus() != 204)
        throw new Exception("Unexpected status " + response.getStatus());
    }

  }


  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#removeTerminology(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void removeTerminology(String terminology, String version,
    String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/terminology/remove/"
            + terminology + "/" + version);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .delete(ClientResponse.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

}
