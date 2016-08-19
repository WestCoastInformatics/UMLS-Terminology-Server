/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;

/**
 * Validates those {@link Concept}s that contain at least one releasable
 * {@link Atom} merged by the merge engine, indicated by being last modified by
 * ENG-.
 */
public class DT_I2 extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /**
   * Validate.
   *
   * @param project the project
   * @param service the service
   * @param source the source
   * @return the validation result
   */
  public ValidationResult validate(Project project, ContentService service,
    Concept source) {
    ValidationResult result = new ValidationResultJpa();

    //
    // Get all atoms
    //
    boolean violation = false;
    List<Atom> atoms = source.getAtoms();

    //
    // Find one last modified by "ENG-"
    //
    for (Atom atom : atoms) {
      if (atom.getLastModifiedBy().startsWith("ENG-") && atom.isPublishable()) {
        violation = true;
        break;
      }
    }

    if (violation) {
      result.getErrors().add(getName()
          + ": Concept contains at least one atom merged by the merge engine.");
      return result;
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "DT_I3";
  }

}
