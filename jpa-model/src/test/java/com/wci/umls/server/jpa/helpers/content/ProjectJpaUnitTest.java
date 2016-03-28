/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.content;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;

/**
 * Unit testing for {@link ProjectJpa}.
 */
public class ProjectJpaUnitTest {

  /** The model object to test. */
  private ProjectJpa object;

  /** The test fixture s1. */
  private Set<String> s1;

  /** The test fixture s2. */
  private Set<String> s2;

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
    object = new ProjectJpa();

    // This one is tricky because there are two kinds of sets
    // <String> and <User> and we can't effectively proxy both
    // Create empty sets and ignore in equals comparison.
    s1 = new HashSet<>();
    s2 = new HashSet<>();
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet001");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("objectId");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode001");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("name");
    // tester.include("scopeConcepts");
    tester.include("scopeDescendantsFlag");
    // tester.include("scopeExcludesConcepts");
    tester.include("scopeExcludesDescendantsFlag");
    tester.include("terminology");
    tester.include("version");

    // Set up objects
    tester.proxy(Set.class, 1, s1);
    tester.proxy(Set.class, 2, s2);

    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());

    // Explicitly test scopeConcepts
    Project p1 = new ProjectJpa();
    Project p2 = new ProjectJpa();
    assertEquals(p1, p2);

    Set<String> s1 = new HashSet<>();
    s1.add("abc");
    Set<String> s2 = new HashSet<>();
    s2.add("def");


  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy001");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up objects
    tester.proxy(Set.class, 1, s1);
    tester.proxy(Set.class, 2, s2);

    assertTrue(tester.testCopyConstructor(Project.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlTransient001");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // Set up objects
    tester.proxy(Set.class, 1, s1);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField001");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("name");
    tester.include("description");
    tester.include("isPublic");
    tester.include("terminology");
    tester.include("version");
    tester.include("scopeDescendantsFlag");
    tester.include("scopeExcludesDescendantsFlag");
    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields001() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields001");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    tester.include("description");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("terminology");
    tester.include("version");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
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
