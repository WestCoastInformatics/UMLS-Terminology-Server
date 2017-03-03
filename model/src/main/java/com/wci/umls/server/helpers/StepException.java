/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents an exception thrown when "step" mode is used for process
 * execution.
 */
@SuppressWarnings("serial")
public class StepException extends Exception {

  /**
   * Instantiates a {@link StepException} from the specified parameters.
   *
   * @param message the message
   * @param t the t
   */
  public StepException(String message, Exception t) {
    super(message, t);
  }

  /**
   * Instantiates a {@link StepException} from the specified parameters.
   *
   * @param message the message
   */
  public StepException(String message) {
    super(message);

  }
}
