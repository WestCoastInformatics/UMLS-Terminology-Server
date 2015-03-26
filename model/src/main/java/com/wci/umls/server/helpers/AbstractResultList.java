package com.wci.umls.server.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.ResultList#getTotalCount()
   */
  @Override
  public int getTotalCount() {
    return totalCount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.mapping.helpers.ResultList#setTotalCount(int)
   */
  @Override
  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#getCount()
   */
  @Override
  public int getCount() {
    return objects.size();
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#sortBy(java.util.Comparator)
   */
  @Override
  public void sortBy(Comparator<T> comparator) {
    Collections.sort(objects, comparator);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#contains(java.lang.Object)
   */
  @Override
  public boolean contains(T element) {
    return objects.contains(element);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#addObject(java.lang.Object)
   */
  @Override
  public void addObject(T object) {
    objects.add(object);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#removeObject(java.lang.Object)
   */
  @Override
  public void removeObject(T object) {
    objects.remove(object);
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#setObjects(java.util.List)
   */
  @Override
  public void setObjects(List<T> objects) {
    this.objects = objects;
  }

  /* (non-Javadoc)
   * @see org.ihtsdo.otf.ts.helpers.ResultList#getObjects()
   */
  @Override
  public List<T> getObjects() {
    return objects;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((objects == null) ? 0 : objects.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
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

}
