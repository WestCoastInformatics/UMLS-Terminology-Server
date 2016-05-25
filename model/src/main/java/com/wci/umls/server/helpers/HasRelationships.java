/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Relationship;

/**
 * Represents a thing that has relationships.
 * @param <T> the type of relationship
 */
public interface HasRelationships<T extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> {

  /**
   * Returns the relationships.
   *
   * @return the relationships
   */
  public List<T> getRelationships();

  /**
   * Sets the relationships.
   *
   * @param relationships the relationships
   */
  public void setRelationships(List<T> relationships);


}
