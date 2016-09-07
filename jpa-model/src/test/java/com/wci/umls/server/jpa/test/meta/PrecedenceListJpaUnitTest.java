/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.meta;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;

/**
 * Unit testing for {@link PrecedenceListJpa}.
 */
public class PrecedenceListJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private PrecedenceListJpa object;

  /** The fixture kvp1 */
  private KeyValuePairList kvp1;

  /** The fixture kvp2 */
  private KeyValuePairList kvp2;

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
    // set up text fixtures
    object = new PrecedenceListJpa();
    kvp1 = new KeyValuePairList();
    kvp1.addKeyValuePair(new KeyValuePair("1", "1"));
    kvp2 = new KeyValuePairList();
    kvp2.addKeyValuePair(new KeyValuePair("2", "2"));
    kvp2.addKeyValuePair(new KeyValuePair("3", "3"));
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
    tester.exclude("terminologies");
    tester.exclude("termTypes");
    tester.proxy(KeyValuePairList.class, 1, kvp1);
    tester.proxy(KeyValuePairList.class, 1, kvp2);

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
    tester.include("name");
    tester.include("terminology");
    tester.include("version");
    tester.include("precedence");

    tester.proxy(KeyValuePairList.class, 1, kvp1);
    tester.proxy(KeyValuePairList.class, 1, kvp2);
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
    tester.proxy(KeyValuePairList.class, 1, kvp1);
    tester.proxy(KeyValuePairList.class, 1, kvp2);
    assertTrue(tester.testCopyConstructor(PrecedenceList.class));
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
    tester.proxy(KeyValuePairList.class, 1, kvp1);
    tester.proxy(KeyValuePairList.class, 1, kvp2);

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
    tester.include("terminology");
    tester.include("version");
    tester.include("name");
    tester.include("terminologies");
    tester.include("termTypes");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test for root terminology reference in XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlTransient() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    ProxyTester tester = new ProxyTester(new PrecedenceListJpa());
    tester.proxy(KeyValuePairList.class, 1, kvp1);
    PrecedenceListJpa terminology = (PrecedenceListJpa) tester.createObject(1);

    String xml = ConfigUtility.getStringForGraph(terminology);
    Assert.assertFalse(xml.contains("<termTypeRankMap>"));

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
