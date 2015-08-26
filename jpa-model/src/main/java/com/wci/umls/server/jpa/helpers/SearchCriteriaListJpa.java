/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.SearchCriteriaList;

/**
 * JAXB enabled implementation of {@link SearchCriteriaList}.
 */
@XmlRootElement(name = "searchCriteriaList")
public class SearchCriteriaListJpa extends AbstractResultList<SearchCriteria>
    implements SearchCriteriaList {

  /* see superclass */
  @Override
  @XmlElement(type = SearchCriteriaJpa.class, name = "criteria")
  public List<SearchCriteria> getObjects() {
    return super.getObjectsTransient();
  }

}
