/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Iterator;
import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for the values of a map.
 */
public class MapValueToCsvBridge implements StringBridge {

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.hibernate.search.bridge.StringBridge#objectToString(java.lang.Object)
   */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

      Map<?,?> map = (Map<?,?>) value;
      Iterator<?> it = map.values().iterator();
      while (it.hasNext()) {
        String next = it.next().toString();
        buf.append(next);
        if (it.hasNext())
          buf.append(", ");
      }
      return buf.toString();
    }
    return null;
  }
}