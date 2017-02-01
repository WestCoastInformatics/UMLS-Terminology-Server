/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.WorkflowEpochList;
import com.wci.umls.server.jpa.workflow.WorkflowEpochJpa;
import com.wci.umls.server.model.workflow.WorkflowEpoch;

/**
 * JAXB enabled implementation of {@link WorkflowEpochList}.
 */
@XmlRootElement(name = "workflowEpochList")
public class WorkflowEpochListJpa extends AbstractResultList<WorkflowEpoch>
    implements WorkflowEpochList {

  /* see superclass */
  @Override
  @XmlElement(type = WorkflowEpochJpa.class, name = "epochs")
  public List<WorkflowEpoch> getObjects() {
    return super.getObjectsTransient();
  }

}
