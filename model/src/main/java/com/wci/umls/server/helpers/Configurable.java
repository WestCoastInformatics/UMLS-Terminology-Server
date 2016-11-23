/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.Properties;

/**
 * Represents something configurable.
 */
public interface Configurable {

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName();

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception the exception
   */
  public void setProperties(Properties p) throws Exception;

  /**
   * Check properties.
   *
   * @param p the p
   * @throws Exception the exception
   */
  public void checkProperties(Properties p) throws Exception;
}
