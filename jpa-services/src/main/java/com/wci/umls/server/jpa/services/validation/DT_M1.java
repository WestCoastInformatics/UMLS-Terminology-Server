/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Validates those {@link Concept}s lacking a publishable
 * {@link SemanticTypeComponent}.
 *
 */
public class DT_M1 extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Concept source) {
    ValidationResult result = new ValidationResultJpa();

    if (source==null){
      return result;
    }
    
    //
    // Get semantic types
    //
    List<SemanticTypeComponent> stys = source.getSemanticTypes();

    //
    // Violation if there are none
    //
    if (stys.isEmpty()) {
      result.getErrors()
          .add(getName() + ": Concept contains no semantic type components");
      return result;
    }

    //
    // Check that there is a publishable, approved STY
    //
    boolean hasPublishableSty = false;
    for (SemanticTypeComponent sty : stys) {
      if (sty.isPublishable()) {
        hasPublishableSty = true;
        break;
      }
    }

    if (!hasPublishableSty) {
      result.getErrors().add(getName()
          + ": Concept contains no publishable semantic type components");
      return result;
    }

    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
