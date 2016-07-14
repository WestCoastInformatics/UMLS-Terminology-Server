/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;

/**
 * Represents attribute identity for Metathesaurus editing.
 */
public interface RelationshipIdentity extends HasId {

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the terminology id.
   *
   * @return the terminology id
   */
  public String getTerminologyId();

  /**
   * Sets the terminology id.
   *
   * @param terminologyId the terminology id
   */
  public void setTerminologyId(String terminologyId);

  /**
   * Returns the relationship type.
   *
   * @return the relationship type
   */
  public String getRelationshipType();

  /**
   * Sets the relationship type.
   *
   * @param relationshipType the relationship type
   */
  public void setRelationshipType(String relationshipType);

  /**
   * Returns the additional relationship type.
   *
   * @return the additional relationship type
   */
  public String getAdditionalRelationshipType();

  /**
   * Sets the additional relationship type.
   *
   * @param additionalRelationshipType the additional relationship type
   */
  public void setAdditionalRelationshipType(String additionalRelationshipType);

  /**
   * Returns the from id.
   *
   * @return the from id
   */
  public String getFromId();

  /**
   * Sets the from id.
   *
   * @param fromId the from id
   */
  public void setFromId(String fromId);

  /**
   * Returns the from type.
   *
   * @return the from type
   */
  public IdType getFromType();

  /**
   * Sets the from type.
   *
   * @param fromType the from type
   */
  public void setFromType(IdType fromType);

  /**
   * Returns the from terminology.
   *
   * @return the from terminology
   */
  public String getFromTerminology();

  /**
   * Sets the from terminology.
   *
   * @param fromTerminology the from terminology
   */
  public void setFromTerminology(String fromTerminology);

  /**
   * Returns the to id.
   *
   * @return the to id
   */
  public String getToId();

  /**
   * Sets the to id.
   *
   * @param toId the to id
   */
  public void setToId(String toId);

  /**
   * Returns the to type.
   *
   * @return the to type
   */
  public IdType getToType();

  /**
   * Sets the to type.
   *
   * @param toType the to type
   */
  public void setToType(IdType toType);

  /**
   * Returns the to terminology.
   *
   * @return the to terminology
   */
  public String getToTerminology();

  /**
   * Sets the to terminology.
   *
   * @param toTerminology the to terminology
   */
  public void setToTerminology(String toTerminology);

  /**
   * Returns the inverse id.
   *
   * @return the inverse id
   */
  public Long getInverseId();

  /**
   * Sets the inverse id.
   *
   * @param inverseId the inverse id
   */
  public void setInverseId(Long inverseId);

}
