/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Abstract JPA-enabled implementation of an {@link Atom} {@link SubsetMember}.
 */
@Entity
@Table(name = "atom_subset_members", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "atomMember")
public class AtomSubsetMemberJpa extends AbstractSubsetMember<Atom> implements
    AtomSubsetMember {

  /** The member. */
  @ManyToOne(targetEntity = AtomJpa.class, optional = false)
  @JoinColumn(nullable = false, name="atom_id")
  private Atom member;

  /** The subset. */
  @ManyToOne(targetEntity = AtomSubsetJpa.class, optional = false)
  private AtomSubset subset;

  /**
   * Instantiates an empty {@link AtomSubsetMemberJpa}.
   */
  public AtomSubsetMemberJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AtomSubsetMemberJpa} from the specified parameters.
   *
   * @param member the subset
   * @param deepCopy the deep copy
   */
  public AtomSubsetMemberJpa(AtomSubsetMember member, boolean deepCopy) {
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
  public Atom getMember() {
    return member;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.SubsetMember#setMember(java.lang.Object)
   */
  @Override
  public void setMember(Atom member) {
    this.member = member;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.AtomSubsetMember#getSubset()
   */
  @Override
  public AtomSubset getSubset() {
    return subset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.AtomSubsetMember#setSubset(com.wci.umls
   * .server.model.content.AtomSubset)
   */
  @Override
  public void setSubset(AtomSubset subset) {
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
    AtomSubsetMemberJpa other = (AtomSubsetMemberJpa) obj;
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
    return "AtomSubsetMemberJpa [member=" + member + ", subset=" + subset + "]";
  }

}
