/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;

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
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.workflow.WorkflowStatus;

/**
 * A molecular action for redoing a previously undone action.
 */
public class RedoMolecularAction extends AbstractMolecularAction {

  /** The molecular action id. */
  private Long molecularActionId;

  /**
   * Instantiates an empty {@link RedoMolecularAction}.
   *
   * @throws Exception the exception
   */
  public RedoMolecularAction() throws Exception {
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

    // Check to make sure action has already been undone
    if (!getMolecularAction(molecularActionId).isUndoneFlag()) {
      throw new LocalException("Cannot redo Molecular action "
          + molecularActionId + " - it has not been undone.");
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

    // Call up the molecular Action we're redoing
    final MolecularAction redoMolecularAction =
        getMolecularAction(molecularActionId);

    // Perform the opposite action for each of the molecular action's atomic
    // actions
    final List<AtomicAction> atomicActions =
        redoMolecularAction.getAtomicActions();

    // Sort atomic actions, so they can be re-done in the same order they were
    // originally done.
    final List<AtomicAction> sortedAtomicActions =
        new ArrayList<AtomicAction>();
    atomicActions.stream()
        .sorted((a1, a2) -> Long.compare(a1.getId(), a2.getId())).forEach(a -> {
          sortedAtomicActions.add(a);
        });

    for (AtomicAction a : sortedAtomicActions) {
      //
      // Redo add
      //
      if (a.getOldValue() == null && a.getField().equals("id")) {

        // Get the class of the object we're looking for, so we can pass it into
        // the Hibernate query

        final AuditReader reader = AuditReaderFactory.get(manager);
        final AuditQuery query =
            reader.createQuery()
                // last updated revision
                .forRevisionsOfEntity(Class.forName(a.getClassName()), true,
                    true)
                .addProjection(AuditEntity.revisionNumber().max())
                // add id and owner as constraints
                .add(AuditEntity.property("id").eq(a.getObjectId()));
        final Number revision = (Number) query.getSingleResult();
        final HasLastModified returnedObject =
            (HasLastModified) reader.find(Class.forName(a.getClassName()),
                a.getClassName(), a.getObjectId(), revision, true);

        // Set the concept the component will be readded to (different depending
        // on molecular action type)
        Concept containingConcept = null;
        if (getMolecularAction(molecularActionId).getName().equals("SPLIT")) {
          containingConcept = getConcept2();
        } else {
          containingConcept = getConcept();
        }

        // Add the object back in, using the method appropriate to its type
        if (a.getClassName().equals(AtomJpa.class.getName())) {
          updateHasLastModified(returnedObject);
          containingConcept.getAtoms().add((AtomJpa) returnedObject);
          updateHasLastModified(containingConcept);
        } else if (a.getClassName().equals(AttributeJpa.class.getName())) {
          updateHasLastModified(returnedObject);
          containingConcept.getAttributes().add((AttributeJpa) returnedObject);
          updateHasLastModified(containingConcept);
        } else if (a.getClassName()
            .equals(SemanticTypeComponentJpa.class.getName())) {
          updateHasLastModified(returnedObject);
          containingConcept.getSemanticTypes()
              .add((SemanticTypeComponentJpa) returnedObject);
          updateHasLastModified(containingConcept);
        } else if (a.getClassName()
            .equals(ConceptRelationshipJpa.class.getName())) {
          updateHasLastModified(returnedObject);
        } else if (a.getClassName().equals(ConceptJpa.class.getName())) {
          // TODO - This is a total hack - figure out why Concept pulled back
          // from the dead has all of its relationships.
          ConceptJpa returnedConcept =
              new ConceptJpa((Concept) returnedObject, false);
          updateHasLastModified(returnedConcept);
          // If this concept is referenced in the molecular action, set this
          // actions concept2.
          // This wasn't set at initialization since the concept was deleted at
          // the time
          if (returnedObject.getId().equals(
              getMolecularAction(molecularActionId).getComponentId2())) {
            concept2 = returnedConcept;
          }
        } else {
          throw new LocalException("Redoing an add for " + a.getClassName()
              + " is unhandled. Update RedoMolecularAction.java");
        }
      }

      //
      // Redo remove
      //
      else if (a.getNewValue() == null && a.getField().equals("id")) {

        // Get the object that was restored, and make sure it still exists
        Object referencedObject =
            getObject(a.getObjectId(), Class.forName(a.getClassName()));

        if (referencedObject == null) {
          throw new Exception(
              "Redo remove failed: cannot find object with class "
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
        // left on it, if the redo functionality is working correctly.
        else if (a.getClassName().equals(ConceptJpa.class.getName())) {
          removeObject(referencedObject, Object.class);
        } else {
          throw new LocalException("Redoing a remove for " + a.getClassName()
              + " is unhandled. Update RedoMolecularAction.java");
        }
      }

      //
      // Redo move
      //
      else if (a.getField().equals("concept")
          && a.getIdType().equals(IdType.ATOM)) {
        Atom movedAtom = this.getAtom(a.getObjectId());

        // Set the concepts that the atoms came from and went to
        Concept originatingConcept = null;
        Concept sentToConcept = null;
        if (a.getOldValue().equals(getConcept().getId().toString())) {
          originatingConcept = getConcept();
          sentToConcept = getConcept2();
        } else {
          originatingConcept = getConcept2();
          sentToConcept = getConcept();
        }

        // Ensure that the listed atom exists in the concept it originally came
        // from,
        // and doesn't already exist in the concept it was previously moved into
        if (!originatingConcept.getAtoms().contains(movedAtom)) {
          throw new Exception("Redo move failed: Atom " + movedAtom
              + " not in Concept " + originatingConcept);
        }
        if (sentToConcept.getAtoms().contains(movedAtom)) {
          throw new Exception("Redo move failed: Atom " + movedAtom
              + " already in Concept " + sentToConcept);
        }

        // Move the Atom back to the concept it was sent to
        sentToConcept.getAtoms().add(movedAtom);
        updateHasLastModified(sentToConcept);

        // Remove the Atom from the concept it originally came from
        originatingConcept.getAtoms().remove(movedAtom);
        updateHasLastModified(originatingConcept);

        updateHasLastModified(movedAtom);
      }

      //
      // Redo a field change
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

        // Check to make sure the field is still in the state it was set to
        // after the action was undone
        if (!accessorMethod.invoke(referencedObject).toString()
            .equals(a.getOldValue().toString())) {
          throw new Exception("Error: field " + a.getField() + " in "
              + referencedObject + " no longer has value: " + a.getOldValue());
        }

        // If all is well, set the field back to the previous value
        Object setObject = null;

        if (accessorMethod.invoke(referencedObject).getClass().getName()
            .equals(String.class.getName())) {
          setObject = a.getNewValue();
        } else if (accessorMethod.invoke(referencedObject).getClass().getName()
            .equals(Long.class.getName())) {
          setObject = Long.parseLong(a.getNewValue());
        } else if (accessorMethod.invoke(referencedObject).getClass().getName()
            .equals(WorkflowStatus.class.getName())) {
          setObject = WorkflowStatus.valueOf(a.getNewValue());
        } else {
          throw new LocalException("Redoing modifications for field type "
              + accessorMethod.invoke(referencedObject).getClass().getName()
              + " is unhandled.  Update RedoMolecularAction.java");
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
    redoMolecularAction.setUndoneFlag(false);
    updateObject(redoMolecularAction);

    // log the REST call
    addLogEntry(getUserName(), getProject().getId(), molecularActionId,
        getMolecularAction().getActivityId(), getMolecularAction().getWorkId(),
        getName() + " molecular action " + molecularActionId);

  }

}
