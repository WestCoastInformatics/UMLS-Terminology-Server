/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasAlternateTerminologyIds;

/**
 * Represents an attribute name/value pair.
 */
public interface Attribute extends Component, HasAlternateTerminologyIds {

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value
   */
  public void setValue(String value);

}