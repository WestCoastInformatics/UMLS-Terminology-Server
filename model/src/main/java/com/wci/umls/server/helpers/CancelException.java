/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents an exception thrown by canceling an operation.
 */
@SuppressWarnings("serial")
public class CancelException extends Exception {

  /**
   * Instantiates a {@link CancelException} from the specified parameters.
   *
   * @param message the message
   * @param t the t
   */
  public CancelException(String message, Exception t) {
    super(message, t);
  }

  /**
   * Instantiates a {@link CancelException} from the specified parameters.
   *
   * @param message the message
   */
  public CancelException(String message) {
    super(message);

  }
}
