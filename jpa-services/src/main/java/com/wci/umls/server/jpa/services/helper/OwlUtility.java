/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import com.wci.umls.server.helpers.ConfigUtility;

/**
 * Helper class for working with Owl API.
 */
public class OwlUtility {

  /**
   * Check OWL2EL profile. From http://www.w3.org/TR/owl2-profiles/ NOTE:
   * features related to individuals are NOT supported.
   * 
   * <pre>
   * OWL 2 EL places restrictions on the type of class restrictions that can be used in axioms. 
   * In particular, the following types of class restrictions are supported:
   * 
   * - existential quantification to a class expression (ObjectSomeValuesFrom) or a data range (DataSomeValuesFrom)
   * - existential quantification to an individual (ObjectHasValue) or a literal (DataHasValue) self-restriction (ObjectHasSelf)
   * - NOTE (about individuals): enumerations involving a single individual (ObjectOneOf) or a single literal (DataOneOf)
   * - intersection of classes (ObjectIntersectionOf) and data ranges (DataIntersectionOf)
   * 
   * OWL 2 EL supports the following axioms, all of which are restricted to the allowed set of class expressions:
   * 
   * - class inclusion (SubClassOf)
   * - class equivalence (EquivalentClasses)
   * - class disjointness (DisjointClasses)
   * - object property inclusion (SubObjectPropertyOf) with or without property chains, and 
   * - data property inclusion (SubDataPropertyOf)
   * - property equivalence (EquivalentObjectProperties and EquivalentDataProperties),
   * - transitive object properties (TransitiveObjectProperty)
   * - reflexive object properties (ReflexiveObjectProperty)
   * - domain restrictions (ObjectPropertyDomain and DataPropertyDomain)
   * - range restrictions (ObjectPropertyRange and DataPropertyRange)
   * - NOTE: (about individuals):assertions (SameIndividual, DifferentIndividuals, ClassAssertion, ObjectPropertyAssertion, DataPropertyAssertion, NegativeObjectPropertyAssertion, and NegativeDataPropertyAssertion)
   * - functional data properties (FunctionalDataProperty)
   * - NOTE: (not supported) keys (HasKey)
   * </pre>
   * 
   * * @param directOntology the direct ontology
   * @throws Exception the exception
   */
  public static void checkOWL2ELProfile(OWLOntology directOntology)
    throws Exception {
    //
    // Compliance testing - OWL 2 EL
    //
    OWL2ELProfile profile = new OWL2ELProfile();
    OWLProfileReport report = profile.checkOntology(directOntology);
    if (!report.isInProfile()) {

      boolean flag = false;
      for (OWLProfileViolation violation : report.getViolations()) {
        // Allow violation: Use of undeclared annotation property
        if (violation.toString().indexOf(
            "Use of undeclared annotation property") == -1) {
          flag = true;
          break;
        }
      }
      if (flag) {
        throw new Exception("OWL is not in expected profile OWL EL 2 - "
            + report);
      }
    }
  }

  /**
   * Check OWL2DL profile.
   *
   * @param directOntology the direct ontology
   * @throws Exception the exception
   */
  public static void checkOWL2DLProfile(OWLOntology directOntology)
    throws Exception {
    //
    // Compliance testing - OWL 2 DL
    //
    OWL2DLProfile profile = new OWL2DLProfile();
    OWLProfileReport report = profile.checkOntology(directOntology);
    if (!report.isInProfile()) {

      boolean flag = false;
      for (OWLProfileViolation violation : report.getViolations()) {
        // Allow violation: Use of undeclared annotation property
        if (violation.toString().indexOf(
            "Use of undeclared annotation property") == -1) {
          flag = true;
          break;
        }
      }
      if (flag) {
        throw new Exception("OWL is not in expected profile OWL DL 2 - "
            + report);
      }
    }
  }

  /**
   * Log ontology for debugging.
   *
   * @param ontology the ontology
   */
  public static void logOntology(OWLOntology ontology) {
    Logger.getLogger(OwlUtility.class).info("  ontology = " + ontology);
    Logger.getLogger(OwlUtility.class).info(
        "    IRI = " + ontology.getOntologyID().getOntologyIRI());
    Logger.getLogger(OwlUtility.class).info(
        "    vIRI = " + ontology.getOntologyID().getVersionIRI());

    Logger.getLogger(OwlUtility.class).debug(
        "  Imports = " + ontology.getImports());
    Logger.getLogger(OwlUtility.class).debug(
        "    Direct imports = " + ontology.getDirectImports());
    Logger.getLogger(OwlUtility.class).debug(
        "    Imports closure = " + ontology.getImportsClosure());
    Logger.getLogger(OwlUtility.class).debug(
        "    Axiom count = " + ontology.getAxiomCount());
    Logger.getLogger(OwlUtility.class).debug(
        "    Logical axiom count = " + ontology.getLogicalAxiomCount());
    // Logger.getLogger(OwlUtility.class).debug(
    // "  AboxAxioms (imports excluded) = "
    // + ontology.getABoxAxioms(Imports.EXCLUDED));
    //
    Logger.getLogger(OwlUtility.class).debug(
        "    Annotation properties in signature = "
            + ontology.getAnnotationPropertiesInSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    Annotations = " + ontology.getAnnotations());
    Logger.getLogger(OwlUtility.class).debug(
        "    Anonymous individuals = " + ontology.getAnonymousIndividuals());
    Logger.getLogger(OwlUtility.class).debug(
        "    Classes in signature = " + ontology.getClassesInSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    Data properties in signature = "
            + ontology.getDataPropertiesInSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    Data types in signature = " + ontology.getDatatypesInSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    General class axioms = " + ontology.getGeneralClassAxioms());
    Logger.getLogger(OwlUtility.class).debug(
        "    Individuals in signature = "
            + ontology.getIndividualsInSignature());
    // Logger.getLogger(OwlUtility.class).debug(
    // "    Nested class expressions = "
    // + ontology.getNestedClassExpressions());
    Logger.getLogger(OwlUtility.class).debug(
        "    Object properties in signature = "
            + ontology.getObjectPropertiesInSignature());
    // Logger.getLogger(OwlUtility.class).debug(
    // "    Signature = " + ontology.getSignature());
  }

  /**
   * Log owl class expression.
   *
   * @param expr the expr
   * @param ontology the ontology
   * @param level the level
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  public static void logOwlClassExpression(OWLClassExpression expr,
    OWLOntology ontology, int level) throws Exception {
    final String indent = ConfigUtility.getIndentForLevel(level);
    Logger.getLogger(OwlUtility.class).info(
        indent + "class expression = " + expr);

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  signature = " + expr.getSignature());

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  class expression type = " + expr.getClassExpressionType());
    Logger.getLogger(OwlUtility.class).debug(
        indent + "  anonymous = " + expr.isAnonymous());

    // Things connected to the expr

    // Only works in OWLAPI 4
    // Logger.getLogger(OwlUtility.class).debug(
    // indent + "  annotation properties in signature = "
    // + expr.getAnnotationPropertiesInSignature().size());
    // for (OWLAnnotationProperty prop :
    // expr.getAnnotationPropertiesInSignature()) {
    // Logger.getLogger(OwlUtility.class).debug(indent + "    prop = " + prop);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  data type properties in signature = "
            + expr.getDataPropertiesInSignature().size());
    for (OWLDataProperty prop : expr.getDataPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    prop = " + prop);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  object properties in signature = "
            + expr.getObjectPropertiesInSignature());
    for (OWLObjectProperty prop : expr.getObjectPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    prop = " + prop);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  nested class expressions = "
            + expr.getNestedClassExpressions().size());
    for (OWLClassExpression expr2 : expr.getNestedClassExpressions()) {
      Logger.getLogger(OwlUtility.class).debug(
          indent + "    class expr = " + expr2);
    }

    if (expr instanceof OWLObjectIntersectionOf) {
      Logger.getLogger(OwlUtility.class).debug(
          indent + "  operands = "
              + ((OWLObjectIntersectionOf) expr).getOperands().size());
      for (OWLClassExpression expr2 : ((OWLObjectIntersectionOf) expr)
          .getOperands()) {
        Logger.getLogger(OwlUtility.class).debug(
            indent + "    operand = " + expr2);
      }
    }

    if (expr instanceof OWLRestriction) {
      Logger.getLogger(OwlUtility.class).debug(
          indent + "  property = " + ((OWLRestriction) expr).getProperty());
    }

    // things connected to ontology

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  class assertion axioms = "
            + ontology.getClassAssertionAxioms(expr).size());
    for (OWLClassAssertionAxiom axiom : ontology.getClassAssertionAxioms(expr)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

  }

  /**
   * Log owl class.
   *
   * @param owlClass the owl class
   * @param ontology the ontology
   * @param level the level
   * @throws Exception the exception
   */
  public static void logOwlClass(OWLClass owlClass, OWLOntology ontology,
    int level) throws Exception {
    final String indent = ConfigUtility.getIndentForLevel(level);
    Logger.getLogger(OwlUtility.class).info(indent + "class = " + owlClass);
    Logger.getLogger(OwlUtility.class).debug(
        indent + "  IRI = " + owlClass.getIRI());

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  signature = " + owlClass.getSignature());

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  class expression type = "
            + owlClass.getClassExpressionType());
    Logger.getLogger(OwlUtility.class).debug(
        indent + "  entity type = " + owlClass.getEntityType());
    Logger.getLogger(OwlUtility.class).debug(
        indent + "  anonymous = " + owlClass.isAnonymous());
    Logger.getLogger(OwlUtility.class).debug(
        indent + "  class expression type = "
            + owlClass.getClassExpressionType());

    // Things connected to the class

    // Only works in OWLAPI 4
    // Logger.getLogger(OwlUtility.class).debug(
    // indent + "  annotation properties in signature = "
    // + owlClass.getAnnotationPropertiesInSignature().size());
    // for (OWLAnnotationProperty prop : owlClass
    // .getAnnotationPropertiesInSignature()) {
    // Logger.getLogger(OwlUtility.class).debug(indent + "    prop = " + prop);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  data type properties in signature = "
            + owlClass.getDataPropertiesInSignature().size());
    for (OWLDataProperty prop : owlClass.getDataPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    prop = " + prop);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  object properties in signature = "
            + owlClass.getObjectPropertiesInSignature());
    for (OWLObjectProperty prop : owlClass.getObjectPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    prop = " + prop);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  nested class expressions = "
            + owlClass.getNestedClassExpressions().size());
    for (OWLClassExpression expr : owlClass.getNestedClassExpressions()) {
      Logger.getLogger(OwlUtility.class).debug(
          indent + "    class expr = " + expr);
    }

    // things connected to ontology

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  class assertion axioms = "
            + ontology.getClassAssertionAxioms(owlClass).size());
    for (OWLClassAssertionAxiom axiom : ontology
        .getClassAssertionAxioms(owlClass)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  disjoint classes axioms = "
            + ontology.getDisjointClassesAxioms(owlClass).size());
    for (OWLDisjointClassesAxiom axiom : ontology
        .getDisjointClassesAxioms(owlClass)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  disjoint union axioms = "
            + ontology.getDisjointUnionAxioms(owlClass).size());
    for (OWLDisjointUnionAxiom axiom : ontology
        .getDisjointUnionAxioms(owlClass)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  equivalent classes axioms = "
            + ontology.getEquivalentClassesAxioms(owlClass).size());
    for (OWLEquivalentClassesAxiom axiom : ontology
        .getEquivalentClassesAxioms(owlClass)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  sub class axioms for subclass  = "
            + ontology.getSubClassAxiomsForSubClass(owlClass).size());
    for (OWLSubClassOfAxiom axiom : ontology
        .getSubClassAxiomsForSubClass(owlClass)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  sub class axioms for superclass  = "
            + ontology.getSubClassAxiomsForSuperClass(owlClass).size());
    for (OWLSubClassOfAxiom axiom : ontology
        .getSubClassAxiomsForSuperClass(owlClass)) {
      Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        indent + "  annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(owlClass.getIRI()));
    // for (OWLAnnotationAssertionAxiom axiom : ontology
    // .getAnnotationAssertionAxioms(owlClass.getIRI())) {
    // OWLAnnotation annotation = axiom.getAnnotation();
    // Logger.getLogger(OwlUtility.class).debug(indent + "    axiom = " +
    // axiom);
    // Logger.getLogger(OwlUtility.class).debug(
    // indent + "      property = " + annotation.getProperty());
    // Logger.getLogger(OwlUtility.class).debug(
    // indent + "      value = " + annotation.getValue());
    // if (annotation.getValue() instanceof OWLLiteral) {
    // Logger.getLogger(OwlUtility.class).debug(
    // indent + "        literal = "
    // + ((OWLLiteral) annotation.getValue()).getLiteral());
    // Logger.getLogger(OwlUtility.class).debug(
    // indent + "        lang = "
    // + ((OWLLiteral) annotation.getValue()).getLang());
    // }
    // }
  }

  /**
   * Log annotation property.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  public static void logAnnotationProperty(OWLAnnotationProperty prop,
    OWLOntology ontology) {
    Logger.getLogger(OwlUtility.class).debug("  annotation property = " + prop);
    Logger.getLogger(OwlUtility.class).debug("    IRI = " + prop.getIRI());
    Logger.getLogger(OwlUtility.class).debug(
        "    signature = " + prop.getSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    builtIn = " + prop.isBuiltIn());
    Logger.getLogger(OwlUtility.class).debug(
        "    entity type = " + prop.getEntityType());

    // Things connected to the property

    // Only works in OWLAPI 4
    // Logger.getLogger(OwlUtility.class).debug(
    // "    annotation properties in signature = "
    // + prop.getAnnotationPropertiesInSignature().size());
    // for (OWLAnnotationProperty aprop : prop
    // .getAnnotationPropertiesInSignature()) {
    // Logger.getLogger(OwlUtility.class).debug("      annotation = " + aprop);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      class = " + owlClass);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class)
          .debug("      data property = " + dprop);
    }

    Logger.getLogger(OwlUtility.class)
        .debug(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      datatype = " + dtype);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(OwlUtility.class).debug("      expr = " + expr);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      property = " + oprop);
    }

    // Things connected to ontology

    Logger.getLogger(OwlUtility.class).debug(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(prop.getIRI()));
    // for (OWLAnnotationAssertionAxiom axiom : ontology
    // .getAnnotationAssertionAxioms(prop.getIRI())) {
    // Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        "    annotation domain axioms = "
            + ontology.getAnnotationPropertyDomainAxioms(prop));
    for (OWLAnnotationPropertyDomainAxiom axiom : ontology
        .getAnnotationPropertyDomainAxioms(prop)) {
      Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    annotation range axioms = "
            + ontology.getAnnotationPropertyRangeAxioms(prop));
    for (OWLAnnotationPropertyRangeAxiom axiom : ontology
        .getAnnotationPropertyRangeAxioms(prop)) {
      Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    }

  }

  /**
   * Log object property.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  public static void logObjectProperty(OWLObjectProperty prop,
    OWLOntology ontology) {
    Logger.getLogger(OwlUtility.class).debug("  object property = " + prop);
    Logger.getLogger(OwlUtility.class).debug("    IRI = " + prop.getIRI());
    Logger.getLogger(OwlUtility.class).debug(
        "    inverse property = " + prop.getInverseProperty());
    Logger.getLogger(OwlUtility.class).debug(
        "    named property = " + prop.getNamedProperty());
    Logger.getLogger(OwlUtility.class).debug(
        "    signature = " + prop.getSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    simplified = " + prop.getSimplified());
    Logger.getLogger(OwlUtility.class).debug(
        "    anonymous = " + prop.isAnonymous());
    Logger.getLogger(OwlUtility.class).debug(
        "    builtIn = " + prop.isBuiltIn());
    Logger.getLogger(OwlUtility.class).debug(
        "    entity type = " + prop.getEntityType());

    // Only works in OWLAPI 4
    // Logger.getLogger(OwlUtility.class).debug(
    // "    annotation properties in signature = "
    // + prop.getAnnotationPropertiesInSignature().size());
    // for (OWLAnnotationProperty aprop : prop
    // .getAnnotationPropertiesInSignature()) {
    // Logger.getLogger(OwlUtility.class).debug("      annotation = " + aprop);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      class = " + owlClass);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class)
          .debug("      data property = " + dprop);
    }

    Logger.getLogger(OwlUtility.class)
        .debug(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      datatype = " + dtype);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(OwlUtility.class).debug("      expr = " + expr);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      property = " + oprop);
    }

    // loaded from ontology

    Logger.getLogger(OwlUtility.class).debug(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(prop.getIRI()));
    // for (OWLAnnotationAssertionAxiom axiom : ontology
    // .getAnnotationAssertionAxioms(prop.getIRI())) {
    // Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        "    sub properties for sub property = "
            + ontology.getObjectSubPropertyAxiomsForSubProperty(prop).size());
    for (OWLSubObjectPropertyOfAxiom axiom : ontology
        .getObjectSubPropertyAxiomsForSubProperty(prop)) {
      Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    sub properties for super property = "
            + ontology.getObjectSubPropertyAxiomsForSuperProperty(prop).size());
    for (OWLSubObjectPropertyOfAxiom axiom : ontology
        .getObjectSubPropertyAxiomsForSuperProperty(prop)) {
      Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    }

  }

  /**
   * Log data property.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  public static void logDataProperty(OWLDataProperty prop, OWLOntology ontology) {
    Logger.getLogger(OwlUtility.class).debug("  data property = " + prop);
    Logger.getLogger(OwlUtility.class).debug("    IRI = " + prop.getIRI());
    Logger.getLogger(OwlUtility.class).debug(
        "    signature = " + prop.getSignature());

    Logger.getLogger(OwlUtility.class).debug(
        "    anonymous = " + prop.isAnonymous());
    Logger.getLogger(OwlUtility.class).debug(
        "    builtIn = " + prop.isBuiltIn());
    Logger.getLogger(OwlUtility.class).debug(
        "    entity type = " + prop.getEntityType());

    // Only works in OWLAPI 4
    // Logger.getLogger(OwlUtility.class).debug(
    // "    annotation properties in signature = "
    // + prop.getAnnotationPropertiesInSignature().size());
    // for (OWLAnnotationProperty aprop : prop
    // .getAnnotationPropertiesInSignature()) {
    // Logger.getLogger(OwlUtility.class).debug("      annotation = " + aprop);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      class = " + owlClass);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class)
          .debug("      data property = " + dprop);
    }

    Logger.getLogger(OwlUtility.class)
        .debug(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      datatype = " + dtype);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(OwlUtility.class).debug("      expr = " + expr);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      property = " + oprop);
    }

    // loaded from ontology

    Logger.getLogger(OwlUtility.class).debug(
        "    annotation assertion axioms = "
            + ontology.getAnnotationAssertionAxioms(prop.getIRI()));
    // for (OWLAnnotationAssertionAxiom axiom : ontology
    // .getAnnotationAssertionAxioms(prop.getIRI())) {
    // Logger.getLogger(OwlUtility.class).debug("      axiom = " + axiom);
    // }

  }

  /**
   * Log property chain.
   *
   * @param prop the prop
   * @param ontology the ontology
   */
  public static void logPropertyChain(OWLSubPropertyChainOfAxiom prop,
    OWLOntology ontology) {
    Logger.getLogger(OwlUtility.class).info("  property chain= " + prop);
    Logger.getLogger(OwlUtility.class).debug(
        "    chain = " + prop.getPropertyChain());
    Logger.getLogger(OwlUtility.class).debug(
        "    super property = " + prop.getSuperProperty());
    Logger.getLogger(OwlUtility.class).debug(
        "    signature = " + prop.getSignature());
    Logger.getLogger(OwlUtility.class).debug(
        "    entity type = " + prop.getAxiomType());

    // Only works in OWLAPI 4
    // Logger.getLogger(OwlUtility.class).debug(
    // "    annotation properties in signature = "
    // + prop.getAnnotationPropertiesInSignature().size());
    // for (OWLAnnotationProperty aprop : prop
    // .getAnnotationPropertiesInSignature()) {
    // Logger.getLogger(OwlUtility.class).debug("      annotation = " + aprop);
    // }

    Logger.getLogger(OwlUtility.class).debug(
        "    classes in signature = " + prop.getClassesInSignature().size());
    for (OWLClass owlClass : prop.getClassesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      class = " + owlClass);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    data properties in signature = "
            + prop.getDataPropertiesInSignature().size());
    for (OWLDataProperty dprop : prop.getDataPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class)
          .debug("      data property = " + dprop);
    }

    Logger.getLogger(OwlUtility.class)
        .debug(
            "    datatypes in signature = "
                + prop.getDatatypesInSignature().size());
    for (OWLDatatype dtype : prop.getDatatypesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      datatype = " + dtype);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    nested class expressions = "
            + prop.getNestedClassExpressions().size());
    for (OWLClassExpression expr : prop.getNestedClassExpressions()) {
      Logger.getLogger(OwlUtility.class).debug("      expr = " + expr);
    }

    Logger.getLogger(OwlUtility.class).debug(
        "    object properties in signature = "
            + prop.getObjectPropertiesInSignature().size());
    for (OWLObjectProperty oprop : prop.getObjectPropertiesInSignature()) {
      Logger.getLogger(OwlUtility.class).debug("      property = " + oprop);
    }

    // loaded from ontology - n/a

  }
}
