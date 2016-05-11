/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ComponentInfoList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ComponentInfoJpa;

/**
 * JAXB enabled implementation of {@link UserList}.
 */
@XmlRootElement(name = "userList")
public class ComponentInfoListJpa extends AbstractResultList<ComponentInfo> implements ComponentInfoList {

  /* see superclass */
  @Override
  @XmlElement(type = ComponentInfoJpa.class, name = "userFavorites")
  public List<ComponentInfo> getObjects() {
    return super.getObjectsTransient();
  }

}
