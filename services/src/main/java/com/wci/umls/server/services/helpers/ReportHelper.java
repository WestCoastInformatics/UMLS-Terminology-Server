/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.helpers;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Helper class for walking graphs of objects.
 */
public class ReportHelper {

  /**
   * Returns the concept report.
   *
   * @param concept the concept
   * @return the concept report
   */
  public static String getConceptReport(Concept concept) {
    final String nl = System.getProperty("line.separator");
    final StringBuilder builder = new StringBuilder();
    builder.append(nl);
    builder.append("CONCEPT = " + concept).append(nl);
    for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
      builder.append("  STY = " + sty);
    }
    for (Atom atom : concept.getAtoms()) {
      builder.append("  ATOM = " + atom).append(nl);
      for (Attribute att : atom.getAttributes()) {
        builder.append("    ATT = " + att).append(nl);
      }
      for (Definition def : atom.getDefinitions()) {
        builder.append("    DEF = " + def).append(nl);
      }
      for (AtomRelationship rel : atom.getRelationships()) {
        builder.append("    REL = " + rel).append(nl);
      }
    }
    for (Attribute att : concept.getAttributes()) {
      builder.append("    ATT = " + att).append(nl);
    }
    for (Definition def : concept.getDefinitions()) {
      builder.append("    DEF = " + def).append(nl);
    }
    for (ConceptRelationship rel : concept.getRelationships()) {
      builder.append("  REL = " + rel).append(nl);
    }

    return builder.toString();
  }

  /**
   * Returns the descriptor report.
   *
   * @param descriptor the descriptor
   * @return the descriptor report
   */
  public static String getDescriptorReport(Descriptor descriptor) {
    final String nl = System.getProperty("line.separator");
    final StringBuilder builder = new StringBuilder();
    builder.append(nl);
    builder.append("DESCRIPTOR = " + descriptor).append(nl);
    for (Atom atom : descriptor.getAtoms()) {
      builder.append("  ATOM = " + atom).append(nl);
      for (Attribute att : atom.getAttributes()) {
        builder.append("    ATT = " + att).append(nl);
      }
      for (Definition def : atom.getDefinitions()) {
        builder.append("    DEF = " + def).append(nl);
      }
      for (AtomRelationship rel : atom.getRelationships()) {
        builder.append("    REL = " + rel).append(nl);
      }
    }
    for (Attribute att : descriptor.getAttributes()) {
      builder.append("    ATT = " + att).append(nl);
    }
    for (Definition def : descriptor.getDefinitions()) {
      builder.append("    DEF = " + def).append(nl);
    }
    for (DescriptorRelationship rel : descriptor.getRelationships()) {
      builder.append("  REL = " + rel).append(nl);
    }

    return builder.toString();
  }

  /**
   * Returns the code report.
   *
   * @param code the code
   * @return the code report
   */
  public static String getCodeReport(Code code) {
    final String nl = System.getProperty("line.separator");
    final StringBuilder builder = new StringBuilder();
    builder.append(nl);
    builder.append("CODE = " + code).append(nl);
    for (Atom atom : code.getAtoms()) {
      builder.append("  ATOM = " + atom).append(nl);
      for (Attribute att : atom.getAttributes()) {
        builder.append("    ATT = " + att).append(nl);
      }
      for (Definition def : atom.getDefinitions()) {
        builder.append("    DEF = " + def).append(nl);
      }
      for (AtomRelationship rel : atom.getRelationships()) {
        builder.append("    REL = " + rel).append(nl);
      }
    }
    for (Attribute att : code.getAttributes()) {
      builder.append("    ATT = " + att).append(nl);
    }
    for (CodeRelationship rel : code.getRelationships()) {
      builder.append("  REL = " + rel).append(nl);
    }

    return builder.toString();
  }

  /**
   * Returns the atom report.
   *
   * @param atom the atom
   * @return the atom report
   */
  public static String getAtomReport(Atom atom) {
    final String nl = System.getProperty("line.separator");
    final StringBuilder builder = new StringBuilder();
    builder.append(nl);
    builder.append("ATOM = " + atom).append(nl);
    for (Attribute att : atom.getAttributes()) {
      builder.append("  ATT = " + att).append(nl);
    }
    for (Definition def : atom.getDefinitions()) {
      builder.append("  DEF = " + def).append(nl);
    }
    for (AtomRelationship rel : atom.getRelationships()) {
      builder.append("  REL = " + rel).append(nl);
    }
    return builder.toString();
  }

}
