/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Abstract JPA-enabled implementation of {@link SubsetMember}. Used mostly to
 * define the table.
 * @param <T> the type
 */
@Audited
@MappedSuperclass
public abstract class AbstractSubsetMember<T extends ComponentHasAttributesAndName>
    extends AbstractComponentHasAttributes implements SubsetMember<T> {

  /**
   * Instantiates an empty {@link AbstractSubsetMember}.
   */
  public AbstractSubsetMember() {
    // do nothing
  }

  /**
   * Instantiates a {@link AbstractSubsetMember} from the specified parameters.
   *
   * @param member the subset
   * @param deepCopy the deep copy
   */
  public AbstractSubsetMember(SubsetMember<T> member, boolean deepCopy) {
    super(member, deepCopy);
  }

}
