/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

/**
 * Represents a {@link PfsParameter} with the addition of search criteria.
 */
public interface PfscParameter extends PfsParameter {

  /**
   * Returns the search criteria.
   *
   * @return the search criteria
   */
  public List<SearchCriteria> getSearchCriteria();

  /**
   * Sets the search criteria.
   *
   * @param searchCriteria the search criteria
   */
  public void setSearchCriteria(List<SearchCriteria> searchCriteria);

  /**
   * Adds the search criteria.
   *
   * @param searchCriteria the search criteria
   */
  public void addSearchCriteria(SearchCriteria searchCriteria);
}
