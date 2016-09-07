/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;

/**
 * The Interface LexicalClassIdentity.
 */
public interface LexicalClassIdentity extends HasId {

  /**
   * Returns the normalized name.
   *
   * @return the normalized name
   */
  public String getNormalizedName();

  /**
   * Sets the normalized name.
   *
   * @param normalizedName the normalized name
   */
  public void setNormalizedName(String normalizedName);

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
