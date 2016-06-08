/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import java.util.Date;


/**
 * Represents a worklist.
 */
public interface Worklist extends Checklist {


  /**
   * Gets the editor.
   *
   * @return the editor
   */
  public String getEditor();


  /**
   * Sets the editor.
   *
   * @param editor the new editor
   */
  public void setEditor(String editor);

  /**
   * Gets the group.
   *
   * @return the group
   */
  public String getGroup();
  
  /**
   * Sets the group.
   *
   * @param group the new group
   */
  public void setGroup(String group);
  
  /**
   * Gets the assign date.
   *
   * @return the assign date
   */
  public Date getAssignDate();
  
  /**
   * Sets the assign date.
   *
   * @param assignDate the new assign date
   */
  public void setAssignDate(Date assignDate);
  
  /**
   * Gets the return date.
   *
   * @return the return date
   */
  public Date getReturnDate();
  
  /**
   * Sets the return date.
   *
   * @param returnDate the new return date
   */
  public void setReturnDate(Date returnDate);
  
  /**
   * Gets the stamp date.
   *
   * @return the stamp date
   */
  public Date getStampDate();
  
  /**
   * Sets the stamp date.
   *
   * @param stampDate the new stamp date
   */
  public void setStampDate(Date stampDate);
  
  /**
   * Gets the stamped by.
   *
   * @return the stamped by
   */
  public String getStampedBy();
  
  /**
   * Sets the stamped by.
   *
   * @param stampedBy the new stamped by
   */
  public void setStampedBy(String stampedBy);
  
  /**
   * Gets the worklist status.
   *
   * @return the worklist status
   */
  public String getStatus();
  
  /**
   * Sets the worklist status.
   *
   * @param worklistStatus the new worklist status
   */
  public void setStatus(String worklistStatus);


}