/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;

/**
 * Implementation of an algorithm to clear information after an insertion is
 * finished.
 */
public class PostInsertionAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /**
   * Instantiates an empty {@link PostInsertionAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PostInsertionAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("POSTINSERTION");
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
      throw new Exception("Post Insertion requires a project to be set");
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
    logInfo("Starting " + getName());

    // No Molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Clear out the static caches that have been populated during
    // the insertion - we don't need them anymore either
    clearCaches();
    
    // Makes sure automations are turned back on
    // Editing should not be turned back on until insertion results are reviewed
    // by lead
    getProject().setAutomationsEnabled(true);

    logInfo("Finished " + getName());
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
    logInfo("Finished RESET " + getName());
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

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  @Override
  public String getDescription() {
    return "Clears information after an insertion is finished.";
  }
}