/**
 * Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents an exception thrown by the pause run algorithm.
 */
@SuppressWarnings("serial")
public class PauseException extends Exception {

  /**
   * Instantiates a {@link PauseException} from the specified parameters.
   *
   * @param message the message
   * @param t the t
   */
  public PauseException(String message, Exception t) {
    super(message, t);
  }

  /**
   * Instantiates a {@link PauseException} from the specified parameters.
   *
   * @param message the message
   */
  public PauseException(String message) {
    super(message);

  }
}
