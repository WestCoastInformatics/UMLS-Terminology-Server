/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;


import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;


/**
 * Represents a content available via a REST service.
 */
public interface ContentServiceRest {


  /**
   * Returns the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Find concepts for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find ancestor concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Find descendant concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;
  
  /**
   * Returns the descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @return the descriptor
   * @throws Exception the exception
   */
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Gets the subset member for atom.
   *
   * @param atomId the atom id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the subset members for atom
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembersForAtom(String atomId, String terminology,
    String version, String authToken) throws Exception;
  
  /**
   * Gets the subset member for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the subset members for concept
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembersForConcept(String conceptId, String terminology,
    String version, String authToken) throws Exception;
  
  /**
   * Find descriptorss for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find ancestor descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Find descendant descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;
  
  /**
   * Returns the code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @return the code
   * @throws Exception the exception
   */
  public Code getCode(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Find codes for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find ancestor codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public CodeList findAncestorCodes(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Find descendant codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public CodeList findDescendantCodes(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;
  
  /**
   * Returns the lexical class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @return the lexical class
   * @throws Exception the exception
   */
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Find lexical classes for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findLexicalClassesForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Returns the string class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @return the string class
   * @throws Exception the exception
   */
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Find string classes for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findStringClassesForQuery(String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  
  /**
   * Recomputes lucene indexes for the specified objects as a comma-separated
   * string list.
   *
   * @param indexedObjects the indexed objects
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception;

  /**
   * Compute transitive closure for latest version of a terminology.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void computeTransitiveClosure(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Compute tree positions.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void computeTreePositions(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Load terminology from RRF directory.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRrf(String terminology, String version,
    String inputDir, String authToken) throws Exception;

  /**
   * Removes the terminology.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeTerminology(String terminology, String version,
    String authToken) throws Exception;

}
