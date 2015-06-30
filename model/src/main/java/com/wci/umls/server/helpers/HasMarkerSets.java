/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

/**
 * Represents a thing that has marker sets.
 */
public interface HasMarkerSets {
  /**
   * Returns the marker sets.
   *
   * @return the marker sets
   */
  public List<String> getMarkerSets();

  /**
   * Sets the marker sets.
   *
   * @param markerSets the marker sets
   */
  public void setMarkerSets(List<String> markerSets);

  /**
   * Add marker set.
   *
   * @param markerSet the marker set
   */
  public void addMarkerSet(String markerSet);

  /**
   * Removes the marker set.
   *
   * @param markerSet the marker set
   */
  public void removeMarkerSet(String markerSet);
}
