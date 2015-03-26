package com.wci.umls.server.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Container for key value pairs.
 */
@XmlRootElement(name = "keyValuePair")
public class KeyValuesMap {

  /** The map. */
  private Map<String, HashSet<String>> map;

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
  @XmlTransient
  public Map<String, HashSet<String>> getMap() {
    return map;
  }

  /**
   * Sets the map.
   *
   * @param map the map
   */
  public void setMap(Map<String, HashSet<String>> map) {
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
      HashSet<String> values = new HashSet<>();
      map.put(key, values);
    }
    map.get(key).add(value);
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
}
