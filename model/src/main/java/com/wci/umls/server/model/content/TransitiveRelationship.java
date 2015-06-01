/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a transitive relationship between two {@link AtomClass}es.
 * @param <T> the type of hierarchy
 */
public interface TransitiveRelationship<T extends AtomClass> extends ComponentHasAttributes {
 
  /**
   * Returns the depth.
   *s
   * @return the depth
   */
  public int getDepth();
  
  /**
   * Sets the depth.
   *
   * @param depth the depth
   */
  public void setDepth(int depth);

  /**
   * Returns the super type.
   *
   * @return the super type
   */
  public T getSuperType();

  /**
   * Sets the super type.
   *
   * @param ancestor the super type
   */
  public void setSuperType(T ancestor);

  /**
   * Returns the sub type.
   *
   * @return the sub type
   */
  public T getSubType();

  /**
   * Sets the sub type.
   *
   * @param descendant the sub type
   */
  public void setSubType(T descendant);

}
