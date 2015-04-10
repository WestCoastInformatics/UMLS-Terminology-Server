/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for key value pairs.
 */
@XmlRootElement(name = "keyValuePair")
public class KeyValuePair {

  /** The key. */
  private String key;

  /** The value. */
  private String value;

  /**
   * Instantiates an empty {@link KeyValuePair}.
   */
  public KeyValuePair() {
    // do nothing
  }

  /**
   * Instantiates a {@link KeyValuePair} from the specified parameters.
   *
   * @param pair the pair
   */
  public KeyValuePair(KeyValuePair pair) {
    this.key = pair.getKey();
    this.value = pair.getValue();
  }

  /**
   * Instantiates a {@link KeyValuePair} from the specified parameters.
   * 
   * @param key the key
   * @param value the value
   */
  public KeyValuePair(String key, String value) {
    this.key = key;
    this.value = value;
  }
  /**
   * Returns the key.
   * 
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the key.
   * 
   * @param key the key
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * Sets the value.
   * 
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Returns the value.
   * 
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    KeyValuePair other = (KeyValuePair) obj;
    if (key == null) {
      if (other.key != null)
        return false;
    } else if (!key.equals(other.key))
      return false;
    if (value == null) {
      if (other.value != null)
        return false;
    } else if (!value.equals(other.value))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "(" + key + ", " + value + ")";
  }
}
