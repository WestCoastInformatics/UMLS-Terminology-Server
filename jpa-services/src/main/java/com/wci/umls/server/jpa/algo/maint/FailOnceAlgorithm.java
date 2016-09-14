/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Implementation of an algorithm to fail the first time it's run, then succeed
 * when restarted. 
 * This will be used for testing purposes only
 */
public class FailOnceAlgorithm extends AbstractAlgorithm {

  /** Flag saying whether this is the first time the algorithm has run or not. */
  private static Boolean firstRun = true;

  /**
   * Instantiates an empty {@link FailOnceAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public FailOnceAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("FAILONCE");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("FailOnce initializer requires a project to be set");
    }
    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting FAILONCE");

    // If this is the first time running, throw a failure message
    if(firstRun){
      setFirstRun(false);
      throw new Exception("FAILONCE first run failed.");
    }
    
    // If this is a restart, succeed and finish.
    else{
      fireProgressEvent((int) 100,
          "FAILONCE progress: " + 100 + "%");
    }

    logInfo("Finished FAILONCE");

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /**
   * Sets the first run.
   *
   * @param firstRun the first run
   */
  public void setFirstRun(Boolean firstRun) {
    FailOnceAlgorithm.firstRun = firstRun;
  }
  
  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    return params;
  }

}
