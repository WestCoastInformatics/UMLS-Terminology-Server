/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.services.ContentService;

/**
 * Validates those {@link Concept}s lacking an approved, releasable
 * {@link SemanticTypeComponent}.
 *
 */
public class DT_M1 extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /**
   * Validate.
   *
   * @param project the project
   * @param source the source
   * @param service the service
   * @return the validation result
   */
  public ValidationResult validate(Project project,
    ContentService service, Concept source) {
    ValidationResult result = new ValidationResultJpa();

    //
    // Get semantic types
    //
    List<SemanticTypeComponent> stys = source.getSemanticTypes();

    //
    // Violation if there are none
    //
    if (stys.isEmpty()) {
      result.getErrors().add(getName() + ": Concept contains no semantic type components");
      return result;
    }

    //
    // Check that there is a publishable, approved STY
    //
    boolean hasPublishableSty = false;
    for (SemanticTypeComponent sty : stys) {
      if (sty.isPublishable()) {
        hasPublishableSty = true;
      }
    }

    if (!hasPublishableSty) {
      result.getErrors()
          .add(getName() + ": Concept contains no publishable semantic type components");
      return result;
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "DT_M1";
  }

}
