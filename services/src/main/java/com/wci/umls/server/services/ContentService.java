/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import java.util.Map;

import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Generically represents a service for accessing content.
 */
public interface ContentService extends RootService {

  /**
   * Enable listeners.
   */
  public void enableListeners();

  /**
   * Disable listeners.
   */
  public void disableListeners();

  /**
   * Returns the concept.
   * 
   * @param id the id
   * @return the concept
   * @throws Exception if anything goes wrong
   */
  public Concept getConcept(Long id) throws Exception;

  /**
   * Returns the concept matching the specified parameters. May return more than
   * one concept if there are multiple entries with the same id, terminology,
   * and version. This view is needed for conflict resolution.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the concept
   * @throws Exception if anything goes wrong
   */
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the single concept for the specified parameters. If there are more
   * than one it throws an exception.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the terminology version
   * @param branch the branch to lookup
   * @return the single concept
   * @throws Exception if there are more than one matching concepts.
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the subset.
   *
   * @param id the id
   * @return the subset
   * @throws Exception the exception
   */
  public Subset getSubset(Long id) throws Exception;

  /**
   * Returns the subset.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the subset
   * @throws Exception the exception
   */
  public Subset getSubset(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the atom subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the subsets
   * @throws Exception the exception
   */
  public SubsetList getAtomSubsets(String terminology, String version)
    throws Exception;

  /**
   * Returns the concept subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the concept subsets
   * @throws Exception the exception
   */
  public SubsetList getConceptSubsets(String terminology, String version)
    throws Exception;

  /**
   * Returns the atom subset members for the specified subset.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the subset members
   */
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String branch, PfsParameter pfs);

  /**
   * Returns the concept subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the concept subset members
   */
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String branch, PfsParameter pfs);

  /**
   * Returns the atom subset members for the specified atom.
   *
   * @param atomId the atom id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the atom subset members
   */
  public SubsetMemberList getSubsetMembersForAtom(String atomId,
    String terminology, String version, String branch);

  /**
   * Returns the concept subset members for the specified concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the concept subset members
   */
  public SubsetMemberList getSubsetMembersForConcept(String conceptId,
    String terminology, String version, String branch);

  /**
   * Gets the relationships for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the relationships for concept
   */
  public RelationshipList findRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, PfsParameter pfs);

  /**
   * Returns the relationships for descriptor.
   *
   * @param descriptorId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the relationships for descriptor
   */
  public RelationshipList findRelationshipsForDescriptor(String descriptorId,
    String terminology, String version, String branch, PfsParameter pfs);

  /**
   * Returns the relationships for code.
   *
   * @param codeId the code id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the relationships for code
   */
  public RelationshipList findRelationshipsForCode(String codeId,
    String terminology, String version, String branch, PfsParameter pfs);

  /**
   * Returns the relationships for atom.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param pfs the pfs
   * @return the relationships for atom
   */
  public RelationshipList findRelationshipsForAtom(String conceptId,
    String terminology, String version, String branch, PfsParameter pfs);

  /**
   * Returns the descriptor.
   * 
   * @param id the id
   * @return the descriptor
   * @throws Exception if anything goes wrong
   */
  public Descriptor getDescriptor(Long id) throws Exception;

  /**
   * Returns the descriptor matching the specified parameters. May return more
   * than one descriptor if there are multiple entries with the same id,
   * terminology, and version. This view is needed for conflict resolution.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the descriptor
   * @throws Exception if anything goes wrong
   */
  public DescriptorList getDescriptors(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Returns the single descriptor for the specified parameters. If there are
   * more than one it throws an exception.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the terminology version
   * @param branch the branch to lookup
   * @return the single descriptor
   * @throws Exception if there are more than one matching descriptors.
   */
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the code.
   * 
   * @param id the id
   * @return the code
   * @throws Exception if anything goes wrong
   */
  public Code getCode(Long id) throws Exception;

  /**
   * Returns the code matching the specified parameters. May return more than
   * one code if there are multiple entries with the same id, terminology, and
   * version. This view is needed for conflict resolution.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the code
   * @throws Exception if anything goes wrong
   */
  public CodeList getCodes(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the single code for the specified parameters. If there are more
   * than one it throws an exception.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the terminology version
   * @param branch the branch to lookup
   * @return the single code
   * @throws Exception if there are more than one matching codes.
   */
  public Code getCode(String terminologyId, String terminology, String version,
    String branch) throws Exception;

  /**
   * Returns the lexical class.
   * 
   * @param id the id
   * @return the lexical class
   * @throws Exception if anything goes wrong
   */
  public LexicalClass getLexicalClass(Long id) throws Exception;

  /**
   * Returns the lexical class matching the specified parameters. May return
   * more than one lexical class if there are multiple entries with the same id,
   * terminology, and version. This view is needed for conflict resolution.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the lexical class
   * @throws Exception if anything goes wrong
   */
  public LexicalClassList getLexicalClasses(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Returns the single lexical class for the specified parameters. If there are
   * more than one it throws an exception.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the terminology version
   * @param branch the branch to lookup
   * @return the lexical class
   * @throws Exception if there are more than one matching lexicalClasss.
   */
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Returns the string class.
   * 
   * @param id the id
   * @return the string class
   * @throws Exception if anything goes wrong
   */
  public StringClass getStringClass(Long id) throws Exception;

  /**
   * Returns the string class matching the specified parameters. May return more
   * than one string class if there are multiple entries with the same id,
   * terminology, and version. This view is needed for conflict resolution.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the string class
   * @throws Exception if anything goes wrong
   */
  public StringClassList getStringClasses(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Returns the single string class for the specified parameters. If there are
   * more than one it throws an exception.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the terminology version
   * @param branch the branch to lookup
   * @return the string class
   * @throws Exception if there are more than one matching stringClasss.
   */
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Adds the concept.
   * 
   * @param concept the concept
   * @return the concept
   * @throws Exception the exception
   */
  public Concept addConcept(Concept concept) throws Exception;

  /**
   * Update concept.
   * 
   * @param concept the concept
   * @throws Exception the exception
   */
  public void updateConcept(Concept concept) throws Exception;

  /**
   * Removes the concept.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeConcept(Long id) throws Exception;

  /**
   * Adds the descriptor.
   * 
   * @param descriptor the descriptor
   * @return the descriptor
   * @throws Exception the exception
   */
  public Descriptor addDescriptor(Descriptor descriptor) throws Exception;

  /**
   * Update descriptor.
   * 
   * @param descriptor the descriptor
   * @throws Exception the exception
   */
  public void updateDescriptor(Descriptor descriptor) throws Exception;

  /**
   * Removes the descriptor.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDescriptor(Long id) throws Exception;

  /**
   * Adds the code.
   * 
   * @param code the code
   * @return the code
   * @throws Exception the exception
   */
  public Code addCode(Code code) throws Exception;

  /**
   * Update code.
   * 
   * @param code the code
   * @throws Exception the exception
   */
  public void updateCode(Code code) throws Exception;

  /**
   * Removes the code.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeCode(Long id) throws Exception;

  /**
   * Adds the lexical class.
   * 
   * @param lexicalClass the lexicalClass
   * @return the lexicalClass
   * @throws Exception the exception
   */
  public LexicalClass addLexicalClass(LexicalClass lexicalClass)
    throws Exception;

  /**
   * Update lexical class.
   * 
   * @param lexicalClass the lexicalClass
   * @throws Exception the exception
   */
  public void updateLexicalClass(LexicalClass lexicalClass) throws Exception;

  /**
   * Removes the lexical class.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLexicalClass(Long id) throws Exception;

  /**
   * Adds the string class.
   * 
   * @param stringClass the stringClass
   * @return the stringClass
   * @throws Exception the exception
   */
  public StringClass addStringClass(StringClass stringClass) throws Exception;

  /**
   * Update string class.
   * 
   * @param stringClass the stringClass
   * @throws Exception the exception
   */
  public void updateStringClass(StringClass stringClass) throws Exception;

  /**
   * Removes the string class.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeStringClass(Long id) throws Exception;

  /**
   * Find descendant concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the parents only flag
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find ancestor concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the children only flag
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find concept tree positions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs parameter
   * @param branch the branch
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findTreePositionsForConcept(String terminologyId,
    String terminology, String version, PfsParameter pfs, String branch)
    throws Exception;

  /**
   * Find tree positions for descriptor.
   *
   * @param descriptorId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs parameter
   * @param branch the branch
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findTreePositionsForDescriptor(String descriptorId,
    String terminology, String version, PfsParameter pfs, String branch)
    throws Exception;

  /**
   * Find tree positions for code.
   *
   * @param codeId the code id
   * @param terminology the terminology
   * @param version the version
   * @param pfs the pfs parameter
   * @param branch the branch
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findTreePositionsForCode(String codeId,
    String terminology, String version, PfsParameter pfs, String branch)
    throws Exception;

  /**
   * Find descendant descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the parents only
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the descriptor list
   * @throws Exception the exception
   */
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find ancestor concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the children only
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the descriptor list
   * @throws Exception the exception
   */
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find descriptor tree positions.
   *
   * @param descriptor the descriptor
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findDescriptorTreePositions(Descriptor descriptor,
    String branch, PfsParameter pfs) throws Exception;

  /**
   * Find descendant descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the parents only
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the code list
   * @throws Exception the exception
   */
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception;

  /**
   * Find ancestor concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the children only
   * @param branch the branch
   * @param pfs the pfs parameter
   * @return the code list
   * @throws Exception the exception
   */
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception;

  /**
   * Find code tree positions.
   *
   * @param code the code
   * @param pfs the pfs parameter
   * @param branch the branch
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findCodeTreePositions(Code code, String branch, PfsParameter pfs
    ) throws Exception;

  /**
   * Returns the atom.
   * 
   * @param id the id
   * @return the atom
   * @throws Exception if anything goes wrong
   */
  public Atom getAtom(Long id) throws Exception;

  /**
   * Returns the atoms.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the atom
   * @throws Exception the exception
   */
  public AtomList getAtoms(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the atom matching the specified parameters.
   *
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the atom
   * @throws Exception if anything goes wrong
   */
  public Atom getAtom(String terminologyId, String terminology, String version,
    String branch) throws Exception;

  /**
   * Adds the atom.
   * 
   * @param atom the atom
   * @return the atom
   * @throws Exception the exception
   */
  public Atom addAtom(Atom atom) throws Exception;

  /**
   * Update atom.
   * 
   * @param atom the atom
   * @throws Exception the exception
   */
  public void updateAtom(Atom atom) throws Exception;

  /**
   * Removes the atom.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAtom(Long id) throws Exception;

  /**
   * Adds the relationship.
   * 
   * @param relationship the relationship
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> addRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception;

  /**
   * Update relationship.
   * 
   * @param relationship the relationship
   * @throws Exception the exception
   */
  public void updateRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception;

  /**
   * Removes the relationship.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeRelationship(Long id) throws Exception;

  /**
   * Adds the transitive relationship.
   * 
   * @param transitiveRelationship the transitive relationship
   * @return the transitive relationship
   * @throws Exception the exception
   */
  public TransitiveRelationship<? extends ComponentHasAttributes> addTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> transitiveRelationship)
    throws Exception;

  /**
   * Update transitive relationship.
   * 
   * @param transitiveRelationship the transitive relationship
   * @throws Exception the exception
   */
  public void updateTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> transitiveRelationship)
    throws Exception;

  /**
   * Removes the transitive relationship.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeTransitiveRelationship(Long id) throws Exception;

  /**
   * Adds the subset.
   * 
   * @param subset the subset
   * @return the subset
   * @throws Exception the exception
   */
  public Subset addSubset(Subset subset) throws Exception;

  /**
   * Update subset.
   * 
   * @param subset the subset
   * @throws Exception the exception
   */
  public void updateSubset(Subset subset) throws Exception;

  /**
   * Removes the subset.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSubset(Long id) throws Exception;

  /**
   * Adds the subset member.
   *
   * @param member the member
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMember<? extends ComponentHasAttributesAndName> addSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName> member)
    throws Exception;

  /**
   * Update subset member.
   *
   * @param member the member
   * @throws Exception the exception
   */
  public void updateSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName> member)
    throws Exception;

  /**
   * Removes the subset member.
   * 
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSubsetMember(Long id) throws Exception;

  /**
   * Returns the concept search results matching the query. Results can be
   * paged, filtered, and sorted.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the search string
   * @param pfs the paging, filtering, sorting parameter
   * @return the search results for the search string
   * @throws Exception if anything goes wrong
   */
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Autocomplete concepts.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm) throws Exception;

  /**
   * Find descriptors for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Autocomplete descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm) throws Exception;

  /**
   * Find codes for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForQuery(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception;

  /**
   * Autocomplete codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @return the string list
   * @throws Exception the exception
   */
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm) throws Exception;

  /**
   * Gets the all concepts.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @param branch the branch
   * @return the all concepts
   */
  public ConceptList getAllConcepts(String terminology, String version,
    String branch);

  /**
   * Returns the all descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all descriptors
   */
  public DescriptorList getAllDescriptors(String terminology, String version,
    String branch);

  /**
   * Returns the all codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all codes
   */
  public CodeList getAllCodes(String terminology, String version, String branch);

  /**
   * Returns the all subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all subsets
   */
  public SubsetList getAllSubsets(String terminology, String version,
    String branch);

  /**
   * Clear transitive closure.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @throws Exception the exception
   */
  public void clearTransitiveClosure(String terminology, String version)
    throws Exception;

  /**
   * Clear tree positions.
   *
   * @param terminology the terminology
   * @param version the version
   * @throws Exception the exception
   */
  public void clearTreePositions(String terminology, String version)
    throws Exception;

  /**
   * Removes all concepts and connected data structures.
   *
   * @param terminology the terminology
   * @param version the terminology version
   */
  public void clearContent(String terminology, String version);

  /**
   * Clear all content in the (non null) branch.
   *
   * @param branch the branch
   */
  public void clearBranch(String branch);

  /**
   * Returns the graph resolution handler. This is configured internally but
   * made available through this service.
   *
   * @param terminology the terminology
   * @return the graph resolution handler
   * @throws Exception the exception
   */
  public GraphResolutionHandler getGraphResolutionHandler(String terminology)
    throws Exception;

  /**
   * Returns the identifier assignment handler.
   *
   * @param terminology the terminology
   * @return the identifier assignment handler
   * @throws Exception the exception
   */
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception;

  /**
   * Returns the compute preferred name handler.
   *
   * @param terminology the terminology
   * @return the compute preferred name handler
   * @throws Exception the exception
   */
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception;

  /**
   * Returns the computed preferred name.
   *
   * @param atomClass the atom class
   * @return the computed preferred name
   * @throws Exception the exception
   */
  public String getComputedPreferredName(AtomClass atomClass) throws Exception;

  /**
   * Returns the normalized string.
   *
   * @param string the string
   * @return the normalized string
   * @throws Exception the exception
   */
  public String getNormalizedString(String string) throws Exception;

  /**
   * Sets the assign identifiers flag.
   *
   * @param assignIdentifiersFlag the assign identifiers flag
   */
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag);

  /**
   * Returns the component stats.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the component stats
   * @throws Exception the exception
   */
  public Map<String, Integer> getComponentStats(String terminology,
    String version, String branch) throws Exception;

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
   * Removes the definition.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDefinition(Long id) throws Exception;

  /**
   * Update definition.
   *
   * @param definition the definition
   * @throws Exception the exception
   */
  public void updateDefinition(Definition definition) throws Exception;

  /**
   * Adds the definition.
   *
   * @param definition the definition
   * @return the definition
   * @throws Exception the exception
   */
  public Definition addDefinition(Definition definition) throws Exception;

  /**
   * Removes the semantic type component.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSemanticTypeComponent(Long id) throws Exception;

  /**
   * Update semantic type component.
   *
   * @param sty the sty
   * @throws Exception the exception
   */
  public void updateSemanticTypeComponent(SemanticTypeComponent sty)
    throws Exception;

  /**
   * Adds the semantic type component.
   *
   * @param sty the sty
   * @return the semantic type component
   * @throws Exception the exception
   */
  public SemanticTypeComponent addSemanticTypeComponent(
    SemanticTypeComponent sty) throws Exception;

  /**
   * Removes the attribute.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAttribute(Long id) throws Exception;

  /**
   * Update attribute.
   *
   * @param attribute the attribute
   * @throws Exception the exception
   */
  public void updateAttribute(Attribute attribute) throws Exception;

  /**
   * Adds the attribute.
   *
   * @param attribute the attribute
   * @return the attribute
   * @throws Exception the exception
   */
  public Attribute addAttribute(Attribute attribute) throws Exception;

  /**
   * Gets the attribute.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the attribute
   * @throws Exception the exception
   */
  public Attribute getAttribute(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the attributes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the attributes
   * @throws Exception the exception
   */
  public AttributeList getAttributes(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Gets the attribute.
   *
   * @param id the id
   * @return the attribute
   * @throws Exception the exception
   */
  public Attribute getAttribute(Long id) throws Exception;

  /**
   * Gets the relationship.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;

  /**
   * Gets the relationships.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the relationships
   * @throws Exception the exception
   */
  public RelationshipList getRelationships(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Gets the relationship.
   *
   * @param id the id
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    Long id) throws Exception;

  /**
   * Gets the subset member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMember<? extends ComponentHasAttributesAndName> getSubsetMember(
    String terminologyId, String terminology, String version, String branch)
    throws Exception;

  /**
   * Gets the subset member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembers(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Gets the subset members.
   *
   * @param id the id
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMember<? extends ComponentHasAttributesAndName> getSubsetMember(
    Long id) throws Exception;

}