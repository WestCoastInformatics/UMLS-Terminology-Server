/*
 *    Copyright 2016 West Coast Informatics, LLC
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
   * Returns the version.
   *
   * @return the version
   */
  public String getVersion();

  /**
   * Sets the version.
   *
   * @param version the version to set
   */
  public void setVersion(String version);

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

  /**
   * Indicates whether or not obsolete is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isObsolete();

  /**
   * Sets the obsolete.
   *
   * @param obsolete the obsolete
   */
  public void setObsolete(boolean obsolete);

  /**
   * Returns the score.
   *
   * @return the score
   */
  public Float getScore();

  /**
   * Sets the score.
   *
   * @param score the score
   */
  public void setScore(Float score);
}
