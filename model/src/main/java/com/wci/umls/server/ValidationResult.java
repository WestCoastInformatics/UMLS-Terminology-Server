/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server;

import java.util.Set;

/**
 * Generically represents a validation result, either an error or a warning.
 */
public interface ValidationResult {

  /**
   * Checks if is valid.
   * 
   * @return true, if is valid
   */
  public boolean isValid();

  /**
   * Gets the errors.
   * 
   * @return the errors
   */
  public Set<String> getErrors();

  /**
   * Sets the errors.
   * 
   * @param errors the new errors
   */
  public void setErrors(Set<String> errors);

  /**
   * Gets the warnings.
   * 
   * @return the warnings
   */
  public Set<String> getWarnings();

  /**
   * Returns the comments.
   *
   * @return the comments
   */
  public Set<String> getComments();

  /**
   * Sets the warnings.
   * 
   * @param warnings the new warnings
   */
  public void setWarnings(Set<String> warnings);

  /**
   * Sets the comments.
   *
   * @param comments the comments
   */
  public void setComments(Set<String> comments);

  /**
   * Merge a second validation result into this validation result
   * 
   * @param validationResult the validation result
   */
  public void merge(ValidationResult validationResult);

}
