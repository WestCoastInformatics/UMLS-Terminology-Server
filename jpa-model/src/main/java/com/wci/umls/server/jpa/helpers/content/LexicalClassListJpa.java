/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.model.content.LexicalClass;

/**
 * JAXB enabled implementation of {@link LexicalClassList}.
 */
@XmlRootElement(name = "lexicalClassList")
public class LexicalClassListJpa extends AbstractResultList<LexicalClass>
    implements LexicalClassList {


  /* see superclass */
  @Override
  @XmlElement(type = LexicalClassJpa.class, name = "lexicalClasses")
  public List<LexicalClass> getObjects() {
    return super.getObjectsTransient();
  }

}
