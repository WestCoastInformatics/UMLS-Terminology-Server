/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.model.workflow.Checklist;

/**
 * Implementation of an algorithm to create report table checklists.
 */
public class ReportChecklistAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  /**
   * Instantiates an empty {@link ReportChecklistAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ReportChecklistAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("REPORTCHECKLIST");
    setLastModifiedBy("admin");
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception(
          "Report Checklist Algorithm requires a project to be set");
    }

    // Check the input directories

    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting REPORTCHECKLIST");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    try {

      logInfo("[ReportChecklist] Creating the report table checklists");
      commitClearBegin();

      // Get all terminologies referenced in the sources.src file
      // terminologies.left = Terminology
      // terminolgoies.right = Version
      Set<Pair<String, String>> terminologies = new HashSet<>();
      terminologies = getReferencedTerminologies();

      setSteps(terminologies.size());

      // For each terminology, create four checklists
      for (Pair<String, String> terminology : terminologies) {
        final String term = terminology.getLeft();
        final String version = terminology.getRight();

        checkCancel();

        // All four queries start with the same clauses
        final String queryPrefix =
            "atoms.terminology:" + term + " AND atoms.version:" + version;

        Checklist checklist = computeChecklist(getProject(),
            queryPrefix + " AND atoms.workflowStatus:NEEDS_REVIEW",
            QueryType.LUCENE, "chk_" + term + "_" + version + "_NEEDS_REVIEW",
            null, true);
        logInfo("[ReportChecklist] Created chk_" + term + "_" + version
            + "_NEEDS_REVIEW checklist, containing "
            + checklist.getTrackingRecords().size() + " tracking records.");

        checklist = computeChecklist(getProject(),
            queryPrefix + " AND atoms.workflowStatus:DEMOTION",
            QueryType.LUCENE, "chk_" + term + "_" + version + "_DEMOTION", null,
            true);
        logInfo("[ReportChecklist] Created chk_" + term + "_" + version
            + "_DEMOTION checklist, containing "
            + checklist.getTrackingRecords().size() + " tracking records.");

        checklist = computeChecklist(getProject(),
            queryPrefix + " AND atoms.workflowStatus:READY_FOR_PUBLICATION",
            QueryType.LUCENE,
            "chk_" + term + "_" + version + "_READY_FOR_PUBLICATION", null,
            true);
        logInfo("[ReportChecklist] Created chk_" + term + "_" + version
            + "_READY_FOR_PUBLICATION checklist, containing "
            + checklist.getTrackingRecords().size() + " tracking records.");

        checklist = computeChecklist(getProject(),
            queryPrefix + " AND atoms.lastModifiedBy:ENG-*", QueryType.LUCENE,
            "chk_" + term + "_" + version + "_MIDMERGES", null, true);
        logInfo("[ReportChecklist] Created chk_" + term + "_" + version
            + "_MIDMERGES checklist, containing "
            + checklist.getTrackingRecords().size() + " tracking records.");

        // Update the progress
        updateProgress();
      }

      commitClearBegin();

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished REPORTCHECKLIST");

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /**
   * Returns the parameters.
   *
   * @return the parameters
   */
  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  @Override
  public String getDescription() {
    return "Generates standard report table checklists for the insertion.";
  }
}