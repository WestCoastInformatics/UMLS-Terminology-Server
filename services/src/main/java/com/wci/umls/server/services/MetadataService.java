/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.Map;

import com.wci.umls.server.helpers.Configurable;
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
import com.wci.umls.server.model.content.Relationship;
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
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Services to retrieve metadata objects.
 */
public interface MetadataService extends ProjectService, Configurable {

  /**
   * An enum for the keys of the get all metadata call.
   */
  public enum MetadataKeys {

    /** The Relationship types. */
    Relationship_Types,
    /** The Additional relationship types. */
    Additional_Relationship_Types,
    /** The Attribute names. */
    Attribute_Names,
    /** The Semantic_ types. */
    Semantic_Types,
    /** The Term_ types. */
    Term_Types,
    /** The Languages. */
    Languages,
    /** The General_ metadata_ entries. */
    General_Metadata_Entries,
    /** The Label sets. */
    Label_Sets,
  }

  /**
   * Returns the terminologies.
   * 
   * @return the terminologies
   * @throws Exception if anything goes wrong
   */
  public RootTerminologyList getRootTerminologies() throws Exception;

  /**
   * Returns the root terminology.
   *
   * @param terminology the terminology
   * @return the root terminology
   * @throws Exception the exception
   */
  public RootTerminology getRootTerminology(String terminology)
    throws Exception;

  /**
   * Returns the terminology.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the terminology
   * @throws Exception the exception
   */
  public Terminology getTerminology(String terminology, String version)
    throws Exception;

  /**
   * Returns the versions.
   * 
   * @param terminology the terminology
   * @return the versions
   * @throws Exception if anything goes wrong
   */
  public TerminologyList getVersions(String terminology) throws Exception;

  /**
   * Returns the latest version.
   * 
   * @param terminology the terminology
   * @return the latest version
   * @throws Exception if anything goes wrong
   */
  public String getLatestVersion(String terminology) throws Exception;

  /**
   * Returns the terminology latest versions.
   * 
   * @return the terminology latest versions
   * @throws Exception if anything goes wrong
   */
  public TerminologyList getTerminologyLatestVersions() throws Exception;

  /**
   * Returns the terminology latest version.
   *
   * @param terminology the terminology
   * @return the terminology latest version
   * @throws Exception the exception
   */
  public Terminology getTerminologyLatestVersion(String terminology)
    throws Exception;

  /**
   * Returns the terminologies.
   *
   * @return the terminologies
   * @throws Exception the exception
   */
  public TerminologyList getTerminologies() throws Exception;

  /**
   * Returns the current terminologies.
   *
   * @return the current terminologies
   * @throws Exception the exception
   */
  public TerminologyList getCurrentTerminologies() throws Exception;

  /**
   * Returns the all metadata.
   * 
   * @param terminology the terminology
   * @param version the version
   * @return all metadata
   * @throws Exception if anything goes wrong
   */
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception;

  // ////////////////////////////
  // Basic retrieval services //
  // ////////////////////////////

  /**
   * Returns the relation types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the relation types
   * @throws Exception the exception
   */
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception;

  /**
   * Returns the property chains.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the property chains
   * @throws Exception the exception
   */
  public PropertyChainList getPropertyChains(String terminology, String version)
    throws Exception;

  /**
   * Returns the additional relation types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the additional relation types
   * @throws Exception the exception
   */
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception;

  /**
   * Returns the attribute names.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the attribute names
   * @throws Exception the exception
   */
  public AttributeNameList getAttributeNames(String terminology, String version)
    throws Exception;

  /**
   * Returns the marked sets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the marked sets.
   * @throws Exception the exception
   */
  public LabelSetList getLabelSets(String terminology, String version)
    throws Exception;

  /**
   * Returns the semantic types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the semantic types
   * @throws Exception the exception
   */
  public SemanticTypeList getSemanticTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the term types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the term types
   * @throws Exception the exception
   */
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the languages.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the languages
   * @throws Exception the exception
   */
  public LanguageList getLanguages(String terminology, String version)
    throws Exception;

  /**
   * Indicates whether or not stated relationship is the case.
   *
   * @param relationship the r
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isStatedRelationship(Relationship<?, ?> relationship);

  /**
   * Indicates whether or not inferred relationship is the case.
   *
   * @param relationship the r
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isInferredRelationship(Relationship<?, ?> relationship);

  /**
   * Returns the non grouping relationship types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the non grouping relationship types
   * @throws Exception the exception
   */
  public RelationshipTypeList getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception;

  /**
   * Returns the general metadata entries.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the general metadata entries
   */
  public GeneralMetadataEntryList getGeneralMetadataEntries(String terminology,
    String version);

  /**
   * Returns the precedence list.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the precedence list
   * @throws Exception the exception
   */
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version) throws Exception;

  /**
   * Adds the semantic type.
   *
   * @param semanticType the semantic type
   * @return the semantic type
   * @throws Exception the exception
   */
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception;

  /**
   * Update semantic type.
   *
   * @param semanticType the semantic type
   * @throws Exception the exception
   */
  public void updateSemanticType(SemanticType semanticType) throws Exception;

  /**
   * Removes the semantic type.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSemanticType(Long id) throws Exception;

  /**
   * Adds the attribute name.
   *
   * @param AttributeName the attribute name
   * @return the attribute name
   * @throws Exception the exception
   */
  public AttributeName addAttributeName(AttributeName AttributeName)
    throws Exception;

  /**
   * Update attribute name.
   *
   * @param AttributeName the attribute name
   * @throws Exception the exception
   */
  public void updateAttributeName(AttributeName AttributeName) throws Exception;

  /**
   * Removes the attribute name.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAttributeName(Long id) throws Exception;

  /**
   * Adds the marked sets.
   *
   * @param labelSet the label set
   * @return the label set
   * @throws Exception the exception
   */
  public LabelSet addLabelSet(LabelSet labelSet) throws Exception;

  /**
   * Update label set.
   *
   * @param labelSet the label set
   * @throws Exception the exception
   */
  public void updateLabelSet(LabelSet labelSet) throws Exception;

  /**
   * Removes the label set.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLabelSet(Long id) throws Exception;

  /**
   * Adds the language.
   *
   * @param language the language
   * @return the language
   * @throws Exception the exception
   */
  public Language addLanguage(Language language) throws Exception;

  /**
   * Update language.
   *
   * @param language the language
   * @throws Exception the exception
   */
  public void updateLanguage(Language language) throws Exception;

  /**
   * Removes the language.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLanguage(Long id) throws Exception;

  /**
   * Adds the additional relationship type.
   *
   * @param additionalRelationshipType the additional relationship type
   * @return the additionalRelationshipType the additional relationship type
   * @throws Exception the exception
   */
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception;

  /**
   * Update additional relationship type.
   *
   * @param additionalRelationshipType the additional relationship type
   * @throws Exception the exception
   */
  public void updateAdditionalRelationshipType(
    AdditionalRelationshipType additionalRelationshipType) throws Exception;

  /**
   * Removes the additional relationship type.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAdditionalRelationshipType(Long id) throws Exception;

  /**
   * Adds the property chain.
   *
   * @param propertyChain the property chain
   * @return the term type
   * @throws Exception the exception
   */
  public PropertyChain addPropertyChain(PropertyChain propertyChain)
    throws Exception;

  /**
   * Updates the property chain.
   *
   * @param propertyChain the term type
   * @throws Exception the exception
   */
  public void updatePropertyChain(PropertyChain propertyChain) throws Exception;

  /**
   * Removes the property chain.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removePropertyChain(Long id) throws Exception;

  /**
   * Adds the relationship type.
   *
   * @param relationshipType the relationship type
   * @return the relationshipType the relationship type
   * @throws Exception the exception
   */
  public RelationshipType addRelationshipType(RelationshipType relationshipType)
    throws Exception;

  /**
   * Update relationship type.
   *
   * @param relationshipType the relationship type
   * @throws Exception the exception
   */
  public void updateRelationshipType(RelationshipType relationshipType)
    throws Exception;

  /**
   * Removes the relationship type.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRelationshipType(Long id) throws Exception;

  /**
   * Adds the term type.
   *
   * @param termType the term type
   * @return the term type
   * @throws Exception the exception
   */
  public TermType addTermType(TermType termType) throws Exception;

  /**
   * Update term type.
   *
   * @param termType the term type
   * @throws Exception the exception
   */
  public void updateTermType(TermType termType) throws Exception;

  /**
   * Removes the term type.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTermType(Long id) throws Exception;

  /**
   * Adds the general metadata entry.
   *
   * @param entry the entry
   * @return the general metadata entry
   * @throws Exception the exception
   */
  public GeneralMetadataEntry addGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception;

  /**
   * Update general metadata entry.
   *
   * @param entry the entry
   * @throws Exception the exception
   */
  public void updateGeneralMetadataEntry(GeneralMetadataEntry entry)
    throws Exception;

  /**
   * Removes the general metadata entry.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeGeneralMetadataEntry(Long id) throws Exception;

  /**
   * Adds the terminology.
   *
   * @param terminology the terminology
   * @return the terminology
   * @throws Exception the exception
   */
  public Terminology addTerminology(Terminology terminology) throws Exception;

  /**
   * Update terminology.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  public void updateTerminology(Terminology terminology) throws Exception;

  /**
   * Removes the terminology.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTerminology(Long id) throws Exception;

  /**
   * Adds the root terminology.
   *
   * @param rootTerminology the root terminology
   * @return the root terminology
   * @throws Exception the exception
   */
  public RootTerminology addRootTerminology(RootTerminology rootTerminology)
    throws Exception;

  /**
   * Update root terminology.
   *
   * @param rootTerminology the root terminology
   * @throws Exception the exception
   */
  public void updateRootTerminology(RootTerminology rootTerminology)
    throws Exception;

  /**
   * Removes the root terminology.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRootTerminology(Long id) throws Exception;

  /**
   * Adds the precedence list.
   *
   * @param list the list
   * @return the root terminology
   * @throws Exception the exception
   */
  public PrecedenceList addPrecedenceList(PrecedenceList list) throws Exception;

  /**
   * Update precedence list.
   *
   * @param list the list
   * @throws Exception the exception
   */
  public void updatePrecedenceList(PrecedenceList list) throws Exception;

  /**
   * Removes the precedence list.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removePrecedenceList(Long id) throws Exception;

  /**
   * Returns the graph resolution handler. This is configured internally but
   * made available through this service.
   *
   * @param terminology the terminology
   * @return the graph resolution handler
   * @throws Exception the exception
   */
  public GraphResolutionHandler getGraphResolutionHandler(String terminology)
    throws Exception;

  /**
   * Gets the semantic type descendants.
   *
   * @param terminology the terminology
   * @param version the version
   * @param treeNumber the tree number
   * @param includeSelf the include self
   * @return the semantic type descendants
   * @throws Exception the exception
   */
  public SemanticTypeList getSemanticTypeDescendants(String terminology,
    String version, String treeNumber, boolean includeSelf) throws Exception;

  /**
   * Gets the precedence list.
   *
   * @param precedenceListId the precedence list id
   * @return the precedence list
   * @throws Exception the exception
   */
  public PrecedenceList getPrecedenceList(Long precedenceListId)
    throws Exception;

  /**
   * Gets the semantic type for a terminology.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @return the semantic type
   * @throws Exception the exception
   */
  public SemanticType getSemanticType(String type, String terminology,
    String version) throws Exception;

  /**
   * Gets the attribute name.
   *
   * @param name the name
   * @param terminology the terminology
   * @param version the version
   * @return the attribute name
   * @throws Exception the exception
   */
  public AttributeName getAttributeName(String name, String terminology,
    String version) throws Exception;

  /**
   * Gets the term type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @return the term type
   * @throws Exception the exception
   */
  public TermType getTermType(String type, String terminology, String version)
    throws Exception;

  /**
   * Gets the relationship type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @return the relationship type
   * @throws Exception the exception
   */
  public RelationshipType getRelationshipType(String type, String terminology,
    String version) throws Exception;

  /**
   * Gets the additional relationship type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @return the additional relationship type
   * @throws Exception the exception
   */
  public AdditionalRelationshipType getAdditionalRelationshipType(String type,
    String terminology, String version) throws Exception;

  /**
   * Gets the language.
   *
   * @param language the language
   * @param terminology the terminology
   * @param version the version
   * @return the language
   * @throws Exception the exception
   */
  public Language getLanguage(String language, String terminology,
    String version) throws Exception;
}
