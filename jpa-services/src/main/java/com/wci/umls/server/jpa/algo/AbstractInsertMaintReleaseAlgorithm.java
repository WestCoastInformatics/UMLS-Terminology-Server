/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Query;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * Abstract support for source-file insertion algorithms.
 */
public abstract class AbstractInsertMaintReleaseAlgorithm
    extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link AbstractInsertMaintReleaseAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractInsertMaintReleaseAlgorithm() throws Exception {
    // n/a
  }

  /** The search handler. */
  public SearchHandler searchHandler = getSearchHandler(ConfigUtility.DEFAULT);

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The previous progress. */
  private int previousProgress = 0;

  /** The progress check, indicates when to check the progress monitor. */
  private int progressCheck = 0;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted = 0;

  /**
   * The atom ID cache. Key = AUI; Value = atomJpa Id
   */
  private static Map<String, Long> atomIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their atoms loaded and cached.
   */
  private static Set<String> atomCachedTerms = new HashSet<>();

  /**
   * The attribute ID cache. Key = ATUI Value = attributeJpa Id
   */
  private static Map<String, Long> attributeIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their attributes loaded and cached.
   */
  private static Set<String> attributeCachedTerms = new HashSet<>();

  /**
   * The definition ID cache. Key = DUI Value = definitionJpa Id
   */
  private static Map<String, Long> definitionIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their definitions loaded and
   * cached.
   */
  private static Set<String> definitionCachedTerms = new HashSet<>();

  /**
   * The relationship ID cache. Key = AlternateTerminologyId; Value =
   * relationship Id
   */
  private static Map<String, Long> relIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their relationships loaded and
   * cached.
   */
  private static Set<String> relCachedTerms = new HashSet<>();

  /**
   * The concept ID cache. Key = terminologyId + terminology; Value =
   * ConceptJpa.Id
   */
  private static Map<String, Long> conceptIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their concepts loaded and cached.
   */
  private static Set<String> conceptCachedTerms = new HashSet<>();

  /**
   * The code ID cache. Key = terminologyId + terminology; Value = CodeJpa.Id
   */
  private static Map<String, Long> codeIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their codes loaded and cached.
   */
  private static Set<String> codeCachedTerms = new HashSet<>();

  /**
   * The descriptor ID cache. Key = terminologyId + terminology; Value =
   * DescriptorJpa.Id
   */
  private static Map<String, Long> descriptorIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their descriptors loaded and
   * cached.
   */
  private static Set<String> descriptorCachedTerms = new HashSet<>();

  /**
   * The cached termTypes. Key = abbreviation Value = TermType object
   */
  private static Map<String, TermType> cachedTermTypes = new HashMap<>();

  /**
   * The cached termTypes. Key = abbreviation Value = AdditionalRelationshipType
   * object
   */
  private static Map<String, AdditionalRelationshipType> cachedAdditionalRelationshipTypes =
      new HashMap<>();

  /**
   * The cached termTypes. Key = abbreviation Value = AttributeName object
   */
  private static Map<String, AttributeName> cachedAttributeNames =
      new HashMap<>();

  /**
   * The cached root terminologies. Key = Terminology; Value = Root Terminology
   * object
   */
  private static Map<String, RootTerminology> cachedRootTerminologies =
      new HashMap<>();

  /**
   * The cached terminologies. Key = Terminology + "_" + Version (or just
   * Terminology, if Version = "latest") Value = Terminology object
   */
  private static Map<String, Terminology> cachedTerminologies = new HashMap<>();

  /**
   * Load file into string list.
   *
   * @param srcDirFile the src dir file
   * @param fileName the file name
   * @param keepRegexFilter the regex filter
   * @param skipRegexFilter the skip regex filter
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public List<String> loadFileIntoStringList(File srcDirFile, String fileName,
    String keepRegexFilter, String skipRegexFilter) throws Exception {
    final String sourcesFile = srcDirFile + File.separator + fileName;
    BufferedReader sources = null;
    try {
      sources = new BufferedReader(new FileReader(sourcesFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + sourcesFile);
    }

    final List<String> lines = new ArrayList<>();
    String linePre = null;
    while ((linePre = sources.readLine()) != null) {
      linePre = linePre.replace("\r", "");
      // Filter rows if defined
      if (ConfigUtility.isEmpty(keepRegexFilter)
          && ConfigUtility.isEmpty(skipRegexFilter)) {
        lines.add(linePre);
      } else if (!ConfigUtility.isEmpty(keepRegexFilter)
          && ConfigUtility.isEmpty(skipRegexFilter)) {
        if (linePre.matches(keepRegexFilter)) {
          lines.add(linePre);
        }
      } else if (ConfigUtility.isEmpty(keepRegexFilter)
          && !ConfigUtility.isEmpty(skipRegexFilter)) {
        if (!linePre.matches(skipRegexFilter)) {
          lines.add(linePre);
        }
      } else if (!ConfigUtility.isEmpty(keepRegexFilter)
          && !ConfigUtility.isEmpty(skipRegexFilter)) {
        if (linePre.matches(keepRegexFilter)
            && !linePre.matches(skipRegexFilter)) {
          lines.add(linePre);
        }
      }
    }

    sources.close();

    return lines;
  }

  /**
   * Cache existing atoms' alternateTerminologyIds and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingAtomIds(String terminology) throws Exception {

    // Load alternateTerminologyIds
    Query jpaQuery = getEntityManager().createQuery(
        "select value(b), a.id from AtomJpa a join a.alternateTerminologyIds b where KEY(b) = :terminology and a.publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    List<Object[]> list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String alternateTerminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      atomIdCache.put(alternateTerminologyId, id);
    }

    // Load terminologyIds
    jpaQuery = getEntityManager().createQuery(
        "select a.terminologyId, a.id from AtomJpa a WHERE a.terminology = :terminology AND a.terminologyId <> '' and a.publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String terminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      atomIdCache.put(terminologyId, id);
    }

    logInfo("[SourceLoader] Loading Atom Ids from database for terminology "
        + terminology);

    // Add this terminology to the cached set.
    atomCachedTerms.add(terminology);
  }

  /**
   * Cache existing attributes' ATUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingAttributeIds(String terminology) throws Exception {

    final Query jpaQuery = getEntityManager().createQuery(
        "select value(b), a.id from AttributeJpa a join a.alternateTerminologyIds b where KEY(b) = :terminology and a.publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    logInfo(
        "[SourceLoader] Loading attribute alternate Terminology Ids from database for terminology "
            + terminology);

    final List<Object[]> list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String terminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      attributeIdCache.put(terminologyId, id);
    }

    // Add this terminology to the cached set.
    attributeCachedTerms.add(terminology);
  }

  /**
   * Cache existing definitions' ATUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingDefinitionIds(String terminology) throws Exception {

    final Query jpaQuery = getEntityManager().createQuery(
        "select value(b), a.id from DefinitionJpa a join a.alternateTerminologyIds b where KEY(b) = :terminology and a.publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    logInfo(
        "[SourceLoader] Loading definition alternate Terminology Ids from database for terminology "
            + terminology);

    final List<Object[]> list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String terminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      definitionIdCache.put(terminologyId, id);
    }

    // Add this terminology to the cached set.
    definitionCachedTerms.add(terminology);
  }

  /**
   * Cache existing relationships' RUIs and IDs.
   *
   * @param altTerminologyKey the alt terminology key
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingRelationshipIds(String altTerminologyKey,
    String terminology) throws Exception {

    logInfo(
        "[SourceLoader] Loading relationship Terminology Ids from database for terminology "
            + terminology);

    final List<String> relationshipPrefixes =
        Arrays.asList("Atom", "Code", "Concept", "Descriptor", "ComponentInfo");

    // Get RUIs for ConceptRelationships, CodeRelationships, and
    // ComponentInfoRelationships.
    for (String relPrefix : relationshipPrefixes) {
      final Query jpaQuery = getEntityManager()
          .createQuery("select value(b), a.id from " + relPrefix
              + "RelationshipJpa a join a.alternateTerminologyIds b "
              + "where KEY(b)  = :projectTerminology and "
              + "a.terminology = :terminology and a.publishable=true");
      jpaQuery.setParameter("terminology", terminology);

      logInfo("[SourceLoader] Loading " + relPrefix
          + " Terminology Ids from database for terminology " + terminology);

      final List<Object[]> list = jpaQuery.getResultList();
      for (final Object[] entry : list) {
        final String terminologyId = entry[0].toString();
        final Long id = Long.valueOf(entry[1].toString());
        relIdCache.put(terminologyId, id);
      }
    }

    // Add this terminology to the cached set.
    relCachedTerms.add(altTerminologyKey + terminology);
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
      cachedTermTypes.put(tty.getAbbreviation(), tty);
    }
  }

  /**
   * Cache existing additional relationship types.
   *
   * @throws Exception the exception
   */
  private void cacheExistingAdditionalRelationshipTypes() throws Exception {

    for (final AdditionalRelationshipType rela : getAdditionalRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      cachedAdditionalRelationshipTypes.put(rela.getAbbreviation(), rela);
    }
  }

  /**
   * Cache existing attributeNames.
   *
   * @throws Exception the exception
   */
  private void cacheExistingAttributeNames() throws Exception {

    for (final AttributeName atn : getAttributeNames(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      cachedAttributeNames.put(atn.getAbbreviation(), atn);
    }
  }

  /**
   * Cache existing root terminologies. Key = Terminology_Version, or just
   * Terminology if version = "latest"
   *
   * @throws Exception the exception
   */
  public void cacheExistingRootTerminologies() throws Exception {

    for (final RootTerminology root : getRootTerminologies().getObjects()) {
      // lazy init
      root.getSynonymousNames().size();
      cachedRootTerminologies.put(root.getTerminology(), root);
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
        cachedTerminologies.put(term.getTerminology(), term);
      } else {
        cachedTerminologies.put(term.getTerminology() + "_" + term.getVersion(),
            term);
      }
    }
  }

  /**
   * Cache existing codes.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingCodeIds(String terminology) throws Exception {

    final Query jpaQuery =
        getEntityManager().createQuery("select c.terminologyId, c.id "
            + "from CodeJpa c where terminology = :terminology AND publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    logInfo(
        "[SourceLoader] Loading code Terminology Ids from database for terminology "
            + terminology);

    final List<Object[]> list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String terminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      codeIdCache.put(terminologyId + terminology, id);
    }

    // Add this terminology to the cached set.
    codeCachedTerms.add(terminology);
  }

  /**
   * Cache existing concept Ids.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingConceptIds(String terminology) throws Exception {

    final Query jpaQuery =
        getEntityManager().createQuery("select c.terminologyId, c.id "
            + "from ConceptJpa c where terminology = :terminology AND publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    logInfo(
        "[SourceLoader] Loading concept Terminology Ids from database for terminology "
            + terminology);

    final List<Object[]> list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String terminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      conceptIdCache.put(terminologyId + terminology, id);
    }

    // Add this terminology to the cached set.
    conceptCachedTerms.add(terminology);
  }

  /**
   * Cache existing descriptors.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingDescriptorIds(String terminology) throws Exception {

    final Query jpaQuery =
        getEntityManager().createQuery("select c.terminologyId, c.id "
            + "from DescriptorJpa c where terminology = :terminology AND publishable=true");
    jpaQuery.setParameter("terminology", terminology);

    logInfo(
        "[SourceLoader] Loading descriptor Terminology Ids from database for terminology "
            + terminology);

    final List<Object[]> list = jpaQuery.getResultList();
    for (final Object[] entry : list) {
      final String terminologyId = entry[0].toString();
      final Long id = Long.valueOf(entry[1].toString());
      descriptorIdCache.put(terminologyId + terminology, id);
    }

    // Add this terminology to the cached set.
    descriptorCachedTerms.add(terminology);
  }

  /**
   * Put component.
   *
   * @param component the component
   * @param terminologyId the terminology id
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public void putComponent(Component component, String terminologyId)
    throws Exception {
    if (component instanceof Atom) {
      atomIdCache.put(terminologyId, component.getId());
    } else if (component instanceof Attribute) {
      attributeIdCache.put(terminologyId, component.getId());
    } else if (component instanceof Relationship) {
      relIdCache.put(terminologyId, component.getId());
    } else if (component instanceof Concept) {
      conceptIdCache.put(terminologyId + component.getTerminology(),
          component.getId());
    } else if (component instanceof Code) {
      codeIdCache.put(terminologyId + component.getTerminology(),
          component.getId());
    } else if (component instanceof Definition) {
      definitionIdCache.put(terminologyId, component.getId());
    } else if (component instanceof Descriptor) {
      descriptorIdCache.put(terminologyId + component.getTerminology(),
          component.getId());
    }

    else {
      throw new Exception("ERROR: " + component.getClass().getName()
          + " is an unhandled type.");
    }
  }

  /**
   * Returns the component.
   *
   * @param type the type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param relClass the rel class
   * @return the component
   * @throws Exception the exception
   */
  public Component getComponent(String type, String terminologyId,
    String terminology, Class<? extends Relationship<?, ?>> relClass)
    throws Exception {

    if (type.equals("AUI")) {
      if (!atomCachedTerms.contains(getProject().getTerminology())) {
        cacheExistingAtomIds(getProject().getTerminology());
      }
      return getComponent(atomIdCache.get(terminologyId), AtomJpa.class);
    }

    else if (type.equals("SRC_ATOM_ID")) {
      if (!atomCachedTerms.contains(getProject().getTerminology() + "-SRC")) {
        cacheExistingAtomIds(getProject().getTerminology() + "-SRC");
      }
      return getComponent(atomIdCache.get(terminologyId), AtomJpa.class);
    }

    else if (type.equals("SOURCE_AUI")) {
      if (!atomCachedTerms.contains(terminology)) {
        cacheExistingAtomIds(terminology);
      }
      return getComponent(atomIdCache.get(terminologyId), AtomJpa.class);
    }

    else if (type.equals("ATUI")) {
      if (!attributeCachedTerms.contains(getProject().getTerminology())) {
        cacheExistingAttributeIds(getProject().getTerminology());
      }
      return getComponent(attributeIdCache.get(terminologyId),
          AttributeJpa.class);
    }

    else if (type.equals("CODE_SOURCE")) {
      if (!codeCachedTerms.contains(terminology)) {
        cacheExistingCodeIds(terminology);
      }
      return getComponent(codeIdCache.get(terminologyId + terminology),
          CodeJpa.class);
    }

    else if (type.equals("SOURCE_CUI")) {
      if (!conceptCachedTerms.contains(terminology)) {
        cacheExistingConceptIds(terminology);
      }
      return getComponent(conceptIdCache.get(terminologyId + terminology),
          ConceptJpa.class);
    }

    else if (type.equals("DEFINITION")) {
      if (!definitionCachedTerms.contains(getProject().getTerminology())) {
        cacheExistingDefinitionIds(getProject().getTerminology());
      }
      return getComponent(definitionIdCache.get(terminologyId),
          DefinitionJpa.class);
    }

    else if (type.equals("SOURCE_DUI")) {
      if (!descriptorCachedTerms.contains(terminology)) {
        cacheExistingDescriptorIds(terminology);
      }
      return getComponent(descriptorIdCache.get(terminologyId + terminology),
          DescriptorJpa.class);
    }

    else if (type.equals("RUI")) {
      if (!relCachedTerms
          .contains(getProject().getTerminology() + terminology)) {
        cacheExistingRelationshipIds(getProject().getTerminology(),
            terminology);
      }

      return getComponent(relIdCache.get(terminologyId), relClass);
    }

    else if (type.equals("SRC_REL_ID")) {
      if (!relCachedTerms
          .contains(getProject().getTerminology() + "-SRC" + terminology)) {
        cacheExistingRelationshipIds(getProject().getTerminology() + "-SRC",
            terminology);
      }

      return getComponent(relIdCache.get(terminologyId), relClass);
    }

    else {
      throw new Exception("ERROR: " + type + " is an unhandled idType.");
    }

  }

  /**
   * Returns the src dir file.
   *
   * @return the src dir file
   */
  public File getSrcDirFile() {
    return srcDirFile;
  }

  /**
   * Sets the src dir file.
   *
   * @param srcDirFile the src dir file
   */
  public void setSrcDirFile(File srcDirFile) {
    this.srcDirFile = srcDirFile;
  }

  /**
   * Returns the previous progress.
   *
   * @return the previous progress
   */
  public int getPreviousProgress() {
    return previousProgress;
  }

  /**
   * Sets the previous progress.
   *
   * @param previousProgress the previous progress
   */
  public void setPreviousProgress(int previousProgress) {
    this.previousProgress = previousProgress;
  }

  /**
   * Returns the steps.
   *
   * @return the steps
   */
  public int getSteps() {
    return steps;
  }

  /**
   * Sets the steps.
   *
   * @param steps the steps
   */
  public void setSteps(int steps) {
    this.steps = steps;
    this.progressCheck = (int) ((steps + 1 / 99.0));
  }

  /**
   * Returns the steps completed.
   *
   * @return the steps completed
   */
  public int getStepsCompleted() {
    return stepsCompleted;
  }

  /**
   * Sets the steps completed.
   *
   * @param stepsCompleted the steps completed
   */
  public void setStepsCompleted(int stepsCompleted) {
    this.stepsCompleted = stepsCompleted;
  }

  /**
   * Returns the cached root terminologies.
   *
   * @return the cached root terminologies
   * @throws Exception the exception
   */
  public Map<String, RootTerminology> getCachedRootTerminologies()
    throws Exception {
    if (cachedRootTerminologies.isEmpty()) {
      cacheExistingRootTerminologies();
    }

    return cachedRootTerminologies;
  }

  /**
   * Returns the id.
   *
   * @param terminology the terminology
   * @return the id
   * @throws Exception the exception
   */
  public RootTerminology getCachedRootTerminology(String terminology)
    throws Exception {

    if (cachedRootTerminologies.isEmpty()) {
      cacheExistingRootTerminologies();
    }

    return cachedRootTerminologies.get(terminology);
  }

  /**
   * Returns the cached terminologies.
   *
   * @return the cached terminologies
   * @throws Exception the exception
   */
  public Map<String, Terminology> getCachedTerminologies() throws Exception {
    if (cachedTerminologies.isEmpty()) {
      cacheExistingTerminologies();
    }

    return cachedTerminologies;
  }

  /**
   * Returns the id.
   *
   * @param terminologyAndVersion the terminology and version
   * @return the id
   * @throws Exception the exception
   */
  public Terminology getCachedTerminology(String terminologyAndVersion)
    throws Exception {

    if (cachedTerminologies.isEmpty()) {
      cacheExistingTerminologies();
    }

    // terminologyAndVersion strings can sometimes be prefixed with "E-", "L-",
    // etc.
    // Strip these out if found.
    if (terminologyAndVersion.matches("^[A-Z]\\-(.*)")) {
      return cachedTerminologies.get(terminologyAndVersion.substring(2));
    } else {
      return cachedTerminologies.get(terminologyAndVersion);
    }
  }

  /**
   * Returns the cached terminology name.
   *
   * @param terminologyAndVersion the terminology and version
   * @return the cached terminology name
   * @throws Exception the exception
   */
  public String getCachedTerminologyName(String terminologyAndVersion)
    throws Exception {

    final Terminology terminology = getCachedTerminology(terminologyAndVersion);
    if (terminology == null) {
      return null;
    } else {
      return terminology.getTerminology();
    }

  }

  /**
   * Returns the id.
   *
   * @param terminology the terminology
   * @param version the version
   * @return the id
   * @throws Exception the exception
   */
  public Terminology getCachedTerminology(String terminology, String version)
    throws Exception {

    if (cachedTerminologies.isEmpty()) {
      cacheExistingTerminologies();
    }

    if (version.equals("latest")) {
      return cachedTerminologies.get(terminology);
    } else {
      return cachedTerminologies.get(terminology + "_" + version);
    }
  }

  /**
   * Returns the id.
   *
   * @param abbreviation the abbreviation
   * @return the id
   * @throws Exception the exception
   */
  public TermType getCachedTermType(String abbreviation) throws Exception {

    if (cachedTermTypes.isEmpty()) {
      cacheExistingTermTypes();
    }

    return cachedTermTypes.get(abbreviation);
  }

  /**
   * Returns the cached attribute names.
   *
   * @return the cached attribute names
   * @throws Exception the exception
   */
  public Map<String, AttributeName> getCachedAttributeNames() throws Exception {
    if (cachedAttributeNames.isEmpty()) {
      cacheExistingAttributeNames();
    }

    return cachedAttributeNames;
  }

  /**
   * Returns the cached attribute name.
   *
   * @param abbreviation the abbreviation
   * @return the cached attribute name
   * @throws Exception the exception
   */
  public AttributeName getCachedAttributeName(String abbreviation)
    throws Exception {

    if (cachedAttributeNames.isEmpty()) {
      cacheExistingAttributeNames();
    }

    return cachedAttributeNames.get(abbreviation);
  }

  /**
   * Returns the cached additional relationship types.
   *
   * @return the cached additional relationship types
   * @throws Exception the exception
   */
  public Map<String, AdditionalRelationshipType> getCachedAdditionalRelationshipTypes()
    throws Exception {
    if (cachedAdditionalRelationshipTypes.isEmpty()) {
      cacheExistingAdditionalRelationshipTypes();
    }

    return cachedAdditionalRelationshipTypes;
  }

  /**
   * Returns the cached additional relationship type.
   *
   * @param abbreviation the abbreviation
   * @return the cached additional relationship type
   * @throws Exception the exception
   */
  public AdditionalRelationshipType getCachedAdditionalRelationshipType(
    String abbreviation) throws Exception {

    if (cachedAdditionalRelationshipTypes.isEmpty()) {
      cacheExistingAdditionalRelationshipTypes();
    }

    return cachedAdditionalRelationshipTypes.get(abbreviation);
  }

  /**
   * Lookup workflow status.
   *
   * @param string the string
   * @return the workflow status
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public WorkflowStatus lookupWorkflowStatus(String string) throws Exception {
    switch (string) {
      case "R":
        return WorkflowStatus.READY_FOR_PUBLICATION;
      case "N":
        return WorkflowStatus.NEEDS_REVIEW;
      default:
        throw new Exception("Invalid workflowStatus type: " + string);
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
  public String lookupRelationshipType(String string) throws Exception {
    switch (string) {
      case "AQ":
      case "BRB":
      case "BRN":
      case "BRO":
      case "QB":
      case "RB":
      case "RN":
      case "RO":
      case "RQ":
      case "SY":
      case "PAR":
      case "CHD":
      case "XR":
        return string;
      case "RT":
        return "RO";
      case "NT":
        return "RN";
      case "BT":
        return "RB";
      case "RT?":
        return "RQ";
      case "SFO/LFO":
        return "SY";
      default:
        throw new Exception("Invalid relationship type: " + string);
    }

  }

  /**
   * Log warn and update.
   *
   * @param line the line
   * @param warningMessage the warning message
   * @throws Exception the exception
   */
  public void logWarnAndUpdate(String line, String warningMessage)
    throws Exception {
    logWarn(
        warningMessage + " Could not process the following line:\n\t" + line);
    updateProgress();
  }

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    logAndCommit(stepsCompleted, RootService.logCt, RootService.commitCt);

    if (stepsCompleted % progressCheck == 0) {
      final int currentProgress = (int) ((100.0 * stepsCompleted / steps));
      if (currentProgress > previousProgress) {
        fireProgressEvent(currentProgress, currentProgress + "%");
        previousProgress = currentProgress;
      }
    }

  }

  /**
   * Clear out all of the caches.
   */
  @SuppressWarnings("static-method")
  public void clearCaches() {
    atomCachedTerms.clear();
    atomIdCache.clear();
    attributeCachedTerms.clear();
    attributeIdCache.clear();
    cachedAdditionalRelationshipTypes.clear();
    cachedAttributeNames.clear();
    cachedRootTerminologies.clear();
    cachedTerminologies.clear();
    cachedTermTypes.clear();
    codeCachedTerms.clear();
    codeIdCache.clear();
    conceptCachedTerms.clear();
    conceptIdCache.clear();
    definitionCachedTerms.clear();
    definitionIdCache.clear();
    descriptorCachedTerms.clear();
    descriptorIdCache.clear();
    relCachedTerms.clear();
    relIdCache.clear();
  }

  /**
   * Clear relationship alt terminologies.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void clearRelationshipAltTerminologies() throws Exception {

    final List<String> relationshipPrefixes =
        Arrays.asList("Code", "Concept", "Descriptor", "Atom", "ComponentInfo");

    logInfo("[SourceLoader] Removing " + getProject().getTerminology() + "-SRC"
        + " Relationship Alternate Terminology Ids from database");

    for (final String relPrefix : relationshipPrefixes) {

      final Query jpaQuery = getEntityManager().createQuery("select a from "
          + relPrefix
          + "RelationshipJpa a join a.alternateTerminologyIds b where KEY(b)  = :terminology and a.publishable=true");
      jpaQuery.setParameter("terminology",
          getProject().getTerminology() + "-SRC");

      final List<Object[]> list = jpaQuery.getResultList();
      for (final Object[] entry : list) {
        final Relationship<?, ?> relationship = (Relationship<?, ?>) entry[0];
        relationship.getAlternateTerminologyIds()
            .remove(getProject().getTerminology() + "-SRC");
        updateRelationship(relationship);
      }
    }
  }

  /**
   * Returns the referenced terminologies.
   *
   * @return the referenced terminologies
   * @throws Exception the exception
   */
  public Set<Pair<String, String>> getReferencedTerminologies()
    throws Exception {

    final Set<Pair<String, String>> referencedTerminologies = new HashSet<>();

    //
    // Load the sources.src file
    //
    final List<String> lines =
        loadFileIntoStringList(getSrcDirFile(), "sources.src", null, null);

    final String fields[] = new String[20];

    // Each line of sources.src corresponds to one terminology.
    // Save the each terminology and version as a pair, and add to the results
    // list
    for (final String line : lines) {
      FieldedStringTokenizer.split(line, "|", 20, fields);
      referencedTerminologies.add(new ImmutablePair<>(fields[4], fields[5]));
    }

    return referencedTerminologies;

  }

}