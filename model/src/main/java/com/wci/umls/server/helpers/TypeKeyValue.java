/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Generically represents a tuple of type, key, and value. Used for configuring
 * filters, acronym lists, etcs.
 */
public interface TypeKeyValue extends HasId {

  /**
   * Returns the type.
   *
   * @return the type
   */
  public String getType();

  /**
   * Sets the type.
   *
   * @param type the type
   */
  public void setType(String type);

  /**
   * Returns the key.
   *
   * @return the key
   */
  public String getKey();

  /**
   * Sets the key.
   *
   * @param key the key
   */
  public void setKey(String key);

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
