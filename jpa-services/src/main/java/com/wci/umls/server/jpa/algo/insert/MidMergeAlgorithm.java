/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.util.UUID;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;

/**
 * Implementation of an algorithm to import attributes.
 */
public class MidMergeAlgorithm extends GeneratedMergeAlgorithm {
  
  /**
   * Instantiates an empty {@link GeneratedMergeAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public MidMergeAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("MIDMERGE");
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
      throw new Exception("Mid Merge requires a project to be set");
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
    // Set GeneratedMergeAlgorithm's midMerge flag to True, and run it.
    super.setMidMerge(true);
    super.compute();
    
  }

}