/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;

/**
 * A visitor that returns ObjectSomeValuesFrom restrictions
 */
public class RestrictionVisitor extends OWLClassExpressionVisitorAdapter {

  /** The processed classes. */
  private final Set<OWLClass> processedClasses;

  /** The restricted properties. */
  private final Set<OWLObjectSomeValuesFrom> restrictions;

  /** The onts. */
  private final Set<OWLOntology> ontologies;

  /**
   * Instantiates a {@link RestrictionVisitor} from the specified parameters.
   *
   * @param ontologies the ontologies
   */
  public RestrictionVisitor(Set<OWLOntology> ontologies) {
    restrictions = new HashSet<>();
    processedClasses = new HashSet<OWLClass>();
    this.ontologies = ontologies;
  }

  /**
   * Returns the restrictions
   *
   * @return the restrictions
   */
  public Set<OWLObjectSomeValuesFrom> getRestrictions() {
    return restrictions;
  }

  /* see superclass */
  @Override
  public void visit(OWLClass desc) {
    if (!processedClasses.contains(desc)) {
      // If we are processing inherited restrictions then we
      // recursively visit named supers. Note that we need to keep
      // track of the classes that we have processed so that we don't
      // get caught out by cycles in the taxonomy
      processedClasses.add(desc);
      for (final OWLOntology ontology : ontologies) {
        for (final OWLSubClassOfAxiom ax : ontology
            .getSubClassAxiomsForSubClass(desc)) {
          ax.getSuperClass().accept(this);
        }
      }
    }
  }

  /* see superclass */
  @Override
  public void visit(OWLObjectSomeValuesFrom desc) {
    // This method gets called when a class expression is an existential
    // (someValuesFrom) restriction and it asks us to visit it
    restrictions.add(desc);
  }
}