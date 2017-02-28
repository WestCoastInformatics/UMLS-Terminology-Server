/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.WorkflowBinDefinitionList;
import com.wci.umls.server.jpa.workflow.WorkflowBinDefinitionJpa;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;

/**
 * JAXB enabled implementation of {@link WorkflowBinDefinitionList}.
 */
@XmlRootElement(name = "workflowBinDefinitionList")
public class WorkflowBinDefinitionListJpa extends AbstractResultList<WorkflowBinDefinition>
    implements WorkflowBinDefinitionList {

  /* see superclass */
  @Override
  @XmlElement(type = WorkflowBinDefinitionJpa.class, name = "bins")
  public List<WorkflowBinDefinition> getObjects() {
    return super.getObjectsTransient();
  }

}
