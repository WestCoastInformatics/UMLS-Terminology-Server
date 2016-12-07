/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractSourceInsertionAlgorithm;
import com.wci.umls.server.jpa.algo.maint.QueryActionAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;

/**
 * Implementation of an algorithm to update releasability / publishable.
 */
public class UpdateReleasabilityAlgorithm
    extends AbstractSourceInsertionAlgorithm {

  /**
   * Instantiates an empty {@link UpdateReleasabilityAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public UpdateReleasabilityAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("UPDATERELEASABILITY");
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
      throw new Exception("Metadata Loading requires a project to be set");
    }

    // Check the input directories

    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "rawtypes"
  })
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting UPDATERELEASABILITY");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    try {

      logInfo(
          "[UpdateReleasaibility] Making all old version content unpublishable");

      // Get all terminologies referenced in the sources.src file
      Set<Pair<String, String>> referencedTerminologies = new HashSet<>();
      referencedTerminologies = getReferencedTerminologies();

      final List<Class> classList = new ArrayList<>(Arrays.asList(AtomJpa.class,
          AtomRelationshipJpa.class, AtomSubsetJpa.class,
          AtomSubsetMemberJpa.class, AttributeJpa.class, CodeJpa.class,
          CodeRelationshipJpa.class, ComponentInfoRelationshipJpa.class,
          ConceptJpa.class, ConceptRelationshipJpa.class,
          ConceptSubsetJpa.class, ConceptSubsetMemberJpa.class,
          DefinitionJpa.class, DescriptorJpa.class,
          DescriptorRelationshipJpa.class, MappingJpa.class, MapSetJpa.class,
          SemanticTypeComponentJpa.class));

      // Each class will be counted as its own step for this algorithm's
      // progress
      setSteps(classList.size());

      // Find all of the old version component ids
      for (Class clazz : classList) {
        String query = "SELECT DISTINCT c.id " + "FROM " + clazz.getSimpleName()
            + " c "
            + "WHERE (c.terminology=:terminology AND NOT c.version=:version)";

        // Make sure all of the terminologies in sources.src are included in the
        // query
        for (Pair<String, String> referencedTerminology : referencedTerminologies) {
          query += " OR (c.terminology='" + referencedTerminology.getLeft()
              + "' AND NOT c.version='" + referencedTerminology.getRight()
              + "')";
        }

        // Perform a QueryActionAlgorithm using the class and query
        final QueryActionAlgorithm queryAction = new QueryActionAlgorithm();
        queryAction.setLastModifiedBy(getLastModifiedBy());
        queryAction.setLastModifiedFlag(isLastModifiedFlag());
        queryAction.setProcess(getProcess());
        queryAction.setProject(getProject());
        queryAction.setTerminology(getTerminology());
        queryAction.setVersion(getVersion());

        Properties algoProperties = new Properties();
        algoProperties.put("objectType", clazz.getSimpleName());
        algoProperties.put("action", "Make Unpublishable");
        algoProperties.put("queryType", QueryType.JQL.toString());
        algoProperties.put("query", query);
        queryAction.setProperties(algoProperties);

        // Run the QUERYACTION algorithm
        try {

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

        } catch (Exception e) {
          e.printStackTrace();
          fail("Unexpected exception thrown - please review stack trace.");
        } finally {
          queryAction.close();
        }

        // Update the progress
        updateProgress();
      }

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished UPDATERELEASABILITY");

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Returns the referenced terminologies.
   *
   * @return the referenced terminologies
   * @throws Exception the exception
   */
  private Set<Pair<String, String>> getReferencedTerminologies()
    throws Exception {

    Set<Pair<String, String>> referencedTerminologies = new HashSet<>();

    //
    // Load the sources.src file
    //
    List<String> lines =
        loadFileIntoStringList(getSrcDirFile(), "sources.src", null, null);

    String fields[] = new String[20];

    // Each line of sources.src corresponds to one terminology.
    // Save the each terminology and version as a pair, and add to the results
    // list
    for (String line : lines) {
      FieldedStringTokenizer.split(line, "|", 20, fields);
      referencedTerminologies.add(new ImmutablePair<>(fields[4], fields[5]));
    }

    return referencedTerminologies;

  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

}