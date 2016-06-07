/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Abstract JPA and JAXB enabled implementation of {@link SubsetMember}. Used mostly to
 * define the table.
 * @param <T> the member type
 * @param <S> the subset type
 */
@Audited
@MappedSuperclass
@XmlSeeAlso({
    AtomSubsetMemberJpa.class, ConceptSubsetMemberJpa.class
})
public abstract class AbstractSubsetMember<T extends ComponentHasAttributesAndName, S extends Subset>
    extends AbstractComponentHasAttributes implements SubsetMember<T, S> {

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
  public AbstractSubsetMember(SubsetMember<T, S> member, boolean deepCopy) {
    super(member, deepCopy);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " [" + super.toString() + ", member="
        + getMember() + ", subset=" + getSubset() + "]";
  }

}
