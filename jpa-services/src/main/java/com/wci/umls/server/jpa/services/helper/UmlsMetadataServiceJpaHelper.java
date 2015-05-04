/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.GeneralMetadataEntryList;
import com.wci.umls.server.helpers.meta.PropertyChainList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.GeneralMetadataEntryListJpa;
import com.wci.umls.server.jpa.helpers.meta.PropertyChainListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.SemanticTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.MetadataService;

/**
 * Default implementation of {@link MetadataService}.
 */
public class UmlsMetadataServiceJpaHelper extends
    AbstractMetadataServiceJpaHelper {

  /**
   * Instantiates an empty {@link UmlsMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public UmlsMetadataServiceJpaHelper() throws Exception {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getRelationshipTypes(java.
   * lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT r from RelationshipTypeJpa r");
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  @SuppressWarnings("unchecked")
  @Override
  public PropertyChainList getPropertyChains(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT r from PropertyChainJpa r");
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
        manager.createQuery("SELECT r from AdditionalRelationshipTypeJpa r");
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
        manager.createQuery("SELECT a from AttributeNameJpa a");
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
        manager.createQuery("SELECT s from SemanticTypeJpa s");
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
        manager.createQuery("SELECT t from TermTypeJpa t");
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
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from RelationshipTypeJpa r where abbreviation = :rel");
    query.setParameter("rel", "CHD");
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
    return relationship.getRelationshipType().equals("PAR");
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
    return false;
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
            .createQuery("SELECT r from RelationshipTypeJpa r where groupingType = 0");
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
        manager.createQuery("SELECT g from GeneralMetadataEntryJpa g");
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
    String version) {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT p from PrecedenceListJpa p where defaultList = 1");
    return (PrecedenceList) query.getSingleResult();
  }

}
