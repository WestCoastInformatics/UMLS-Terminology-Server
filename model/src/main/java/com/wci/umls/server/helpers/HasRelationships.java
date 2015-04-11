/**
 * Copyright 2015 West Coast Informatics, LLC
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

  /**
   * Adds the relationship.
   *
   * @param relationship the relationship
   */
  public void addRelationship(T relationship);

  /**
   * Removes the relationship.
   *
   * @param relationship the relationship
   */
  public void removeRelationship(T relationship);

}
