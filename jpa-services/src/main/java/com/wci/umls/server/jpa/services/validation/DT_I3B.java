/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;

/**
 * Validates those {@link Concept}s which contain at least one demoted
 * {@link Relationship} without a matching releasable <code>MTH</code> asserted
 * {@link Relationship}.
 *
 */
public class DT_I3B extends AbstractValidationCheck {

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
    // Get demotions
    //
    List<ConceptRelationship> demotions = source.getRelationships().stream()
        .filter(r -> r.getWorkflowStatus().equals(WorkflowStatus.DEMOTION))
        .collect(Collectors.toList());

    //
    // Get non-demotion Concept relationships
    //
    List<ConceptRelationship> relationships = source.getRelationships().stream()
        .filter(r -> !r.getWorkflowStatus().equals(WorkflowStatus.DEMOTION))
        .collect(Collectors.toList());

    //
    // Assume no violation
    //
    boolean matchFound = true;

    //
    // Scan for violations
    //
    for (ConceptRelationship demotion : demotions) {
      matchFound = false;
      for (ConceptRelationship relationship : relationships) {
        if (demotion.getTo().equals(relationship.getTo())
            && relationship.getTo().isPublishable()) {
          matchFound = true;
          break;
        }
        //
        // If we did not find a matching Concept relationship, VIOLATION!
        //
        if (!matchFound) {
          result.getErrors().add(getName()
              + ": Concept contains at least one demoted relationship without a matching releasable relationship");
          return result;
        }
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
