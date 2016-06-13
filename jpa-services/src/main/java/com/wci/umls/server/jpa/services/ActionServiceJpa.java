/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuesMap;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.actions.AtomicActionJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.services.ActionService;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * Implementation of {@link ActionService}.
 */
public class ActionServiceJpa extends HistoryServiceJpa implements
    ActionService {

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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clear(String sessionToken) {
    // TODO Auto-generated method stub

  }

  @Override
  public float getProgress(String sessionToken) throws Exception {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void cancel(String sessionToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void prepareToClassify(String sessionToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void classify(String sessionToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void incrementalClassify(String sessionToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public KeyValuesMap getClassificationEquivalents(String sessionToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RelationshipList getOldInferredRelationships(String sessionToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RelationshipList getNewInferredRelationships(String sessionToken)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addNewInferredRelationships(String sessionToken) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void retireOldInferredRelationships(String sessionToken)
    throws Exception {
    // TODO Auto-generated method stub

  }
  
  @Override
  public MolecularAction addMolecularAction(MolecularAction action) throws Exception {
    Logger.getLogger(getClass()).debug("Action Service - add molecular action " + action);
    return addObject(action);
  }

  @Override
  public void updateMolecularAction(MolecularAction action) throws Exception {
    Logger.getLogger(getClass()).debug("Action Service - update molecular action " + action);
    updateObject(action);
  }

  @Override
  public void removeMolecularAction(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Action Service - remove molecular action " + id);
    MolecularActionJpa action = getObject(id, MolecularActionJpa.class);
    this.removeObject(action, MolecularActionJpa.class);
  }
  

  @Override
  public AtomicAction addAtomicAction(AtomicAction action) throws Exception {
    Logger.getLogger(getClass()).debug("Action Service - add atomic action " + action);
    return addObject(action);
  }

  @Override
  public void updateAtomicAction(AtomicAction action) throws Exception {
    Logger.getLogger(getClass()).debug("Action Service - update atomic action " + action);
    updateObject(action);
  }

  @Override
  public void removeAtomicAction(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Action Service - remove atomic action " + id);
    AtomicActionJpa action = getObject(id, AtomicActionJpa.class);
    this.removeObject(action, AtomicActionJpa.class);
  }

}
