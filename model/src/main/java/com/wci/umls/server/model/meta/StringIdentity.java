/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;

/**
 * Represents atom identity for Metathesaurus editing.
 */
public interface StringIdentity extends HasId {

  /**
   * Returns the string.
   *
   * @return the string
   */
  public String getString();

  /**
   * Sets the string.
   *
   * @param string the string
   */
  public void setString(String string);

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

}
