/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*************************************************************

 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.helpers;

import java.util.List;

import com.wci.umls.server.model.content.ComponentHistory;

/**
 * Represents a thing that is associated with a terminology and an identifier.
 */
public interface HasComponentHistory {


  /**
   * Gets the component history.
   *
   * @return the component history
   */
  public List<ComponentHistory> getComponentHistory();

  /**
   * Sets the component history.
   *
   * @param componentHistory the new component history
   */
  public void setComponentHistory(List<ComponentHistory> componentHistory);

}
