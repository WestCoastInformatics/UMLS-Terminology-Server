/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.SourceDataFile;
import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.SourceDataFileList;
import com.wci.umls.server.jpa.SourceDataFileJpa;

/**
 * JAXB enabled implementation of {@link SourceDataFileList}.
 */
@XmlRootElement(name = "sourceDataFileList")
public class SourceDataFileListJpa extends AbstractResultList<SourceDataFile>
    implements SourceDataFileList {

  /* see superclass */
  @Override
  @XmlElement(type = SourceDataFileJpa.class, name = "sourceDataFiles")
  public List<SourceDataFile> getObjects() {
    return super.getObjectsTransient();
  }

}
