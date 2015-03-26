package com.wci.umls.server.services;

import java.util.List;
import java.util.Map;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.TermType;

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
   * Returns the terminologies.
   * 
   * @return the terminologies
   * @throws Exception if anything goes wrong
   */
  public List<String> getTerminologies() throws Exception;

  /**
   * Returns the versions.
   * 
   * @param terminology the terminology
   * @return the versions
   * @throws Exception if anything goes wrong
   */
  public List<String> getVersions(String terminology) throws Exception;

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
  public Map<String, String> getTerminologyLatestVersions() throws Exception;

  // ////////////////////////////
  // Basic retrieval services //
  // ////////////////////////////
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

  /**
   * Returns the relation types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the relation types
   * @throws Exception the exception
   */
  public Map<String, String> getRelationTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the additional relation types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the additional relation types
   * @throws Exception the exception
   */
  public Map<String, String> getAdditionalRelationTypes(String terminology,
    String version) throws Exception;

  /**
   * Returns the attribute names.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the attribute names
   * @throws Exception the exception
   */
  public Map<String, String> getAttributeNames(String terminology,
    String version) throws Exception;

  /**
   * Returns the identifier types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the identifier types
   * @throws Exception the exception
   */
  public Map<String, String> getIdentifierTypes(String terminology,
    String version) throws Exception;

  /**
   * Returns the semantic types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the semantic types
   * @throws Exception the exception
   */
  public Map<String, String> getSemanticTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the term types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the term types
   * @throws Exception the exception
   */
  public Map<String, String> getTermTypes(String terminology, String version)
    throws Exception;

  /**
   * Returns the map sets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the map sets
   * @throws Exception the exception
   */
  public Map<String, String> getMapSets(String terminology, String version)
    throws Exception;

  /**
   * Subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the map
   * @throws Exception the exception
   */
  public Map<String, String> Subsets(String terminology, String version)
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
  public Map<String, String> getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception;

  /**
   * Returns the non grouping relationship types.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the non grouping relationship types
   * @throws Exception the exception
   */
  public Map<String, String> getNonGroupingRelationshipTypes(
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
}
