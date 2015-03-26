package org.ihtsdo.otf.ts.helpers;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.KeyValuePairLists;

/**
 * JUnit tgest for {@link KeyValuePair}, {@link KeyValuePairList}, and
 * {@link KeyValuePairLists}.
 */
public class KeyValuePairTest {

  /**
   * Setup.
   */
  @Before
  public void setup() {
    // do nothing
  }

  /**
   * Test {@link KeyValuePair}.
   */
  @Test
  public void testKeyValuePair() {
    try {
      // Test getter/setter
      KeyValuePair pair = new KeyValuePair();
      Logger.getLogger(this.getClass()).info(
          "  Testing " + pair.getClass().getName());
      GetterSetterTester tester = new GetterSetterTester(pair);
      tester.exclude("objectId");
      tester.test();

      // test equals/hashcode
      pair.setKey("key1");
      pair.setValue("value1");
      KeyValuePair pair2 = new KeyValuePair("key1", "value1");
      KeyValuePair pair3 = new KeyValuePair("key2", "value2");
      Assert.assertEquals(pair, pair2);
      Assert.assertEquals(pair.hashCode(), pair2.hashCode());
      Assert.assertNotEquals(pair, pair3);
      Assert.assertNotEquals(pair.hashCode(), pair3.hashCode());

    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  /**
   * Test {@link KeyValuePair}.
   */
  @SuppressWarnings("static-method")
  @Test
  public void testKeyValuePairList() {

    KeyValuePairList pairs1 = new KeyValuePairList();
    KeyValuePairList pairs2 = new KeyValuePairList();
    KeyValuePair pair1 = new KeyValuePair("key1", "value1");
    KeyValuePair pair2 = new KeyValuePair("key2", "value2");
    KeyValuePair pair3 = new KeyValuePair("key3", "value3");

    pairs1.setName("abc");
    pairs2.setName("abc");
    Assert.assertEquals(pairs1.getName(), "abc");

    // Test equality, hashcodes
    pairs1.addKeyValuePair(pair1);
    pairs1.addKeyValuePair(pair2);
    pairs2.addKeyValuePair(pair1);
    pairs2.addKeyValuePair(pair2);
    Assert.assertEquals(pairs1, pairs2);
    Assert.assertEquals(pairs1.hashCode(), pairs2.hashCode());
    pairs2.addKeyValuePair(pair3);
    Assert.assertNotEquals(pairs1, pairs2);
    Assert.assertNotEquals(pairs1.hashCode(), pairs2.hashCode());

    // test get/set
    pairs1.setKeyValuePairList(pairs2.getKeyValuePairList());
    Assert.assertEquals(pairs1, pairs2);
    Assert.assertEquals(pairs1.hashCode(), pairs2.hashCode());

  }

  /**
   * Test {@link KeyValuePair}.
   */
  @Test
  public void testKeyValuePairLists() {
    // TODO: implement this
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

}
