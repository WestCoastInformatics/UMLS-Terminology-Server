/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.google.common.base.CaseFormat;
import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.HasLastModified;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.TrackingRecordList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;

/**
 * Abstract {@link MolecularActionAlgorithm}.
 */
public abstract class AbstractMolecularAction extends AbstractAlgorithm
    implements MolecularActionAlgorithm {

  /** The concept id. */
  private Long conceptId;

  /** The concept id 2. */
  private Long conceptId2;

  /** The concept. */
  private Concept concept;

  /** The concept2. */
  private Concept concept2;

  /** The user name. */
  private String userName;

  /** The last modified. */
  private Long lastModified;

  /** The change status flag. */
  private boolean changeStatusFlag;

  /** The override warnings. */
  private boolean overrideWarnings;

  /** The validation checks. */
  private List<String> validationChecks;

  /**
   * Instantiates an empty {@link AbstractMolecularAction}.
   *
   * @throws Exception the exception
   */
  public AbstractMolecularAction() throws Exception {
    super();
    // n/a
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /**
   * Returns the concept id.
   *
   * @return the concept id
   */
  public Long getConceptId() {
    return conceptId;
  }

  /**
   * Sets the concept id.
   *
   * @param conceptId the concept id
   */
  public void setConceptId(Long conceptId) {
    this.conceptId = conceptId;
  }

  /**
   * Returns the concept id 2.
   *
   * @return the concept id 2
   */
  public Long getConceptId2() {
    return conceptId2;
  }

  /**
   * Sets the concept id 2.
   *
   * @param conceptId2 the concept id 2
   */
  public void setConceptId2(Long conceptId2) {
    this.conceptId2 = conceptId2;
  }

  /**
   * Returns the concept.
   *
   * @return the concept
   */
  @Override
  public Concept getConcept() {
    return concept;
  }

  /* see superclass */
  @Override
  public Concept getConcept2() {
    return concept2;
  }

  /* see superclass */
  @Override
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the user name.
   *
   * @param userName the user name
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /* see superclass */
  @Override
  public Long getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the last modified
   */
  public void setLastModified(Long lastModified) {
    this.lastModified = lastModified;
  }

  /* see superclass */
  @Override
  public boolean getChangeStatusFlag() {
    return changeStatusFlag;
  }

  /* see superclass */
  @Override
  public String getName() {
    String objectName = this.getClass().getSimpleName();
    objectName = objectName.replace("MolecularAction", "");
    objectName =
        CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, objectName);
    objectName = objectName.toUpperCase();

    return objectName;
  }

  /* see superclass */
  @Override
  public void setChangeStatusFlag(boolean changeStatusFlag) {
    this.changeStatusFlag = changeStatusFlag;
  }

  /* see superclass */
  @Override
  public void setValidationChecks(List<String> validationChecks) {
    this.validationChecks = validationChecks;
  }

  /**
   * Sets the override warnings.
   *
   * @param overrideWarnings the override warnings
   */
  public void setOverrideWarnings(boolean overrideWarnings) {
    this.overrideWarnings = overrideWarnings;
  }

  /**
   * Indicates whether or not override warnings is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isOverrideWarnings() {
    return overrideWarnings;
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();
    for (final String key : getValidationHandlersMap().keySet()) {
      if (validationChecks.contains(key)) {
        result.merge(getValidationHandlersMap().get(key).validateAction(this));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public void initialize(Project project, Long conceptId, Long conceptId2,
    String userName, Long lastModified, boolean molecularActionFlag)
    throws Exception {

    setProject(project);
    this.userName = userName;
    this.lastModified = lastModified;

    // Extract concept ids and sort them
    final List<Long> conceptIdList = new ArrayList<Long>();
    conceptIdList.add(conceptId);
    if (conceptId2 != null && !(conceptId.equals(conceptId2))) {
      conceptIdList.add(conceptId2);
    }
    Collections.sort(conceptIdList);

    this.concept = null;
    this.concept2 = null;
    for (final Long i : conceptIdList) {
      Concept tempConcept = null;

      // Lock on the concept id (in Java)
      synchronized (i.toString().intern()) {

        // retrieve the concept
        tempConcept = getConcept(i);

        // Verify concept exists
        if (tempConcept == null) {
          // unlock concepts and fail
          rollback();
          throw new Exception("Concept does not exist " + i);
        }

        if (i == conceptId) {
          this.concept = new ConceptJpa(tempConcept, true);
        }
        if (i == conceptId2) {
          this.concept2 = new ConceptJpa(tempConcept, true);
        }

        // Fail if already locked - this is secondary protection
        if (isObjectLocked(tempConcept)) {
          // unlock concepts and fail
          rollback();
          throw new Exception("Fatal error: concept is locked " + i);
        }

        // lock the concept via JPA
        lockObject(tempConcept);

      }
    }

    // If the action is of a type that can affect other concepts, lock those as
    // well
    if (this instanceof MergeMolecularAction
        || this instanceof SplitMolecularAction
        || this instanceof ApproveMolecularAction) {
      lockRelatedConcepts();
    }

    setTerminology(concept.getTerminology());
    setVersion(concept.getVersion());

    // Prepare the service
    setMolecularActionFlag(molecularActionFlag);
    setLastModifiedFlag(true);
    setLastModifiedBy(userName);

    // construct the molecular action
    if (molecularActionFlag) {
      final MolecularAction molecularAction = new MolecularActionJpa();
      molecularAction.setTerminology(this.concept.getTerminology());
      molecularAction.setComponentId(this.concept.getId());
      if (conceptId2 != null) {
        molecularAction.setComponentId2(this.concept2.getId());
      }
      molecularAction.setVersion(concept.getVersion());
      molecularAction.setName(getName());
      molecularAction.setTimestamp(new Date());
      molecularAction.setActivityId(getActivityId());
      molecularAction.setWorkId(getWorkId());

      // Add the molecular action and pass to the service.
      // It needs to be added now so that when atomic actions
      // are created by the service, this object already has
      // an identifier.
      final MolecularAction newMolecularAction =
          addMolecularAction(molecularAction);
      setMolecularAction(newMolecularAction);
    }

    // throw exception on terminology mismatch
    if (!concept.getTerminology().equals(project.getTerminology())) {
      // unlock concepts and fail
      rollback();
      throw new Exception("Project and concept terminologies do not match");
    }

    if (concept.getLastModified().getTime() != lastModified) {
      // unlock concepts and fail
      rollback();
      throw new LocalException(
          "Concept has changed since last read, please refresh and try again");
    }
  }

  /**
   * Lock related concepts.
   *
   * @throws Exception the exception
   */
  public void lockRelatedConcepts() throws Exception {

    Concept sourceConcept = null;
    // For merges, conceptId2 will have the affected relationships
    if (this instanceof MergeMolecularAction) {
      sourceConcept = getConcept2();
    }
    // For splits and approvals, conceptId will have the affected relationships
    if (this instanceof ApproveMolecularAction
        || this instanceof SplitMolecularAction) {
      sourceConcept = getConcept();
    }

    // Get all of the inverse relationships associated with the source concept
    List<ConceptRelationship> relationships = sourceConcept.getRelationships();

    // Lock all of the concepts that are NOT concept or concept2
    for (final ConceptRelationship rel : relationships) {
      // Grab the concept from the relationship
      Concept associatedConcept = rel.getTo();

      // Verify concept exists
      if (associatedConcept == null) {
        // unlock concepts and fail
        rollback();
        throw new Exception("Concept does not exist");
      }

      // Make sure we're not trying to re-lock Concept or Concept2
      if (getConcept().getId().equals(associatedConcept.getId())) {
        continue;
      }
      if (getConcept2() != null
          && getConcept2().getId().equals(associatedConcept.getId())) {
        continue;
      }

      // Lock on the concept id (in Java)
      synchronized (associatedConcept.getId().toString().intern()) {

        // Fail if already locked - this is secondary protection
        if (isObjectLocked(associatedConcept)) {
          // unlock concepts and fail
          rollback();
          throw new Exception(
              "Fatal error: concept is locked " + associatedConcept.getId());
        }

        // lock the concept via JPA
        lockObject(associatedConcept);

      }
    }
  }

  /**
   * Find inverse relationship.
   *
   * @param relationship the relationship
   * @return the relationship<? extends component info,? extends component info>
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> findInverseRelationship(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception {

    // instantiate required services
    final ContentService contentService = new ContentServiceJpa();

    RelationshipList relList =
        contentService.getInverseRelationships(relationship);

    // If there's only one inverse relationship returned, that's the one we
    // want.
    if (relList.size() == 1) {
      return relList.getObjects().get(0);
    }
    // If more than one inverse relationship is returned (can happen in the case
    // of demotions), return the appropriate one.
    else {
      if (relationship.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
            .getObjects()) {
          if (rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
            return rel;
          }
        }
      } else {
        for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
            .getObjects()) {
          if (!rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
            return rel;
          }
        }
      }

    }

    return null;
  }

  /**
   * Indicates whether or not delete action is the case.
   *
   * @param action the action
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  public boolean isRemoveAction(AtomicAction action) {
    return action.getNewValue() == null && action.getField().equals("id");
  }

  /**
   * Indicates whether or not insert action is the case.
   *
   * @param action the action
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  public boolean isAddAction(AtomicAction action) {
    return action.getOldValue() == null && action.getField().equals("id");
  }

  /**
   * Indicates whether or not change action is the case.
   *
   * @param action the action
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  public boolean isChangeAction(AtomicAction action) {
    return action.getOldValue() != null && action.getNewValue() != null
        && action.getCollectionClassName() == null;
  }

  /**
   * Indicates whether or not collections action is the case.
   *
   * @param action the action
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  public boolean isCollectionsAction(AtomicAction action) {
    return action.getCollectionClassName() != null;
  }

  /**
   * Returns the referenced object.
   *
   * @param action the action
   * @return the referenced object
   * @throws Exception the exception
   */
  public HasLastModified getReferencedObject(AtomicAction action)
    throws Exception {
    final Object referencedObject =
        getObject(action.getObjectId(), Class.forName(action.getClassName()));

    if (referencedObject == null) {
      throw new Exception("Unable to find referenced object for "
          + action.getObjectId() + ", " + action.getClassName());
    }
    return (HasLastModified) referencedObject;
  }

  /**
   * Returns the referenced collection object.
   *
   * @param action the action
   * @return the referenced collection object
   * @throws Exception the exception
   */
  public HasLastModified getReferencedCollectionObject(AtomicAction action)
    throws Exception {
    final String id = action.getOldValue() == null ? action.getNewValue()
        : action.getOldValue();
    final Object referencedObject = getObject(Long.parseLong(id),
        Class.forName(action.getCollectionClassName()));

    if (referencedObject == null) {
      throw new Exception("Unable to find referenced (collection) object for "
          + id + ", " + action.getCollectionClassName());
    }
    return (HasLastModified) referencedObject;
  }

  /**
   * Returns the collection.
   *
   * @param a the a
   * @param containerObject the referenced object
   * @return the collection
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "static-method", "rawtypes"
  })
  public Collection getCollection(AtomicAction a, Object containerObject)
    throws Exception {
    final List<Method> oneToManyMethods =
        IndexUtility.getAllCollectionGetMethods(containerObject.getClass());

    // Iterate through @OneToMan methods
    for (final Method m : oneToManyMethods) {
      if (IndexUtility.getFieldNameFromMethod(m, null).equals(a.getField())) {
        return (Collection) m.invoke(containerObject, new Object[] {});
      }
    }

    throw new Exception(
        "Unable to find collection method for: " + a.getField());
  }

  /**
   * Returns the column method.
   *
   * @param a the a
   * @return the column method
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Method getColumnGetMethod(AtomicAction a) throws Exception {
    final List<Method> oneToManyMethods =
        IndexUtility.getAllColumnGetMethods(Class.forName(a.getClassName()));

    // Iterate through @OneToMan methods
    for (final Method m : oneToManyMethods) {
      if (IndexUtility.getFieldNameFromMethod(m, null).equals(a.getField())) {
        return m;
      }
    }

    throw new Exception(
        "Unable to find column get method for: " + a.getField());
  }

  /**
   * Returns the column set method.
   *
   * @param a the a
   * @return the column set method
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Method getColumnSetMethod(AtomicAction a) throws Exception {
    final List<Method> oneToManyMethods =
        IndexUtility.getAllColumnSetMethods(Class.forName(a.getClassName()));

    // Iterate through @OneToMan methods
    for (final Method m : oneToManyMethods) {
      if (IndexUtility.getFieldNameFromMethod(m, null).equals(a.getField())) {
        return m;
      }
    }

    throw new Exception(
        "Unable to find column set method for: " + a.getField());
  }

  /**
   * Returns the object for value.
   *
   * @param type the type
   * @param value the value
   * @return the object for value
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public Object getObjectForValue(Class<?> type, String value)
    throws Exception {
    Object setObject = null;
    if (type == String.class) {
      setObject = value;
    } else if (type == Long.class) {
      setObject = Long.parseLong(value);
    } else if (type == WorkflowStatus.class) {
      setObject = WorkflowStatus.valueOf(value);
    } else {
      throw new Exception(
          "Unrecognized getter method type for undo operation - " + type);
    }
    return setObject;
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    throw new UnsupportedOperationException(
        "Individual molecular actions should not "
            + "be used as configurable algorithms");
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    throw new UnsupportedOperationException(
        "Individual molecular actions should not "
            + "be used as configurable algorithms");
  }

  /**
   * Post action maintenance.
   *
   * @throws Exception the exception
   */
  public void postActionMaintenance() throws Exception {

    List<Concept> conceptList = new ArrayList<Concept>();
    conceptList.add(getConcept());
    conceptList.add(getConcept2());

    // Only concepts that exist and contain atoms will need to go through this
    // process
    for (Concept c : conceptList) {
      if (c != null && !c.getAtoms().isEmpty()) {

        // Start a new action that doesn't create molecular/atomic actions
        beginTransaction();
        setMolecularActionFlag(false);

        //
        // Recompute tracking record workflow status
        //

        // Any tracking record that references this concept may potentially be
        // updated.
        final TrackingRecordList trackingRecords =
            findTrackingRecordsForConcept(getProject(), c, null, null);

        // Set trackingRecord to READY_FOR_PUBLICATION if all contained
        // concepts and atoms are all set to READY_FOR_PUBLICATION.
        if (trackingRecords != null) {
          for (TrackingRecord rec : trackingRecords.getObjects()) {
            final WorkflowStatus status = computeTrackingRecordStatus(rec);
            rec.setWorkflowStatus(status);
            updateTrackingRecord(rec);
          }
        }

        //
        // Recompute the concept's preferred name
        //

        c.setName(getComputePreferredNameHandler(c.getTerminology())
            .computePreferredName(c.getAtoms(),
                getPrecedenceList(c.getTerminology(), c.getVersion())));

        commit();
      }
    }

  }

}
