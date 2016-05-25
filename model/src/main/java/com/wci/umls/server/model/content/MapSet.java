/**
 * Copyright 2016 West Coast Informatics, LLC
 */
/*************************************************************
 * MapSet: MapSet.java
 * Last Updated: Feb 27, 2009
 *************************************************************/
package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.model.meta.Terminology;

/**
 * Represents a group of {@link Mapping}s between one {@link Terminology} and
 * another.
 */
public interface MapSet extends ComponentHasAttributes {

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName();

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name);

  /**
   * Returns the from complexity.
   * 
   * @return the from complexity
   */
  public String getFromComplexity();

  /**
   * Sets the from complexity.
   * 
   * @param fromComplexity the from complexity
   */
  public void setFromComplexity(String fromComplexity);

  /**
   * Returns the to complexity.
   * 
   * @return the to complexity
   */
  public String getToComplexity();

  /**
   * Sets the to complexity.
   * 
   * @param toComplexity the to complexity
   */
  public void setToComplexity(String toComplexity);

  /**
   * Gets the complexity.
   *
   * @return the complexity
   */
  public String getComplexity();

  /**
   * Sets the complexity.
   *
   * @param complexity the new complexity
   */
  public void setComplexity(String complexity);

  /**
   * Returns the to exhaustive.
   * 
   * @return the to exhaustive
   */
  public String getToExhaustive();

  /**
   * Returns the from exhaustive.
   * 
   * @return the from exhaustive
   */
  public String getFromExhaustive();

  /**
   * Sets the to exhaustive.
   * 
   * @param toExhaustive the to exhaustive
   */
  public void setToExhaustive(String toExhaustive);

  /**
   * Sets the from exhaustive.
   * 
   * @param fromExhaustive the from exhaustive
   */
  public void setFromExhaustive(String fromExhaustive);

  /**
   * Sets the type.
   * 
   * @param type the type
   */
  public void setType(String type);

  /**
   * Returns the type.
   * 
   * @return the type
   */
  public String getType();

  /**
   * Returns the mappings.
   * 
   * @return the mappings
   */
  public List<Mapping> getMappings();

  /**
   * Sets the mappings.
   * 
   * @param mappings the mappings
   */
  public void setMappings(List<Mapping> mappings);


  /**
   * Gets the from terminology.
   *
   * @return the from terminology
   */
  public String getFromTerminology();

  /**
   * Sets the from terminology.
   *
   * @param fromTerminology the new from terminology
   */
  public void setFromTerminology(String fromTerminology);

  /**
   * Gets the to terminology.
   *
   * @return the to terminology
   */
  public String getToTerminology();

  /**
   * Sets the to terminology.
   *
   * @param toTerminology the new to terminology
   */
  public void setToTerminology(String toTerminology);

  /**
   * Gets the from version.
   *
   * @return the from version
   */
  public String getFromVersion();

  /**
   * Sets the from version.
   *
   * @param fromVersion the new from version
   */
  public void setFromVersion(String fromVersion);

  /**
   * Gets the to version.
   *
   * @return the to version
   */
  public String getToVersion();

  /**
   * Sets the to version.
   *
   * @param toVersion the new to version
   */
  public void setToVersion(String toVersion);

  /**
   * Clear mappings.
   */
  public void clearMappings();

}
