/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.Date;

import com.wci.umls.server.helpers.HasTerminologyId;

/**
 * Represents a terminology component.
 */
public interface Component extends HasTerminologyId {

  /**
   * Returns the id.
   * 
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   * 
   * @param id the id
   */
  public void setId(Long id);

  /**
   * Returns the timestamp.
   *
   * @return the timestamp
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
   * Indicates whether or not the component is obsolete.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isObsolete();

  /**
   * Sets the obsolete.
   *
   * @param obsolete the obsolete
   */
  public void setObsolete(boolean obsolete);

  /**
   * Indicates whether or not suppressible is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSuppressible();

  /**
   * Sets the suppressible.
   *
   * @param suppressible the suppressible
   */
  public void setSuppressible(boolean suppressible);

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
  
}