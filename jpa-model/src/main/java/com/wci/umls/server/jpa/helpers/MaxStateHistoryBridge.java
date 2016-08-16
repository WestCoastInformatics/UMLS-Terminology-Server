/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

/**
 * Hibernate search field bridge for the key/values of a map.
 */
public class MaxStateHistoryBridge implements StringBridge {

  /* see superclass */
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      @SuppressWarnings("unchecked")
      Map<String, Date> map = (Map<String, Date>) value;
      Iterator<? extends Map.Entry<String, Date>> it =
          map.entrySet().iterator();
      String max = "";
      long maxTime = 0L;
      while (it.hasNext()) {
        final Map.Entry<String, Date> entry = it.next();
        final String key = entry.getKey();
        final Date d = entry.getValue();
        if (d.getTime() > maxTime) {
          maxTime = d.getTime();
          max = key;
        }
      }
      return max;
    }
    return null;
  }
}
