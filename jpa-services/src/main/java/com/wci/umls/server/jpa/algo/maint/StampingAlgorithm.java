/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;
import com.wci.umls.server.jpa.algo.action.ApproveMolecularAction;
import com.wci.umls.server.jpa.algo.action.UpdateConceptMolecularAction;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.Checklist;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * Implementation of a algorithm to stamp a worklist.
 */
public class StampingAlgorithm extends AbstractAlgorithm {

  /** The worklist id. */
  private Long worklistId;
  
  /**  The checklist id. */
  private Long checklistId;
  
  /**  Indicates if the action should be to approve or to unapprove. */
  private boolean approve;

  /**
   * Instantiates an empty {@link StampingAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public StampingAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("STAMPING");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("Stamping requires a project to be set");
    }

    if (worklistId == null || getWorklist(worklistId) == null) {
      throw new Exception("Stamping requires a valid worklist id.");
    }

    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting STAMPING");

    try {
      // precollect records based on if checklistId or worklistId is set
      List<TrackingRecord> records = new ArrayList<>();
      if (worklistId != null) {
        final Worklist worklist = getWorklist(worklistId);
        records = worklist.getTrackingRecords();
      } else if (checklistId != null){
        final Checklist checklist = getChecklist(checklistId);
        records = checklist.getTrackingRecords();
      } else {
        throw new Exception("Expecting either a worklist or checklist id.");
      }
      int ct = 0;
      for (final TrackingRecord record : records) {
        lookupTrackingRecordConcepts(record);
        for (final Concept c : record.getConcepts()) {
          // skip those that don't need action
          if (approve && c.getWorkflowStatus() != WorkflowStatus.NEEDS_REVIEW) {
            continue;
          }
          if (!approve && c.getWorkflowStatus() == WorkflowStatus.NEEDS_REVIEW) {
            continue;
          }
          ct++;
          AbstractMolecularAction action;
          if (approve) {
            action = new ApproveMolecularAction();
          } else {
            action = new UpdateConceptMolecularAction();
            ((UpdateConceptMolecularAction)action).setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
          }
          // set workflowStatus action to NEEDS_REVIEW
          final Concept concept = action.getConcept(c.getId());
          // Configure the action
          action.setProject(getProject());
          action.setActivityId(getActivityId());
          action.setConceptId(concept.getId());
          action.setConceptId2(null);
          action.setLastModifiedBy(getLastModifiedBy());
          action.setLastModified(concept.getLastModified().getTime());
          action.setOverrideWarnings(true);
          action.setTransactionPerOperation(false);
          action.setMolecularActionFlag(true);
          action.setChangeStatusFlag(true);

          // Perform the action
          final ValidationResult validationResult =
              action.performMolecularAction(action);

          // If the action failed, bail out now.
          if (!validationResult.isValid()) {
            logError("  unable to approve " + concept.getId());
            for (final String error : validationResult.getErrors()) {
              logError("    error = " + error);
            }
          }
        }
      }

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("  count = " + ct);
      if (worklistId != null) {
        logInfo("  worklistId = " + worklistId);
      } else if (checklistId != null) {
        logInfo("  checklistId = " + checklistId);
      }
      logInfo("  approve = " + approve);
      if (approve) {
        logInfo("Finished STAMPING");
      } else {
        logInfo("Finished UNAPPROVING");
      }

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /**
   * Sets the worklist id.
   *
   * @param worklistId the worklist id
   */
  public void setWorklistId(Long worklistId) {
    this.worklistId = worklistId;
  }
  
  /**
   * Sets the checklist id.
   *
   * @param checklistId the checklist id
   */
  public void setChecklistId(Long checklistId) {
    this.checklistId = checklistId;
  }
  
  /**
   * Sets the approve.
   *
   * @param approve the approve
   */
  public void setApprove(boolean approve) {
    this.approve = approve;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        
    }, p);

    worklistId = Long.valueOf(p.getProperty("worklistId"));
    checklistId = Long.valueOf(p.getProperty("checklistId"));
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param =
        new AlgorithmParameterJpa("Worklist Id", "worklistId", "Worklist id.",
            "e.g. 12345", 20, AlgorithmParameter.Type.INTEGER);
    params.add(param);
    param =
        new AlgorithmParameterJpa("Checklist Id", "checklistId", "Checklist id.",
            "e.g. 12345", 20, AlgorithmParameter.Type.INTEGER);
    params.add(param);
    return params;
  }

}
