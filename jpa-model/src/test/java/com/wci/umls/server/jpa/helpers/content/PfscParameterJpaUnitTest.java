/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchCriteriaJpa;

/**
 * Unit testing for {@link PfscParameterJpa}.
 */
public class PfscParameterJpaUnitTest {

  /** The model object to test. */
  private PfscParameterJpa object;

  /** test fixture */
  private SearchCriteria sc1;

  /** test fixture */
  private List<?> list1;

  /** test fixture */
  private List<?> list2;

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    object = new PfscParameterJpa();
    ProxyTester tester = new ProxyTester(new SearchCriteriaJpa());
    sc1 = (SearchCriteria) tester.createObject(1);
    list1 = new ArrayList<>();
    list1.add(null);
    list2 = new ArrayList<>();
    list2.add(null);
    list2.add(null);
    object.addSearchCriteria(sc1);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet024() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet024");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode024() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode024");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("activeOnly");
    tester.include("inactiveOnly");
    tester.include("maxResults");
    tester.include("startIndex");
    tester.include("queryRestriction");
    tester.include("sortField");
    tester.include("sortFields");
    tester.include("ascending");
    tester.include("branch");
    tester.include("searchCriteria");

    tester.proxy(List.class, 1, list1);
    tester.proxy(List.class, 2, list2);

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
  public void testModelCopy024() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy024");
    CopyConstructorTester tester = new CopyConstructorTester(object);
    assertTrue(tester.testCopyConstructor(PfscParameter.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization024() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient024");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // tester.proxy(List.class, 1, list1);
    // tester.proxy(List.class, 2, list2);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField024() throws Exception {
    // n/a
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
