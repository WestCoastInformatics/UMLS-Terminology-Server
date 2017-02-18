/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.test.report;

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
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.report.ReportJpa;
import com.wci.umls.server.jpa.report.ReportResultItemJpa;
import com.wci.umls.server.jpa.report.ReportResultJpa;
import com.wci.umls.server.jpa.workflow.ChecklistJpa;
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportResult;
import com.wci.umls.server.model.report.ReportResultItem;

/**
 * Unit testing for {@link ChecklistJpa}.
 */
public class ReportResultItemJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private ReportResultItemJpa object;

  /** The fixture r1. */
  private Report r1;

  /** The fixture r2. */
  private Report r2;

  /** The fixture i1. */
  private ReportResult i1;

  /** The fixture i2. */
  private ReportResult i2;

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
  /**
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new ReportResultItemJpa();

    final ProxyTester tester2 = new ProxyTester(new ReportJpa());
    r1 = (ReportJpa) tester2.createObject(1);
    r2 = (ReportJpa) tester2.createObject(2);
    final ProxyTester tester3 = new ProxyTester(new ReportResultJpa());
    i1 = (ReportResultJpa) tester3.createObject(1);
    i2 = (ReportResultJpa) tester3.createObject(2);

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
    tester.include("itemId");
    tester.include("itemName");

    tester.proxy(Report.class, 1, r1);
    tester.proxy(Report.class, 2, r2);
    tester.proxy(ReportResult.class, 1, i1);
    tester.proxy(ReportResult.class, 2, i2);

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
  public void testModelCollectionCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(Report.class, 1, r1);
    tester.proxy(Report.class, 2, r2);
    tester.proxy(ReportResult.class, 1, i1);
    tester.proxy(ReportResult.class, 2, i2);
    assertTrue(tester.testCopyConstructor(ReportResultItem.class));

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

    tester.proxy(Report.class, 1, r1);
    tester.proxy(Report.class, 2, r2);
    tester.proxy(ReportResult.class, 1, i1);
    tester.proxy(ReportResult.class, 2, i2);

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
    tester.include("itemId");
    tester.include("itemName");

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