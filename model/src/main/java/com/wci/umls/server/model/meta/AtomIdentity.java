/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;

/**
 * Represents atom identity for Metathesaurus editing.
 */
public interface AtomIdentity extends HasId/*, HasName*/ {

  /* see superclass */
  public Long getId();

  /* see superclass */
  public void setId(Long id);

  /**
   * Returns the string class id.
   *
   * @return the string class id
   */
  public String getStringClassId();

  /**
   * Sets the string class id.
   *
   * @param stringClassid the string class id
   */
  public void setStringClassId(String stringClassid);

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the new terminology
   */
  public void setTerminology(String terminology);

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public String getTerminologyId();

  /**
   * Sets the terminology.
   *
   * @param terminologyId the new terminology id
   */
  public void setTerminologyId(String terminologyId);

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
   * Returns the code.
   *
   * @return the code
   */
  public String getCode();

  /**
   * Sets the code.
   *
   * @param code the code
   */
  public void setCode(String code);

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  public String getConceptId();

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(String conceptId);

   /**
    * Returns the descriptor id.
    *
    * @return the descriptor id
    */
   public String getDescriptorId();

  /**
   * Sets the descriptor id.
   *
   * @param descriptorId the descriptor id
   */
  public void setDescriptorId(String descriptorId);

}
