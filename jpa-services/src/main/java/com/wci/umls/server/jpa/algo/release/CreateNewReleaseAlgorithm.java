/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

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
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.WorklistList;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * Algorithm for creating a new release.
 */
public class CreateNewReleaseAlgorithm extends AbstractAlgorithm {

  /** The steps. */
  private int steps = 3;

  /** The previous progress. */
  private int previousProgress = 0;

  /** The step ct. */
  private int stepCt = 0;

  /** The bypass validation checks. */
  private boolean warnValidationChecks = false;

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
        + getProcess().getInputPath();
    final File dir = new File(path, "NET");
    if (!dir.exists()) {
      result.addError("Release requires a 'NET' directory at the input path "
          + dir.getPath());
    }

    // Verify that there is a "NET" directory at input path
    final File metadir = new File(path, "META");
    if (!metadir.exists()) {
      result.addError(
          "Release requires a 'META' directory at the input path with\n"
              + "template MRCOLS.RRF and MRFILES.RRF files in it:\n "
              + metadir.getPath());
    }

    // Verify that there are no concepts with workflowStatus == NEEDS_REVIEW
    final PfsParameter pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(1);
    final SearchResultList unreviewedConcepts = findConceptSearchResults(
        getProject().getTerminology(), getProject().getVersion(), Branch.ROOT,
        " workflowStatus:NEEDS_REVIEW", pfs);
    if (unreviewedConcepts.size() > 0) {
      final String msg =
          "Release requires no concepts have NEEDS_REVIEW workflow status";
      if (warnValidationChecks) {
        fireWarningEvent(msg);
        logWarn(msg);
      } else {
        result.addError(msg);
      }
    }

    // Verify that all worklists in the epoch for the project are
    // READY_FOR_PUBLICATION
    final WorklistList notReadyWorklists = findWorklists(getProject(),
        " NOT workflowStatus:READY_FOR_PUBLICATION", pfs);
    if (notReadyWorklists.size() > 0) {
      final String msg =
          "Release requires all worklists in epoch to be READY_FOR_PUBLICATION";
      if (warnValidationChecks) {
        fireWarningEvent(msg);
        logWarn(msg);
      } else {
        result.addError(msg);
      }

    }

    // Verify that all “required” bins have zero counts
    for (final WorkflowConfig config : getWorkflowConfigs(getProject())) {
      for (final WorkflowBin bin : getWorkflowBins(getProject(),
          config.getType())) {
        if (bin.isRequired()) {
          if (bin.getStats().size() != 0) {
            final String msg =
                "Release requires that all required bins have a count of 0 "
                    + bin.getName();
            if (warnValidationChecks) {
              fireWarningEvent(msg);
              logWarn(msg);
            } else {
              result.addError(msg);
            }
          }
        }
      }
    }

    // Project needs to be set
    if (getProject() == null) {
      throw new LocalException(
          "Create new release requires a project to be set");
    }

    // Make sure process terminology matches project terminology.
    if (!getProcess().getTerminology().equals(getProject().getTerminology())) {
      throw new LocalException(
          "Create new release requires the process' terminology to be set to the project terminology ("
              + getProject().getTerminology() + ")");
    }

    // Make sure process version is a 6-digit number that is greater than
    // the previous project terminology's most recent release
    final ReleaseInfo currenReleaseInfo =
        getCurrentReleaseInfo(getProject().getTerminology());
    if (getProcess().getVersion().length() != 6
        || !(Long.parseLong(getProcess().getVersion()) > Long
            .parseLong(currenReleaseInfo.getVersion()))) {
      throw new LocalException(
          "Create new release requires the process' version to be set a 6-digit number that is greater than the most recent releases' version ("
              + currenReleaseInfo.getVersion() + ")");
    }

    return result;

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting create new release");
    fireProgressEvent(0, "Starting");

    steps = 2;
    previousProgress = 0;
    stepCt = 0;

    // Create a release directory
    File releaseDir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion());
    if (!releaseDir.exists()) {
      logInfo("  Make release directories = " + releaseDir);
      releaseDir.mkdirs();
    }

    // Create directories
    final String[] dirs = new String[] {
        "META", "METASUBSET", "log", "QA", "FEEDBACK"
    };
    for (final String dir : dirs) {
      File lDir = new File(releaseDir, dir);
      if (!lDir.exists()) {
        logInfo("    " + dir);
        lDir.mkdir();
      }
    }

    updateProgress();

    // Add a release info for the current release
    final ReleaseInfo releaseInfo = new ReleaseInfoJpa();
    releaseInfo.setTerminology(getProject().getTerminology());
    releaseInfo.setVersion(getProcess().getVersion());
    releaseInfo.setName(getProcess().getVersion());
    releaseInfo.setDescription("Base release for " + releaseInfo.getName());
    releaseInfo.setPlanned(true);
    releaseInfo.setPublished(false);
    releaseInfo.setReleaseBeginDate(new Date());
    releaseInfo.setTimestamp(new Date());
    logInfo("  Add release info = " + releaseInfo);
    addReleaseInfo(releaseInfo);

    updateProgress();

    // Set first/last release based on release info
    final ReleaseInfo prevReleaseInfo =
        getPreviousReleaseInfo(getProject().getTerminology());
    for (final Terminology terminology : getTerminologies().getObjects()) {
      // Mark unpublished current terminologies with "first" release as this
      // release
      if (terminology.isCurrent() && terminology.getFirstReleases()
          .get(releaseInfo.getTerminology()) == null) {
        logInfo("    firstRelease = " + releaseInfo.getVersion() + " "
            + terminology.getTerminology());
        terminology.getFirstReleases().put(releaseInfo.getTerminology(),
            releaseInfo.getVersion());
      }

      // Mark non-current terminologies, previously published, with "last"
      // release as previous release
      else if (!terminology.isCurrent()
          && terminology.getFirstReleases()
              .get(releaseInfo.getTerminology()) != null
          && terminology.getLastReleases()
              .get(releaseInfo.getTerminology()) == null) {
        logInfo("    lastRelease = " + prevReleaseInfo.getVersion() + " "
            + terminology.getTerminology());
        terminology.getLastReleases().put(releaseInfo.getTerminology(),
            prevReleaseInfo.getVersion());
      }
    }

    updateProgress();
    fireProgressEvent(100, "Finished");
    logInfo("Finished Create new release");
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Reset create new release");

    // Undo firstReleases/lastReleases settings to current release info.
    // Set first/last release based on release info
    final ReleaseInfo releaseInfo =
        getCurrentReleaseInfo(getProject().getTerminology());
    final ReleaseInfo prevReleaseInfo =
        getPreviousReleaseInfo(getProject().getTerminology());
    for (final Terminology terminology : getTerminologies().getObjects()) {
      // Mark unpublished current terminologies with "first" release as this
      // release
      if (terminology.isCurrent() && terminology.getFirstReleases()
          .get(releaseInfo.getTerminology()).equals(releaseInfo.getVersion())) {
        logInfo("  reset firstRelease = " + releaseInfo.getVersion() + " "
            + terminology.getTerminology());
        terminology.getFirstReleases().remove(releaseInfo.getTerminology());
      }

      // Mark non-current terminologies, previously published, with "last"
      // release as previous release
      else if (!terminology.isCurrent()
          && terminology.getLastReleases().get(releaseInfo.getTerminology())
              .equals(prevReleaseInfo.getVersion())) {
        logInfo("  reset lastRelease = " + prevReleaseInfo.getVersion() + " "
            + terminology.getTerminology());
        terminology.getLastReleases().remove(releaseInfo.getTerminology());
      }
    }

    // IF matches this version, remove it
    if (releaseInfo.getName().equals(getProject().getVersion())) {
      logInfo("  Remove release info = " + releaseInfo);
      removeReleaseInfo(releaseInfo.getId());
    }

    // Cleanup all directories
    File releaseDir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion());
    logInfo("  Remove directories = " + releaseDir.getPath());
    FileUtils.deleteDirectory(releaseDir);
    logInfo("Finished reset of Create new release");

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "warnValidationChecks"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    warnValidationChecks =
        Boolean.valueOf(p.getProperty("warnValidationChecks"));
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    AlgorithmParameter param = new AlgorithmParameterJpa(
        "Warn validation checks", "warnValidationChecks",
        "Indicates whether or not to produce warnings for validation checks.",
        "e.g. false", 0, AlgorithmParameter.Type.BOOLEAN, "");
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
      checkCancel();
      fireProgressEvent(currentProgress,
          "CREATE RELEASE progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
