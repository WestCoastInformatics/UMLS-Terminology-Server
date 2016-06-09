/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;

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
  public ProjectList findProjectsForQuery(String query, PfsParameter pfs)
    throws Exception;


  /**
   * Validate concept.
   *
   * @param project the project
   * @param concept the concept
   * @return the validation result
   */
  public ValidationResult validateConcept(Project project, Concept concept);
  
  /**
   * Validate atom.
   *
   * @param project the project
   * @param atom the atom
   * @return the validation result
   */
  public ValidationResult validateAtom(Project project, Atom atom);
  
  /**
   * Validate descriptor.
   *
   * @param project the project
   * @param descriptor the descriptor
   * @return the validation result
   */
  public ValidationResult validateDescriptor(Project project, Descriptor descriptor);
  
  /**
   * Validate code.
   *
   * @param project the project
   * @param code the code
   * @return the validation result
   */
  public ValidationResult validateCode(Project project, Code code);
  
  /**
   * Validate merge.
   *
   * @param project the project
   * @param concept1 the concept1
   * @param concept2 the concept2
   * @return the validation result
   */
  public ValidationResult validateMerge(Project project, Concept concept1, Concept concept2);

  /**
   * Gets the validation check names.
   *
   * @param project the project
   * @return the validation check names
   */
  public KeyValuePairList getValidationCheckNames(Project project);

  
}