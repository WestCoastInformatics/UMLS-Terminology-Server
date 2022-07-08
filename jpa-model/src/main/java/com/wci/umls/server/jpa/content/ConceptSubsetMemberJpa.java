/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.SortableField;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Abstract JPA and JAXB enabled implementation of an {@link Concept}
 * {@link SubsetMember}.
 */
@Entity
@Table(name = "concept_subset_members", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
//@Audited
@Indexed
@XmlRootElement(name = "conceptMember")
public class ConceptSubsetMemberJpa extends AbstractSubsetMember<Concept, ConceptSubset>
    implements ConceptSubsetMember {

  /** The member. */
  @ManyToOne(targetEntity = ConceptJpa.class, optional = false)
  @JoinColumn(nullable = false, name = "concept_id")
  private Concept member;

  /** The subset. */
  @ManyToOne(targetEntity = ConceptSubsetJpa.class, optional = false)
  @JoinColumn(nullable = false, name = "subset_id")
  private ConceptSubset subset;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "concept_subset_members_attributes",
      inverseJoinColumns = @JoinColumn(name = "attributes_id"),
      joinColumns = @JoinColumn(name = "concept_subset_members_id"))
  private List<Attribute> attributes = null;

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
   * @param copy the copy
   * @param collectionCopy the deep copy
   */
  public ConceptSubsetMemberJpa(ConceptSubsetMember copy, boolean collectionCopy) {
    super(copy, collectionCopy);
    subset = copy.getSubset();
    member = copy.getMember();
    if (collectionCopy) {
      for (final Attribute attribute : copy.getAttributes()) {
        getAttributes().add(new AttributeJpa(attribute));
      }
    }
  }

  /* see superclass */
  @Override
  @XmlElement(type = AttributeJpa.class)
  public List<Attribute> getAttributes() {
    if (attributes == null) {
      attributes = new ArrayList<>(1);
    }
    return attributes;
  }

  /* see superclass */
  @Override
  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  /* see superclass */
  @Override
  public Attribute getAttributeByName(String name) {
    for (final Attribute attribute : getAttributes()) {
      // If there are more than one, this just returns the first.
      if (attribute.getName().equals(name)) {
        return attribute;
      }
    }
    return null;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public Concept getMember() {
    return member;
  }

  /* see superclass */
  @Override
  public void setMember(Concept member) {
    this.member = member;
  }

  /**
   * Returns the member id. For JAXB.
   *
   * @return the member id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getMemberId() {
    return member == null ? null : member.getId();
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getMemberTerminologyId() {
    return member == null ? null : member.getTerminologyId();
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
   * Returns the member terminology. For JAXB.
   *
   * @return the member terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getMemberTerminology() {
    return member == null ? null : member.getTerminology();
  }

  /**
   * Sets the member terminology. For JAXB.
   *
   * @param terminology the member terminology
   */
  public void setMemberTerminology(String terminology) {
    if (member == null) {
      member = new ConceptJpa();
    }
    member.setTerminology(terminology);
  }

  /**
   * Returns the member version. For JAXB.
   *
   * @return the member version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getMemberVersion() {
    return member == null ? null : member.getVersion();
  }

  /**
   * Sets the member version. For JAXB.
   *
   * @param version the member version
   */
  public void setMemberVersion(String version) {
    if (member == null) {
      member = new ConceptJpa();
    }
    member.setVersion(version);
  }

  /**
   * Returns the member name. For JAXB.
   *
   * @return the member name
   */
  @Fields({
      @Field(index = Index.YES, store = Store.NO, analyze = Analyze.YES,
          analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "memberNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @SortableField(forField = "memberNameSort")
  public String getMemberName() {
    return member == null ? null : member.getName();
  }

  /**
   * Sets the member name. For JAXB.
   *
   * @param name the member name
   */
  public void setMemberName(String name) {
    if (member == null) {
      member = new ConceptJpa();
    }
    member.setName(name);
  }

  /* see superclass */
  @XmlTransient
  @Override
  public ConceptSubset getSubset() {
    return subset;
  }

  /* see superclass */
  @Override
  public void setSubset(ConceptSubset subset) {
    this.subset = subset;
  }

  /**
   * Returns the subset id. For JAXB.
   *
   * @return the subset id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getSubsetId() {
    return subset == null ? null : subset.getId();
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getSubsetTerminologyId() {
    return subset == null ? null : subset.getTerminologyId();
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
   * Returns the subset terminology. For JAXB.
   *
   * @return the subset terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getSubsetTerminology() {
    return subset == null ? null : subset.getTerminology();
  }

  /**
   * Sets the subset terminology. For JAXB.
   *
   * @param terminology the subset terminology
   */
  public void setSubsetTerminology(String terminology) {
    if (subset == null) {
      subset = new ConceptSubsetJpa();
    }
    subset.setTerminology(terminology);
  }

  /**
   * Returns the subset version. For JAXB.
   *
   * @return the subset version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getSubsetVersion() {
    return subset == null ? null : subset.getVersion();
  }

  /**
   * Sets the subset version. For JAXB.
   *
   * @param version the subset version
   */
  public void setSubsetVersion(String version) {
    if (subset == null) {
      subset = new ConceptSubsetJpa();
    }
    subset.setVersion(version);
  }

  /**
   * Returns the subset name. For JAXB.
   *
   * @return the subset name
   */
  @Field(index = Index.YES, store = Store.NO, analyze = Analyze.YES,
      analyzer = @Analyzer(definition = "noStopWord"))
  public String getSubsetName() {
    return subset == null ? null : subset.getName();
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
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((member == null) ? 0 : member.hashCode());
    result = prime * result + ((member == null || member.getTerminologyId() == null) ? 0
        : member.getTerminologyId().hashCode());
    result = prime * result + ((subset == null || subset.getTerminologyId() == null) ? 0
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
    } else if (!member.getTerminologyId().equals(other.member.getTerminologyId()))
      return false;
    if (subset == null) {
      if (other.subset != null)
        return false;
    } else if (subset.getTerminologyId() == null) {
      if (other.subset != null && other.subset.getTerminologyId() != null)
        return false;
    } else if (!subset.getTerminologyId().equals(other.subset.getTerminologyId()))
      return false;
    return true;
  }

}
