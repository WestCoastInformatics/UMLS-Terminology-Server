/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * The Interface UserAnnotation.
 */
public interface UserAnnotation extends HasId, HasLastModified {

  /**
   * Sets the annotation.
   *
   * @param annotation the new annotation
   */
  public void setAnnotation(String annotation);
  
  /**
   * Gets the annotation.
   *
   * @return the annotation
   */
  public String getAnnotation();
  
}
