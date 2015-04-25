/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Map;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Services to retrieve metadata objects.
 */
public interface MetadataService extends RootService, Configurable {

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
    /** The General_ metadata_ entries. */
    General_Metadata_Entries,
    /** The Semantic_ types. */
    Semantic_Types,
    /** The Term_ types. */
    Term_Types,
    /** The Hierarchical_ relationship_ types. */
    Hierarchical_Relationship_Types
  }

  /**
   * Enable listeners.
   */
  public void enableListeners();

  /**
   * Disable listeners.
   */
  public void disableListeners();

  /**
   * Returns the terminologies.
   * 
   * @return the terminologies
   * @throws Exception if anything goes wrong
   */
  public List<RootTerminology> getTerminologies() throws Exception;

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
  public List<Terminology> getVersions(String terminology) throws Exception;

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
  public List<Terminology> getTerminologyLatestVersions() throws Exception;

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
  public List<RelationshipType> getRelationshipTypes(String terminology,
    String version) throws Exception;

  /**
   * Returns the additional relation types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the additional relation types
   * @throws Exception the exception
   */
  public List<AdditionalRelationshipType> getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception;

  /**
   * Returns the attribute names.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the attribute names
   * @throws Exception the exception
   */
  public List<AttributeName> getAttributeNames(String terminology,
    String version) throws Exception;

  /**
   * Returns the semantic types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the semantic types
   * @throws Exception the exception
   */
  public List<SemanticType> getSemanticTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the term types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the term types
   * @throws Exception the exception
   */
  public List<TermType> getTermTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the hierarchical relationship types. The idea is that these
   * relationship types define "parent" and "child" relationships. When looking
   * through a concept's relationships, anything with one of these types means
   * the destinationId is a "parent". When looking through a concept's inverse
   * relationships, anything with one of these types means the sourceId is a
   * "child".
   * 
   * @param terminology the terminology
   * @param version the version
   * @return the relationship types
   * @throws Exception if anything goes wrong
   */
  public List<RelationshipType> getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception;

  /**
   * Indicates whether or not hierarchcial relationship is the case.
   *
   * @param relationship the r
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isHierarchcialRelationship(Relationship<?, ?> relationship);

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
  public List<RelationshipType> getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception;

  /**
   * Returns the general metadata entries.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the general metadata entries
   */
  public List<GeneralMetadataEntry> getGeneralMetadataEntries(
    String terminology, String version);

  /**
   * Returns the precedence list.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the precedence list
   */
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version);

  /**
   * Indicates whether or not to assign last modified when changing terminology
   * components. Supports a loader that wants to disable this feature.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isLastModifiedFlag();

  /**
   * Sets the last modified flag.
   *
   * @param lastModifiedFlag the last modified flag
   */
  public void setLastModifiedFlag(boolean lastModifiedFlag);

  /**
   * Clear metadata.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  public void clearMetadata(String terminology, String version)
    throws Exception;

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
  public GeneralMetadataEntry addGeneralMetadataEntry(GeneralMetadataEntry entry) throws Exception;

  /**
   * Update general metadata entry.
   *
   * @param entry the entry
   * @throws Exception the exception
   */
  public void updateGeneralMetadataEntry(GeneralMetadataEntry entry) throws Exception;

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
  public PrecedenceList addPrecedenceList(PrecedenceList list)
    throws Exception;

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

}
