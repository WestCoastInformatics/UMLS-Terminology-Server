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
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;

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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#loadTerminologyRrf
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * computeTransitiveClosure(java.lang.String, java.lang.String,
   * java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#luceneReindex(
   * java.lang.String, java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#removeTerminology
   * (java.lang.String, java.lang.String, java.lang.String)
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

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#getConcept(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept " + terminologyId + ", " + terminology
            + ", " + version);
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId + "/"
            + Branch.ROOT);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptJpa concept =
        (ConceptJpa) ConfigUtility.getGraphForString(resultString,
            ConceptJpa.class);
    return concept;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#findConceptsForQuery(java.lang.String, java.lang.String, java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
 
    Logger.getLogger(getClass()).debug(
        "Content Client - find concepts " + terminology + ", " + version + ", "
            + query + ", " + pfs);

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + Branch.ROOT + "/query/" + query);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Logger.getLogger(getClass()).debug(pfsString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchResultList findLexicalClassesForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SearchResultList findStringClassesForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
