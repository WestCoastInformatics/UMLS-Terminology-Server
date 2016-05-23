/*
 *    Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreeList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.StringClass;

/**
 * The Interface ContentServiceRest.
 */
public interface ContentServiceRest {

  /**
   * Gets the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
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
   * Find concepts for general query.
   *
   * @param query the query
   * @param jql the jql
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForGeneralQuery(String query, String jql,
    PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @param parentsOnly the parents only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the concept list
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
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Gets the descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the descriptor
   * @throws Exception the exception
   */
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Gets the subset members for atom.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the subset members for atom
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembersForAtom(String terminologyId,
    String terminology, String version, String authToken) throws Exception;

  /**
   * Gets the subset members for concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the subset members for concept
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembersForConcept(String terminologyId,
    String terminology, String version, String authToken) throws Exception;

  /**
   * Find deep relationships for concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param filter the filter
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findDeepRelationshipsForConcept(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String filter,
    String authToken) throws Exception;

  /**
   * Find relationships for descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find relationships for code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForCode(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find relationships for concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findRelationshipsForConcept(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

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
   * Find descriptors for general query.
   *
   * @param query the query
   * @param jql the jql
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForGeneralQuery(String query,
    String jql, PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @return the descriptor list
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
   * @return the descriptor list
   * @throws Exception the exception
   */
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly,
    PfsParameterJpa pfsParameter, String authToken) throws Exception;

  /**
   * Gets the code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
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
   * Find codes for general query.
   *
   * @param query the query
   * @param jql the jql
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForGeneralQuery(String query, String jql,
    PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @param parentsOnly the parents only
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the code list
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
   * @return the code list
   * @throws Exception the exception
   */
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, PfsParameterJpa pfsParameter,
    String authToken) throws Exception;

  /**
   * Gets the lexical class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the lexical class
   * @throws Exception the exception
   */
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Gets the string class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the string class
   * @throws Exception the exception
   */
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Lucene reindex.
   *
   * @param indexedObjects the indexed objects
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void luceneReindex(String indexedObjects, String authToken)
    throws Exception;

  /**
   * Compute transitive closure.
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
   * Load terminology rrf.
   *
   * @param terminology the terminology
   * @param version the version
   * @param singleMode the single mode
   * @param codeFlag the code flag
   * @param prefix the prefix
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRrf(String terminology, String version,
    Boolean singleMode, Boolean codeFlag, String prefix, String inputDir,
    String authToken) throws Exception;

  /**
   * Load terminology rf2 snapshot.
   *
   * @param terminology the terminology
   * @param version the version
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRf2Snapshot(String terminology, String version,
    String inputDir, String authToken) throws Exception;

  /**
   * Load terminology rf2 delta.
   *
   * @param terminology the terminology
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRf2Delta(String terminology, String inputDir,
    String authToken) throws Exception;

  /**
   * Load terminology rf2 full.
   *
   * @param terminology the terminology
   * @param version the version
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRf2Full(String terminology, String version,
    String inputDir, String authToken) throws Exception;

  /**
   * Load terminology claml.
   *
   * @param terminology the terminology
   * @param version the version
   * @param inputFile the input file
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyClaml(String terminology, String version,
    String inputFile, String authToken) throws Exception;

  /**
   * Load terminology owl.
   *
   * @param terminology the terminology
   * @param version the version
   * @param inputFile the input file
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyOwl(String terminology, String version,
    String inputFile, String authToken) throws Exception;

  /**
   * Remove terminology.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean removeTerminology(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Find concept trees.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findConceptTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find descriptor trees.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findDescriptorTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find code trees.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findCodeTrees(String terminologyId, String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find concept tree for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  public Tree findConceptTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find descriptor tree for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  public Tree findDescriptorTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find code tree for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  public Tree findCodeTreeForQuery(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

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

  /**
   * Find concept tree children.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findConceptTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
      throws Exception;

  /**
   * Find descriptor tree children.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findDescriptorTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
      throws Exception;

  /**
   * Find code tree children.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findCodeTreeChildren(String terminology, String version,
    String terminologyId, PfsParameterJpa pfs, String authToken)
      throws Exception;

  /**
   * Find concept tree roots.
   *
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  public Tree findConceptTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find code tree roots.
   *
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  public Tree findCodeTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find descriptor tree roots.
   *
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree
   * @throws Exception the exception
   */
  public Tree findDescriptorTreeRoots(String terminology, String version,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Gets the map set.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the map set
   * @throws Exception the exception
   */
  public MapSet getMapSet(String terminologyId, String terminology,
    String version, String authToken) throws Exception;

  /**
   * Gets the map sets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the map sets
   * @throws Exception the exception
   */
  public MapSetList getMapSets(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Find mappings for map set.
   *
   * @param mapSetId the map set id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findMappingsForMapSet(String mapSetId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
      throws Exception;

  /**
   * Find mappings for concept.
   *
   * @param mapSetId the map set id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findMappingsForConcept(String mapSetId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
      throws Exception;

  /**
   * Find mappings for code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findMappingsForCode(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find mappings for descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findMappingsForDescriptor(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Compute ecl indexes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void computeExpressionIndexes(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Gets the ecl expression result count.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param authToken the auth token
   * @return the ecl expression result count
   * @throws Exception the exception
   */
  public Integer getEclExpressionResultCount(String terminology, String version,
    String query, String authToken) throws Exception;

  /**
   * Gets the ecl expression results.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param authToken the auth token
   * @return the ecl expression results
   * @throws Exception the exception
   */
  public SearchResultList getEclExpressionResults(String terminology,
    String version, String query, String authToken) throws Exception;

  /**
   * Gets the favorites for user.
   *
   * @param pfs the pfs
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the favorites for user
   * @throws Exception the exception
   */
  public SearchResultList getFavoritesForUser(String terminology,
    String version, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Remove concept note.
   *
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeConceptNote(Long noteId, String authToken) throws Exception;

  /**
   * Add concept note.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addConceptNote(String terminology, String version,
    String terminologyId, String noteText, String authToken) throws Exception;

  /**
   * Add code note.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addCodeNote(String terminology, String version,
    String terminologyId, String noteText, String authToken) throws Exception;

  /**
   * Remove code note.
   *
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeCodeNote(Long noteId, String authToken) throws Exception;

  /**
   * Add descriptor note.
   *
   * @param terminology the terminology
   * @param version the version
   * @param terminologyId the terminology id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addDescriptorNote(String terminology, String version,
    String terminologyId, String noteText, String authToken) throws Exception;

  /**
   * Remove descriptor note.
   *
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeDescriptorNote(Long noteId, String authToken)
    throws Exception;

}
