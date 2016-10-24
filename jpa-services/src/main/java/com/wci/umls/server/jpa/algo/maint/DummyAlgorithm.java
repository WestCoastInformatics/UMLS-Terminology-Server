/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.ArrayList;
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
public class DummyAlgorithm extends AbstractAlgorithm {

  /**
   * The number of times the algorithm will print to the log before finishing.
   */
  private Double num;

  /**
   * Instantiates an empty {@link DummyAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public DummyAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("DUMMY");
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
    logInfo("Starting DUMMY");

    // Print algorithm progress to the log, waiting a second between.
    int previousProgress = 0;
    for (int i = 1; i <= num; i += 1) {
      if (isCancelled()) {
        throw new CancelException("Cancelled");
      }
      Thread.sleep(1000);
      int currentProgress = (int) ((100 / num) * i);
      if(currentProgress > previousProgress){
      fireProgressEvent(currentProgress,
          "DUMMY progress: " + currentProgress + "%");
      previousProgress = currentProgress;
      }
    }

    logInfo("Finished DUMMY");

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
      num = Double.parseDouble(p.getProperty("num"));
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
    param = new AlgorithmParameterJpa("Test boolean",
        "boo", "Test boolean description", "e.g. true", 0,
        AlgorithmParameter.Type.BOOLEAN);
    params.add(param);
    param = new AlgorithmParameterJpa("Test text",
        "tex", "Test text description", "e.g. abcabc", 0,
        AlgorithmParameter.Type.TEXT);
    params.add(param);
    param = new AlgorithmParameterJpa("Test enum",
        "enu", "Test enum description", "e.g. enum", 0,
        AlgorithmParameter.Type.ENUM);
    List<String> valueList = new ArrayList<>();
    valueList.add("option1");
    valueList.add("option2");
    valueList.add("option3");
    param.setPossibleValues(valueList);
    params.add(param);
    return params;
  }

}
