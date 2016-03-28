/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Date;
import java.util.Map;

/**
 * Generically represents an editing project.
 */
public interface Project {

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
   * Returns the description.
   * 
   * @return the description
   */
  public String getDescription();

  /**
   * Sets the description.
   * 
   * @param description the description
   */
  public void setDescription(String description);

  /**
   * Checks if the project is viewable by public roles.
   *
   * @return true, if is public
   */
  public boolean isPublic();

  /**
   * Sets whether the project is viewable by public roles.
   *
   * @param isPublic the new public
   */
  public void setPublic(boolean isPublic);

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
   * Returns the terminology.
   * 
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   * 
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   * 
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   * 
   * @param version the version
   */
  public void setVersion(String version);

  /**
   * Returns the user role map.
   *
   * @return the user role map
   */
  public Map<User, UserRole> getUserRoleMap();

  /**
   * Sets the user role map.
   *
   * @param userRoleMap the user role map
   */
  public void setUserRoleMap(Map<User, UserRole> userRoleMap);

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