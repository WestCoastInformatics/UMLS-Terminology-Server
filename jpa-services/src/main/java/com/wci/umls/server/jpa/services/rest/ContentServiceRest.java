/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
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
   * Autocomplete concepts.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm, String authToken) throws Exception;

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
  public SubsetMemberList getSubsetMembersForAtom(String atomId,
    String terminology, String version, String authToken) throws Exception;

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
  public SubsetMemberList getSubsetMembersForConcept(String conceptId,
    String terminology, String version, String authToken) throws Exception;

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
   * Autocomplete descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm, String authToken) throws Exception;

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
  public Code getCode(String terminologyId, String terminology, String version,
    String authToken) throws Exception;

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
  public SearchResultList findCodesForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Autocomplete codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm, String authToken) throws Exception;

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
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, PfsParameterJpa pfsParameter,
    String authToken) throws Exception;

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
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, PfsParameterJpa pfsParameter,
    String authToken) throws Exception;

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
   * Load all termionlogies from an RRF directory.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param singleMode the single mode
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRrf(String terminology, String version,
    boolean singleMode, String inputDir, String authToken) throws Exception;

  /**
   * Load terminology snapshot from RF2 directory.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRf2Snapshot(String terminology, String version,
    String inputDir, String authToken) throws Exception;

  /**
   * Load terminology full from RF2 directory.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRf2Full(String terminology, String version,
    String inputDir, String authToken) throws Exception;

  /**
   * Load terminology delta from RF2 directory.
   *
   * @param terminology the terminology
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRf2Delta(String terminology, String inputDir,
    String authToken) throws Exception;

  /**
   * Load terminology from ClaML file.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param inputFile the input file
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyClaml(String terminology, String version,
    String inputFile, String authToken) throws Exception;

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

  /**
   * Gets the tree positions for a concept/code/descriptor given search criteria.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param query the query the lexical search query string
   * @param searchCriteria the search criteria containing semantic search information
   * @param authToken the auth token
   * @return the tree positions for query and search criteria
   * @throws Exception the exception
   */
  public TreeList getTreePositionsForQuery(String terminology, String version,
    String query, SearchCriteria searchCriteria, String authToken)
    throws Exception;

  /**
   * Autocomplete concept query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param authToken the auth token
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteConceptQuery(String terminology, String version, String query, String authToken) throws Exception;

 

}
