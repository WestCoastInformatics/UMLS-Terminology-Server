/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

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
    String xml = ConfigUtility.getStringForGraph(obj);
    Logger.getLogger(getClass()).debug(xml);
    Object obj2 =
        ConfigUtility
            .getGraphForString(xml, obj.getClass());
    String json = ConfigUtility.getJsonForGraph(obj);
    Logger.getLogger(getClass()).debug(json);
    Object obj3 =
        ConfigUtility
            .getGraphForJson(json, obj.getClass());
    Logger.getLogger(getClass()).debug(obj);    
    Logger.getLogger(getClass()).debug(obj2);    
    Logger.getLogger(getClass()).debug(obj3);    
    
    return obj.equals(obj2) && obj.equals(obj3);
  }

}
