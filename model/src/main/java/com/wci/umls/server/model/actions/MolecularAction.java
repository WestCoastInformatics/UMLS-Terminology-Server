package com.wci.umls.server.model.actions;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasTerminologyId;

/**
 * The Interface MolecularAction.
 */
public interface MolecularAction extends HasTerminologyId, HasLastModified {
  
  /**
   * Gets the type.
   *
   * @return the type
   */
  public String getType();
  
  /**
   * Sets the type.
   *
   * @param type the new type
   */
  public void setType(String type);
  
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
}
