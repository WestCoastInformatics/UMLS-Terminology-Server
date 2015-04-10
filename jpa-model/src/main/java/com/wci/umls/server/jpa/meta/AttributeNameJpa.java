/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.meta;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.AttributeName;

/**
 * JPA-enabled implementation of {@link AttributeName}.
 */
@Entity
@Table(name = "attribute_names", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "attributeName")
public class AttributeNameJpa extends AbstractAbbreviation implements
    AttributeName {

  /**
   * Instantiates an empty {@link AttributeNameJpa}.
   */
  public AttributeNameJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link AttributeNameJpa} from the specified parameters.
   *
   * @param atn the atn
   */
  public AttributeNameJpa(AttributeName atn) {
    super(atn);
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
  // isDlEnabled?
  // isreflexive, is transitive, is functional, etc.
  // domain/range
  // is non-grouping
  // property chains.

}
