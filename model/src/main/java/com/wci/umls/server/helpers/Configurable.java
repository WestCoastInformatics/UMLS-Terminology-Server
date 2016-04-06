/*
 *    Copyright 2016 West Coast Informatics, LLC
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

}
