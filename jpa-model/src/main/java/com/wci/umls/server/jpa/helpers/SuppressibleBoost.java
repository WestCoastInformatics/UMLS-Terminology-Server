/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import org.hibernate.search.engine.BoostStrategy;

import com.wci.umls.server.model.content.Component;

/**
 * Used to boost non-suppressible data.
 */
public class SuppressibleBoost implements BoostStrategy {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.hibernate.search.engine.BoostStrategy#defineBoost(java.lang.Object)
   */
  @Override
  public float defineBoost(Object value) {
    Component component = (Component) value;
    if (!component.isSuppressible()) {
      return 1.5f;
    } else {
      return 1.0f;
    }
  }
}
