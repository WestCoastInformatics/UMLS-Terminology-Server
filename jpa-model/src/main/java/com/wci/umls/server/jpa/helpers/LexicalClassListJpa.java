/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.LexicalClassList;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.model.content.LexicalClass;

/**
 * JAXB enabled implementation of {@link LexicalClassList}.
 */
@XmlRootElement(name = "lexicalClassList")
public class LexicalClassListJpa extends AbstractResultList<LexicalClass>
    implements LexicalClassList {

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = LexicalClassJpa.class, name = "lexicalClass")
  public List<LexicalClass> getObjects() {
    return super.getObjects();
  }

}
