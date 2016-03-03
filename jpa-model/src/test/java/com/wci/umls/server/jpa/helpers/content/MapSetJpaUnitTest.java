/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;

/**
 * Unit testing for {@link MapSetJpa}.
 */
public class MapSetJpaUnitTest {

  /** The model object to test. */
  private MapSetJpa object;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    object = new MapSetJpa();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet041");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode041");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.include("name");
    tester.include("fromComplexity");
    tester.include("toComplexity");
    tester.include("fromExhaustive");
    tester.include("toExhaustive");
    tester.include("type");
    tester.include("fromTerminology");
    tester.include("toTerminology");
    tester.include("fromVersion");
    tester.include("toVersion");
    

    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy041");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructorDeep(MapSet.class));
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy041");

    MapSet mapSet = new MapSetJpa();
    ProxyTester tester = new ProxyTester(mapSet);
    mapSet = (MapSet) tester.createObject(1);

    ProxyTester tester2 = new ProxyTester(new AttributeJpa());
    Attribute att = (Attribute) tester2.createObject(1);

    ProxyTester tester4 = new ProxyTester(new MappingJpa());
    Mapping mapping = (Mapping) tester4.createObject(1);

    mapSet.addMapping(mapping);
    mapSet.addAttribute(att);

    MapSet mapSet2 = new MapSetJpa(mapSet, false);
    assertEquals(0, mapSet2.getAttributes().size());
    assertEquals(0, mapSet2.getMappings().size());

    MapSet mapSet3 = new MapSetJpa(mapSet, true);
    assertEquals(1, mapSet3.getAttributes().size());
    assertEquals(att, mapSet3.getAttributes().iterator().next());
    assertTrue(att != mapSet3.getAttributes().iterator().next());
    assertEquals(1, mapSet3.getMappings().size());
    assertEquals(mapping, mapSet3.getMappings().iterator().next());
    assertTrue(mapping != mapSet3.getMappings().iterator().next());

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization041");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField041() throws Exception {
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    tester.include("name");
    tester.include("fromComplexity");
    tester.include("toComplexity");
    tester.include("fromExhaustive");
    tester.include("toExhaustive");
    tester.include("type");
    tester.include("fromTerminology");
    tester.include("toTerminology");
    tester.include("fromVersion");
    tester.include("toVersion");
    
    assertTrue(tester.testNotNullFields());
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
//