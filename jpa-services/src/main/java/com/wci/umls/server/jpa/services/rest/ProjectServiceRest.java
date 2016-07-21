/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.UserList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
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
  public Project assignUserToProject(Long id, String userName, String role,
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
   * @param lines the lines
   * @param authToken the auth token
   * @return the log
   * @throws Exception the exception
   */
  public String getLog(Long projectId, Long objectId, int lines,
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
   * Validates the specified concept. Checks are defined the "run.config.umls"
   * setting for the deployed server.
   *
   * @param projectId the project id
   * @param concept the concept
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateConcept(Long projectId, ConceptJpa concept,
    String authToken) throws Exception;

  /**
   * Validate atom.
   *
   * @param projectId the project id
   * @param atom the atom
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateAtom(Long projectId, AtomJpa atom,
    String authToken) throws Exception;

  /**
   * Validate descriptor.
   *
   * @param projectId the project id
   * @param descriptor the descriptor
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateDescriptor(Long projectId,
    DescriptorJpa descriptor, String authToken) throws Exception;

  /**
   * Validate code.
   *
   * @param projectId the project id
   * @param code the code
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateCode(Long projectId, CodeJpa code,
    String authToken) throws Exception;

  /**
   * Validate merge.
   *
   * @param projectId the project id
   * @param terminology the terminology
   * @param version the version
   * @param conceptId the concept id
   * @param conceptId2 the concept id 2
   * @param authToken the auth token
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateMerge(Long projectId, String terminology,
    String version, Long conceptId, Long conceptId2, String authToken)
    throws Exception;

  /**
   * Gets the validation checks.
   *
   * @param projectId the project id
   * @param authToken the auth token
   * @return the validation checks
   * @throws Exception the exception
   */
  public KeyValuePairList getValidationChecks(Long projectId, String authToken)
    throws Exception;

}
