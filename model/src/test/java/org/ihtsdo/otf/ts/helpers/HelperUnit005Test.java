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
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.KeyValuePairLists;
import com.wci.umls.server.helpers.XmlSerializationTester;

/**
 * Unit testing for {@link KeyValuePairLists}.
 */
public class HelperUnit005Test {

  /** The helper object to test. */
  private KeyValuePairLists object;

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
    object = new KeyValuePairLists();
  }

  /**
   * Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse005() throws Exception {
    Logger.getLogger(getClass()).info("TEST testHelperNormalUse005");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();

    // Test equals
    KeyValuePair pair1 = new KeyValuePair("1", "1");
    KeyValuePair pair2 = new KeyValuePair("1", "1");
    KeyValuePairList list1 = new KeyValuePairList();
    list1.setName("list1");
    list1.addKeyValuePair(pair1);
    KeyValuePairList list2 = new KeyValuePairList();
    list2.setName("list2");
    list2.addKeyValuePair(pair2);
    KeyValuePairLists lists1 = new KeyValuePairLists();
    lists1.addKeyValuePairList(list1);
    lists1.addKeyValuePairList(list2);
    KeyValuePairLists lists2 = new KeyValuePairLists();
    lists2.addKeyValuePairList(list1);
    lists2.addKeyValuePairList(list2);
    assertTrue(lists1.equals(lists2));
    assertTrue(lists1.hashCode() == lists2.hashCode());
    lists1.toString();

    // Test not equals
    lists2.addKeyValuePairList(list1);
    assertTrue(!lists1.equals(lists2));

    // Test get/set of list are equal
    List<KeyValuePairList> kvpLists = new ArrayList<>();
    kvpLists.add(list1);
    kvpLists.add(list2);
    lists1.setKeyValuePairLists(kvpLists);
    assertTrue(lists1.getKeyValuePairLists().equals(kvpLists));

    CopyConstructorTester tester3 = new CopyConstructorTester(lists2);
    assertTrue(tester3.testCopyConstructor(KeyValuePairLists.class));

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
      "static-method", "unused"
  })
  @Test
  public void testHelperDegenerateUse005() throws Exception {
    try {
      new KeyValuePairLists(null);
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
  public void testHelperEdgeCases005() throws Exception {
    KeyValuePairLists lists1 = new KeyValuePairLists();
    lists1.setKeyValuePairLists(null);

    KeyValuePairLists lists2 = new KeyValuePairLists();
    lists2.setKeyValuePairLists(null);

    assertTrue(lists1.equals(lists2));
    assertTrue(lists1.hashCode() == lists2.hashCode());
    lists1.toString();
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
