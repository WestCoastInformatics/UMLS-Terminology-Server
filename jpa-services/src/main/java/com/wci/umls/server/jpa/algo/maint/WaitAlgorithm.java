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

/**
 * Implementation of an algorithm to wait for a second and print to the log.
 * This will be used for testing purposes only
 */
public class WaitAlgorithm extends AbstractAlgorithm {

  /**
   * The number of times the algorithm will print to the log before finishing.
   */
  private int num;

  /** The delay. */
  private int delay = 1000;

  /**
   * Instantiates an empty {@link WaitAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public WaitAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("WAIT");
    setLastModifiedBy("admin");

  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("Wait initializer requires a project to be set");
    }
    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting WAIT");

    // Print algorithm progress to the log, waiting a second between.
    int previousProgress = 0;
    for (int i = 1; i <= num; i += 1) {
      checkCancel();
      Thread.sleep(delay);
      int currentProgress = (int) ((100.0 / num) * i);
      if (currentProgress > previousProgress) {
        fireProgressEvent(currentProgress,
            "WAIT progress: " + currentProgress + "%");
        previousProgress = currentProgress;
      }
    }

    logInfo("Finished WAIT");

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "num", "delay"
    }, p);
    setProperties(p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p.getProperty("num") != null) {
      num = Integer.parseInt(p.getProperty("num"));
    }
    if (p.getProperty("delay") != null) {
      delay = Integer.parseInt(p.getProperty("delay"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Number of Iterations",
        "num", "Number of times the algorithm will run", "e.g. 5", 10,
        AlgorithmParameter.Type.INTEGER, "");
    params.add(param);
    // Test a "default" value
    param = new AlgorithmParameterJpa("Delay", "delay",
        "Delay time in milliseconds", "e.g. 500", 10,
        AlgorithmParameter.Type.INTEGER, "500");
    params.add(param);

    return params;
  }

}
