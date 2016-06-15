/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.search.annotations.Analyze;

import com.wci.umls.server.helpers.ProxyTester;

/**
 * Automates JUnit testing of equals and hashcode methods.
 */
public class IndexedFieldTester extends ProxyTester {

  /**
   * Constructs a new getter/setter tester to test objects of a particular
   * class.
   * 
   * @param obj Object fto test.
   */
  public IndexedFieldTester(Object obj) {
    super(obj);
  }

  /**
   * Test analyzed indexed fields.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testAnalyzedIndexedFields() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Test analyzed indexed fields - " + clazz.getName());

    final Map<String, Boolean> analyzedFieldsMap = getAnalyzedFieldsMap(clazz);
    for (final String field : includes == null ? new HashSet<String>()
        : includes) {
      boolean found = false;
      if (analyzedFieldsMap.containsKey(field)) {
        found = analyzedFieldsMap.get(field);
      }
      if (!found) {
        Logger.getLogger(getClass()).info(
            "  " + field + " is not defined as analyzed");
        return false;
      }
    }
    for (final String field : analyzedFieldsMap.keySet()) {
      if (analyzedFieldsMap.get(field) && !includes.contains(field)) {
        Logger.getLogger(getClass()).info(
            "  " + field + " should be in the include list as analyzed");
        return false;
      }
    }

    return true;
  }

  /**
   * Test analyzed indexed fields.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testNotAnalyzedIndexedFields() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Test not analyzed indexed fields - " + clazz.getName());

    final Map<String, Boolean> analyzedFieldsMap = getAnalyzedFieldsMap(clazz);
    for (final String field : includes) {
      boolean found = true;
      if (analyzedFieldsMap.containsKey(field)) {
        found = analyzedFieldsMap.get(field);
      }
      if (found) {
        Logger.getLogger(getClass()).info(
            "  " + field + " is defined as analyzed");
        return false;
      }
    }
    for (final String field : analyzedFieldsMap.keySet()) {
      if (!analyzedFieldsMap.get(field) && !includes.contains(field)) {
        Logger.getLogger(getClass()).info(
            "  " + field + " should be in the include list as not analyzed");
        return false;
      }
    }

    return true;
  }

  /**
   * Returns the name analyzed pairs from annotation.
   *
   * @param clazz the clazz
   * @return the name analyzed pairs from annotation
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   */
  @SuppressWarnings("static-method")
  private Map<String, Boolean> getAnalyzedFieldsMap(Class<?> clazz)
    throws NoSuchMethodException, SecurityException {

    // initialize the name->analyzed pair map
    final Map<String, Boolean> nameAnalyzedPairs = new HashMap<>();

    for (final Method m : clazz.getMethods()) {

      // Look at "get" method sfor field annotations
      String fieldName = null;
      if (m.getName().startsWith("get")) {
        fieldName = m.getName().substring(3);
        fieldName =
            fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
      } else if (m.getName().startsWith("is")) {
        fieldName = m.getName().substring(2);
        fieldName =
            fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
      } else {
        continue;
      }

      // check for Field annotation
      if (m.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
        nameAnalyzedPairs.put(fieldName.toLowerCase(),
            m.getAnnotation(org.hibernate.search.annotations.Field.class)
                .analyze().equals(Analyze.YES));
      }

      // check for Fields annotation
      if (m.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
        // add all specified fields
        for (final org.hibernate.search.annotations.Field f : m.getAnnotation(
            org.hibernate.search.annotations.Fields.class).value()) {
          if (f.name().equals("")) {
            nameAnalyzedPairs.put(fieldName.toLowerCase(),
                f.analyze().equals(Analyze.YES));
          } else {
            nameAnalyzedPairs.put(f.name().toLowerCase(),
                f.analyze().equals(Analyze.YES));
          }
        }
      }
    }
    return nameAnalyzedPairs;
  }

}
