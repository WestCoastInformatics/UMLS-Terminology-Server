/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents membership of something in a {@link Subset}.
 * @param <T> the member type
 * @param <S> the subset type
 */
public interface SubsetMember<T extends ComponentHasAttributesAndName, S extends Subset>
    extends ComponentHasAttributes {

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

  /**
   * Returns the subset.
   *
   * @return the subset
   */
  public S getSubset();

  /**
   * Sets the subset.
   *
   * @param subset the new subset
   */
  public void setSubset(S subset);
}
