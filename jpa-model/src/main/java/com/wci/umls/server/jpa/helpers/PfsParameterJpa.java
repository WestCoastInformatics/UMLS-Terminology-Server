/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchCriteria;

/**
 * The JPA enabled implementation of the paging/filtering/sorting object.
 */
@XmlRootElement
public class PfsParameterJpa implements PfsParameter {

  /** The maximum number of results. */
  private int maxResults = -1;

  /** The start index for queries. */
  private int startIndex = -1;

  /** The filter string. */
  private String queryRestriction = null;

  /** The branch restriction. */
  private String branch = null;

  /** The comparator for sorting. */
  private String sortField = null;

  /** The ascending flag. */
  private boolean ascending = true;

  /** The search criteria. */
  private List<SearchCriteria> searchCriteria = new ArrayList<>();

  /**
   * The default constructor.
   */
  public PfsParameterJpa() {
    // do nothing
  }

  /**
   * Instantiates a {@link PfsParameterJpa} from the specified parameters.
   *
   * @param pfs the pfs
   */
  public PfsParameterJpa(PfsParameter pfs) {
    maxResults = pfs.getMaxResults();
    startIndex = pfs.getStartIndex();
    queryRestriction = pfs.getQueryRestriction();
    branch = pfs.getBranch();
    sortField = pfs.getSortField();
    ascending = pfs.isAscending();
    searchCriteria = pfs.getSearchCriteria();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#getMaxResults()
   */
  @Override
  public int getMaxResults() {
    return maxResults;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#setMaxResults(int)
   */
  @Override
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#getStartIndex()
   */
  @Override
  public int getStartIndex() {
    return startIndex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#setStartIndex(int)
   */
  @Override
  public void setStartIndex(int startIndex) {
    this.startIndex = startIndex;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#getQueryRestriction()
   */
  @Override
  public String getQueryRestriction() {
    return queryRestriction;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PfsParameter#setQueryRestriction(java.lang.
   * String)
   */
  @Override
  public void setQueryRestriction(String queryRestriction) {
    this.queryRestriction = queryRestriction;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#getBranch()
   */
  @Override
  public String getBranch() {
    return branch;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#setBranch(java.lang.String)
   */
  @Override
  public void setBranch(String branch) {
    this.branch = branch;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#isAscending()
   */
  @Override
  public boolean isAscending() {
    return ascending;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#setAscending(boolean)
   */
  @Override
  public void setAscending(boolean ascending) {
    this.ascending = ascending;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#getSortField()
   */
  @Override
  public String getSortField() {
    return sortField;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.PfsParameter#setSortField(java.lang.String)
   */
  @Override
  public void setSortField(String sortField) {
    this.sortField = sortField;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.helpers.PfsParameter#getSearchCriteria()
   */
  @XmlElement(type = SearchCriteriaJpa.class, name = "criteria")
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
   * com.wci.umls.server.helpers.PfsParameter#setSearchCriteria(java.util.List)
   */
  @Override
  public void setSearchCriteria(List<SearchCriteria> searchCriteria) {
    this.searchCriteria = searchCriteria;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (ascending ? 1231 : 1237);
    result = prime * result + ((branch == null) ? 0 : branch.hashCode());
    result = prime * result + maxResults;
    result =
        prime * result
            + ((queryRestriction == null) ? 0 : queryRestriction.hashCode());
    result =
        prime * result
            + ((searchCriteria == null) ? 0 : searchCriteria.hashCode());
    result = prime * result + ((sortField == null) ? 0 : sortField.hashCode());
    result = prime * result + startIndex;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PfsParameterJpa other = (PfsParameterJpa) obj;
    if (ascending != other.ascending)
      return false;
    if (branch == null) {
      if (other.branch != null)
        return false;
    } else if (!branch.equals(other.branch))
      return false;
    if (maxResults != other.maxResults)
      return false;
    if (queryRestriction == null) {
      if (other.queryRestriction != null)
        return false;
    } else if (!queryRestriction.equals(other.queryRestriction))
      return false;
    if (searchCriteria == null) {
      if (other.searchCriteria != null)
        return false;
    } else if (!searchCriteria.equals(other.searchCriteria))
      return false;
    if (sortField == null) {
      if (other.sortField != null)
        return false;
    } else if (!sortField.equals(other.sortField))
      return false;
    if (startIndex != other.startIndex)
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.PfsParameter#isIndexInRange(int)
   */
  @Override
  public boolean isIndexInRange(int i) {
    return getStartIndex() != -1 && getMaxResults() != -1
        && i >= getStartIndex() && i < (getStartIndex() + getMaxResults());
  }

  @Override
  public String toString() {
    return "PfsParameterJpa [maxResults=" + maxResults + ", startIndex="
        + startIndex + ", queryRestriction=" + queryRestriction + ", branch="
        + branch + ", sortField=" + sortField + ", ascending=" + ascending
        + ", searchCriteria=" + searchCriteria + "]";
  }

}
