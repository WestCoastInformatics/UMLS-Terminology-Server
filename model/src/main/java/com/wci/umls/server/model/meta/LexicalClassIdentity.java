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
   * Returns the normalized string.
   *
   * @return the normalized string
   */
  public String getNormalizedString();

  /**
   * Sets the normalized string.
   *
   * @param normalizedString the normalized string
   */

  public void setNormalizedString(String normalizedString);

  /**
   * Returns the normalized string pre.
   *
   * @return the normalized string pre
   */
  public String getNormalizedStringPre();

  /**
   * Sets the normalized string pre.
   *
   * @param normalizedStringPre the normalized string pre
   */
  public void setNormalizedStringPre(String normalizedStringPre);

}
