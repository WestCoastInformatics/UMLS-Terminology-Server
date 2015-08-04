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

   /*
   * (non-Javadoc)
   *
   * @see
   * org.ihtsdo.otf.ts.rest.ValidationServiceRest#validateConcept(org.ihtsdo
   * .otf.ts.rf2.Concept, java.lang.String)
   */
   @Override
  public ValidationResult validateConcept(ConceptJpa c, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/validation/cui");

    String conceptString =
        (c != null ? ConfigUtility.getStringForGraph(c) : null);
    Logger.getLogger(getClass()).info(conceptString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, conceptString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }

   @Override
  public ValidationResult validateAtom(AtomJpa atom, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/validation/aui");

    String atomString =
        (atom != null ? ConfigUtility.getStringForGraph(atom) : null);
    Logger.getLogger(getClass()).info(atomString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, atomString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }

   @Override
  public ValidationResult validateDescriptor(DescriptorJpa descriptor, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/validation/dui");

    String descriptorString =
        (descriptor != null ? ConfigUtility.getStringForGraph(descriptor) : null);
    Logger.getLogger(getClass()).info(descriptorString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, descriptorString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }


  @Override
  public ValidationResult validateCode(CodeJpa code, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/validation/code");

    String codeString =
        (code != null ? ConfigUtility.getStringForGraph(code) : null);
    Logger.getLogger(getClass()).info(codeString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, codeString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ValidationResult result =
        (ValidationResult) ConfigUtility.getGraphForString(resultString,
            ValidationResultJpa.class);
    return result;
  }


  @Override
  public ValidationResult validateMerge(String terminology, String version,
    String cui1, String cui2, String authToken) throws Exception {

      Client client = Client.create();
      WebResource resource =
          client.resource(config.getProperty("base.url") + "/validate/cui/merge/" +
        terminology + "/" + version + "/" + cui1 + "/" + cui2);

      
      ClientResponse response =
          resource.accept(MediaType.APPLICATION_XML)
              .header("Authorization", authToken).get(ClientResponse.class);


      String resultString = response.getEntity(String.class);
      if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
        Logger.getLogger(getClass()).debug(resultString);
      } else {
        throw new Exception(resultString);
      }

      // converting to object
      ValidationResult result =
          (ValidationResult) ConfigUtility.getGraphForString(resultString,
              ValidationResultJpa.class);
      return result;
    }


}
