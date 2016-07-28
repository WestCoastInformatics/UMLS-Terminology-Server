/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.actions.AtomicAction;
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

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult validationResult = new ValidationResultJpa();
    // Perform action specific validation - n/a

    // Metadata referential integrity checking

    // Check if action has already been undone
    if (getMolecularAction().isUndoneFlag()) {
      throw new LocalException("Cannot undo Molecular action "
          + molecularActionId + " - it has already been undone.");
    }

    // Check preconditions
    validationResult.merge(super.checkPreconditions());

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    //
    // Perform the action (contentService will create atomic actions for CRUD
    // operations)
    //

    // Perform the opposite action for each of the molecular action's atomic
    // actions
    final List<AtomicAction> atomicActions =
        getMolecularAction(molecularActionId).getAtomicActions();

    // Sort atomic actions, so they can be un-done in reverse order
    final List<AtomicAction> sortedAtomicActions =
        new ArrayList<AtomicAction>();
    atomicActions.stream()
        .sorted((a1, a2) -> Long.compare(a2.getId(), a1.getId())).forEach(a -> {
          sortedAtomicActions.add(a);
        });

    for (AtomicAction a : sortedAtomicActions) {
      // Undo creation
      if (a.getOldValue() == null && a.getField().equals("id")) {
        // Get the object that was added, and make sure it still exists
        Object referencedObject =
            getObject(a.getObjectId(), Class.forName(a.getClassName()));

        Class clazz = Class.forName(a.getClassName());
        clazz.cast(referencedObject);
        
        if (referencedObject == null) {
          throw new Exception("Cannot find object with class " + a.getClassName() + " and an id of " + a.getObjectId());
        }    
        
        System.out.println("TESTTEST - stop here");
        //Remove the object
        //TODO - figure out how to get a Class<T> out of an object
        removeObject(referencedObject, Object.class);
        updateObject(referencedObject);
        System.out.println("TESTTEST - stop here");
        
      }

      // Undo deletion
      else if (a.getNewValue() == null && a.getField().equals("id")) {
        System.out.println("TESTTEST - we're undoing a deletion");
      }

      // Undo a field modification
      else if (a.getOldValue() != null && a.getNewValue() != null) {
        // Get the object that was modified, and make sure it still exists
        Object referencedObject =
            getObject(a.getObjectId(), Class.forName(a.getClassName()));

        if (referencedObject == null) {
          throw new Exception("Cannot find object with class " + a.getClassName() + " and an id of " + a.getObjectId());
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
          throw new Exception(
              "Error: field no longer has value: " + a.getNewValue());
        }

        // If all is well, set the field back to the previous value
        Object setObject = null;

        switch (accessorMethod.invoke(referencedObject).getClass()
            .getSimpleName()) {
          case "String":
            setObject = a.getOldValue();
            break;
          case "Long":
            setObject = Long.parseLong(a.getOldValue());
            break;
          case "WorkflowStatus":
            setObject = WorkflowStatus.valueOf(a.getOldValue());
            break;
            default: throw new LocalException ("Field type " + accessorMethod.invoke(referencedObject).getClass()
                .getSimpleName() + " unhandled.  Update UndoMolecularAction.");
        }
        
        if (setObject == null){
          throw new Exception ("Unable to successfully construct value for assigning to field " + accessorMethod.invoke(referencedObject).getClass()
              .getSimpleName());
        }

        setMethod.invoke(referencedObject,
            setObject);
        updateObject(referencedObject);

      }
    }

    System.out.println("TESTTEST - Stop here");

    // update the concept(s)
    updateConcept(getConcept());
    if (getConcept2() != null) {
      updateConcept(getConcept2());
    }

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), molecularActionId,
        getName() + " molecular action + " + molecularActionId);

  }

}
