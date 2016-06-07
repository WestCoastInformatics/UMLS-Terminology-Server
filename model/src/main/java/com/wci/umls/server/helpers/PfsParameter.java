/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

/**
 * Represents a parameter for paging, filtering, and sorting (i.e. "pfs")
 */
public interface PfsParameter {

  /**
   * Gets the max results.
   *
   * @return the max results
   */
  public int getMaxResults();

  /**
   * Sets the max results.
   *
   * @param maxResults the new max results
   */
  public void setMaxResults(int maxResults);

  /**
   * Gets the start index.
   *
   * @return the start index
   */
  public int getStartIndex();

  /**
   * Sets the start index.
   *
   * @param startIndex the new start index
   */
  public void setStartIndex(int startIndex);

  /**
   * Gets the active only.
   *
   * @return the active only
   */
  public boolean getActiveOnly();

  /**
   * Sets the active only.
   *
   * @param activeOnly the new active only
   */
  public void setActiveOnly(boolean activeOnly);

  /**
   * Gets the inactive only.
   *
   * @return the inactive only
   */
  public boolean getInactiveOnly();

  /**
   * Sets the inactive only.
   *
   * @param inactiveOnly the new inactive only
   */
  public void setInactiveOnly(boolean inactiveOnly);

  /**
   * Gets the query restriction.
   *
   * @return the query restriction
   */
  public String getQueryRestriction();

  /**
   * Sets the query restriction.
   *
   * @param queryRestriction the new query restriction
   */
  public void setQueryRestriction(String queryRestriction);

  /**
   * Gets the sort field.
   *
   * @return the sort field
   */
  public String getSortField();

  /**
   * Sets the sort field.
   *
   * @param sortField the new sort field
   */
  public void setSortField(String sortField);

  /**
   * Checks if is index in range.
   *
   * @param i the i
   * @return true, if is index in range
   */
  public boolean isIndexInRange(int i);

  /**
   * Checks if is ascending.
   *
   * @return true, if is ascending
   */
  public boolean isAscending();

  /**
   * Sets the ascending.
   *
   * @param ascending the new ascending
   */
  public void setAscending(boolean ascending);

  /**
   * Gets the branch.
   *
   * @return the branch
   */
  public String getBranch();

  /**
   * Sets the branch.
   *
   * @param branch the new branch
   */
  public void setBranch(String branch);

  /**
   * Gets the sort fields.
   *
   * @return the sort fields
   */
  public List<String> getSortFields();

  /**
   * Sets the sort fields.
   *
   * @param sortFields the new sort fields
   */
  public void setSortFields(List<String> sortFields);

  /**
   * Gets the expression.
   *
   * @return the expression
   */
  public String getExpression();

  /**
   * Sets the expression.
   *
   * @param expression the new expression
   */
  public void setExpression(String expression);
  
}
