/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.report;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportList;

/**
 * JAXB enabled implementation of {@link ReportList}.
 */
@XmlRootElement(name = "atomList")
public class ReportListJpa extends AbstractResultList<Report> implements ReportList {

  /* see superclass */
  @Override
  @XmlElement(type = ReportJpa.class, name = "atoms")
  public List<Report> getObjects() {
    return super.getObjectsTransient();
  }

}
