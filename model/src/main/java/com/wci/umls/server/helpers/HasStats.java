/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.Map;

/**
 * Represents a thing that has a name.
 */
public interface HasStats {

  /**
   * Returns statistics with integer values.
   *
   * @return statistics with integer values
   */
  public Map<String, Integer> getStats();

  /**
   * Sets statistics with integer values.
   *
   * @param stats statistics with integer values
   */
  public void setStats(Map<String, Integer> stats);

}
