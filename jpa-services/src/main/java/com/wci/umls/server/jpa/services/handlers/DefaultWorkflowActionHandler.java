/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler implements WorkflowActionHandler {

  /**
   * Instantiates an empty {@link DefaultWorkflowActionHandler}.
   *
   * @throws Exception the exception
   */
  public DefaultWorkflowActionHandler() throws Exception {
    super();
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    // n/a
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default workflow handler";
  }

  /* see superclass */
  @Override
  public TrackingRecordList findAvailableWork(Project project, UserRole role,
    PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();

    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    sb.append(" AND ");
    if (role == null) {
      sb.append("userRole:[* TO *]");
    } else {
      sb.append("userRole:" + role.name());
    }
    sb.append(" AND ").append("( NOT worklistName:[* TO *])");

    return service.findTrackingRecordsForQuery(sb.toString(), pfs);

  }

  /* see superclass */
  @Override
  public WorklistList findAvailableWorklists(Project project, UserRole role,
    PfsParameter pfs, WorkflowService service) throws Exception {

    final StringBuilder sb = new StringBuilder();

    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    sb.append(" AND ");
    if (role == null) {
      sb.append("userRole:[* TO *]");
    } else {
      sb.append("userRole:" + role.name());
    }
    sb.append(" AND ").append("(NOT editor:[* TO *])");

    return service.findWorklistsForQuery(sb.toString(), pfs);

  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Project project,
    Worklist worklist, User user, UserRole userRole,
    WorkflowAction workflowAction, WorkflowService service) throws Exception {
    ValidationResult result = new ValidationResultJpa();

    // An author cannot do review work
    if (userRole == UserRole.AUTHOR
        && worklist.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
        && workflowAction == WorkflowAction.ASSIGN) {
      result
          .addError("User does not have permissions to perform this action - "
              + workflowAction + ", " + user);
      return result;
    }

    // Validate actions that workflow status will allow
    boolean flag = false;
    switch (workflowAction) {

      case ASSIGN:

        boolean authorFlag =
            worklist.getAuthors().size() == 0
                && userRole == UserRole.AUTHOR
                && EnumSet.of(WorkflowStatus.NEW).contains(
                    worklist.getWorkflowStatus());

        boolean reviewerFlag =
            worklist.getReviewers().size() == 0
                && userRole == UserRole.REVIEWER
                && EnumSet.of(WorkflowStatus.EDITING_DONE).contains(
                    worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case UNASSIGN:
        // an "assigned" state must be present
        authorFlag =
            worklist.getAuthors().size() == 1
                && userRole == UserRole.AUTHOR
                && user.getUserName().equals(worklist.getAuthors().get(0))
                && EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE).contains(
                    worklist.getWorkflowStatus());

        reviewerFlag =
            worklist.getReviewers().size() == 1
                && userRole == UserRole.REVIEWER
                && user.getUserName().equals(worklist.getReviewers().get(0))
                && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS).contains(
                    worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;

        break;

      case REASSIGN:
        flag = false;
        break;

      case SAVE:
        // dependent on user role
        authorFlag =
            userRole == UserRole.AUTHOR
                && EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                    WorkflowStatus.EDITING_DONE).contains(
                    worklist.getWorkflowStatus());
        reviewerFlag =
            userRole == UserRole.REVIEWER
                && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case FINISH:
        // dependent on project role
        authorFlag =
            userRole == UserRole.AUTHOR
                && EnumSet.of(WorkflowStatus.NEW,
                    WorkflowStatus.EDITING_IN_PROGRESS).contains(
                    worklist.getWorkflowStatus());
        reviewerFlag =
            userRole == UserRole.REVIEWER
                && EnumSet.of(WorkflowStatus.REVIEW_NEW,
                    WorkflowStatus.REVIEW_IN_PROGRESS,
                    WorkflowStatus.REVIEW_DONE).contains(
                    worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      default:
        throw new LocalException("Illegal workflow action - " + workflowAction);
    }

    if (!flag) {
      result.addError("Invalid workflowAction for worklist workflow status: "
          + (user != null ? user.getUserName() : "") + "," + userRole + ", "
          + workflowAction + ", "
          + (worklist != null ? worklist.getWorkflowStatus() : "") + ", "
          + (worklist != null ? worklist.getId() : ""));
    }

    return result;
  }

  /* see superclass */
  @Override
  public Worklist performWorkflowAction(Project project, Worklist worklist,
    User user, UserRole userRole, WorkflowAction workflowAction,
    WorkflowService service) throws Exception {

    switch (workflowAction) {
      case ASSIGN:

        // Author case
        if (userRole == UserRole.AUTHOR) {
          worklist.getAuthors().add(user.getUserName());
          worklist.setWorkflowStatus(WorkflowStatus.NEW);
          worklist.getWorkflowStateHistory().put("Assigned", new Date());
        }

        // Reviewer case
        else if (userRole == UserRole.REVIEWER) {
          worklist.getReviewers().add(user.getUserName());
          worklist.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);
        }
        break;

      case UNASSIGN:
        // For authoring, removes the author and sets workflow status
        // back
        if (EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
            WorkflowStatus.EDITING_DONE).contains(worklist.getWorkflowStatus())) {

          worklist.setWorkflowStatus(WorkflowStatus.NEW);
          worklist.getAuthors().remove(user.getUserName());
        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (EnumSet.of(WorkflowStatus.REVIEW_NEW,
            WorkflowStatus.REVIEW_IN_PROGRESS).contains(
            worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
          worklist.getReviewers().remove(user.getUserName());
        }
        break;

      case REASSIGN:
        // N/a
        break;

      case SAVE:
        // AUTHOR - NEW becomes EDITING_IN_PROGRESS
        if (userRole == UserRole.AUTHOR
            && EnumSet.of(WorkflowStatus.NEW).contains(
                worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // REVIEWER - REVIEWER_NEW becomes REVIEW_IN_PROGRESS
        else if (userRole == UserRole.REVIEWER
            && EnumSet.of(WorkflowStatus.REVIEW_NEW).contains(
                worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        // EDITING_IN_PROGRESS, EDITING_DONE, REVIEW_IN_PROGRESS, REVIEW_DONE
        break;

      case FINISH:
        // EDITING_IN_PROGRESS => EDITING_DONE (and mark as not for authoring)
        if (EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS)
            .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
          worklist.getWorkflowStateHistory().put("Returned", new Date());
        }

        // REVIEW_NEW, REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (EnumSet.of(WorkflowStatus.REVIEW_NEW,
            WorkflowStatus.REVIEW_IN_PROGRESS).contains(
            worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
          worklist.getWorkflowStateHistory().put("Stamped", new Date());
        }

        // REVIEW_DONE => READY_FOR_PUBLICATION
        else if (EnumSet.of(WorkflowStatus.REVIEW_DONE).contains(
            worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          worklist.getWorkflowStateHistory().put("Done", new Date());
        }

        // Otherwise status stays the same
        break;

      default:
        throw new LocalException("Illegal workflow action - " + workflowAction);
    }

    service.updateWorklist(worklist);

    return worklist;
  }

  /* see superclass */
  @Override
  public TrackingRecordList findAssignedWork(Project project, String userName,
    PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();

    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }
    sb.append(" AND ");
    if (userName == null || userName.equals("")) {
      sb.append("userName:[* TO *]");
    } else {
      sb.append("userName:" + userName);
    }
    sb.append(" AND ").append("worklistName:[* TO *]");

    return service.findTrackingRecordsForQuery(sb.toString(), pfs);
  }

  /* see superclass */
  @Override
  public WorklistList findAssignedWorklists(Project project, String userName,
    PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();

    if (project == null) {
      sb.append("projectId:[* TO *]");
    } else {
      sb.append("projectId:" + project.getId());
    }

    sb.append(" AND ").append(" editor:").append(userName);

    return service.findWorklistsForQuery(sb.toString(), pfs);
  }

}
