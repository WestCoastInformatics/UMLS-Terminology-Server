/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a classification by case-sensitive string. Any two atoms with the
 * same string class have the same case-sensitive name.
 */
public interface StringClass extends AtomClass {

  /**
   * Sets the language.
   *
   * @param language the language
   */
  public void setLanguage(String language);

  /**
   * Returns the language.
   *
   * @return the language
   */
  public String getLanguage();

  // nothing extra, the "name" is the string

}