/**
 * Copyright 2016 West Coast Informatics, LLC
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
  DESCRIPTOR,
  
  /** The atom. */
  ATOM,

  /** The other. */
  OTHER;

  /**
   * Gets the id type.
   *
   * @param abbrev the abbrev
   * @return the id type
   */
  public static IdType getIdType(String abbrev) {
    if (abbrev.equals("CUI")) {
      return CONCEPT;
    } else if (abbrev.equals("SCUI")) {
      return CONCEPT;
    } else if (abbrev.equals("DUI")) {
      return DESCRIPTOR;
    } else if (abbrev.equals("CODE")) {
      return CODE;
    } else if (abbrev.equals("ATOM")) {
      return ATOM;
    }
    try {
      return IdType.valueOf(abbrev);
    } catch (Exception e) {
      return OTHER;
    }
  }

}
