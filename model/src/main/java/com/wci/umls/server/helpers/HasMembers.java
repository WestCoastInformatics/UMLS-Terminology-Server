/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Represents a thing that has definitions.
 *
 * @param <T> the generic type
 */
public interface HasMembers<T extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> {

  /**
   * Returns the members.
   * 
   * @return the members
   */
  public List<T> getMembers();

  /**
   * Sets the members.
   * 
   * @param members the members
   */
  public void setMembers(List<T> members);


}