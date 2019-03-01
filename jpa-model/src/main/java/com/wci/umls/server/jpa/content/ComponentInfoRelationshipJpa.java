/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
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
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.jpa.ComponentInfoJpa;
import com.wci.umls.server.jpa.helpers.MapKeyValueToCsvBridge;
import com.wci.umls.server.model.content.ComponentInfoRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA-enabled implementation of {@link ComponentInfoRelationship}.
 */
@Entity
@Table(name = "component_info_relationships", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "version", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "componentInfoRelationship")
public class ComponentInfoRelationshipJpa
    extends AbstractRelationship<ComponentInfo, ComponentInfo>
    implements ComponentInfoRelationship {

  /** The from terminology id. */
  private String fromTerminologyId;

  /** The from terminology. */
  private String fromTerminology;

  /** The from version. */
  private String fromVersion;

  /** The from name. */
  @Column(length = 4000)
  private String fromName;

  /** The from type. */
  @Enumerated(EnumType.STRING)
  private IdType fromType;

  /** The to terminology id. */
  private String toTerminologyId;

  /** The to terminology. */
  private String toTerminology;

  /** The to version. */
  private String toVersion;

  /** The to name. */
  @Column(length = 4000)  
  private String toName;

  /** The to type. */
  @Enumerated(EnumType.STRING)
  private IdType toType;

  /** The alternate terminology ids. */
  @ElementCollection(fetch = FetchType.EAGER)
  @Column(nullable = true)
  private Map<String, String> alternateTerminologyIds; // index

  /**
   * Instantiates an empty {@link ComponentInfoRelationshipJpa}.
   */
  public ComponentInfoRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link ComponentInfoRelationshipJpa} from the specified
   * parameters.
   *
   * @param relationship the relationship
   * @param collectionCopy the deep copy
   * @throws Exception the exception
   */
  public ComponentInfoRelationshipJpa(ComponentInfoRelationship relationship,
      boolean collectionCopy) throws Exception {
    super(relationship, collectionCopy);
    fromTerminologyId = relationship.getFrom().getTerminologyId();
    fromTerminology = relationship.getFrom().getTerminology();
    fromVersion = relationship.getFrom().getVersion();
    fromName = relationship.getFrom().getName();
    fromType = relationship.getFrom().getType();

    toTerminologyId = relationship.getTo().getTerminologyId();
    toTerminology = relationship.getTo().getTerminology();
    toVersion = relationship.getTo().getVersion();
    toName = relationship.getTo().getName();
    toType = relationship.getTo().getType();

    if (collectionCopy) {
      alternateTerminologyIds =
          new HashMap<>(relationship.getAlternateTerminologyIds());
    }
  }

  /* see superclass */
  @Override
  @XmlTransient
  public ComponentInfo getFrom() {
    final ComponentInfo info = new ComponentInfoJpa();
    info.setTerminology(fromTerminology);
    info.setVersion(fromVersion);
    info.setType(fromType);
    info.setTerminologyId(fromTerminologyId);
    info.setName(fromName);
    return info;
  }

  /* see superclass */
  @Override
  public void setFrom(ComponentInfo component) throws Exception {
    fromTerminology = component.getTerminology();
    fromVersion = component.getVersion();
    fromType = component.getType();
    fromTerminologyId = component.getTerminologyId();
    fromName = component.getName();
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminology() {
    return fromTerminology;
  }

  /* see superclass */
  @Override
  public void setFromTerminology(String terminology) {
    fromTerminology = terminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromVersion() {
    return fromVersion;
  }

  /* see superclass */
  @Override
  public void setFromVersion(String version) {
    fromVersion = version;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = EnumBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public IdType getFromType() {
    return fromType;
  }

  /* see superclass */
  @Override
  public void setFromType(IdType type) {
    fromType = type;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getFromTerminologyId() {
    return fromTerminologyId;
  }

  /* see superclass */
  @Override
  public void setFromTerminologyId(String terminologyId) {
    fromTerminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO, analyzer = @Analyzer(definition = "noStopWord")),
      @Field(name = "fromNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getFromName() {
    return fromName;
  }

  /* see superclass */
  @Override
  public void setFromName(String term) {
    fromName = term;
  }

  /* see superclass */
  @Override
  @XmlTransient
  public ComponentInfo getTo() {
    final ComponentInfo info = new ComponentInfoJpa();
    info.setTerminology(toTerminology);
    info.setVersion(toVersion);
    info.setType(toType);
    info.setTerminologyId(toTerminologyId);
    info.setName(toName);
    return info;
  }

  /* see superclass */
  @Override
  public void setTo(ComponentInfo component) throws Exception {
    toTerminology = component.getTerminology();
    toVersion = component.getVersion();
    toType = component.getType();
    toName = component.getName();
    toTerminologyId = component.getTerminologyId();
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminologyId() {
    return toTerminologyId;
  }

  /* see superclass */
  @Override
  public void setToTerminologyId(String terminologyId) {
    toTerminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToTerminology() {
    return toTerminology;
  }

  /* see superclass */
  @Override
  public void setToTerminology(String terminology) {
    toTerminology = terminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getToVersion() {
    return toVersion;
  }

  /* see superclass */
  @Override
  public void setToVersion(String version) {
    toVersion = version;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = EnumBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public IdType getToType() {
    return toType;
  }

  /* see superclass */
  @Override
  public void setToType(IdType type) {
    toType = type;
  }

  /* see superclass */
  @Override
  @Fields({
      @Field(index = Index.YES, analyze = Analyze.YES, store = Store.NO),
      @Field(name = "toNameSort", index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  })
  public String getToName() {
    return toName;
  }

  /* see superclass */
  @Override
  public void setToName(String term) {
    toName = term;
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
    result = prime * result
        + ((fromTerminology == null) ? 0 : fromTerminology.hashCode());
    result = prime * result
        + ((fromTerminologyId == null) ? 0 : fromTerminologyId.hashCode());
    result = prime * result + ((fromType == null) ? 0 : fromType.hashCode());
    result =
        prime * result + ((fromVersion == null) ? 0 : fromVersion.hashCode());
    result = prime * result
        + ((toTerminology == null) ? 0 : toTerminology.hashCode());
    result = prime * result
        + ((toTerminologyId == null) ? 0 : toTerminologyId.hashCode());
    result = prime * result + ((toType == null) ? 0 : toType.hashCode());
    result = prime * result + ((toVersion == null) ? 0 : toVersion.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public Relationship<ComponentInfo, ComponentInfo> createInverseRelationship(
    Relationship<ComponentInfo, ComponentInfo> relationship,
    String inverseRelType, String inverseAdditionalRelType) throws Exception {
    final ComponentInfoRelationship inverseRelationship =
        new ComponentInfoRelationshipJpa(
            (ComponentInfoRelationship) relationship, false);

    return populateInverseRelationship(relationship, inverseRelationship,
        inverseRelType, inverseAdditionalRelType);
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
    ComponentInfoRelationshipJpa other = (ComponentInfoRelationshipJpa) obj;
    if (fromTerminology == null) {
      if (other.fromTerminology != null)
        return false;
    } else if (!fromTerminology.equals(other.fromTerminology))
      return false;
    if (fromTerminologyId == null) {
      if (other.fromTerminologyId != null)
        return false;
    } else if (!fromTerminologyId.equals(other.fromTerminologyId))
      return false;
    if (fromType != other.fromType)
      return false;
    if (fromVersion == null) {
      if (other.fromVersion != null)
        return false;
    } else if (!fromVersion.equals(other.fromVersion))
      return false;
    if (toTerminology == null) {
      if (other.toTerminology != null)
        return false;
    } else if (!toTerminology.equals(other.toTerminology))
      return false;
    if (toTerminologyId == null) {
      if (other.toTerminologyId != null)
        return false;
    } else if (!toTerminologyId.equals(other.toTerminologyId))
      return false;
    if (toType != other.toType)
      return false;
    if (toVersion == null) {
      if (other.toVersion != null)
        return false;
    } else if (!toVersion.equals(other.toVersion))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "ComponentInfoRelationshipJpa [" + "getFrom()=" + getFrom()
        + ", getFromTerminology()=" + getFromTerminology()
        + ", getFromVersion()=" + getFromVersion() + ", getFromTerminologyId()="
        + getFromTerminologyId() + ", getFromName()=" + getFromName()
        + ", getTo()=" + getTo() + ", getToTerminologyId()="
        + getToTerminologyId() + ", getToTerminology()=" + getToTerminology()
        + ", getToVersion()=" + getToVersion() + ", getToName()=" + getToName()
        + ", getAlternateTerminologyIds()=" + getAlternateTerminologyIds()
        + ", hashCode()=" + hashCode() + ", getRelationshipType()="
        + getRelationshipType() + ", getAdditionalRelationshipType()="
        + getAdditionalRelationshipType() + ", getGroup()=" + getGroup()
        + ", isInferred()=" + isInferred() + ", isStated()=" + isStated()
        + ", isHierarchical()=" + isHierarchical() + ", isAssertedDirection()="
        + isAssertedDirection() + ", toString()=" + super.toString()
        + ", getAttributes()=" + getAttributes() + ", getId()=" + getId()
        + ", getTimestamp()=" + getTimestamp() + ", getLastModified()="
        + getLastModified() + ", getLastModifiedBy()=" + getLastModifiedBy()
        + ", isSuppressible()=" + isSuppressible() + ", isObsolete()="
        + isObsolete() + ", isPublished()=" + isPublished()
        + ", isPublishable()=" + isPublishable() + ", getBranch()="
        + getBranch() + ", getVersion()=" + getVersion() + ", getTerminology()="
        + getTerminology() + ", getTerminologyId()=" + getTerminologyId()
        + ", getClass()=" + getClass() + "]";
  }

}
