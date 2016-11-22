/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.algo.action.AddDemotionMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.RootService;

/**
 * Abstract support for merge algorithms.
 */
public abstract class AbstractMergeAlgorithm
    extends AbstractSourceLoaderAlgorithm {

  /**
   * Instantiates an empty {@link AbstractMergeAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractMergeAlgorithm() throws Exception {
    // n/a
  }

  /**
   * Merge.
   *
   * @param atomId the atom id
   * @param atomId2 the atom id 2
   * @param validationChecks the integrity check names
   * @param makeDemotion the make demotion
   * @param changeStatus the change status
   * @param project the project
   * @return true, if successful
   * @throws Exception the exception
   */
  public boolean merge(Long atomId, Long atomId2, List<String> validationChecks,
    boolean makeDemotion, boolean changeStatus, Project project)
    throws Exception {   
    
    // Copy the project, and overwrite its validation checks with the ones that
    // were specified
    final Project projectCopy = new ProjectJpa(project);
    projectCopy.setValidationChecks(validationChecks);

    // Get the two concepts associated with the two atoms
    List<ConceptJpa> concepts =
        searchHandler.getQueryResults(projectCopy.getTerminology(),
            projectCopy.getVersion(), Branch.ROOT, "atoms.id:" + atomId, null,
            ConceptJpa.class, null, new int[1], getEntityManager());
    if (concepts.size() != 1) {
      throw new Exception("Unexpected number of concepts: " + concepts.size()
          + ", for atom: " + atomId);
    }
    final Concept concept = concepts.get(0);

    concepts = searchHandler.getQueryResults(projectCopy.getTerminology(),
        projectCopy.getVersion(), Branch.ROOT, "atoms.id:" + atomId2, null,
        ConceptJpa.class, null, new int[1], getEntityManager());
    if (concepts.size() != 1) {
      throw new Exception("Unexpected number of concepts: " + concepts.size()
          + ", for atom: " + atomId2);
    }
    final Concept concept2 = concepts.get(0);

    // Identify the from and to concepts, and from/to Atoms
    // FromConcept will be the smaller concept (least number of atoms)
    Concept fromConcept = null;
    Concept toConcept = null;
    Atom fromAtom = null;
    Atom toAtom = null;

    if (concept.getAtoms().size() < concept2.getAtoms().size()) {
      fromConcept = concept;
      fromAtom = getAtom(atomId);
      toConcept = concept2;
      toAtom = getAtom(atomId2);
    } else {
      fromConcept = concept2;
      fromAtom = getAtom(atomId2);
      toConcept = concept;
      toAtom = getAtom(atomId);
    }

    // If Atoms are in the same concept, DON'T perform merge, and log that the
    // atoms are already merged.
    if (fromConcept.getId().equals(toConcept.getId())) {
      addLogEntry(getLastModifiedBy(), projectCopy.getId(),
          fromConcept.getId(), getActivityId(), getWorkId(),
          "Failure merging atom " + atomId + " with atom " + atomId
              + " - atoms are both already in the same concept "
              + toConcept.getId());
      return false;
    }

    // Create and set up a merge action
    final MergeMolecularAction action = new MergeMolecularAction();

    try {

      // Configure the action
      action.setProject(projectCopy);
      action.setActivityId(getActivityId());
      action.setConceptId(fromConcept.getId());
      action.setConceptId2(toConcept.getId());
      action.setLastModifiedBy(getLastModifiedBy());
      action.setLastModified(fromConcept.getLastModified().getTime());
      action.setOverrideWarnings(false);
      action.setTransactionPerOperation(false);
      action.setMolecularActionFlag(true);
      action.setChangeStatusFlag(changeStatus);

      // Perform the action
      final ValidationResult validationResult =
          action.performMolecularAction(action, getLastModifiedBy(), false);

      // If the action failed, log the failure, and make a demotion if
      // makeDemotion=true.
      if (!validationResult.isValid()) {
        addLogEntry(getLastModifiedBy(), projectCopy.getId(),
            fromConcept.getId(), getActivityId(), getWorkId(),
            "Failure merging concept " + fromConcept.getId() + " into concept "
                + toConcept.getId() + ": " + validationResult);
        addLogEntry(getLastModifiedBy(), projectCopy.getId(),
            toConcept.getId(), getActivityId(), getWorkId(),
            "Failure merging concept " + toConcept.getId() + " from concept "
                + fromConcept.getId() + ": " + validationResult);

        if (makeDemotion) {
          final AddDemotionMolecularAction action2 =
              new AddDemotionMolecularAction();
          action2.setTransactionPerOperation(false);
          action2.setProject(projectCopy);
          action2.setTerminology(projectCopy.getTerminology());
          action2.setVersion(projectCopy.getVersion());
          action2.setWorkId(getWorkId());
          action2.setActivityId(getActivityId());
          action2.setAtom(fromAtom);
          action2.setAtom2(toAtom);
          action2.setChangeStatusFlag(changeStatus);
          action2.setConceptId(fromConcept.getId());
          action2.setConceptId2(toConcept.getId());
          action2.setLastModifiedBy(getLastModifiedBy());
          action2.performMolecularAction(action2, getLastModifiedBy(),false);
          action2.close();
        }

        return false;
      }
      // Otherwise, it was successful.
      else {
        return true;
      }

    } catch (Exception e) {
      try {
        action.rollback();
        e.printStackTrace();
      } catch (Exception e2) {
        // do nothing
      }
      return false;
    } finally {
      action.close();
    }

  }

  /**
   * Returns the merge sets.
   *
   * @param srcDirFile the src dir file
   * @return the merge sets
   */
  public List<String> getMergeSets(File srcDirFile) {

    final List<String> mergeSets = new ArrayList<>();
    final Set<String> mergeSetsUnique = new HashSet<>();
    List<String> lines = new ArrayList<>();
    //
    // Load the mergefacts.src file
    //
    try {
      lines =
          loadFileIntoStringList(getSrcDirFile(), "mergefacts.src", null, null);
    }
    // If file not found, return null
    catch (Exception e) {
      return null;
    }

    final int fieldCount = StringUtils.countMatches(lines.get(0), "|") + 1;
    String fields[] = new String[fieldCount];

    // For this method's purpose, the only field we care about is merge_set, at
    // index 7
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", fieldCount, fields);
      final String mergeSet = fields[7];
      mergeSetsUnique.add(mergeSet);
    }

    // Add all of the unique mergeSets referenced in the file to the stringList,
    // and return
    mergeSets.addAll(mergeSetsUnique);

    return mergeSets;
  }

  /*
   * see superclass Need to override, because the Merge algorithm naming
   * convention is different from the loader algorithms
   */
  @Override
  public void updateProgress() throws Exception {
    String algoName = getClass().getSimpleName();
    String shortName = algoName.substring(0, algoName.indexOf("Algorithm"));
    String objectType = "Merge";

    setStepsCompleted(getStepsCompleted() + 1);

    int currentProgress = (int) ((100.0 * getStepsCompleted() / getSteps()));
    if (currentProgress > getPreviousProgress()) {
      fireProgressEvent(currentProgress,
          shortName.toUpperCase() + " progress: " + currentProgress + "%");
      setPreviousProgress(currentProgress);
    }

    if (!transactionPerOperation) {
      logAndCommit("[" + shortName + "] " + objectType + " lines processed ",
          getStepsCompleted(), RootService.logCt, RootService.commitCt);
    }
  }

}