/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;



/**
 * JPA and JAXB enabled implementation of a {@link MapSet}.
 */
@Entity
@Table(name = "mapsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "mapSet")
public class MapSetJpa extends AbstractComponentHasAttributes implements MapSet {

  /** The mappings. */
  @OneToMany(mappedBy = "mapSet", targetEntity = MappingJpa.class)
  private List<Mapping> mappings = null;

  /** The name. */
  @Column(nullable = false)
  private String name;

  /** The from complexity. */
  @Column(nullable = true)
  private String fromComplexity;

  /** The to complexity. */
  @Column(nullable = true)
  private String toComplexity;

  /** The complexity. */
  @Column(nullable = true)
  private String complexity;

  /** The from exhaustive. */
  @Column(nullable = true)
  private String fromExhaustive;

  /** The to exhaustive. */
  @Column(nullable = true)
  private String toExhaustive;

  /** The type. */
  @Column(nullable = true)
  private String type;

  /** The from terminology. */
  @Column(nullable = false)
  private String fromTerminology;

  /** The to terminology. */
  @Column(nullable = true)
  private String toTerminology;

  /** The from version. */
  @Column(nullable = true)
  private String fromVersion;

  /** The to version. */
  @Column(nullable = true)
  private String toVersion;

  /**
   * Instantiates an empty {@link MapSetJpa}.
   */
  public MapSetJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link MapSetJpa} from the specified parameters.
   *
   * @param mapSet the map set
   * @param deepCopy the deep copy
   */
  public MapSetJpa(MapSet mapSet, boolean deepCopy) {
    super(mapSet, deepCopy);
    name = mapSet.getName();
    fromComplexity = mapSet.getFromComplexity();
    toComplexity = mapSet.getToComplexity();
    complexity = mapSet.getComplexity();
    fromExhaustive = mapSet.getFromExhaustive();
    toExhaustive = mapSet.getToExhaustive();
    type = mapSet.getType();
    fromTerminology = mapSet.getFromTerminology();
    toTerminology = mapSet.getToTerminology();
    fromVersion = mapSet.getFromVersion();
    toVersion = mapSet.getToVersion();
    if (deepCopy) {
      for (final Mapping mapping : mapSet.getMappings()) {
        getMappings().add((Mapping) new MappingJpa(mapping, deepCopy));
      }
    }

  }

  /* see superclass */
  @Override
  @Fields({
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO),
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  public String getName() {
    return name;
  }

  /* see superclass */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /* see superclass */
  @Override
  public String getFromComplexity() {
    return fromComplexity;
  }

  /* see superclass */
  @Override
  public void setFromComplexity(String fromComplexity) {
    this.fromComplexity = fromComplexity;
  }

  /* see superclass */
  @Override
  public String getToComplexity() {
    return toComplexity;
  }

  /* see superclass */
  @Override
  public void setToComplexity(String toComplexity) {
    this.toComplexity = toComplexity;
  }

  /* see superclass */
  @Override
  public String getToExhaustive() {
    return toExhaustive;
  }

  /* see superclass */
  @Override
  public String getFromExhaustive() {
    return fromExhaustive;
  }

  /* see superclass */
  @Override
  public void setToExhaustive(String toExhaustive) {
    this.toExhaustive = toExhaustive;
  }

  /* see superclass */
  @Override
  public void setFromExhaustive(String fromExhaustive) {
    this.fromExhaustive = fromExhaustive;
  }

  /* see superclass */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /* see superclass */
  @Override
  public String getType() {
    return type;
  }

  /* see superclass */
  @XmlElement(type = MappingJpa.class)
  @Override
  public List<Mapping> getMappings() {
    if (mappings == null) {
      return new ArrayList<>();
    }
    return mappings;
  }

  /* see superclass */
  @Override
  public void setMappings(List<Mapping> mappings) {
    this.mappings = mappings;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminology() {
    return fromTerminology;
  }

  /* see superclass */
  @Override
  public void setFromTerminology(String fromTerminology) {
    this.fromTerminology = fromTerminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminology() {
    return toTerminology;
  }

  /* see superclass */
  @Override
  public void setToTerminology(String toTerminology) {
    this.toTerminology = toTerminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromVersion() {
    return fromVersion;
  }

  /* see superclass */
  @Override
  public void setFromVersion(String fromVersion) {
    this.fromVersion = fromVersion;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToVersion() {
    return toVersion;
  }

  /* see superclass */
  @Override
  public void setToVersion(String toVersion) {
    this.toVersion = toVersion;
  }

  /* see superclass */
  @Override
  public String getComplexity() {
    return complexity;
  }

  /* see superclass */
  @Override
  public void setComplexity(String complexity) {
    this.complexity = complexity;
  }

  /* see superclass */
  @Override
  public void clearMappings() {
    mappings = new ArrayList<>();
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((complexity == null) ? 0 : complexity.hashCode());
    result =
        prime * result
            + ((fromComplexity == null) ? 0 : fromComplexity.hashCode());
    result =
        prime * result
            + ((fromExhaustive == null) ? 0 : fromExhaustive.hashCode());
    result =
        prime * result
            + ((fromTerminology == null) ? 0 : fromTerminology.hashCode());
    result =
        prime * result + ((fromVersion == null) ? 0 : fromVersion.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((toComplexity == null) ? 0 : toComplexity.hashCode());
    result =
        prime * result + ((toExhaustive == null) ? 0 : toExhaustive.hashCode());
    result =
        prime * result
            + ((toTerminology == null) ? 0 : toTerminology.hashCode());
    result = prime * result + ((toVersion == null) ? 0 : toVersion.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    MapSetJpa other = (MapSetJpa) obj;
    if (complexity == null) {
      if (other.complexity != null)
        return false;
    } else if (!complexity.equals(other.complexity))
      return false;
    if (fromComplexity == null) {
      if (other.fromComplexity != null)
        return false;
    } else if (!fromComplexity.equals(other.fromComplexity))
      return false;
    if (fromExhaustive == null) {
      if (other.fromExhaustive != null)
        return false;
    } else if (!fromExhaustive.equals(other.fromExhaustive))
      return false;
    if (fromTerminology == null) {
      if (other.fromTerminology != null)
        return false;
    } else if (!fromTerminology.equals(other.fromTerminology))
      return false;
    if (fromVersion == null) {
      if (other.fromVersion != null)
        return false;
    } else if (!fromVersion.equals(other.fromVersion))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (toComplexity == null) {
      if (other.toComplexity != null)
        return false;
    } else if (!toComplexity.equals(other.toComplexity))
      return false;
    if (toExhaustive == null) {
      if (other.toExhaustive != null)
        return false;
    } else if (!toExhaustive.equals(other.toExhaustive))
      return false;
    if (toTerminology == null) {
      if (other.toTerminology != null)
        return false;
    } else if (!toTerminology.equals(other.toTerminology))
      return false;
    if (toVersion == null) {
      if (other.toVersion != null)
        return false;
    } else if (!toVersion.equals(other.toVersion))
      return false;
    if (type == null) {
      if (other.type != null)
        return false;
    } else if (!type.equals(other.type))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "MapSetJpa [mappings=" + mappings + ", name=" + name
        + ", fromComplexity=" + fromComplexity + ", toComplexity="
        + toComplexity + ", fromExhaustive=" + fromExhaustive
        + ", toExhaustive=" + toExhaustive + ", type=" + type
        + ", fromTerminology=" + fromTerminology + ", toTerminology="
        + toTerminology + ", fromVersion=" + fromVersion + ", toVersion="
        + toVersion + ", complexity=" + complexity + "]";
  }

}
