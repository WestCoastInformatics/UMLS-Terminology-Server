package com.wci.umls.server.jpa.meta;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;

import com.wci.umls.server.model.meta.IdentifierType;

/**
 * JPA-enabled implementation of {@link IdentifierType}.
 */
@Entity
@Table(name = "identifierTypes", uniqueConstraints = @UniqueConstraint(columnNames = {
  "abbreviation"
}))
@Audited
@XmlRootElement(name = "identifierType")
public class IdentifierTypeJpa extends AbstractAbbreviation implements
    IdentifierType {

  /**
   * Instantiates an empty {@link IdentifierTypeJpa}.
   */
  public IdentifierTypeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link IdentifierTypeJpa} from the specified parameters.
   *
   * @param idType the id type
   */
  public IdentifierTypeJpa(IdentifierType idType) {
    super(idType);
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

}
