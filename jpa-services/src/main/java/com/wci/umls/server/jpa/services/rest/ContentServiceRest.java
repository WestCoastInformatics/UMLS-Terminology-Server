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
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;

/**
 * Represents a content available via a REST service.
 */
public interface ContentServiceRest {

  /**
   * Value representing a blank query. Needed so that jersey can identify URL
   * containing an "optional" parameter.  This could be done alternatively
   * with complex regular expressions, but we feel this is a more straightforward
   * solution.
   */
  public final String QUERY_BLANK = "~BLANK~";

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
   * @param pfsc the pfsc
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfscParameterJpa pfsc, String authToken)
    throws Exception;

  /**
   * Find concepts for query.
   *
   * @param luceneQuery the lucene query
   * @param hqlQuery the hql query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForGeneralQuery(String luceneQuery,
    String hqlQuery, PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @param parentsOnly the children only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Find descendant concepts.
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
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly,
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
   * @param terminologyId the atom id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the subset members for atom
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembersForAtom(String terminologyId,
    String terminology, String version, String authToken) throws Exception;

  /**
   * Gets the subset member for concept.
   *
   * @param terminologyId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the subset members for concept
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembersForConcept(String terminologyId,
    String terminology, String version, String authToken) throws Exception;


  /**
   * Find relationships for concept or any part of its graph and push them all
   * up to the same level. For example a UMLS concept may return the CUI
   * relationships, the atom relationships, the SCUI, SDUI, and CODE
   * relationships - all represented as {@link ConceptRelationship}.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findDeepRelationshipsForConcept(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Returns the relationships for atom.
   *
   * @param terminologyId the atom id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationships for atom
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForAtom(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Returns the relationships for descriptor.
   *
   * @param terminologyId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationships for descriptor
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Returns the relationships for code.
   *
   * @param terminologyId the code id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationships for code
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForCode(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;
  
  /**
   * Returns the relationships for concept.
   *
   * @param terminologyId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationships for concept
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForConcept(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Find descriptors for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfsc the pfsc
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String query, PfscParameterJpa pfsc, String authToken)
    throws Exception;

  /**
   * Find descriptors for query.
   *
   * @param luceneQuery the lucene query
   * @param hqlQuery the hql query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForGeneralQuery(String luceneQuery,
    String hqlQuery, PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @param parentsOnly the parents only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean parentsOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Find descendant descriptors.
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
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly,
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
   * @param pfsc the pfsc
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForQuery(String terminology, String version,
    String query, PfscParameterJpa pfsc, String authToken) throws Exception;

  /**
   * Find codes for query.
   *
   * @param luceneQuery the lucene query
   * @param hqlQuery the hql query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForGeneralQuery(String luceneQuery,
    String hqlQuery, PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @param parentsOnly the children only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, PfsParameterJpa pfsParameter,
    String authToken) throws Exception;

  /**
   * Find descendant codes.
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
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, PfsParameterJpa pfsParameter,
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
   * Returns the tree positions for concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree positions for concept
   * @throws Exception the exception
   */
  public TreeList findTreesForConcept(String terminologyId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Returns the tree positions for descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree positions for descriptor
   * @throws Exception the exception
   */
  public TreeList findTreesForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Returns the tree positions for code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree positions for code
   * @throws Exception the exception
   */
  public TreeList findTreesForCode(String terminologyId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

  /**
   * Gets the atom subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the atom subsets
   * @throws Exception the exception
   */
  public SubsetList getAtomSubsets(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Gets the concept subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the concept subsets
   * @throws Exception the exception
   */
  public SubsetList getConceptSubsets(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Find atom subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the subset member list
   * @throws Exception the exception
   */
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find concept subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the subset member list
   * @throws Exception the exception
   */
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

}
