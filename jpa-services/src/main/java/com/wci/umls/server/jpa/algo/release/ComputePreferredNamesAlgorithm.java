/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
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
    logInfo("Starting PREFNAMES");

    // Configure algorithm
    final ComputePreferredNameHandler handler =
        getComputePreferredNameHandler(getProject().getTerminology());
    setMolecularActionFlag(false);

    // 1. Collect all atoms from project concepts
    final Map<String, String> params = new HashMap<>();
    params.put("terminology", getProject().getTerminology());
    params.put("version", getProject().getVersion());
    // Normalization is only for English
    final List<Long> conceptIds = executeSingleComponentIdQuery(
        "select c.id from ConceptJpa c " + "where c.terminology = :terminology "
            + "  and c.version = :version and publishable = true",
        QueryType.JQL, params, ConceptJpa.class);
    commitClearBegin();

    // Iterate through each concept
    int objectCt = 0;
    int updatedCt = 0;
    int prevProgress = 0;
    int totalCt = conceptIds.size();
    final int progressCheck = (int) (totalCt / 200.0) + 1;
    for (final Long id : conceptIds) {
      final Concept concept = getConcept(id);

      // if something changed, update the concept
      if (isChanged(concept, handler)) {
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
    logInfo("Finished PREFNAMES");

  }

  /**
   * Helper.
   *
   * @param concept the concept
   * @param handler the handler
   * @return true, if successful
   * @throws Exception the exception
   */
  private boolean isChanged(Concept concept,
    ComputePreferredNameHandler handler) throws Exception {

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
          handler.computePreferredName(concept.getAtoms(), getPrecedenceList(
              getProject().getTerminology(), getProject().getVersion()));
      if (computedName == null) {
        throw new Exception(
            "Unexpected concept without preferred name - " + concept.getId());
      }

      if (!computedName.equals(concept.getName())) {
        updateConcept = true;
        concept.setName(computedName);
      }
    }

    return updateConcept;
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // No reset, this can be safely re-run
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
