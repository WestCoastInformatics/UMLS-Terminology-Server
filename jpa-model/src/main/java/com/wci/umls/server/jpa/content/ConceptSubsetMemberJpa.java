/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Abstract JPA-enabled implementation of an {@link Concept}
 * {@link SubsetMember}.
 */
@Entity
@Audited
@DiscriminatorValue("Concept")
@XmlRootElement(name = "member")
public class ConceptSubsetMemberJpa extends AbstractSubsetMember<Concept>
    implements ConceptSubsetMember {

  /** The member. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @JoinColumn(nullable = false, name="concept_id")
  private Concept member;

  /** The subset. */
  @ManyToOne(targetEntity = ConceptSubsetJpa.class, optional = false)
  private ConceptSubset subset;

  /**
   * Instantiates an empty {@link ConceptSubsetMemberJpa}.
   */
  public ConceptSubsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ConceptSubsetMemberJpa} from the specified
   * parameters.
   *
   * @param member the subset
   * @param deepCopy the deep copy
   */
  public ConceptSubsetMemberJpa(ConceptSubsetMember member, boolean deepCopy) {
    super(member, deepCopy);
    subset = member.getSubset();
    this.member = member.getMember();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.SubsetMember#getMember()
   */
  @Override
  public Concept getMember() {
    return member;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.SubsetMember#setMember(java.lang.Object)
   */
  @Override
  public void setMember(Concept member) {
    this.member = member;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.ConceptSubsetMember#getSubset()
   */
  @Override
  public ConceptSubset getSubset() {
    return subset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.ConceptSubsetMember#setSubset(com.wci
   * .umls .server.model.content.ConceptSubset)
   */
  @Override
  public void setSubset(ConceptSubset subset) {
    this.subset = subset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((member == null) ? 0 : member.hashCode());
    result = prime * result + ((subset == null) ? 0 : subset.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.content.AbstractComponent#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ConceptSubsetMemberJpa other = (ConceptSubsetMemberJpa) obj;
    if (member == null) {
      if (other.member != null)
        return false;
    } else if (!member.equals(other.member))
      return false;
    if (subset == null) {
      if (other.subset != null)
        return false;
    } else if (!subset.equals(other.subset))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractComponent#toString()
   */
  @Override
  public String toString() {
    return "ConceptSubsetMemberJpa [member=" + member + ", subset=" + subset
        + "]";
  }

}
