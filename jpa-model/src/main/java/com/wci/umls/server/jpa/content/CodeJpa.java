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

import com.wci.umls.server.model.content.Code;

/**
 * JPA-enabled implementation of {@link Code}.
 */
@Entity
@Table(name = "codes", uniqueConstraints = @UniqueConstraint(columnNames = {
    "terminologyId", "terminology", "terminologyVersion", "id"
}))
@Audited
@Indexed
@XmlRootElement(name = "code")
public class CodeJpa extends AbstractAtomClass implements Code {

  /**
   * Instantiates an empty {@link CodeJpa}.
   */
  protected CodeJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link CodeJpa} from the specified parameters.
   *
   * @param code the code
   * @param deepCopy the deep copy
   */
  protected CodeJpa(Code code, boolean deepCopy) {
    super(code, deepCopy);
  }
}
