package com.wci.umls.server.jpa.services.rest;

import java.util.HashMap;

/**
 * Interface for Configuration Service REST APIs
 *
 */
public interface ConfigureServiceRest {

  /**
   * Checks if is configured.
   *
   * @return true, if is configured
   * @throws Exception 
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
   * Destroy and rebuild the database. Can only be invoked after a failed process.
   * @throws Exception 
   */
  public void destroy() throws Exception;
  
}
