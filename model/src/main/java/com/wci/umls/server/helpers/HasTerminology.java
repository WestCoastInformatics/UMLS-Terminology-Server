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
   * Returns the terminology version.
   * 
   * @return the terminology version
   */
  public String getVersion();

  /**
   * Sets the terminology version.
   * 
   * @param version the terminology version
   */
  public void setVersion(String version);

}
