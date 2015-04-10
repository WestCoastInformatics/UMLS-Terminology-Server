/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Map;

import javax.management.relation.RelationType;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdentifierType;
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
    /** The Identifier types. */
    Identifier_Types,
    /** The Semantic_ types. */
    Semantic_Types,
    /** The Term_ types. */
    Term_Types,
    /** The Hierarchical_ relationship_ types. */
    Hierarchical_Relationship_Types,
    /** Stated relationship types. */
    Stated_Characteristic_Types,
    /** Inferred relationship types. */
    Inferred_Characteristic_Types;
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
  public Terminology getLatestVersion(String terminology) throws Exception;

  /**
   * Returns the terminology latest versions.
   * 
   * @return the terminology latest versions
   * @throws Exception if anything goes wrong
   */
  public Map<RootTerminology, Terminology> getTerminologyLatestVersions()
    throws Exception;

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
  public List<RelationType> getRelationTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the additional relation types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the additional relation types
   * @throws Exception the exception
   */
  public List<AdditionalRelationshipType> getAdditionalRelationTypes(
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
   * Returns the identifier types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the identifier types
   * @throws Exception the exception
   */
  public List<IdentifierType> getIdentifierTypes(String terminology,
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
  public List<TermType> getTermTypePrecedenceList(String terminology,
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
   * Adds the identifier type.
   *
   * @param IdentifierType the identifier type
   * @return the identifier type
   * @throws Exception the exception
   */
  public IdentifierType addIdentifierType(IdentifierType IdentifierType)
    throws Exception;

  /**
   * Update identifier type.
   *
   * @param IdentifierType the identifier type
   * @throws Exception the exception
   */
  public void updateIdentifierType(IdentifierType IdentifierType)
    throws Exception;

  /**
   * Removes the identifier type.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeIdentifierType(Long id) throws Exception;

  /**
   * Adds the language.
   *
   * @param Language the language
   * @return the language
   * @throws Exception the exception
   */
  public Language addLanguage(Language Language)
    throws Exception;

  /**
   * Update language.
   *
   * @param Language the identifier type
   * @throws Exception the exception
   */
  public void updateLanguage(Language Language)
    throws Exception;

  /**
   * Removes the langauge.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLanguage(Long id) throws Exception;

}
