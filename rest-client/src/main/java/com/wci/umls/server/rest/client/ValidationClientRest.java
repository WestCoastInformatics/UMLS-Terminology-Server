/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
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
// TODO
//  /*
//   * (non-Javadoc)
//   * 
//   * @see
//   * org.ihtsdo.otf.ts.rest.ValidationServiceRest#validateConcept(org.ihtsdo
//   * .otf.ts.rf2.Concept, java.lang.String)
//   */
//  @Override
//  public ValidationResult validateConcept(ConceptJpa concept, String authToken)
//    throws Exception {
//    Client client = Client.create();
//    WebResource resource =
//        client.resource(config.getProperty("base.url") + "/validation/concept");
//
//    String conceptString =
//        (concept != null ? ConfigUtility.getStringForGraph(concept) : null);
//    Logger.getLogger(getClass()).info(conceptString);
//    ClientResponse response =
//        resource.accept(MediaType.APPLICATION_XML)
//            .header("Authorization", authToken)
//            .header("Content-type", MediaType.APPLICATION_XML)
//            .post(ClientResponse.class, conceptString);
//
//    String resultString = response.getEntity(String.class);
//    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
//      Logger.getLogger(getClass()).debug(resultString);
//    } else {
//      throw new Exception(resultString);
//    }
//
//    // converting to object
//    ValidationResult result =
//        (ValidationResult) ConfigUtility.getGraphForString(resultString,
//            ValidationResultJpa.class);
//    return result;
//  }

}
