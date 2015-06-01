/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.helpers.SearchCriteria;

/**
 * The JPA enabled implementation of the paging/filtering/sorting/criteria
 * object.
 */
@XmlRootElement(name = "pfsc")
public class PfscParameterJpa extends PfsParameterJpa implements PfscParameter {

  /** The search criteria. */
  private List<SearchCriteria> searchCriteria = new ArrayList<>();

  /**
   * The default constructor.
   */
  public PfscParameterJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PfscParameterJpa} from the specified parameters.
   *
   * @param pfsc the pfsc
   */
  public PfscParameterJpa(PfscParameter pfsc) {
    super(pfsc);
    searchCriteria = pfsc.getSearchCriteria();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfscParameter#getSearchCriteria()
   */
  @XmlElement(type = SearchCriteriaJpa.class)
  @Override
  public List<SearchCriteria> getSearchCriteria() {
    if (searchCriteria == null) {
      searchCriteria = new ArrayList<SearchCriteria>();
    }
    return searchCriteria;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PfscParameter#addSearchCriteria(com.wci.umls
   * .server.helpers.SearchCriteria)
   */
  @Override
  public void addSearchCriteria(SearchCriteria criteria) {
    if (searchCriteria == null) {
      searchCriteria = new ArrayList<SearchCriteria>();
    }
    searchCriteria.add(criteria);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PfscParameter#setSearchCriteria(java.util.List)
   */
  @Override
  public void setSearchCriteria(List<SearchCriteria> searchCriteria) {
    this.searchCriteria = searchCriteria;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.helpers.PfsParameterJpa#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result
            + ((searchCriteria == null) ? 0 : searchCriteria.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.helpers.PfsParameterJpa#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    PfscParameterJpa other = (PfscParameterJpa) obj;
    if (searchCriteria == null) {
      if (other.searchCriteria != null)
        return false;
    } else if (!searchCriteria.equals(other.searchCriteria))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.jpa.helpers.PfsParameterJpa#toString()
   */
  @Override
  public String toString() {
    return super.toString() + ", searchCriteria = " + searchCriteria;
  }

}
