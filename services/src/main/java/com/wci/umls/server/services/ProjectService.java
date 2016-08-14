/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import java.util.Map;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.services.handlers.ValidationCheck;

/**
 * Represents a service for accessing {@link Project} information.
 */
public interface ProjectService extends RootService {

  /**
   * Returns the concepts in scope.
   *
   * @param project the project
   * @param pfs the pfs
   * @return the concepts in scope
   * @throws Exception the exception
   */
  public ConceptList findConceptsInScope(Project project, PfsParameter pfs)
    throws Exception;

  /**
   * Returns the project.
   *
   * @param id the id
   * @return the project
   */
  public Project getProject(Long id);

  /**
   * Adds the project.
   *
   * @param project the project
   * @return the project
   * @throws Exception the exception
   */
  public Project addProject(Project project) throws Exception;

  /**
   * Update project.
   *
   * @param project the project
   * @throws Exception the exception
   */
  public void updateProject(Project project) throws Exception;

  /**
   * Removes the project.
   *
   * @param projectId the project id
   * @throws Exception the exception
   */
  public void removeProject(Long projectId) throws Exception;

  /**
   * Returns the projects.
   *
   * @return the projects
   */
  public ProjectList getProjects();

  /**
   * Returns the user role for project.
   *
   * @param username the username
   * @param projectId the project id
   * @return the user role for project
   * @throws Exception the exception
   */
  public UserRole getUserRoleForProject(String username, Long projectId)
    throws Exception;

  /**
   * Find projects for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the project list
   * @throws Exception the exception
   */
  public ProjectList findProjects(String query, PfsParameter pfs)
    throws Exception;

  /**
   * Gets the validation check names.
   *
   * @return the validation check names
   * @throws Exception the exception
   */
  public KeyValuePairList getValidationCheckNames() throws Exception;

  /**
   * Returns the validation handlers map.
   *
   * @return the validation handlers map
   * @throws Exception the exception
   */
  public Map<String, ValidationCheck> getValidationHandlersMap()
    throws Exception;
}