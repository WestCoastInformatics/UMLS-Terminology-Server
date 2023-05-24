/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.model.meta.IdType;

/**
 * Represents a relationship between two {@link ComponentInfo}s.
 */
public interface ComponentInfoRelationship extends
    Relationship<ComponentInfo, ComponentInfo> {

  /**
   * Returns the from terminology.
   *
   * @return the from terminology
   */
  public String getFromTerminology();

  /**
   * Sets the from terminology.
   *
   * @param terminology the from terminology
   */
  public void setFromTerminology(String terminology);

  /**
   * Returns the from version.
   *
   * @return the from version
   */
  public String getFromVersion();

  /**
   * Sets the from version.
   *
   * @param version the from version
   */
  public void setFromVersion(String version);

  /**
   * Returns the from type.
   *
   * @return the from type
   */
  public IdType getFromType();

  /**
   * Sets the from type.
   *
   * @param type the from type
   */
  public void setFromType(IdType type);

  /**
   * Returns the from terminology id.
   *
   * @return the from terminology id
   */
  public String getFromTerminologyId();

  /**
   * Sets the from terminology id.
   *
   * @param terminologyId the from terminology id
   */
  public void setFromTerminologyId(String terminologyId);

  /**
   * Returns the from name.
   *
   * @return the from name
   */
  public String getFromName();

  /**
   * Sets the from name.
   *
   * @param term the from name
   */
  public void setFromName(String term);
  
  /**
   * Returns the to terminology.
   *
   * @return the to terminology
   */
  public String getToTerminology();

  /**
   * Sets the to terminology.
   *
   * @param terminology the to terminology
   */
  public void setToTerminology(String terminology);

  /**
   * Returns the to version.
   *
   * @return the to version
   */
  public String getToVersion();

  /**
   * Sets the to version.
   *
   * @param version the to version
   */
  public void setToVersion(String version);

  /**
   * Returns the to type.
   *
   * @return the to type
   */
  public IdType getToType();

  /**
   * Sets the to type.
   *
   * @param type the to type
   */
  public void setToType(IdType type);

  /**
   * Returns the to terminology id.
   *
   * @return the to terminology id
   */
  public String getToTerminologyId();

  /**
   * Sets the to terminology id.
   *
   * @param terminologyId the to terminology id
   */
  public void setToTerminologyId(String terminologyId);

  /**
   * Returns the to name.
   *
   * @return the to name
   */
  public String getToName();

  /**
   * Sets the to name.
   *
   * @param term the to name
   */
  public void setToName(String term);
  
}
