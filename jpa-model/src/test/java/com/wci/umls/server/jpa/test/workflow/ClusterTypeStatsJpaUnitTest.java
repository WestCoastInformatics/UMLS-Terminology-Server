/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.workflow;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ModelUnitSupport;
import com.wci.umls.server.jpa.worfklow.ClusterTypeStatsJpa;
import com.wci.umls.server.model.workflow.ClusterTypeStats;

/**
 * Unit testing for {@link ClusterTypeStatsJpa}.
 */
public class ClusterTypeStatsJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private ClusterTypeStatsJpa object;

  /** The fixture m1. */
  private Map<String, Integer> m1;

  /** The fixture m2. */
  private Map<String, Integer> m2;

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
    object = new ClusterTypeStatsJpa();

    m1 = new HashMap<>();
    m1.put("1", 1);
    m2 = new HashMap<>();
    m2.put("2", 2);
    m2.put("3", 3);

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
    tester.include("clusterType");
    tester.include("stats");
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    assertTrue(tester.testCopyConstructor(ClusterTypeStats.class));
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
    tester.proxy(Map.class, 1, m1);
    tester.proxy(Map.class, 2, m2);
    assertTrue(tester.testXmlSerialization());
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