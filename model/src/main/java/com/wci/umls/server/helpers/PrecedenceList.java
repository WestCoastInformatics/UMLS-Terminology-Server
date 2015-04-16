/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.meta.TermType;

/**
 * Represents an ordered list of {@link TermType}s for use in computing atom
 * ranks.
 */
public interface PrecedenceList extends HasLastModified {

  /**
   * Indicates whether or not default is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isDefaultList();

  /**
   * Sets the default.
   *
   * @param defaultList the default list
   */
  public void setDefaultList(boolean defaultList);

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Sets the source term types.
   * 
   * @param precedence the source term types
   */
  public void setPrecedence(KeyValuePairList precedence);

  /**
   * Returns the source term types.
   * 
   * @return the source term types
   */
  public KeyValuePairList getPrecedence();
}
