package com.wci.umls.server.model.meta;

/**
 * Enum for {@link TermType} name variant types.
 */
public enum NameVariantType {

  /** The lexical variant. */
  LEXICAL_VARIANT("Lexical variant (synonym)"),

  /** The sfo. */
  SFO("Short form (synonym)"),

  /** The ab. */
  AB("Abbreviation or acronym (synonym)"),

  /** The expanded. */
  EXPANDED("Expanded form (synonym)"),

  /** The fn. */
  FN("Fully specified name (synonym)"),

  /** The langauge variant. */
  LANGAUGE_VARIANT("Language variant (synonym)"),

  /** The common. */
  COMMON("Common, or colloquial form (synonym)");

  /** The description. */
  private String description;

  /**
   * Instantiates a {@link NameVariantType} from the specified parameters.
   *
   * @param description the description
   */
  private NameVariantType(String description) {
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
