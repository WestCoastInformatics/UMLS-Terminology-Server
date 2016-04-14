/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.model.content.Concept;

/**
 * Represents a set of labels for a specific purpose. See {@link Concept}.
 */
public interface LabelSet extends Abbreviation {

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   *
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Indicates whether or not the label set is derived from another one.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDerived();

  /**
   * Sets the derived flag.
   *
   * @param derived the derived for flag
   */
  public void setDerived(boolean derived);
}
