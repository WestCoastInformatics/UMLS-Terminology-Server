/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Iterator;
import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for the keys of a map.
 */
public class MapKeyToCsvBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

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