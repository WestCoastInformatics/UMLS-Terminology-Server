/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.AbstractResultList;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;

/**
 * JAXB-enabled implementation of {@link SearchResultList}.
 */
@XmlRootElement(name = "searchResultList")
public class SearchResultListJpa extends AbstractResultList<SearchResult>
    implements SearchResultList {

  /* see superclass */
  @Override
  @XmlElement(type = SearchResultJpa.class, name = "results")
  public List<SearchResult> getObjects() {
    return super.getObjectsTransient();
  }

  /* see superclass */
  @Override
  public String toString() {
    return "SearchResultListJpa [searchResults=" + getObjects()
        + ", getCount()=" + getCount() + "]";
  }

}
