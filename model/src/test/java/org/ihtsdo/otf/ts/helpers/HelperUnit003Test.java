/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.ts.helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.XmlSerializationTester;

/**
 * Unit testing for {@link KeyValuePair}.
 */
public class HelperUnit003Test {

  /** The helper object to test. */
  private KeyValuePair object;

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
    object = new KeyValuePair();
  }

  /**
   * Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse003() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHelperNormalUse003");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();

    EqualsHashcodeTester tester2 = new EqualsHashcodeTester(object);
    tester2.include("key");
    tester2.include("value");
    assertTrue(tester2.testIdentitiyFieldEquals());
    assertTrue(tester2.testNonIdentitiyFieldEquals());
    assertTrue(tester2.testIdentityFieldNotEquals());
    assertTrue(tester2.testIdentitiyFieldHashcode());
    assertTrue(tester2.testNonIdentitiyFieldHashcode());
    assertTrue(tester2.testIdentityFieldDifferentHashcode());

    CopyConstructorTester tester3 = new CopyConstructorTester(object);
    assertTrue(tester3.testCopyConstructor(KeyValuePair.class));

    XmlSerializationTester tester4 = new XmlSerializationTester(object);
    assertTrue(tester4.testXmlSerialization());

    // test normal toString behavior
    object.toString();
  }

  /**
   * Test degenerate use of the helper object.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", 
  })
  @Test
  public void testHelperDegenerateUse003() throws Exception {
    try {
      KeyValuePair p = new KeyValuePair(null);
      fail("Expected exception did not occur." + p);
    } catch (Exception e) {
      // do nothing, this is expected
    }
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  @Test
  public void testHelperEdgeCases003() throws Exception {
    // Expect no exceptions
    KeyValuePair pair = new KeyValuePair();
    pair.setKey(null);
    pair.setValue(null);
    KeyValuePair pair2 = new KeyValuePair(null, null);

    // verify tostring behavior
    pair.toString();
    pair2.toString();

    // assert equality
    assertTrue(pair.equals(pair2));
    assertTrue(pair.hashCode() == pair2.hashCode());

    KeyValuePair pair3 = new KeyValuePair(pair);
    assertTrue(pair.equals(pair3));
    assertTrue(pair.hashCode() == pair3.hashCode());
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
