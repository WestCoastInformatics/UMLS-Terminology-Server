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
    ValidationResult validationResult = new ValidationResultJpa();

    //Verify that there is a “NET” directory at input path 
    String path = config.getProperty("source.data.dir") + "/" + getProcess().getInputPath();
    File dir = new File(path, "NET");
    if (!dir.exists()) {
      throw new Exception(
          "Creating a new release requires a 'NET' directory at the input path.");
    }
    
    // remaining checks are bypassable
    if (bypassValidationChecks) {
      return validationResult;
    }
    
    //Verify that there are no concepts with workflowStatus == NEEDS_REVIEW
    PfsParameter pfs = new PfsParameterJpa();
    SearchResultList unreviewedConcepts = findConcepts(getProject().getTerminology(), getProject().getVersion(),
        getProject().getBranch(), " workflowStatus:NEEDS_REVIEW", pfs);
    if (unreviewedConcepts.size() > 1) {
      throw new Exception(
          "Creating a new release requires all concepts to have workflowStatus reviewed.");
    }
    
    //Verify that all worklists in the epoch for the project are READY_FOR_PUBLICATION
    WorklistList notReadyWorklists = findWorklists(getProject(), " NOT workflowStatus:READY_FOR_PUBLICATION", pfs);
    if (notReadyWorklists.size() > 1) {
      throw new Exception("Creating a new release requires all worklists in the epoch to be READY_FOR_PUBLICATION");
    }
    
    //Verify that all “required” bins have zero counts 
    for (WorkflowConfig config : getWorkflowConfigs(getProject())) {
      for (WorkflowBin bin : getWorkflowBins(getProject(), config.getType())) {
        if (bin.isRequired()) {
          if (bin.getStats().size() != 0) {
            throw new Exception(
                "Creating a new release requires that all required bins be completed and have a count of 0.");
          }
        } 
      }
    } 
        
    /*MID Validation passes – TODO later
      Monster QA passes – TODO later*/
    return validationResult;

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {

    logInfo("Starting Create new release");
    fireProgressEvent(0, "Starting progress: " + 0 + "%");

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

    fireProgressEvent(50, "Progress: " + 50 + "%");

    // Add a release info for the current release
    logInfo("  Add release info for the current release = " + getProject().getVersion());
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
    fireProgressEvent(100, "Progress: " + 100 + "%");
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
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
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
        AlgorithmParameter.Type.BOOLEAN);
    params.add(param);

    return params;
  }
}
