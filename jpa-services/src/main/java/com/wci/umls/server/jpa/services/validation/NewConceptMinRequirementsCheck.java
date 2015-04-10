/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.handlers.ValidationCheck;

/**
 * A sample validation check for a new concept meeting the minimum qualifying
 * criteria.
 */
public class NewConceptMinRequirementsCheck implements ValidationCheck {

  /* (non-Javadoc)
   * @see com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties)
   */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.handlers.ValidationCheck#validate(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public ValidationResult validate(Concept c) {
    ValidationResult result = new ValidationResultJpa();

    if (!c.isObsolete()) {
      // TODO
    }
    return result;
  }

}
