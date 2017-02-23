/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Represents a thing that has tree positions.
 * @param <T> the type of tree position
 */
public interface HasTreePositions<T extends TreePosition<? extends ComponentHasAttributesAndName>> {

  /**
   * Returns the tree positions.
   *
   * @return the tree positions
   */
  public List<T> getTreePositions();

  /**
   * Sets the tree positions.
   *
   * @param treePositions the tree positions
   */
  public void setTreePositions(List<T> treePositions);

}
