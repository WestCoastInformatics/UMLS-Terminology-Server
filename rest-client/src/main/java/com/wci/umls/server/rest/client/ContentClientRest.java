/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreeListJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;

/**
 * A client for connecting to a content REST service.
 */
public class ContentClientRest extends RootClientRest implements
    ContentServiceRest {

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
   * (java.lang.String, java.lang.String, boolean, java.lang.String,
   * java.lang.String)
   */
  @Override
  public void loadTerminologyRrf(String terminology, String version,
    boolean singleMode, String inputDir, String authToken) throws Exception {

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputDir, "inputDir");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/terminology/load/rrf/" + singleMode + "/" + terminology + "/"
            + version);
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

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

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
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#computeTreePositions
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void computeTreePositions(String terminology, String version,
    String authToken) throws Exception {

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/terminology/treepos/compute/" + terminology + "/"
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
    validateNotEmpty(indexedObjects, "indexedObjects");

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
    Logger.getLogger(getClass()).debug(
        "Content Client - remove terminology " +  terminology
            + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/terminology/remove/"
            + terminology + "/" + version);
    /*ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .delete(ClientResponse.class);*/

    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).delete(ClientResponse.class);
    
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
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getConcept(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept " + terminologyId + ", " + terminology
            + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptJpa concept =
        (ConceptJpa) ConfigUtility.getGraphForString(resultString,
            ConceptJpa.class);
    return concept;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findConceptsForQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfscParameterJpa, java.lang.String)
   */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfscParameterJpa pfsc, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find concepts " + terminology + ", " + version + ", "
            + query + ", " + pfsc);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();

    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/cui/"
            + terminology
            + "/"
            + version
            + "/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfsc == null ? new PfscParameterJpa()
            : pfsc);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findConceptsForQuery
   * (java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public SearchResultList findConceptsForGeneralQuery(String luceneQuery,
    String hqlQuery, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find concepts " + luceneQuery + ", " + hqlQuery
            + ", " + pfs);

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/cui"
            + "/luceneQuery/"
            + (luceneQuery == null || luceneQuery.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(
                    luceneQuery, "UTF8").replaceAll("\\+", "%20"))
            + "/hqlQuery/"
            + (hqlQuery == null || hqlQuery.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(hqlQuery,
                    "UTF8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#autocompleteConcepts
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - autocomplete concepts " + terminology + ", "
            + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getDescriptor(
   * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get descriptor " + terminologyId + ", " + terminology
            + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/" + terminologyId);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    DescriptorJpa descriptor =
        (DescriptorJpa) ConfigUtility.getGraphForString(resultString,
            DescriptorJpa.class);
    return descriptor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescriptorsForQuery(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfscParameterJpa,
   * java.lang.String)
   */
  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String query, PfscParameterJpa pfsc, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descriptors " + terminology + ", " + version
            + ", " + query + ", " + pfsc);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/dui/"
            + terminology
            + "/"
            + version
            + "/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfsc == null ? new PfscParameterJpa()
            : pfsc);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescriptorsForQuery(java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public SearchResultList findDescriptorsForGeneralQuery(String luceneQuery,
    String hqlQuery, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descriptors " + luceneQuery + ", " + hqlQuery
            + ", " + pfs);

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/dui"
            + "/luceneQuery/"
            + (luceneQuery == null || luceneQuery.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(
                    luceneQuery, "UTF8").replaceAll("\\+", "%20"))
            + "/hqlQuery/"
            + (hqlQuery == null || hqlQuery.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(hqlQuery,
                    "UTF8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * autocompleteDescriptors(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - autocomplete descriptors " + terminology + ", "
            + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getCode(java.lang
   * .String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get code " + terminologyId + ", " + terminology
            + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/" + terminologyId);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    CodeJpa descriptor =
        (CodeJpa) ConfigUtility.getGraphForString(resultString, CodeJpa.class);
    return descriptor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findCodesForQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfscParameterJpa, java.lang.String)
   */
  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String query, PfscParameterJpa pfsc, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find codes " + terminology + ", " + version + ", "
            + query + ", " + pfsc);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/code/"
            + terminology
            + "/"
            + version
            + "/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfsc == null ? new PfscParameterJpa()
            : pfsc);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findCodesForQuery
   * (java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public SearchResultList findCodesForGeneralQuery(String luceneQuery,
    String hqlQuery, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find codes " + luceneQuery + ", " + hqlQuery + ", "
            + pfs);

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/code"
            + "/luceneQuery/"
            + (luceneQuery == null || luceneQuery.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(
                    luceneQuery, "UTF8").replaceAll("\\+", "%20"))
            + "/hqlQuery/"
            + (hqlQuery == null || hqlQuery.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(hqlQuery,
                    "UTF8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SearchResultListJpa list =
        (SearchResultListJpa) ConfigUtility.getGraphForString(resultString,
            SearchResultListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#autocompleteCodes
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - autocomplete codes " + terminology + ", " + version
            + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getLexicalClass
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get lexical class " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/lui/"
            + terminology + "/" + version + "/" + terminologyId);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    LexicalClassJpa lexicalClass =
        (LexicalClassJpa) ConfigUtility.getGraphForString(resultString,
            LexicalClassJpa.class);
    return lexicalClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getStringClass
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get string class " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/sui/"
            + terminology + "/" + version + "/" + terminologyId);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    StringClassJpa stringClass =
        (StringClassJpa) ConfigUtility.getGraphForString(resultString,
            StringClassJpa.class);
    return stringClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findAncestorConcepts
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find ancestor concepts " + terminologyId + ", "
            + terminology + ", " + version + ", " + parentsOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId + "/ancestors/"
            + parentsOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list =
        (ConceptListJpa) ConfigUtility.getGraphForString(resultString,
            ConceptListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findDescendantConcepts
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descendant concepts " + terminologyId + ", "
            + terminology + ", " + version + ", " + childrenOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId
            + "/descendants/" + childrenOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ConceptListJpa list =
        (ConceptListJpa) ConfigUtility.getGraphForString(resultString,
            ConceptListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findAncestorDescriptors(java.lang.String, java.lang.String,
   * java.lang.String, boolean, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find ancestor descriptors " + terminologyId + ", "
            + terminology + ", " + version + ", " + parentsOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/" + terminologyId + "/ancestors/"
            + parentsOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    DescriptorListJpa list =
        (DescriptorListJpa) ConfigUtility.getGraphForString(resultString,
            DescriptorListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescendantDescriptors(java.lang.String, java.lang.String,
   * java.lang.String, boolean, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descendant descriptors " + terminologyId + ", "
            + terminology + ", " + version + ", " + childrenOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/" + terminologyId
            + "/descendants/" + childrenOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    DescriptorListJpa list =
        (DescriptorListJpa) ConfigUtility.getGraphForString(resultString,
            DescriptorListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findAncestorCodes
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find ancestor codes " + terminologyId + ", "
            + terminology + ", " + version + ", " + parentsOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/" + terminologyId + "/ancestors/"
            + parentsOnly);

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    CodeListJpa list =
        (CodeListJpa) ConfigUtility.getGraphForString(resultString,
            CodeListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findDescendantCodes
   * (java.lang.String, java.lang.String, java.lang.String, boolean,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descendant codes " + terminologyId + ", "
            + terminology + ", " + version + ", " + childrenOnly + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/" + terminologyId
            + "/descendants/" + childrenOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    CodeListJpa list =
        (CodeListJpa) ConfigUtility.getGraphForString(resultString,
            CodeListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * getSubsetMembersForConcept(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public SubsetMemberList getSubsetMembersForConcept(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get subset members for concept " + terminologyId
            + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = Client.create();

    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId + "/members");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetMemberListJpa list =
        (SubsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetMemberListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * getSubsetMembersForAtom(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public SubsetMemberList getSubsetMembersForAtom(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get subset members for atom " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/aui/"
            + terminology + "/" + version + "/" + terminologyId + "/members");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetMemberListJpa list =
        (SubsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetMemberListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * loadTerminologyRf2Snapshot(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void loadTerminologyRf2Snapshot(String terminology, String version,
    String inputDir, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology rf2 snapshot " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/terminology/load/rf2/snapshot/" + terminology + "/" + version);
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
   * loadTerminologyRf2Delta(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  @Override
  public void loadTerminologyRf2Delta(String terminology, String inputDir,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology rf2 delta " + terminology);
    validateNotEmpty(inputDir, "inputDir");
    validateNotEmpty(terminology, "terminology");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/terminology/load/rf2/delta/" + terminology);
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
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#loadTerminologyClaml
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void loadTerminologyClaml(String terminology, String version,
    String inputFile, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology ClaML " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputFile, "inputFile");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/terminology/load/claml/" + terminology + "/" + version);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, inputFile);

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
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getAtomSubsets
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public SubsetList getAtomSubsets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get atom subsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/aui/subset/all/" + terminology + "/" + version);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetListJpa list =
        (SubsetListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#getConceptSubsets
   * (java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public SubsetList getConceptSubsets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept subsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/cui/subset/all/" + terminology + "/" + version);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    SubsetListJpa list =
        (SubsetListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findAtomSubsetMembers
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get atom subset members " + terminology + ", "
            + version);
    validateNotEmpty(subsetId, "subsetId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/aui/subset/"
            + subsetId
            + "/"
            + terminology
            + "/"
            + version
            + "/members/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    SubsetMemberListJpa list =
        (SubsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetMemberListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findConceptSubsetMembers(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept subset members " + terminology + ", "
            + version);
    validateNotEmpty(subsetId, "subsetId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/cui/subset/"
            + subsetId
            + "/"
            + terminology
            + "/"
            + version
            + "/members/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8").replaceAll("\\+", "%20")));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    SubsetMemberListJpa list =
        (SubsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetMemberListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDeepRelationshipsForConcept(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  public RelationshipList findDeepRelationshipsForConcept(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find deep relationships for concept " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId
            + "/relationships/deep");
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    RelationshipListJpa list =
        (RelationshipListJpa) ConfigUtility.getGraphForString(resultString,
            RelationshipListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * getRelationshipsForConcept(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  public RelationshipList findRelationshipsForConcept(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Logger.getLogger(getClass()).debug(
        "Content Client - find relationships for concept " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs + ", " + query);
    return findRelationshipsHelper("cui", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findRelationshipsForDescriptor(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public RelationshipList findRelationshipsForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find relationships for descriptor " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs + ", " + query);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findRelationshipsHelper("dui", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findRelationshipsForCode(java.lang.String, java.lang.String,
   * java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public RelationshipList findRelationshipsForCode(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find relationships for code " + terminologyId + ", "
            + terminology + ", " + version + ", " + pfs + ", " + query);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findRelationshipsHelper("code", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /**
   * Find relationships helper.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  private RelationshipList findRelationshipsHelper(String type,
    String terminologyId, String terminology, String version, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/"
            + type
            + "/"
            + terminology
            + "/"
            + version
            + "/"
            + terminologyId
            + "/relationships/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8").replaceAll("\\+", "%20")));

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    RelationshipListJpa list =
        (RelationshipListJpa) ConfigUtility.getGraphForString(resultString,
            RelationshipListJpa.class);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findTreesForConcept
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public TreeList findTreesForConcept(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get tree positions for concept " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("cui", terminologyId, terminology, version, pfs,
        authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findTreesForDescriptor
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public TreeList findTreesForDescriptor(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get tree positions for descriptor " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("dui", terminologyId, terminology, version, pfs,
        authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findTreesForCode
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public TreeList findTreesForCode(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get tree positions for code " + terminologyId + ", "
            + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("code", terminologyId, terminology, version, pfs,
        authToken);
  }

  /**
   * Find trees helper.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  private TreeList findTreesHelper(String type, String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/content/" + type
            + "/" + terminology + "/" + version + "/" + terminologyId
            + "/trees");
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeListJpa list =
        (TreeListJpa) ConfigUtility.getGraphForString(resultString,
            TreeListJpa.class);
    return list;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findConceptTreeForQuery(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  public Tree findConceptTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept tree for query " + ", " + terminology
            + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreeForQueryHelper("cui", terminology, version, query, pfs,
        authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.services.rest.ContentServiceRest#
   * findDescriptorTreeForQuery(java.lang.String, java.lang.String,
   * java.lang.String, com.wci.umls.server.jpa.helpers.PfsParameterJpa,
   * java.lang.String)
   */
  @Override
  public Tree findDescriptorTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get descriptor tree for query " + ", " + terminology
            + ", " + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreeForQueryHelper("dui", terminology, version, query, pfs,
        authToken);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.rest.ContentServiceRest#findCodeTreeForQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa, java.lang.String)
   */
  @Override
  public Tree findCodeTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get code tree for query " + ", " + terminology + ", "
            + version + ", " + query + ", " + pfs);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreeForQueryHelper("code", terminology, version, query, pfs,
        authToken);
  }

  /**
   * Find tree for query helper.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  private Tree findTreeForQueryHelper(String type, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url")
            + "/content/"
            + type
            + "/"
            + terminology
            + "/"
            + version
            + "/trees/query/"
            + (query == null || query.isEmpty()
                ? ContentServiceRest.QUERY_BLANK : URLEncoder.encode(query,
                    "UTF-8").replaceAll("\\+", "%20")));

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    TreeJpa tree =
        (TreeJpa) ConfigUtility.getGraphForString(resultString, TreeJpa.class);
    return tree;
  }
}
