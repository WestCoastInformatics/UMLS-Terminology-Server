/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.util.Map;

import org.hibernate.search.bridge.StringBridge;
import com.wci.umls.server.Project;
import com.wci.umls.server.UserRole;

/**
 * Hibernate search field bridge for searching project/role combinations. For
 * example, "projectRoleMap:10ADMIN"
 */
public class ProjectRoleBridge implements StringBridge {

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public String objectToString(Object value) {
    if (value != null) {
      StringBuilder buf = new StringBuilder();

      Map<Project, UserRole> map = (Map<Project, UserRole>) value;
      for (Map.Entry<Project, UserRole> entry : map.entrySet()) {
        buf.append(entry.getKey().getId()).append(entry.getValue().toString())
            .append(",");
      }
      return buf.toString();
    }
    return null;
  }
}