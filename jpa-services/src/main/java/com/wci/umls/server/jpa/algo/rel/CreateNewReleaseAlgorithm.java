/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.codehaus.plexus.util.FileUtils;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * Algorithm for creating a new release.
 */
public class CreateNewReleaseAlgorithm extends AbstractAlgorithm {

  /** The steps. */
  private int steps = 2;

  /** The previous progress. */
  private int previousProgress = 0;

  /** The step ct. */
  private int stepCt = 0;

  /** The bypass validation checks. */
  private boolean bypassValidationChecks = false;

  /**
   * Instantiates an empty {@link CreateNewReleaseAlgorithm}.
   *
   * @throws Exception the exception
   */
  public CreateNewReleaseAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("CREATENEWRELEASE");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();

    // Verify that there is a "NET" directory at input path
    final String path = config.getProperty("source.data.dir") + "/"
        +  getProcess().getInputPath();
    final File dir = new File(path, "NET");
    if (!dir.exists()) {
      result.addError("Release requires a 'NET' directory at the input path "
          + dir.getPath());
    }

    // remaining checks are bypassable
    if (bypassValidationChecks) {
      return result;
    }

    // Verify that there are no concepts with workflowStatus == NEEDS_REVIEW
    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    final SearchResultList unreviewedConcepts =
        findConcepts(getProject().getTerminology(), getProject().getVersion(),
            Branch.ROOT, " workflowStatus:NEEDS_REVIEW", pfs);
    if (unreviewedConcepts.size() > 0) {
      result.addError(
          "Release requires no concepts have NEEDS_REVIEW workflow status");
    }

    // Verify that all worklists in the epoch for the project are
    // READY_FOR_PUBLICATION
    final WorklistList notReadyWorklists = findWorklists(getProject(),
        " NOT workflowStatus:READY_FOR_PUBLICATION", pfs);
    if (notReadyWorklists.size() > 0) {
      result.addError(
          "Release requires all worklists in epoch to be READY_FOR_PUBLICATION");
    }

    // Verify that all “required” bins have zero counts
    for (final WorkflowConfig config : getWorkflowConfigs(getProject())) {
      for (final WorkflowBin bin : getWorkflowBins(getProject(),
          config.getType())) {
        if (bin.isRequired()) {
          if (bin.getStats().size() != 0) {
            result.addError(
                "Release requires that all required bins have a count of 0 "
                    + bin.getName());
          }
        }
      }
    }

    //
    // +
    //
    return result;

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Starting Create new release");

    steps = 2;
    previousProgress = 0;
    stepCt = 0;

    // Create a release directory
    File releaseDir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion());
    if (!releaseDir.exists()) {
      releaseDir.mkdirs();
    }
    logInfo("  releaseDir = " + releaseDir.getPath());

    // Create “META”, “METASUBSET”, “log”, “QA” sub-directories.
    logInfo("  Create “META”, “METASUBSET”, “log”, “QA” sub-directories.");
    File metaDir = new File(releaseDir, "META");
    if (!metaDir.exists()) {
      metaDir.mkdir();
    }
    File metasubsetDir = new File(releaseDir, "METASUBSET");
    if (!metasubsetDir.exists()) {
      metasubsetDir.mkdir();
    }
    File logDir = new File(releaseDir, "log");
    if (!logDir.exists()) {
      logDir.mkdir();
    }
    File qaDir = new File(releaseDir, "QA");
    if (!qaDir.exists()) {
      qaDir.mkdir();
    }

    updateProgress();

    // Add a release info for the current release
    logInfo("  Add release info for the current release = "
        + getProject().getVersion());
    ReleaseInfo releaseInfo = new ReleaseInfoJpa();
    releaseInfo.setTerminology(getProject().getTerminology());
    releaseInfo.setVersion(getProcess().getVersion());
    releaseInfo.setName(getProcess().getVersion());
    releaseInfo.setDescription("Base release for " + releaseInfo.getName());
    releaseInfo.setPlanned(true);
    releaseInfo.setPublished(false);
    releaseInfo.setReleaseBeginDate(new Date());
    releaseInfo.setTimestamp(new Date());
    addReleaseInfo(releaseInfo);

    logInfo("Finished Create new release");
    updateProgress();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting reset of Create new release");

    ReleaseInfo releaseInfo =
        this.getCurrentReleaseInfo(getProject().getTerminology());
    if (releaseInfo.getName().equals(getProject().getVersion())) {
      removeReleaseInfo(releaseInfo.getId());
    }

    File releaseDir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion());
    FileUtils.deleteDirectory(releaseDir);

    logInfo("Finished reset of Create new release");

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "bypassValidationChecks"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    bypassValidationChecks =
        Boolean.valueOf(p.getProperty("bypassValidationChecks"));
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    AlgorithmParameter param = new AlgorithmParameterJpa(
        "Bypass validation checks", "bypassValidationChecks",
        "Indicates whether or not to skip validation checks.", "e.g. false", 0,
        AlgorithmParameter.Type.BOOLEAN, "");
    params.add(param);

    return params;
  }

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepCt++;
    int currentProgress = (int) ((100.0 * stepCt / steps));
    if (currentProgress > previousProgress) {
      fireProgressEvent(currentProgress,
          "CREATE RELEASE progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }
}
