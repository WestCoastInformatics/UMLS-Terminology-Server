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

import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.TermTypeStyle;

/**
 * JPA-enabled implementation of {@link TermType}.
 */
@Entity
@Table(name = "term_types", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "termType")
public class TermTypeJpa extends AbstractAbbreviation implements TermType {

  /** The code variant type. */
  @Column(nullable = false)
  private String codeVariantType;

  /** The hierarchical type. */
  @Column(nullable = false)
  private String hierarchicalType;

  /** The name variant type. */
  @Column(nullable = false)
  private String nameVariantType;

  /** The obsolete. */
  @Column(nullable = false)
  private boolean obsolete = false;

  /** The suppressible flag. */
  @Column(nullable = false)
  private boolean suppressible = false;

  /** The style. */
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private TermTypeStyle style;

  /** The usage. */
  @Column(nullable = false)
  private String usage;

  /**
   * Instantiates an empty {@link TermTypeJpa}.
   */
  protected TermTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link TermTypeJpa} from the specified parameters.
   *
   * @param tty the tty
   */
  protected TermTypeJpa(TermType tty) {
    super(tty);
    codeVariantType = tty.getCodeVariantType();
    hierarchicalType = tty.getHierarchicalType();
    nameVariantType = tty.getNameVariantType();
    obsolete = tty.isObsolete();
    suppressible = tty.isSuppressible();
    style = tty.getStyle();
    usage = tty.getUsage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#isObsolete()
   */
  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#setObsolete(boolean)
   */
  @Override
  public void setObsolete(boolean obsolete) {
    this.obsolete = obsolete;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#isSuppressible()
   */
  @Override
  public boolean isSuppressible() {
    return suppressible;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#setSuppressible(boolean)
   */
  @Override
  public void setSuppressible(boolean suppressible) {
    this.suppressible = suppressible;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.TermType#setNameVariantType(java.lang.String
   * )
   */
  @Override
  public void setNameVariantType(String nameVariantType) {
    this.nameVariantType = nameVariantType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#getNameVariantType()
   */
  @Override
  public String getNameVariantType() {
    return nameVariantType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.TermType#setCodeVariantType(java.lang.String
   * )
   */
  @Override
  public void setCodeVariantType(String codeVariantType) {
    this.codeVariantType = codeVariantType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#getCodeVariantType()
   */
  @Override
  public String getCodeVariantType() {
    return codeVariantType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.TermType#setHierarchicalType(java.lang.String
   * )
   */
  @Override
  public void setHierarchicalType(String hierarchicalType) {
    this.hierarchicalType = hierarchicalType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#getHierarchicalType()
   */
  @Override
  public String getHierarchicalType() {
    return hierarchicalType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#setUsage(java.lang.String)
   */
  @Override
  public void setUsage(String usage) {
    this.usage = usage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#getUsage()
   */
  @Override
  public String getUsage() {
    return usage;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.TermType#setStyle(com.wci.umls.server.model
   * .meta.TermTypeStyle)
   */
  @Override
  public void setStyle(TermTypeStyle style) {
    this.style = style;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.TermType#getStyle()
   */
  @Override
  public TermTypeStyle getStyle() {
    return style;
  }

}
