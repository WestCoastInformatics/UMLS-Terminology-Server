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
  private List<KeyValuePair> keyValuePairList = new ArrayList<>();

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
    keyValuePairList = list.getKeyValuePairList();
  }

  /**
   * Returns the key value pair list.
   * 
   * @return the key value pair list
   */
  @XmlElement(name = "keyValuePair")
  public List<KeyValuePair> getKeyValuePairList() {
    return keyValuePairList;
  }

  /**
   * Sets the key value pair list.
   * 
   * @param keyValuePairList the key value pair list
   */
  public void setKeyValuePairList(List<KeyValuePair> keyValuePairList) {
    this.keyValuePairList = keyValuePairList;
  }

  /**
   * Adds the key value pair.
   * 
   * @param keyValuePair the key value pair
   */
  public void addKeyValuePair(KeyValuePair keyValuePair) {
    keyValuePairList.add(keyValuePair);
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

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result
            + ((keyValuePairList == null) ? 0 : keyValuePairList.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    KeyValuePairList other = (KeyValuePairList) obj;
    if (keyValuePairList == null) {
      if (other.keyValuePairList != null)
        return false;
    } else if (!keyValuePairList.equals(other.keyValuePairList))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "KeyValuePairList [keyValuePairList=" + keyValuePairList + ", name="
        + name + "]";
  }

}
