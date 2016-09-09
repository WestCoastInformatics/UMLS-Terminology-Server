/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.AlgorithmExecution;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.AlgorithmExecutionList;
import com.wci.umls.server.jpa.AlgorithmExecutionJpa;

/**
 * JAXB enabled implementation of {@link AlgorithmExecutionList}.
 */
@XmlRootElement(name = "algorithmList")
public class AlgorithmExecutionListJpa extends
    AbstractResultList<AlgorithmExecution> implements AlgorithmExecutionList {

  /* see superclass */
  @Override
  @XmlElement(type = AlgorithmExecutionJpa.class, name = "algorithms")
  public List<AlgorithmExecution> getObjects() {
    return super.getObjectsTransient();
  }

}
