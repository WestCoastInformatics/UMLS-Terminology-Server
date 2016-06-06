/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

/**
 * Enumeration of workflow action values.
 */
public enum WorkflowAction {

  /** The assign from existing action. */
  ASSIGN,
  /** The unassign action. */
  UNASSIGN,
  /** The save action. */
  SAVE,
  /** The finish action. */
  FINISH,
  /** The beta action. */
  BETA,
  /** The publish action. */
  PUBLISH,
  /** The cancel action. */
  CANCEL,   
  /**  The reassign. */
  REASSIGN,
}