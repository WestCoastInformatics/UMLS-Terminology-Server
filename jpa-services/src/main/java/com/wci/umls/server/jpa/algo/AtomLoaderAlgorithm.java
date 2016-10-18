/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

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
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Implementation of an algorithm to import atoms.
 */
public class AtomLoaderAlgorithm extends AbstractAlgorithm {

  /** The directory (relative to source.data.dir). */
  private String directory = null;

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /**
   * The aui ID map. Key = AlternateTerminologyId Value = atomJpa Id
   */
  private Map<String, Long> auiIdMap = new HashMap<>();

  /**
   * The loaded termTypes. Key = abbreviation Value = TermType object
   */
  private Map<String, TermType> loadedTermTypes =
      new HashMap<String, TermType>();

  /**
   * The loaded terminologies. Key = Terminology_Version (or just Terminology,
   * if Version = "latest") Value = Terminology object
   */
  private Map<String, Terminology> loadedTerminologies =
      new HashMap<String, Terminology>();

  /**
   * The code ID map. Key = CodeJpa.TerminologyId Value = CodeJpa.Id
   */
  private Map<String, Long> codeIdMap = new HashMap<String, Long>();

  /**
   * The concept ID map. Key = ConceptJpa.TerminologyId Value = ConceptJpa.Id
   */
  private Map<String, Long> conceptIdMap = new HashMap<String, Long>();

  /**
   * The descriptor ID map. Key = DescriptorJpa.TerminologyId Value =
   * DescriptorJpa.Id
   */
  private Map<String, Long> descriptorIdMap = new HashMap<String, Long>();

  /**
   * Set containing the name of all terminologies referenced in the
   * classes_atoms.src file
   */
  private Set<String> allTerminologiesFromInsertion = new HashSet<>();

  /**
   * Instantiates an empty {@link AtomLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public AtomLoaderAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("ATOMLOADER");
    setLastModifiedBy("admin");
  }

  /**
   * Sets the directory.
   *
   * @param directory the directory
   */
  public void setDirectory(String directory) {
    this.directory = directory;
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
      throw new Exception("Atom Loading requires a project to be set");
    }
    if (directory == null) {
      throw new Exception("Atom Loading requires a directory to be set.");
    }

    // Check the input directories

    String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + directory;

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
  @Override
  public void compute() throws Exception {
    logInfo("Starting ATOMLOADING");

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    // Set up the handler for identifier assignment
    final IdentifierAssignmentHandler handler =
        newIdentifierAssignmentHandler(getProject().getTerminology());
    handler.setTransactionPerOperation(false);
    handler.beginTransaction();

    // Count number of added and updated Atoms, for logging
    int addCount = 0;
    int updateCount = 0;

    try {

      //
      // Load the classes_atoms.src file
      //
      List<String> lines =
          loadFileIntoStringList("classes_atoms.src", null);

      logInfo("[AtomLoader] Loading associated resources");

      // Cache all currently existing objects
      cacheExistingTermTypes();
      cacheExistingTerminologies();
      identifyAllTerminologiesFromInsertion(lines);
      cacheExistingCodes();
      cacheExistingConcepts();
      cacheExistingDescriptors();
      cacheExistingAtoms();

      logInfo("[AtomLoader] Checking for new/updated Atoms");

      // Set the number of steps to the number of atoms to be processed
      steps = lines.size();

      previousProgress = 0;
      stepsCompleted = 0;

      String fields[] = new String[15];

      // Each line of classes_atoms.src corresponds to one atom.
      // Check to make sure the atom doesn't already exist in the database
      // If it does, skip it.
      // If it does not, add it.
      for (String line : lines) {

        // Print progress and check for a cancelled call once every 100 atoms
        // (doing it for every atom
        // makes things too slow)
        if (stepsCompleted % 100 == 0) {
          if (isCancelled()) {
            throw new CancelException("Cancelled");
          }
        }

        FieldedStringTokenizer.split(line, "|", 15, fields);

        // Fields:
        // 0 src_atom_id (Atom.alternateTerminologlyId(), where Key =
        // ProjectTerminology + "-SRC")
        // 1 source (Atom.terminology, Atom.version)
        // 2 termgroup (Atom.termType (portion after the forward-slash))
        // 3 code (Atom.codeId)
        // 4 status (Atom.WorkflowStatus)
        // 5 tobereleased (Atom.publishable)
        // 6 released (Atom.published)
        // 7 atom_name (Atom.name)
        // 8 suppressible (Atom.suppresible, Atom.obsolete)
        // obsolete = true IFF "O".
        // suppresible = true IFF "O, Y, E";
        // 9 source_aui (Atom.terminologyId)
        // 10 source_cui (Atom.conceptId)
        // 11 source_dui (Atom.descriptorId)
        // 12 language (Atom.language)
        // 13 order_id (Atom.alternateTerminologlyId(), where Key =
        // ProjectTerminology + "-ORDER")
        // 14 last_release_cui (Atom.conceptTerminologyId(), where
        // Key=ProjectTerminology)

        // e.g.
        // 362166319|NCI_2016_05E|NCI_2016_05E/PT|C28777|R|Y|N|1,9-Nonanediol|N||
        // C28777||ENG|362166319|

        //
        // Atom based on input line.
        //
        Atom newAtom = new AtomJpa();
        if (!ConfigUtility.isEmpty(fields[0])) {
          newAtom.getAlternateTerminologyIds()
              .put(getProject().getTerminology() + "-SRC", fields[0]);
        }
        Terminology term = loadedTerminologies.get(fields[1]);
        if (term == null) {
          throw new Exception(
              "ERROR: lookup for " + fields[1] + " returned no terminology");
        } else {
          newAtom.setTerminology(term.getTerminology());
          newAtom.setVersion(term.getVersion());
        }
        newAtom.setTermType(fields[2].substring(fields[2].indexOf("/") + 1));
        newAtom.setCodeId(fields[3]);
        if (fields[4].equals("N")) {
          newAtom.setWorkflowStatus(WorkflowStatus.NEEDS_REVIEW);
        }
        if (fields[4].equals("R")) {
          newAtom.setWorkflowStatus(WorkflowStatus.READY_FOR_PUBLICATION);
        }
        newAtom
            .setPublishable((fields[5].equals("Y") || fields[5].equals("y")));
        newAtom.setPublished((fields[6].equals("Y")));
        newAtom.setName(fields[7]);
        newAtom.setSuppressible((fields[8].equals("Y")));
        newAtom.setObsolete("OYE".contains(fields[8]));
        newAtom.setTerminologyId(fields[9]);
        newAtom.setConceptId(fields[10]);
        newAtom.setDescriptorId(fields[11]);
        newAtom.setLanguage(fields[12]);
        if (!ConfigUtility.isEmpty(fields[13])) {
          newAtom.getAlternateTerminologyIds()
              .put(getProject().getTerminology() + "-ORDER", fields[13]);
        }
        if (!ConfigUtility.isEmpty(fields[14])) {
          newAtom.getConceptTerminologyIds().put(getProject().getTerminology(),
              fields[14]);
        }

        // Add string and lexical classes to get assign their Ids
        final StringClass strClass = new StringClassJpa();
        strClass.setLanguage(newAtom.getLanguage());
        strClass.setName(newAtom.getName());
        newAtom.setStringClassId(handler.getTerminologyId(strClass));

        // Get normalization handler
        final LexicalClass lexClass = new LexicalClassJpa();
        lexClass.setLanguage(newAtom.getLanguage());
        lexClass.setNormalizedName(getNormalizedString(newAtom.getName()));
        newAtom.setLexicalClassId(handler.getTerminologyId(lexClass));

        // Compute atom identity
        String newAtomAui = handler.getTerminologyId(newAtom);

        // Check to see if atom with matching AUI already exists in the database
        Long oldAtomId = auiIdMap.get(newAtomAui);

        // If no atom with the same AUI exists, add this new Atom.
        if (oldAtomId == null) {
          newAtom.getAlternateTerminologyIds().put("SRC", newAtomAui);
          newAtom = addAtom(newAtom);
          addCount++;
          auiIdMap.put(newAtomAui, newAtom.getId());

          // Reconcile code/concept/descriptor
          reconcileCodeConceptDescriptor(newAtom);

        }
        // If a previous atom with same AUI exists, load that object.
        else {
          final Atom oldAtom = getAtom(oldAtomId);

          boolean oldAtomChanged = false;

          // Create an "alternateTerminologyId" for the atom
          oldAtom.getAlternateTerminologyIds().put("UMLS-SRC", newAtomAui);

          // Update the version
          if (!oldAtom.getVersion().equals(newAtom.getVersion())) {
            oldAtom.setVersion(newAtom.getVersion());
            oldAtomChanged = true;
          }

          // If this loaded Atom is not exactly the same as the new Atom:
          if (!oldAtom.equals(newAtom)) {

            // Update obsolete and suppresible.
            // If the old version of the atom is suppresible, and its term type
            // is not, keep the old atom's suppresibility. Otherwise, use the
            // new Atom's suppresible value.
            TermType atomTty = loadedTermTypes.get(oldAtom.getTermType());
            if (oldAtom.isSuppressible() != newAtom.isSuppressible()
                && !(oldAtom.isSuppressible() && !atomTty.isSuppressible())) {
              oldAtom.setSuppressible(newAtom.isSuppressible());
              oldAtomChanged = true;
            }
            if (oldAtom.isObsolete() != newAtom.isObsolete()
                && !(oldAtom.isObsolete() && !atomTty.isObsolete())) {
              oldAtom.setObsolete(newAtom.isObsolete());
              oldAtomChanged = true;
            }
          }

          if (oldAtomChanged) {
            updateAtom(oldAtom);
            updateCount++;
          }
        }

        // Update the progress
        updateProgress();

        logAndCommit("[Atom Loader] Atoms processed ", stepsCompleted,
            RootService.logCt, RootService.commitCt);
        handler.commitClearBegin();

      }

      commitClearBegin();
      handler.commitClearBegin();

      logInfo("[AtomLoader] Added " + addCount + " new Atoms.");
      logInfo("[AtomLoader] Updated " + updateCount + " existing Atoms.");

      //
      // Load the atoms from classes_atoms.src
      //

      logInfo("  project = " + getProject().getId());
      logInfo("  workId = " + getWorkId());
      logInfo("  activityId = " + getActivityId());
      logInfo("  user  = " + getLastModifiedBy());
      logInfo("Finished ATOMLOADING");

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /**
   * Reconcile code concept descriptor.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  private void reconcileCodeConceptDescriptor(Atom atom) throws Exception {
    // Check map to see if code already exists
    if (!atom.getCodeId().isEmpty()) {

      if (codeIdMap.containsKey(atom.getCodeId() + atom.getTerminology())) {
        final Code code =
            getCode(codeIdMap.get(atom.getCodeId() + atom.getTerminology()));
        code.getAtoms().add(atom);
        code.setVersion(atom.getVersion());
        updateCode(code);
      }

      // else create a new code
      else {
        final Code code = new CodeJpa();
        code.setTerminology(atom.getTerminology());
        code.setTerminologyId(atom.getCodeId());
        code.setVersion(atom.getVersion());
        code.setBranch(Branch.ROOT);
        code.setName(atom.getName());
        code.setObsolete(false);
        code.setPublished(false);
        code.setPublishable(true);
        code.setSuppressible(false);
        code.setWorkflowStatus(atom.getWorkflowStatus());

        code.getAtoms().add(atom);
        addCode(code);
        codeIdMap.put(code.getTerminologyId() + code.getTerminology(),
            code.getId());
      }
    }

    // Check map to see if concept already exists
    if (!atom.getConceptId().isEmpty()) {

      if (conceptIdMap
          .containsKey(atom.getConceptId() + atom.getTerminology())) {
        final Concept concept = getConcept(
            conceptIdMap.get(atom.getConceptId() + atom.getTerminology()));
        concept.getAtoms().add(atom);
        concept.setVersion(atom.getVersion());
        updateConcept(concept);
      }

      // else create a new concept
      else {
        final Concept concept = new ConceptJpa();
        concept.setTerminology(atom.getTerminology());
        concept.setTerminologyId(atom.getConceptId());
        concept.setVersion(atom.getVersion());
        concept.setBranch(Branch.ROOT);
        concept.setName(atom.getName());
        concept.setObsolete(false);
        concept.setPublished(false);
        concept.setPublishable(true);
        concept.setSuppressible(false);
        concept.setWorkflowStatus(atom.getWorkflowStatus());

        concept.getAtoms().add(atom);
        addConcept(concept);
        conceptIdMap.put(concept.getTerminologyId() + concept.getTerminology(),
            concept.getId());
      }
    }
    // Check map to see if descriptor already exists
    if (!atom.getDescriptorId().isEmpty()) {

      if (descriptorIdMap
          .containsKey(atom.getDescriptorId() + atom.getTerminology())) {
        final Descriptor descriptor = getDescriptor(descriptorIdMap
            .get(atom.getDescriptorId() + atom.getTerminology()));
        descriptor.getAtoms().add(atom);
        descriptor.setVersion(atom.getVersion());
        updateDescriptor(descriptor);
      }

      // else create a new descriptor
      else {
        final Descriptor descriptor = new DescriptorJpa();
        descriptor.setTerminology(atom.getTerminology());
        descriptor.setTerminologyId(atom.getDescriptorId());
        descriptor.setVersion(atom.getVersion());
        descriptor.setBranch(Branch.ROOT);
        descriptor.setName(atom.getName());
        descriptor.setObsolete(false);
        descriptor.setPublished(false);
        descriptor.setPublishable(true);
        descriptor.setSuppressible(false);
        descriptor.setWorkflowStatus(atom.getWorkflowStatus());

        descriptor.getAtoms().add(atom);
        addDescriptor(descriptor);
        descriptorIdMap.put(
            descriptor.getTerminologyId() + descriptor.getTerminology(),
            descriptor.getId());
      }
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
   * Cache existing atoms' AUIs and IDs.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingAtoms() throws Exception {

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        // "select alternateTerminologyIds, AtomJpa_id from
        // atomjpa_alternateterminologyids";
        "select b.alternateTerminologyIds, a.id from atoms a join atomjpa_alternateterminologyids b where a.id = b.AtomJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[AtomLoader] Loading atom AUIs from database: "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        auiIdMap.put(result[0].toString(), Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);
  }

  /**
   * Cache existing termTypes.
   *
   * @throws Exception the exception
   */
  private void cacheExistingTermTypes() throws Exception {

    for (final TermType tty : getTermTypes(getProject().getTerminology(),
        getProject().getVersion()).getObjects()) {
      // lazy init
      tty.getNameVariantType().toString();
      tty.getCodeVariantType().toString();
      tty.getStyle().toString();
      loadedTermTypes.put(tty.getAbbreviation(), tty);
    }
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
   * Cache existing descriptors.
   *
   * @throws Exception the exception
   */
  private void cacheExistingDescriptors() throws Exception {

    // Pre-populate descriptorIdMap (for all terminologies from this insertion)
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.terminology, c.id "
            + "from DescriptorJpa c where terminology in :terminologies");
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
          "ATOMLOADING progress: " + currentProgress + "%");
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

    directory = String.valueOf(p.getProperty("directory"));

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
    AlgorithmParameter param = new AlgorithmParameterJpa("Directory",
        "directory", "Directory of input files, relative to source.data.dir.",
        "e.g. terminologies/NCI_INSERT", 2000, AlgorithmParameter.Type.STRING);
    params.add(param);

    return params;
  }

}