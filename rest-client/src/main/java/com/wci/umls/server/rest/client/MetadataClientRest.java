
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

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TerminologyListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.jpa.services.rest.MetadataServiceRest;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;

/**
 * A client for connecting to a metadata REST service.
 */
public class MetadataClientRest extends RootClientRest implements MetadataServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ContentClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public MetadataClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public KeyValuePairLists getAllMetadata(String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Client - get all metadata " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/metadata/all/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    KeyValuePairLists result =
        ConfigUtility.getGraphForString(resultString, KeyValuePairLists.class);
    return result;
  }

  /* see superclass */
  @Override
  public TerminologyList getCurrentTerminologies(String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get all terminologyies versions");
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/terminology/current");
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    return ConfigUtility.getGraphForString(resultString, TerminologyListJpa.class);

  }

  /* see superclass */
  @Override
  public Terminology getTerminology(String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Client - get terminology " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/metadata/terminology/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    Terminology result = ConfigUtility.getGraphForString(resultString, TerminologyJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public RootTerminology getRootTerminology(String terminology, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get root terminology " + terminology);
    validateNotEmpty(terminology, "terminology");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/rootTerminology/" + terminology);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    RootTerminology result =
        ConfigUtility.getGraphForString(resultString, RootTerminologyJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public PrecedenceList getDefaultPrecedenceList(String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Client - get default precedence list " + terminology + ", " + version);
    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/metadata/precedence/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    PrecedenceList result = ConfigUtility.getGraphForString(resultString, PrecedenceListJpa.class);
    return result;
  }

  @Override
  public PrecedenceList getPrecedenceList(Long precedenceListId, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get precedence list " + precedenceListId);

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/precedence/" + precedenceListId);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    PrecedenceList result = ConfigUtility.getGraphForString(resultString, PrecedenceListJpa.class);
    return result;
  }

  @Override
  public PrecedenceList addPrecedenceList(PrecedenceListJpa precedenceList, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - add precedence list ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/precedence");
    final String precString = ConfigUtility
        .getStringForGraph(precedenceList == null ? new PrecedenceListJpa() : precedenceList);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .put(Entity.xml(precString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    PrecedenceList result = ConfigUtility.getGraphForString(resultString, PrecedenceListJpa.class);
    return result;
  }

  @Override
  public void updatePrecedenceList(PrecedenceListJpa precedenceList, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update precedence list ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/precedence");
    final String precedenceListString = ConfigUtility
        .getStringForGraph(precedenceList == null ? new PrecedenceListJpa() : precedenceList);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(precedenceListString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void updateTermType(TermTypeJpa termType, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update term type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/termType");
    final String termTypeString =
        ConfigUtility.getStringForGraph(termType == null ? new TermTypeJpa() : termType);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(termTypeString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void updateAttributeName(AttributeNameJpa attributeName, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update attribute name ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/attributeName");
    final String attributeNameString = ConfigUtility
        .getStringForGraph(attributeName == null ? new AttributeNameJpa() : attributeName);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(attributeNameString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void updateRelationshipType(RelationshipTypeJpa relationshipType, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update relationship type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/relationshipType");
    final String relationshipTypeString = ConfigUtility
        .getStringForGraph(relationshipType == null ? new RelationshipTypeJpa() : relationshipType);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(relationshipTypeString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void updateRootTerminology(RootTerminologyJpa rootTerminology, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update root terminology ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/rootTerminology");
    final String rootTerminologyString = ConfigUtility
        .getStringForGraph(rootTerminology == null ? new RootTerminologyJpa() : rootTerminology);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(rootTerminologyString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void updateTerminology(TerminologyJpa terminology, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update terminology ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/terminology");
    final String terminologyString =
        ConfigUtility.getStringForGraph(terminology == null ? new TerminologyJpa() : terminology);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(terminologyString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void updateAdditionalRelationshipType(
    AdditionalRelationshipTypeJpa additionalRelationshipType, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - update add relationship type ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/additionalRelationshipType");
    final String additionalRelationshipTypeString =
        ConfigUtility.getStringForGraph(additionalRelationshipType == null
            ? new AdditionalRelationshipTypeJpa() : additionalRelationshipType);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .post(Entity.xml(additionalRelationshipTypeString));

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
  }

  @Override
  public void removePrecedenceList(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - remove precedence list ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/precedence/" + id);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public void removeTermType(String type, String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - remove term type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/termType/" + type
        + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public TermType getTermType(String type, String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - remove term type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/termType/" + type
        + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    TermType result = ConfigUtility.getGraphForString(resultString, TermTypeJpa.class);
    return result;
  }

  @Override
  public AttributeName getAttributeName(String type, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get atn ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/attributeName/"
        + type + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    AttributeName result = ConfigUtility.getGraphForString(resultString, AttributeNameJpa.class);
    return result;
  }

  @Override
  public RelationshipType getRelationshipType(String type, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get rel type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/relationshipType/"
        + type + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    RelationshipType result =
        ConfigUtility.getGraphForString(resultString, RelationshipTypeJpa.class);
    return result;
  }

  @Override
  public AdditionalRelationshipType getAdditionalRelationshipType(String type, String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get add rel type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/metadata/additionalRelationshipType/" + type + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    AdditionalRelationshipType result =
        ConfigUtility.getGraphForString(resultString, AdditionalRelationshipTypeJpa.class);
    return result;
  }

  @Override
  public void removeAttributeName(String type, String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - remove attribute name ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/attributeName/"
        + type + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public void removeRelationshipType(String type, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - remove rel type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/relationshipType/"
        + type + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public void removeAdditionalRelationshipType(String type, String terminology, String version,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - remove add rel type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/metadata/additionalRelationshipType/" + type + "/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).delete();

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

  }

  @Override
  public TermType addTermType(TermTypeJpa termType, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - add term type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/termType");
    final String termTypeString =
        ConfigUtility.getStringForGraph(termType == null ? new TermTypeJpa() : termType);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .put(Entity.xml(termTypeString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    TermType result = ConfigUtility.getGraphForString(resultString, TermTypeJpa.class);
    return result;
  }

  @Override
  public AttributeName addAttributeName(AttributeNameJpa attributeName, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - add attributeName ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/attributeName");
    final String termTypeString = ConfigUtility
        .getStringForGraph(attributeName == null ? new AttributeNameJpa() : attributeName);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .put(Entity.xml(termTypeString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    AttributeName result = ConfigUtility.getGraphForString(resultString, AttributeNameJpa.class);
    return result;
  }

  @Override
  public RelationshipType addRelationshipType(RelationshipTypeListJpa relationshipTypeList,
    String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - add relationship type ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url") + "/metadata/relationshipType");
    final String relationshipTypeString = ConfigUtility.getStringForGraph(
        relationshipTypeList == null ? new RelationshipTypeListJpa() : relationshipTypeList);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .put(Entity.xml(relationshipTypeString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    RelationshipType result =
        ConfigUtility.getGraphForString(resultString, RelationshipTypeJpa.class);
    return result;
  }

  @Override
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipTypeListJpa additionalRelationshipTypeList, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - add additionalRelationship type ");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/metadata/additionalRelationshipType");
    final String additionalRelationshipTypeString =
        ConfigUtility.getStringForGraph(additionalRelationshipTypeList == null
            ? new AdditionalRelationshipTypeListJpa() : additionalRelationshipTypeList);
    Response response = target.request(MediaType.APPLICATION_XML).header("Authorization", authToken)
        .put(Entity.xml(additionalRelationshipTypeString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    AdditionalRelationshipType result =
        ConfigUtility.getGraphForString(resultString, AdditionalRelationshipTypeJpa.class);
    return result;
  }

  /* see superclass */
  @Override
  public SemanticTypeList getSemanticTypes(String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Client - get semantic types " + terminology + ", " + version);

    validateNotEmpty(terminology, "terminology");
    validateNotEmpty(version, "version");

    Client client = ClientBuilder.newClient();
    WebTarget target = client
        .target(config.getProperty("base.url") + "/metadata/sty/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    // converting to object
    return ConfigUtility.getGraphForString(resultString, SemanticTypeListJpa.class);
  }

  @Override
  public TermTypeList getTermTypes(String terminology, String version, String authToken)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get term types ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(
        config.getProperty("base.url") + "/metadata/termType/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    TermTypeList result = ConfigUtility.getGraphForString(resultString, TermTypeListJpa.class);
    return result;

  }

  /* see superclass */
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(String terminology,
    String version, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Client - get add rel types ");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/metadata/additionalRelationshipType/" + terminology + "/" + version);
    Response response =
        target.request(MediaType.APPLICATION_XML).header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }
    AdditionalRelationshipTypeList result =
        ConfigUtility.getGraphForString(resultString, AdditionalRelationshipTypeListJpa.class);
    return result;

  }
}
