package org.ihtsdo.otf.ts.helpers;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.helpers.StringList;

/**
 * JUnit test for {@link ResultList} and its descendants.
 */
public class StringListTest {

  /**
   * Setup.
   */
  @Before
  public void setup() {
    // do nothing
  }

  /**
   * Test model.
   */
  @SuppressWarnings("static-method")
  @Test
  public void testStringList() {

    // Create and add some stuff
    StringList list = new StringList();
    list.addObject("abc");
    list.addObject("def");

    // Check assumptions
    Assert.assertEquals(list.getCount(), 2);
    Assert.assertTrue(list.contains("abc"));
    Assert.assertTrue(list.contains("def"));
    Assert.assertEquals(list.getObjects().size(), 2);

    // remove something not present and check assumptions
    list.removeObject("ghi");
    Assert.assertEquals(list.getCount(), 2);
    Assert.assertTrue(list.contains("abc"));
    Assert.assertTrue(list.contains("def"));
    Assert.assertEquals(list.getObjects().size(), 2);

    // remove something present and check assumptions
    list.removeObject("abc");
    Assert.assertEquals(list.getCount(), 1);
    Assert.assertFalse(list.contains("abc"));
    Assert.assertTrue(list.contains("def"));
    Assert.assertEquals(list.getObjects().size(), 1);

    // test set strings and check assumptions
    List<String> strings = new ArrayList<>();
    strings.add("def");
    strings.add("abc");
    list.setObjects(strings);
    Assert.assertEquals(list.getCount(), 2);
    Assert.assertTrue(list.contains("abc"));
    Assert.assertTrue(list.contains("def"));
    Assert.assertEquals(list.getObjects().size(), 2);

    // Test equals and hashcode
    StringList list2 = new StringList();
    list2.setObjects(strings);
    Assert.assertEquals(list, list2);
    Assert.assertEquals(list.hashCode(), list2.hashCode());

    // Test get/set Total Count
    list.setTotalCount(100);
    Assert.assertEquals(list.getTotalCount(), 100);

    // Test sorting
    Assert.assertEquals(list.getObjects().get(0), "def");
    Assert.assertEquals(list.getObjects().get(1), "abc");
    list.sortBy(String.CASE_INSENSITIVE_ORDER);
    Assert.assertEquals(list.getObjects().get(0), "abc");
    Assert.assertEquals(list.getObjects().get(1), "def");

  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

}
