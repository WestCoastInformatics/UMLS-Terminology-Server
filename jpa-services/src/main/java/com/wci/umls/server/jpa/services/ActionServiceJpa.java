/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuesMap;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.actions.AtomicActionJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.services.ActionService;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * Implementation of {@link ActionService}.
 */
public class ActionServiceJpa extends HistoryServiceJpa
    implements ActionService {

  /** The config properties. */
  protected static Properties config = null;

  /** The listeners enabled. */
  protected boolean listenersEnabled = true;

  /** The listener. */
  protected static List<WorkflowListener> listeners = null;

  static {
    listeners = new ArrayList<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "workflow.listener.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        WorkflowListener handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowListener.class);
        listeners.add(handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      listeners = null;
    }
  }

  /**
   * Instantiates an empty {@link ActionServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ActionServiceJpa() throws Exception {
    super();

    if (listeners == null) {
      throw new Exception(
          "Listeners did not properly initialize, serious error.");
    }

  }

  /* see superclass */
  @Override
  public void enableListeners() {
    listenersEnabled = true;
  }

  /* see superclass */
  @Override
  public void disableListeners() {
    listenersEnabled = false;
  }

  @Override
  public String configureActionService(Project project) throws Exception {
    
    return null;
  }

  @Override
  public void clear(String sessionToken) {
    

  }

  @Override
  public float getProgress(String sessionToken) throws Exception {
    
    return 0;
  }

  @Override
  public void cancel(String sessionToken) throws Exception {
    

  }

  @Override
  public void prepareToClassify(String sessionToken) throws Exception {
    

  }

  @Override
  public void classify(String sessionToken) throws Exception {
    

  }

  @Override
  public void incrementalClassify(String sessionToken) throws Exception {
    

  }

  @Override
  public KeyValuesMap getClassificationEquivalents(String sessionToken)
    throws Exception {
    
    return null;
  }

  @Override
  public RelationshipList getOldInferredRelationships(String sessionToken)
    throws Exception {
    
    return null;
  }

  @Override
  public RelationshipList getNewInferredRelationships(String sessionToken)
    throws Exception {
    
    return null;
  }

  @Override
  public void addNewInferredRelationships(String sessionToken)
    throws Exception {
    

  }

  @Override
  public void retireOldInferredRelationships(String sessionToken)
    throws Exception {
    

  }

  @Override
  // TODO Half-written code, left as placeholder
  public MolecularAction resolveAction(String actionType, Concept oldConcept,
    Concept newConcept) throws Exception {

    // check pre-requisites
    if (oldConcept == null && newConcept == null) {
      throw new Exception(
          "Cannot compute atomic actions: two null concepts passed in");
    }
    if (oldConcept != null && newConcept != null) {
      if (!oldConcept.getTerminology().equals(newConcept.getTerminology())) {
        throw new Exception(
            "Cannot compute atomic actions: concepts have different terminologies");
      }
      if (!oldConcept.getTerminologyId().equals(newConcept.getTerminologyId())) {
        throw new Exception(
            "Cannot compute atomic actions: concepts have different terminology ids");
      }
    }

    // extract the basic fields
    String terminology = oldConcept == null ? newConcept.getTerminology()
        : oldConcept.getTerminology();
    String version =
        oldConcept == null ? newConcept.getVersion() : oldConcept.getVersion();
    String terminologyId = oldConcept == null ? newConcept.getTerminologyId()
        : oldConcept.getTerminologyId();

    MolecularAction molecularAction = new MolecularActionJpa();
    molecularAction.setTerminology(terminology);
    molecularAction.setVersion(version);
    molecularAction.setTerminologyId(terminologyId);
    molecularAction.setLastModified(new Date());
    molecularAction.setLastModifiedBy(getLastModifiedBy());
    molecularAction.setTimestamp(new Date());
    molecularAction.setType(actionType);

    // cycle over getter fields
    for (Method m : ConceptJpa.class.getMethods()) {
      
      // only use get/is methods with no arguments to determine changes
      // NOTE: Motivated by Concept.getLabelForName
      if (m.getName().startsWith("get") && m.getParameterCount() == 0
          || m.getName().startsWith("is") && m.getParameterCount() == 0) {
        Object oldValue = oldConcept == null ? null : m.invoke(oldConcept);
        Object newValue = newConcept == null ? null : m.invoke(newConcept);

        // if change from null or change in value
        if (oldValue == null && newValue != null
            || oldValue != null && !oldValue.equals(newValue)) {

          AtomicAction action = new AtomicActionJpa();
          action.setIdType(IdType.CONCEPT);
      
          // retrieve and set the field name (based on is vs. get)
          int fromIndex = m.getName().startsWith("get") ? 3 : 2;
          action.setField(m.getName().substring(fromIndex,fromIndex+1).toLowerCase() + m.getName().substring(fromIndex));

          // This is obviously very clumsy, and we'll need to deal with
          // collections and the like
          // Putting this in as a placeholder for now
          action.setNewValue(newValue == null ? null : newValue.toString());
          action.setOldValue(oldValue == null ? null : oldValue.toString());

          molecularAction.getAtomicActions().add(action);
        }
      }
    }
    return molecularAction;
  }
  
  @Override
  public boolean hasChangedField(MolecularAction action, String fieldName) {
    for (AtomicAction a : action.getAtomicActions()) {
      if (a.getField().equals(fieldName)) {
        return true;
      }
    }
    return false;
  }
}
