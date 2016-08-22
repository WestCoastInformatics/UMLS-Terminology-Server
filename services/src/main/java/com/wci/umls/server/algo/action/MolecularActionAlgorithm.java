/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.algo.action;

import com.wci.umls.server.Project;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.model.content.Concept;

/**
 * Represents an algorithm for performing an action.
 */
public interface MolecularActionAlgorithm extends Algorithm {

  /**
   * Returns the concept.
   *
   * @return the concept
   */
  public Concept getConcept();

  /**
   * Returns the concept2.
   *
   * @return the concept2
   */
  public Concept getConcept2();

  /**
   * Returns the user name.
   *
   * @return the user name
   */
  public String getUserName();

  /**
   * Returns the last modified.
   *
   * @return the last modified
   */
  public Long getLastModified();

  /**
   * Returns the change status flag.
   *
   * @return the change status flag
   */
  public boolean getChangeStatusFlag();

  /**
   * Sets the change status flag.
   *
   * @param changeStatusFlag the change status flag
   */
  public void setChangeStatusFlag(boolean changeStatusFlag);

  /**
   * This method is responsible for locking the concepts involved in the action,
   * verifying the dirty last modified date, and creating the initial molecular
   * action to be used by this action.
   * 
   * NOTE: this action is its own content service, so any related usage of
   * content service should take that into account (e.g. in a REST call).
   *
   * @param project the project
   * @param conceptId the concept id
   * @param conceptId2 the concept id2
   * @param userName the user name
   * @param lastModified the last modified
   * @param molecularActionFlag the molecular action flag
   * @throws Exception the exception
   */
  public void initialize(Project project, Long conceptId, Long conceptId2,
    String userName, Long lastModified, boolean molecularActionFlag)
    throws Exception;

}
