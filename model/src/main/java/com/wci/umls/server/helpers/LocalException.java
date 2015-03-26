package com.wci.umls.server.helpers;

/**
 * Represents a known exception with a user-friendly error message that is
 * handled differently by error handlers.
 */
@SuppressWarnings("serial")
public class LocalException extends Exception {

  /**
   * Instantiates a {@link LocalException} from the specified parameters.
   *
   * @param message the message
   * @param t the t
   */
  public LocalException(String message, Exception t) {
    super(message, t);
  }

  /**
   * Instantiates a {@link LocalException} from the specified parameters.
   *
   * @param message the message
   */
  public LocalException(String message) {
    super(message);

  }
}
