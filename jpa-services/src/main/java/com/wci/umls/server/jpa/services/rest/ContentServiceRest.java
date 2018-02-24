/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import java.io.InputStream;
import java.util.Set;

import com.wci.umls.server.ValidationResult;
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
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.IdType;

// TODO: Auto-generated Javadoc
/**
 * Represents a service for managing content.
 */
public interface ContentServiceRest {

  /**
   * Gets the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param projectId the project id
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version, Long projectId, String authToken) throws Exception;

  /**
   * Returns the concept.
   *
   * @param conceptId the concept id
   * @param projectId the project id
   * @param authToken the auth token
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(Long conceptId, Long projectId, String authToken)
    throws Exception;

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
  public SearchResultList findConcepts(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find concepts for general query.
   *
   * @param query the query
   * @param JPQL the JPQL
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForGeneralQuery(String query, String JPQL,
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
   * @param projectId the project id
   * @param authToken the auth token
   * @return the descriptor
   * @throws Exception the exception
   */
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, Long projectId, String authToken) throws Exception;

  /**
   * Returns the inverse relationship type.
   *
   * @param relationshipType the relationship type
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the inverse relationship type
   * @throws Exception the exception
   */
  public String getInverseRelationshipType(String relationshipType,
    String terminology, String version, String authToken) throws Exception;

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
  public SubsetMemberList getAtomSubsetMembers(String terminologyId,
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
  public SubsetMemberList getConceptSubsetMembers(String terminologyId,
    String terminology, String version, String authToken) throws Exception;

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
  public RelationshipList findDescriptorRelationships(String terminologyId,
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
  public RelationshipList findCodeRelationships(String terminologyId,
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
  public RelationshipList findConceptRelationships(String terminologyId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find relationships for component info.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param type the type
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findComponentInfoRelationships(String terminologyId,
    String terminology, String version, IdType type, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find descriptors for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptors(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find descriptors for general query.
   *
   * @param query the query
   * @param JPQL the JPQL
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForGeneralQuery(String query,
    String JPQL, PfsParameterJpa pfs, String authToken) throws Exception;

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
   * @param projectId the project id
   * @param authToken the auth token
   * @return the code
   * @throws Exception the exception
   */
  public Code getCode(String terminologyId, String terminology, String version,
    Long projectId, String authToken) throws Exception;

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
  public SearchResultList findCodes(String terminology, String version,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Find codes for general query.
   *
   * @param query the query
   * @param JPQL the JPQL
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForGeneralQuery(String query, String JPQL,
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
   * @param projectId the project id
   * @param authToken the auth token
   * @return the lexical class
   * @throws Exception the exception
   */
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, Long projectId, String authToken) throws Exception;

  /**
   * Gets the string class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param projectId the project id
   * @param authToken the auth token
   * @return the string class
   * @throws Exception the exception
   */
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, Long projectId, String authToken) throws Exception;

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
   * Load terminology simple.
   *
   * @param terminology the terminology
   * @param version the version
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologySimple(String terminology, String version,
    String inputDir, String authToken) throws Exception;

  /**
   * Export terminology simple.
   *
   * @param terminology the terminology
   * @param version the version
   * @param authToken the auth token
   * @return the input stream
   * @throws Exception the exception
   */
  public InputStream exportTerminologySimple(String terminology, String version,
    String authToken) throws Exception;

  /**
   * Load terminology rrf.
   *
   * @param terminology the terminology
   * @param version the version
   * @param style the style
   * @param prefix the prefix
   * @param inputDir the input dir
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadTerminologyRrf(String terminology, String version,
    String style, String prefix, String inputDir, String authToken)
    throws Exception;

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
   * Find atom trees.
   *
   * @param atomId the atom id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findAtomTrees(Long atomId, PfsParameterJpa pfs,
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
  public Tree findConceptTree(String terminology, String version, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

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
  public Tree findDescriptorTree(String terminology, String version,
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
  public Tree findCodeTree(String terminology, String version, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

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
   * Find atom tree children.
   *
   * @param atomId the atom id
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the tree list
   * @throws Exception the exception
   */
  public TreeList findAtomTreeChildren(Long atomId, PfsParameterJpa pfs,
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
  public MappingList findMappings(String mapSetId, String terminology,
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
  public MappingList findConceptMappings(String mapSetId, String terminology,
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
  public MappingList findCodeMappings(String terminologyId, String terminology,
    String version, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

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
  public MappingList findDescriptorMappings(String terminologyId,
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
   * @param id the id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addConceptNote(Long id, String noteText, String authToken)
    throws Exception;

  /**
   * Add code note.
   *
   * @param id the id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addCodeNote(Long id, String noteText, String authToken)
    throws Exception;

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
   * @param id the id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addDescriptorNote(Long id, String noteText, String authToken)
    throws Exception;

  /**
   * Remove descriptor note.
   *
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeDescriptorNote(Long noteId, String authToken)
    throws Exception;

  /**
   * Gets favorite components for a user.
   *
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the favorites for user
   * @throws Exception the exception
   */
  public SearchResultList getFavoritesForUser(PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Gets the components with notes for user.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the components with notes for user
   * @throws Exception the exception
   */
  public SearchResultList getComponentsWithNotes(String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Validates the specified concept. Checks are defined the "run.config.umls"
   * setting for the deployed server.
   *
   * @param projectId the project id
   * @param concept the concept
   * @param check the check name
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(Long projectId, ConceptJpa concept,
    String check, String authToken) throws Exception;

  /**
   * Validate concepts.
   *
   * @param projectId the project id
   * @param check the check
   * @param authToken the auth token
   * @return the sets the
   * @throws Exception the exception
   */
  public Set<Long> validateConcepts(Long projectId, String check,
    String authToken) throws Exception;

  /**
   * Validate atom.
   *
   * @param projectId the project id
   * @param atom the atom
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateAtom(Long projectId, AtomJpa atom,
    String authToken) throws Exception;

  /**
   * Validate descriptor.
   *
   * @param projectId the project id
   * @param descriptor the descriptor
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateDescriptor(Long projectId,
    DescriptorJpa descriptor, String authToken) throws Exception;

  /**
   * Validate code.
   *
   * @param projectId the project id
   * @param code the code
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateCode(Long projectId, CodeJpa code,
    String authToken) throws Exception;

  /**
   * Find concept deep relationships.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param inverseFlag the inverse flag
   * @param includeConceptRels the include concept rels
   * @param preferredOnly the preferred only
   * @param includeSelfReferential the include self referential
   * @param pfs the pfs
   * @param query the query
   * @param authToken the auth token
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findConceptDeepRelationships(String terminologyId,
    String terminology, String version, boolean inverseFlag,
    boolean includeConceptRels, boolean preferredOnly,
    boolean includeSelfReferential, PfsParameterJpa pfs, String query,
    String authToken) throws Exception;

  /**
   * Find concept deep tree positions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs
   * @param query the query
   * @param authToken the auth token
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findConceptDeepTreePositions(String terminologyId,
    String terminology, String version, PfsParameterJpa pfs, String query,
    String authToken) throws Exception;

  /**
   * Add atom note.
   *
   * @param id the id
   * @param noteText the note text
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void addAtomNote(Long id, String noteText, String authToken)
    throws Exception;

  /**
   * Remove atom note.
   *
   * @param noteId the note id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeAtomNote(Long noteId, String authToken) throws Exception;

  /**
   * Gets the atom.
   *
   * @param atomId the atom id
   * @param projectId the project id
   * @param authToken the auth token
   * @return the atom
   * @throws Exception the exception
   */
  public Atom getAtom(Long atomId, Long projectId, String authToken)
    throws Exception;

  /**
   * Gets the concepts for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the concepts for query
   * @throws Exception the exception
   */
  public ConceptList getConceptsForQuery(String terminology, String version,
    Long projectId, String query, PfsParameterJpa pfs, String authToken)
    throws Exception;

}
