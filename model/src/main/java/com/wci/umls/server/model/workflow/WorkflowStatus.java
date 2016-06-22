/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

/**
 * Enumeration of workflow status values.
 */
public enum WorkflowStatus {

  /** The new status. */
  NEW,

  /** The editing in progress status. */
  EDITING_IN_PROGRESS,

  /** The editing done status. */
  EDITING_DONE,

  /** The review new status. */
  REVIEW_NEW,

  /** The review in progress status. */
  REVIEW_IN_PROGRESS,

  /** The review done status. */
  REVIEW_DONE,

  /**  ready for publication. */
  READY_FOR_PUBLICATION,

  /** The published status. */
  PUBLISHED,
  
  /** The needs review. */
  NEEDS_REVIEW,
  
  /** The demotion. */
  DEMOTION;


}