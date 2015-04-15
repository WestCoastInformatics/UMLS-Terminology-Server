/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

/**
 * Enum for {@link TermType} code variant types.
 */
public enum CodeVariantType {

  /** The Attribute. */
  ATTRIBUTE("Attribute"),

  /** The pn. */
  PN("Preferred name"),

  /** The sy. */
  SY("Synonym"),

  /** The et. */
  ET("Entry term (typically for a concept with descriptor-only structure)"),

  /** The pet. */
  PET(
      "Preferred entry term (Entry term of descriptor, preferred name of its concept)"),

  /** The syet. */
  SYET(
      "Synonym entry trm (Entry term of descriptor, synonym of preferred name of its concept"),

  /** The undefined. */
  UNDEFINED("Undefined");

  /** The description. */
  private String description;

  /**
   * Instantiates a {@link CodeVariantType} from the specified parameters.
   *
   * @param description the description
   */
  private CodeVariantType(String description) {
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
