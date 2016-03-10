/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents a thing that is associated with a terminology.
 */
public interface HasTerminology {

  /**
   * Returns the terminology.
   * 
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   * 
   * @param terminology the terminology
   */
  public void setTerminology(String terminology);

  /**
   * Returns the version.
   * 
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   * 
   * @param version the version
   */
  public void setVersion(String version);

}
