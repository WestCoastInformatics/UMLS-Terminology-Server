/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.content;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.envers.Audited;
import org.hibernate.search.annotations.Indexed;

import com.wci.umls.server.model.content.StringClass;

/**
 * JPA-enabled implementation of {@link StringClass}.
 */
@Entity
@Table(name = "string_classes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@XmlRootElement(name = "stringClass")
@Indexed
public class StringClassJpa extends AbstractAtomClass implements StringClass {

  /**
   * Instantiates an empty {@link StringClassJpa}.
   */
  public StringClassJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link StringClassJpa} from the specified parameters.
   *
   * @param code the code
   * @param deepCopy the deep copy
   */
  public StringClassJpa(StringClass code, boolean deepCopy) {
    super(code, deepCopy);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.content.AbstractAtomClass#toString()
   */
  @Override
  public String toString() {
    return "StringClassJpa [isObsolete()=" + isObsolete()
        + ", getTerminologyVersion()=" + getTerminologyVersion()
        + ", getTerminology()=" + getTerminology() + ", getTerminologyId()="
        + getTerminologyId() + "]";
  }

}
