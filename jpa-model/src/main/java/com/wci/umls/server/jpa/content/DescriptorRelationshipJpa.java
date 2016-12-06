/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.jpa.helpers.MapKeyValueToCsvBridge;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.Relationship;

/**
 * JPA and JAXB enabled implementation of {@link DescriptorRelationship}.
 */
@Entity
@Table(name = "descriptor_relationships", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "descriptorRelationship")
public class DescriptorRelationshipJpa
    extends AbstractRelationship<Descriptor, Descriptor>
    implements DescriptorRelationship {

  /** The from concept. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  @JoinColumn(nullable = false)
  private Descriptor from;

  /** the to concept. */
  @ManyToOne(targetEntity = DescriptorJpa.class, optional = false)
  @JoinColumn(nullable = false)
  private Descriptor to;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds;

  /**
   * Instantiates an empty {@link DescriptorRelationshipJpa}.
   */
  public DescriptorRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link DescriptorRelationshipJpa} from the specified
   * parameters.
   *
   * @param relationship the concept relationship
   * @param collectionCopy the deep copy
   */
  public DescriptorRelationshipJpa(DescriptorRelationship relationship,
      boolean collectionCopy) {
    super(relationship, collectionCopy);
    to = relationship.getTo();
    from = relationship.getFrom();
    if (collectionCopy) {
      alternateTerminologyIds =
          new HashMap<>(relationship.getAlternateTerminologyIds());
    }
  }

  /**
   * Returns the from.
   *
   * @return the from
   */
  /* see superclass */
  @Override
  @XmlTransient
  public Descriptor getFrom() {
    return from;
  }

  /**
   * Sets the from.
   *
   * @param component the from
   */
  /* see superclass */
  @Override
  public void setFrom(Descriptor component) {
    this.from = component;
  }

  /**
   * Returns the from id. For JAXB.
   *
   * @return the from id
   */
  public Long getFromId() {
    return from == null ? null : from.getId();
  }

  /**
   * Sets the from id.
   *
   * @param id the from id
   */
  public void setFromId(Long id) {
    if (from == null) {
      from = new DescriptorJpa();
    }
    from.setId(id);
  }

  /**
   * Returns the from terminology.
   *
   * @return the from terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminology() {
    return from == null ? null : from.getTerminology();
  }

  /**
   * Sets the from terminology.
   *
   * @param terminology the from terminology
   */
  public void setFromTerminology(String terminology) {
    if (from == null) {
      from = new DescriptorJpa();
    }
    from.setTerminology(terminology);
  }

  /**
   * Returns the from version.
   *
   * @return the from version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromVersion() {
    return from == null ? null : from.getVersion();
  }

  /**
   * Sets the from terminology id.
   *
   * @param version the from terminology id
   */
  public void setFromVersion(String version) {
    if (from == null) {
      from = new DescriptorJpa();
    }
    from.setVersion(version);
  }

  /**
   * Returns the from terminology id.
   *
   * @return the from terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminologyId() {
    return from == null ? null : from.getTerminologyId();
  }

  /**
   * Sets the from terminology id.
   *
   * @param terminologyId the from terminology id
   */
  public void setFromTerminologyId(String terminologyId) {
    if (from == null) {
      from = new DescriptorJpa();
    }
    from.setTerminologyId(terminologyId);
  }

  /**
   * Returns the from term. For JAXB.
   *
   * @return the from term
   */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "fromNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getFromName() {
    return from == null ? null : from.getName();
  }

  /**
   * Sets the from term.
   *
   * @param term the from term
   */
  public void setFromName(String term) {
    if (from == null) {
      from = new DescriptorJpa();
    }
    from.setName(term);
  }

  /**
   * Returns the to.
   *
   * @return the to
   */
  /* see superclass */
  @Override
  @XmlTransient
  public Descriptor getTo() {
    return to;
  }

  /**
   * Sets the to.
   *
   * @param component the to
   */
  /* see superclass */
  @Override
  public void setTo(Descriptor component) {
    this.to = component;
  }

  /**
   * Returns the to id. For JAXB.
   *
   * @return the to id
   */
  public Long getToId() {
    return to == null ? null : to.getId();
  }

  /**
   * Sets the to id.
   *
   * @param id the to id
   */
  public void setToId(Long id) {
    if (to == null) {
      to = new DescriptorJpa();
    }
    to.setId(id);
  }

  /**
   * Returns the to terminology id.
   *
   * @return the to terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminologyId() {
    return to == null ? null : to.getTerminologyId();
  }

  /**
   * Sets the to terminology id.
   *
   * @param terminologyId the to terminology id
   */
  public void setToTerminologyId(String terminologyId) {
    if (to == null) {
      to = new DescriptorJpa();
    }
    to.setTerminologyId(terminologyId);
  }

  /**
   * Returns the to terminology.
   *
   * @return the to terminology
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminology() {
    return to == null ? null : to.getTerminology();
  }

  /**
   * Sets the to terminology.
   *
   * @param terminology the to terminology
   */
  public void setToTerminology(String terminology) {
    if (to == null) {
      to = new DescriptorJpa();
    }
    to.setTerminology(terminology);
  }

  /**
   * Returns the to version.
   *
   * @return the to version
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToVersion() {
    return to == null ? null : to.getVersion();
  }

  /**
   * Sets the to version.
   *
   * @param version the to version
   */
  public void setToVersion(String version) {
    if (to == null) {
      to = new DescriptorJpa();
    }
    to.setVersion(version);
  }

  /**
   * Returns the to term. For JAXB.
   *
   * @return the to term
   */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "toNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getToName() {
    return to == null ? null : to.getName();
  }

  /**
   * Sets the to term.
   *
   * @param term the to term
   */
  public void setToName(String term) {
    if (to == null) {
      to = new DescriptorJpa();
    }
    to.setName(term);
  }

  /**
   * Returns the alternate terminology ids.
   *
   * @return the alternate terminology ids
   */
  /* see superclass */
  @Override
  @FieldBridge(impl = MapKeyValueToCsvBridge.class)
  @Field(name = "alternateTerminologyIds", index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  public Map<String, String> getAlternateTerminologyIds() {
    if (alternateTerminologyIds == null) {
      alternateTerminologyIds = new HashMap<>(2);
    }
    return alternateTerminologyIds;
  }

  /**
   * Sets the alternate terminology ids.
   *
   * @param alternateTerminologyIds the alternate terminology ids
   */
  /* see superclass */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /**
   * Put alternate terminology id.
   *
   * @param relationship the relationship
   * @param inverseRelType the inverse rel type
   * @param inverseAdditionalRelType the inverse additional rel type
   * @return the relationship
   * @throws Exception the exception
   */

  /* see superclass */
  @Override
  public Relationship<Descriptor, Descriptor> createInverseRelationship(
    Relationship<Descriptor, Descriptor> relationship, String inverseRelType,
    String inverseAdditionalRelType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Create inverse of descriptor relationship " + relationship);
    DescriptorRelationship inverseRelationship = new DescriptorRelationshipJpa(
        (DescriptorRelationship) relationship, false);

    return populateInverseRelationship(relationship, inverseRelationship,
        inverseRelType, inverseAdditionalRelType);
  }

  /**
   * Hash code.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((from == null) ? 0 : from.hashCode());
    result = prime * result + ((to == null) ? 0 : to.hashCode());
    return result;
  }

  /**
   * Equals.
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
    DescriptorRelationshipJpa other = (DescriptorRelationshipJpa) obj;
    if (from == null) {
      if (other.from != null)
        return false;
    } else if (!from.equals(other.from))
      return false;
    if (to == null) {
      if (other.to != null)
        return false;
    } else if (!to.equals(other.to))
      return false;
    return true;
  }

  // Use superclass toString()

}
