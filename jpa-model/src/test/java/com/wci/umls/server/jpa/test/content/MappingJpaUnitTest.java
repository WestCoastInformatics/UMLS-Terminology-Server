/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.content;

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
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.meta.AdditionalRelationshipTypeJpa;
import com.wci.umls.server.jpa.meta.RelationshipTypeJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;

/**
 * Unit testing for {@link MappingJpa}.
 */
public class MappingJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private Mapping object;

  /** The a1. */
  private Attribute a1;

  /** The a2. */
  private Attribute a2;

  /** The ms1. */
  private MapSet ms1;

  /** The ms2. */
  private MapSet ms2;

  /** The rt1. */
  private RelationshipType rt1;

  /** The rt2. */
  private RelationshipType rt2;

  /** The art1. */
  private AdditionalRelationshipType art1;

  /** The art2. */
  private AdditionalRelationshipType art2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {
    object = new MappingJpa();
    ProxyTester tester = new ProxyTester(new AttributeJpa());
    a1 = (AttributeJpa) tester.createObject(1);
    a2 = (AttributeJpa) tester.createObject(2);

    ms1 = new MapSetJpa();
    ms1.setId(1L);
    ms2 = new MapSetJpa();
    ms2.setId(2L);

    ProxyTester tester2 = new ProxyTester(new RelationshipTypeJpa());
    rt1 = (RelationshipTypeJpa) tester2.createObject(1);
    rt2 = (RelationshipTypeJpa) tester2.createObject(2);

    ProxyTester tester3 = new ProxyTester(new AdditionalRelationshipTypeJpa());
    art1 = (AdditionalRelationshipTypeJpa) tester3.createObject(1);
    art2 = (AdditionalRelationshipTypeJpa) tester3.createObject(2);

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("mapSetId");
    tester.exclude("mapSetTerminologyId");
    tester.exclude("mapSet");
    tester.exclude("type");
    tester.exclude("name");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
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
    tester.include("advice");
    tester.include("relationshipType");
    tester.include("additionalRelationshipType");

    tester.proxy(Attribute.class, 1, a1);
    tester.proxy(Attribute.class, 2, a2);
    tester.proxy(RelationshipType.class, 1, rt1);
    tester.proxy(RelationshipType.class, 2, rt2);
    tester.proxy(AdditionalRelationshipType.class, 1, art1);
    tester.proxy(AdditionalRelationshipType.class, 2, art2);
    tester.proxy(MapSet.class, 1, ms1);
    tester.proxy(MapSet.class, 2, ms2);
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
  public void testModelCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Attribute.class, 1, a1);
    tester.proxy(Attribute.class, 2, a2);
    tester.proxy(RelationshipType.class, 1, rt1);
    tester.proxy(RelationshipType.class, 2, rt2);
    tester.proxy(AdditionalRelationshipType.class, 1, art1);
    tester.proxy(AdditionalRelationshipType.class, 2, art2);
    tester.proxy(MapSet.class, 1, ms1);
    tester.proxy(MapSet.class, 2, ms2);
    assertTrue(tester.testCopyConstructorDeep(Mapping.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);
    tester.proxy(Attribute.class, 1, a1);
    tester.proxy(Attribute.class, 2, a2);
    tester.proxy(RelationshipType.class, 1, rt1);
    tester.proxy(RelationshipType.class, 2, rt2);
    tester.proxy(AdditionalRelationshipType.class, 1, art1);
    tester.proxy(AdditionalRelationshipType.class, 2, art2);
    tester.proxy(MapSet.class, 1, ms1);
    tester.proxy(MapSet.class, 2, ms2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
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

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    // no analyzed fields
    // assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    tester = new IndexedFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("suppressible");
    tester.include("obsolete");
    tester.include("published");
    tester.include("publishable");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("branch");
    tester.include("fromTerminology");
    tester.include("toTerminology");
    tester.include("fromTerminologyId");
    tester.include("toTerminologyId");
    tester.include("mapSetId");
    tester.include("fromIdType");
    tester.include("toIdType");
    tester.include("fromNameSort");
    tester.include("toNameSort");
    tester.include("fromversion");
    tester.include("toversion");
    tester.include("mapsetterminologyid");

    assertTrue(tester.testNotAnalyzedIndexedFields());
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