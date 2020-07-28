/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.NoResultException;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.model.meta.TermType;

/**
 * Implementation of an algorithm to save information before an insertion.
 */
public class PreInsertionAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  /**
   * Instantiates an empty {@link PreInsertionAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public PreInsertionAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("PREINSERTION");
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
      throw new LocalException("Pre Insertion requires a project to be set");
    }

    // Go through all the files needed by insertion and check for presence
    // Check the input directories
    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir") + "/"
            + getProcess().getInputPath();

    final Path realPath = Paths.get(srcFullPath).toRealPath();
    setSrcDirFile(new File(realPath.toString()));

    if (!getSrcDirFile().exists()) {
      throw new LocalException(
          "Specified input directory does not exist - " + srcFullPath);
    }

    checkFileExist(srcFullPath, "attributes.src");
    checkFileExist(srcFullPath, "classes_atoms.src");
    checkFileExist(srcFullPath, "contexts.src");
    checkFileExist(srcFullPath, "mergefacts.src");
    checkFileExist(srcFullPath, "MRDOC.RRF");
    checkFileExist(srcFullPath, "relationships.src");
    checkFileExist(srcFullPath, "sources.src");
    checkFileExist(srcFullPath, "termgroups.src");

    // Checking for UMLS-specific files.
    if (getProcess().getTerminology().equals("MTH")) {
      checkFileExist(srcFullPath, "umlscui.txt");
      checkFileExist(srcFullPath, "bequeathal.relationships.src");
    }

    // Ensure permissions are sufficient to write files
    try {
      final File outputFile = new File(srcFullPath, "testFile.txt");

      final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
      out.print("Test");
      out.close();

      // Remove test file
      outputFile.delete();
    } catch (Exception e) {
      throw new LocalException("Unable to write files to " + srcFullPath
          + " - update permissions before continuing insertion.");
    }

    // Makes sure editing is turned off before continuing
    if (getProject().isEditingEnabled()) {
      throw new LocalException(
          "Editing is turned on - disable before continuing insertion.");
    }

    // Makes sure automations are turned off before continuing
    if (getProject().isAutomationsEnabled()) {
      throw new LocalException(
          "Automations are turned on - disable before continuing insertion.");
    }

    //
    // Check for duplicate source atom ids
    //

    // Lookup all existing source atom ids from the database
    logInfo("[PreInsertionAlgorithm] Loading Source Atom Ids from database");

    String query =
        "select value(b) from AtomJpa a join a.alternateTerminologyIds b "
            + "where KEY(b) = :terminology ";

    javax.persistence.Query jpaQuery = getEntityManager().createQuery(query);
    jpaQuery.setParameter("terminology",
        getProject().getTerminology() + "-SRC");

    List<Object> list = jpaQuery.getResultList();
    Set<String> existingSourceAtomIds = new HashSet<>();
    for (Object entry : list) {
      existingSourceAtomIds.add(entry.toString());
    }

    // Check each of the classes_atoms source lines
    List<String> srcLines = loadFileIntoStringList(getSrcDirFile(),
        "classes_atoms.src", null, null, null);

    String fields[] = new String[14];

    for (String line : srcLines) {
      FieldedStringTokenizer.split(line, "|", 14, fields);
      if (existingSourceAtomIds.contains(fields[0])) {
        validationResult
            .addError("ERROR: classes_atoms.src references a SRC atom id "
                + fields[0] + " that is already contained in the database.");
        break;
      }
    }

    //
    // Check for mismatches between to-be-inserted atoms' suppressible/obsolete
    // values and the associated TermType suppressible/obsolete values
    //

    Map<String, Set<String>> termTypeToSuppressibilityMap = new HashMap<>();

    for (String line : srcLines) {
      FieldedStringTokenizer.split(line, "|", 14, fields);
      String termType = fields[2].substring(fields[2].indexOf("/") + 1);
      String suppressible = fields[8];

      if (!termTypeToSuppressibilityMap.containsKey(termType)) {
        final Set<String> suppressibilityValues = new HashSet<>();
        suppressibilityValues.add(suppressible);
        termTypeToSuppressibilityMap.put(termType, suppressibilityValues);
      } else {
        final Set<String> suppressibilityValues =
            termTypeToSuppressibilityMap.get(termType);
        suppressibilityValues.add(suppressible);
        termTypeToSuppressibilityMap.put(termType, suppressibilityValues);
      }
    }

    for (String termTypeAbbreviation : termTypeToSuppressibilityMap.keySet()) {
      Set<String> suppressibilityValues =
          termTypeToSuppressibilityMap.get(termTypeAbbreviation);

      // If multiple different suppressibility values identified, log as error
      if (suppressibilityValues.size() > 1) {
        validationResult
            .addError("ERROR: classes_atoms.src references atom termType "
                + termTypeAbbreviation
                + " that has inconsistent suppressibility values: "
                + suppressibilityValues);
      }

      // Check against the global termType and log error if mismatch
      else {
        Iterator<String> iterator = suppressibilityValues.iterator();

        String suppressibilityValue = iterator.next();
        Boolean atomTermTypeSuppressible = "OYE".contains(suppressibilityValue);
        Boolean atomTermTypeObsolete = suppressibilityValue.equals("O");

        TermType termType = getCachedTermType(termTypeAbbreviation);

        if (termType.isObsolete() != atomTermTypeObsolete) {
          validationResult.addError("ERROR: obsolete value for global TermType "
              + termTypeAbbreviation + " is "
              + (termType.isObsolete() ? " true " : " false")
              + ", and does not match " + termTypeAbbreviation
              + "'s obsolete value in classes_atoms.src.");
        }

        if (termType.isSuppressible() != atomTermTypeSuppressible) {
          validationResult
              .addError("ERROR: supressible value for global TermType "
                  + termTypeAbbreviation + " is "
                  + (termType.isSuppressible() ? " true " : " false")
                  + ", and does not match " + termTypeAbbreviation
                  + "'s suppressible value in classes_atoms.src.");
        }
      }
    }

    return validationResult;
  }

  /**
   * Check file exist.
   *
   * @param srcFullPath the src full path
   * @param fileName the file name
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void checkFileExist(String srcFullPath, String fileName)
    throws Exception {

    File sourceFile = new File(srcFullPath + File.separator + fileName);
    if (!sourceFile.exists()) {
      throw new Exception(fileName
          + " file doesn't exist at specified input directory: " + srcFullPath);
    }

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

    // No Molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Populate the executionInfo map of the process' execution.
    ProcessExecution processExecution = getProcess();

    // Get the max atom Id prior to the insertion starting (used to identify
    // which atoms are new)
    Long atomId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AtomJpa a ");
      final Long atomId2 = (Long) query.getSingleResult();
      atomId = atomId2 != null ? atomId2 : atomId;
    } catch (NoResultException e) {
      atomId = 0L;
    }
    processExecution.getExecutionInfo().put("maxAtomIdPreInsertion",
        atomId.toString());
    logInfo(" maxAtomIdPreInsertion = "
        + processExecution.getExecutionInfo().get("maxAtomIdPreInsertion"));
    commitClearBegin();

    // Get the max AUI prior to the insertion starting (used to identify
    // newly created AUIs)
    Long AUI = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AtomIdentityJpa a ");
      final Long AUI2 = (Long) query.getSingleResult();
      AUI = AUI2 != null ? AUI2 : AUI;
    } catch (NoResultException e) {
      AUI = 0L;
    }
    processExecution.getExecutionInfo().put("maxAUIPreInsertion",
        AUI.toString());
    logInfo(" maxAUIPreInsertion = "
        + processExecution.getExecutionInfo().get("maxAUIPreInsertion"));
    commitClearBegin();

    // Get the max Semantic Type Component Id prior to the insertion starting
    Long styId = null;
    try {
      final javax.persistence.Query query = manager
          .createQuery("select max(a.id) from SemanticTypeComponentJpa a ");
      final Long styId2 = (Long) query.getSingleResult();
      styId = styId2 != null ? styId2 : styId;
    } catch (NoResultException e) {
      styId = 0L;
    }
    processExecution.getExecutionInfo().put("maxStyIdPreInsertion",
        styId.toString());
    logInfo(" maxStyIdPreInsertion = "
        + processExecution.getExecutionInfo().get("maxStyIdPreInsertion"));

    // Get the max MapSet Id prior to the insertion starting
    Long mapSetId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from MapSetJpa a ");
      final Long mapSetId2 = (Long) query.getSingleResult();
      mapSetId = mapSetId2 != null ? mapSetId2 : mapSetId;
    } catch (NoResultException e) {
      mapSetId = 0L;
    }
    processExecution.getExecutionInfo().put("maxMapSetIdPreInsertion",
        mapSetId.toString());
    logInfo(" maxMapSetIdPreInsertion = "
        + processExecution.getExecutionInfo().get("maxMapSetIdPreInsertion"));

    // Get the max Atom Subset Id prior to the insertion starting
    Long atomSubsetId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from AtomSubsetJpa a ");
      final Long atomSubsetId2 = (Long) query.getSingleResult();
      atomSubsetId = atomSubsetId2 != null ? atomSubsetId2 : atomSubsetId;
    } catch (NoResultException e) {
      atomSubsetId = 0L;
    }
    processExecution.getExecutionInfo().put("maxAtomSubsetIdPreInsertion",
        atomSubsetId.toString());
    logInfo(" maxAtomSubsetIdPreInsertion = " + processExecution
        .getExecutionInfo().get("maxAtomSubsetIdPreInsertion"));

    // Get the max Concept Subset Id prior to the insertion starting
    Long conceptSubsetId = null;
    try {
      final javax.persistence.Query query =
          manager.createQuery("select max(a.id) from ConceptSubsetJpa a ");
      final Long conceptSubsetId2 = (Long) query.getSingleResult();
      conceptSubsetId = conceptSubsetId2 != null ? conceptSubsetId2 : 0L;
    } catch (NoResultException e) {
      conceptSubsetId = 0L;
    }
    processExecution.getExecutionInfo().put("maxConceptSubsetIdPreInsertion",
        conceptSubsetId.toString());
    logInfo(" maxConceptSubsetIdPreInsertion = " + processExecution
        .getExecutionInfo().get("maxConceptSubsetIdPreInsertion"));

    // NOTE: the processExecution is updated by the calling method,
    // typically RunProcessAsThread in ProcessServiceRestImpl

    logInfo("Finished " + getName());
  }

  /**
   * Reset.
   *
   * @throws Exception the exception
   */
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
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  @Override
  public String getDescription() {
    return "Prepares an insertion to operate and validates starting conditions.";
  }
}