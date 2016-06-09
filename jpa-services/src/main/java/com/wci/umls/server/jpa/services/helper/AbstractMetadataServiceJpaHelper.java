/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.RootTerminologyList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.services.RootServiceJpa;
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
 * Default implementation of {@link MetadataService}.
 */
public abstract class AbstractMetadataServiceJpaHelper extends RootServiceJpa
    implements MetadataService {

  /**
   * Instantiates an empty {@link AbstractMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public AbstractMetadataServiceJpaHelper() throws Exception {
    super();
  }

  //
  // Not needed for sub-handler

  /* see superclass */
  @Override
  public RootTerminologyList getRootTerminologies() throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public RootTerminology getRootTerminology(String terminology)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public Terminology getTerminology(String terminology, String version)
    throws Exception {
    // n/a
    return null;
  }

  @Override
  public TerminologyList getVersions(String terminology) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public String getLatestVersion(String terminology) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public TerminologyList getTerminologyLatestVersions() throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public Terminology getTerminologyLatestVersion(String terminology)
    throws Exception {
    // n/a
    return null;
  }

  @Override
  public TerminologyList getTerminologies() throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    // n/a handled by superclass
    return null;
  }

  /* see superclass */
  @Override
  public boolean isLastModifiedFlag() {
    // n/a
    return false;
  }

  /* see superclass */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    // n/a

  }

  /* see superclass */
  @Override
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateSemanticType(SemanticType semanticType) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeSemanticType(Long id) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public AttributeName addAttributeName(AttributeName AttributeName)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateAttributeName(AttributeName AttributeName) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeAttributeName(Long id) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public LabelSet addLabelSet(LabelSet labelSet) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateLabelSet(LabelSet labelSet) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void removeLabelSet(Long id) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public Language addLanguage(Language Language) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateLanguage(Language Language) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeLanguage(Long id) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeAdditionalRelationshipType(Long id) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public PropertyChain addPropertyChain(PropertyChain propertyChain)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updatePropertyChain(PropertyChain propertyChain) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void removePropertyChain(Long id) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public RelationshipType addRelationshipType(RelationshipType relationshipType)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateRelationshipType(RelationshipType relationshipType)
    throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void removeRelationshipType(Long id) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public Terminology addTerminology(Terminology terminology) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateTerminology(Terminology terminology) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeTerminology(Long id) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public RootTerminology addRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateRootTerminology(RootTerminology rootTerminology)
    throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeRootTerminology(Long id) throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public TermType addTermType(TermType termType) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateTermType(TermType termType) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void removeTermType(Long id) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public GeneralMetadataEntry addGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updateGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void removeGeneralMetadataEntry(Long id) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public PrecedenceList addPrecedenceList(PrecedenceList list) throws Exception {
    // n/a
    return null;
  }

  /* see superclass */
  @Override
  public void updatePrecedenceList(PrecedenceList list) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void removePrecedenceList(Long id) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public GraphResolutionHandler getGraphResolutionHandler(String terminology)
    throws Exception {
    // n/a
    return null;
  }

}
