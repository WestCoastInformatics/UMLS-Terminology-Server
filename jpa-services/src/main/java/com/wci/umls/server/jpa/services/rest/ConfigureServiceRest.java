package com.wci.umls.server.jpa.services.rest;

import java.util.HashMap;

/**
 * Represents a service for configuring an environment.
 */
public interface ConfigureServiceRest {

  /**
   * Checks if is configured.
   *
   * @return true, if is configured
   * @throws Exception the exception
   */
  public boolean isConfigured() throws Exception;

  /**
   * Configure.
   *
   * @param parameters the parameters
   * @throws Exception the exception
   */

  public void configure(HashMap<String, String> parameters) throws Exception;

  /**
   * Destroy and rebuild the database. Can only be invoked after a failed
   * process.
   *
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void destroy(String authToken) throws Exception;

}
