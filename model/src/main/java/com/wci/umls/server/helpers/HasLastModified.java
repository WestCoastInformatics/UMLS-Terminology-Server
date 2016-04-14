/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.Date;

/**
 * Represents a thing that has last modified tracking.
 */
public interface HasLastModified extends HasId {

  /**
   * Timestamp.
   *
   * @return the date
   */
  public Date getTimestamp();

  /**
   * Sets the timestamp.
   *
   * @param timestamp the timestamp
   */
  public void setTimestamp(Date timestamp);

  /**
   * Returns the last modified.
   * 
   * @return the last modified
   */
  public Date getLastModified();

  /**
   * Sets the last modified.
   * 
   * @param lastModified the last modified
   */
  public void setLastModified(Date lastModified);

  /**
   * Returns the last modified by.
   * 
   * @return the last modified by
   */
  public String getLastModifiedBy();

  /**
   * Sets the last modified by.
   * 
   * @param lastModifiedBy the last modified by
   */
  public void setLastModifiedBy(String lastModifiedBy);

}
