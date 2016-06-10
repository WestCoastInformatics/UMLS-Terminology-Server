/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.worfklow;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.model.workflow.WorklistList;


/**
 * JAXB enabled implementation of {@link WorklistList}.
 */
@XmlRootElement(name = "worklistList")
public class WorklistListJpa extends AbstractResultList<Worklist>
    implements WorklistList {

  /* see superclass */
  @Override
  @XmlElement(type = WorklistJpa.class, name = "worklists")
  public List<Worklist> getObjects() {
    return super.getObjectsTransient();
  }

}
