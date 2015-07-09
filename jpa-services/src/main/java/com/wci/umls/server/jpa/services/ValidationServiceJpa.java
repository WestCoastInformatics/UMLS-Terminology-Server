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



  @Override
  public void refreshCaches() throws Exception {
    // TODO Auto-generated method stub
    
  }



  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ValidationService#validateConcept(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public ValidationResult validateConcept(Concept concept) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(concept));
    }
    return result;
  }



  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ValidationService#validateAtom(com.wci.umls.server.model.content.Atom)
   */
  @Override
  public ValidationResult validateAtom(Atom atom) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(atom));
    }
    return result;
  }



  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ValidationService#validateDescriptor(com.wci.umls.server.model.content.Descriptor)
   */
  @Override
  public ValidationResult validateDescriptor(Descriptor descriptor) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(descriptor));
    }
    return result;
  }



  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ValidationService#validateCode(com.wci.umls.server.model.content.Code)
   */
  @Override
  public ValidationResult validateCode(Code code) {
    ValidationResult result = new ValidationResultJpa();
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validate(code));
    }
    return result;
  }



  /* (non-Javadoc)
   * @see com.wci.umls.server.services.ValidationService#validateMerge(com.wci.umls.server.model.content.Concept, com.wci.umls.server.model.content.Concept)
   */
  @Override
  public ValidationResult validateMerge(Concept concept1, Concept concept2) {
    ValidationResult result = new ValidationResultJpa(); 
    for (String key : validationHandlersMap.keySet()) {
      result.merge(validationHandlersMap.get(key).validateMerge(concept1, concept2));
    }
    return result;
  }



}
