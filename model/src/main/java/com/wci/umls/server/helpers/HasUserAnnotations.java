/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.List;

/**
 * Represents a thing that has userAnnotations.
 */
public interface HasUserAnnotations {

  /**
   * Returns the userAnnotations.
   *
   * @return the userAnnotations
   */
  public List<Note> getUserAnnotations();

  /**
   * Sets the userAnnotations.
   *
   * @param userAnnotations the userAnnotations
   */
  public void setUserAnnotations(List<Note> userAnnotations);

  /**
   * Adds the userAnnotation.
   *
   * @param userAnnotation the userAnnotation
   */
  public void addUserAnnotation(Note userAnnotation);

  /**
   * Removes the userAnnotation.
   *
   * @param userAnnotation the userAnnotation
   */
  public void removeUserAnnotation(Note userAnnotation);

}
