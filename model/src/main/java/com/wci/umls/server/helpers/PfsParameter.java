/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.content.Concept;

/**
 * Represents a parameter container for paging, filtering and sorting. NOTE:
 * filtering is not currently imported or supported.
 */
public interface PfsParameter {

  /**
   * Returns the maximum number of results.
   *
   * @return the maximum number of results
   */
  public int getMaxResults();

  /**
   * Sets the maximum number of results.
   *
   * @param maxResults the maximum number of results
   */
  public void setMaxResults(int maxResults);

  /**
   * Returns the starting index of a query result subset.
   *
   * @return the start index
   */
  public int getStartIndex();

  /**
   * Sets the starting index of a query result subset.
   *
   * @param startIndex the start index
   */
  public void setStartIndex(int startIndex);

  /**
   * Returns the filter string.
   *
   * @return the filter string
   */
  public String getQueryRestriction();

  /**
   * Sets a string that can be used as part of a query to restrict results. In
   * practice, this is expressed in Lucene query syntax.
   * @param queryRestriction the filter string
   */
  public void setQueryRestriction(String queryRestriction);

  /**
   * Returns the sort field name. Valid values for the sort field would include
   * a bean-style property name for the underlying object. For example, for
   * {@link Concept} retrieval, you could pass id or definitionStatusId, or
   * defaultPrefereredName.
   * @return the sort field name
   */
  public String getSortField();

  /**
   * Sets the sort field name.
   *
   * @param sortField the sort field name
   */
  public void setSortField(String sortField);

  /**
   * Indicates whether the index is in range for the given start index and max
   * results settings.
   * @param i the index to check
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isIndexInRange(int i);

  /**
   * Indicates whether or not ascending is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAscending();

  /**
   * Sets the ascending flag
   *
   * @param ascending the ascending
   */
  public void setAscending(boolean ascending);
}
