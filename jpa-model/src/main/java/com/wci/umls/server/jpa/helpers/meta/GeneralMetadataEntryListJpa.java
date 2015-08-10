/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.meta;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.meta.GeneralMetadataEntryList;
import com.wci.umls.server.jpa.meta.GeneralMetadataEntryJpa;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;

/**
 * JAXB enabled implementation of {@link GeneralMetadataEntryList}.
 */
@XmlRootElement(name = "relationshipTypeList")
public class GeneralMetadataEntryListJpa extends
    AbstractResultList<GeneralMetadataEntry> implements
    GeneralMetadataEntryList {

  /* see superclass */
  @Override
  @XmlElement(type = GeneralMetadataEntryJpa.class, name = "type")
  public List<GeneralMetadataEntry> getObjects() {
    return super.getObjectsTransient();
  }

}
