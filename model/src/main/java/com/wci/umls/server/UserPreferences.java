/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import com.wci.umls.server.helpers.PrecedenceList;

/**
 * Represents a user.
 */
public interface UserPreferences {

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
   * Returns the user.
   *
   * @return the user
   */
  public User getUser();
  
  /**
   * Sets the user.
   *
   * @param user the user
   */
  public void setUser(User user);

  /**
   * Returns the precedence list.
   *
   * @return the precedence list
   */
  public PrecedenceList getPrecedenceList();


  /**
   * Sets the precedence list.
   *
   * @param list the precedence list
   */
  public void setPrecedenceList(PrecedenceList list);

}
