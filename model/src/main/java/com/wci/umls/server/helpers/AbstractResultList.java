/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Abstract implementation of {@link ResultList}.
 *
 * @param <T> the type sorting
 */
public abstract class AbstractResultList<T> implements ResultList<T> {

  /** The total count. */
  private int totalCount = 0;

  /** The objects. */
  private List<T> objects = new ArrayList<>();

  /* see superclass */
  @Override
  public int getTotalCount() {
    return totalCount;
  }

  /* see superclass */
  @Override
  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  /* see superclass */
  @Override
  public int size() {
    return objects.size();
  }

  /* see superclass */
  @Override
  public void sortBy(Comparator<T> comparator) {
    Collections.sort(objects, comparator);
  }

  /* see superclass */
  @Override
  public boolean contains(T element) {
    return objects.contains(element);
  }

  /* see superclass */
  @Override
  public void setObjects(List<T> objects) {
    this.objects = objects;
  }

  /**
   * Returns the but in an XML transient way.
   *
   * @return the objects transient
   */
  @XmlTransient
  protected List<T> getObjectsTransient() {
    return objects;
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((objects == null) ? 0 : objects.hashCode());
    return result;
  }

  /* see superclass */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("rawtypes")
    AbstractResultList other = (AbstractResultList) obj;
    if (objects == null) {
      if (other.objects != null)
        return false;
    } else if (!objects.equals(other.objects))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(getClass().getSimpleName() + " [totalCount=" + totalCount
        + ", objects=[");
    for (final Object o : objects) {
      sb.append(o.toString()).append(",");
    }
    sb.append("]");
    return sb.toString();
  }

}
