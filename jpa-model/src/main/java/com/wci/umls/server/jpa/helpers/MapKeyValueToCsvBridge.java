/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Iterator;
import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for the key/values of a map.
 */
public class MapKeyValueToCsvBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuffer buf = new StringBuffer();

      Map<?, ?> map = (Map<?, ?>) value;
      Iterator<? extends Map.Entry<?, ?>> it = map.entrySet().iterator();
      while (it.hasNext()) {
        String key = it.next().getKey().toString();
        String v = it.next().getValue().toString();
        buf.append(key).append("=").append(v);
        if (it.hasNext())
          buf.append(" ");
      }
      return buf.toString();
      
      
    }
    return null;
  }
}
