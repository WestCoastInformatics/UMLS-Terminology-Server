/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ProcessExecutionList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;

/**
 * JAXB enabled implementation of {@link ProcessExecutionList}.
 */
@XmlRootElement(name = "processList")
public class ProcessExecutionListJpa extends
    AbstractResultList<ProcessExecution> implements ProcessExecutionList {

  /* see superclass */
  @Override
  @XmlElement(type = ProcessExecutionJpa.class, name = "processes")
  public List<ProcessExecution> getObjects() {
    return super.getObjectsTransient();
  }

}
