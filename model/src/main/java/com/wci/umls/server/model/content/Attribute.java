/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.model.meta.AttributeName;

/**
 * Represents an attribute name/value pair.
 */
public interface Attribute extends Component {

  /**
   * Returns the name.
   *
   * @return the name
   */
  public AttributeName getName();

  /**
   * Sets the name.
   *
   * @param name the name
   */
  public void setName(AttributeName name);

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