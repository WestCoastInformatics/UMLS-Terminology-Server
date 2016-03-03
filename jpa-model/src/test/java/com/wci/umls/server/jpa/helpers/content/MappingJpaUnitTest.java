/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.MapSet;

/**
 * Unit testing for {@link MapSetMemberJpa}.
 */
public class MappingJpaUnitTest {

  /** The model object to test. */
  private MapSetJpa object;

  /** Test fixture */
  private Mapping mapping1;

  /** Test fixture */
  private Mapping mapping2;

  /** Test fixture */
  private MapSet mapSet1;

  /** Test fixture */
  private MapSet mapSet2;

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

    mapping1 = new MappingJpa();
    mapping1.setId(1L);
    mapping1.setTerminologyId("1");
    mapping1.setTerminology("1");
    mapping1.setVersion("1");
    mapping2 = new MappingJpa();
    mapping2.setId(2L);
    mapping2.setTerminologyId("2");
    mapping2.setTerminology("2");
    mapping2.setVersion("2");

    mapSet1 = new MapSetJpa();
    mapSet1.setId(1L);
    mapSet1.setTerminologyId("1");
    mapSet1.setTerminology("1");
    mapSet1.setVersion("1");
    mapSet1.setName("2");
    mapSet2 = new MapSetJpa();
    mapSet2.setId(2L);
    mapSet2.setTerminologyId("2");
    mapSet2.setTerminology("2");
    mapSet2.setVersion("2");
    mapSet2.setName("2");
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet043");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("memberId");
    tester.exclude("memberTerminologyId");
    tester.exclude("memberTerminology");
    tester.exclude("memberVersion");
    tester.exclude("memberName");
    tester.exclude("mapSetId");
    tester.exclude("mapSetTerminologyId");
    tester.exclude("mapSetTerminology");
    tester.exclude("mapSetVersion");
    tester.exclude("mapSetName");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode043");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("publishable");
    tester.include("published");
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");
    
    tester.include("mapSet");
    tester.include("fromTerminologyId");
    tester.include("toTerminologyId");
    tester.include("fromIdType");
    tester.include("toIdType");
    tester.include("rule");
    tester.include("group");
    tester.include("rank");

    tester.proxy(Mapping.class, 1, new MappingJpa(mapping1, false));
    tester.proxy(Mapping.class, 2, new MappingJpa(mapping2, false));
    tester.proxy(MapSet.class, 1, new MapSetJpa(mapSet1, false));
    tester.proxy(MapSet.class, 2, new MapSetJpa(mapSet2, false));
    assertTrue(tester.testIdentityFieldEquals());
    tester.proxy(Mapping.class, 1, new MappingJpa(mapping1, false));
    tester.proxy(Mapping.class, 2, new MappingJpa(mapping2, false));
    tester.proxy(MapSet.class, 1, new MapSetJpa(mapSet1, false));
    tester.proxy(MapSet.class, 2, new MapSetJpa(mapSet2, false));
    assertTrue(tester.testNonIdentityFieldEquals());
    tester.proxy(Mapping.class, 1, new MappingJpa(mapping1, false));
    tester.proxy(Mapping.class, 2, new MappingJpa(mapping2, false));
    tester.proxy(MapSet.class, 1, new MapSetJpa(mapSet1, false));
    tester.proxy(MapSet.class, 2, new MapSetJpa(mapSet2, false));
    assertTrue(tester.testIdentityFieldNotEquals());
    tester.proxy(Mapping.class, 1, new MappingJpa(mapping1, false));
    tester.proxy(Mapping.class, 2, new MappingJpa(mapping2, false));
    tester.proxy(MapSet.class, 1, new MapSetJpa(mapSet1, false));
    tester.proxy(MapSet.class, 2, new MapSetJpa(mapSet2, false));
    assertTrue(tester.testIdentityFieldHashcode());
    tester.proxy(Mapping.class, 1, new MappingJpa(mapping1, false));
    tester.proxy(Mapping.class, 2, new MappingJpa(mapping2, false));
    tester.proxy(MapSet.class, 1, new MapSetJpa(mapSet1, false));
    tester.proxy(MapSet.class, 2, new MapSetJpa(mapSet2, false));
    assertTrue(tester.testNonIdentityFieldHashcode());
    tester.proxy(Mapping.class, 1, new MappingJpa(mapping1, false));
    tester.proxy(Mapping.class, 2, new MappingJpa(mapping2, false));
    tester.proxy(MapSet.class, 1, new MapSetJpa(mapSet1, false));
    tester.proxy(MapSet.class, 2, new MapSetJpa(mapSet2, false));
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy043");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Mapping.class, 1, mapping1);
    tester.proxy(Mapping.class, 2, mapping2);
    tester.proxy(MapSet.class, 1, mapSet1);
    tester.proxy(MapSet.class, 2, mapSet2);
    assertTrue(tester.testCopyConstructorDeep(MapSet.class));
  }

  /**
   * Test xml transient fields
   *
   * @throws Exception the exception
   */
  @Test
  public void testXmlTransient043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient043");

    object = new MapSetJpa(mapSet1, false);
    object.addMapping(mapping1);
    //object.setMapSet(mapSet1);
    String xml = ConfigUtility.getStringForGraph(object);
    assertTrue(xml.contains("<mapSetId>"));
    assertTrue(xml.contains("<mapSetTerminologyId>"));
    assertTrue(xml.contains("<mapSetTerminology>"));
    assertTrue(xml.contains("<mapSetVersion>"));
    assertTrue(xml.contains("<mapSetName>"));
    assertFalse(xml.contains("<mapSet>"));
    assertTrue(xml.contains("<memberId>"));
    assertTrue(xml.contains("<memberTerminologyId>"));
    assertTrue(xml.contains("<memberTerminology>"));
    assertTrue(xml.contains("<memberVersion>"));
    assertTrue(xml.contains("<memberName>"));
    assertFalse(xml.contains("<member>"));

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization043() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization043");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Mapping.class, 1, mapping1);
    tester.proxy(Mapping.class, 2, mapping2);
    tester.proxy(MapSet.class, 1, mapSet1);
    tester.proxy(MapSet.class, 2, mapSet2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField043() throws Exception {
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
    
    tester.include("mapSet");
    tester.include("fromTerminologyId");
    tester.include("toTerminologyId");
    tester.include("fromIdType");
    tester.include("toIdType");
    tester.include("rule");
    tester.include("mapGroup");
    tester.include("rank"); 
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