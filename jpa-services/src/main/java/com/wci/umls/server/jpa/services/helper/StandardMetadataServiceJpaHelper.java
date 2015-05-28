/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.GeneralMetadataEntryList;
import com.wci.umls.server.helpers.meta.LanguageList;
import com.wci.umls.server.helpers.meta.PropertyChainList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.GeneralMetadataEntryListJpa;
import com.wci.umls.server.jpa.helpers.meta.LanguageListJpa;
import com.wci.umls.server.jpa.helpers.meta.PropertyChainListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.model.content.Relationship;
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getRelationshipTypes(java.
   * lang.String, java.lang.String)
   */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from RelationshipTypeJpa r where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public LanguageList getLanguages(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from LanguageJpa r where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    LanguageList types = new LanguageListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }
  @SuppressWarnings("unchecked")
  @Override
  public PropertyChainList getPropertyChains(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from PropertyChainJpa r where terminology = :terminology"
                + " and terminologyVersion = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    PropertyChainList types = new PropertyChainListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAdditionalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from AdditionalRelationshipTypeJpa r where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    AdditionalRelationshipTypeList types =
        new AdditionalRelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());

    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAttributeNames(java.lang
   * .String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeNameList getAttributeNames(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT a from AttributeNameJpa a where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    AttributeNameList names = new AttributeNameListJpa();
    names.setObjects(query.getResultList());
    names.setTotalCount(names.getObjects().size());
    return names;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getSemanticTypes(java.lang
   * .String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SemanticTypeList getSemanticTypes(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT s from SemanticTypeJpa s where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    SemanticTypeList types = new SemanticTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTermTypes(java.lang.String,
   * java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT t from TermTypeJpa t where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    TermTypeList types = new TermTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getHierarchicalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipTypeList getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    // Here, not terminology specific
    javax.persistence.Query query =
        manager.createQuery("SELECT r from RelationshipTypeJpa r "
            + "where abbreviation = :rel " + "and terminology = :terminology"
            + " and terminologyVersion = :version");
    query.setParameter("rel", "CHD");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());

    return types;
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
    return relationship.getRelationshipType().equals("CHD");
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
    return true;
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
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getNonGroupingRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipTypeList getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from RelationshipTypeJpa r "
                + " where groupingType = 0"
                + " and terminology = :terminology"
                + " and terminologyVersion = :version");
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getGeneralMetadataEntries(
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public GeneralMetadataEntryList getGeneralMetadataEntries(String terminology,
    String version) {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT g from GeneralMetadataEntryJpa g"
                + " where terminology = :terminology"
                + " and terminologyVersion = :version");

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    GeneralMetadataEntryList entries = new GeneralMetadataEntryListJpa();
    entries.setObjects(query.getResultList());
    entries.setTotalCount(entries.getObjects().size());
    return entries;
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
    javax.persistence.Query query =
        manager
            .createQuery("SELECT p from PrecedenceListJpa p"
                + " where defaultList = 1");

    return (PrecedenceList) query.getSingleResult();
  }

  @Override
  public void refreshCaches() throws Exception {
    // n/a
  }

}
