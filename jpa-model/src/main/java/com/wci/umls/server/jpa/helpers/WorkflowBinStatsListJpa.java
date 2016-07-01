/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.WorkflowBinStatsList;
import com.wci.umls.server.jpa.worfklow.WorkflowBinStatsJpa;
import com.wci.umls.server.model.workflow.WorkflowBinStats;


/**
 * JAXB enabled implementation of {@link WorkflowBinStatsList}.
 */
@XmlRootElement(name = "workflowBinStatsList")
public class WorkflowBinStatsListJpa extends AbstractResultList<WorkflowBinStats>
    implements WorkflowBinStatsList {

  /* see superclass */
  @Override
  @XmlElement(type = WorkflowBinStatsJpa.class, name = "workflowBinStats")
  public List<WorkflowBinStats> getObjects() {
    return super.getObjectsTransient();
  }

}
