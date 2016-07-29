/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for undoing a previously performed action.
 */
public class UndoMolecularAction extends AbstractMolecularAction {

  /** The molecular action id. */
  private Long molecularActionId;

  /**
   * Instantiates an empty {@link UndoMolecularAction}.
   *
   * @throws Exception the exception
   */
  public UndoMolecularAction() throws Exception {
    super();
    // n/a
  }

  /**
   * Returns the molecular action id.
   *
   * @return the molecular action id
   */
  public Long getMolecularActionId() {
    return molecularActionId;
  }

  /**
   * Sets the molecular action id.
   *
   * @param molecularActionId the molecular action id
   */
  public void setMolecularActionId(Long molecularActionId) {
    this.molecularActionId = molecularActionId;
  }

  /**
   * Check preconditions.
   *
   * @return the validation result
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Check if action has already been undone
    if (getMolecularAction(molecularActionId).isUndoneFlag()) {
      throw new LocalException("Cannot undo Molecular action "
          + molecularActionId + " - it has already been undone.");
    }

    // Check preconditions
    validationResult.merge(super.checkPreconditions());

    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Call up the molecular Action we're undoing
    final MolecularAction undoMolecularAction =
        getMolecularAction(molecularActionId);

    // Perform the opposite action for each of the molecular action's atomic
    // actions
    final List<AtomicAction> atomicActions =
        undoMolecularAction.getAtomicActions();

    // Sort atomic actions, so they can be un-done in reverse order
    final List<AtomicAction> sortedAtomicActions =
        new ArrayList<AtomicAction>();
    atomicActions.stream()
        .sorted((a1, a2) -> Long.compare(a2.getId(), a1.getId())).forEach(a -> {
          sortedAtomicActions.add(a);
        });

    for (AtomicAction a : sortedAtomicActions) {
      //
      // Undo add
      //
      if (a.getOldValue() == null && a.getField().equals("id")) {
        // Get the object that was added, and make sure it still exists
        Object referencedObject =
            getObject(a.getObjectId(), Class.forName(a.getClassName()));

        if (referencedObject == null) {
          throw new Exception("Undo add failed: cannot find object with class "
              + a.getClassName() + " and an id of " + a.getObjectId());
        }

        // For Atoms, remove the Atom from the containing concept, and remove
        // the Atom.
        if (a.getClassName().equals(AtomJpa.class.getName())) {
          getConcept().getAtoms().remove(referencedObject);
          updateHasLastModified(getConcept());
          removeObject(referencedObject, Object.class);
        }
        
        // For Semantic Types, remove the Semantic Type from the containing
        // concept, and remove the Semantic Type.
        else if (a.getClassName()
            .equals(SemanticTypeComponentJpa.class.getName())) {
          getConcept().getSemanticTypes().remove(referencedObject);
          updateHasLastModified(getConcept());
          removeObject(referencedObject, Object.class);
        }
        
        // For Attributes, remove the Attribute from the containing
        // concept, and remove the Attribute.
        else if (a.getClassName().equals(AttributeJpa.class.getName())) {
          getConcept().getAttributes().remove(referencedObject);
          updateHasLastModified(getConcept());
          removeObject(referencedObject, Object.class);
        }
        
        // For ConceptRelationships, remove the relationship
        else if (a.getClassName()
            .equals(ConceptRelationshipJpa.class.getName())) {
          removeObject(referencedObject, Object.class);
        }
        // For Concepts, we should be able to just remove it (nothing Should be
        // left on it, if the undo functionality is working correctly.
        else if (a.getClassName().equals(ConceptJpa.class.getName())) {
          removeObject(referencedObject, Object.class);
        } else {
          throw new LocalException("Undoing an add for " + a.getClassName()
              + " is unhandled. Update UndoMolecularAction.java");
        }
      }

      //
      // Undo remove
      //
      else if (a.getNewValue() == null && a.getField().equals("id")) {
        System.out.println("TESTTEST - we're undoing a remove");
      }

      //
      // Undo move
      //
      else if (a.getField().equals("concept")
          && a.getIdType().equals(IdType.ATOM)) {
        Atom movedAtom = this.getAtom(a.getObjectId());

        // Ensure that the listed atom exists in the concept it claims it is in,
        // and doesn't already exist in the concept it would be moved into
        if (!getConcept2().getAtoms().contains(movedAtom)) {
          throw new Exception("Undo move failed: Atom " + movedAtom
              + " not in Concept " + getConcept2());
        }
        if (getConcept().getAtoms().contains(movedAtom)) {
          throw new Exception("Undo move failed: Atom " + movedAtom
              + " already in Concept " + getConcept());
        }

        // The molecular action's Concept is always where the atom was moved
        // from, and Concept2 is where it was moved to.
        getConcept().getAtoms().add(movedAtom);
        updateHasLastModified(getConcept());

        getConcept2().getAtoms().remove(movedAtom);
        updateHasLastModified(getConcept2());

        updateHasLastModified(movedAtom);
      }

      //
      // Undo a field change
      //
      else if (a.getOldValue() != null && a.getNewValue() != null) {
        // Get the object that was modified, and make sure it still exists
        HasLastModified referencedObject =
            (HasLastModified) getObject(a.getObjectId(),
                Class.forName(a.getClassName()));

        // If the referenced object is one of the concepts associated with this
        // molecular action, use that concept instead (later actions may call
        // updateHasLastModified again, and it can override these changes)
        if (getConcept() != null
            && referencedObject.getClass().getName()
                .equals(ConceptJpa.class.getName())
            && referencedObject.getId() == getConcept().getId()) {
          referencedObject = getConcept();
        }
        if (getConcept2() != null
            && referencedObject.getClass().getName()
                .equals(ConceptJpa.class.getName())
            && referencedObject.getId() == getConcept2().getId()) {
          referencedObject = getConcept2();
        }

        if (referencedObject == null) {
          throw new Exception("Cannot find object with class "
              + a.getClassName() + " and an id of " + a.getObjectId());
        }

        // Get the get/set methods for the field
        Method accessorMethod = null;
        Method setMethod = null;

        for (Method m : referencedObject.getClass().getMethods()) {
          if (m.getName().toUpperCase().contains(a.getField().toUpperCase())) {
            if (m.getName().contains("set")) {
              setMethod = m;
            } else if (m.getName().contains("get")
                || m.getName().contains("is")) {
              accessorMethod = m;
            }
          }
        }

        if (accessorMethod == null || setMethod == null) {
          throw new Exception("Cannot find get/set methods for field "
              + a.getField() + " in class " + a.getClassName());
        }

        // Check to make sure the field is still in the state it was set to in
        // the action
        if (!accessorMethod.invoke(referencedObject).toString()
            .equals(a.getNewValue().toString())) {
          throw new Exception("Error: field " + a.getField() + " in "
              + referencedObject + " no longer has value: " + a.getNewValue());
        }

        // If all is well, set the field back to the previous value
        Object setObject = null;

        if (accessorMethod.invoke(referencedObject).getClass().getName().equals(String.class.getName())){
          setObject = a.getOldValue();
        }
        else if (accessorMethod.invoke(referencedObject).getClass().getName().equals(Long.class.getName())){
          setObject = Long.parseLong(a.getOldValue());
        }
        else if (accessorMethod.invoke(referencedObject).getClass().getName().equals(WorkflowStatus.class.getName())){
          setObject = WorkflowStatus.valueOf(a.getOldValue());
        }
        else{
          throw new LocalException("Undoing modifications for field type "
              + accessorMethod.invoke(referencedObject).getClass().getName()
              + " is unhandled.  Update UndoMolecularAction.java");
        }

        if (setObject == null) {
          throw new Exception(
              "Unable to successfully construct value for assigning to field "
                  + accessorMethod.invoke(referencedObject).getClass()
                      .getSimpleName());
        }
        setMethod.invoke(referencedObject, setObject);
        updateHasLastModified(referencedObject);

      }
    }

    // Set the molecular action undone flag
    undoMolecularAction.setUndoneFlag(true);
    updateObject(undoMolecularAction);

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), molecularActionId,
        getName() + " molecular action " + molecularActionId);

  }

}
