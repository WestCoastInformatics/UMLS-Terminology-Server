/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import java.util.Date;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a piece of data with an abbreviation and an expanded form.
 */
public interface Abbreviation extends HasTerminology, HasId {

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

  /**
   * Indicates whether or not the component is published.
   *
   * @return true, if is published
   */
  public boolean isPublished();

  /**
   * Sets the published flag.
   *
   * @param published the new published
   */
  public void setPublished(boolean published);

  /**
   * Indicates whether or not the component should be published. This is a
   * mechanism to have data in the server that can be ignored by publishing
   * processes.
   * 
   * @return true, if is publishable
   */
  public boolean isPublishable();

  /**
   * Sets the publishable flag.
   *
   * @param publishable the new publishable
   */
  public void setPublishable(boolean publishable);

  /**
   * Returns the branch.
   *
   * @return the branch
   */
  public String getBranch();
  
  /**
   * Sets the branch.
   *
   * @param branch the branch
   */
  public void setBranch(String branch);
  
  /**
   * Returns the abbreviation.
   * 
   * @return the abbreviation
   */
  public String getAbbreviation();

  /**
   * Returns the expanded form.
   * 
   * @return the expanded form
   */
  public String getExpandedForm();

  /**
   * Sets the abbreviation.
   * 
   * @param abbreviation the abbreviation
   */
  public void setAbbreviation(String abbreviation);

  /**
   * Sets the expanded form.
   * 
   * @param expandedForm the expanded form
   */
  public void setExpandedForm(String expandedForm);
}
