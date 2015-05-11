/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents membership of something in a {@link Subset}.
 * @param <T> the type
 */
public interface SubsetMember<T extends ComponentHasAttributesAndName> extends
    ComponentHasAttributes {

  /**
   * Returns the member.
   *
   * @return the member
   */
  public T getMember();

  /**
   * Sets the member.
   *
   * @param member the new member
   */
  public void setMember(T member);
}
