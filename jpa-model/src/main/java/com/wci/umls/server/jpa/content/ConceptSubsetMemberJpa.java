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
import javax.xml.bind.annotation.XmlTransient;

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
@Table(name = "concept_subset_members", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "conceptMember")
public class ConceptSubsetMemberJpa extends AbstractSubsetMember<Concept>
    implements ConceptSubsetMember {

  /** The member. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @JoinColumn(nullable = false, name = "concept_id")
  private Concept member;

  /** The subset. */
  @ManyToOne(targetEntity = ConceptSubsetJpa.class, optional = false)
  @JoinColumn(nullable = false, name = "subset_id")
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
  @XmlTransient
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

  /**
   * Returns the member id. For JAXB.
   *
   * @return the member id
   */
  public Long getMemberId() {
    return member == null ? 0 : member.getId();
  }

  /**
   * Sets the member id. For JAXB.
   *
   * @param id the member id
   */
  public void setMemberId(Long id) {
    if (member == null) {
      member = new ConceptJpa();
    }
    member.setId(id);
  }

  /**
   * Returns the member terminology id. For JAXB.
   *
   * @return the member terminology id
   */
  public String getMemberTerminologyId() {
    return member == null ? "" : member.getTerminologyId();
  }

  /**
   * Sets the member terminology id. For JAXB.
   *
   * @param terminologyId the member terminology id
   */
  public void setMemberTerminologyId(String terminologyId) {
    if (member == null) {
      member = new ConceptJpa();
    }
    member.setTerminologyId(terminologyId);
  }

  /**
   * Returns the member default preferred name. For JAXB.
   *
   * @return the member default preferred name
   */
  public String getMemberDefaultPreferredName() {
    return member == null ? "" : member.getDefaultPreferredName();
  }

  /**
   * Sets the member default preferred name. For JAXB.
   *
   * @param name the member default preferred name
   */
  public void setMemberDefaultPreferredName(String name) {
    if (member == null) {
      member = new ConceptJpa();
    }
    member.setDefaultPreferredName(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.ConceptSubsetMember#getSubset()
   */
  @XmlTransient
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

  /**
   * Returns the subset id. For JAXB.
   *
   * @return the subset id
   */
  public Long getSubsetId() {
    return subset == null ? 0 : subset.getId();
  }

  /**
   * Sets the subset id. For JAXB.
   *
   * @param id the subset id
   */
  public void setSubsetId(Long id) {
    if (subset == null) {
      subset = new ConceptSubsetJpa();
    }
    subset.setId(id);
  }

  /**
   * Returns the subset terminology id. For JAXB.
   *
   * @return the subset terminology id
   */
  public String getSubsetTerminologyId() {
    return subset == null ? "" : subset.getTerminologyId();
  }

  /**
   * Sets the subset terminology id. For JAXB.
   *
   * @param terminologyId the subset terminology id
   */
  public void setSubsetTerminologyId(String terminologyId) {
    if (subset == null) {
      subset = new ConceptSubsetJpa();
    }
    subset.setTerminologyId(terminologyId);
  }

  /**
   * Returns the subset name. For JAXB.
   *
   * @return the subset name
   */
  public String getSubsetName() {
    return subset == null ? "" : subset.getName();
  }

  /**
   * Sets the subset name. For JAXB.
   *
   * @param name the subset name
   */
  public void setSubsetName(String name) {
    if (subset == null) {
      subset = new ConceptSubsetJpa();
    }
    subset.setName(name);
  }

  /**
   * CUSTOM equals method for subset.getTerminologyId()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((member == null) ? 0 : member.hashCode());
    result =
        prime
            * result
            + ((member == null || member.getTerminologyId() == null) ? 0
                : member.getTerminologyId().hashCode());
    result =
        prime
            * result
            + ((subset == null || subset.getTerminologyId() == null) ? 0
                : subset.getTerminologyId().hashCode());
    return result;
  }

  /**
   * CUSTOM equals method for subset.getTerminologyId()
   *
   * @param obj the obj
   * @return true, if successful
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
    } else if (member.getTerminologyId() == null) {
      if (other.member != null && other.member.getTerminologyId() != null)
        return false;
    } else if (!member.getTerminologyId().equals(
        other.member.getTerminologyId()))
      return false;
    if (subset == null) {
      if (other.subset != null)
        return false;
    } else if (subset.getTerminologyId() == null) {
      if (other.subset != null && other.subset.getTerminologyId() != null)
        return false;
    } else if (!subset.getTerminologyId().equals(
        other.subset.getTerminologyId()))
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
    return "ConceptSubsetMemberJpa [id = " + getId() + ", member=" + member + ", subset=" + subset
        + "]";
  }

}
