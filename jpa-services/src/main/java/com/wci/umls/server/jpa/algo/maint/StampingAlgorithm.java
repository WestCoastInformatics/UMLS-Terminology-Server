/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.algo.action.ApproveMolecularAction;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.Worklist;

/**
 * Implementation of a algorithm to stamp a worklist.
 */
public class StampingAlgorithm extends AbstractAlgorithm {

  /** The worklist id. */
  private Long worklistId;

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

      final Worklist worklist = getWorklist(worklistId);
      int ct = 0;
      for (final TrackingRecord record : worklist.getTrackingRecords()) {
        lookupTrackingRecordConcepts(record);
        for (final Concept concept : record.getConcepts()) {
          ct++;
          final ApproveMolecularAction action = new ApproveMolecularAction();
          // Configure the action
          action.setProject(getProject());
          action.setActivityId(getActivityId());
          action.setConceptId(concept.getId());
          action.setConceptId2(null);
          action.setLastModifiedBy(getLastModifiedBy());
          action.setLastModified(concept.getLastModified() != null ? 
              concept.getLastModified().getTime() : null);
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
      logInfo("Finished STAMPING");

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

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "worklistId"
    }, p);

    worklistId = Long.valueOf(p.getProperty("worklistId"));
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param =
        new AlgorithmParameterJpa("Worklist Id", "worklistId", "Worklist id.",
            "e.g. 12345", 20, AlgorithmParameter.Type.INTEGER);
    params.add(param);
    return params;
  }

}
