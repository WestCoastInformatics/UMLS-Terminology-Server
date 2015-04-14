/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.lists;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ResultList;

/**
 * Unit testing for {@link ResultList}s.
 *
 * @param <T> the
 */
public class AbstractListUnit<T> {

  /**
   * Test normal use of a list.
   *
   * @param list the list
   * @param list2 the list2
   * @param object1 the object1
   * @param object2 the object2
   * @throws Exception the exception
   */
  public void testNormalUse(ResultList<T> list, ResultList<T> list2, T object1,
    T object2) throws Exception {

    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);
    assertTrue(list.getTotalCount() == 0);

    list.addObject(object1);
    assertTrue(list.getObjects().size() == 1);
    assertTrue(list.getCount() == 1);
    assertTrue(list.getTotalCount() == 0);

    assertFalse(list.contains(object2));

    list.addObject(object2);
    assertTrue(list.getObjects().size() == 2);
    assertTrue(list.getCount() == 2);
    assertTrue(list.getTotalCount() == 0);

    assertTrue(list.contains(object1));
    assertTrue(list.contains(object2));

    list2.addObject(object1);
    assertFalse(list.equals(list2));
    list2.addObject(object2);
    assertTrue(list.equals(list2));
    assertTrue(list.getObjects().equals(list2.getObjects()));

    list.setTotalCount(5);
    assertTrue(list.getTotalCount() == 5);

    list.removeObject(object1);
    assertTrue(list.getObjects().size() == 1);
    assertTrue(list.getCount() == 1);
    assertTrue(list.getTotalCount() == 5);

    list.removeObject(object2);
    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);
    assertTrue(list.getTotalCount() == 5);

    List<T> list3 = new ArrayList<T>();
    list3.add(object1);
    list3.add(object2);
    list.setObjects(list3);
    assertTrue(list.getObjects().size() == 2);
    assertTrue(list.getCount() == 2);
    assertTrue(list.getTotalCount() == 5);
    assertTrue(list.equals(list2));

    // sortBy not tested here
  }

  /**
   * Test degenerate use of a list. Show that the underlying data structure
   * should NOT be manipulated.
   *
   * @param list the list
   * @param list2 the list2
   * @param object1 the object1
   * @param object2 the object2
   * @throws Exception the exception
   */
  public void testDegenerateUse(ResultList<T> list, ResultList<T> list2,
    T object1, T object2) throws Exception {

    // get underlying objects list and change it
    list.addObject(object1);
    List<T> list3 = list.getObjects();
    list3.add(object2);

    assertTrue(list.getObjects().size() == 2);
    assertTrue(list.getCount() == 2);

    list3.remove(object1);
    list3.remove(object2);
    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);
    assertFalse(list.contains(object1));

    // Set underlying object to null then try to add an object
    list.setObjects(null);
    try {
      list.addObject(object1);
      fail("Expected exception did not occur.");
    } catch (Exception e) {
      // expected outcome
    }
    try {
      list.getCount();
      fail("Expected exception did not occur.");
    } catch (Exception e) {
      // expected outcome
    }

  }

  /**
   * Test edge cases of a list.
   *
   * @param list the list
   * @param list2 the list2
   * @param object1 the object1
   * @param object2 the object2
   * @throws Exception the exception
   */
  public void testEdgeCases(ResultList<T> list, ResultList<T> list2,
    T object1, T object2) throws Exception {

    // add and remove null
    list.addObject(null);
    assertTrue(list.getObjects().size() == 1);
    assertTrue(list.getCount() == 1);
    list.removeObject(null);
    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);

    // add the same thing multiple times and remove it multiple times
    list.addObject(object1);
    assertTrue(list.getObjects().size() == 1);
    assertTrue(list.getCount() == 1);
    list.addObject(object1);
    assertTrue(list.getObjects().size() == 2);
    assertTrue(list.getCount() == 2);
    list.removeObject(object1);
    assertTrue(list.getObjects().size() == 1);
    assertTrue(list.getCount() == 1);
    list.removeObject(object1);
    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);

    // add once and remove multiple times
    list.addObject(object1);
    assertTrue(list.getObjects().size() == 1);
    assertTrue(list.getCount() == 1);
    list.removeObject(object1);
    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);
    list.removeObject(object1);
    assertTrue(list.getObjects().size() == 0);
    assertTrue(list.getCount() == 0);

    // contains null
    assertFalse(list.contains(null));
    list.addObject(null);
    assertTrue(list.contains(null));
    list.removeObject(null);
    assertFalse(list.contains(null));

    // total count is managed by user, any value is allowed
    list.setTotalCount(-1);
    assertTrue(list.getTotalCount() == -1);

  }

  /**
   * Test XML serialization of a list.
   *
   * @param list the list
   * @param list2 the list2
   * @param object1 the object1
   * @param object2 the object2
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void testXmllSerialization(ResultList<T> list, ResultList<T> list2,
    T object1, T object2) throws Exception {

    // test serializing an empty list (e.g. no exception)
    String xml = ConfigUtility.getStringForGraph(list);

    // Add contents
    list.addObject(object1);
    xml = ConfigUtility.getStringForGraph(list);
    ResultList<T> list3 =
        (ResultList<T>) ConfigUtility.getGraphForString(xml, list.getClass());
    System.out.println("LIST = " + list.toString());
    System.out.println("LIST3 = " + list3.toString());
    assertTrue(list.equals(list3));

    // Add 2 contents
    list.addObject(object2);
    xml = ConfigUtility.getStringForGraph(list);
    list3 =
        (ResultList<T>) ConfigUtility.getGraphForString(xml, list.getClass());
    assertTrue(list.equals(list3));

    // test serializing with a null array
    list.setObjects(null);
    xml = ConfigUtility.getStringForGraph(list);

  }
}
