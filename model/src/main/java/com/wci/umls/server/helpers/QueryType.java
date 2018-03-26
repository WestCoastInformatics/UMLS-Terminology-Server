/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Enumeration of query type values.
 */
public enum QueryType {

  /** The JPQL type. */
  JPQL,

  /** The sql type. */
  SQL,

  /** The lucene type. */
  LUCENE,

  /**
   * TODO: allow programmatic queries, they return List(Object[]) from some
   * interface (need a way to inject parameters)
   */
  PROGRAM;

}