/**
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;

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
   * Returns the projects.
   *
   * @param authToken the auth token
   * @return the projects
   * @throws Exception the exception
   */
  public ProjectList getProjects(String authToken) throws Exception;

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
  public Project assignUserToProject(Long id, String userName, 
    String role, String authToken) throws Exception;

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
}
