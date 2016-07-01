/*
 *    Copyright 2015 West Coast Informatics, LLC
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

}
