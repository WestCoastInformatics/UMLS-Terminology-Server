/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.GeneralMetadataEntryList;
import com.wci.umls.server.helpers.meta.LanguageList;
import com.wci.umls.server.helpers.meta.PropertyChainList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.RootTerminologyList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.GeneralMetadataEntryListJpa;
import com.wci.umls.server.jpa.helpers.meta.LanguageListJpa;
import com.wci.umls.server.jpa.helpers.meta.PropertyChainListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.RootTerminologyListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TerminologyListJpa;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.Abbreviation;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * Implementation of {@link MetadataService} that redirects to
 * terminology-specific implemlentations.
 */
public class MetadataServiceJpa extends RootServiceJpa implements
    MetadataService {

  /** The config properties. */
  protected static Properties config = null;

  /** The listeners enabled. */
  protected boolean listenersEnabled = true;

  /** The last modified flag. */
  protected boolean lastModifiedFlag = false;

  /** The listener. */
  protected static List<WorkflowListener> listeners = null;
  static {
    listeners = new ArrayList<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "workflow.listener.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        WorkflowListener handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowListener.class);
        listeners.add(handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      listeners = null;
    }
  }

  /** The helper map. */
  private static Map<String, MetadataService> helperMap = null;
  static {
    helperMap = new HashMap<>();
    Properties config;
    try {
      config = ConfigUtility.getConfigProperties();
      String key = "metadata.service.handler";
      for (String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        MetadataService handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, MetadataService.class);
        helperMap.put(handlerName, handlerService);
      }
      if (!helperMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("metadata.service.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      helperMap = null;
    }
  }

  /** The graph resolver. */
  private static Map<String, GraphResolutionHandler> graphResolverMap = null;
  static {
    graphResolverMap = new HashMap<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "graph.resolution.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        GraphResolutionHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, GraphResolutionHandler.class);
        graphResolverMap.put(handlerName, handlerService);
      }
      if (!graphResolverMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("graph.resolution.handler." + ConfigUtility.DEFAULT
            + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      graphResolverMap = null;
    }
  }

  /**
   * Instantiates an empty {@link MetadataServiceJpa}.
   *
   * @throws Exception the exception
   */
  public MetadataServiceJpa() throws Exception {
    super();

    if (helperMap == null) {
      throw new Exception("Helper map not properly initialized, serious error.");
    }
    if (listeners == null) {
      throw new Exception(
          "Listeners did not properly initialize, serious error.");
    }
    if (graphResolverMap == null) {
      throw new Exception(
          "Graph resolver did not properly initialize, serious error.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#enableListeners()
   */
  @Override
  public void enableListeners() {
    listenersEnabled = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#disableListeners()
   */
  @Override
  public void disableListeners() {
    listenersEnabled = false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAllMetadata(java.lang.String
   * , java.lang.String)
   */
  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get all metadata " + terminology + ", " + version);

    Map<String, Map<String, String>> abbrMapList = new TreeMap<>();

    Map<String, String> additionalRelTypeMap =
        getAbbreviationMap(getAdditionalRelationshipTypes(terminology, version)
            .getObjects());
    if (additionalRelTypeMap != null) {
      abbrMapList.put(MetadataKeys.Additional_Relationship_Types.toString(),
          additionalRelTypeMap);
    }

    Map<String, String> relTypeMap =
        getAbbreviationMap(getRelationshipTypes(terminology, version)
            .getObjects());
    if (relTypeMap != null) {
      abbrMapList.put(MetadataKeys.Relationship_Types.toString(), relTypeMap);
    }

    Map<String, String> attNameMap =
        getAbbreviationMap(getAttributeNames(terminology, version).getObjects());
    if (attNameMap != null) {
      abbrMapList.put(MetadataKeys.Attribute_Names.toString(), attNameMap);
    }

    // Skip general metadata entries

    Map<String, String> semanticTypeMap =
        getAbbreviationMap(getSemanticTypes(terminology, version).getObjects());
    if (semanticTypeMap != null) {
      abbrMapList.put(MetadataKeys.Semantic_Types.toString(), semanticTypeMap);
    }

    Map<String, String> termTypeMap =
        getAbbreviationMap(getTermTypes(terminology, version).getObjects());
    if (termTypeMap != null) {
      abbrMapList.put(MetadataKeys.Term_Types.toString(), termTypeMap);
    }

    Map<String, String> hierRelTypeMap =
        getAbbreviationMap(getHierarchicalRelationshipTypes(terminology,
            version).getObjects());
    if (hierRelTypeMap != null) {
      abbrMapList.put(MetadataKeys.Hierarchical_Relationship_Types.toString(),
          hierRelTypeMap);
    }

    Map<String, String> latMap =
        getAbbreviationMap(getLanguages(terminology, version).getObjects());
    if (latMap != null) {
      abbrMapList.put(MetadataKeys.Languages.toString(), latMap);
    }

    Map<String, String> gmeMap =
        getAbbreviationMap(getGeneralMetadataEntries(terminology, version)
            .getObjects());
    if (gmeMap != null) {
      abbrMapList.put(MetadataKeys.General_Metadata_Entries.toString(),
          gmeMap);
    }
    return abbrMapList;
  }

  /**
   * Returns the abbreviation map.
   *
   * @param list the list
   * @return the abbreviation map
   */
  @SuppressWarnings("static-method")
  private Map<String, String> getAbbreviationMap(
    List<? extends Abbreviation> list) {
    Map<String, String> result = new HashMap<>();
    if (list == null) {
      return null;
    }
    for (Abbreviation abbr : list) {
      result.put(abbr.getAbbreviation(), abbr.getExpandedForm());
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getGeneralMetadataEntries(
   * java.lang.String, java.lang.String)
   */
  @Override
  public GeneralMetadataEntryList getGeneralMetadataEntries(String terminology,
    String version) {
    Logger.getLogger(getClass()).info(
        "Metadata service - get general metadata entries " + terminology + ", "
            + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getGeneralMetadataEntries(terminology,
          version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getGeneralMetadataEntries(
          terminology, version);
    } else {
      // return an empty map
      return new GeneralMetadataEntryListJpa();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTermTypePrecedenceList(
   * java.lang.String, java.lang.String)
   */
  @Override
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get default precedence list " + terminology + ", "
            + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getDefaultPrecedenceList(terminology,
          version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getDefaultPrecedenceList(
          terminology, version);
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#getTerminologies()
   */
  @SuppressWarnings("unchecked")
  @Override
  public RootTerminologyList getTerminologies() throws Exception {
    Logger.getLogger(getClass()).info("Metadata service - get terminologies");
    javax.persistence.Query query =
        manager.createQuery("SELECT distinct t from RootTerminologyJpa t");
    RootTerminologyList terminologies = new RootTerminologyListJpa();
    terminologies.setObjects(query.getResultList());
    terminologies.setTotalCount(terminologies.getObjects().size());
    return terminologies;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTerminology(java.lang.String
   * , java.lang.String)
   */
  @Override
  public Terminology getTerminology(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get terminology " + terminology + ", " + version);
    try {
      javax.persistence.Query query =
          manager.createQuery("SELECT t FROM TerminologyJpa t "
              + "WHERE terminology = :terminology AND version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      return (Terminology) query.getSingleResult();
    } catch (Exception e) {
      // not found, or too many found
      return null;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getVersions(java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public TerminologyList getVersions(String terminology) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get versions " + terminology);
    javax.persistence.Query query =
        manager
            .createQuery("SELECT distinct t from TerminologyJpa t where terminology = :terminology");
    query.setParameter("terminology", terminology);
    TerminologyList versions = new TerminologyListJpa();
    versions.setObjects(query.getResultList());
    versions.setTotalCount(versions.getObjects().size());
    return versions;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getLatestVersion(java.lang
   * .String)
   */
  @Override
  public String getLatestVersion(String terminology) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get latest version " + terminology);
    javax.persistence.Query query =
        manager
            .createQuery("SELECT max(t.version) from TerminologyJpa t where terminology = :terminology");

    query.setParameter("terminology", terminology);
    Object o = query.getSingleResult();
    if (o == null) {
      return null;
    }
    return o.toString();

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTerminologyLatestVersions()
   */
  @Override
  public TerminologyList getTerminologyLatestVersions() throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get latest terminology versions");
    javax.persistence.TypedQuery<Object[]> query =
        manager
            .createQuery(
                "SELECT t.terminology, max(t.version) from TerminologyJpa t group by t.terminology",
                Object[].class);

    List<Object[]> resultList = query.getResultList();
    List<Terminology> results = new ArrayList<>();
    for (Object[] result : resultList) {
      results.add(getTerminology((String) result[0], (String) result[1]));

    }
    TerminologyList list = new TerminologyListJpa();
    list.setObjects(results);
    list.setTotalCount(results.size());

    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getRelationshipTypes(java.
   * lang.String, java.lang.String)
   */
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get relationship types " + terminology + ", "
            + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getRelationshipTypes(terminology,
          version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getRelationshipTypes(
          terminology, version);
    } else {
      // return an empty map
      return new RelationshipTypeListJpa();
    }
  }

  @Override
  public LanguageList getLanguages(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get languages " + terminology + ", " + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getLanguages(terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getLanguages(terminology,
          version);
    } else {
      // return an empty map
      return new LanguageListJpa();
    }
  }

  @Override
  public PropertyChainList getPropertyChains(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get property chains " + terminology + ", "
            + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getPropertyChains(terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getPropertyChains(
          terminology, version);
    } else {
      // return an empty map
      return new PropertyChainListJpa();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAdditionalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get additional relationship types " + terminology
            + ", " + version);

    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getAdditionalRelationshipTypes(
          terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT)
          .getAdditionalRelationshipTypes(terminology, version);
    } else {
      // return an empty map
      return new AdditionalRelationshipTypeListJpa();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAttributeNames(java.lang
   * .String, java.lang.String)
   */
  @Override
  public AttributeNameList getAttributeNames(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get attribute names " + terminology + ", "
            + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getAttributeNames(terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getAttributeNames(
          terminology, version);
    } else {
      // return an empty map
      return new AttributeNameListJpa();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getSemanticTypes(java.lang
   * .String, java.lang.String)
   */
  @Override
  public SemanticTypeList getSemanticTypes(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass())
        .info(
            "Metadata service - get semantic types " + terminology + ", "
                + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getSemanticTypes(terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getSemanticTypes(terminology,
          version);
    } else {
      // return an empty map
      return new SemanticTypeListJpa();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTermTypes(java.lang.String,
   * java.lang.String)
   */
  @Override
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get term types " + terminology + ", " + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getTermTypes(terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).getTermTypes(terminology,
          version);
    } else {
      // return an empty map
      return new TermTypeListJpa();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getHierarchicalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @Override
  public RelationshipTypeList getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get hierarchical relationship types " + terminology
            + ", " + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getHierarchicalRelationshipTypes(
          terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT)
          .getHierarchicalRelationshipTypes(terminology, version);
    } else {
      // return an empty map
      return new RelationshipTypeListJpa();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#isHierarchcialRelationship
   * (com.wci.umls.server.model.content.Relationship)
   */
  @Override
  public boolean isHierarchcialRelationship(Relationship<?, ?> relationship) {
    if (helperMap.containsKey(relationship.getTerminology())) {
      Logger.getLogger(getClass()).info(
          "Metadata service - is hierarchical relationship " + relationship);
      return helperMap.get(relationship.getTerminology())
          .isHierarchcialRelationship(relationship);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).isHierarchcialRelationship(
          relationship);
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#isStatedRelationship(com.wci
   * .umls.server.model.content.Relationship)
   */
  @Override
  public boolean isStatedRelationship(Relationship<?, ?> relationship) {
    if (helperMap.containsKey(relationship.getTerminology())) {
      Logger.getLogger(getClass()).info(
          "Metadata service - is stated relationship " + relationship);
      return helperMap.get(relationship.getTerminology()).isStatedRelationship(
          relationship);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).isStatedRelationship(
          relationship);
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#isInferredRelationship(com
   * .wci.umls.server.model.content.Relationship)
   */
  @Override
  public boolean isInferredRelationship(Relationship<?, ?> relationship) {
    if (helperMap.containsKey(relationship.getTerminology())) {
      Logger.getLogger(getClass()).info(
          "Metadata service - is inferred relationship " + relationship);
      return helperMap.get(relationship.getTerminology())
          .isInferredRelationship(relationship);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT).isInferredRelationship(
          relationship);
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getNonGroupingRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @Override
  public RelationshipTypeList getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "Metadata service - get non grouping relationship types " + terminology
            + ", " + version);
    if (helperMap.containsKey(terminology)) {
      return helperMap.get(terminology).getNonGroupingRelationshipTypes(
          terminology, version);
    } else if (helperMap.containsKey(ConfigUtility.DEFAULT)) {
      return helperMap.get(ConfigUtility.DEFAULT)
          .getNonGroupingRelationshipTypes(terminology, version);
    } else {
      // return an empty map
      return new RelationshipTypeListJpa();
    }
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addSemanticType(com.wci.umls
   * .server.model.meta.SemanticType)
   */
  @Override
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add semanticType " + semanticType.getValue());

    // Add component
    SemanticType newSemanticType = addMetadata(semanticType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newSemanticType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateSemanticType(com.wci
   * .umls.server.model.meta.SemanticType)
   */
  @Override
  public void updateSemanticType(SemanticType semanticType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update semantic type " + semanticType.getValue());
    updateMetadata(semanticType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeSemanticType(java.lang
   * .Long)
   */
  @Override
  public void removeSemanticType(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove semantic type " + id);
    // Remove the component
    removeMetadata(id, SemanticTypeJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }
  

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.MetadataService#removePropertyChain(java.lang.Long)
   */
  @Override
  public void removePropertyChain(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove property chain " + id);
    // Remove the component
    removeMetadata(id, PropertyChainJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addAttributeName(com.wci.umls
   * .server.model.meta.AttributeName)
   */
  @Override
  public AttributeName addAttributeName(AttributeName attributeName)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add attributeName "
            + attributeName.getAbbreviation());

    // Add component
    AttributeName newAttributeName = addMetadata(attributeName);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newAttributeName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateAttributeName(com.wci
   * .umls.server.model.meta.AttributeName)
   */
  @Override
  public void updateAttributeName(AttributeName attributeName) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update attributeName "
            + attributeName.getAbbreviation());
    updateMetadata(attributeName);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeAttributeName(java.lang
   * .Long)
   */
  @Override
  public void removeAttributeName(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove attributeName " + id);
    // Remove the component
    removeMetadata(id, AttributeNameJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addLanguage(com.wci.umls.server
   * .model.meta.Language)
   */
  @Override
  public Language addLanguage(Language language) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add language " + language.getAbbreviation());

    // Add component
    Language newLanguage = addMetadata(language);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newLanguage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateLanguage(com.wci.umls
   * .server.model.meta.Language)
   */
  @Override
  public void updateLanguage(Language language) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update language " + language.getAbbreviation());
    updateMetadata(language);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeLanguage(java.lang.Long)
   */
  @Override
  public void removeLanguage(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove language" + id);
    // Remove the component
    removeMetadata(id, LanguageJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addAdditionalRelationshipType
   * (com.wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add additional relationship type "
            + additionalRelationshipType.getAbbreviation());

    // Add component
    AdditionalRelationshipType newAdditionalRelationshipType =
        addMetadata(additionalRelationshipType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newAdditionalRelationshipType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateAdditionalRelationshipType
   * (com.wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void updateAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update additional relationship type "
            + additionalRelationshipType.getAbbreviation());
    updateMetadata(additionalRelationshipType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeAdditionalRelationshipType
   * (java.lang.Long)
   */
  @Override
  public void removeAdditionalRelationshipType(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove additional relationship type" + id);
    // Remove the component
    removeMetadata(id, AdditionalRelationshipTypeJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public PropertyChain addPropertyChain(PropertyChain propertyChain)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add property chain "
            + propertyChain.getAbbreviation());

    // Add component
    PropertyChain newPropertyChain = addMetadata(propertyChain);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newPropertyChain;
  }

  @Override
  public void updatePropertyChain(PropertyChain propertyChain) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update property chain "
            + propertyChain.getAbbreviation());
    updateMetadata(propertyChain);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addRelationshipType(com.wci
   * .umls.server.model.meta.RelationshipType)
   */
  @Override
  public RelationshipType addRelationshipType(RelationshipType relationshipType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add relationship type "
            + relationshipType.getAbbreviation());

    // Add component
    RelationshipType newRelationshipType = addMetadata(relationshipType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newRelationshipType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateRelationshipType(com
   * .wci.umls.server.model.meta.RelationshipType)
   */
  @Override
  public void updateRelationshipType(RelationshipType relationshipType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update relationship type "
            + relationshipType.getAbbreviation());
    updateMetadata(relationshipType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeRelationshipType(java
   * .lang.Long)
   */
  @Override
  public void removeRelationshipType(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove relationship type" + id);
    // Remove the component
    removeMetadata(id, RelationshipTypeJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addTermType(com.wci.umls.server
   * .model.meta.TermType)
   */
  @Override
  public TermType addTermType(TermType termType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add term type " + termType.getAbbreviation());

    // Add component
    TermType newTermType = addMetadata(termType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newTermType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateTermType(com.wci.umls
   * .server.model.meta.TermType)
   */
  @Override
  public void updateTermType(TermType termType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update term type " + termType.getAbbreviation());
    updateMetadata(termType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeTermType(java.lang.Long)
   */
  @Override
  public void removeTermType(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove term type " + id);
    // Remove the component
    removeMetadata(id, TermTypeJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addGeneralMetadataEntry(com
   * .wci.umls.server.model.meta.GeneralMetadataEntry)
   */
  @Override
  public GeneralMetadataEntry addGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add general metadata entry "
            + entry.getAbbreviation());

    // Add component
    GeneralMetadataEntry newEntry = addMetadata(entry);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newEntry;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateGeneralMetadataEntry
   * (com.wci.umls.server.model.meta.GeneralMetadataEntry)
   */
  @Override
  public void updateGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update general metadata entry "
            + entry.getAbbreviation());
    updateMetadata(entry);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeGeneralMetadataEntry
   * (java.lang.Long)
   */
  @Override
  public void removeGeneralMetadataEntry(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove general metadata entry " + id);
    // Remove the component
    removeMetadata(id, GeneralMetadataEntryJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addTerminology(com.wci.umls
   * .server .model.meta.Terminology)
   */
  @Override
  public Terminology addTerminology(Terminology terminology) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add terminology " + terminology.getTerminology()
            + " " + terminology.getVersion());

    // Add component
    Terminology newTerminology = addMetadata(terminology);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newTerminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateTerminology(com.wci.umls
   * .server.model.meta.Terminology)
   */
  @Override
  public void updateTerminology(Terminology terminology) throws Exception {
    Logger.getLogger(getClass())
        .debug(
            "Metadata Service - update terminology "
                + terminology.getTerminology());
    updateMetadata(terminology);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeTerminology(java.lang
   * .Long)
   */
  @Override
  public void removeTerminology(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove terminology" + id);
    // Remove the component
    removeMetadata(id, TerminologyJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#addRootTerminology(com.wci
   * .umls.server .model.meta.RootTerminology)
   */
  @Override
  public RootTerminology addRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add rootTerminology "
            + rootTerminology.getTerminology());

    // Add component
    RootTerminology newRootTerminology = addMetadata(rootTerminology);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newRootTerminology;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#updateRootTerminology(com.
   * wci.umls .server.model.meta.RootTerminology)
   */
  @Override
  public void updateRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update rootTerminology "
            + rootTerminology.getTerminology());
    updateMetadata(rootTerminology);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#removeRootTerminology(java
   * .lang.Long)
   */
  @Override
  public void removeRootTerminology(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove rootTerminology" + id);
    // Remove the component
    removeMetadata(id, RootTerminologyJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public PrecedenceList addPrecedenceList(PrecedenceList precedenceList)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add precedence list" + precedenceList.getName());

    PrecedenceList newPrecedenceList = addMetadata(precedenceList);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newPrecedenceList;
  }

  @Override
  public void updatePrecedenceList(PrecedenceList precedenceList)
    throws Exception {
    Logger.getLogger(getClass())
        .debug(
            "Metadata Service - update precedence list "
                + precedenceList.getName());

    updateMetadata(precedenceList);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public void removePrecedenceList(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove precedence list " + id);

    removeMetadata(id, PrecedenceListJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /**
   * Adds the abbreviation.
   *
   * @param <T> the
   * @param abbreviation the component
   * @return the t
   * @throws Exception the exception
   */
  private <T extends HasLastModified> T addMetadata(T abbreviation)
    throws Exception {
    try {
      // Set last modified date
      if (lastModifiedFlag) {
        abbreviation.setLastModified(new Date());
      }

      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(abbreviation);
        tx.commit();
      } else {
        manager.persist(abbreviation);
      }
      return abbreviation;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Update abbreviation.
   *
   * @param <T> the generic type
   * @param abbreviation the abbreviation
   * @throws Exception the exception
   */
  private <T extends HasLastModified> void updateMetadata(T abbreviation)
    throws Exception {
    try {
      // Set modification date
      if (lastModifiedFlag) {
        abbreviation.setLastModified(new Date());
      }

      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(abbreviation);
        tx.commit();
      } else {
        manager.merge(abbreviation);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /**
   * Removes the abbreviation.
   *
   * @param <T> the generic type
   * @param id the id
   * @param clazz the clazz
   * @return the abbreviation
   * @throws Exception the exception
   */
  private <T extends HasLastModified> T removeMetadata(Long id, Class<T> clazz)
    throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      T abbreviation = manager.find(clazz, id);

      // Set modification date
      if (lastModifiedFlag) {
        abbreviation.setLastModified(new Date());
      }

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(abbreviation)) {
          manager.remove(abbreviation);
        } else {
          manager.remove(manager.merge(abbreviation));
        }
        tx.commit();
      } else {
        if (manager.contains(abbreviation)) {
          manager.remove(abbreviation);
        } else {
          manager.remove(manager.merge(abbreviation));
        }
      }
      return abbreviation;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#isLastModifiedFlag()
   */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#setLastModifiedFlag(boolean)
   */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

  @Override
  public void refreshCaches() throws Exception {
    for (MetadataService service : helperMap.values()) {
      service.refreshCaches();
    }
  }

  @Override
  public GraphResolutionHandler getGraphResolutionHandler(String terminology)
    throws Exception {
    if (graphResolverMap.containsKey(terminology)) {
      return graphResolverMap.get(terminology);
    }
    return graphResolverMap.get(ConfigUtility.DEFAULT);
  }

}
