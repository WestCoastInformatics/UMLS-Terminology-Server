/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Generic object to contain a primitive result.
 * 
 */
public interface RestPrimitive {

  /**
   * Returns the value.
   * 
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   * 
   * @param str the value
   */
  public void setValue(String str);

  /**
   * Returns the type.
   * 
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   * 
   * @param o the type
   */
  public void setType(String o);
}
