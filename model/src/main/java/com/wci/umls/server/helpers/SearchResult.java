/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Generic object to contain search results.
 */
public interface SearchResult {

  /**
   * Returns the id.
   *
   * @return the id
   */
  public Long getId();

  /**
   * Sets the id.
   *
   * @param id the id to set
   */
  public void setId(Long id);

  /**
   * Returns the terminology id.
   *
   * @return the terminologyId
   */
  public String getTerminologyId();

  /**
   * Sets the terminology id.
   *
   * @param terminologyId the terminologyId to set
   */
  public void setTerminologyId(String terminologyId);

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the terminology to set
   */
  public void setTerminology(String terminology);

  /**
   * Returns the terminology version.
   *
   * @return the terminologyVersion
   */
  public String getTerminologyVersion();

  /**
   * Sets the terminology version.
   *
   * @param terminologyVersion the terminologyVersion to set
   */
  public void setTerminologyVersion(String terminologyVersion);

  /**
   * Returns the value.
   *
   * @return the value
   */
  public String getValue();

  /**
   * Sets the value.
   *
   * @param value the value to set
   */
  public void setValue(String value);

}
