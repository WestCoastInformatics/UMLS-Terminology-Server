/*
 *    Copyright 2019 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.insert.UpdateReleasabilityAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Implementation of an algorithm to set all contents of a retired terminology
 * to unpublishable
 */
public class RetireTerminology extends UpdateReleasabilityAlgorithm {

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /**
   * Instantiates an empty {@link RetireTerminology}.
   * @throws Exception if anything goes wrong
   */
  public RetireTerminology() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RETIRETERMINOLOGY");
    setLastModifiedBy("admin");
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

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Retire Terminology requires a project to be set");
    }

    if (terminology == null) {
      throw new Exception("Retire Terminology requires a terminology");
    }

    if (version == null) {
      throw new Exception("Retire Terminology requires a version");
    }

    Terminology terminologyToRemove = getTerminology(terminology, version);

    if (terminologyToRemove == null) {
      throw new Exception("Terminology " + terminology + " and version "
          + version + " not found.");
    }

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
    logInfo("Starting " + getName());

    try {

      logInfo(
          "[Retire Terminology] settings contents of retired terminology to unpublishable.");
      commitClearBegin();

      // Set the retire flag, so UpdateReleasability knows it is being called
      // from RetireTerminology
      setRetire(true);

      // First, set the terminology to current=false (this is a prerequisite for
      // much of the following functionality)
      Terminology retiredTerminology = getTerminology(terminology, version);
      retiredTerminology.setCurrent(false);
      updateTerminology(retiredTerminology);

      // Next, since the terminology is being fully retired, set all of the
      // non-versioned SRC atoms to unpublishable (this is not handled by the
      // underlying UpdateReleasabilityAlgorithm compute)
      String query = "SELECT a.id " + "FROM AtomJpa a, TerminologyJpa t "
          + "WHERE a.terminology='SRC' AND a.publishable=true AND t.current = false AND "
          + "a.codeId=CONCAT('V-',t.terminology)";

      // Perform a QueryActionAlgorithm using the class and query
      QueryActionAlgorithm queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(AtomJpa.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      // Also mark non-current, non-versioned SRC codes as unpublishable.
      query = "SELECT a.id " + "FROM CodeJpa a, TerminologyJpa t "
          + "WHERE a.terminology='SRC' AND a.publishable=true AND t.current = false AND a.terminologyId=CONCAT('V-',t.terminology)";

      // Perform a QueryActionAlgorithm using the class and query
      queryAction = new QueryActionAlgorithm();
      try {
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());
        queryAction.setWorkId(getWorkId());
        queryAction.setActivityId(getActivityId());

        queryAction.setObjectTypeClass(CodeJpa.class);
        queryAction.setAction("Make Unpublishable");
        queryAction.setQueryType(QueryType.JPQL);
        queryAction.setQuery(query);

        queryAction.setTransactionPerOperation(false);
        queryAction.beginTransaction();

        //
        // Check prerequisites
        //
        ValidationResult validationResult = queryAction.checkPreconditions();
        // if prerequisites fail, return validation result
        if (!validationResult.getErrors().isEmpty()
            || (!validationResult.getWarnings().isEmpty())) {
          // rollback -- unlocks the concept and closes transaction
          queryAction.rollback();
        }
        assertTrue(validationResult.getErrors().isEmpty());

        //
        // Perform the algorithm
        //
        queryAction.compute();

        // Commit the algorithm.
        queryAction.commit();

      } catch (Exception e) {
        queryAction.rollback();
        e.printStackTrace();
        fail("Unexpected exception thrown - please review stack trace.");
      } finally {
        // Close algorithm for each loop
        queryAction.close();
      }

      // Finally, run the standard UpdateReleasbilityAlgorithm on the one
      // specified terminology (the above steps will make sure that all
      // remaining objects are set to unpublishable appropriately)
      super.compute();

      logInfo(
          "[Retire Terminology] " + terminology + "/" + version + " retired.");

      commitClearBegin();

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished " + getName());

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "terminology", "version"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("terminology") != null) {
      terminology = String.valueOf(p.getProperty("terminology"));
    }
    if (p.getProperty("version") != null) {
      version = String.valueOf(p.getProperty("version"));
    }
  }

  /**
   * Returns the parameters.
   *
   * @return the parameters
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    // Terminology
    AlgorithmParameter param = new AlgorithmParameterJpa("Terminology",
        "terminology", "The retired terminology to remove", "e.g. NCI", 40,
        AlgorithmParameter.Type.STRING, "");
    params.add(param);

    // Version
    param = new AlgorithmParameterJpa("Version", "version",
        "The version of the retired terminology to remove", "e.g. 2017_06D", 40,
        AlgorithmParameter.Type.STRING, "");
    params.add(param);

    return params;
  }

  /**
   * This overrides the standard
   * AbstractInsertMainRelease.getReferencedTerminologies, and returns just the
   * one retired terminology.
   *
   * @return the referenced terminologies
   * @throws Exception the exception
   */
  public Set<Terminology> getReferencedTerminologies() throws Exception {

    final Set<Terminology> referencedTerminologies = new HashSet<>();

    Terminology retiredTerminology = getTerminology(terminology, version);
    retiredTerminology.setCurrent(false);
    updateTerminology(retiredTerminology);

    referencedTerminologies.add(retiredTerminology);

    return referencedTerminologies;

  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Sets all content to Unpublishable for specified retired terminology";
  }

}