/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.Project;

/**
 * Represents a thing that has a project.
 */
public interface HasProject {

  /**
   * Returns the project.
   *
   * @return the project
   */
  public Project getProject();

  /**
   * Sets the project.
   *
   * @param project the project
   */
  public void setProject(Project project);

}