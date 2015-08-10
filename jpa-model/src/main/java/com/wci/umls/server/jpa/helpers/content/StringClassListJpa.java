/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.model.content.StringClass;

/**
 * JAXB enabled implementation of {@link StringClassList}.
 */
@XmlRootElement(name = "stringClassList")
public class StringClassListJpa extends AbstractResultList<StringClass>
    implements StringClassList {


  /* see superclass */
  @Override
  @XmlElement(type = StringClassJpa.class, name = "stringClass")
  public List<StringClass> getObjects() {
    return super.getObjectsTransient();
  }

}
