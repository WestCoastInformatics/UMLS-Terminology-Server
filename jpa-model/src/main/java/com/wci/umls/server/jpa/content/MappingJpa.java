/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
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

import com.wci.umls.server.jpa.helpers.MapKeyValueToCsvBridge;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA and JAXB enabled implementation of {@link Mapping}.
 */
@Entity
@Table(name = "mappings", uniqueConstraints = @UniqueConstraint(columnNames = {
    "fromTerminologyId", "toTerminologyId", "terminology", "version", "id"
}))
//@Audited
@Indexed
@XmlRootElement(name = "mapping")
public class MappingJpa extends AbstractComponentHasAttributes
    implements Mapping {

  /** The map set. */
  @ManyToOne(targetEntity = MapSetJpa.class, optional = false)
  @JoinColumn(nullable = false, name = "mapSet_id")
  private MapSet mapSet;

  /** The from terminology id. */
  @Column(nullable = false)
  private String fromTerminologyId;

  /** The to terminology id. */
  @Column(nullable = false)
  private String toTerminologyId;

  /** The from id type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IdType fromIdType;

  /** The to id type. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IdType toIdType;

  /** The advice. */
  @Column(nullable = true, length = 4000)
  private String advice;

  /** The rule. */
  @Column(nullable = true, length = 4000)
  private String rule;

  /** The group. */
  @Column(name = "mapGroup", nullable = true)
  private String group;

  /** The rank. */
  @Column(nullable = true)
  private String rank;

  /** The from name. */
  @Column(nullable = true)
  private String fromName;

  /** The to name. */
  @Column(nullable = true)
  private String toName;

  /** The relationship type. */
  @Column(nullable = true)
  private String relationshipType;

  /** The additional relationship type. */
  @Column(nullable = true)
  private String additionalRelationshipType;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Fetch(FetchMode.JOIN)
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds;

  /** The attributes. */
  @OneToMany(targetEntity = AttributeJpa.class)
  @JoinColumn(name = "attributes_id")
  @JoinTable(name = "mappings_attributes",
      inverseJoinColumns = @JoinColumn(name = "attributes_id"),
      joinColumns = @JoinColumn(name = "mappings_id"))
  private List<Attribute> attributes = null;
  
  /**
   * Instantiates an empty {@link MappingJpa}.
   */
  public MappingJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link MappingJpa} from the specified parameters.
   *
   * @param mapping the mapping
   * @param collectionCopy the deep copy
   */
  public MappingJpa(Mapping mapping, boolean collectionCopy) {
    super(mapping, collectionCopy);
    mapSet = mapping.getMapSet();
    fromTerminologyId = mapping.getFromTerminologyId();
    fromName = mapping.getFromName();
    toTerminologyId = mapping.getToTerminologyId();
    fromIdType = mapping.getFromIdType();
    toIdType = mapping.getToIdType();
    toName = mapping.getToName();
    advice = mapping.getAdvice();
    rule = mapping.getRule();
    group = mapping.getGroup();
    rank = mapping.getRank();
    relationshipType = mapping.getRelationshipType();
    additionalRelationshipType = mapping.getAdditionalRelationshipType();
    alternateTerminologyIds = new HashMap<>(mapping.getAlternateTerminologyIds());
    
    if (collectionCopy) {
        for (final Attribute attribute : mapping.getAttributes()) {
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
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public IdType getFromIdType() {
    return fromIdType;
  }

  /* see superclass */
  @Override
  public void setFromIdType(IdType fromIdType) {
    this.fromIdType = fromIdType;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public IdType getToIdType() {
    return toIdType;
  }

  /* see superclass */
  @Override
  public void setToIdType(IdType toIdType) {
    this.toIdType = toIdType;
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
  @SortableField(forField = "fromNameSort")
  @Override
  public String getFromName() {
    return fromName;
  }

  /**
   * Gets the from terminology.
   *
   * @return the from terminology
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminology() {
    return mapSet == null ? null : mapSet.getFromTerminology();
  }

  /**
   * Gets the from version.
   *
   * @return the from version
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromVersion() {
    return mapSet == null ? null : mapSet.getFromVersion();
  }

  /**
   * Gets the to terminology.
   *
   * @return the to terminology
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminology() {
    return mapSet == null ? null : mapSet.getToTerminology();
  }

  /**
   * Gets the to version.
   *
   * @return the to version
   */
  @XmlTransient
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToVersion() {
    return mapSet == null ? null : mapSet.getToVersion();
  }

  /**
   * Sets the from term.
   *
   * @param term the from term
   */
  @Override
  public void setFromName(String term) {
    this.fromName = term;
  }

  /**
   * Returns the to term. For JAXB.
   *
   * @return the to term
   */
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "toNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  @SortableField(forField = "toNameSort")
  @Override
  public String getToName() {
    return toName;
  }

  /**
   * Sets the to term.
   *
   * @param term the to term
   */
  @Override
  public void setToName(String term) {
    this.toName = term;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getFromTerminologyId() {
    return fromTerminologyId;
  }

  /* see superclass */
  @Override
  public void setFromTerminologyId(String id) {
    this.fromTerminologyId = id;
  }

  /* see superclass */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public String getToTerminologyId() {
    return toTerminologyId;
  }

  /* see superclass */
  @Override
  public void setToTerminologyId(String id) {
    this.toTerminologyId = id;
  }

  /* see superclass */
  @Override
  public String getRelationshipType() {
    return relationshipType;
  }

  /* see superclass */
  @Override
  public void setRelationshipType(String relType) {
    this.relationshipType = relType;
  }

  /* see superclass */
  @Override
  public String getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public void setAdditionalRelationshipType(String addRelType) {
    this.additionalRelationshipType = addRelType;
  }

  /* see superclass */
  @Override
  public String getRank() {
    return rank;
  }

  /* see superclass */
  @Override
  public void setRank(String rank) {
    this.rank = rank;
  }

  /* see superclass */
  @Override
  public String getGroup() {
    return group;
  }

  /* see superclass */
  @Override
  public void setGroup(String group) {
    this.group = group;
  }

  /* see superclass */
  @Override
  public String getRule() {
    return rule;
  }

  /* see superclass */
  @Override
  public void setRule(String rule) {
    this.rule = rule;
  }

  /* see superclass */
  @Override
  public String getAdvice() {
    return advice;
  }

  /* see superclass */
  @Override
  public void setAdvice(String advice) {
    this.advice = advice;
  }

  /* see superclass */
  @XmlTransient
  @Override
  public MapSet getMapSet() {
    return mapSet;
  }

  /* see superclass */
  @Override
  public void setMapSet(MapSet mapSet) {
    this.mapSet = mapSet;
  }

  /**
   * Returns the map set id. For JAXB.
   *
   * @return the map set id
   */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public Long getMapSetId() {
    return mapSet == null ? null : mapSet.getId();
  }

  /**
   * Returns the map set terminology id.
   *
   * @return the map set terminology id
   */
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getMapSetTerminologyId() {
    return mapSet == null ? null : mapSet.getTerminologyId();
  }

  /**
   * Sets the map set id. For JAXB
   *
   * @param id the map set id
   */
  public void setMapSetId(Long id) {
    if (mapSet == null) {
      mapSet = new MapSetJpa();
    }
    mapSet.setId(id);
  }

  /**
   * Sets the map set terminology id.
   *
   * @param id the map set terminology id
   */
  public void setMapSetTerminologyId(String id) {
    if (mapSet == null) {
      mapSet = new MapSetJpa();
    }
    mapSet.setTerminologyId(id);
  }

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

  /* see superclass */
  @Override
  public void setAlternateTerminologyIds(
    Map<String, String> alternateTerminologyIds) {
    this.alternateTerminologyIds = alternateTerminologyIds;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((additionalRelationshipType == null) ? 0
        : additionalRelationshipType.hashCode());
    result = prime * result + ((advice == null) ? 0 : advice.hashCode());
    result =
        prime * result + ((fromIdType == null) ? 0 : fromIdType.hashCode());
    result = prime * result
        + ((fromTerminologyId == null) ? 0 : fromTerminologyId.hashCode());
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result
        + ((getMapSetId() == null) ? 0 : getMapSetId().hashCode());
    result = prime * result + ((getMapSetTerminologyId() == null) ? 0
        : getMapSetTerminologyId().hashCode());
    result = prime * result + ((rank == null) ? 0 : rank.hashCode());
    result = prime * result
        + ((relationshipType == null) ? 0 : relationshipType.hashCode());
    result = prime * result + ((rule == null) ? 0 : rule.hashCode());
    result = prime * result + ((toIdType == null) ? 0 : toIdType.hashCode());
    result = prime * result
        + ((toTerminologyId == null) ? 0 : toTerminologyId.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    MappingJpa other = (MappingJpa) obj;
    if (additionalRelationshipType == null) {
      if (other.additionalRelationshipType != null)
        return false;
    } else if (!additionalRelationshipType
        .equals(other.additionalRelationshipType))
      return false;
    if (advice == null) {
      if (other.advice != null)
        return false;
    } else if (!advice.equals(other.advice))
      return false;
    if (fromIdType != other.fromIdType)
      return false;
    if (fromTerminologyId == null) {
      if (other.fromTerminologyId != null)
        return false;
    } else if (!fromTerminologyId.equals(other.fromTerminologyId))
      return false;
    if (group == null) {
      if (other.group != null)
        return false;
    } else if (!group.equals(other.group))
      return false;

    if (getMapSetId() == null) {
      if (other.getMapSetId() != null)
        return false;
    } else if (!getMapSetId().equals(other.getMapSetId()))
      return false;
    if (getMapSetTerminologyId() == null) {
      if (other.getMapSetTerminologyId() != null)
        return false;
    } else if (!getMapSetTerminologyId().equals(other.getMapSetTerminologyId()))
      return false;

    if (rank == null) {
      if (other.rank != null)
        return false;
    } else if (!rank.equals(other.rank))
      return false;

    if (relationshipType == null) {
      if (other.relationshipType != null)
        return false;
    } else if (!relationshipType.equals(other.relationshipType))
      return false;
    if (rule == null) {
      if (other.rule != null)
        return false;
    } else if (!rule.equals(other.rule))
      return false;
    if (toIdType != other.toIdType)
      return false;
    if (toTerminologyId == null) {
      if (other.toTerminologyId != null)
        return false;
    } else if (!toTerminologyId.equals(other.toTerminologyId))
      return false;
    return true;
  }

  @Override
  // NOTE: Do not use autogenerated toString, as mapSet.toString references
  // mapping.toString
  public String toString() {
    return "MappingJpa [mapSet="
        + (mapSet == null ? "null" : mapSet.getTerminologyId())
        + ", fromTerminologyId=" + fromTerminologyId + ", toTerminologyId="
        + toTerminologyId + ", fromIdType=" + fromIdType + ", toIdType="
        + toIdType + ", advice=" + advice + ", rule=" + rule + ", group="
        + group + ", rank=" + rank + ", fromName=" + fromName + ", toName="
        + toName + ", relationshipType=" + relationshipType
        + ", additionalRelationshipType=" + additionalRelationshipType + "]";
  }

}
