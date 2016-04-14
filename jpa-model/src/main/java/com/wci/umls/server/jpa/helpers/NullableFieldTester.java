/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.JoinColumn;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ProxyTester;

/**
 * Automates JUnit testing of equals and hashcode methods.
 */
public class NullableFieldTester extends ProxyTester {

  /**
   * Constructs a new getter/setter tester to test objects of a particular
   * class.
   * 
   * @param obj Object fto test.
   */
  public NullableFieldTester(Object obj) {
    super(obj);
  }

  /**
   * Creates two objects with the same field values and verifies they are equal.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testNotNullFields() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Test null field equals - " + clazz.getName());

    Set<String> notNullFields = getNotNullFields(clazz);
    if (includes != null) {
      for (String field : includes) {
        if (!notNullFields.contains(field)) {
          Logger.getLogger(getClass()).info(
              "  " + field + " is not defined as nullable");
          return false;
        }
      }
    }
    for (String field : notNullFields) {
      if (includes == null || !includes.contains(field)) {
        Logger.getLogger(getClass()).info(
            "  " + field + " should be in the include list as nullable");
        return false;
      }
    }

    return true;
  }

  /**
   * Returns the fields with nullable annotations.
   *
   * @param clazz the clazz
   * @return the fields with nullable annotations
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private Set<String> getNotNullFields(Class<?> clazz) throws Exception {

    Set<String> results = new HashSet<>();

    for (Field field : FieldUtils.getAllFields(clazz)) {
      if (field.isAnnotationPresent(Column.class)) {
        Column annotation = field.getAnnotation(Column.class);
        if (!annotation.nullable()) {
          results.add(field.getName().toLowerCase());
        }
      }

      if (field.isAnnotationPresent(JoinColumn.class)) {
        JoinColumn annotation = field.getAnnotation(JoinColumn.class);
        if (!annotation.nullable()) {
          results.add(field.getName().toLowerCase());
        }
      }

    }

    return results;
  }

}
