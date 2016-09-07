/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.actions;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;
import com.wci.umls.server.helpers.HasTerminology;

/**
 * Generically represents a molecular action
 */
public interface MolecularAction
    extends HasTerminology, HasLastModified, HasName {

  /**
   * Gets the atomic actions.
   *
   * @return the atomic actions
   */
  public List<AtomicAction> getAtomicActions();

  /**
   * Sets the atomic actions.
   *
   * @param atomicActions the new atomic actions
   */
  public void setAtomicActions(List<AtomicAction> atomicActions);

  /**
   * Checks if is macro action.
   *
   * @return true, if is macro action
   */
  public boolean isMacroAction();

  /**
   * Sets the macro action.
   *
   * @param macroAction the new macro action
   */
  public void setMacroAction(boolean macroAction);

  /**
   * Returns the component id. This is the id of the component the action is performed upon 
   *
   * @return the component id
   */
  public Long getComponentId();

  /**
   * Sets the component id.
   *
   * @param componentId the component id
   */
  public void setComponentId(Long componentId);  
  
  /**
   * Returns the referenced component id. This is the component Id on the
   * other end of an action like inserting a relationship, merging, moving, or
   * splitting atoms.
   *
   * @return the referenced component id
   */
  public Long getComponentId2();

  /**
   * Sets the referenced component id.
   *
   * @param referencedComponentId2 the referenced component id
   */
  public void setComponentId2(Long referencedComponentId2);

  /**
   * Returns the batch id. Represents a collection of connected molecular
   * actions.
   *
   * @return the batch id
   */
  public String getBatchId();

  /**
   * Sets the batch id.
   *
   * @param batchId the batch id
   */
  public void setBatchId(String batchId);

  /**
   * Returns the activity id.
   *
   * @return the activity id
   */
  public String getActivityId();

  /**
   * Sets the activity id.
   *
   * @param activityId the activity id
   */
  public void setActivityId(String activityId);

  /**
   * Returns the work id. Represents a collection of connected batches of
   * molecular actions.
   *
   * @return the work id
   */
  public String getWorkId();

  /**
   * Sets the work id.
   *
   * @param workId the work id
   */
  public void setWorkId(String workId);

  /**
   * Returns the undone flag.
   *
   * @return the undone flag
   */
  public boolean isUndoneFlag();

  /**
   * Sets the uundone flag.
   *
   * @param undoneFlag the uundone flag
   */
  public void setUndoneFlag(boolean undoneFlag);

}
