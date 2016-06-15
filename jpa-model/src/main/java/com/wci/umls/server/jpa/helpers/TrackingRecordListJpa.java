/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;

/**
 * JAXB enabled implementation of {@link TrackingRecordList}.
 */
@XmlRootElement(name = "worklistList")
public class TrackingRecordListJpa extends AbstractResultList<TrackingRecord>
    implements TrackingRecordList {

  /* see superclass */
  @Override
  @XmlElement(type = TrackingRecordJpa.class, name = "worklists")
  public List<TrackingRecord> getObjects() {
    return super.getObjectsTransient();
  }

}
