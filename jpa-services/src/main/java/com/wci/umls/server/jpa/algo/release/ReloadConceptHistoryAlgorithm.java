/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.model.content.Concept;

/**
 * Algorithm for reloading concept history.
 */
public class ReloadConceptHistoryAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /** The created count. */
  private int createdCount = 0;

  /**
   * Instantiates an empty {@link ReloadConceptHistoryAlgorithm}.
   *
   *
   * @throws Exception the exception
   */
  public ReloadConceptHistoryAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("FEEDBACKRELEASE");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Algorithm requires a project to be set");
    }

    return result;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    fireProgressEvent(0, "Starting");

    //
    // Load the MRCUI.RRF file
    //
    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());

    final List<String> lines =
        loadFileIntoStringList(path, "MRCUI.RRF", null, null);

    // Set the number of steps to the number of lines to be processed
    setSteps(lines.size());

    final String fields[] = new String[7];

    for (final String line : lines) {

      // Check for a cancelled call once every 100 lines
      if (getStepsCompleted() % 100 == 0) {
        checkCancel();
      }

      FieldedStringTokenizer.split(line, "|", 7, fields);

      //
      // 0 CUI1
      // 1 VER
      // 2 REL
      // 3 RELA
      // 4 MAPREASON
      // 5 CUI2
      // 6 MAPIN

      // e.g. C0000002|2000AC|SY|||C0007404|Y|

      // Figure out if the CUI has a current existing concept associated with it
      final Query jpaQuery = getEntityManager().createQuery("select c "
          + "from ConceptJpa c where terminologyId = :terminologyId");
      jpaQuery.setParameter("terminologyId", fields[0]);

      final List<Object> list = jpaQuery.getResultList();

      if (list.size() > 1) {
        throw new Exception(
            "Unexpected number of project concepts with terminologyId "
                + fields[0]);
      }

      // If no concept exists, create a new unpublishable concept
      if (list.size() == 0) {
        final Concept newConcept = new ConceptJpa();
        newConcept.setPublishable(false);
        newConcept.setTerminologyId(fields[0]);
        // TODO - other concept stuff
      }

      // Update the progress
      updateProgress();
    }

    commitClearBegin();

    fireProgressEvent(100, "Finished - 100%");
    logInfo("  concepts created = " + createdCount);
    logInfo("Finished " + getName());

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
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    return params;
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
