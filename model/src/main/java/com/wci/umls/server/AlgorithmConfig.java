/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

/**
 * Represents a process configuration.
 */
public interface AlgorithmConfig extends AlgorithmInfo<ProcessConfig> {

  /**
   * Indicates whether or not enabled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isEnabled();

  /**
   * Sets the enabled.
   *
   * @param enabled the enabled
   */
  public void setEnabled(boolean enabled);
}