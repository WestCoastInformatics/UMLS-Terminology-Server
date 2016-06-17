package com.wci.umls.server.model.actions;

import java.util.List;

import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.HasName;
import com.wci.umls.server.helpers.HasTerminologyId;

/**
 * The Interface MolecularAction.
 */
public interface MolecularAction extends HasTerminologyId, HasLastModified,
    HasName {

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
   * Returns the referenced terminology id. This is the termionlogyId on the
   * other end of an action like inserting a relationship, merging, moving, or
   * splitting atoms.
   *
   * @return the referenced terminology id
   */
  public String getTerminologyId2();

  /**
   * Sets the referenced terminology id.
   *
   * @param referencedTerminologyId the referenced terminology id
   */
  public void setTerminologyId2(String referencedTerminologyId);

}
