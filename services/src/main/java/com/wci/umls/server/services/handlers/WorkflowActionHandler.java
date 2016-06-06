/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.services.WorkflowService;

/**
 * Generically represents a handler for performing workflow actions.
 */
public interface WorkflowActionHandler extends Configurable {

  /**
   * Validate workflow action.
   *
   * @param concept the concept
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param service the service
   * @return the validation result
   * @throws Exception the exception
   */
  public ValidationResult validateWorkflowAction(Concept concept, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception;



  /**
   * Perform workflow action.
   *
   * @param concept the concept
   * @param user the user
   * @param projectRole the project role
   * @param action the action
   * @param service the service
   * @return the tracking record
   * @throws Exception the exception
   */
  public TrackingRecord performWorkflowAction(Concept concept, User user,
    UserRole projectRole, WorkflowAction action, WorkflowService service)
    throws Exception;



}
