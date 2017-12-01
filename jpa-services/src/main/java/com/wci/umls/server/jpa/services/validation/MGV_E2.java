/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.algo.action.MoveMolecularAction;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;

/**
 * Validates merging between two {@link Concept}s. It is connected by an
 * approved, publishable, non RQ, SY, (maybe SFO/LFO), (non-weak)
 * project-terminology concept-{@link Relationship}
 *
 */
public class MGV_E2 extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult validateAction(MolecularActionAlgorithm action) {
    ValidationResult result = new ValidationResultJpa();

    // Only run this check on merge and move actions
    if (!(action instanceof MergeMolecularAction
        || action instanceof MoveMolecularAction)) {
      return result;
    }

    final Project project = action.getProject();
    final Concept source = (action instanceof MergeMolecularAction
        ? action.getConcept2() : action.getConcept());
    final Concept target = (action instanceof MergeMolecularAction
        ? action.getConcept() : action.getConcept2());

    // Look through publishable project-terminology concept-relationships. If
    // any connects the two concepts, it violates this check.

    for (final ConceptRelationship rel : source.getRelationships()) {
      if (!(rel.isPublishable()
          && rel.getTerminology().equals(project.getTerminology()))) {
        continue;
      }
      if (rel.getTo().getId() == target.getId()) {
        result.getErrors()
            .add(getName() + ": Concepts are connected by a publishable, "
                + project.getTerminology() + " concept-relationship");
        return result;
      }
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
