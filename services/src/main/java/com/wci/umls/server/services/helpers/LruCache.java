/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.helpers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRUCache.
 *
 * @param <K> the
 * @param <V> the
 */
@SuppressWarnings("serial")
class LruCache<K, V> extends LinkedHashMap<K, V> {

  /** The cache size. */
  private int cacheSize;

  /**
   * LRU cache.
   *
   * @param cacheSize the cache size
   */
  public LruCache(int cacheSize) {
    super(cacheSize * 2);
    this.cacheSize = cacheSize;
  }

  /**
   * Removes the eldest entry.
   *
   * @param eldest the eldest
   * @return true, if successful
   */
  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() >= cacheSize;
  }
}