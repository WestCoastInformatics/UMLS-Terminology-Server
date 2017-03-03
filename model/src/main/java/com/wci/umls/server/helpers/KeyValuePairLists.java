/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for all lists of key value pair lists.
 */
@XmlRootElement(name = "keyValuePairLists")
public class KeyValuePairLists {

  /** The key value pair lists. */
  private List<KeyValuePairList> keyValuePairLists = new ArrayList<>();

  /**
   * Instantiates an empty {@link KeyValuePairLists}.
   */
  public KeyValuePairLists() {
    // do nothing
  }

  /**
   * Instantiates an empty {@link KeyValuePairLists}.
   *
   * @param lists the lists
   */
  public KeyValuePairLists(KeyValuePairLists lists) {
    keyValuePairLists = new ArrayList<>(lists.getKeyValuePairLists());
  }

  /**
   * Returns the key value pair lists.
   * 
   * @return the key value pair lists
   */
  @XmlElement
  public List<KeyValuePairList> getKeyValuePairLists() {
    return keyValuePairLists;
  }

  /**
   * Sets the key value pair lists.
   * 
   * @param keyValuePairLists the key value pair lists
   */
  public void setKeyValuePairLists(List<KeyValuePairList> keyValuePairLists) {
    this.keyValuePairLists = keyValuePairLists;
  }

  /**
   * Returns the count.
   * 
   * @return the count
   */
  @XmlElement(name = "count")
  public int getCount() {
    return keyValuePairLists.size();
  }

  /**
   * Adds the key value pair list.
   * 
   * @param keyValuePairList the key value pair list
   */
  public void addKeyValuePairList(KeyValuePairList keyValuePairList) {
    keyValuePairLists.add(keyValuePairList);

  }

  /**
   * Removes the id name map.
   * 
   * @param keyValuePairList the key value pair list
   */
  public void removeKeyValuePairList(KeyValuePairList keyValuePairList) {
    keyValuePairLists.remove(keyValuePairList);

  }

  /**
   * Sort the elements.
   */
  public void sort() {
    for (final KeyValuePairList list : getKeyValuePairLists()) {
      Collections.sort(list.getKeyValuePairs());
    }
    Collections.sort(getKeyValuePairLists(),
        new Comparator<KeyValuePairList>() {
          @Override
          public int compare(KeyValuePairList o1, KeyValuePairList o2) {
            return o1.getName().compareTo(o2.getName());
          }
        });
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((keyValuePairLists == null) ? 0 : keyValuePairLists.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KeyValuePairLists other = (KeyValuePairLists) obj;
    if (keyValuePairLists == null) {
      if (other.keyValuePairLists != null)
        return false;
    } else if (!keyValuePairLists.equals(other.keyValuePairLists))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "KeyValuePairLists [keyValuePairLists=" + keyValuePairLists + "]";
  }
}
