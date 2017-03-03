/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Date;
import java.util.EnumSet;
import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.maint.StampingAlgorithm;
import com.wci.umls.server.jpa.helpers.WorklistListJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowAction;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;
import com.wci.umls.server.services.WorkflowService;
import com.wci.umls.server.services.handlers.WorkflowActionHandler;

/**
 * Default implementation of {@link WorkflowActionHandler}.
 */
public class DefaultWorkflowActionHandler extends AbstractConfigurable
    implements WorkflowActionHandler {

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
  public TrackingRecordList findAvailableWork(Project project, String userName,
    UserRole role, PfsParameter pfs, WorkflowService service) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public WorklistList findAvailableWorklists(Project project, String userName,
    UserRole role, PfsParameter pfs, WorkflowService service) throws Exception {

    final StringBuilder sb = new StringBuilder();
    sb.append("epoch:" + service.getCurrentWorkflowEpoch(project).getName());
    if (UserRole.AUTHOR == role) {
      sb.append(" AND workflowStatus:NEW AND NOT authors:[* TO *]");
    } else if (UserRole.REVIEWER == role) {
      // EITHER things for review, or not yet authored worklists created by this
      // user
      sb.append(" AND ((workflowStatus:EDITING_DONE AND NOT reviewers:[* TO *])"
          + " OR (workflowStatus:NEW AND NOT authors:[* TO *] AND lastModifiedBy:"
          + userName + "))");

    } else if (UserRole.ADMINISTRATOR == role) {
      // n/a, query as is.
    } else {
      throw new Exception("Unexpected user role " + role);
    }
    return service.findWorklists(project, sb.toString(), pfs);
  }

  /* see superclass */
  @Override
  public boolean isAvailable(Worklist worklist, String userName, UserRole role)
    throws Exception {
    if (role == UserRole.AUTHOR) {
      return worklist.getWorkflowStatus() == WorkflowStatus.NEW
          && worklist.getAuthors().size() == 0;
    }

    else if (role == UserRole.REVIEWER) {
      return worklist.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
          && worklist.getReviewers().size() == 0;
    }
    return false;
  }

  /* see superclass */
  @Override
  public ValidationResult validateWorkflowAction(Project project,
    Worklist worklist, String userName, UserRole role,
    WorkflowAction workflowAction, WorkflowService service) throws Exception {
    ValidationResult result = new ValidationResultJpa();

    // An author cannot do review work
    if (role == UserRole.AUTHOR
        && worklist.getWorkflowStatus() == WorkflowStatus.EDITING_DONE
        && workflowAction == WorkflowAction.ASSIGN) {
      result.addError("User does not have permissions to perform this action - "
          + workflowAction + ", " + userName);
      return result;
    }

    // Validate actions that workflow status will allow
    boolean flag = false;
    switch (workflowAction) {

      case ASSIGN:

        boolean authorFlag = worklist.getAuthors().size() == 0
            && role == UserRole.AUTHOR && EnumSet.of(WorkflowStatus.NEW)
                .contains(worklist.getWorkflowStatus());

        boolean reviewerFlag =
            worklist.getReviewers().size() == 0 && role == UserRole.REVIEWER
                && EnumSet.of(WorkflowStatus.EDITING_DONE)
                    .contains(worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case UNASSIGN:
        // an "assigned" state must be present
        authorFlag =
            worklist.getAuthors().size() == 1 && role == UserRole.AUTHOR
                && userName.equals(worklist.getAuthors().get(0))
                && EnumSet
                    .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                        WorkflowStatus.EDITING_DONE)
                    .contains(worklist.getWorkflowStatus());

        reviewerFlag =
            worklist.getReviewers().size() == 1 && role == UserRole.REVIEWER
                && userName.equals(worklist.getReviewers().get(0))
                && EnumSet
                    .of(WorkflowStatus.REVIEW_NEW,
                        WorkflowStatus.REVIEW_IN_PROGRESS)
                    .contains(worklist.getWorkflowStatus());

        boolean administratorFlag = role == UserRole.ADMINISTRATOR && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE, WorkflowStatus.REVIEW_NEW,
                WorkflowStatus.REVIEW_IN_PROGRESS)
            .contains(worklist.getWorkflowStatus());

        flag = authorFlag || reviewerFlag || administratorFlag;

        break;

      case REASSIGN:
        flag = false;
        break;

      case SAVE:
        // dependent on user role
        authorFlag = role == UserRole.AUTHOR && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE)
            .contains(worklist.getWorkflowStatus());
        reviewerFlag = role == UserRole.REVIEWER && EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      case APPROVE:
        // ONLY FOR REVIEWER
        flag = role == UserRole.REVIEWER && EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(worklist.getWorkflowStatus());

        break;

      case FINISH:
        // dependent on project role
        authorFlag = role == UserRole.AUTHOR && EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS)
            .contains(worklist.getWorkflowStatus());
        reviewerFlag = role == UserRole.REVIEWER && EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(worklist.getWorkflowStatus());
        flag = authorFlag || reviewerFlag;
        break;

      default:
        throw new LocalException("Illegal workflow action - " + workflowAction);
    }

    if (!flag) {
      result.addError("Invalid action for worklist: " + userName + "," + role
          + ", " + workflowAction + ", "
          + (worklist != null ? worklist.getWorkflowStatus() : "") + ", "
          + (worklist != null ? worklist.getId() : ""));
    }

    return result;
  }

  /* see superclass */
  @Override
  public Worklist performWorkflowAction(Project project, Worklist worklist,
    String userName, UserRole role, WorkflowAction workflowAction,
    WorkflowService service) throws Exception {

    switch (workflowAction) {
      case ASSIGN:

        // Author case
        if (role == UserRole.AUTHOR) {
          worklist.getAuthors().add(userName);
          worklist.setWorkflowStatus(WorkflowStatus.NEW);
          worklist.getWorkflowStateHistory().put("Assigned", new Date());
        }

        // Reviewer case
        else if (role == UserRole.REVIEWER) {
          worklist.getReviewers().add(userName);
          worklist.setWorkflowStatus(WorkflowStatus.REVIEW_NEW);
          worklist.getWorkflowStateHistory().put("Review Assigned", new Date());
        }
        break;

      case UNASSIGN:
        // For authoring, removes the author and sets workflow status
        // back
        if (EnumSet
            .of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS,
                WorkflowStatus.EDITING_DONE)
            .contains(worklist.getWorkflowStatus())) {

          worklist.setWorkflowStatus(WorkflowStatus.NEW);
          worklist.getAuthors().remove(userName);
          worklist.getWorkflowStateHistory().remove("Assigned");
        }
        // For review, it removes the reviewer and sets the status back to
        // EDITING_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
          worklist.getReviewers().remove(userName);
          worklist.getWorkflowStateHistory().remove("Review Assigned");
        }
        break;

      case REASSIGN:
        // N/a
        break;

      case SAVE:
        // AUTHOR - NEW becomes EDITING_IN_PROGRESS
        if (role == UserRole.AUTHOR && EnumSet.of(WorkflowStatus.NEW)
            .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.EDITING_IN_PROGRESS);
        }
        // REVIEWER - REVIEWER_NEW becomes REVIEW_IN_PROGRESS
        else if (role == UserRole.REVIEWER
            && EnumSet.of(WorkflowStatus.REVIEW_NEW)
                .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.REVIEW_IN_PROGRESS);
        }
        // all other cases, status remains the same
        // EDITING_IN_PROGRESS, EDITING_DONE, REVIEW_IN_PROGRESS, REVIEW_DONE
        break;

      case APPROVE:

        // REVIEW_NEW, REVIEW_IN_PROGRESS => READY_FOR_PUBLICATION
        if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS)
            .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.REVIEW_DONE);
          worklist.getWorkflowStateHistory().put("Stamped", new Date());
        }

        // Otherwise status stays the same
        break;

      case FINISH:
        // EDITING_IN_PROGRESS => EDITING_DONE (and mark as not for authoring)
        if (EnumSet.of(WorkflowStatus.NEW, WorkflowStatus.EDITING_IN_PROGRESS)
            .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.EDITING_DONE);
          worklist.getWorkflowStateHistory().put("Returned", new Date());
        }

        // REVIEW_NEW, REVIEW_IN_PROGRESS => REVIEW_DONE
        else if (EnumSet
            .of(WorkflowStatus.REVIEW_NEW, WorkflowStatus.REVIEW_IN_PROGRESS,
                WorkflowStatus.REVIEW_DONE)
            .contains(worklist.getWorkflowStatus())) {
          worklist.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
          worklist.getWorkflowStateHistory().put("Done", new Date());
        }

        // Otherwise status stays the same
        break;
      default:
        throw new LocalException("Illegal workflow action - " + workflowAction);
    }

    service.updateWorklist(worklist);

    // Stamp the worklist when we send it for publication.
    if (worklist.getWorkflowStatus() == WorkflowStatus.REVIEW_DONE
        && workflowAction == WorkflowAction.APPROVE) {
      final StampingAlgorithm algo = new StampingAlgorithm();

      algo.setProject(worklist.getProject());
      algo.setTerminology(worklist.getProject().getTerminology());
      algo.setActivityId(worklist.getName());
      algo.setLastModifiedBy("S-" + userName);
      algo.setWorklistId(worklist.getId());
      algo.setApprove(true);

      final ValidationResult result = algo.checkPreconditions();
      if (!result.isValid()) {
        throw new LocalException("Stamping failed - " + result.getErrors());
      }
      algo.compute();
    }

    if (worklist.getWorkflowStatus() == WorkflowStatus.READY_FOR_PUBLICATION
        && workflowAction == WorkflowAction.FINISH) {

      // Mark all tracking records as finished
      for (TrackingRecord trackingRecord : worklist.getTrackingRecords()) {
        trackingRecord.setFinished(true);
        service.updateTrackingRecord(trackingRecord);
      }
    }

    return worklist;
  }

  /* see superclass */
  @Override
  public TrackingRecordList findAssignedWork(Project project, String userName,
    UserRole role, PfsParameter pfs, WorkflowService service) throws Exception {
    final StringBuilder sb = new StringBuilder();

    sb.append(" AND ");
    if (userName == null || userName.equals("")) {
      sb.append("lastModifiedBy:[* TO *]");
    } else {
      sb.append("lastModifiedBy:" + userName);
    }
    sb.append(" AND ").append("worklistName:[* TO *]");

    return service.findTrackingRecords(project, sb.toString(), pfs);
  }

  /* see superclass */
  @Override
  public WorklistList findAssignedWorklists(Project project, String userName,
    UserRole role, PfsParameter pfs, WorkflowService service) throws Exception {
    if (role == UserRole.AUTHOR) {
      return service.findWorklists(project,
          "epoch:" + service.getCurrentWorkflowEpoch(project).getName()
              + " AND authors:" + userName + " AND NOT reviewers:[* TO *]"
              + " AND NOT workflowStatus:EDITING_DONE AND NOT workflowStatus:READY_FOR_PUBLICATION ",
          pfs);
    } else if (role == UserRole.REVIEWER) {
      return service.findWorklists(project,
          "epoch:" + service.getCurrentWorkflowEpoch(project).getName()
              + " AND NOT workflowStatus:READY_FOR_PUBLICATION AND "
              + "(reviewers:" + userName + " OR (authors:" + userName
              + " AND NOT workflowStatus:EDITING_DONE AND NOT reviewers:[* TO *]))",
          pfs);
    }
    return new WorklistListJpa();
  }

  /* see superclass */
  @Override
  public TrackingRecordList findDoneWork(Project project, String userName,
    UserRole role, PfsParameter pfs, WorkflowService service) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public WorklistList findDoneWorklists(Project project, String userName,
    UserRole role, PfsParameter pfs, WorkflowService service) throws Exception {
    if (role == UserRole.AUTHOR) {
      return service.findWorklists(project,
          "epoch:" + service.getCurrentWorkflowEpoch(project).getName()
              + " AND authors:" + userName
              + " AND NOT workflowStatus:NEW AND NOT workflowStatus:EDITING_IN_PROGRESS ",
          pfs);
    } else if (role == UserRole.REVIEWER) {
      return service.findWorklists(project,
          "epoch:" + service.getCurrentWorkflowEpoch(project).getName()
              + "( authors:" + userName
              + " AND NOT workflowStatus:NEW AND NOT workflowStatus:EDITING_IN_PROGRESS) OR "
              + "( reviewers:" + userName
              + " AND NOT workflowStatus:REVIEW_NEW AND NOT workflowStatus:REVIEW_IN_PROGRESS)",
          pfs);
    }

    return new WorklistListJpa();
  }

}
