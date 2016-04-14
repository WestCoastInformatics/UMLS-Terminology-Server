/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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

  @Override
  @XmlElement(type = String.class, name = "strings")
  public List<String> getObjects() {
    return super.getObjectsTransient();
  }

}
