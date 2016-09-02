/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.CancelException;
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
  private Long num;

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
    for (int i = 1; i <= num; i += 1) {
      if (isCancelled()) {
        throw new CancelException("Cancelled");
      }
      Thread.sleep(1000);
      fireProgressEvent((int) (100 / num) * i,
          "WAIT progress: " + (100 / num) * i + "%");
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
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "num"
    }, p);

    if (p.getProperty("num") != null) {
      num = Long.parseLong(p.getProperty("num"));
    }
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Number of Iterations",
        "num", "Number of times the algorithm will run", "e.g. 5", 10,
        AlgorithmParameter.Type.INTEGER);
    params.add(param);
    return params;
  }

}
