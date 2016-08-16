/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;

/**
 * Validates those {@link Concept}s that contain at least one demoted
 * {@link Relationship}.
 *
 */
public class DT_I3 extends AbstractValidationCheck {

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
    // Look for demotions.
    //
    for (ConceptRelationship rel : source.getRelationships()) {
      if (rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        result.getErrors()
            .add(getName() + ": Concept contains at least one demoted relationship");
        return result;
      }
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "DT_I3";
  }

}
