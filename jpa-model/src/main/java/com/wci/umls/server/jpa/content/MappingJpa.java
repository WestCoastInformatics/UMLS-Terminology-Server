/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;

/**
 * JPA-enabled implementation of {@link Mapping}.
 */
@Entity
@Table(name = "mappings", uniqueConstraints = @UniqueConstraint(columnNames = {
    "fromTerminologyId", "toTerminologyId", "terminology", "version", "id"
}) )
@Audited
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
  private Long fromTerminologyId;

  /** The to terminology id. */
  @Column(nullable = false)
  private Long toTerminologyId;

  /** The from id type. */
  @Column(nullable = false)
  private String fromIdType;

  /** The to id type. */
  @Column(nullable = false)
  private String toIdType;

  /** The advice. */
  @Column(nullable = false, length = 4000)
  private String advice;

  /** The rule. */
  @Column(nullable = false, length = 4000)
  private String rule;

  /** The group. */
  @Column(name = "mapGroup", nullable = false)
  private String group;

  /** The rank. */
  @Column(nullable = false)
  private String rank;

  /** The relationship type. */
  @OneToOne(targetEntity = RelationshipTypeJpa.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
  private RelationshipType relationshipType;

  /** The additional relationship type. */
  @OneToOne(targetEntity = AdditionalRelationshipTypeJpa.class, cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
  private AdditionalRelationshipType additionalRelationshipType;

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
   * @param deepCopy the deep copy
   */
  public MappingJpa(Mapping mapping, boolean deepCopy) {
    super(mapping, deepCopy);
    mapSet = mapping.getMapSet();
    fromTerminologyId = mapping.getFromTerminologyId();
    toTerminologyId = mapping.getToTerminologyId();
    fromIdType = mapping.getFromIdType();
    toIdType = mapping.getToIdType();
    advice = mapping.getAdvice();
    rule = mapping.getRule();
    group = mapping.getGroup();
    rank = mapping.getRank();
    relationshipType = mapping.getRelationshipType();
    additionalRelationshipType = mapping.getAdditionalRelationshipType();
  }

  /* see superclass */
  @Override
  public String getFromIdType() {
    return fromIdType;
  }

  /* see superclass */
  @Override
  public void setFromIdType(String fromIdType) {
    this.fromIdType = fromIdType;
  }

  /* see superclass */
  @Override
  public String getToIdType() {
    return toIdType;
  }

  /* see superclass */
  @Override
  public void setToIdType(String toIdType) {
    this.toIdType = toIdType;
  }

  /* see superclass */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Long getFromTerminologyId() {
    return fromTerminologyId;
  }

  /* see superclass */
  @Override
  public void setFromTerminologyId(Long id) {
    this.fromTerminologyId = id;
  }

  /* see superclass */
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  @Override
  public Long getToTerminologyId() {
    return toTerminologyId;
  }

  /* see superclass */
  @Override
  public void setToTerminologyId(Long id) {
    this.toTerminologyId = id;
  }

  /* see superclass */
  @XmlElement(type = RelationshipTypeJpa.class)
  @Override
  public RelationshipType getRelationshipType() {
    return relationshipType;
  }

  /* see superclass */
  @Override
  public void setRelationshipType(RelationshipType relType) {
    this.relationshipType = relType;
  }

  /* see superclass */
  @XmlElement(type = AdditionalRelationshipTypeJpa.class)
  @Override
  public AdditionalRelationshipType getAdditionalRelationshipType() {
    return additionalRelationshipType;
  }

  /* see superclass */
  @Override
  public void setAdditionalRelationshipType(
    AdditionalRelationshipType addRelType) {
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
  public Long getMapSetId() {
    return mapSet == null ? null : mapSet.getId();
  }

  /**
   * Returns the map set terminology id.
   *
   * @return the map set terminology id
   */
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
    result = prime * result + ((mapSet == null) ? 0 : mapSet.hashCode());
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
    if (fromIdType == null) {
      if (other.fromIdType != null)
        return false;
    } else if (!fromIdType.equals(other.fromIdType))
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
    if (mapSet == null) {
      if (other.mapSet != null)
        return false;
    } else if (!mapSet.equals(other.mapSet))
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
    if (toIdType == null) {
      if (other.toIdType != null)
        return false;
    } else if (!toIdType.equals(other.toIdType))
      return false;
    if (toTerminologyId == null) {
      if (other.toTerminologyId != null)
        return false;
    } else if (!toTerminologyId.equals(other.toTerminologyId))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "MappingJpa [mapSet=" + mapSet + ", fromTerminologyId="
        + fromTerminologyId + ", toTerminologyId=" + toTerminologyId
        + ", fromIdType=" + fromIdType + ", toIdType=" + toIdType + ", advice="
        + advice + ", rule=" + rule + ", group=" + group + ", rank=" + rank
        + ", relationshipType=" + relationshipType
        + ", additionalRelationshipType=" + additionalRelationshipType + "]";
  }

}
