/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.inversion;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a collection of versioned sabs and their reserved starting and ending
 * source atom ids.
 */
public interface SourceIdRange extends HasTerminology, HasLastModified {

  /**
   * Sets the begin source id.
   *
   * @param beginSourceId the new begin source id
   */
  public void setBeginSourceId(Long beginSourceId);

  /**
   * Gets the begin source id.
   *
   * @return the begin source id
   */
  public Long getBeginSourceId();

  /**
   * Sets the end source id.
   *
   * @param endSourceId the new end source id
   */
  public void setEndSourceId(Long endSourceId);

  /**
   * Gets the end source id.
   *
   * @return the end source id
   */
  public Long getEndSourceId();

  /**
   * Gets the project.
   *
   * @return the project
   */
  public Project getProject();

  /**
   * Sets the project.
   *
   * @param project the new project
   */
  public void setProject(Project project);

}
