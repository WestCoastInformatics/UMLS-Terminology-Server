/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.bridge.builtin.EnumBridge;
import org.hibernate.search.bridge.builtin.LongBridge;

import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.IdType;

/**
 * JPA and JAXB enabled implementation of {@link AttributeIdentity}.
 */
@Entity
@Table(name = "attribute_identity", uniqueConstraints = @UniqueConstraint(columnNames = {
    "componentId", "componentTerminology", "componentType", "id"
}))
@XmlRootElement(name = "attributeIdentity")
@Indexed
public class AttributeIdentityJpa implements AttributeIdentity {

  /** The id. */
  @Id
  private Long id;

  /** The attribute name. */
  @Column(nullable = false)
  private String name;

  /** The terminology id. */
  @Column(nullable = false)
  private String terminologyId;

  /** The terminology. */
  @Column(nullable = false)
  private String terminology;

  /** The component id. */
  @Column(nullable = false)
  private String componentId;

  /** The component type. */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private IdType componentType;

  /** The component terminology. */
  @Column(nullable = false)
  private String componentTerminology;

  /** The attribute value hash code. */
  @Column(nullable = false)
  private String hashcode;

  /**
   * Instantiates an empty {@link AttributeIdentityJpa}.
   */
  public AttributeIdentityJpa() {
    //
  }

  /**
   * Instantiates a {@link AttributeIdentityJpa} from the specified parameters.
   *
   * @param identity the identity
   */
  public AttributeIdentityJpa(AttributeIdentity identity) {
    id = identity.getId();
    name = identity.getName();
    hashcode = identity.getHashcode();
    terminologyId = identity.getTerminologyId();
    terminology = identity.getTerminology();
    componentId = identity.getComponentId();
    componentType = identity.getComponentType();
    componentTerminology = identity.getComponentTerminology();
    hashcode = identity.getHashcode();
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = LongBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.YES)
  public Long getId() {
    return id;
  }

  /* see superclass */
  @Override
  public void setId(Long id) {
    this.id = id;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminologyId() {
    return terminologyId;
  }

  /* see superclass */
  @Override
  public void setTerminologyId(String terminologyId) {
    this.terminologyId = terminologyId;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getTerminology() {
    return terminology;
  }

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getComponentId() {
    return componentId;
  }

  /* see superclass */
  @Override
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /* see superclass */
  @Override
  @FieldBridge(impl = EnumBridge.class)
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public IdType getComponentType() {
    return componentType;
  }

  /* see superclass */
  @Override
  public void setComponentType(IdType componentType) {
    this.componentType = componentType;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getComponentTerminology() {
    return componentTerminology;
  }

  /* see superclass */
  @Override
  public void setComponentTerminology(String componentTerminology) {
    this.componentTerminology = componentTerminology;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
  public String getHashcode() {
    return hashcode;
  }

  /* see superclass */
  @Override
  public void setHashcode(String hashcode) {
    this.hashcode = hashcode;
  }

  /* see superclass */
  @Override
  @Field(index = Index.YES, analyze = Analyze.NO, store = Store.NO)
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((componentId == null) ? 0 : componentId.hashCode());
    result = prime * result + ((componentTerminology == null) ? 0
        : componentTerminology.hashCode());
    result = prime * result
        + ((componentType == null) ? 0 : componentType.hashCode());
    result = prime * result + ((hashcode == null) ? 0 : hashcode.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result =
        prime * result + ((terminology == null) ? 0 : terminology.hashCode());
    result = prime * result
        + ((terminologyId == null) ? 0 : terminologyId.hashCode());
    result = prime * result + ((hashcode == null) ? 0 : hashcode.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AttributeIdentityJpa other = (AttributeIdentityJpa) obj;
    if (componentId == null) {
      if (other.componentId != null)
        return false;
    } else if (!componentId.equals(other.componentId))
      return false;
    if (componentTerminology == null) {
      if (other.componentTerminology != null)
        return false;
    } else if (!componentTerminology.equals(other.componentTerminology))
      return false;
    if (componentType != other.componentType)
      return false;
    if (hashcode == null) {
      if (other.hashcode != null)
        return false;
    } else if (!hashcode.equals(other.hashcode))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    if (terminology == null) {
      if (other.terminology != null)
        return false;
    } else if (!terminology.equals(other.terminology))
      return false;
    if (terminologyId == null) {
      if (other.terminologyId != null)
        return false;
    } else if (!terminologyId.equals(other.terminologyId))
      return false;
    if (hashcode == null) {
      if (other.hashcode != null)
        return false;
    } else if (!hashcode.equals(other.hashcode))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "AttributeIdentityJpa [id=" + id + ", name=" + name
        + ", terminologyId=" + terminologyId + ", terminology=" + terminology
        + ", componentId=" + componentId + ", componentType=" + componentType
        + ", componentTerminology=" + componentTerminology + "]";
  }

}
