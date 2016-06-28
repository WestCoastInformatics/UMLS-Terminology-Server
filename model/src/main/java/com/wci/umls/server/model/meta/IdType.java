/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.model.content.Component;

/**
 * Enum for identifier types. This is used to help bridge the gap between the
 * use of STYPE fields in RRF and the more normal model representation.
 */
public enum IdType {

  /** The code. */
  CODE,

  /** The concept. */
  CONCEPT,

  /** The descriptor. */
  DESCRIPTOR,

  /** The lexical class. */
  LEXICAL_CLASS,

  /** The string class. */
  STRING_CLASS,

  /** The atom. */
  ATOM,

  /** The other. */
  OTHER,

  /** The semantic type. */
  SEMANTIC_TYPE,

  /** The relationship. */
  RELATIONSHIP,

  /** The attribute. */
  ATTRIBUTE,

  /** The subset. */
  SUBSET,

  /** The member. */
  MEMBER,

  /** The map set. */
  MAP_SET,

  /** The mapping. */
  MAPPING,

  /** The component history. */
  COMPONENT_HISTORY,

  /** The general concep axiom. */
  GENERAL_CONCEP_AXIOM,

  /** The definition. */
  DEFINITION;

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

  /**
   * Returns the id type.
   *
   * @param component the component
   * @return the id type
   * @throws Exception the exception
   */
  public static IdType getIdType(Component component) throws Exception {
    final String type = component.getClass().getName().toUpperCase();
    for (final IdType value : IdType.values()) {
      final String valueStr = value.toString().replaceAll("_", "");
      if (type.contains(valueStr)) {
        return value;
      }
    }
    throw new Exception("Unable to determine IdType " + type);
  }

  /**
   * Returns the id type.
   *
   * @param <T> the
   * @param clazz the clazz
   * @return the id type
   * @throws Exception the exception
   */
  public static <T extends Component> IdType getIdType(Class<T> clazz)
    throws Exception {
    final String type = clazz.getName().toUpperCase();
    for (final IdType value : IdType.values()) {
      final String valueStr = value.toString().replaceAll("_", "");
      if (type.contains(valueStr)) {
        return value;
      }
    }
    throw new Exception("Unable to determine IdType " + type);
  }
}