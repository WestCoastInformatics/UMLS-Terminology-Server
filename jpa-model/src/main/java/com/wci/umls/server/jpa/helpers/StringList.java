/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;

/**
 * JAXB enabled implementation of {@link StringList}.
 */
@XmlRootElement(name = "StringList")
public class StringList extends AbstractResultList<String> {

  /**
   * Instantiates a new map String list.
   */
  public StringList() {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = String.class, name = "string")
  public List<String> getObjects() {
    return super.getObjects();
  }

}
