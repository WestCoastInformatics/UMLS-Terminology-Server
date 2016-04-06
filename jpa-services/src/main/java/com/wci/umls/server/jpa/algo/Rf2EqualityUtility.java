/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;

/**
 * Determines whether two objects rendered from RF2 are equal. This is important
 * because terminology server domain may take a component plus some attributes
 * to fully render an RF2 component.
 */
public class Rf2EqualityUtility {

  /**
   * Equal.
   *
   * @param c1 the c1
   * @param c2 the c2
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
   * Equals.
   *
   * @param a1 the a1
   * @param a2 the a2
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
   * Equals.
   *
   * @param r1 the r1
   * @param r2 the r2
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
   * Equals.
   *
   * @param m1 the m1
   * @param m2 the m2
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
   * Compare attributes.
   *
   * @param c1 the c1
   * @param c2 the c2
   * @param attNames the att names
   * @return true, if successful
   */
  public static boolean compareAttributes(ComponentHasAttributes c1,
    ComponentHasAttributes c2, String[] attNames) {
    if (attNames == null) {
      return true;
    }
    boolean flag = true;
    // verify all attribute names from the list have equal values
    for (String atn : attNames) {
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
      if (a1 != null && !a1.getValue().equals(a2. getValue())) {
        flag = false;
        break;
      }
    }
    return flag;
  }

}
