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
 * JPA-enabled implementation of a {@link MapSet}.
 */
@Entity
@Table(name = "mapsets", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "id"
}) )
@Audited
@Indexed
@XmlRootElement(name = "mapSet")
public class MapSetJpa extends AbstractComponentHasAttributes
    implements MapSet {

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
  @Column(nullable = false)
  private String toTerminology;

  /** The from version. */
  @Column(nullable = true)
  private String fromVersion;

  /** The to version. */
  @Column(nullable = true)
  private String toVersion;

  /** The map version. */
  @Column(nullable = false)
  private String mapVersion;

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
    mapVersion = mapSet.getMapVersion();
    if (deepCopy) {
      for (Mapping mapping : mapSet.getMappings()) {
        addMapping(new MappingJpa(mapping, deepCopy));
      }
    }

  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  @Fields({
      @Field(name = "nameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO),
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO)
  })
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the new name
   */
  @Override
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the from complexity.
   *
   * @return the from complexity
   */
  @Override
  public String getFromComplexity() {
    return fromComplexity;
  }

  /**
   * Sets the from complexity.
   *
   * @param fromComplexity the new from complexity
   */
  @Override
  public void setFromComplexity(String fromComplexity) {
    this.fromComplexity = fromComplexity;
  }

  /**
   * Gets the to complexity.
   *
   * @return the to complexity
   */
  @Override
  public String getToComplexity() {
    return toComplexity;
  }

  /**
   * Sets the to complexity.
   *
   * @param toComplexity the new to complexity
   */
  @Override
  public void setToComplexity(String toComplexity) {
    this.toComplexity = toComplexity;
  }

  /**
   * Gets the to exhaustive.
   *
   * @return the to exhaustive
   */
  @Override
  public String getToExhaustive() {
    return toExhaustive;
  }

  /**
   * Gets the from exhaustive.
   *
   * @return the from exhaustive
   */
  @Override
  public String getFromExhaustive() {
    return fromExhaustive;
  }

  /**
   * Sets the to exhaustive.
   *
   * @param toExhaustive the new to exhaustive
   */
  @Override
  public void setToExhaustive(String toExhaustive) {
    this.toExhaustive = toExhaustive;
  }

  /**
   * Sets the from exhaustive.
   *
   * @param fromExhaustive the new from exhaustive
   */
  @Override
  public void setFromExhaustive(String fromExhaustive) {
    this.fromExhaustive = fromExhaustive;
  }

  /**
   * Sets the type.
   *
   * @param type the new type
   */
  @Override
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * Gets the mappings.
   *
   * @return the mappings
   */
  @XmlElement(type = MappingJpa.class)
  @Override
  public List<Mapping> getMappings() {
    if (mappings == null) {
      return new ArrayList<>();
    }
    return mappings;
  }

  /**
   * Sets the mappings.
   *
   * @param mappings the new mappings
   */
  @Override
  public void setMappings(List<Mapping> mappings) {
    this.mappings = mappings;
  }

  /**
   * Adds the mapping.
   *
   * @param mapping the mapping
   */
  @Override
  public void addMapping(Mapping mapping) {
    if (mappings == null) {
      mappings = new ArrayList<>();
    }
    mappings.add(mapping);
  }

  /**
   * Removes the mapping.
   *
   * @param mapping the mapping
   */
  @Override
  public void removeMapping(Mapping mapping) {
    if (mappings == null) {
      mappings = new ArrayList<>();
    }
    mappings.remove(mapping);
  }

  /**
   * Gets the from terminology.
   *
   * @return the from terminology
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminology() {
    return fromTerminology;
  }

  /**
   * Sets the from terminology.
   *
   * @param fromTerminology the new from terminology
   */
  @Override
  public void setFromTerminology(String fromTerminology) {
    this.fromTerminology = fromTerminology;
  }

  /**
   * Gets the to terminology.
   *
   * @return the to terminology
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminology() {
    return toTerminology;
  }

  /**
   * Sets the to terminology.
   *
   * @param toTerminology the new to terminology
   */
  @Override
  public void setToTerminology(String toTerminology) {
    this.toTerminology = toTerminology;
  }

  /**
   * Gets the from version.
   *
   * @return the from version
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromVersion() {
    return fromVersion;
  }

  /**
   * Sets the from version.
   *
   * @param fromVersion the new from version
   */
  @Override
  public void setFromVersion(String fromVersion) {
    this.fromVersion = fromVersion;
  }

  /**
   * Gets the to version.
   *
   * @return the to version
   */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToVersion() {
    return toVersion;
  }

  /**
   * Sets the to version.
   *
   * @param toVersion the new to version
   */
  @Override
  public void setToVersion(String toVersion) {
    this.toVersion = toVersion;
  }

  /**
   * Gets the complexity.
   *
   * @return the complexity
   */
  @Override
  public String getComplexity() {
    return complexity;
  }

  /**
   * Sets the complexity.
   *
   * @param complexity the new complexity
   */
  @Override
  public void setComplexity(String complexity) {
    this.complexity = complexity;
  }

  /**
   * Gets the map version.
   *
   * @return the map version
   */
  @Override
  public String getMapVersion() {
    return mapVersion;
  }

  /**
   * Sets the map version.
   *
   * @param mapVersion the new map version
   */
  @Override
  public void setMapVersion(String mapVersion) {
    this.mapVersion = mapVersion;
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
    result = prime * result
        + ((fromComplexity == null) ? 0 : fromComplexity.hashCode());
    result = prime * result
        + ((fromExhaustive == null) ? 0 : fromExhaustive.hashCode());
    result = prime * result
        + ((fromTerminology == null) ? 0 : fromTerminology.hashCode());
    result =
        prime * result + ((fromVersion == null) ? 0 : fromVersion.hashCode());
    result =
        prime * result + ((mapVersion == null) ? 0 : mapVersion.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((toComplexity == null) ? 0 : toComplexity.hashCode());
    result =
        prime * result + ((toExhaustive == null) ? 0 : toExhaustive.hashCode());
    result = prime * result
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
    if (mapVersion == null) {
      if (other.mapVersion != null)
        return false;
    } else if (!mapVersion.equals(other.mapVersion))
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
        + toVersion + ", complexity=" + complexity + ", mapVersion="
        + mapVersion + "]";
  }

}
