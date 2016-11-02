/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.SemanticTypeList;
import com.wci.umls.server.helpers.meta.TerminologyList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RootTerminologyJpa;
import com.wci.umls.server.jpa.meta.TermTypeJpa;
import com.wci.umls.server.jpa.meta.TerminologyJpa;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Represents a security available via a REST service.
 */
public interface MetadataServiceRest {

  /**
   * Returns all metadata for a terminology and version.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the all metadata
   * @throws Exception if anything goes wrong
   */
  public KeyValuePairLists getAllMetadata(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Returns all terminologies and all versions.
   *
   * @param authToken the auth token
   * @return all terminologies and versions
   * @throws Exception if anything goes wrong
   */

  public TerminologyList getCurrentTerminologies(String authToken)
    throws Exception;

  /**
   * Gets the terminology information for a terminology.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the terminology information
   * @throws Exception the exception
   */
  public Terminology getTerminology(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Gets the default precedence list.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the default precedence list
   * @throws Exception the exception
   */
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version, String authToken) throws Exception;

  /**
   * Add precedence list.
   *
   * @param precedenceList the precedence list
   * @param authToken the auth token
   * @return the precedence list
   * @throws Exception the exception
   */
  public PrecedenceList addPrecedenceList(PrecedenceListJpa precedenceList,
    String authToken) throws Exception;

  /**
   * Update precedence list.
   *
   * @param precedenceList the precedence list
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updatePrecedenceList(PrecedenceListJpa precedenceList,
    String authToken) throws Exception;

  /**
   * Remove precedence list.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removePrecedenceList(Long id, String authToken) throws Exception;

  /**
   * Gets the precedence list.
   *
   * @param precedenceListId the precedence list id
   * @param authToken the auth token
   * @return the precedence list
   * @throws Exception the exception
   */
  public PrecedenceList getPrecedenceList(Long precedenceListId,
    String authToken) throws Exception;

  /**
   * Returns the semantic types.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the semantic types
   * @throws Exception the exception
   */
  public SemanticTypeList getSemanticTypes(String terminology, String version,
    String authToken) throws Exception;


  /**
   * Removes the term type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTermType(String type, String terminology, String version, String authToken) throws Exception;

  /**
   * Removes the attribute name.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAttributeName(String type, String terminology, String version, String authToken) throws Exception;

  /**
   * Removes the relationship type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeRelationshipType(String type, String terminology, String version, String authToken) throws Exception;

  /**
   * Removes the additional relationship type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAdditionalRelationshipType(String type, String terminology, String version, String authToken)
		throws Exception;

  /**
   * Gets the root terminology.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @return the root terminology
   * @throws Exception the exception
   */
  public RootTerminology getRootTerminology(String terminology, String authToken) throws Exception;

  /**
   * Gets the term type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the term type
   * @throws Exception the exception
   */
  public TermType getTermType(String type, String terminology, String version,
    String authToken) throws Exception;

  /**
   * Gets the attribute name.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the attribute name
   * @throws Exception the exception
   */
  public AttributeName getAttributeName(String type, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Gets the relationship type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the relationship type
   * @throws Exception the exception
   */
  public RelationshipType getRelationshipType(String type, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Gets the additional relationship type.
   *
   * @param type the type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the additional relationship type
   * @throws Exception the exception
   */
  public AdditionalRelationshipType getAdditionalRelationshipType(String type,
    String terminology, String version, String authToken) throws Exception;

  /**
   * Update additional relationship type.
   *
   * @param relType the rel type
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAdditionalRelationshipType(AdditionalRelationshipTypeJpa relType,
    String authToken) throws Exception;

  /**
   * Update relationship type.
   *
   * @param relType the rel type
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateRelationshipType(RelationshipTypeJpa relType, String authToken)
    throws Exception;

  /**
   * Update attribute name.
   *
   * @param attributeName the attribute name
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateAttributeName(AttributeNameJpa attributeName, String authToken)
    throws Exception;

  /**
   * Update term type.
   *
   * @param termType the term type
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateTermType(TermTypeJpa termType, String authToken) throws Exception;

  /**
   * Add additional relationship type.
   *
   * @param addRelType the add rel type
   * @param authToken the auth token
   * @return the additional relationship type
   * @throws Exception the exception
   */
  public AdditionalRelationshipType addAdditionalRelationshipType(
    AdditionalRelationshipTypeJpa addRelType, String authToken)
    throws Exception;

  /**
   * Add relationship type.
   *
   * @param relationshipType the relationship type
   * @param authToken the auth token
   * @return the relationship type
   * @throws Exception the exception
   */
  public RelationshipType addRelationshipType(RelationshipTypeJpa relationshipType,
    String authToken) throws Exception;

  /**
   * Add attribute name.
   *
   * @param attributeName the attribute name
   * @param authToken the auth token
   * @return the attribute name
   * @throws Exception the exception
   */
  public AttributeName addAttributeName(AttributeNameJpa attributeName,
    String authToken) throws Exception;

  /**
   * Add term type.
   *
   * @param termType the term type
   * @param authToken the auth token
   * @return the term type
   * @throws Exception the exception
   */
  public TermType addTermType(TermTypeJpa termType, String authToken) throws Exception;

  /**
   * Update root terminology.
   *
   * @param rootTerminology the root terminology
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateRootTerminology(RootTerminologyJpa rootTerminology,
    String authToken) throws Exception;

  /**
   * Update terminology.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateTerminology(TerminologyJpa terminology, String authToken)
    throws Exception;
}
