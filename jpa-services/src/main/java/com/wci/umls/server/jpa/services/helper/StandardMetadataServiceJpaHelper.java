/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.GeneralMetadataEntryList;
import com.wci.umls.server.helpers.meta.LabelSetList;
import com.wci.umls.server.helpers.meta.LanguageList;
import com.wci.umls.server.helpers.meta.PropertyChainList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.GeneralMetadataEntryListJpa;
import com.wci.umls.server.jpa.helpers.meta.LabelSetListJpa;
import com.wci.umls.server.jpa.helpers.meta.LanguageListJpa;
import com.wci.umls.server.jpa.helpers.meta.PropertyChainListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.services.MetadataService;

/**
 * Default implementation of {@link MetadataService}.
 */
public class StandardMetadataServiceJpaHelper extends
    AbstractMetadataServiceJpaHelper {

  /**
   * Instantiates an empty {@link StandardMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public StandardMetadataServiceJpaHelper() throws Exception {
    super();
  }

  /* see superclass */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from RelationshipTypeJpa r where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public LanguageList getLanguages(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from LanguageJpa r where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    LanguageList types = new LanguageListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public PropertyChainList getPropertyChains(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from PropertyChainJpa r where terminology = :terminology"
                + " and version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    PropertyChainList types = new PropertyChainListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from AdditionalRelationshipTypeJpa r where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    AdditionalRelationshipTypeList types =
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
    javax.persistence.Query query =
        manager
            .createQuery("SELECT a from AttributeNameJpa a where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    AttributeNameList names = new AttributeNameListJpa();
    names.setObjects(query.getResultList());
    names.setTotalCount(names.getObjects().size());
    return names;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public LabelSetList getLabelSets(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT a from LabelSetJpa a where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    LabelSetList labelSets = new LabelSetListJpa();
    labelSets.setObjects(query.getResultList());
    labelSets.setTotalCount(labelSets.getObjects().size());
    return labelSets;
  }
  
  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public SemanticTypeList getSemanticTypes(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT s from SemanticTypeJpa s where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    SemanticTypeList types = new SemanticTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT t from TermTypeJpa t where terminology = :terminology"
                + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    TermTypeList types = new TermTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @Override
  public boolean isStatedRelationship(Relationship<?, ?> relationship) {
    return true;
  }

  /* see superclass */
  @Override
  public boolean isInferredRelationship(Relationship<?, ?> relationship) {
    return true;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipTypeList getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT r from RelationshipTypeJpa r "
            + " where groupingType = 0" + " and terminology = :terminology"
            + " and version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public GeneralMetadataEntryList getGeneralMetadataEntries(String terminology,
    String version) {
    javax.persistence.Query query =
        manager.createQuery("SELECT g from GeneralMetadataEntryJpa g"
            + " where terminology = :terminology" + " and version = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    GeneralMetadataEntryList entries = new GeneralMetadataEntryListJpa();
    entries.setObjects(query.getResultList());
    entries.setTotalCount(entries.getObjects().size());
    return entries;
  }

  /* see superclass */
  @Override
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version) throws Exception {

    javax.persistence.Query query =
        manager.createQuery("SELECT p from PrecedenceListJpa p"
            + " where defaultList = 1 and terminology = :terminology "
            + " and version = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    try {
      return (PrecedenceList) query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    close();
    manager = factory.createEntityManager();
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Standard Metadata Handler";
  }

  /* see superclass */
  @Override
  public SemanticTypeList getSemanticTypeDescendants(String terminology,
    String version, String treeNumber, boolean includeSelf) throws Exception {
    List<SemanticType> descendants = new ArrayList<>();
    SemanticTypeList allStys = getSemanticTypes(terminology, version);
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
  

  //
  // Single object metadata retrieval by abbreviation or expanded form
  //  

  @Override
  public SemanticType getSemanticType(String type, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - get semanticType "
        + type + "," + terminology + "," + version);

    javax.persistence.Query query = manager.createQuery(
        "SELECT s from SemanticTypeJpa s where terminology = :terminology and version = :version and expandedForm = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    SemanticType result = null;
    try {
      result = (SemanticType) query.getSingleResult();
      return result;
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public AttributeName getAttributeName(String name, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - get attributeName "
        + name + "," + terminology + "," + version);

    javax.persistence.Query query = manager.createQuery(
        "SELECT a from AttributeNameJpa a where terminology = :terminology and version = :version and abbreviation = :name");
    query.setParameter("name", name);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    AttributeName result = null;
    try {
      result = (AttributeName) query.getSingleResult();
      return result;
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public TermType getTermType(String type, String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - get termType " + type
        + "," + terminology + "," + version);

    javax.persistence.Query query = manager.createQuery(
        "SELECT t from TermTypeJpa t where terminology = :terminology and version = :version and abbreviation = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    TermType result = null;
    try {
      result = (TermType) query.getSingleResult();
      return result;
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public RelationshipType getRelationshipType(String type, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - get relationshipType " + type + ","
            + terminology + "," + version);

    javax.persistence.Query query = manager.createQuery(
        "SELECT r from RelationshipTypeJpa r where terminology = :terminology and version = :version and abbreviation = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    RelationshipType result = null;
    try {
      result = (RelationshipType) query.getSingleResult();
      return result;
    } catch (NoResultException e) {
      return null;
    }

  }

  @Override
  public AdditionalRelationshipType getAdditionalRelationshipType(String type,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass())
        .debug("Metadata Service - get additionalRelationshipType " + type + ","
            + terminology + "," + version);

    javax.persistence.Query query = manager.createQuery(
        "SELECT r from AdditionalRelationshipTypeJpa r where terminology = :terminology and version = :version and abbreviation = :type");
    query.setParameter("type", type);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    AdditionalRelationshipType result = null;
    try {
      result = (AdditionalRelationshipType) query.getSingleResult();
      return result;
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public Language getLanguage(String language, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Metadata Service - get language " + language
        + "," + terminology + "," + version);

    javax.persistence.Query query = manager.createQuery(
        "SELECT l from Language l where terminology = :terminology and version = :version and abbreviation = :language");
    query.setParameter("language", language);
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);

    Language result = null;
    try {
      result = (Language) query.getSingleResult();
      return result;
    } catch (NoResultException e) {
      return null;
    }
  }

  
}
