/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a classification by LVG-normalized string. Any two atoms with the
 * same string class have the same LVG-normalized name.
 */
public interface LexicalClass extends AtomClass {

  /**
   * Returns the normalized string.
   *
   * @return the normalized string
   */
  public String getNormalizedName();

  /**
   * Sets the normalized string.
   *
   * @param normalizedString the normalized string
   */
  public void setNormalizedName(String normalizedString);
}