/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Generically represents any other kind of metadata that does not conform to
 * the model.
 */
public interface GeneralMetadataEntry extends Abbreviation {

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

}
