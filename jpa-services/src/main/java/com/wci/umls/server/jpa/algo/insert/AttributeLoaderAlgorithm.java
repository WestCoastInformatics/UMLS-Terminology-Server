/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AbstractComponentHasAttributes;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentHasDefinitions;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of an algorithm to import attributes.
 */
public class AttributeLoaderAlgorithm extends AbstractAlgorithm {

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /**
   * The atui ID map. Key = AlternateTerminologyId Value = attributeJpa Id
   */
  private Map<String, Long> atuiIdMap = new HashMap<>();

  /**
   * The concept ID map. Key = ConceptJpa.TerminologyId Value = ConceptJpa.Id
   */
  private Map<String, Long> conceptIdMap = new HashMap<String, Long>();

  /**
   * Set containing the name of all terminologies referenced in the
   * attributes.src file
   */
  private Set<String> allTerminologiesFromInsertion = new HashSet<>();

  /**
   * The loaded terminologies. Key = Terminology_Version (or just Terminology,
   * if Version = "latest") Value = Terminology object
   */
  private Map<String, Terminology> loadedTerminologies =
      new HashMap<String, Terminology>();

  /**
   * Instantiates an empty {@link AttributeLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public AttributeLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("ATTRIBUTELOADER");
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
      throw new Exception("Attribute Loading requires a project to be set");
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
   * Identify all terminologies from insertion.
   *
   * @param lines the lines
   * @throws Exception the exception
   */
  private void identifyAllTerminologiesFromInsertion(List<String> lines)
    throws Exception {

    String fields[] = new String[15];
    steps = lines.size();
    stepsCompleted = 0;

    for (String line : lines) {

      FieldedStringTokenizer.split(line, "|", 15, fields);

      // For the purpose of this method, all we care about is fields[1]: source
      final String terminology = fields[1].contains("_")
          ? fields[1].substring(0, fields[1].indexOf('_')) : fields[1];

      allTerminologiesFromInsertion.add(terminology);
    }
  }

  /**
   * Cache existing attributes' ATUIs and IDs.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingAttributes() throws Exception {

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from attributes a join attributejpa_alternateterminologyids b where a.id = b.AttributeJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[AttributeLoader] Loading attribute ATUIs from database: "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        atuiIdMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);
  }

  /**
   * Cache existing definitions' ATUIs and IDs.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingDefinitions() throws Exception {

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from definitions a join definitionjpa_alternateterminologyids b where a.id = b.DefinitionJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[AttributeLoader] Loading definitions ATUIs from database: "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        atuiIdMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);
  }

  /**
   * Class lookup.
   *
   * @param string the string
   * @return the class<? extends hasid>
   */
  private Class<? extends Component> lookupClass(String string)
    throws Exception {

    Class<? extends Component> objectClass = null;

    switch (string) {
      case "CODE_SOURCE":
        objectClass = CodeJpa.class;
        break;
      case "SOURCE_CUI":
        objectClass = ConceptJpa.class;
        break;
      case "SRC_ATOM_ID":
        objectClass = AtomJpa.class;
        break;
      default:
        throw new IllegalArgumentException("Invalid class type: " + string);
    }

    return objectClass;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting ATTRIBUTELOADING");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Set up the handler for identifier assignment
    final IdentifierAssignmentHandler handler =
        newIdentifierAssignmentHandler(getProject().getTerminology());
    handler.setTransactionPerOperation(false);
    handler.beginTransaction();

    // Count number of added and updated Attributes and Definitions
    // for logging
    int attributeAddCount = 0;
    int attributeUpdateCount = 0;
    int definitionAddCount = 0;
    int definitionUpdateCount = 0;

    try {

      previousProgress = 0;
      stepsCompleted = 0;

      logInfo(
          "[AttributeLoader] Checking for new/updated Attributes and Definitions");

      //
      // Load the attributes.src file
      //
      List<String> lines = loadFileIntoStringList("attributes.src", null);

      // Set the number of steps to the number of atoms to be processed
      steps = lines.size();

      // Cache all of the currently existing atom RUIs and Terminologies
      cacheExistingTerminologies();
      cacheExistingAttributes();
      cacheExistingDefinitions();
      identifyAllTerminologiesFromInsertion(lines);
      cacheExistingConcepts();

      String fields[] = new String[14];

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

        FieldedStringTokenizer.split(line, "|", 14, fields);

        // Fields:
        // 0 source_attribute_id
        // 1 sg_id
        // 2 attribute_level
        // 3 attribute_name
        // 4 attribute_value
        // 5 source
        // 6 status
        // 7 tobereleased
        // 8 released
        // 9 suppressible
        // 10 sg_type_1
        // 11 sg_qualifier_1
        // 12 source_atui
        // 13 hashcode

        // e.g.
        // 49|C47666|S|Chemical_Formula|C19H32N2O5.C4H11N|NCI_2016_05E|R|Y|N|N|SOURCE_CUI|NCI_2016_05E||875b4a03f8dedd9de05d6e9e4a440401|

        // Skip SEMANTIC_TYPE, CONTEXT, SUBSET_MEMBER, XMAP, XMAPTO, XMAPFROM

        if (Arrays.asList("SEMANTIC_TYPE", "CONTEXT", "SUBSET_MEMBER", "XMAP",
            "XMAPTO", "XMAPFROM").contains(fields[4])) {
          updateProgress();
          logAndCommit("[Attribute Loader] Attribute lines processed ",
              stepsCompleted, RootService.logCt, RootService.commitCt);
        }

        // If it's a DEFITION, process the line as a definition instead of an
        // attribute
        else if (fields[4].equals("DEFINITION")) {
          // Create the definition
          Definition newDefinition = new DefinitionJpa();
          newDefinition.setBranch(Branch.ROOT);
          newDefinition.setName(fields[3]);
          newDefinition.setValue(fields[4]);
          newDefinition.setTerminologyId("TestId");
          Terminology term = loadedTerminologies.get(fields[5]);
          if (term == null) {
            throw new Exception(
                "ERROR: lookup for " + fields[5] + " returned no terminology");
          } else {
            newDefinition.setTerminology(term.getTerminology());
            newDefinition.setVersion(term.getVersion());
          }
          newDefinition.setTimestamp(new Date());
          newDefinition.setSuppressible(fields[9].equals("Y"));
          newDefinition.setPublished(fields[6].equals("Y"));
          newDefinition.setPublishable(fields[7].equals("Y"));
          newDefinition.setObsolete(false);

          // Load the containing object
          final String containerTerminologyId = fields[1];
          final String containerTerminology = fields[5].contains("_")
              ? fields[5].substring(0, fields[5].indexOf('_')) : fields[5];
          final Class<? extends Component> containerClass =
              lookupClass(fields[10]);

          Long containerComponentId = null;
          if (containerClass.equals(ConceptJpa.class)) {
            containerComponentId =
                conceptIdMap.get(containerTerminologyId + containerTerminology);
          } else {
            throw new IllegalArgumentException(
                "Invalid class type: " + containerClass);
          }

          ComponentHasDefinitions containerComponent =
              containerComponentId == null ? null
                  : (ComponentHasDefinitions) getComponent(containerComponentId,
                      containerClass);

          if (containerComponent == null) {
            // TODO - remove update and continue, and uncomment Exception once
            // testing is completed.
            updateProgress();
            logAndCommit("[Attribute Loader] Attributes processed ",
                stepsCompleted, RootService.logCt, RootService.commitCt);
            continue;
            // throw new Exception("Error - lookup returned no object: " +
            // fromClass.getSimpleName() + " with terminologyId=" +
            // fromTerminologyId
            // + ", terminology=" + fromTerminology + ", version=" +
            // fromVersion);
          }

          // Compute definition identity
          String newDefinitionAtui =
              handler.getTerminologyId(newDefinition, containerComponent);

          // Check to see if attribute with matching ATUI already exists in the
          // database
          Long oldDefinitionId = atuiIdMap.get(newDefinitionAtui);

          // If no attribute with the same ATUI exists, create this new
          // Attribute, and add it to its containing component
          if (oldDefinitionId == null) {
            newDefinition.getAlternateTerminologyIds()
                .put(getProject().getTerminology() + "-SRC", newDefinitionAtui);
            newDefinition = addDefinition(newDefinition, containerComponent);

            definitionAddCount++;
            atuiIdMap.put(newDefinitionAtui, newDefinition.getId());

            // TODO - find out if this is needed.
            // If so, create a cache-map, so that all updates are made to same
            // copy of the component
            // // Add the definition to component
            // containerComponent.getDefinitions().add(newDefinition);
            // if (containerComponent instanceof ConceptJpa) {
            // updateComponent((ConceptJpa) containerComponent);
            // } else {
            // throw new Exception(
            // "Unhandled class type " + containerComponent.getClass());
            // }
          }
          // If a previous definition with same ATUI exists, load that object.
          else {
            final Definition oldDefinition = getDefinition(oldDefinitionId);

            boolean oldDefinitionChanged = false;

            // Create an "alternateTerminologyId" for the definition
            oldDefinition.getAlternateTerminologyIds()
                .put(getProject().getTerminology() + "-SRC", newDefinitionAtui);

            // Update the version
            if (!oldDefinition.getVersion()
                .equals(newDefinition.getVersion())) {
              oldDefinition.setVersion(newDefinition.getVersion());
              oldDefinitionChanged = true;
            }

            // If the existing relationship doesn't exactly equal the new one,
            // update obsolete, and suppressible
            if (!oldDefinition.equals(newDefinition)) {
              oldDefinition.setObsolete(newDefinition.isObsolete());
              oldDefinition.setSuppressible(newDefinition.isSuppressible());
            }

            if (oldDefinitionChanged) {
              updateDefinition(oldDefinition, containerComponent);
              definitionUpdateCount++;
            }
          }

        }

        // Otherwise, process the line as an attribute
        else {
          // Create the attribute
          Attribute newAttribute = new AttributeJpa();
          newAttribute.setBranch(Branch.ROOT);
          newAttribute.setName(fields[3]);
          newAttribute.setValue(fields[4]);
          newAttribute.setTerminologyId("TestId");
          Terminology term = loadedTerminologies.get(fields[5]);
          if (term == null) {
            throw new Exception(
                "ERROR: lookup for " + fields[5] + " returned no terminology");
          } else {
            newAttribute.setTerminology(term.getTerminology());
            newAttribute.setVersion(term.getVersion());
          }
          newAttribute.setTimestamp(new Date());
          newAttribute.setSuppressible(fields[9].equals("Y"));
          newAttribute.setPublished(fields[6].equals("Y"));
          newAttribute.setPublishable(fields[7].equals("Y"));
          newAttribute.setObsolete(false);

          // Load the containing object
          final String containerTerminologyId = fields[1];
          final String containerTerminology = fields[5].contains("_")
              ? fields[5].substring(0, fields[5].indexOf('_')) : fields[5];
          final Class<? extends Component> containerClass =
              lookupClass(fields[10]);

          Long containerComponentId = null;
          if (containerClass.equals(ConceptJpa.class)) {
            containerComponentId =
                conceptIdMap.get(containerTerminologyId + containerTerminology);
          } else {
            throw new IllegalArgumentException(
                "Invalid class type: " + containerClass);
          }

          AbstractComponentHasAttributes containerComponent =
              containerComponentId == null ? null
                  : (AbstractComponentHasAttributes) getComponent(
                      containerComponentId, containerClass);

          if (containerComponent == null) {
            // TODO - remove update and continue, and uncomment Exception once
            // testing is completed.
            updateProgress();
            logAndCommit("[Attribute Loader] Attributes processed ",
                stepsCompleted, RootService.logCt, RootService.commitCt);
            continue;
            // throw new Exception("Error - lookup returned no object: " +
            // fromClass.getSimpleName() + " with terminologyId=" +
            // fromTerminologyId
            // + ", terminology=" + fromTerminology + ", version=" +
            // fromVersion);
          }

          // Compute attribute identity
          String newAttributeAtui =
              handler.getTerminologyId(newAttribute, containerComponent);

          // Check to see if attribute with matching ATUI already exists in the
          // database
          Long oldAttributeId = atuiIdMap.get(newAttributeAtui);

          // If no attribute with the same ATUI exists, create this new
          // Attribute, and add it to its containing component
          if (oldAttributeId == null) {
            newAttribute.getAlternateTerminologyIds()
                .put(getProject().getTerminology() + "-SRC", newAttributeAtui);
            newAttribute = addAttribute(newAttribute, containerComponent);

            attributeAddCount++;
            atuiIdMap.put(newAttributeAtui, newAttribute.getId());

            // TODO - find out if this is needed.
            // If so, create a cache-map, so that all updates are made to same
            // copy of the component
            // // Add the attribute to component
            // containerComponent.getAttributes().add(newAttribute);
            // updateComponent(containerComponent);

          }
          // If a previous attribute with same ATUI exists, load that object.
          else {
            final Attribute oldAttribute = getAttribute(oldAttributeId);

            boolean oldAttributeChanged = false;

            // Create an "alternateTerminologyId" for the attribute
            oldAttribute.getAlternateTerminologyIds()
                .put(getProject().getTerminology() + "-SRC", newAttributeAtui);

            // Update the version
            if (!oldAttribute.getVersion().equals(newAttribute.getVersion())) {
              oldAttribute.setVersion(newAttribute.getVersion());
              oldAttributeChanged = true;
            }

            // If the existing relationship doesn't exactly equal the new one,
            // update obsolete, and suppressible
            if (!oldAttribute.equals(newAttribute)) {
              oldAttribute.setObsolete(newAttribute.isObsolete());
              oldAttribute.setSuppressible(newAttribute.isSuppressible());
            }

            if (oldAttributeChanged) {
              updateAttribute(oldAttribute, containerComponent);
              attributeUpdateCount++;
            }
          }
        }

        // Update the progress
        updateProgress();

        logAndCommit("[Attribute Loader] Attributes processed ", stepsCompleted,
            RootService.logCt, RootService.commitCt);
      }

      commitClearBegin();
      handler.commitClearBegin();

      logInfo(
          "[AttributeLoader] Added " + attributeAddCount + " new Attributes.");
      logInfo("[AttributeLoader] Updated " + attributeUpdateCount
          + " existing Attributes.");
      logInfo("[AttributeLoader] Added " + definitionAddCount
          + " new Definitions.");
      logInfo("[AttributeLoader] Updated " + definitionUpdateCount
          + " existing Definitions.");

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished ATTRIBUTELOADING");

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

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
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      fireProgressEvent(currentProgress,
          "ATTRIBUTELOADING progress: " + currentProgress + "%");
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