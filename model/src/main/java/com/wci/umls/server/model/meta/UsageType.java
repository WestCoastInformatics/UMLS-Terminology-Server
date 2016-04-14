/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Enum for {@link TermType} usage types.
 */
public enum UsageType {

  /** The display. */
  DISPLAY("Intended for display"),

  /** The nodisplay. */
  NODISPLAY(
      "Not intended for display (e.g. a \"lookup\" term) - these may be candidates for suppressibility"),

  /** The query. */
  QUERY(
      "Representative of a query or intended to be used for querying (entry point into a graph intended for use in querying or building a query"),

  /** The qualifier. */
  QUALIFIER("Qualifier (modifier, attribute)"),

  /** The obsolete. */
  OBSOLETE("Obsolete"),

  /** The undefined. */
  UNDEFINED("Undefined");

  /** The description. */
  private String description;

  /**
   * Instantiates a {@link UsageType} from the specified parameters.
   *
   * @param description the description
   */
  private UsageType(String description) {
    this.description = description;
  }

  /**
   * Returns the description.
   *
   * @return the description
   */
  public String getDescription() {
    return description;
  }

}
