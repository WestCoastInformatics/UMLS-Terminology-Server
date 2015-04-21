/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasAlternateTerminologyIds;

/**
 * Represents a definition.
 */
public interface Definition extends ComponentHasAttributes, HasAlternateTerminologyIds {

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