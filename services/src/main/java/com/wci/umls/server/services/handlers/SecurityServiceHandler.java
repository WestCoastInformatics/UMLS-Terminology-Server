/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.User;
import com.wci.umls.server.helpers.Configurable;

/**
 * Generically represents a handler that can authenticate a user.
 */
public interface SecurityServiceHandler extends Configurable {

  /**
   * Authenticate.
   *
   * @param user the user
   * @param password the password
   * @return the user
   * @throws Exception the exception
   */
  public User authenticate(String user, String password) throws Exception;

  /**
   * Indicates whether or not the user should be timed out.
   *
   * @param user the user
   * @return true, if successful
   */
  public boolean timeoutUser(String user);

  /**
   * Computes token for user. For example, a UUID or an MD5 or a counter. Each
   * login requires yields a potentially different token, even for the same
   * user.
   *
   * @param user the user
   * @return the string
   */
  public String computeTokenForUser(String user);
}
