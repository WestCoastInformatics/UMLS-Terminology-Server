/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Enum for identifier types. This is used to help bridge the gap between the
 * use of STYPE fields in RRF and the more normal model representation.
 */
public enum IdType {

  /** The code. */
  CODE,

  /** The scui. */
  CONCEPT,

  /** The sdui. */
  DESCRIPTOR

}
