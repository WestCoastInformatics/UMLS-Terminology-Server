/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

/**
 * Automates JUnit testing of equals and hashcode methods.
 */
public class XmlSerializationTester extends ProxyTester {

  /**
   * Constructs a new getter/setter tester to test objects of a particular
   * class.
   * 
   * @param obj Object to test.
   */
  public XmlSerializationTester(Object obj) {
    super(obj);
  }

  /**
   * Tests XML and JSON serialization for equality,.
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean testXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug(
        "Test xml serialization - " + clazz.getName());
    Object obj = createObject(1);
    Logger.getLogger(getClass()).info(obj);
    String xml = ConfigUtility.getStringForGraph(obj);
    Logger.getLogger(getClass()).info("xml = " + xml);
    Object obj2 = ConfigUtility.getGraphForString(xml, obj.getClass());
    String json = ConfigUtility.getJsonForGraph(obj);
    Logger.getLogger(getClass()).info("json = " + json);
    Object obj3 = ConfigUtility.getGraphForJson(json, obj.getClass());
    Logger.getLogger(getClass()).debug(obj);
    Logger.getLogger(getClass()).debug(obj2);
    Logger.getLogger(getClass()).debug(obj3);

    // If obj has an "id" field, compare the ids
    try {
      final Method method =
          obj.getClass().getMethod("getId", new Class<?>[] {});
      if (method != null && method.getReturnType() == Long.class) {

        final Long id1 = (Long) method.invoke(obj, new Object[] {});
        final Long id2 = (Long) method.invoke(obj2, new Object[] {});
        final Long id3 = (Long) method.invoke(obj3, new Object[] {});
        if (!id1.equals(id2) || !id2.equals(id3)) {
          Logger.getLogger(getClass()).debug(
              "  id fields do not match " + id1 + ", " + id2 + ", " + id3);
          return false;
        }
      }
    } catch (NoSuchMethodException e) {
      // this is OK
    }
    if (obj.equals(obj2) && obj.equals(obj3)) {
      return true;
    } else {
      Logger.getLogger(getClass()).info("obj = " + obj);
      Logger.getLogger(getClass()).info("obj2 = " + obj2);
      Logger.getLogger(getClass()).info("obj3 = " + obj3);
      return false;
    }
  }

}
