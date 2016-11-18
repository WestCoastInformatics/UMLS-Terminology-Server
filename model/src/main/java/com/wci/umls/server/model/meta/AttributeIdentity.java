/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.HasName;

/**
 * Represents attribute identity for Metathesaurus editing.
 */
public interface AttributeIdentity extends HasId, HasName {

  /**
   * Sets the component type.
   *
   * @param componentType the new component type
   */
  public void setComponentType(IdType componentType);

  /**
   * Gets the component type.
   *
   * @return the component type
   */
  public IdType getComponentType();

  /**
   * Sets the component id.
   *
   * @param componentId the new component id
   */
  public void setComponentId(String componentId);

  /**
   * Gets the component id.
   *
   * @return the component id
   */
  public String getComponentId();

  /**
   * Sets the component terminology.
   *
   * @param terminology the new component terminology
   */
  public void setComponentTerminology(String terminology);

  /**
   * Gets the component terminology.
   *
   * @return the component terminology
   */
  public String getComponentTerminology();

  /**
   * Gets the hash code.
   *
   * @return the hash code
   */
  public String getHashcode();

  /**
   * Sets the hash code.
   *
   * @param hashCode the new hash code
   */
  public void setHashcode(String hashCode);

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

}
