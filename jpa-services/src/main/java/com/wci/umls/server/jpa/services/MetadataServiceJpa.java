/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.GeneralMetadataEntryList;
import com.wci.umls.server.helpers.meta.LabelSetList;
import com.wci.umls.server.helpers.meta.LanguageList;
import com.wci.umls.server.helpers.meta.PropertyChainList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.RootTerminologyList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.GeneralMetadataEntryListJpa;
import com.wci.umls.server.jpa.helpers.meta.LabelSetListJpa;
import com.wci.umls.server.jpa.helpers.meta.LanguageListJpa;
import com.wci.umls.server.jpa.helpers.meta.PropertyChainListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.RootTerminologyListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TerminologyListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.jpa.meta.LabelSetJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.PropertyChainJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.meta.Abbreviation;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.LabelSet;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.PropertyChain;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Implementation of {@link MetadataService} that redirects to
 * terminology-specific implemlentations.
 */
public class MetadataServiceJpa extends ProjectServiceJpa
    implements MetadataService {

  /** The graph resolver. */
  private static Map<String, GraphResolutionHandler> graphResolverMap = null;

  static {
    init();
  }

  /**
   * Static initialization (also used by refresh caches).
   */
  private static void init() {
    graphResolverMap = new HashMap<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "graph.resolution.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
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
    validateInit();
  }

  /* see superclass */
  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata service - get all metadata " + terminology + ", " + version);

    final Map<String, Map<String, String>> abbrMapList = new TreeMap<>();

    final Map<String, String> additionalRelTypeMap = getAbbreviationMap(
        getAdditionalRelationshipTypes(terminology, version).getObjects());
    if (additionalRelTypeMap != null) {
      abbrMapList.put(MetadataKeys.Additional_Relationship_Types.toString(),
          additionalRelTypeMap);
    }

    final Map<String, String> relTypeMap = getAbbreviationMap(
        getRelationshipTypes(terminology, version).getObjects());
    if (relTypeMap != null) {
      abbrMapList.put(MetadataKeys.Relationship_Types.toString(), relTypeMap);
    }

    final Map<String, String> attNameMap = getAbbreviationMap(
        getAttributeNames(terminology, version).getObjects());
    if (attNameMap != null) {
      abbrMapList.put(MetadataKeys.Attribute_Names.toString(), attNameMap);
    }

    final Map<String, String> msMap =
        getAbbreviationMap(getLabelSets(terminology, version).getObjects());
    if (msMap != null) {
      abbrMapList.put(MetadataKeys.Label_Sets.toString(), msMap);
    }

    // Skip general metadata entries

    final Map<String, String> semanticTypeMap =
        getAbbreviationMap(getSemanticTypes(terminology, version).getObjects());
    if (semanticTypeMap != null) {
      abbrMapList.put(MetadataKeys.Semantic_Types.toString(), semanticTypeMap);
    }

    final Map<String, String> termTypeMap =
        getAbbreviationMap(getTermTypes(terminology, version).getObjects());
    if (termTypeMap != null) {
      abbrMapList.put(MetadataKeys.Term_Types.toString(), termTypeMap);
    }

    final Map<String, String> latMap =
        getAbbreviationMap(getLanguages(terminology, version).getObjects());
    if (latMap != null) {
      abbrMapList.put(MetadataKeys.Languages.toString(), latMap);
    }

    final Map<String, String> gmeMap = getAbbreviationMap(
        getGeneralMetadataEntries(terminology, version).getObjects());
    if (gmeMap != null && !gmeMap.isEmpty()) {
      abbrMapList.put(MetadataKeys.General_Metadata_Entries.toString(), gmeMap);
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
    final Map<String, String> result = new HashMap<>();
    if (list == null) {
      return null;
    }
    for (final Abbreviation abbr : list) {
      result.put(abbr.getAbbreviation(), abbr.getExpandedForm());
    }
    return result;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public GeneralMetadataEntryList getGeneralMetadataEntries(String terminology,
    String version) {
    final Query query =
        manager.createQuery("SELECT g from GeneralMetadataEntryJpa g"
            + " where terminology = :terminology" + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final GeneralMetadataEntryList entries = new GeneralMetadataEntryListJpa();
    entries.setObjects(query.getResultList());
    entries.setTotalCount(entries.getObjects().size());
    return entries;
  }

  /* see superclass */
  @Override
  public PrecedenceList getPrecedenceList(String terminology, String version)
    throws Exception {

    final Query query = manager.createQuery("SELECT p.id from PrecedenceListJpa p"
        + " where terminology = :terminology " + " and version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    try {
      final Long precedenceListId = (Long) query.getSingleResult();
      final PrecedenceList precedenceList = getPrecedenceList(precedenceListId);
      //Handle lazy init
      precedenceList.getTermTypeRankMap().size();
      precedenceList.getTerminologyRankMap().size();
      precedenceList.getPrecedence().getName();
      return precedenceList;
      //return (PrecedenceList) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RootTerminologyList getRootTerminologies() throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get root terminologies");
    final Query query =
        manager.createQuery("SELECT distinct t from RootTerminologyJpa t");
    final RootTerminologyList terminologies = new RootTerminologyListJpa();
    terminologies.setObjects(query.getResultList());
    terminologies.setTotalCount(terminologies.getObjects().size());
    return terminologies;
  }

  /* see superclass */
  @Override
  public RootTerminology getRootTerminology(String terminology)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get root terminology " + terminology);
    try {
      final Query query =
          manager.createQuery("SELECT t FROM RootTerminologyJpa t "
              + "WHERE terminology = :terminology");
      query.setParameter("terminology", terminology);
      return (RootTerminology) query.getSingleResult();
    } catch (Exception e) {
      // not found, or too many found
      return null;
    }

  }

  /* see superclass */
  @Override
  public Terminology getTerminology(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata service - get terminology " + terminology + ", " + version);
    try {
      final Query query = manager.createQuery("SELECT t FROM TerminologyJpa t "
          + "WHERE terminology = :terminology AND version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      return (Terminology) query.getSingleResult();
    } catch (Exception e) {
      // not found, or too many found
      return null;
    }

  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TerminologyList getVersions(String terminology) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get versions " + terminology);
    final Query query = manager.createQuery(
        "SELECT distinct t from TerminologyJpa t where terminology = :terminology");
    query.setParameter("terminology", terminology);
    final TerminologyList versions = new TerminologyListJpa();
    versions.setObjects(query.getResultList());
    versions.setTotalCount(versions.getObjects().size());
    return versions;

  }

  /* see superclass */
  @Override
  public String getLatestVersion(String terminology) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get latest version " + terminology);
    final Query query = manager.createQuery(
        "SELECT max(t.version) from TerminologyJpa t where terminology = :terminology");

    query.setParameter("terminology", terminology);
    final Object o = query.getSingleResult();
    if (o == null) {
      return null;
    }
    return o.toString();

  }

  /* see superclass */
  @Override
  public String getPreviousVersion(String terminology) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get latest version " + terminology);
    Query query = manager.createQuery(
        "SELECT max(t.version) from TerminologyJpa t where terminology = :terminology");

    query.setParameter("terminology", terminology);
    Object o = query.getSingleResult();
    if (o == null) {
      return null;
    }
    final String latestVersion = o.toString();

    query = manager.createQuery(
        "SELECT max(t.version) from TerminologyJpa t where terminology = :terminology and not version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", latestVersion);
    o = query.getSingleResult();
    if (o == null) {
      return null;
    }
    return o.toString();

  }

  /* see superclass */
  @Override
  public TerminologyList getTerminologyLatestVersions() throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get latest terminology versions");
    final TypedQuery<Object[]> query = manager.createQuery(
        "SELECT t.terminology, max(t.version) from TerminologyJpa t group by t.terminology",
        Object[].class);

    final List<Object[]> resultList = query.getResultList();
    final List<Terminology> results = new ArrayList<>();
    for (final Object[] result : resultList) {
      results.add(getTerminology((String) result[0], (String) result[1]));

    }
    final TerminologyList list = new TerminologyListJpa();
    list.setObjects(results);
    list.setTotalCount(results.size());

    return list;
  }

  /* see superclass */
  @Override
  public Terminology getTerminologyLatestVersion(String terminology)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata service - get latest terminology version - " + terminology);
    final Query query =
        manager.createQuery("SELECT max(t.version) from TerminologyJpa t "
            + "WHERE terminology = :terminology");
    query.setParameter("terminology", terminology);
    final String version = query.getSingleResult().toString();
    return getTerminology(terminology, version);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TerminologyList getTerminologies() throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get terminologies ");
    final Query query = manager.createQuery("SELECT t FROM TerminologyJpa t ");

    final List<Terminology> results = query.getResultList();
    final TerminologyList list = new TerminologyListJpa();
    list.setObjects(results);
    list.setTotalCount(results.size());

    return list;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TerminologyList getCurrentTerminologies() throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get terminologies ");
    final Query query =
        manager.createQuery("SELECT t FROM TerminologyJpa t WHERE current = 1");

    final List<Terminology> results = query.getResultList();
    final TerminologyList list = new TerminologyListJpa();
    list.setObjects(results);
    list.setTotalCount(results.size());

    return list;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Terminology getCurrentTerminology(String terminologyName)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get current terminology ");
    final Query query = manager.createQuery(
        "SELECT t FROM TerminologyJpa t WHERE terminology = :terminology AND current = 1");
    query.setParameter("terminology", terminologyName);

    final List<Terminology> results = query.getResultList();
    final Terminology terminology = results.get(0);

    return terminology;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get relationship types " + terminology + ", "
            + version);
    final Query query = manager.createQuery(
        "SELECT r from RelationshipTypeJpa r where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public LanguageList getLanguages(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata service - get languages " + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT r from LanguageJpa r where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final LanguageList types = new LanguageListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public PropertyChainList getPropertyChains(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get property chains "
        + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT r from PropertyChainJpa r where terminology = :terminology"
            + " and version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final PropertyChainList types = new PropertyChainListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get additional relationship types "
            + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT r from AdditionalRelationshipTypeJpa r where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final AdditionalRelationshipTypeList types =
        new AdditionalRelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());

    return types;

  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeNameList getAttributeNames(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get attribute names "
        + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT a from AttributeNameJpa a where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final AttributeNameList names = new AttributeNameListJpa();
    names.setObjects(query.getResultList());
    names.setTotalCount(names.getObjects().size());
    return names;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public LabelSetList getLabelSets(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata service - get label sets " + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT a from LabelSetJpa a where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final LabelSetList labelSets = new LabelSetListJpa();
    labelSets.setObjects(query.getResultList());
    labelSets.setTotalCount(labelSets.getObjects().size());
    return labelSets;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public SemanticTypeList getSemanticTypes(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get semantic types "
        + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT s from SemanticTypeJpa s where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final SemanticTypeList types = new SemanticTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata service - get term types " + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT t from TermTypeJpa t where terminology = :terminology"
            + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final TermTypeList types = new TermTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipTypeList getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get non grouping relationship types "
            + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT r from RelationshipTypeJpa r " + " where groupingType = 0"
            + " and terminology = :terminology" + " and version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    final RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @Override
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - add semanticType "
        + semanticType.getExpandedForm());

    // Add component
    return addHasLastModified(semanticType);
  }

  /* see superclass */
  @Override
  public void updateSemanticType(SemanticType semanticType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update semantic type "
            + semanticType.getExpandedForm());
    updateHasLastModified(semanticType);

  }

  /* see superclass */
  @Override
  public void removeSemanticType(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove semantic type " + id);
    // Remove the component
    removeHasLastModified(id, SemanticTypeJpa.class);
  }

  /* see superclass */
  @Override
  public void removePropertyChain(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove property chain " + id);
    // Remove the component
    removeHasLastModified(id, PropertyChainJpa.class);

  }

  /* see superclass */
  @Override
  public AttributeName addAttributeName(AttributeName attributeName)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - add attributeName "
        + attributeName.getAbbreviation());

    // Add component
    return addHasLastModified(attributeName);
  }

  /* see superclass */
  @Override
  public void updateAttributeName(AttributeName attributeName)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update attributeName "
            + attributeName.getAbbreviation());
    updateHasLastModified(attributeName);

  }

  /* see superclass */
  @Override
  public void removeAttributeName(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove attributeName " + id);
    // Remove the component
    removeHasLastModified(id, AttributeNameJpa.class);

  }

  /* see superclass */
  @Override
  public LabelSet addLabelSet(LabelSet labelSet) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - add labelSet " + labelSet.getAbbreviation());

    // Add component
    return addHasLastModified(labelSet);

  }

  /* see superclass */
  @Override
  public void updateLabelSet(LabelSet labelSet) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update labelSet " + labelSet.getAbbreviation());
    updateHasLastModified(labelSet);

  }

  /* see superclass */
  @Override
  public void removeLabelSet(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove labelSet " + id);
    // Remove the component
    removeHasLastModified(id, LabelSetJpa.class);

  }

  /* see superclass */
  @Override
  public Language addLanguage(Language language) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - add language " + language.getAbbreviation());

    // Add component
    return addHasLastModified(language);

  }

  /* see superclass */
  @Override
  public void updateLanguage(Language language) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update language " + language.getAbbreviation());
    updateHasLastModified(language);

  }

  /* see superclass */
  @Override
  public void removeLanguage(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove language" + id);
    // Remove the component
    removeHasLastModified(id, LanguageJpa.class);

  }

  /* see superclass */
  @Override
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - add additional relationship type "
            + additionalRelationshipType.getAbbreviation());

    // Add component
    return addHasLastModified(additionalRelationshipType);

  }

  /* see superclass */
  @Override
  public void updateAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update additional relationship type "
            + additionalRelationshipType.getAbbreviation());
    updateHasLastModified(additionalRelationshipType);

  }

  /* see superclass */
  @Override
  public void removeAdditionalRelationshipType(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove additional relationship type" + id);
    // Remove the component
    removeHasLastModified(id, AdditionalRelationshipTypeJpa.class);

  }

  /* see superclass */
  @Override
  public PropertyChain addPropertyChain(PropertyChain propertyChain)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - add property chain "
        + propertyChain.getAbbreviation());

    // Add component
    return addHasLastModified(propertyChain);

  }

  /* see superclass */
  @Override
  public void updatePropertyChain(PropertyChain propertyChain)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update property chain "
            + propertyChain.getAbbreviation());
    updateHasLastModified(propertyChain);

  }

  /* see superclass */
  @Override
  public RelationshipType addRelationshipType(RelationshipType relationshipType)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - add relationship type "
            + relationshipType.getAbbreviation());

    // Add component
    return addHasLastModified(relationshipType);
  }

  /* see superclass */
  @Override
  public void updateRelationshipType(RelationshipType relationshipType)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update relationship type "
            + relationshipType.getAbbreviation());
    updateHasLastModified(relationshipType);

  }

  /* see superclass */
  @Override
  public void removeRelationshipType(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove relationship type" + id);
    // Remove the component
    removeHasLastModified(id, RelationshipTypeJpa.class);

  }

  /* see superclass */
  @Override
  public TermType addTermType(TermType termType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add term type " + termType.getAbbreviation());

    // Add component
    return addHasLastModified(termType);

  }

  /* see superclass */
  @Override
  public void updateTermType(TermType termType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update term type " + termType.getAbbreviation());
    updateHasLastModified(termType);

  }

  /* see superclass */
  @Override
  public void removeTermType(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove term type " + id);
    // Remove the component
    removeHasLastModified(id, TermTypeJpa.class);

  }

  /* see superclass */
  @Override
  public GeneralMetadataEntry addGeneralMetadataEntry(
    GeneralMetadataEntry entry) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - add general metadata entry "
            + entry.getAbbreviation());

    // Add component
    return addHasLastModified(entry);

  }

  /* see superclass */
  @Override
  public void updateGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update general metadata entry "
            + entry.getAbbreviation());
    updateHasLastModified(entry);

  }

  /* see superclass */
  @Override
  public void removeGeneralMetadataEntry(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove general metadata entry " + id);
    // Remove the component
    removeHasLastModified(id, GeneralMetadataEntryJpa.class);

  }

  /* see superclass */
  @Override
  public Terminology addTerminology(Terminology terminology) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - add terminology "
        + terminology.getTerminology() + " " + terminology.getVersion());

    // Add component
    return addHasLastModified(terminology);

  }

  /* see superclass */
  @Override
  public void updateTerminology(Terminology terminology) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - update terminology "
        + terminology.getTerminology());
    updateHasLastModified(terminology);

  }

  /* see superclass */
  @Override
  public void removeTerminology(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove terminology" + id);
    // Remove the component
    removeHasLastModified(id, TerminologyJpa.class);

  }

  /* see superclass */
  @Override
  public RootTerminology addRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - add rootTerminology "
        + rootTerminology.getTerminology());

    // Add component
    return addHasLastModified(rootTerminology);

  }

  /* see superclass */
  @Override
  public void updateRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update rootTerminology "
            + rootTerminology.getTerminology());
    updateHasLastModified(rootTerminology);

  }

  /* see superclass */
  @Override
  public void removeRootTerminology(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove rootTerminology" + id);
    // Remove the component
    removeHasLastModified(id, RootTerminologyJpa.class);

  }

  @Override
  public PrecedenceList getPrecedenceList(Long precedenceListId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - get precedence list" + precedenceListId);

    final PrecedenceList newPrecedenceList =
        this.getObject(precedenceListId, PrecedenceListJpa.class);

    return newPrecedenceList;
  }

  /* see superclass */
  @Override
  public PrecedenceList addPrecedenceList(PrecedenceList precedenceList)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add precedence list" + precedenceList.getName());

    return addHasLastModified(precedenceList);
  }

  /* see superclass */
  @Override
  public void updatePrecedenceList(PrecedenceList precedenceList)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - update precedence list "
            + precedenceList.getName());

    updateHasLastModified(precedenceList);
  }

  /* see superclass */
  @Override
  public void removePrecedenceList(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - remove precedence list " + id);

    removeHasLastModified(id, PrecedenceListJpa.class);

  }

  /* see superclass */
  @Override
  public GraphResolutionHandler getGraphResolutionHandler(String terminology)
    throws Exception {
    if (graphResolverMap.containsKey(terminology)) {
      return graphResolverMap.get(terminology);
    }
    return graphResolverMap.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  @Override
  public SemanticTypeList getSemanticTypeDescendants(String terminology,
    String version, String treeNumber, boolean includeSelf) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get semantic type descendants " + terminology
            + ", " + version + ", " + treeNumber + ", " + includeSelf);
    final List<SemanticType> descendants = new ArrayList<>();
    final SemanticTypeList allStys = getSemanticTypes(terminology, version);
    for (final SemanticType sty : allStys.getObjects()) {
      if ((includeSelf && sty.getTreeNumber().equals(treeNumber))
          || sty.getTreeNumber().startsWith(treeNumber))
        descendants.add(sty);
    }
    final SemanticTypeList descendantList = new SemanticTypeListJpa();
    descendantList.setObjects(descendants);
    descendantList.setTotalCount(descendants.size());
    return descendantList;
  }

  @Override
  public SemanticType getSemanticType(String type, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get semantic type "
        + type + "," + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT s from SemanticTypeJpa s where terminology = :terminology and version = :version and expandedForm = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    try {
      return (SemanticType) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public AttributeName getAttributeName(String name, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get attribute name "
        + name + "," + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT a from AttributeNameJpa a where terminology = :terminology and version = :version and abbreviation = :name");
    query.setParameter("name", name);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    try {
      return (AttributeName) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public TermType getTermType(String type, String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - get term type "
        + type + "," + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT t from TermTypeJpa t where terminology = :terminology and version = :version and abbreviation = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    try {
      return (TermType) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public RelationshipType getRelationshipType(String type, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get relationship type " + type + ","
            + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT r from RelationshipTypeJpa r where terminology = :terminology and version = :version and abbreviation = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    try {
      return (RelationshipType) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public AdditionalRelationshipType getAdditionalRelationshipType(String type,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata service - get additional relationship type " + type
            + "," + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT r from AdditionalRelationshipTypeJpa r where terminology = :terminology and version = :version and abbreviation = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    try {
      return (AdditionalRelationshipType) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public Language getLanguage(String language, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata service - getterm type "
        + language + "," + terminology + ", " + version);
    final Query query = manager.createQuery(
        "SELECT l from LanguageJpa l " + "where terminology = :terminology and "
            + "version = :version and abbreviation = :language");
    query.setParameter("language", language);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    try {
      return (Language) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    super.refreshCaches();
    init();
    validateInit();
  }

  /**
   * Validate init.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void validateInit() throws Exception {
    if (graphResolverMap == null) {
      throw new Exception(
          "Graph resolver did not properly initialize, serious error.");
    }
  }
}
