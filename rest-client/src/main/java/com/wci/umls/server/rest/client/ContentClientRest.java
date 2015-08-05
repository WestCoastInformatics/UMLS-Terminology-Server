/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

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

  /* see superclass */
  @Override
  public void loadTerminologyRrf(String terminology, String version,
    boolean singleMode, String inputDir, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology rrf " + terminology + ", " + version
            + ", " + inputDir);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputDir, "inputDir");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/terminology/load/rrf/"
            + singleMode + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void computeTransitiveClosure(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - compute transitive closure " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/terminology/closure/compute/" + terminology + "/"
            + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.text(""));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void computeTreePositions(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - compute tree positions " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/terminology/treepos/compute/" + terminology + "/"
            + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).post(Entity.text(""));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - lucene reindex " + indexedObjects);
    validateNotEmpty(indexedObjects, "indexedObjects");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/reindex");
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.text(indexedObjects));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      if (response.getStatus() != 204)
        throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public boolean removeTerminology(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - remove terminology " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/terminology/remove/" + terminology + "/" + version);

    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      if (response.getStatus() != 204)
        throw new Exception("Unexpected status " + response.getStatus());
    }
    return true;
  }

  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept " + terminologyId + ", " + terminology
            + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId);
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
    ConceptJpa concept =
        (ConceptJpa) ConfigUtility.getGraphForString(resultString,
            ConceptJpa.class);
    return concept;
  }

  /* see superclass */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfscParameterJpa pfsc, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find concepts " + terminology + ", " + version + ", "
            + query + ", " + pfsc);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/cui/"
            + terminology
            + "/"
            + version
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfsc == null ? new PfscParameterJpa()
            : pfsc);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public SearchResultList findConceptsForGeneralQuery(String query, String jql,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find concepts " + query + ", " + jql + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/cui"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "?jql="
            + URLEncoder.encode(jql == null ? "" : jql, "UTF-8").replaceAll(
                "\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - autocomplete concepts " + terminology + ", "
            + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
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
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get descriptor " + terminologyId + ", " + terminology
            + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/" + terminologyId);
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
    DescriptorJpa descriptor =
        (DescriptorJpa) ConfigUtility.getGraphForString(resultString,
            DescriptorJpa.class);
    return descriptor;
  }

  /* see superclass */
  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String query, PfscParameterJpa pfsc, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descriptors " + terminology + ", " + version
            + ", " + query + ", " + pfsc);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/dui/"
            + terminology
            + "/"
            + version
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfsc == null ? new PfscParameterJpa()
            : pfsc);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public SearchResultList findDescriptorsForGeneralQuery(String query,
    String jql, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find descriptors " + query + ", " + jql + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/dui"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "?jql="
            + URLEncoder.encode(jql == null ? "" : jql, "UTF-8").replaceAll(
                "\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - autocomplete descriptors " + terminology + ", "
            + version + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
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
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get code " + terminologyId + ", " + terminology
            + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/" + terminologyId);
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
    CodeJpa descriptor =
        (CodeJpa) ConfigUtility.getGraphForString(resultString, CodeJpa.class);
    return descriptor;
  }

  /* see superclass */
  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String query, PfscParameterJpa pfsc, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find codes " + terminology + ", " + version + ", "
            + query + ", " + pfsc);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/code/"
            + terminology
            + "/"
            + version
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfsc == null ? new PfscParameterJpa()
            : pfsc);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public SearchResultList findCodesForGeneralQuery(String query, String jql,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find codes " + query + ", " + jql + ", " + pfs);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/code"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20")
            + "?jql="
            + URLEncoder.encode(jql == null ? "" : jql, "UTF-8").replaceAll(
                "\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - autocomplete codes " + terminology + ", " + version
            + ", " + searchTerm);
    validateNotEmpty(searchTerm, "searchTerm");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/autocomplete/" + searchTerm);
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
    StringList list =
        (StringList) ConfigUtility.getGraphForString(resultString,
            StringList.class);
    return list;
  }

  /* see superclass */
  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get lexical class " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/lui/"
            + terminology + "/" + version + "/" + terminologyId);
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
    LexicalClassJpa lexicalClass =
        (LexicalClassJpa) ConfigUtility.getGraphForString(resultString,
            LexicalClassJpa.class);
    return lexicalClass;
  }

  /* see superclass */
  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get string class " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/sui/"
            + terminology + "/" + version + "/" + terminologyId);
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
    StringClassJpa stringClass =
        (StringClassJpa) ConfigUtility.getGraphForString(resultString,
            StringClassJpa.class);
    return stringClass;
  }

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId + "/ancestors/"
            + parentsOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId
            + "/descendants/" + childrenOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/" + terminologyId + "/ancestors/"
            + parentsOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/dui/"
            + terminology + "/" + version + "/" + terminologyId
            + "/descendants/" + childrenOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/" + terminologyId + "/ancestors/"
            + parentsOnly);

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/code/"
            + terminology + "/" + version + "/" + terminologyId
            + "/descendants/" + childrenOnly);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public SubsetMemberList getSubsetMembersForConcept(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get subset members for concept " + terminologyId
            + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    Client client = ClientBuilder.newClient();

    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId + "/members");
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
    SubsetMemberListJpa list =
        (SubsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetMemberList getSubsetMembersForAtom(String terminologyId,
    String terminology, String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get subset members for atom " + terminologyId + ", "
            + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/aui/"
            + terminology + "/" + version + "/" + terminologyId + "/members");
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
    SubsetMemberListJpa list =
        (SubsetMemberListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetMemberListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public void loadTerminologyRf2Snapshot(String terminology, String version,
    String inputDir, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology rf2 snapshot " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/terminology/load/rf2/snapshot/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  @Override
  public void loadTerminologyRf2Full(String terminology, String version,
    String inputDir, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology rf2 full " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/terminology/load/rf2/full/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void loadTerminologyRf2Delta(String terminology, String inputDir,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology rf2 delta " + terminology);
    validateNotEmpty(inputDir, "inputDir");
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/terminology/load/rf2/delta/" + terminology);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.text(inputDir));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }

  }

  /* see superclass */
  @Override
  public void loadTerminologyClaml(String terminology, String version,
    String inputFile, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - load terminology ClaML " + terminology + ", "
            + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputFile, "inputFile");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/terminology/load/claml/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.text(inputFile));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }
  }

  @Override
  public void loadTerminologyOwl(String terminology, String version,
    String inputFile, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug(
            "Content Client - load terminology Owl " + terminology + ", "
                + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    validateNotEmpty(inputFile, "inputFile");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/terminology/load/owl/"
            + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .put(Entity.text(inputFile));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing
    } else {
      throw new Exception("Unexpected status " + response.getStatus());
    }
  }

  /* see superclass */
  @Override
  public SubsetList getAtomSubsets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get atom subsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/aui/subset/all/" + terminology + "/" + version);
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
    SubsetListJpa list =
        (SubsetListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetListJpa.class);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetList getConceptSubsets(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get concept subsets " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/cui/subset/all/" + terminology + "/" + version);
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
    SubsetListJpa list =
        (SubsetListJpa) ConfigUtility.getGraphForString(resultString,
            SubsetListJpa.class);
    return list;
  }

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/aui/subset/"
            + subsetId
            + "/"
            + terminology
            + "/"
            + version
            + "/members"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/cui/subset/"
            + subsetId
            + "/"
            + terminology
            + "/"
            + version
            + "/members"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/cui/"
            + terminology + "/" + version + "/" + terminologyId
            + "/relationships/deep");
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public RelationshipList findRelationshipsForConcept(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - find relationships for concept " + terminologyId
            + ", " + terminology + ", " + version);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Logger.getLogger(getClass()).debug(
        "Content Client - find relationships for concept " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs + ", " + query);
    return findRelationshipsHelper("cui", terminologyId, terminology, version,
        query, pfs, authToken);
  }

  /* see superclass */
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

  /* see superclass */
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
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/"
            + type
            + "/"
            + terminology
            + "/"
            + version
            + "/"
            + terminologyId
            + "/relationships"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public TreeList findConceptTrees(String terminologyId, String terminology,
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

  /* see superclass */
  @Override
  public TreeList findDescriptorTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Client - get tree positions for descriptor " + terminologyId
            + ", " + terminology + ", " + version + ", " + pfs);
    validateNotEmpty(terminologyId, "terminologyId");
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");
    return findTreesHelper("dui", terminologyId, terminology, version, pfs,
        authToken);
  }

  /* see superclass */
  @Override
  public TreeList findCodeTrees(String terminologyId, String terminology,
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
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + type + "/"
            + terminology + "/" + version + "/" + terminologyId + "/trees");
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
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

  /* see superclass */
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

  /* see superclass */
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
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url")
            + "/content/"
            + type
            + "/"
            + terminology
            + "/"
            + version
            + "/trees"
            + "?query="
            + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public TreeList findConceptTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/cui"
            + "/" + terminology + "/" + version + "/trees/children");

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public TreeList findDescriptorTreeChildren(String terminology,
    String version, String terminologyId, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/dui"
            + "/" + terminology + "/" + version + "/trees/children");

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public TreeList findCodeTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
    throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/code"
            + "/" + terminology + "/" + version + "/trees/children");

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public Tree findConceptTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/cui"
            + "/" + terminology + "/" + version + "/trees/roots");

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public Tree findCodeTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/code"
            + "/" + terminology + "/" + version + "/trees/roots");

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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

  /* see superclass */
  @Override
  public Tree findDescriptorTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/content/" + "/dui"
            + "/" + terminology + "/" + version + "/trees/roots");

    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Response response =
        target.request(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
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
