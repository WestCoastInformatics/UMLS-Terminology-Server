/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.WorkflowConfigList;
import com.wci.umls.server.jpa.workflow.WorkflowConfigJpa;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * JAXB enabled implementation of {@link WorkflowConfigList}.
 */
@XmlRootElement(name = "workflowConfigList")
public class WorkflowConfigListJpa extends AbstractResultList<WorkflowConfig>
    implements WorkflowConfigList {

  /* see superclass */
  @Override
  @XmlElement(type = WorkflowConfigJpa.class, name = "configs")
  public List<WorkflowConfig> getObjects() {
    return super.getObjectsTransient();
  }

}
