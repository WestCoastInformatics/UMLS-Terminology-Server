/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.PauseException;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Implementation of an algorithm to pause the running of a process. Once it
 * pauses, send an email to the process users to let them know the process is
 * paused
 */
public class PauseRunAlgorithm extends AbstractAlgorithm {

  /** Flag saying whether the algorithm should pause the run (toggles after a successful pause). */
  private Boolean pauseRun = true;

  /**
   * Instantiates an empty {@link PauseRunAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PauseRunAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("PAUSERUN");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("PauseRun initializer requires a project to be set");
    }
    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    // If the pause flag is set, cancel the run and toggle the pause flag.
    if (pauseRun) {
      setPauseRun(false);
      throw new PauseException("Pause the process");
    }

    // If this is a restart, succeed and finish.
    else {
      fireProgressEvent(100, "PAUSERUN progress: " + 100 + "%");
    }

    logInfo("Finished " + getName());

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
    logInfo("Finished RESET " + getName());
  }

  /**
   * Sets the first run.
   *
   * @param pauseRun the pause run
   */
  @SuppressWarnings("static-method")
  public void setPauseRun(Boolean pauseRun) {
    this.pauseRun = pauseRun;
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("pauseRun") != null) {
      pauseRun = Boolean.parseBoolean(p.getProperty("pauseRun"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    // pause run
    AlgorithmParameter param =
        new AlgorithmParameterJpa("Pause Run", "pauseRun", "Pause the run?",
            "e.g. true", 5, AlgorithmParameter.Type.BOOLEAN, "true");
    params.add(param);

    return params;
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Algorithm for pausing a process run, so things can be checked and/or the server can be bounced before continuing the process.";
  }
}
