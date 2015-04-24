/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeTransitiveRelationship;

/**
 * JPA-enabled implementation of {@link CodeTransitiveRelationship}.
 */
@Entity
@Table(name = "code_transitive_rels", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "codeTransitiveRel")
public class CodeTransitiveRelationshipJpa extends AbstractComponentHasAttributes
    implements CodeTransitiveRelationship {

  /** The super type. */
  @ManyToOne(targetEntity = CodeJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Code superType;

  /** The sub type. */
  @ManyToOne(targetEntity = CodeJpa.class, optional = false)
  @JoinColumn(nullable = true)
  private Code subType;

  /**
   * Instantiates an empty {@link CodeTransitiveRelationshipJpa}.
   */
  public CodeTransitiveRelationshipJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link CodeTransitiveRelationshipJpa} from the specified
   * parameters.
   *
   * @param relationship the relationship
   * @param deepCopy the deep copy
   */
  public CodeTransitiveRelationshipJpa(
    CodeTransitiveRelationship relationship, boolean deepCopy) {
    super(relationship, deepCopy);
    superType = relationship.getSuperType();
    subType = relationship.getSubType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TransitiveRelationship#getSuperType()
   */
  @Override
  public Code getSuperType() {
    return superType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TransitiveRelationship#setSuperType(com
   * .wci.umls.server.model.content.AtomClass)
   */
  @Override
  public void setSuperType(Code ancestor) {
    this.superType = ancestor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.content.TransitiveRelationship#getSubType()
   */
  @Override
  public Code getSubType() {
    return subType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.content.TransitiveRelationship#setSubType(com
   * .wci.umls.server.model.content.AtomClass)
   */
  @Override
  public void setSubType(Code descendant) {
    this.subType = descendant;
  }

}
