/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.PrecedenceList;

/**
 * Represents an project with users, roles, and configuration for
 * editing.
 */
public interface Project extends HasId {

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

  /**
   * Gets the feedback email.
   *
   * @return the feedback email
   */
  public String getFeedbackEmail();

  /**
   * Sets the feedback email.
   *
   * @param feedbackEmail the new feedback email
   */
  public void setFeedbackEmail(String feedbackEmail);

  /**
   * Returns the precedence list.
   *
   * @return the precedence list
   */
  public PrecedenceList getPrecedenceList();

  /**
   * Sets the precedence list.
   *
   * @param precedenceList the precedence list
   */
  public void setPrecedenceList(PrecedenceList precedenceList);

  /**
   * Gets the validation checks.
   *
   * @return the validation checks
   */
  public List<String> getValidationChecks();

  /**
   * Sets the validation checks.
   *
   * @param validationChecks the new validation checks
   */
  public void setValidationChecks(List<String> validationChecks);

  /**
   * Gets the valid categories.
   *
   * @return the valid categories
   */
  public List<String> getValidCategories();

  /**
   * Sets the valid categories.
   *
   * @param validCategories the new valid categories
   */
  public void setValidCategories(List<String> validCategories);

  /**
   * Gets the semantic type category map.  This is used primarily to
   * identify "chem" and "nonchem" by semantic type value.  There may be
   * other categories desired or needed by the workflow environment
   * for repartitioning.  For UMLS editing, the "chem" semantic
   * types will be "Chemical" and all of its descenants.
   *
   * @return the semantic type category map
   */
  public Map<String, String> getSemanticTypeCategoryMap();

  /**
   * Sets the semantic type category map.
   *
   * @param semanticTypeCategoryMap the semantic type category map
   */
  public void setSemanticTypeCategoryMap(Map<String, String> semanticTypeCategoryMap);
  
  /**
   * Returns the workflow path.
   *
   * @return the workflow path
   */
  public String getWorkflowPath();
  
  /**
   * Sets the workflow path.
   *
   * @param workflowPath the workflow path
   */
  public void setWorkflowPath(String workflowPath);

}