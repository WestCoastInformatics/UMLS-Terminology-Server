/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;

import com.wci.umls.server.User;
import com.wci.umls.server.UserRole;

/**
 * Hibernate search field bridge for searching user/role combinations. For
 * example, "userRoleMap:user1ADMIN"
 */
public class UserRoleBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      final Map<User, UserRole> map = (Map<User, UserRole>) value;
      for (final Map.Entry<User, UserRole> entry : map.entrySet()) {
        buf.append(entry.getKey().getUserName())
            .append(entry.getValue().toString()).append(",");
      }
      return buf.toString();
    }
    return null;
  }
}