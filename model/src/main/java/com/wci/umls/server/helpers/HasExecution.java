/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.Date;

/**
 * Represents a thing that is or has been executed.
 */
public interface HasExecution {
  /**
   * Returns the start date.
   *
   * @return the start date
   */
  public Date getStartDate();

  /**
   * Sets the start date.
   *
   * @param startDate the start date
   */
  public void setStartDate(Date startDate);

  /**
   * Returns the finish date.
   *
   * @return the finish date
   */
  public Date getFinishDate();

  /**
   * Sets the finish date.
   *
   * @param finishDate the finish date
   */
  public void setFinishDate(Date finishDate);

  /**
   * Returns the fail date.
   *
   * @return the fail date
   */
  public Date getFailDate();

  /**
   * Sets the fail date.
   *
   * @param failDate the fail date
   */
  public void setFailDate(Date failDate);

}
