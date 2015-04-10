/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.List;
import java.util.Map;

import javax.management.relation.RelationType;

import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdentifierType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;

/**
 * Default implementation of {@link MetadataService}.
 */
public class DefaultMetadataServiceJpaHelper extends
    AbstractMetadataServiceJpaHelper {

  /**
   * Instantiates an empty {@link DefaultMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public DefaultMetadataServiceJpaHelper() throws Exception {
    super();
  }

  @Override
  public List<RootTerminology> getTerminologies() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Terminology> getVersions(String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Terminology getLatestVersion(String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<RootTerminology, Terminology> getTerminologyLatestVersions()
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RelationType> getRelationTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<AdditionalRelationshipType> getAdditionalRelationTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<AttributeName> getAttributeNames(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IdentifierType> getIdentifierTypes(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<SemanticType> getSemanticTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TermType> getTermTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RelationshipType> getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RelationshipType> getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GeneralMetadataEntry> getGeneralMetadataEntries(
    String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TermType> getTermTypePrecedenceList(String terminology,
    String version) {
    // TODO Auto-generated method stub
    return null;
  }



}
