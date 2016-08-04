/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.algo.Algorithm;

/**
 * Represents a source data handler.
 */
public interface SourceDataHandler extends Algorithm {

  /**
   * Sets the source data.
   *
   * @param sourceData the source data
   */
  public void setSourceData(SourceData sourceData);

  /**
   * Removes the loaded data.
   *
   * @throws Exception the exception
   */
  public void remove() throws Exception;

}
