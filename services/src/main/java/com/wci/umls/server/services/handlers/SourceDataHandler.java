/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Configurable;

/**
 * Generically represents a source data handler.
 */
public interface SourceDataHandler extends Algorithm, Configurable {

  /**
   * Sets the source data.
   *
   * @param sourceData the source data
   */
  public void setSourceData(SourceData sourceData);

  /**
   * Removes the loaded data
   * @throws Exception
   */
  public void remove() throws Exception;

  /**
   * Hook for checking pre-conditions before starting a load. For example,
   * verifying that the data to be loaded is not already loaded.
   *
   * @return true, if is loadable
   * @throws Exception
   */
  public boolean checkPreconditions() throws Exception;

}
