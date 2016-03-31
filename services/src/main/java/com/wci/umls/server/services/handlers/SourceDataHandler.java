/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.Configurable;

/**
 * Generically represents a sour.
 */
public interface SourceDataHandler extends Algorithm, Configurable {

  /**
   * Sets the source data.
   *
   * @param sourceData the source data
   */
  public void setSourceData(SourceData sourceData);

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Sets the version.
   *
   * @param version the version
   */
  public void setVersion(String version);
  
  /**
   * Execute load.
   */
  public void executeLoad();
  
  /**
   * Execute remove.
   */
  public void executeRemove();
}
