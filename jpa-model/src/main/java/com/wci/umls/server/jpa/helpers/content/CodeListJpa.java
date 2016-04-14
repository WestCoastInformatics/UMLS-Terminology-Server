/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.model.content.Code;

/**
 * JAXB enabled implementation of {@link CodeList}.
 */
@XmlRootElement(name = "codeList")
public class CodeListJpa extends AbstractResultList<Code> implements CodeList {

  /* see superclass */
  @Override
  @XmlElement(type = CodeJpa.class, name = "codes")
  public List<Code> getObjects() {
    return super.getObjectsTransient();
  }

}
