/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.Map;

import com.wci.umls.server.helpers.HasDefinitions;
import com.wci.umls.server.helpers.HasRelationships;

/**
 * Represents a single atomic unit of meaning. It's a name from a vocabulary
 * with associated identifiers.
 */
public interface Atom extends ComponentHasAttributes, HasDefinitions,
    HasRelationships<AtomRelationship> {

  /**
   * Returns the term.
   *
   * @return the term
   */
  public String getTerm();

  /**
   * Sets the term.
   *
   * @param term the term
   */
  public void setTerm(String term);

  /**
   * Returns the string class.
   *
   * @return the string class
   */
  public String getStringClassId();

  /**
   * Sets the string class.
   *
   * @param id the string class id
   */
  public void setStringClassId(String id);

  /**
   * Returns the lexical class.
   *
   * @return the lexical class
   */
  public String getLexicalClassId();

  /**
   * Sets the lexical class.
   *
   * @param id the lexical class id
   */
  public void setLexicalClassId(String id);

  /**
   * Returns the code.
   *
   * @return the code
   */
  public String getCodeId();

  /**
   * Sets the code.
   *
   * @param id the code id
   */
  public void setCodeId(String id);

  /**
   * Returns the descriptor.
   *
   * @return the descriptor
   */
  public String getDescriptorId();

  /**
   * Sets the descriptor.
   *
   * @param id the descriptor id
   */
  public void setDescriptorId(String id);

  /**
   * Returns the map of terminology values to concept terminology ids.
   *
   * @return the map of terminology values to concept terminology ids
   */
  public Map<String, String> getConceptTerminologyIdMap();

  /**
   * Sets the concepts.
   *
   * @param map the map
   */
  public void setConceptTerminologyIdMap(Map<String, String> map);

  /**
   * Adds the concept to the terminology values to concept terminology ids map.
   *
   * @param concept the concept
   */
  public void addConcept(Concept concept);

  /**
   * Removes the concept from the terminology values to concept terminology ids
   * map.
   *
   * @param concept the concept
   */
  public void removeConcept(Concept concept);

  /**
   * Returns the language.
   *
   * @return the language
   */
  public String getLanguage();

  /**
   * Sets the language.
   *
   * @param language the language
   */
  public void setLanguage(String language);

  /**
   * Returns the term type.
   *
   * @return the term type
   */
  public String getTermType();

  /**
   * Sets the term type.
   *
   * @param termType the term type
   */
  public void setTermType(String termType);

  /**
   * Returns the workflow status.
   *
   * @return the workflow status
   */
  public String getWorkflowStatus();

  /**
   * Sets the workflow status.
   *
   * @param workflowStatus the workflow status
   */
  public void setWorkflowStatus(String workflowStatus);

}