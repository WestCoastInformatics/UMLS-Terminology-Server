/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.ProcessConfig;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ProcessConfigList;
import com.wci.umls.server.jpa.ProcessConfigJpa;

/**
 * JAXB enabled implementation of {@link ProcessConfigList}.
 */
@XmlRootElement(name = "processList")
public class ProcessConfigListJpa extends AbstractResultList<ProcessConfig>
    implements ProcessConfigList {

  /* see superclass */
  @Override
  @XmlElement(type = ProcessConfigJpa.class, name = "processes")
  public List<ProcessConfig> getObjects() {
    return super.getObjectsTransient();
  }

}
