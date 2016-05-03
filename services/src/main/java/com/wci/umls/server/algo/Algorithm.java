/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.algo;

import com.wci.umls.server.services.helpers.ProgressReporter;

/**
 * Generically represents an algortihm. Implementations must fully configure
 * themselves before the compute call is made.
 */
public interface Algorithm extends ProgressReporter {

  /**
   * Rests to initial conditions.
   *
   * @throws Exception the exception
   */
  public void reset() throws Exception;

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  public void compute() throws Exception;

  /**
   * Cancel.
   *
   * @throws Exception the exception
   */
  public void cancel() throws Exception;

  /**
   * Close.
   *
   * @throws Exception the exception
   */
  public void close() throws Exception;
  
  
}
