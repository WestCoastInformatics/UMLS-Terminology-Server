package com.wci.umls.server.helpers;

import java.util.Properties;

/**
 * Represents something configurable.
 */
public interface Configurable {

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception 
   */
  public void setProperties(Properties p) throws Exception;
}
