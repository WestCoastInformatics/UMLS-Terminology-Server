/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ChangeEventList;
import com.wci.umls.server.jpa.actions.ChangeEventJpa;
import com.wci.umls.server.model.actions.ChangeEvent;

/**
 * JAXB enabled implementation of {@link ChangeEventList}.
 */
@XmlRootElement(name = "changeEventList")
public class ChangeEventListJpa extends AbstractResultList<ChangeEvent>
    implements ChangeEventList {

  /**
   * Returns the objects.
   *
   * @return the objects
   */
  /* see superclass */
  @Override
  @XmlElement(type = ChangeEventJpa.class, name = "events")
  public List<ChangeEvent> getObjects() {
    return super.getObjectsTransient();
  }

}
