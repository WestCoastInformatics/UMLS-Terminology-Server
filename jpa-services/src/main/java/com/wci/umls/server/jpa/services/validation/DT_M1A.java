/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.ContentService;

/**
 * Validates missing sty (matrixinit).
 *
 */
public class DT_M1A extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /**
   * Not implemented. This is merely a place-holder to be backwards-compatable
   * with MEME3.
   *
   * @param project the project
   * @param service the service
   * @param source the source {@link Concept}
   * @return <code>true</code> if there is a violation, <code>false</code>
   *         otherwise
   */
  public ValidationResult validate(Project project, ContentService service,
    Concept source) {
    // unimplemented
    ValidationResult result = new ValidationResultJpa();
    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "DT_M1A";
  }

}
