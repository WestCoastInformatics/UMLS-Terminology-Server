package com.wci.umls.server.jpa.helpers.meta;

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
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.meta.SemanticTypeGroupJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.SemanticTypeGroup;

/**
 * Unit testing for {@link SemanticTypeJpa}.
 */
public class ModelUnit036Test {

  /** The model object to test. */
  private SemanticTypeJpa object;

  /** The group proxy. */
  private SemanticTypeGroupJpa groupProxy;

  /** The group proxy2. */
  private SemanticTypeGroupJpa groupProxy2;

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
    object = new SemanticTypeJpa();

    ProxyTester tester = new ProxyTester(new SemanticTypeGroupJpa());
    groupProxy = (SemanticTypeGroupJpa) tester.createObject(1);
    groupProxy2 = (SemanticTypeGroupJpa) tester.createObject(2);

  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet036");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.proxy(SemanticTypeGroup.class, 1, groupProxy);
    tester.proxy(SemanticTypeGroup.class, 2, groupProxy2);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode036");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("definition");
    tester.include("example");
    tester.include("group");
    tester.include("nonHuman");
    tester.include("treeNumber");
    tester.include("typeId");
    tester.include("usageNote");
    tester.include("value");

    tester.proxy(SemanticTypeGroup.class, 1, groupProxy);
    tester.proxy(SemanticTypeGroup.class, 2, groupProxy2);

    assertTrue(tester.testIdentitiyFieldEquals());
    assertTrue(tester.testNonIdentitiyFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentitiyFieldHashcode());
    assertTrue(tester.testNonIdentitiyFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelCopy036");
    CopyConstructorTester tester = new CopyConstructorTester(object);

    tester.proxy(SemanticTypeGroup.class, 1, groupProxy);
    tester.proxy(SemanticTypeGroup.class, 2, groupProxy2);

    assertTrue(tester.testCopyConstructor(SemanticType.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization036");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // serialization only recovers id and abbreviation
    SemanticTypeGroupJpa g1 = new SemanticTypeGroupJpa();
    g1.setId(1L);
    g1.setAbbreviation("1");
    tester.proxy(SemanticTypeGroup.class, 1, g1);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField036() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelNotNullField036");
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("abbreviation");
    tester.include("expandedForm");
    tester.include("value");
    tester.include("definition");
    tester.include("typeId");
    tester.include("nonHuman");

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
