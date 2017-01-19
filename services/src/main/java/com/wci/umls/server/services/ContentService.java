/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.NoteList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DefinitionList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.GeneralConceptAxiomList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentHasDefinitions;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.GeneralConceptAxiom;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.ExpressionHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;

/**
 * Represents a service for interacting with content.
 */
public interface ContentService extends MetadataService {

  /**
   * Gets the concept.
   *
   * @param id the id
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(Long id) throws Exception;

  /**
   * Gets the concepts.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the concepts
   * @throws Exception the exception
   */
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Gets the concept.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the concept
   * @throws Exception the exception
   */
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the subset.
   *
   * @param id the id
   * @param subsetClass the subset class
   * @return the subset
   * @throws Exception the exception
   */
  public Subset getSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception;

  /**
   * Gets the subset.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param subsetClass the subset class
   * @return the subset
   * @throws Exception the exception
   */
  public Subset getSubset(String terminologyId, String terminology,
    String version, String branch, Class<? extends Subset> subsetClass)
    throws Exception;

  /**
   * Gets the atom subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the atom subsets
   * @throws Exception the exception
   */
  public SubsetList getAtomSubsets(String terminology, String version,
    String branch) throws Exception;

  /**
   * Gets the concept subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the concept subsets
   * @throws Exception the exception
   */
  public SubsetList getConceptSubsets(String terminology, String version,
    String branch) throws Exception;

  /**
   * Find atom subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the subset member list
   * @throws Exception the exception
   */
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Find concept subset members.
   *
   * @param subsetId the subset id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the subset member list
   * @throws Exception the exception
   */
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Gets the subset members for atom.
   *
   * @param atomId the atom id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the subset members for atom
   */
  public SubsetMemberList getAtomSubsetMembers(String atomId,
    String terminology, String version, String branch);

  /**
   * Gets the subset members for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the subset members for concept
   */
  public SubsetMemberList getConceptSubsetMembers(String conceptId,
    String terminology, String version, String branch);

  /**
   * Find relationships for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findConceptRelationships(String conceptId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception;

  /**
   * Find deep relationships for concept by terminology id.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param filter the filter
   * @param inverseFlag the inverse flag
   * @param includeConceptRels the include concept rels
   * @param preferredOnly the preferred only
   * @param includeSelfReferential the include self referential
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findConceptDeepRelationships(String conceptId,
    String terminology, String version, String branch, String filter,
    boolean inverseFlag, boolean includeConceptRels, boolean preferredOnly,
    boolean includeSelfReferential, PfsParameter pfs) throws Exception;

  /**
   * Find relationships for descriptor.
   *
   * @param descriptorId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findDescriptorRelationships(String descriptorId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception;

  /**
   * Find relationships for code.
   *
   * @param codeId the code id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findCodeRelationships(String codeId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception;

  /**
   * Gets the descriptor.
   *
   * @param id the id
   * @return the descriptor
   * @throws Exception the exception
   */
  public Descriptor getDescriptor(Long id) throws Exception;

  /**
   * Gets the descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the descriptors
   * @throws Exception the exception
   */
  public DescriptorList getDescriptors(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Gets the descriptor.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the descriptor
   * @throws Exception the exception
   */
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the code.
   *
   * @param id the id
   * @return the code
   * @throws Exception the exception
   */
  public Code getCode(Long id) throws Exception;

  /**
   * Gets the codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the codes
   * @throws Exception the exception
   */
  public CodeList getCodes(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Gets the code.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the code
   * @throws Exception the exception
   */
  public Code getCode(String terminologyId, String terminology, String version,
    String branch) throws Exception;

  /**
   * Gets the lexical class.
   *
   * @param id the id
   * @return the lexical class
   * @throws Exception the exception
   */
  public LexicalClass getLexicalClass(Long id) throws Exception;

  /**
   * Gets the lexical classes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the lexical classes
   * @throws Exception the exception
   */
  public LexicalClassList getLexicalClasses(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Gets the lexical class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the lexical class
   * @throws Exception the exception
   */
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the string class.
   *
   * @param id the id
   * @return the string class
   * @throws Exception the exception
   */
  public StringClass getStringClass(Long id) throws Exception;

  /**
   * Gets the string classes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the string classes
   * @throws Exception the exception
   */
  public StringClassList getStringClasses(String terminologyId,
    String terminology, String version) throws Exception;

  /**
   * Gets the string class.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the string class
   * @throws Exception the exception
   */
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Add concept.
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
   * Remove concept.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeConcept(Long id) throws Exception;

  /**
   * Add descriptor.
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
   * Remove descriptor.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDescriptor(Long id) throws Exception;

  /**
   * Add code.
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
   * Remove code.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeCode(Long id) throws Exception;

  /**
   * Add lexical class.
   *
   * @param lexicalClass the lexical class
   * @return the lexical class
   * @throws Exception the exception
   */
  public LexicalClass addLexicalClass(LexicalClass lexicalClass)
    throws Exception;

  /**
   * Update lexical class.
   *
   * @param lexicalClass the lexical class
   * @throws Exception the exception
   */
  public void updateLexicalClass(LexicalClass lexicalClass) throws Exception;

  /**
   * Remove lexical class.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeLexicalClass(Long id) throws Exception;

  /**
   * Add string class.
   *
   * @param stringClass the string class
   * @return the string class
   * @throws Exception the exception
   */
  public StringClass addStringClass(StringClass stringClass) throws Exception;

  /**
   * Update string class.
   *
   * @param stringClass the string class
   * @throws Exception the exception
   */
  public void updateStringClass(StringClass stringClass) throws Exception;

  /**
   * Remove string class.
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
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
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
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find descendant atoms.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the atom list
   * @throws Exception the exception
   */
  public AtomList findDescendantAtoms(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception;

  /**
   * Find ancestor atoms.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @return the atom list
   * @throws Exception the exception
   */
  public AtomList findAncestorAtoms(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception;

  /**
   * Find tree positions for the specified type.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  public TreePositionList findTreePositions(String terminologyId,
    String terminology, String version, String branch, String query,
    Class<? extends TreePosition> clazz, PfsParameter pfs) throws Exception;

  /**
   * Find descendant descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the descriptor list
   * @throws Exception the exception
   */
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find ancestor descriptors.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @return the descriptor list
   * @throws Exception the exception
   */
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception;

  /**
   * Find descendant codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @return the code list
   * @throws Exception the exception
   */
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception;

  /**
   * Find ancestor codes.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @return the code list
   * @throws Exception the exception
   */
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception;

  /**
   * Gets the atom.
   *
   * @param id the id
   * @return the atom
   * @throws Exception the exception
   */
  public Atom getAtom(Long id) throws Exception;

  /**
   * Gets the atoms.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the atoms
   * @throws Exception the exception
   */
  public AtomList getAtoms(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Gets the atom.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the atom
   * @throws Exception the exception
   */
  public Atom getAtom(String terminologyId, String terminology, String version,
    String branch) throws Exception;

  /**
   * Add atom.
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
   * Remove atom.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAtom(Long id) throws Exception;

  /**
   * Move atoms.
   *
   * @param toConcept the to concept
   * @param fromConcept the from concept
   * @param fromAtoms the from atoms
   * @throws Exception the exception
   */
  public void moveAtoms(Concept toConcept, Concept fromConcept,
    List<Atom> fromAtoms) throws Exception;

  /**
   * Add relationship.
   *
   * @param relationship the relationship
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> addRelationship(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception;

  /**
   * Update relationship.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  public void updateRelationship(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception;

  /**
   * Remove relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @throws Exception the exception
   */
  public void removeRelationship(Long id,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception;

  /**
   * Get transitive relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @return the transitive relationship
   * @throws Exception the exception
   */
  public TransitiveRelationship<? extends AtomClass> getTransitiveRelationship(
    Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception;

  /**
   * Add transitive relationship.
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
   * Remove transitive relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @throws Exception the exception
   */
  public void removeTransitiveRelationship(Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception;

  /**
   * Get tree position.
   *
   * @param id the id
   * @param treeposClass the treepos class
   * @return the tree position
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  public TreePosition<?> getTreePosition(Long id,
    Class<? extends TreePosition> treeposClass) throws Exception;

  /**
   * Add tree position.
   *
   * @param treepos the treepos
   * @return the tree position
   * @throws Exception the exception
   */
  public TreePosition<?> addTreePosition(TreePosition<?> treepos)
    throws Exception;

  /**
   * Update tree position.
   *
   * @param treepos the treepos
   * @throws Exception the exception
   */
  public void updateTreePosition(TreePosition<?> treepos) throws Exception;

  /**
   * Remove tree position.
   *
   * @param id the id
   * @param treeposClass the treepos class
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  public void removeTreePosition(Long id,
    Class<? extends TreePosition> treeposClass) throws Exception;

  /**
   * Add subset.
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
   * Remove subset.
   *
   * @param id the id
   * @param subsetClass the subset class
   * @throws Exception the exception
   */
  public void removeSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception;

  /**
   * Add subset member.
   *
   * @param member the member
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> addSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member)
    throws Exception;

  /**
   * Update subset member.
   *
   * @param member the member
   * @throws Exception the exception
   */
  public void updateSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member)
    throws Exception;

  /**
   * Remove subset member.
   *
   * @param id the id
   * @param memberClass the member class
   * @throws Exception the exception
   */
  public void removeSubsetMember(Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception;

  /**
   * Find concept search results for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptSearchResults(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find concepts.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList findConcepts(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception;

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
   * Find descriptor search results for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorSearchResults(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the descriptor list
   * @throws Exception the exception
   */
  public DescriptorList findDescriptors(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception;

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
   * Find code search results for query.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodeSearchResults(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the code list
   * @throws Exception the exception
   */
  public CodeList findCodes(String terminology, String version, String branch,
    String query, PfsParameter pfs) throws Exception;

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
   * @param version the version
   * @param branch the branch
   * @return the all concepts
   */
  public ConceptList getAllConcepts(String terminology, String version,
    String branch);

  /**
   * Returns the all concept ids.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all concept ids
   * @throws Exception the exception
   */
  public List<Long> getAllConceptIds(String terminology, String version,
    String branch) throws Exception;

  /**
   * Gets the all descriptors.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all descriptors
   */
  public DescriptorList getAllDescriptors(String terminology, String version,
    String branch);

  /**
   * Returns the all descriptor ids.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all descriptor ids
   * @throws Exception the exception
   */
  public List<Long> getAllDescriptorIds(String terminology, String version,
    String branch) throws Exception;

  /**
   * Gets the all codes.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all codes
   */
  public CodeList getAllCodes(String terminology, String version,
    String branch);

  /**
   * Returns the all code ids.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all code ids
   * @throws Exception the exception
   */
  public List<Long> getAllCodeIds(String terminology, String version,
    String branch) throws Exception;

  /**
   * Gets the all subsets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the all subsets
   * @throws Exception the exception
   */
  public SubsetList getAllSubsets(String terminology, String version,
    String branch) throws Exception;

  /**
   * Clear transitive closure.
   *
   * @param terminology the terminology
   * @param version the version
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
   * Clear branch.
   *
   * @param branch the branch
   */
  public void clearBranch(String branch);

  /**
   * Gets the identifier assignment handler.
   *
   * @param terminology the terminology
   * @return the identifier assignment handler
   * @throws Exception the exception
   */
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception;

  /**
   * Gets the compute preferred name handler.
   *
   * @param terminology the terminology
   * @return the compute preferred name handler
   * @throws Exception the exception
   */
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception;

  /**
   * Gets the computed preferred name.
   *
   * @param atomClass the atom class
   * @param list the list
   * @return the computed preferred name
   * @throws Exception the exception
   */
  public String getComputedPreferredName(AtomClass atomClass,
    PrecedenceList list) throws Exception;

  /**
   * Gets the normalized string.
   *
   * @param string the string
   * @return the normalized string
   * @throws Exception the exception
   */
  public String getNormalizedString(String string) throws Exception;

  /**
   * Sets the assign identifiers flag.
   *
   * @param assignIdentifiersFlag the new assign identifiers flag
   */
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag);

  /**
   * Gets the component stats.
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
   * Remove definition.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeDefinition(Long id) throws Exception;

  /**
   * Update definition.
   *
   * @param definition the definition
   * @param component the component
   * @throws Exception the exception
   */
  public void updateDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception;

  /**
   * Add definition.
   *
   * @param definition the definition
   * @param component the component
   * @return the definition
   * @throws Exception the exception
   */
  public Definition addDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception;

  /**
   * Returns the semantic type component.
   *
   * @param id the id
   * @return the semantic type component
   * @throws Exception the exception
   */
  public SemanticTypeComponent getSemanticTypeComponent(Long id)
    throws Exception;

  /**
   * Remove semantic type component.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSemanticTypeComponent(Long id) throws Exception;

  /**
   * Update semantic type component.
   *
   * @param sty the sty
   * @param concept the concept
   * @throws Exception the exception
   */
  public void updateSemanticTypeComponent(SemanticTypeComponent sty,
    Concept concept) throws Exception;

  /**
   * Add semantic type component.
   *
   * @param sty the sty
   * @param concept the concept
   * @return the semantic type component
   * @throws Exception the exception
   */
  public SemanticTypeComponent addSemanticTypeComponent(
    SemanticTypeComponent sty, Concept concept) throws Exception;

  /**
   * Remove attribute.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeAttribute(Long id) throws Exception;

  /**
   * Update attribute.
   *
   * @param attribute the attribute
   * @param component the component
   * @throws Exception the exception
   */
  public void updateAttribute(Attribute attribute, ComponentInfo component)
    throws Exception;

  /**
   * Add attribute.
   *
   * @param attribute the attribute
   * @param component the component
   * @return the attribute
   * @throws Exception the exception
   */
  public Attribute addAttribute(Attribute attribute, ComponentInfo component)
    throws Exception;

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
   * Gets the definition.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the definition
   * @throws Exception the exception
   */
  public Definition getDefinition(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the definitions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @return the definitions
   * @throws Exception the exception
   */
  public DefinitionList getDefinitions(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Gets the definition.
   *
   * @param id the id
   * @return the definition
   * @throws Exception the exception
   */
  public Definition getDefinition(Long id) throws Exception;

  /**
   * Get relationship.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param relationshipClass the relationship class
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> getRelationship(
    String terminologyId, String terminology, String version, String branch,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception;

  /**
   * Gets the relationships.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param relationshipClass the relationship class
   * @return the relationships
   * @throws Exception the exception
   */
  public RelationshipList getRelationships(String terminologyId,
    String terminology, String version,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception;

  /**
   * Get relationship.
   *
   * @param id the id
   * @param relationshipClass the relationship class
   * @return the relationship
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> getRelationship(
    Long id,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception;

  /**
   * Returns the inverse relationships.
   *
   * @param terminology the terminology
   * @param version the version
   * @param relationship the relationship
   * @return the inverse relationships
   * @throws Exception the exception
   */
  public RelationshipList getInverseRelationships(String terminology,
    String version,
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception;

  /**
   * Find inverse relationship.
   *
   * @param terminology the terminology
   * @param version the version
   * @param relationship the relationship
   * @return the relationship<? extends component info,? extends component info>
   * @throws Exception the exception
   */
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> getInverseRelationship(
    String terminology, String version,
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception;

  /**
   * Creates the inverse concept relationship.
   *
   * @param relationship the relationship
   * @return the concept relationship
   * @throws Exception the exception
   */
  public ConceptRelationship createInverseConceptRelationship(
    ConceptRelationship relationship) throws Exception;

  /**
   * Get subset member.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param memberClass the member class
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    String terminologyId, String terminology, String version, String branch,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception;

  /**
   * Gets the subset members.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param memberClass the member class
   * @return the subset members
   * @throws Exception the exception
   */
  public SubsetMemberList getSubsetMembers(String terminologyId,
    String terminology, String version,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception;

  /**
   * Get subset member.
   *
   * @param id the id
   * @param memberClass the member class
   * @return the subset member
   * @throws Exception the exception
   */
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception;

  /**
   * Find codes for general query.
   *
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findCodesForGeneralQuery(String luceneQuery,
    String jqlQuery, String branch, PfsParameter pfs) throws Exception;

  /**
   * Find concepts for general query.
   *
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForGeneralQuery(String luceneQuery,
    String jqlQuery, String branch, PfsParameter pfs) throws Exception;

  /**
   * Find descriptors for general query.
   *
   * @param luceneQuery the lucene query
   * @param jqlQuery the jql query
   * @param branch the branch
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findDescriptorsForGeneralQuery(String luceneQuery,
    String jqlQuery, String branch, PfsParameter pfs) throws Exception;

  /**
   * Gets the tree for tree position.
   *
   * @param treePosition the tree position
   * @return the tree for tree position
   * @throws Exception the exception
   */
  public Tree getTreeForTreePosition(TreePosition<?> treePosition)
    throws Exception;

  /**
   * Find concept tree position children.
   *
   * @param nodeId the node id
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param clazz the clazz
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  public TreePositionList findTreePositionChildren(Long nodeId,
    String terminologyId, String terminology, String version, String branch,
    Class<? extends TreePosition> clazz, PfsParameter pfs) throws Exception;

  /**
   * Add general concept axiom.
   *
   * @param axiom the axiom
   * @return the general concept axiom
   * @throws Exception the exception
   */
  public GeneralConceptAxiom addGeneralConceptAxiom(GeneralConceptAxiom axiom)
    throws Exception;

  /**
   * Update general concept axiom.
   *
   * @param axiom the axiom
   * @throws Exception the exception
   */
  public void updateGeneralConceptAxiom(GeneralConceptAxiom axiom)
    throws Exception;

  /**
   * Remove general concept axiom.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeGeneralConceptAxiom(Long id) throws Exception;

  /**
   * Gets the general concept axioms.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the general concept axioms
   * @throws Exception the exception
   */
  public GeneralConceptAxiomList getGeneralConceptAxioms(String terminology,
    String version, String branch) throws Exception;

  /**
   * Add mapping.
   *
   * @param mapping the mapping
   * @return the mapping
   * @throws Exception the exception
   */
  public Mapping addMapping(Mapping mapping) throws Exception;

  /**
   * Update mapping.
   *
   * @param mapping the mapping
   * @throws Exception the exception
   */
  public void updateMapping(Mapping mapping) throws Exception;

  /**
   * Remove mapping.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMapping(Long id) throws Exception;

  /**
   * Gets the mapping.
   *
   * @param id the id
   * @return the mapping
   * @throws Exception the exception
   */
  public Mapping getMapping(Long id) throws Exception;

  /**
   * Gets the mapping.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the mapping
   * @throws Exception the exception
   */
  public Mapping getMapping(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Find mappings for map set.
   *
   * @param mapSetId the map set id
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findMappings(Long mapSetId, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Add map set.
   *
   * @param mapSet the map set
   * @return the map set
   * @throws Exception the exception
   */
  public MapSet addMapSet(MapSet mapSet) throws Exception;

  /**
   * Update map set.
   *
   * @param mapSet the map set
   * @throws Exception the exception
   */
  public void updateMapSet(MapSet mapSet) throws Exception;

  /**
   * Remove map set.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeMapSet(Long id) throws Exception;

  /**
   * Gets the map set.
   *
   * @param id the id
   * @return the map set
   * @throws Exception the exception
   */
  public MapSet getMapSet(Long id) throws Exception;

  /**
   * Gets the map set.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the map set
   * @throws Exception the exception
   */
  public MapSet getMapSet(String terminologyId, String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the map sets.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the map sets
   * @throws Exception the exception
   */
  public MapSetList getMapSets(String terminology, String version,
    String branch) throws Exception;

  /**
   * Find mappings for concept.
   *
   * @param conceptId the concept id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findConceptMappings(String conceptId, String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find mappings for code.
   *
   * @param codeId the code id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findCodeMappings(String codeId, String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find mappings for descriptor.
   *
   * @param descriptorId the descriptor id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  public MappingList findDescriptorMappings(String descriptorId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Gets the terminology id map.
   *
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @return the terminology id map
   * @throws Exception the exception
   */
  public Map<Long, String> getTerminologyIdMap(String terminology,
    String version, String branch) throws Exception;

  /**
   * Gets the expression handler.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the expression handler
   * @throws Exception the exception
   */
  public ExpressionHandler getExpressionHandler(String terminology,
    String version) throws Exception;

  /**
   * Add user note.
   *
   * @param userNote the user note
   * @return the note
   * @throws Exception the exception
   */
  public Note addNote(Note userNote) throws Exception;

  /**
   * Remove user note.
   *
   * @param id the id
   * @param type the type
   * @throws Exception the exception
   */
  public void removeNote(Long id, Class<? extends Note> type) throws Exception;

  /**
   * Gets the note.
   *
   * @param id the id
   * @param noteClass the note class
   * @return the note
   * @throws Exception the exception
   */
  public Note getNote(Long id, Class<? extends Note> noteClass)
    throws Exception;

  /**
   * Find code notes for user.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the note list
   * @throws Exception the exception
   */
  public NoteList findCodeNotes(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find descriptor notes for user.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the note list
   * @throws Exception the exception
   */
  public NoteList findDescriptorNotes(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find concept notes for user.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the note list
   * @throws Exception the exception
   */
  public NoteList findConceptNotes(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Find relationships for component info.
   *
   * @param componentInfoId the component info id
   * @param terminology the terminology
   * @param version the version
   * @param type the type
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @return the relationship list
   * @throws Exception the exception
   */
  public RelationshipList findComponentInfoRelationships(String componentInfoId,
    String terminology, String version, IdType type, String branch,
    String query, boolean inverseFlag, PfsParameter pfs) throws Exception;

  /**
   * Validate concept.
   *
   * @param validationChecks the validation checks
   * @param concept the concept
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(List<String> validationChecks,
    Concept concept) throws Exception;

  /**
   * Validate concepts.
   *
   * @param project the project
   * @param conceptIds the concept ids
   * @return the validation result
   * @throws Exception the exception
   */
  public Set<Long> validateConcepts(Project project, Set<Long> conceptIds)
    throws Exception;

  /**
   * Validate atom.
   *
   * @param validationChecks the validation checks
   * @param atom the atom
   * @return the validation result
   */
  public ValidationResult validateAtom(List<String> validationChecks,
    Atom atom);

  /**
   * Validate descriptor.
   *
   * @param validationChecks the validation checks
   * @param descriptor the descriptor
   * @return the validation result
   */
  public ValidationResult validateDescriptor(List<String> validationChecks,
    Descriptor descriptor);

  /**
   * Validate code.
   *
   * @param validationChecks the validation checks
   * @param code the code
   * @return the validation result
   */
  public ValidationResult validateCode(List<String> validationChecks,
    Code code);

  /**
   * Returns the ambiguous atom ids.
   *
   * @param concept the concept
   * @return the ambiguous atom ids
   */
  public List<Long> getAmbiguousAtomIds(Concept concept);

  /**
   * New identifier assignment handler.
   *
   * @param terminology the terminology
   * @return the identifier assignment handler
   * @throws Exception the exception
   */
  IdentifierAssignmentHandler newIdentifierAssignmentHandler(String terminology)
    throws Exception;

  /**
   * Find concept deep tree positions.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the tree position list
   * @throws Exception the exception
   */
  public TreePositionList findConceptDeepTreePositions(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception;

  /**
   * Gets the normalized string handler.
   *
   * @return the normalized string handler
   * @throws Exception the exception
   */
  public NormalizedStringHandler getNormalizedStringHandler() throws Exception;
}