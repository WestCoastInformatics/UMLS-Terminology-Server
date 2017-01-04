/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.Properties;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
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
    // There must be a project precedence list with at least one entry.
    if (getProject().getPrecedenceList() == null) {
      result.addError("Project does not have a precedence list");
    } else if (getProject().getPrecedenceList().getPrecedence()
        .getKeyValuePairs().size() == 0) {
      result.addError("Project has a precedence list with no entries.");
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
    logInfo("Starting compute preferred names");

    // Configure algorithm
    final ComputePreferredNameHandler handler =
        getComputePreferredNameHandler(getProject().getTerminology());
    setMolecularActionFlag(false);

    // get progress monitor max (all project concepts)
    final javax.persistence.Query query =
        manager.createQuery("select count(*) from ConceptJpa c "
            + "where terminology = :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    int progressMax = Integer.parseInt(query.getSingleResult().toString());

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select distinct c from ConceptJpa c join c.atoms a where c.terminology = :terminology order by c");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);

    // Iterate through each concept
    int objectCt = 0;
    int ct = 0;
    int prevProgress = 0;
    final int progressCheck = (int) (progressMax / 100.0) + 1;
    while (results.next()) {
      final Concept concept = (Concept) results.get()[0];
      ct++;

      // if something changed, update the concept
      if (isChanged(concept, handler)) {
        logInfo("    concept = " + concept.getId() + ", "
            + concept.isPublishable() + ", " + concept.getName());
        updateConcept(concept);
        // log/commit
        logAndCommit(objectCt++, RootService.logCt, RootService.commitCt);
      }

      // progress/cancel
      if (ct % progressCheck == 0) {
        int currentProgress = (int) ((100.0 * ct / progressMax));
        if (currentProgress > prevProgress) {
          fireProgressEvent(currentProgress,
              "Progress: " + currentProgress + "%");
          prevProgress = currentProgress;
        }
        checkCancel();
      }

    }
    results.close();
    commitClearBegin();

    fireProgressEvent(100, "Finished - 100%");
    logInfo("  concept count = " + progressMax);
    logInfo("  concepts updated = " + objectCt);
    logInfo("Finished compute preferred names");

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
      final String computedName = handler.computePreferredName(
          concept.getAtoms(), getProject().getPrecedenceList());
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
