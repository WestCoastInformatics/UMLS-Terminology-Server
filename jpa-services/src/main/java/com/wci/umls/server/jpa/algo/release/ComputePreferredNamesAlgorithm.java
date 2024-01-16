/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;

/**
 * Algorithm for computing preferred names and publication status for a project
 * terminology. TODO: change this package to "rel" => "release (fix config
 * files)
 * 01/11/2024 added code to recompute source derived concept names based on process's terminology/version
 * such as NCI/2024_12D
 * this is to ensure that the preferred names in the concept context hierarchies stay up-to-date
 */
public class ComputePreferredNamesAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link ComputePreferredNamesAlgorithm}.
   *
   *
   * @throws Exception the exception
   */
  public ComputePreferredNamesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("PREFNAMES");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();
    // There must be a default precedence list with at least one entry.
    if (getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion()) == null) {
      result.addError("Precedence list not found for terminology: "
          + getProject().getTerminology() + ", " + getProject().getVersion());
    } else if (getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion()).getPrecedence().getKeyValuePairs()
            .size() == 0) {
      result.addError(
          "Precedence list for terminology " + getProject().getTerminology()
              + ", " + getProject().getVersion() + " has no entries.");
    }

    return result;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
	// first run the computer preferred names for the project terminology (ex. NCIMTH/latest)
    logInfo("Starting " + getName() + " " + getProject().getTerminology() + getProject().getVersion());

    // Configure algorithm
    ComputePreferredNameHandler handler =
        getComputePreferredNameHandler(getProject().getTerminology());
    setMolecularActionFlag(false);
    PrecedenceList list = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());

    // 1. Collect all atoms from project concepts
    // Normalization is only for English
    List<Long> conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c " + "where c.terminology = :terminology "
            + "  and c.version = :version and publishable = true",
        QueryType.JPQL, getDefaultQueryParams(getProject()), ConceptJpa.class,
        false);
    commitClearBegin();

    // Iterate through each concept
    int objectCt = 0;
    int updatedCt = 0;
    int prevProgress = 0;
    int totalCt = conceptIds.size();
    int progressCheck = (int) (totalCt / 200.0) + 1;
    for (final Long id : conceptIds) {
      final Concept concept = getConcept(id);

      // if something changed, update the concept
      if (isChanged(concept, handler, list)) {
        updateConcept(concept);
        // // Reindex the concept relationships because the name changed
        // for (final ConceptRelationship rel : concept.getRelationships()) {
        // updateRelationship(rel);
        // }
        // for (final ConceptRelationship rel : concept
        // .getInverseRelationships()) {
        // updateRelationship(rel);
        // }
        updatedCt++;
      }

      // progress/cancel
      if (objectCt % progressCheck == 0) {
        int currentProgress = (int) ((100.0 * objectCt / totalCt));
        if (currentProgress > prevProgress) {
          fireProgressEvent(currentProgress,
              "Progress: " + currentProgress + "%");
          prevProgress = currentProgress;
        }
        checkCancel();
      }

      // log/commit
      logAndCommit(objectCt++, RootService.logCt, RootService.commitCt);

    }
    commitClearBegin();

    fireProgressEvent(100, "Finished - 100%");
    logInfo("  concept count = " + objectCt);
    logInfo("  concepts updated = " + updatedCt);
    logInfo("Finished " + getName() + " " + getProject().getTerminology() + getProject().getVersion());
    
    
    
    
    // rerun the compute preferred names algorithm for the source concept names (ex. NCI/2024_12D)   
    logInfo("Starting " + getName() + " " + getProcess().getTerminology() + getProcess().getVersion());
    if (getProcess().getTerminology().isEmpty() || getProcess().getVersion().isEmpty()) {
    	return;
    }
    // Configure algorithm
    handler =
        getComputePreferredNameHandler(getProcess().getTerminology());
    setMolecularActionFlag(false);
    
    // 1. Collect all atoms from project concepts
    // Normalization is only for English
    conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c " + "where c.terminology = :terminology "
            + "  and c.version = :version and publishable = true",
        QueryType.JPQL, getProcessQueryParams(), ConceptJpa.class,
        false);
    commitClearBegin();

    // Iterate through each concept
    objectCt = 0;
    updatedCt = 0;
    prevProgress = 0;
    totalCt = conceptIds.size();
    progressCheck = (int) (totalCt / 200.0) + 1;
    for (final Long id : conceptIds) {
      final Concept concept = getConcept(id);

      // if something changed, update the concept
      if (isChanged(concept, handler, list)) {
        updateConcept(concept);
        updatedCt++;
      }

      // progress/cancel
      if (objectCt % progressCheck == 0) {
        int currentProgress = (int) ((100.0 * objectCt / totalCt));
        if (currentProgress > prevProgress) {
          fireProgressEvent(currentProgress,
              "Progress: " + currentProgress + "%");
          prevProgress = currentProgress;
        }
        checkCancel();
      }

      // log/commit
      logAndCommit(objectCt++, RootService.logCt, RootService.commitCt);

    }
    commitClearBegin();

    fireProgressEvent(100, "Finished - 100%");
    logInfo("  concept count = " + objectCt);
    logInfo("  concepts updated = " + updatedCt);
    logInfo("Finished " + getName() + " " + getProcess().getTerminology() + getProcess().getVersion());

  }

  public Map<String, String> getProcessQueryParams() {
	    final Map<String, String> params = new HashMap<>();
	    params.put("projectTerminology", getProject().getTerminology());
	    params.put("terminology", getProcess().getTerminology());
	    params.put("version", getProcess().getVersion());
	    return params;
	  }
  
  /**
   * Helper.
   *
   * @param concept the concept
   * @param handler the handler
   * @param list the list
   * @return true, if successful
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private boolean isChanged(Concept concept,
    ComputePreferredNameHandler handler, PrecedenceList list) throws Exception {

    // Calculate publishable
    boolean publishable = false;
    boolean hasAtoms = false;
    boolean updateConcept = false;
    for (final Atom atom : concept.getAtoms()) {
      hasAtoms = true;
      if (atom.isPublishable()) {
        publishable = true;
      }
    }
    if (concept.isPublishable() != publishable) {
      updateConcept = true;
      concept.setPublishable(publishable);
    }

    // If there are atoms, recompute the preferred name
    if (hasAtoms) {
      final String computedName =
          handler.computePreferredName(concept.getAtoms(), list);
      if (computedName == null) {
        throw new Exception(
            "Unexpected concept without preferred name - " + concept.getId());
      }

      if (!computedName.equals(concept.getName())) {
        updateConcept = true;
        if (concept.getTerminology().equals(getProcess().getTerminology())) {
        	logInfo("source preferred name updated: " + concept.getName() + "   " + computedName );
        }
        concept.setName(computedName);
      }
    }

    return updateConcept;
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // No reset, this can be safely re-run
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
