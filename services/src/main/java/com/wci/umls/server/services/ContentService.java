/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import java.util.Map;

import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchCriteriaList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.StringClass;
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
   * and version. NOTE: this only applies to concept, not to other data types.
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
   * Returns the descriptor.
   * 
   * @param id the id
   * @return the descriptor
   * @throws Exception if anything goes wrong
   */
  public Descriptor getDescriptor(Long id) throws Exception;

  /**
   * Returns the descriptor matching the specified parameters. May return more than
   * one descriptor if there are multiple entries with the same id, terminology,
   * and version. NOTE: this only applies to descriptor, not to other data types.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the descriptor
   * @throws Exception if anything goes wrong
   */
  public DescriptorList getDescriptors(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the single descriptor for the specified parameters. If there are more
   * than one it throws an exception.
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
   * one code if there are multiple entries with the same id, terminology,
   * and version. NOTE: this only applies to code, not to other data types.
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
  public Code getCode(String terminologyId, String terminology,
    String version, String branch) throws Exception;


  /**
   * Returns the lexical class.
   * 
   * @param id the id
   * @return the lexical class
   * @throws Exception if anything goes wrong
   */
  public LexicalClass getLexicalClass(Long id) throws Exception;

  /**
   * Returns the lexical class matching the specified parameters. May return more than
   * one lexical class if there are multiple entries with the same id, terminology,
   * and version. NOTE: this only applies to lexical class, not to other data types.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the lexical class
   * @throws Exception if anything goes wrong
   */
  public LexicalClassList getLexicalClasss(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the single lexical class for the specified parameters. If there are more
   * than one it throws an exception.
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
   * Returns the string class matching the specified parameters. May return more than
   * one string class if there are multiple entries with the same id, terminology,
   * and version. NOTE: this only applies to string class, not to other data types.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the string class
   * @throws Exception if anything goes wrong
   */
  public StringClassList getStringClasss(String terminologyId, String terminology,
    String version) throws Exception;

  /**
   * Returns the single string class for the specified parameters. If there are more
   * than one it throws an exception.
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
  public LexicalClass addLexicalClass(LexicalClass lexicalClass) throws Exception;

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
   * Get descendant concepts.
   *
   * @param concept the concept
   * @param pfsParameter the pfs parameter
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList getDescendantConcepts(Concept concept,
    PfsParameter pfsParameter) throws Exception;

  /**
   * Get ancestor concepts.
   *
   * @param concept the concept
   * @param pfsParameter the pfs parameter
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList getAncestorConcepts(Concept concept,
    PfsParameter pfsParameter) throws Exception;

  /**
   * Get child concepts.
   *
   * @param concept the concept
   * @param pfs the pfs
   * @return the concept list
   * @throws Exception the exception
   */
  public ConceptList getChildConcepts(Concept concept, PfsParameter pfs)
    throws Exception;

  /**
   * Returns the atom.
   * 
   * @param id the id
   * @return the atom
   * @throws Exception if anything goes wrong
   */
  public Atom getAtom(Long id) throws Exception;

  /**
   * Returns the atom matching the specified parameters.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the atom
   * @throws Exception if anything goes wrong
   */
  public Atom getAtom(String terminologyId, String terminology, String version)
    throws Exception;

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
   * Returns the relationship.
   * 
   * @param id the id
   * @return the relationship
   * @throws Exception if anything goes wrong
   */
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    Long id) throws Exception;

  /**
   * Returns the relationship matching the specified parameters.
   * 
   * @param terminologyId the id
   * @param terminology the terminology
   * @param version the version
   * @return the relationship
   * @throws Exception if anything goes wrong
   */
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    String terminologyId, String terminology, String version) throws Exception;

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
   * Returns the concept search results matching the query. Results can be
   * paged, filtered, and sorted.
   * @param terminology the terminology
   * @param version the version
   * @param query the search string
   * @param pfs the paging, filtering, sorting parameter
   * @return the search results for the search string
   * @throws Exception if anything goes wrong
   */
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String query, PfsParameter pfs) throws Exception;

  /**
   * Find concepts for search criteria.
   *
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param criteria the criteria
   * @param pfs the pfs
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList findConceptsForSearchCriteria(String terminology,
    String version, String query, SearchCriteriaList criteria, PfsParameter pfs)
    throws Exception;

  /**
   * Gets the all concepts.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @return the all concepts
   */
  public ConceptList getAllConcepts(String terminology, String version);

  /**
   * Gets the all relationship ids.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @return the all relationship ids
   */
  public StringList getAllRelationshipTerminologyIds(String terminology,
    String version);

  /**
   * Gets the all atom ids.
   *
   * @param terminology the terminology
   * @param version the terminology version
   * @return the all atom ids
   */
  public StringList getAllAtomTerminologyIds(String terminology, String version);

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
   * Removes all concepts and connected data structures.
   *
   * @param terminology the terminology
   * @param version the terminology version
   */
  public void clearConcepts(String terminology, String version);

  /**
   * Returns the graph resolution handler. This is configured internally but
   * made available through this service.
   *
   * @return the graph resolution handler
   * @throws Exception the exception
   */
  public GraphResolutionHandler getGraphResolutionHandler() throws Exception;

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
   * @param concept the concept
   * @return the computed preferred name
   * @throws Exception the exception
   */
  public String getComputedPreferredName(Concept concept) throws Exception;


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
   * @return the component stats
   * @throws Exception the exception
   */
  public Map<String, Integer> getComponentStats(String terminology,
    String version) throws Exception;

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
  
}