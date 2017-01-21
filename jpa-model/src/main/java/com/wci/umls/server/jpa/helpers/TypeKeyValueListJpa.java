/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.helpers.TypeKeyValueList;

/**
 * JAXB-enabled implementation of {@link TypeKeyValueList}.
 * 
 * Useful for sending type key values to REST Server.
 */
@XmlRootElement(name = "typeKeyValueList")
public class TypeKeyValueListJpa extends AbstractResultList<TypeKeyValue>
    implements TypeKeyValueList {

  /* see superclass */
  @Override
  @XmlElement(type = TypeKeyValueJpa.class, name = "typeKeyValues")
  public List<TypeKeyValue> getObjects() {
    return super.getObjectsTransient();
  }

  /* see superclass */
  @Override
  public String toString() {
    return "TypeKeyValueListJpa [TypeKeyValues=" + getObjects() + ", size="
        + getObjects().size() + "]";
  }
}
