/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Date;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents a process configuration.
 */
public interface AlgorithmConfig
    extends HasLastModified, HasTerminology, Configurable {

  // Need to figure how to scope and define configuration capabilities.
  // One option, each algorithm could also implement an algorithm config and it
  // could return
  // an object that contained types of representations with default values, etc.
  // e.g. algorithm.getAlgorithmConfig(...) - each algo is a service so it can
  // look stuff up.
  // The config could itself have fields that get sent back in javascript
  // containign UI details
  // like picklist contents or default values (placeholder text).s

  /**
   * Returns the process.
   *
   * @return the process
   */
  public Process getProcess();

  /**
   * Sets the process.
   *
   * @param p the process
   */
  public void setProcess(Process p);

  /**
   * Returns the start date.
   *
   * @return the start date
   */
  public Date getStartDate();

  /**
   * Sets the start date.
   *
   * @param startDate the start date
   */
  public void setStartDate(Date startDate);

  /**
   * Returns the finish date.
   *
   * @return the finish date
   */
  public Date getFinishDate();

  /**
   * Sets the finish date.
   *
   * @param startDate the finish date
   */
  public void setFinishDate(Date startDate);

  /**
   * Returns the fail date.
   *
   * @return the fail date
   */
  public Date getFailDate();

  /**
   * Sets the fail date.
   *
   * @param startDate the fail date
   */
  public void setFailDate(Date startDate);
}