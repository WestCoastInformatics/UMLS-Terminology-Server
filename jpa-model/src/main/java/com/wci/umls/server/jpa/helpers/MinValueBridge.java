/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for a list.
 */
public class MinValueBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      final List<?> values = new ArrayList<>((Collection<?>) value);
      Collections.sort(values,
          (a1, a2) -> a1.toString().compareTo(a2.toString()));
      if (values.size() > 0) {
        return values.get(0).toString();
      }
    }
    return null;
  }
}