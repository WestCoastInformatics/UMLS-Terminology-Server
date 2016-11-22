/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;

/**
 * Implementation of an algorithm to reload config properties. This is
 * implemented at this level because it can refresh caches of all of the various
 * service types.
 */
public class ReloadConfigPropertiesAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link ReloadConfigPropertiesAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public ReloadConfigPropertiesAlgorithm() throws Exception {
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    // n/a
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    // Clear existing properties
    ConfigUtility.clearConfigProperties();

    // Handle security service (different type hierarchy)
    new SecurityServiceJpa().refreshCaches();

    // Refresh caches (will handle WorkflowServiceJpa and all superclasses)
    // If others have caches, explicitly create and refresh them here.
    refreshCaches();

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
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    return super.getParameters();
  }

}
