/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import java.util.Properties;

import com.wci.umls.server.helpers.Configurable;

/**
 * Abstract {@link Configurable} implementation to avoid needing to fuss with
 * properties if an implementation does not want to.
 */
public abstract class AbstractConfigurable implements Configurable {

  /* see superclass */
  @Override
  public void checkProperties(Properties properties) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties properties) throws Exception {
    // n/a
  }

}
