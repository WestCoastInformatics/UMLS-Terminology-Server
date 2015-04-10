/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.SemanticType;

/**
 * JPA-enabled implementation of {@link SemanticType}.
 */
@Entity
@Table(name = "semantic_types", uniqueConstraints = @UniqueConstraint(columnNames = {
  "value"
}))
@Audited
@XmlRootElement(name = "semanticType")
public class SemanticTypeJpa extends AbstractAbbreviation implements
    SemanticType {

  /** The value. */
  @Column(nullable = false)
  private String value;

  /** The definition. */
  @Column(nullable = false, length = 4000)
  private String definition;

  /** The example. */
  @Column(nullable = true, length = 4000)
  private String example;

  /** The type id. */
  @Column(nullable = false)
  private String typeId;

  /** The non human. */
  @Column(nullable = false)
  private boolean nonHuman;

  /** The tree number. */
  @Column(nullable = true, length = 4000)
  private String treeNumber;

  /** The usage note. */
  @Column(nullable = true, length = 4000)
  private String usageNote;

  /**
   * Instantiates an empty {@link SemanticTypeJpa}.
   */
  public SemanticTypeJpa() {
    super();
    setExpandedForm("n/a");
  }

  /**
   * Instantiates a {@link SemanticTypeJpa} from the specified parameters.
   *
   * @param sty the sty
   */
  public SemanticTypeJpa(SemanticType sty) {
    super(sty);
    definition = sty.getDefinition();
    example = sty.getExample();
    treeNumber = sty.getTreeNumber();
    typeId = sty.getTypeId();
    usageNote = sty.getUsageNote();
    value = sty.getValue();
    nonHuman = sty.isNonHuman();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#getValue()
   */
  @Override
  public String getValue() {
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#setValue(java.lang.String)
   */
  @Override
  public void setValue(String value) {
    this.value = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#getDefinition()
   */
  @Override
  public String getDefinition() {
    return definition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.SemanticType#setDefinition(java.lang.String)
   */
  @Override
  public void setDefinition(String definition) {
    this.definition = definition;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#getExample()
   */
  @Override
  public String getExample() {
    return example;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.SemanticType#setExample(java.lang.String)
   */
  @Override
  public void setExample(String example) {
    this.example = example;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#getTypeId()
   */
  @Override
  public String getTypeId() {
    return typeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.SemanticType#setTypeId(java.lang.String)
   */
  @Override
  public void setTypeId(String typeId) {
    this.typeId = typeId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#isNonHuman()
   */
  @Override
  public boolean isNonHuman() {
    return nonHuman;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#setNonHuman(boolean)
   */
  @Override
  public void setNonHuman(boolean nonHuman) {
    this.nonHuman = nonHuman;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#getTreeNumber()
   */
  @Override
  public String getTreeNumber() {
    return treeNumber;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.SemanticType#setTreeNumber(java.lang.String)
   */
  @Override
  public void setTreeNumber(String treeNumber) {
    this.treeNumber = treeNumber;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.SemanticType#getUsageNote()
   */
  @Override
  public String getUsageNote() {
    return usageNote;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.SemanticType#setUsageNote(java.lang.String)
   */
  @Override
  public void setUsageNote(String usageNote) {
    this.usageNote = usageNote;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((definition == null) ? 0 : definition.hashCode());
    result = prime * result + ((example == null) ? 0 : example.hashCode());
    result = prime * result + (nonHuman ? 1231 : 1237);
    result =
        prime * result + ((treeNumber == null) ? 0 : treeNumber.hashCode());
    result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
    result = prime * result + ((usageNote == null) ? 0 : usageNote.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SemanticTypeJpa other = (SemanticTypeJpa) obj;
    if (definition == null) {
      if (other.definition != null)
        return false;
    } else if (!definition.equals(other.definition))
      return false;
    if (example == null) {
      if (other.example != null)
        return false;
    } else if (!example.equals(other.example))
      return false;
    if (nonHuman != other.nonHuman)
      return false;
    if (treeNumber == null) {
      if (other.treeNumber != null)
        return false;
    } else if (!treeNumber.equals(other.treeNumber))
      return false;
    if (typeId == null) {
      if (other.typeId != null)
        return false;
    } else if (!typeId.equals(other.typeId))
      return false;
    if (usageNote == null) {
      if (other.usageNote != null)
        return false;
    } else if (!usageNote.equals(other.usageNote))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SemanticTypeJpa [value=" + value + ", definition=" + definition
        + ", example=" + example + ", typeId=" + typeId + ", nonHuman="
        + nonHuman + ", treeNumber=" + treeNumber + ", usageNote=" + usageNote
        + "]";
  }
}
