/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.Comparator;
import java.util.List;

/**
 * Container for some kind of results
 * @param <T> the type for sorting
 */
public interface ResultList<T> {

  /**
   * Returns the number of objects in the list.
   * @return the number of objects in the list
   */
  public int getCount();

  /**
   * Returns the total count.
   * 
   * @return the totalCount
   */
  public int getTotalCount();

  /**
   * Sets the total count.
   * 
   * @param totalCount the totalCount to set
   */
  public void setTotalCount(int totalCount);

  /**
   * Sorts by the specified comparator.
   * 
   * @param comparator the comparator
   */
  public void sortBy(Comparator<T> comparator);

  /**
   * Indicates whether or not the list contains the specified element.
   * 
   * @param element the element
   * @return <code>true</code> if so; <code>false</code> otherwise
   */
  public boolean contains(T element);

  /**
   * Adds the object.
   *
   * @param object the object
   */
  public void addObject(T object);

  /**
   * Removes the object.
   *
   * @param object the object
   */
  public void removeObject(T object);

  /**
   * Sets the objects.
   *
   * @param objects the new objects
   */
  public void setObjects(List<T> objects);

  /**
   * Gets the objects.
   * 
   * @return the objects
   */
  public List<T> getObjects();
}
