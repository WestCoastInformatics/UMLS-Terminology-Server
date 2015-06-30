/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.model.content.Concept;

/**
 * Represents a set of markers for a specific purpose.
 * See {@link Concept}.
 */
public interface MarkerSet extends Abbreviation {

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
   * Indicates whether or not marker for is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isMarkerFor();
  
  /**
   * Sets the marker for.
   *
   * @param markerFor the marker for flag
   */
  public void setMarkerFor(boolean markerFor);
}
