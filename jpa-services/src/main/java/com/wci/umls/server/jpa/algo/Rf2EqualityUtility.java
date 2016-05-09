/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.Subset;

/**
 * Determines whether two objects rendered from RF2 are equal. This is important
 * because terminology server domain may take a component plus some attributes
 * to fully render an RF2 component.
 */
public class Rf2EqualityUtility {

  /**
   * Equality test for concepts
   *
   * @param c1 the concept
   * @param c2 the comparison concept
   * @return true, if successful
   */
  public static boolean equals(Concept c1, Concept c2) {
    if (c1.equals(c2)) {
      return compareAttributes(c1, c2, new String[] {
          "moduleId", "definitionStatusId"
      });
    }
    return false;
  }

  /**
   * Equality test for atoms.
   *
   * @param a1 the atom
   * @param a2 the comparison atom
   * @return true, if successful
   */
  public static boolean equals(Atom a1, Atom a2) {
    if (a1.equals(a2)) {
      return compareAttributes(a1, a2, new String[] {
          "moduleId", "caseSignificanceId"
      });
    }
    return false;
  }

  /**
   * Equality test for relationships.
   *
   * @param r1 the relationship
   * @param r2 the comparison relationship
   * @return true, if successful
   */
  @SuppressWarnings("rawtypes")
  public static boolean equals(Relationship r1, Relationship r2) {
    if (r1.equals(r2)) {
      return compareAttributes(r1, r2, new String[] {
          "moduleId", "characteristicTypeId", "modifierId"
      });
    }
    return false;
  }

  /**
   * Equality test for mappings.
   *
   * @param m1 the mapping
   * @param m2 the comparison mapping
   * @return true, if successful
   */
  public static boolean equals(Mapping m1, Mapping m2) {
    if (m1.equals(m2)) {
      return compareAttributes(m1, m2, new String[] {
        "moduleId"
      });
    }
    return false;
  }

  /**
   * Equality test for map sets.
   *
   * @param m1 the map set
   * @param m2 the comparison map set
   * @return true, if successful
   */
  public static boolean equals(MapSet m1, MapSet m2) {
    if (m1.equals(m2)) {
      return compareAttributes(m1, m2, new String[] {
        "moduleId"
      });
    }
    return false;
  }

  /**
   * Equality test for Subset
   *
   * @param s1 the subset
   * @param s2 the comparison subset
   * @return true, if successful
   */
  public static boolean equals(Subset s1, Subset s2) {
    if (s1.equals(s2)) {
      return compareAttributes(s1, s2, new String[] {
        "moduleId"
      });
    }
    return false;
  }

  /**
   * Compare attributes.
   *
   * @param c1 the component with attributes
   * @param c2 the comparison component
   * @param attributeNames the attribute names
   * @return true, if successful
   */
  public static boolean compareAttributes(ComponentHasAttributes c1,
    ComponentHasAttributes c2, String[] attributeNames) {
    if (attributeNames == null) {
      return true;
    }
    boolean flag = true;
    // verify all attribute names from the list have equal values
    for (String atn : attributeNames) {
      final Attribute a1 = c1.getAttributeByName(atn);
      final Attribute a2 = c2.getAttributeByName(atn);
      if (a1 == null && a2 == null) {
        continue;
      }
      if (a1 != null && a2 == null) {
        flag = false;
        break;
      }
      if (a1 == null && a2 != null) {
        flag = false;
        break;
      }
      if (a1 != null && !a1.getValue().equals(a2.getValue())) {
        flag = false;
        break;
      }
    }
    return flag;
  }

}
