/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

import com.wci.umls.server.helpers.HasId;

/**
 * Hibernate search field bridge for a map of {@link HasId} -> anything.
 */
public class MapIdBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      Map<HasId, ?> map = (Map<HasId, ?>) value;
      for (HasId item : map.keySet()) {
        buf.append(item.getId()).append(" ");
      }
      return buf.toString();
    }
    return null;
  }
}