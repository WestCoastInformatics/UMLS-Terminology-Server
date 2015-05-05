/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package org.ihtsdo.otf.ts.helpers;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.XmlSerializationTester;

/**
 * Unit testing for {@link KeyValuePairList}.
 */
public class HelperUnit004Test {

  /** The helper object to test. */
  private KeyValuePairList object;

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
    object = new KeyValuePairList();
  }

  /**
   * Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse004() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHelperNormalUse004");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();

    EqualsHashcodeTester tester2 = new EqualsHashcodeTester(object);
    tester2.include("keyValuePairList");
    tester2.include("name");

    // Test equals
    KeyValuePair pair1 = new KeyValuePair("1", "1");
    KeyValuePair pair2 = new KeyValuePair("1", "1");
    KeyValuePairList list1 = new KeyValuePairList();
    list1.setName("list");
    list1.addKeyValuePair(pair1);
    KeyValuePairList list2 = new KeyValuePairList();
    list2.setName("list");
    list2.addKeyValuePair(pair1);
    assertTrue(list1.equals(list2));

    // Test change name is not equal
    list2.setName("list2");
    assertTrue(!list1.equals(list2));

    // Test change list is not equal
    list2.setName("list");
    list2.addKeyValuePair(pair2);
    assertTrue(!list1.equals(list2));

    // Test get/set of list are equal
    List<KeyValuePair> kvpList = new ArrayList<>();
    kvpList.add(pair1);
    kvpList.add(pair2);
    list1.setKeyValuePairList(kvpList);
    assertTrue(list1.getKeyValuePairList().equals(kvpList));

    CopyConstructorTester tester3 = new CopyConstructorTester(list2);
    assertTrue(tester3.testCopyConstructor(KeyValuePairList.class));

    XmlSerializationTester tester4 = new XmlSerializationTester(list2);
    assertTrue(tester4.testXmlSerialization());

    list1.toString();
  }

  /**
   * Test degenerate use of the helper object.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method"
  })
  @Test
  public void testHelperDegenerateUse004() throws Exception {
    try {
      new KeyValuePairList(null);
      fail("Expected exception did not occur.");
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
  public void testHelperEdgeCases004() throws Exception {
    KeyValuePairList list1 = new KeyValuePairList();
    list1.setName(null);
    list1.setKeyValuePairList(null);

    KeyValuePairList list2 = new KeyValuePairList();
    list2.setName(null);
    list2.setKeyValuePairList(null);

    assertTrue(list1.equals(list2));
    assertTrue(list1.hashCode() == list2.hashCode());
    list1.toString();
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
