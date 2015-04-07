package com.wci.umls.server.jpa.meta;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.RelationshipType;

/**
 * JPA-enabled implementation of {@link RelationshipType}.
 */
@Entity
@Table(name = "relationship_types", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "relationshipType")
public class RelationshipTypeJpa extends AbstractAbbreviation
    implements RelationshipType {

  /** The concept. */
  @OneToOne(targetEntity = RelationshipTypeJpa.class, optional = false)
  private RelationshipType inverse;

  /**
   * Instantiates an empty {@link RelationshipTypeJpa}.
   */
  protected RelationshipTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link RelationshipTypeJpa} from the specified
   * parameters.
   *
   * @param rela the rela
   */
  protected RelationshipTypeJpa(RelationshipType rela) {
    super(rela);
    inverse = rela.getInverse();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.RelationshipType#getInverse()
   */
  @Override
  public RelationshipType getInverse() {
    return inverse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.RelationshipType#setInverse(com
   * .wci.umls.server.model.meta.RelationshipType)
   */
  @Override
  public void setInverse(RelationshipType inverse) {
    this.inverse = inverse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.meta.AbstractAbbreviation#hashCode()
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.meta.AbstractAbbreviation#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  // TODO
  // isreflexive, is transitive, is functional, etc.
  // domain/range
  // is non-grouping
  // property chains.

}
