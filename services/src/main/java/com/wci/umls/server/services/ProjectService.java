/*
 * 
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.helpers.ConceptList;
import com.wci.umls.server.helpers.ProjectList;

/**
 * Generically represents a service for accessing project info.
 */
public interface ProjectService extends RootService {

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
   * Returns the concepts in scope.
   *
   * @param project the project
   * @return the concepts in scope
   * @throws Exception the exception
   */
  public ConceptList getConceptsInScope(Project project) throws Exception;

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
   */
  public Project addProject(Project project);

  /**
   * Update project.
   *
   * @param project the project
   */
  public void updateProject(Project project);

  /**
   * Removes the project.
   *
   * @param projectId the project id
   */
  public void removeProject(Long projectId);
}