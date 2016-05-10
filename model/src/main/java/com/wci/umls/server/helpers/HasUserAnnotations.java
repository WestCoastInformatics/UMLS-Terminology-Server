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
  public List<UserAnnotation> getUserAnnotations();

  /**
   * Sets the userAnnotations.
   *
   * @param userAnnotations the userAnnotations
   */
  public void setUserAnnotations(List<UserAnnotation> userAnnotations);

  /**
   * Adds the userAnnotation.
   *
   * @param userAnnotation the userAnnotation
   */
  public void addUserAnnotation(UserAnnotation userAnnotation);

  /**
   * Removes the userAnnotation.
   *
   * @param userAnnotation the userAnnotation
   */
  public void removeUserAnnotation(UserAnnotation userAnnotation);

}
