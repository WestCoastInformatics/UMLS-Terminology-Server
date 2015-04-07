package com.wci.umls.server.jpa.meta;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.AdditionalRelationshipType;

/**
 * JPA-enabled implementation of {@link AdditionalRelationshipType}.
 */
@Entity
@Table(name = "additional_relationship_types", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "additionalRelationshipType")
public class AdditionalRelationshipTypeJpa extends AbstractAbbreviation
    implements AdditionalRelationshipType {

  /** The concept. */
  @OneToOne(targetEntity = AdditionalRelationshipTypeJpa.class, optional = false)
  private AdditionalRelationshipType inverse;

  /**
   * Instantiates an empty {@link AdditionalRelationshipTypeJpa}.
   */
  protected AdditionalRelationshipTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AdditionalRelationshipTypeJpa} from the specified
   * parameters.
   *
   * @param rela the rela
   */
  protected AdditionalRelationshipTypeJpa(AdditionalRelationshipType rela) {
    super(rela);
    inverse = rela.getInverse();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.model.meta.AdditionalRelationshipType#getInverse()
   */
  @Override
  public AdditionalRelationshipType getInverse() {
    return inverse;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.model.meta.AdditionalRelationshipType#setInverse(com
   * .wci.umls.server.model.meta.AdditionalRelationshipType)
   */
  @Override
  public void setInverse(AdditionalRelationshipType inverse) {
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
