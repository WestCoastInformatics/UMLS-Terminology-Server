/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Container for key value-list pairs.
 */
@XmlRootElement(name = "keyValuesMap")
public class KeyValuesMap {

  /** The map. */
  private Map<String, Values> map;

  /**
   * Instantiates an empty {@link KeyValuesMap}.
   */
  public KeyValuesMap() {
    map = new HashMap<>();
  }

  /**
   * Instantiates a {@link KeyValuesMap} from the specified parameters.
   *
   * @param map the map
   */
  public KeyValuesMap(KeyValuesMap map) {
    this.map = map.getMap();
  }

  /**
   * Gets the map.
   *
   * @return the map
   */
  @XmlElement(name = "map")
  public Map<String, Values> getMap() {
    return map;
  }

  /**
   * Sets the map.
   *
   * @param map the map
   */
  public void setMap(Map<String, Values> map) {
    this.map = map;
  }

  /**
   * Put key and value into the map.
   *
   * @param key the key
   * @param value the value
   */
  public void put(String key, String value) {
    if (!map.containsKey(key)) {
      Values values = new Values();
      map.put(key, values);
    }
    map.get(key).getSet().add(value);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((map == null) ? 0 : map.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
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
    KeyValuesMap other = (KeyValuesMap) obj;
    if (map == null) {
      if (other.map != null)
        return false;
    } else if (!map.equals(other.map))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return map.toString();
  }

  /**
   * The values class, for JAXB serialization.
   */
  @XmlRootElement(name = "values")
  public static class Values {

    /** The set. */
    protected Set<String> set;

    /**
     * Instantiates an empty {@link Values}.
     */
    public Values() {
      set = new HashSet<String>();
    }

    /**
     * Returns the sets the.
     *
     * @return the sets the
     */
    @XmlElement(name = "item")
    public Set<String> getSet() {
      return set;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
      return set.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((set == null) ? 0 : set.hashCode());
      return result;
    }

    /*
     * (non-Javadoc)
     * 
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
      Values other = (Values) obj;
      if (set == null) {
        if (other.set != null)
          return false;
      } else if (!set.equals(other.set))
        return false;
      return true;
    }

  }

}