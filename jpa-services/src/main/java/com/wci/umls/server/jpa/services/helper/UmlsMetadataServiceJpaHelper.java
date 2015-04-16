/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.List;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
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
  @Override
  public List<RelationshipType> getRelationshipTypes(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT r from RelationshipTypeJpa r");
    @SuppressWarnings("unchecked")
    List<RelationshipType> types = query.getResultList();
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAdditionalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @Override
  public List<AdditionalRelationshipType> getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT r from AdditionalRelationshipTypeJpa r");
    @SuppressWarnings("unchecked")
    List<AdditionalRelationshipType> types = query.getResultList();
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAttributeNames(java.lang
   * .String, java.lang.String)
   */
  @Override
  public List<AttributeName> getAttributeNames(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT a from AttributeNameJpa a");
    @SuppressWarnings("unchecked")
    List<AttributeName> names = query.getResultList();
    return names;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getSemanticTypes(java.lang
   * .String, java.lang.String)
   */
  @Override
  public List<SemanticType> getSemanticTypes(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT s from SemanticTypeJpa s");
    @SuppressWarnings("unchecked")
    List<SemanticType> types = query.getResultList();
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTermTypes(java.lang.String,
   * java.lang.String)
   */
  @Override
  public List<TermType> getTermTypes(String terminology, String version)
    throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT t from TermTypeJpa t");
    @SuppressWarnings("unchecked")
    List<TermType> types = query.getResultList();
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getHierarchicalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @Override
  public List<RelationshipType> getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from RelationshipTypeJpa r where abbreviation = :rel");
    query.setParameter("rel", "PAR");
    @SuppressWarnings("unchecked")
    List<RelationshipType> types = query.getResultList();
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
  @Override
  public List<RelationshipType> getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT r from RelationshipTypeJpa r where groupingType = 0");
    @SuppressWarnings("unchecked")
    List<RelationshipType> types = query.getResultList();
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getGeneralMetadataEntries(
   * java.lang.String, java.lang.String)
   */
  @Override
  public List<GeneralMetadataEntry> getGeneralMetadataEntries(
    String terminology, String version) {
    javax.persistence.Query query =
        manager.createQuery("SELECT g from GeneralMetadataEntryJpa g");
    @SuppressWarnings("unchecked")
    List<GeneralMetadataEntry> entries = query.getResultList();
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
  public List<TermType> getTermTypePrecedenceList(String terminology,
    String version) {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT p from PrecedenceListJpa p where defaultList = 1");
    PrecedenceList list = (PrecedenceList) query.getSingleResult();
    return list.getTermTypes();
  }

}
