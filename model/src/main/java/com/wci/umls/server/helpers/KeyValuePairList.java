/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for key value pairs (e.g., id, name metadata tuples).
 */
@XmlRootElement(name = "keyValuePairList")
public class KeyValuePairList {

  /** The entries. */
  private List<KeyValuePair> keyValuePairs = new ArrayList<>();

  /** The name. */
  private String name = "";

  /**
   * Instantiates an empty {@link KeyValuePairList}.
   */
  public KeyValuePairList() {
    // do nothing
  }

  /**
   * Instantiates a {@link KeyValuePairList} from the specified parameters.
   *
   * @param list the list
   */
  public KeyValuePairList(KeyValuePairList list) {
    name = list.getName();
    keyValuePairs = list.getKeyValuePairs();
  }

  /**
   * Returns the key value pair list.
   * 
   * @return the key value pair list
   */
  @XmlElement
  public List<KeyValuePair> getKeyValuePairs() {
    return keyValuePairs;
  }

  /**
   * Sets the key value pair list.
   * 
   * @param keyValuePairs the key value pair list
   */
  public void setKeyValuePairs(List<KeyValuePair> keyValuePairs) {
    this.keyValuePairs = keyValuePairs;
  }

  /**
   * Adds the key value pair.
   * 
   * @param keyValuePair the key value pair
   */
  public void addKeyValuePair(KeyValuePair keyValuePair) {
    keyValuePairs.add(keyValuePair);
  }

  /**
   * Sets the name.
   * 
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Indicates whether not it contains the specified key pair.
   *
   * @param pair the pair
   * @return true, if successful
   */
  public boolean contains(KeyValuePair pair) {
    return this.getKeyValuePairs().contains(pair);
  }

  /**
   * Indicates whether not it contains the specified key pair list.
   *
   * @param pairList the pair list
   * @return true, if successful
   */
  public boolean contains(KeyValuePairList pairList) {
    return this.getKeyValuePairs().containsAll(pairList.getKeyValuePairs());
  }

  /* see superclass */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((keyValuePairs == null) ? 0 : keyValuePairs.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    KeyValuePairList other = (KeyValuePairList) obj;
    if (keyValuePairs == null) {
      if (other.keyValuePairs != null)
        return false;
    } else if (!keyValuePairs.equals(other.keyValuePairs))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  /* see superclass */
  @Override
  public String toString() {
    return "KeyValuePairList [keyValuePairList=" + keyValuePairs + ", name="
        + name + "]";
  }

}
