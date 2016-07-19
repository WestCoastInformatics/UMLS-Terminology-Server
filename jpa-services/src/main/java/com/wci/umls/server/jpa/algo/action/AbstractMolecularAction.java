/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.wci.umls.server.Project;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.actions.MolecularActionJpa;
import com.wci.umls.server.jpa.algo.AbstractTerminologyAlgorithm;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Concept;

/**
 * Abstract {@link MolecularActionAlgorithm}.
 */
public abstract class AbstractMolecularAction
    extends AbstractTerminologyAlgorithm implements MolecularActionAlgorithm {

  /** The concept. */
  private Concept concept;

  /** The concept2. */
  private Concept concept2;

  /** The project. */
  private Project project;

  /** The user name. */
  private String userName;

  /** The last modified. */
  private Long lastModified;

  /** The change status flag. */
  private boolean changeStatusFlag;

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

  /**
   * Returns the project.
   *
   * @return the project
   */
  @Override
  public Project getProject() {
    return project;
  }

  /* see superclass */
  @Override
  public String getUserName() {
    return userName;
  }

  /* see superclass */
  @Override
  public Long getLastModified() {
    return lastModified;
  }


  /* see superclass */
  @Override
  public boolean getChangeStatusFlag() {
    return changeStatusFlag;
  }


  /* see superclass */
  @Override  
  public void setChangeStatusFlag(boolean changeStatusFlag) {
    this.changeStatusFlag = changeStatusFlag;
  }

  /* see superclass */
  @Override
  public void initialize(Project project, Long conceptId, Long conceptId2,
    String userName, Long lastModified) throws Exception {

    this.project = project;
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
          throw new Exception("Fatal error: concept is locked " + i);
        }

        // lock the concept via JPA
        lockObject(tempConcept);

      }
    }

    // construct the molecular action
    final MolecularAction molecularAction = new MolecularActionJpa();
    molecularAction.setTerminology(this.concept.getTerminology());
    molecularAction.setTerminologyId(this.concept.getTerminologyId());
    if (conceptId2 != null) {
      molecularAction.setTerminologyId2(this.concept2.getTerminologyId());
    }
    molecularAction.setVersion(concept.getVersion());
    molecularAction.setName(getName());
    molecularAction.setTimestamp(new Date());

    // Prepare the service
    setMolecularActionFlag(true);
    setLastModifiedFlag(true);
    setLastModifiedBy(userName);

    // Add the molecular action and pass to the service.
    // It needs to be added now so that when atomic actions
    // are created by the service, this object already has
    // an identifier.
    final MolecularAction newMolecularAction =
        addMolecularAction(molecularAction);
    setMolecularAction(newMolecularAction);

    // throw exception on terminology mismatch
    if (!concept.getTerminology().equals(project.getTerminology())) {
      throw new Exception("Project and concept terminologies do not match");
    }

    if (concept.getLastModified().getTime() != lastModified) {
      throw new LocalException(
          "Concept has changed since last read, please refresh and try again");
    }
  }
}
