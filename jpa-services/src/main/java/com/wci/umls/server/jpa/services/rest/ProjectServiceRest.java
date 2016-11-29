/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.actions.AtomicActionList;
import com.wci.umls.server.model.actions.MolecularActionList;

/**
 * Represents a content available via a REST service.
 */
public interface ProjectServiceRest {

  /**
   * Adds the project.
   *
   * @param project the project
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project addProject(ProjectJpa project, String authToken)
    throws Exception;

  /**
   * Update project.
   *
   * @param project the project
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateProject(ProjectJpa project, String authToken)
    throws Exception;

  /**
   * Removes the project.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeProject(Long projectId, String authToken) throws Exception;

  /**
   * Returns the project.
   *
   * @param id the id
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project getProject(Long id, String authToken) throws Exception;

  /**
   * Assign users to project.
   *
   * @param id the id
   * @param userName the user name
   * @param role the role
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project assignUserToProject(Long id, String userName, UserRole role,
    String authToken) throws Exception;

  /**
   * Find unassigned users for project.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findUnassignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Unassign user from project.
   *
   * @param projectId the project id
   * @param userName the user name
   * @param authToken the auth token
   * @return the project
   * @throws Exception the exception
   */
  public Project unassignUserFromProject(Long projectId, String userName,
    String authToken) throws Exception;

  /**
   * Returns the project roles.
   *
   * @param authToken the auth token
   * @return the project roles
   * @throws Exception the exception
   */
  public StringList getProjectRoles(String authToken) throws Exception;

  /**
   * Find assigned users for project.
   *
   * @param projectId the project id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the user list
   * @throws Exception the exception
   */
  public UserList findAssignedUsersForProject(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * User has some project role.
   *
   * @param authToken the auth token
   * @return the boolean
   * @throws Exception the exception
   */
  public Boolean userHasSomeProjectRole(String authToken) throws Exception;

  /**
   * Find projects for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the project list
   * @throws Exception the exception
   */
  public ProjectList findProjects(String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Returns the log.
   *
   * @param projectId the project id
   * @param objectId the object id
   * @param message the message
   * @param lines the lines
   * @param authToken the auth token
   * @return the log
   * @throws Exception the exception
   */
  public String getLog(Long projectId, Long objectId, String message, int lines,
    String authToken) throws Exception;

  /**
   * Returns the log.
   *
   * @param terminology the terminology
   * @param version the version
   * @param activity the activity
   * @param lines the lines
   * @param authToken the auth token
   * @return the log
   * @throws Exception the exception
   */
  public String getLog(String terminology, String version, String activity,
    int lines, String authToken) throws Exception;

  /**
   * Finds molecular actions for concept and query.
   *
   * @param componentId the component id
   * @param terminology the terminology
   * @param version the version
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the molecular actions for concept
   * @throws Exception the exception
   */
  public MolecularActionList findMolecularActions(Long componentId,
    String terminology, String version, String query, PfsParameterJpa pfs,
    String authToken) throws Exception;

  /**
   * Find atomic actions.
   *
   * @param molecularActionId the molecular action id
   * @param query the query
   * @param pfs the pfs
   * @param authToken the auth token
   * @return the atomic action list
   * @throws Exception the exception
   */
  public AtomicActionList findAtomicActions(Long molecularActionId,
    String query, PfsParameterJpa pfs, String authToken) throws Exception;

  /**
   * Returns the validation checks.
   *
   * @param authToken the auth token
   * @return the validation checks
   * @throws Exception the exception
   */
  public KeyValuePairList getValidationChecks(String authToken)
    throws Exception;

  /**
   * Returns the query types.
   *
   * @param authToken the auth token
   * @return the query types
   * @throws Exception the exception
   */
  public StringList getQueryTypes(String authToken) throws Exception;

  /**
   * Force an exception.
   *
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void reloadConfigProperties(String authToken) throws Exception;

  /**
   * Force an exception.
   *
   * @param localFlag the local flag
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void forceException(Boolean localFlag, String authToken)
    throws Exception;

}
