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
   * Returns the string pre.
   *
   * @return the string pre
   */
  public String getStringPre();

  /**
   * Sets the string pre.
   *
   * @param stringPre the string pre
   */
  public void setStringPre(String stringPre);

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

}
