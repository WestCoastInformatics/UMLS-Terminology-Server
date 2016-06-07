/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;
import com.wci.umls.server.model.meta.UsageType;

/**
 * JPA and JAXB enabled implementation of {@link TermType}.
 */
@Entity
@Table(name = "term_types", uniqueConstraints = @UniqueConstraint(columnNames = {
    "abbreviation", "terminology"
}))
@Audited
@XmlRootElement(name = "termType")
public class TermTypeJpa extends AbstractAbbreviation implements TermType {

  /** The code variant type. */
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private CodeVariantType codeVariantType;

  /** The hierarchical type. */
  @Column(nullable = false)
  private boolean hierarchicalType;

  /** The name variant type. */
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private NameVariantType nameVariantType;

  /** The obsolete. */
  @Column(nullable = false)
  private boolean obsolete = false;

  /** The suppressible flag. */
  @Column(nullable = false)
  private boolean suppressible = false;

  /** The style. */
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private TermTypeStyle style;

  /** The usage type. */
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private UsageType usageType;

  /**
   * Instantiates an empty {@link TermTypeJpa}.
   */
  public TermTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TermTypeJpa} from the specified parameters.
   *
   * @param tty the tty
   */
  public TermTypeJpa(TermType tty) {
    super(tty);
    codeVariantType = tty.getCodeVariantType();
    hierarchicalType = tty.isHierarchicalType();
    nameVariantType = tty.getNameVariantType();
    obsolete = tty.isObsolete();
    suppressible = tty.isSuppressible();
    style = tty.getStyle();
    usageType = tty.getUsageType();
  }

  /* see superclass */
  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  /* see superclass */
  @Override
  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  /* see superclass */
  @Override
  public boolean isSuppressible() {
    return suppressible;
  }

  /* see superclass */
  @Override
  public void setSuppressible(boolean suppressible) {
    this.suppressible = suppressible;
  }

  /* see superclass */
  @Override
  public void setNameVariantType(NameVariantType nameVariantType) {
    this.nameVariantType = nameVariantType;
  }

  /* see superclass */
  @Override
  public NameVariantType getNameVariantType() {
    return nameVariantType;
  }

  /* see superclass */
  @Override
  public void setCodeVariantType(CodeVariantType codeVariantType) {
    this.codeVariantType = codeVariantType;
  }

  /* see superclass */
  @Override
  public CodeVariantType getCodeVariantType() {
    return codeVariantType;
  }

  /* see superclass */
  @Override
  public void setHierarchicalType(boolean hierarchicalType) {
    this.hierarchicalType = hierarchicalType;
  }

  /* see superclass */
  @Override
  public boolean isHierarchicalType() {
    return hierarchicalType;
  }

  /* see superclass */
  @Override
  public void setUsageType(UsageType usageType) {
    this.usageType = usageType;
  }

  /* see superclass */
  @Override
  public UsageType getUsageType() {
    return usageType;
  }

  /* see superclass */
  @Override
  public void setStyle(TermTypeStyle style) {
    this.style = style;
  }

  /* see superclass */
  @Override
  public TermTypeStyle getStyle() {
    return style;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((codeVariantType == null) ? 0 : codeVariantType.hashCode());
    result = prime * result + (hierarchicalType ? 1231 : 1237);
    result =
        prime * result
            + ((nameVariantType == null) ? 0 : nameVariantType.hashCode());
    result = prime * result + (obsolete ? 1231 : 1237);
    result = prime * result + ((style == null) ? 0 : style.hashCode());
    result = prime * result + (suppressible ? 1231 : 1237);
    result = prime * result + ((usageType == null) ? 0 : usageType.hashCode());
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
    TermTypeJpa other = (TermTypeJpa) obj;
    if (codeVariantType != other.codeVariantType)
      return false;
    if (hierarchicalType != other.hierarchicalType)
      return false;
    if (nameVariantType != other.nameVariantType)
      return false;
    if (obsolete != other.obsolete)
      return false;
    if (style != other.style)
      return false;
    if (suppressible != other.suppressible)
      return false;
    if (usageType != other.usageType)
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TermTypeJpa [" + super.toString() + "codeVariantType="
        + codeVariantType + ", hierarchicalType=" + hierarchicalType
        + ", nameVariantType=" + nameVariantType + ", obsolete=" + obsolete
        + ", suppressible=" + suppressible + ", style=" + style
        + ", usageType=" + usageType + "]";
  }
}
