/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Automates JUnit testing of equals and hashcode methods.
 */
public class CopyConstructorTester extends ProxyTester {

  /**
   * Constructs a new getter/setter tester to test objects of a particular
   * class.
   * 
   * @param obj Object to test.
   */
  public CopyConstructorTester(Object obj) {
    super(obj);
  }

  /**
   * Creates an object from the object.
   *
   * @param interfaceType the interface type
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testCopyConstructor(Class<?> interfaceType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Test copy constructor - " + clazz.getName());
    Object o1 = createObject(1);
    Object o2 = clazz.getConstructor(new Class<?>[] {
        interfaceType
    }).newInstance(new Object[] {
        o1
    });

    return checkAllFields(o1, o2, null);
  }

  /**
   * Test copy constructor deep.
   *
   * @param interfaceType the interface type
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testCopyConstructorCollection(Class<?> interfaceType)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Test copy constructor - " + clazz.getName());
    Object o1 = createObject(1);
    Object o2 = clazz.getConstructor(new Class<?>[] {
        interfaceType, boolean.class
    }).newInstance(new Object[] {
        o1, true
    });

    return checkAllFields(o1, o2, true);

  }

  /**
   * Check all fields.
   *
   * @param o1 the o 1
   * @param o2 the o 2
   * @param deepFlag the deep
   * @return true, if successful
   */
  private boolean checkAllFields(Object o1, Object o2, Boolean deepFlag) {
    boolean allFields = true;
    final Method[] methods = clazz.getMethods();
    for (int i = 0; i < methods.length; i++) {
      /* We're looking for single-argument setters. */
      final Method setter = methods[i];
      if (!setter.getName().startsWith("set")) {
        continue;
      }
      final String fieldName = setter.getName().substring(3);
      final Class<?>[] args = setter.getParameterTypes();
      if (args.length != 1)
        continue;

      /* Check the field name against our include/exclude list. */
      if (!includes.isEmpty() && !includes.contains(fieldName.toLowerCase())) {
        continue;
      }
      if (excludes.contains(fieldName.toLowerCase()))
        continue;

      /* Is there a getter that returns the same type? */
      Method getter;
      try {
        getter = clazz.getMethod("get" + fieldName, new Class[] {});
        if (getter.getReturnType() != args[0])
          continue;
      } catch (NoSuchMethodException e) {
        try {
          getter = clazz.getMethod("is" + fieldName, new Class[] {});
          if (getter.getReturnType() != args[0])
            continue;
        } catch (NoSuchMethodException e2) {
          continue;
        }
      }

      Object result1;
      Object result2;
      try {
        result1 = getter.invoke(o1, new Object[] {});
      } catch (IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e1) {
        throw new RuntimeException(
            "Getter" + getter.getDeclaringClass().getName() + "."
                + getter.getName() + " threw " + e1);
      }

      try {
        result2 = getter.invoke(o2, new Object[] {});
      } catch (IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e2) {
        throw new RuntimeException(
            "Getter" + getter.getDeclaringClass().getName() + "."
                + getter.getName() + " threw " + e2);
      }

      // Determine if "result1" and "result2" match
      boolean match = true;

      // Null check - if one is null, both must be null
      if (result1 == null || result2 == null) {
        match = result1 == null && result2 == null;
      }

      // else if primitive, use equals
      else if (getter.getReturnType().isPrimitive()) {
        match = result1.equals(result2);
      }

      // else if it is a collection, either skip if deep flag isn't set
      // or use .equals and verify not ==
      else if (Collection.class.isAssignableFrom(getter.getReturnType())
          || Map.class.isAssignableFrom(getter.getReturnType())) {
        // Only compare if deep flag is set
        if (deepFlag == null || deepFlag) {
          // .equals but not the same object -> e.g. a copy
          match = result1.equals(result2) && result1 != result2;
          // TODO: make sure elements are copies too
          if (!match) {
            Logger.getLogger(getClass()).debug("Collection equivalence issue");
          }
        }
      }

      // else it is a regular object, look for identity equivalence
      else {
        // exception for "key value pair list"
        if (getter.getReturnType().equals(KeyValuePairList.class)) {
          match = result1.equals(result2);
        } else {
          match = result1 == result2;
        }
      }

      // Determine if "result1" and "result2" match
      if (!match) {
        Logger.getLogger(getClass())
            .info("Field " + fieldName + " does not match");
        Logger.getLogger(getClass()).info("  o1 = " + result1);
        Logger.getLogger(getClass()).info("  o2 = " + result2);
        return false;
      }

    }
    return allFields;
  }
}
