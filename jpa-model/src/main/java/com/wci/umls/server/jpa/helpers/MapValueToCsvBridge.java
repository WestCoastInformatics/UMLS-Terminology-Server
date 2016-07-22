/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Iterator;
import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for the values of a map.
 */
public class MapValueToCsvBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      Map<?, ?> map = (Map<?, ?>) value;
      Iterator<?> it = map.values().iterator();
      while (it.hasNext()) {
        String next = it.next().toString();
        buf.append(next);
        if (it.hasNext())
          buf.append(" ");
      }
      return buf.toString();
    }
    return null;
  }
}