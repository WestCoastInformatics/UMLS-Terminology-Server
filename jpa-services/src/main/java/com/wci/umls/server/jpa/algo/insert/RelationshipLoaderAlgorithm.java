/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractSourceLoaderAlgorithm;
import com.wci.umls.server.jpa.content.AbstractRelationship;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of an algorithm to import relationships.
 */
public class RelationshipLoaderAlgorithm extends AbstractSourceLoaderAlgorithm {

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /**
   * The rui ID map. Key = AlternateTerminologyId; Value = relationship Id
   */
  private Map<String, Long> ruiIdMap = new HashMap<>();

  /**
   * The loaded terminologies. Key = Terminology_Version (or just Terminology,
   * if Version = "latest") Value = Terminology object
   */
  private Map<String, Terminology> loadedTerminologies = new HashMap<>();

  /**
   * The code ID map. Key = CodeJpa.TerminologyId Value = CodeJpa.Id
   */
  private Map<String, Long> codeIdMap = new HashMap<String, Long>();

  /**
   * The concept ID map. Key = ConceptJpa.TerminologyId Value = ConceptJpa.Id
   */
  private Map<String, Long> conceptIdMap = new HashMap<String, Long>();

  /**
   * Set containing the name of all terminologies referenced in the
   * classes_atoms.src file
   */
  private Set<String> allTerminologiesFromInsertion = new HashSet<>();

  /**
   * Instantiates an empty {@link RelationshipLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public RelationshipLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RELATIONSHIPLOADER");
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
      throw new Exception("Relationship Loading requires a project to be set");
    }

    // Check the input directories

    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    srcDirFile = new File(srcFullPath);
    if (!srcDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /**
   * Load file into string list.
   *
   * @param fileName the file name
   * @param startsWithFilter the starts with filter
   * @return the list
   * @throws Exception the exception
   */
  private List<String> loadFileIntoStringList(String fileName,
    String startsWithFilter) throws Exception {
    String sourcesFile =
        srcDirFile + File.separator + "src" + File.separator + fileName;
    BufferedReader sources = null;
    try {
      sources = new BufferedReader(new FileReader(sourcesFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + sourcesFile);
    }

    List<String> lines = new ArrayList<>();
    String linePre = null;
    while ((linePre = sources.readLine()) != null) {
      linePre = linePre.replace("\r", "");
      // Filter rows if defined
      if (ConfigUtility.isEmpty(startsWithFilter)) {
        lines.add(linePre);
      } else {
        if (linePre.startsWith(startsWithFilter)) {
          lines.add(linePre);
        }
      }
    }

    sources.close();

    return lines;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public void compute() throws Exception {
    logInfo("Starting RELATIONSHIPLOADING");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Set up the handler for identifier assignment
    final IdentifierAssignmentHandler handler =
        newIdentifierAssignmentHandler(getProject().getTerminology());
    handler.setTransactionPerOperation(false);
    handler.beginTransaction();

    // Count number of added and updated Relationships, for logging
    int addCount = 0;
    int updateCount = 0;

    try {

      previousProgress = 0;
      stepsCompleted = 0;

      logInfo("[RelationshipLoader] Checking for new/updated Relationships");

      //
      // Load the relationships.src file
      //
      List<String> lines = loadFileIntoStringList("relationships.src", null);

      // TODO - will need to for contexts.src once working for relationships.src
      // Use sg_type_1 and sg_type_2 to get to/from components
      // if sg_type_2 = SCR_ATOM_ID, skip.

      // Set the number of steps to the number of atoms to be processed
      steps = lines.size();

      // Cache all of the currently existing atom RUIs and Terminologies
      cacheExistingTerminologies();
      cacheExistingRelationships();
      identifyAllTerminologiesFromInsertion(lines);
      cacheExistingCodes();
      cacheExistingConcepts();

      String fields[] = new String[18];

      // Each line of relationships.src corresponds to one relationship.
      // Check to make sure the relationship doesn't already exist in the
      // database
      // If it does, skip it.
      // If it does not, add it.
      for (String line : lines) {

        // Check for a cancelled call once every 100 relationships (doing it
        // every time
        // makes things too slow)
        if (stepsCompleted % 100 == 0 && isCancelled()) {
          throw new CancelException("Cancelled");
        }

        FieldedStringTokenizer.split(line, "|", 18, fields);

        // Fields:
        // 0 src_relationship_id (Not used)
        // 1 level
        // 2 id_1
        // 3 relationship_name
        // 4 relationship_attribute
        // 5 id_2
        // 6 source
        // 7 source_of_label
        // 8 status
        // 9 tobereleased
        // 10 released
        // 11 suppressible
        // 12 id_type_1
        // 13 id_qualifier_1
        // 14 id_type_2
        // 15 id_qualifier_2
        // 16 source_rui
        // 17 relationship_group

        // e.g.
        // 40|S|C17260|RT|Gene_Plays_Role_In_Process|C29949|NCI_2016_05E|
        // NCI_2016_05E|R|Y|N|N|SOURCE_CUI|NCI_2016_05E|SOURCE_CUI|NCI_2016_05E|||

        //
        // Relationship based on input line.
        //

        // Load the containing objects based on type
        final String fromTerminologyId = fields[5];
        final String fromTerminology = fields[15].contains("_")
            ? fields[15].substring(0, fields[15].indexOf('_')) : fields[15];
        final Class<? extends Component> fromClass = lookupClass(fields[14]);

        Long fromComponentId = null;
        if (fromClass.equals(CodeJpa.class)) {
          fromComponentId = codeIdMap.get(fromTerminologyId + fromTerminology);
        } else if (fromClass.equals(ConceptJpa.class)) {
          fromComponentId =
              conceptIdMap.get(fromTerminologyId + fromTerminology);
        }

        Component fromComponent = fromComponentId == null ? null
            : getComponent(fromComponentId, fromClass);

        if (fromComponent == null) {
          logWarn(
              "Warning - could not find from Component for the following line:\n\t"
                  + line);
          updateProgress();
          logAndCommit("[Relationship Loader] Relationships processed ",
              stepsCompleted, RootService.logCt, RootService.commitCt);
          continue;
        }

        final String toTerminologyId = fields[2];
        final String toTerminology = fields[13].contains("_")
            ? fields[13].substring(0, fields[13].indexOf('_')) : fields[13];
        final Class<? extends Component> toClass = lookupClass(fields[12]);

        Long toComponentId = null;
        if (toClass.equals(CodeJpa.class)) {
          toComponentId = codeIdMap.get(toTerminologyId + toTerminology);
        } else if (toClass.equals(ConceptJpa.class)) {
          toComponentId = conceptIdMap.get(toTerminologyId + toTerminology);
        }
        Component toComponent =
            toComponentId == null ? null : getComponent(toComponentId, toClass);

        if (toComponent == null) {
          logWarn(
              "Warning - could not find to Component for the following line:\n\t"
                  + line);
          updateProgress();
          logAndCommit("[Relationship Loader] Relationships processed ",
              stepsCompleted, RootService.logCt, RootService.commitCt);
          continue;
        }

        // Create the relationship.
        // If id_type_1 equals id_type_2, the relationship is of that type.
        // If they are not equal, it's a Component Info Relationship
        AbstractRelationship newRelationship = null;
        Class relClass = null;

        if (!fromClass.equals(toClass)) {
          relClass = ComponentInfoRelationshipJpa.class;
          newRelationship = new ComponentInfoRelationshipJpa();
        } else if (fromClass.equals(ConceptJpa.class)
            && toClass.equals(ConceptJpa.class)) {
          relClass = ConceptRelationshipJpa.class;
          newRelationship = new ConceptRelationshipJpa();
        } else if (fromClass.equals(CodeJpa.class)
            && toClass.equals(CodeJpa.class)) {
          relClass = CodeRelationshipJpa.class;
          newRelationship = new CodeRelationshipJpa();
        } else {
          throw new Exception("Error - unhandled class type: " + fromClass);
        }

        newRelationship.setAdditionalRelationshipType(fields[4]);
        newRelationship.setBranch(Branch.ROOT);
        newRelationship.setFrom(fromComponent);
        newRelationship.setGroup(fields[17]);
        newRelationship.setInferred(true);
        newRelationship.setName(fields[4]);
        newRelationship.setObsolete(false);
        newRelationship.setPublishable(fields[9].equals("Y"));
        newRelationship.setPublished(fields[10].equals("Y"));
        newRelationship.setRelationshipType(lookupRelationshipType(fields[3]));
        newRelationship.setHierarchical(false);
        newRelationship.setStated(true);
        newRelationship.setSuppressible(fields[11].equals("Y"));
        Terminology term = loadedTerminologies.get(fields[6]);
        if (term == null) {
          throw new Exception(
              "ERROR: lookup for " + fields[6] + " returned no terminology");
        } else {
          newRelationship.setAssertedDirection(term.isAssertsRelDirection());
          newRelationship.setTerminology(term.getTerminology());
          newRelationship.setVersion(term.getVersion());
        }
        newRelationship.setTerminologyId(fields[16]);
        newRelationship.setTo(toComponent);
        newRelationship.setWorkflowStatus(lookupWorkflowStatus(fields[8]));

        // Calculate inverseRel and inverseAdditionalRel types, to use in the
        // RUI handler and the inverse relationship creation
        String inverseRelType =
            getRelationshipType(newRelationship.getRelationshipType(),
                getProject().getTerminology(), getProject().getVersion())
                    .getInverse().getAbbreviation();

        String inverseAdditionalRelType = "";
        if (!newRelationship.getAdditionalRelationshipType().equals("")) {
          inverseAdditionalRelType = getAdditionalRelationshipType(
              newRelationship.getAdditionalRelationshipType(),
              getProject().getTerminology(), getProject().getVersion())
                  .getInverse().getAbbreviation();
        }

        // Create the inverse relationship
        AbstractRelationship newInverseRelationship =
            (AbstractRelationship) newRelationship.createInverseRelationship(
                newRelationship, inverseRelType, inverseAdditionalRelType);

        // Compute identity for relationship and its inverse
        // Note: need to pass in the inverse RelType and AdditionalRelType
        String newRelationshipRui = handler.getTerminologyId(newRelationship,
            inverseRelType, inverseAdditionalRelType);
        String newInverseRelationshipRui = handler.getTerminologyId(
            newInverseRelationship, newRelationship.getRelationshipType(),
            newRelationship.getAdditionalRelationshipType());

        // Check to see if relationship with matching RUI already exists in the
        // database
        Long oldRelationshipId = ruiIdMap.get(newRelationshipRui);
        Long oldInverseRelationshipId = ruiIdMap.get(newInverseRelationshipRui);

        // If no relationships with the same RUI exists, add this new
        // relationship
        if (oldRelationshipId == null) {
          newRelationship.getAlternateTerminologyIds()
              .put(getProject().getTerminology() + "-SRC", newRelationshipRui);
          newRelationship =
              (AbstractRelationship) addRelationship(newRelationship);

          addCount++;
          ruiIdMap.put(newRelationshipRui, newRelationship.getId());

          // No need to explicitly attach to component - will be done
          // automatically by addRelationship.

        }
        // If an existing relationship DOES exist, update the version
        else {
          final AbstractRelationship oldRelationship =
              (AbstractRelationship) getRelationship(oldRelationshipId,
                  relClass);
          oldRelationship.getAlternateTerminologyIds()
              .put(getProject().getTerminology() + "-SRC", newRelationshipRui);
          oldRelationship.setVersion(newRelationship.getVersion());

          // If the existing relationship doesn't exactly equal the new one,
          // update obsolete, suppressible, and group as well
          if (!oldRelationship.equals(newRelationship)) {
            oldRelationship.setObsolete(newRelationship.isObsolete());
            oldRelationship.setSuppressible(newRelationship.isSuppressible());
            oldRelationship.setGroup(newRelationship.getGroup());
          }

          updateCount++;
          updateRelationship(oldRelationship);
        }

        // If no inverse relationships with the same RUI exists, add the new
        // inverse relationship
        if (oldInverseRelationshipId == null) {
          newInverseRelationship.getAlternateTerminologyIds().put("SRC",
              newInverseRelationshipRui);
          newInverseRelationship =
              (AbstractRelationship) addRelationship(newInverseRelationship);

          addCount++;
          ruiIdMap.put(newInverseRelationshipRui,
              newInverseRelationship.getId());

          // No need to explicitly attach to component - will be done
          // automatically by addRelationship.

        }
        // If an existing inverse relationship DOES exist, update the version,
        // add an
        // AlternateTermminologyId, and update
        else {
          final AbstractRelationship oldInverseRelationship =
              (AbstractRelationship) getRelationship(oldInverseRelationshipId,
                  relClass);
          oldInverseRelationship.getAlternateTerminologyIds().put("SRC",
              newInverseRelationshipRui);
          oldInverseRelationship
              .setVersion(newInverseRelationship.getVersion());

          // If the existing inverse relationship doesn't exactly equal the new
          // one,
          // update obsolete, suppressible, and group as well
          if (!oldInverseRelationship.equals(newInverseRelationship)) {
            oldInverseRelationship
                .setObsolete(newInverseRelationship.isObsolete());
            oldInverseRelationship
                .setSuppressible(newInverseRelationship.isSuppressible());
            oldInverseRelationship.setGroup(newInverseRelationship.getGroup());
          }

          updateCount++;
          updateRelationship(oldInverseRelationship);
        }

        // Update the progress
        updateProgress();

        logAndCommit("[Relationship Loader] Relationships processed ",
            stepsCompleted, RootService.logCt, RootService.commitCt);
        handler.logAndCommit(
            "[Relationship Loader] Relationship Identities processed ",
            stepsCompleted, RootService.logCt, RootService.commitCt);

      }

      logInfo("[RelationshipLoader] Added " + addCount + " new Relationships.");
      logInfo("[RelationshipLoader] Updated " + updateCount
          + " existing Relationships.");

      //
      // Load the relationships from relationships.src
      //

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished RELATIONSHIPLOADING");

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Lookup workflow status.
   *
   * @param string the string
   * @return the workflow status
   * @throws Exception the exception
   */
  @Override
  public WorkflowStatus lookupWorkflowStatus(String string) throws Exception {

    WorkflowStatus workflowStatus = null;

    switch (string) {
      case "R":
        workflowStatus = WorkflowStatus.READY_FOR_PUBLICATION;
        break;
      case "N":
        workflowStatus = WorkflowStatus.NEEDS_REVIEW;
        break;
      default:
        throw new Exception("Invalid workflowStatus type: " + string);
    }

    return workflowStatus;
  }

  /**
   * Identify all terminologies from insertion.
   *
   * @param lines the lines
   * @throws Exception the exception
   */
  private void identifyAllTerminologiesFromInsertion(List<String> lines)
    throws Exception {

    String fields[] = new String[18];
    steps = lines.size();
    stepsCompleted = 0;

    for (String line : lines) {

      FieldedStringTokenizer.split(line, "|", 18, fields);

      // For the purpose of this method, all we care about:
      // fields[6]: source
      // fields[13]: id_qualifier_1
      // fields[15]: id_qualifier_2

      String terminology = fields[6].contains("_")
          ? fields[6].substring(0, fields[6].indexOf('_')) : fields[6];
      allTerminologiesFromInsertion.add(terminology);

      terminology = fields[13].contains("_")
          ? fields[13].substring(0, fields[13].indexOf('_')) : fields[13];
      allTerminologiesFromInsertion.add(terminology);

      terminology = fields[15].contains("_")
          ? fields[15].substring(0, fields[15].indexOf('_')) : fields[15];

      allTerminologiesFromInsertion.add(terminology);
    }
  }

  /**
   * Lookup relationship type.
   *
   * @param string the string
   * @return the string
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private String lookupRelationshipType(String string) throws Exception {

    String relationshipType = null;

    switch (string) {
      case "RT":
        relationshipType = "RO";
        break;
      case "NT":
        relationshipType = "RN";
        break;
      case "BT":
        relationshipType = "RB";
        break;
      case "RT?":
        relationshipType = "RQ";
        break;
      case "SY":
        relationshipType = "SY";
        break;
      case "SFO/LFO":
        relationshipType = "SY";
        break;
      default:
        throw new Exception("Invalid relationship type: " + relationshipType);
    }

    return relationshipType;

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

  /**
   * Cache existing relationships' RUIs and IDs.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingRelationships() throws Exception {

    // Get RUIs for ConceptRelationships, CodeRelationships, and
    // ComponentInfoRelationships.

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from concept_relationships a join conceptrelationshipjpa_alternateterminologyids b where a.id = b.ConceptRelationshipJpa_id AND a.publishable = 1"
            + " UNION ALL "
            + "select b.alternateTerminologyIds, a.id from code_relationships a join coderelationshipjpa_alternateterminologyids b where a.id = b.CodeRelationshipJpa_id AND a.publishable = 1"
            + " UNION ALL "
            + "select b.alternateTerminologyIds, a.id from component_info_relationships a join componentinforelationshipjpa_alternateterminologyids b where a.id = b.ComponentInfoRelationshipJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[RelationshipLoader] Loading relationship RUIs from database: "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        ruiIdMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);
  }

  /**
   * Cache existing terminologies. Key = Terminology_Version, or just
   * Terminology if version = "latest"
   *
   * @throws Exception the exception
   */
  private void cacheExistingTerminologies() throws Exception {

    for (final Terminology term : getTerminologies().getObjects()) {
      // lazy init
      term.getSynonymousNames().size();
      term.getRootTerminology().getTerminology();
      if (term.getVersion().equals("latest")) {
        loadedTerminologies.put(term.getTerminology(), term);
      } else {
        loadedTerminologies.put(term.getTerminology() + "_" + term.getVersion(),
            term);
      }
    }
  }

  /**
   * Cache existing codes.
   *
   * @throws Exception the exception
   */
  private void cacheExistingCodes() throws Exception {

    // Pre-populate codeIdMap (for all terminologies from this insertion)
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.terminology, c.id "
            + "from CodeJpa c where terminology in :terminologies");
    hQuery.setParameterList("terminologies", allTerminologiesFromInsertion);
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final String terminology = results.get()[1].toString();
      final Long id = Long.valueOf(results.get()[2].toString());
      codeIdMap.put(terminologyId + terminology, id);
    }
    results.close();
  }

  /**
   * Cache existing concepts.
   *
   * @throws Exception the exception
   */
  private void cacheExistingConcepts() throws Exception {

    // Pre-populate conceptIdMap (for all terminologies from this insertion)
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.terminology, c.id "
            + "from ConceptJpa c where terminology in :terminologies");
    hQuery.setParameterList("terminologies", allTerminologiesFromInsertion);
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final String terminology = results.get()[1].toString();
      final Long id = Long.valueOf(results.get()[2].toString());
      conceptIdMap.put(terminologyId + terminology, id);
    }
    results.close();
  }

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      fireProgressEvent(currentProgress,
          "RELATIONSHIPLOADING progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /**
   * Sets the properties.
   *
   * @param p the properties
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        // TODO - handle problem with config.properties needing properties
    }, p);

  }

  /**
   * Returns the parameters.
   *
   * @return the parameters
   */
  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

}