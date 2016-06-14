/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.helpers.Configurable;
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
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;

/**
 * Handler for terminology-specific metadata lookups.
 */
public interface MetadataHandler extends Configurable {

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
   * Returns the general metadata entries.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the general metadata entries
   */
  public GeneralMetadataEntryList getGeneralMetadataEntries(String terminology,
    String version);

  /**
   * Returns the marked label sets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the marked sets.
   * @throws Exception the exception
   */
  public LabelSetList getLabelSets(String terminology, String version)
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
   * Refresh caches.
   *
   * @throws Exception the exception
   */
  public void refreshCaches() throws Exception;
}
