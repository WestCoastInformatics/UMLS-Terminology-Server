/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.WorkflowService;

/**
 * Generically represents a handler for performing workflow actions.
 */
public interface WorkflowActionHandler extends Configurable {

  /**
   * Find available work. Tracking records not associated with assigned
   * worklists.
   *
   * @param project the project
   * @param role the user
   * @param pfs the pfs
   * @param service WorkflowService
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAvailableWork(Project project, UserRole role,
    PfsParameter pfs, WorkflowService service) throws Exception;

  /**
   * Find available worklists. Worklists not assigned.
   *
   * @param project the project
   * @param userRole the user
   * @param pfs the pfs
   * @param service workflowService
   * @return the worklist list
   * @throws Exception the exception
   */
  public WorklistList findAvailableWorklists(Project project, UserRole userRole,
    PfsParameter pfs, WorkflowService service) throws Exception;

  /**
   * Validate workflow action.
   *
   * @param project the project
   * @param worklist the worklist
   * @param user the user
   * @param userRole the user role
   * @param workflowAction the workflow action
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Project project,
    Worklist worklist, User user, UserRole userRole,
    WorkflowAction workflowAction, WorkflowService service) throws Exception;

 
  /**
   * Perform workflow action.
   *
   * @param project the project
   * @param worklist the worklist
   * @param user the user
   * @param userRole the user role
   * @param workflowAction the workflow action
   * @param service the service
   * @return the worklist
   * @throws Exception the exception
   */
  public Worklist performWorkflowAction(Project project, Worklist worklist,
    User user, UserRole userRole, WorkflowAction workflowAction, WorkflowService service)
    throws Exception;

  /**
   * Find assigned work.
   *
   * @param project the project
   * @param userName the user name
   * @param pfs the pfs
   * @param service the service
   * @return the tracking record list
   * @throws Exception the exception
   */
  public TrackingRecordList findAssignedWork(Project project, String userName,
    PfsParameter pfs, WorkflowService service) throws Exception;

  /**
   * Find assigned worklists.
   *
   * @param project the project
   * @param userName the user name
   * @param pfs the pfs
   * @param service the service
   * @return the worklist list
   * @throws Exception the exception
   */
  public WorklistList findAssignedWorklists(Project project, String userName,
    PfsParameter pfs, WorkflowService service) throws Exception;
}
