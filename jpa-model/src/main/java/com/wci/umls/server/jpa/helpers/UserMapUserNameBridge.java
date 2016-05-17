/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

import com.wci.umls.server.User;

/**
 * Hibernate search field bridge for a map of {@link User} -&gt; anything.
 */
public class UserMapUserNameBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      Map<User, ?> map = (Map<User, ?>) value;
      for (User item : map.keySet()) {
        buf.append(item.getUserName()).append(" ");
      }
      return buf.toString();
    }
    return null;
  }
}