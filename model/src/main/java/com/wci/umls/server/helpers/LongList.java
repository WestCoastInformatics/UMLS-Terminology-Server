/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB enabled implementation of {@link LongList}.
 */
@XmlRootElement(name = "LongList")
public class LongList extends AbstractResultList<Long> {

  /**
   * Instantiates a new map Long list.
   */
  public LongList() {
    // do nothing
  }

  @Override
  @XmlElement(type = Long.class, name = "longs")
  public List<Long> getObjects() {
    return super.getObjectsTransient();
  }

}
