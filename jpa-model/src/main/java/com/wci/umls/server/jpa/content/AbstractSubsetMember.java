/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Abstract JPA-enabled implementation of {@link SubsetMember}.
 * Used mostly to define the table.
 * @param <T> the type
 */
@Entity
@Table(name = "subset_members")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 50)
@Audited
public abstract class AbstractSubsetMember<T extends ComponentHasAttributes> extends
    AbstractComponentHasAttributes implements SubsetMember<T> {


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
