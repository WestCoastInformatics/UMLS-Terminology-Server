/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for a string with underscores.
 */
public class SplitUnderscoreBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      return value.toString().replaceAll("_", " ");
    }
    return null;
  }
}