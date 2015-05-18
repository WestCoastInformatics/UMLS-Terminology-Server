/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.content.DefinitionList;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.model.content.Definition;

/**
 * JAXB enabled implementation of {@link DefinitionList}.
 */
@XmlRootElement(name = "definitionList")
public class DefinitionListJpa extends AbstractResultList<Definition> implements
    DefinitionList {


  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.AbstractResultList#getObjects()
   */
  @Override
  @XmlElement(type = DefinitionJpa.class, name = "definition")
  public List<Definition> getObjects() {
    return super.getObjectsTransient();
  }

}
