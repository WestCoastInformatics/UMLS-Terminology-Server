/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.Map;

import com.wci.umls.server.helpers.HasAlternateTerminologyIds;
import com.wci.umls.server.helpers.HasDefinitions;
import com.wci.umls.server.helpers.HasMembers;
import com.wci.umls.server.helpers.HasRelationships;

/**
 * Represents a single atomic unit of meaning. It's a name from a vocabulary
 * with associated identifiers.
 */
public interface Atom extends ComponentHasAttributesAndName, HasDefinitions,
    HasRelationships<AtomRelationship>, HasAlternateTerminologyIds,
    HasMembers<AtomSubsetMember> {

  /**
   * Returns the string class id.
   *
   * @return the string class id
   */
  public String getStringClassId();

  /**
   * Sets the string class id.
   *
   * @param id the string class id
   */
  public void setStringClassId(String id);

  /**
   * Returns the lexical class id.
   *
   * @return the lexical class id
   */
  public String getLexicalClassId();

  /**
   * Sets the lexical class id.
   *
   * @param id the lexical class id
   */
  public void setLexicalClassId(String id);

  /**
   * Returns the code id for this atom in its own terminology.
   *
   * @return the code id
   */
  public String getCodeId();

  /**
   * Sets the code id for this atom in its own terminology.
   *
   * @param id the code id
   */
  public void setCodeId(String id);

  /**
   * Returns the concept id for this atom in its own terminology.
   *
   * @return the concept id
   */
  public String getConceptId();

  /**
   * Sets the concept id for this atom in its own terminology.
   *
   * @param id the concept id
   */
  public void setConceptId(String id);

  /**
   * Returns the descriptor id for this atom in its own terminology.
   *
   * @return the descriptor
   */
  public String getDescriptorId();

  /**
   * Sets the descriptor id for this atom in its own terminology.
   *
   * @param id the descriptor id
   */
  public void setDescriptorId(String id);

  /**
   * Returns the map of terminology values to concept terminology ids.
   *
   * @return the map of terminology values to concept terminology ids
   */
  public Map<String, String> getConceptTerminologyIds();

  /**
   * Sets the concepts.
   *
   * @param map the map
   */
  public void setConceptTerminologyIds(Map<String, String> map);

  /**
   * Put concept terminology id.
   *
   * @param terminology the terminology
   * @param terminologyId the terminology id
   */
  public void putConceptTerminologyId(String terminology, String terminologyId);

  /**
   * Removes the concept terminology id.
   *
   * @param terminology the terminology
   */
  public void removeConceptTerminologyId(String terminology);

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