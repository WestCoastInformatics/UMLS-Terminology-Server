/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.services.ValidationService;
import com.wci.umls.server.services.handlers.ValidationCheck;

/**
 * Implementation of {@link ValidationService} that redirects to
 * terminology-specific implementations.
 */
public class ValidationServiceJpa extends RootServiceJpa implements
    ValidationService {

  /** The config properties. */
  protected static Properties config = null;

  /** The validation handlers. */
  protected static Map<String, ValidationCheck> validationHandlersMap = null;
  static {
    validationHandlersMap = new HashMap<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "validation.service.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        ValidationCheck handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ValidationCheck.class);
        validationHandlersMap.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      validationHandlersMap = null;
    }
  }

  /**
   * Instantiates an empty {@link ValidationServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ValidationServiceJpa() throws Exception {
    super();

    if (validationHandlersMap == null) {
      throw new Exception(
          "Validation handlers did not properly initialize, serious error.");
    }

  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    // TODO Auto-generated method stub

  }

  /* see superclass */
  @Override
  public ValidationResult validateConcept(Concept concept) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(concept));
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateAtom(Atom atom) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(atom));
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateDescriptor(Descriptor descriptor) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(descriptor));
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateCode(Code code) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(code));
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateMerge(Concept concept1, Concept concept2) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validateMerge(concept1,
          concept2));
    }
    return result;
  }

}
