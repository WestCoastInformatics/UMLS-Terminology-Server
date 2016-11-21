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

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.helpers.ConfigUtility;
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

/**
 * Abstract support for source-file loader algorithms.
 */
public abstract class AbstractSourceLoaderAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link AbstractSourceLoaderAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractSourceLoaderAlgorithm() throws Exception {
    // n/a
  }

  /** The full directory where the src files are. */
  private File srcDirFile = null;

  /** The previous progress. */
  private int previousProgress = 0;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted = 0;

  /**
   * The atom ID cache. Key = AlternateTerminologyId; Value = atomJpa Id
   */
  private static Map<String, Long> atomIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their atoms loaded and cached.
   */
  private static Set<String> atomCachedTerms = new HashSet<>();

  /**
   * The attribute ID cache. Key = AlternateTerminologyId Value = attributeJpa
   * Id
   */
  private static Map<String, Long> attributeIdCache = new HashMap<>();

  /**
   * The terminologies that have already had their attributes loaded and cached.
   */
  private static Set<String> attributeCachedTerms = new HashSet<>();

  /**
   * The definition ID cache. Key = AlternateTerminologyId Value = definitionJpa
   * Id
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
    String sourcesFile = srcDirFile + File.separator + fileName;
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
  private void cacheExistingAtomIds(String terminology) throws Exception {

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select value(b), a.id from AtomJpa a join a.alternateTerminologyIds b where KEY(b) = :terminology and a.publishable=true");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(10000);

    logInfo(
        "[SourceLoader] Loading code Atom Ids from database for terminology "
            + terminology);

    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[1].toString());
      atomIdCache.put(terminologyId, id);
    }
    results.close();

    // Add this terminology to the cached set.
    atomCachedTerms.add(terminology);
  }

  /**
   * Cache existing attributes' ATUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  private void cacheExistingAttributeIds(String terminology) throws Exception {

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select value(b), a.id from AttributeJpa a join a.alternateTerminologyIds b where KEY(b) = :terminology and a.publishable=true");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(10000);

    logInfo(
        "[SourceLoader] Loading attribute alternate Terminology Ids from database for terminology "
            + terminology);

    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[1].toString());
      attributeIdCache.put(terminologyId, id);
    }
    results.close();

    // Add this terminology to the cached set.
    attributeCachedTerms.add(terminology);
  }

  /**
   * Cache existing definitions' ATUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  private void cacheExistingDefinitionIds(String terminology) throws Exception {

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select value(b), a.id from DefinitionJpa a join a.alternateTerminologyIds b where KEY(b) = :terminology and a.publishable=true");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(10000);

    logInfo(
        "[SourceLoader] Loading definition alternate Terminology Ids from database for terminology "
            + terminology);

    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[1].toString());
      definitionIdCache.put(terminologyId, id);
    }
    results.close();

    // Add this terminology to the cached set.
    definitionCachedTerms.add(terminology);
  }

  /**
   * Cache existing relationships' RUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  private void cacheExistingRelationshipIds(String terminology)
    throws Exception {

    logInfo(
        "[SourceLoader] Loading relationship Terminology Ids from database for terminology "
            + terminology);

    List<String> relationshipPrefixes =
        Arrays.asList("Code", "Concept", "Descriptor");

    // Get RUIs for ConceptRelationships, CodeRelationships, and
    // ComponentInfoRelationships.
    for (String relPrefix : relationshipPrefixes) {
      final Session session = manager.unwrap(Session.class);
      org.hibernate.Query hQuery =
          session.createQuery("select value(b), a.id from " + relPrefix
              + "RelationshipJpa a join a.alternateTerminologyIds b where KEY(b)  = :terminology and a.publishable=true");
      hQuery.setParameter("terminology", terminology);
      hQuery.setReadOnly(true).setFetchSize(10000);

      ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
      while (results.next()) {
        final String terminologyId = results.get()[0].toString();
        final Long id = Long.valueOf(results.get()[1].toString());
        relIdCache.put(terminologyId, id);
      }
      results.close();
    }

    // Add this terminology to the cached set.
    relCachedTerms.add(terminology);
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
  private void cacheExistingCodeIds(String terminology) throws Exception {

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.id "
            + "from CodeJpa c where terminology = :terminology AND publishable=1");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(10000);

    logInfo(
        "[SourceLoader] Loading code Terminology Ids from database for terminology "
            + terminology);

    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[1].toString());
      codeIdCache.put(terminologyId + terminology, id);
    }
    results.close();

    // Add this terminology to the cached set.
    codeCachedTerms.add(terminology);
  }

  /**
   * Cache existing concept Ids.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  private void cacheExistingConceptIds(String terminology) throws Exception {

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.id "
            + "from ConceptJpa c where terminology = :terminology AND publishable=1");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(10000);

    logInfo(
        "[SourceLoader] Loading concept Terminology Ids from database for terminology "
            + terminology);

    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[1].toString());
      conceptIdCache.put(terminologyId + terminology, id);
    }
    results.close();

    // Add this terminology to the cached set.
    conceptCachedTerms.add(terminology);
  }

  /**
   * Cache existing descriptors.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  private void cacheExistingDescriptorIds(String terminology) throws Exception {

    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.id "
            + "from DescriptorJpa c where terminology = :terminology AND publishable=1");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(10000);

    logInfo(
        "[SourceLoader] Loading descriptor Terminology Ids from database for terminology "
            + terminology);

    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[1].toString());
      descriptorIdCache.put(terminologyId + terminology, id);
    }
    results.close();

    // Add this terminology to the cached set.
    descriptorCachedTerms.add(terminology);
  }

  // /**
  // * Returns the id.
  // *
  // * @param idType the id type
  // * @param terminologyId the terminology id
  // * @param terminology the terminology
  // * @return the id
  // * @throws Exception the exception
  // */
  // public Long getId(IdType idType, String terminologyId, String terminology)
  // throws Exception {
  //
  // if (idType.equals(IdType.ATOM)) {
  // if (!atomCachedTerms.contains(terminology)) {
  // cacheExistingAtomIds(terminology);
  // }
  // return atomIdCache.get(terminologyId);
  // }
  //
  // else if (idType.equals(IdType.ATTRIBUTE)) {
  // if (!attributeCachedTerms.contains(terminology)) {
  // cacheExistingAttributeIds(terminology);
  // }
  // return attributeIdCache.get(terminologyId);
  // }
  //
  // else if (idType.equals(IdType.CODE)) {
  // if (!codeCachedTerms.contains(terminology)) {
  // cacheExistingCodeIds(terminology);
  // }
  // return codeIdCache.get(terminologyId + terminology);
  // }
  //
  // else if (idType.equals(IdType.CONCEPT)) {
  // if (!conceptCachedTerms.contains(terminology)) {
  // cacheExistingConceptIds(terminology);
  // }
  // return conceptIdCache.get(terminologyId + terminology);
  // }
  //
  // else if (idType.equals(IdType.DEFINITION)) {
  // if (!definitionCachedTerms.contains(terminology)) {
  // cacheExistingDefinitionIds(terminology);
  // }
  // return definitionIdCache.get(terminologyId);
  // }
  //
  // else if (idType.equals(IdType.DESCRIPTOR)) {
  // if (!descriptorCachedTerms.contains(terminology)) {
  // cacheExistingDescriptorIds(terminology);
  // }
  // return descriptorIdCache.get(terminologyId + terminology);
  // }
  //
  // else if (idType.equals(IdType.RELATIONSHIP)) {
  // if (!relCachedTerms.contains(terminology)) {
  // cacheExistingRelationshipIds(terminology);
  // }
  // return relIdCache.get(terminologyId + terminology);
  // }
  //
  // else {
  // throw new Exception("ERROR: " + idType + " is an unhandled idType.");
  // }
  //
  // }

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
    }

    if (component instanceof Attribute) {
      attributeIdCache.put(terminologyId, component.getId());
    }

    else if (component instanceof Concept) {
      conceptIdCache.put(terminologyId + component.getTerminology(),
          component.getId());
    }

    else if (component instanceof Code) {
      codeIdCache.put(terminologyId + component.getTerminology(),
          component.getId());
    }

    else if (component instanceof Definition) {
      definitionIdCache.put(terminologyId, component.getId());
    }

    else if (component instanceof Descriptor) {
      descriptorIdCache.put(terminologyId + component.getTerminology(),
          component.getId());
    }

    else if (component instanceof Relationship) {
      relIdCache.put(terminologyId, component.getId());
    } else {
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
      if (!relCachedTerms.contains(getProject().getTerminology())) {
        cacheExistingRelationshipIds(getProject().getTerminology());
      }

      return getComponent(relIdCache.get(terminologyId), relClass);
    }

    else if (type.equals("SRC_REL_ID")) {
      if (!relCachedTerms.contains(getProject().getTerminology() + "-SRC")) {
        cacheExistingRelationshipIds(getProject().getTerminology() + "-SRC");
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

  // /**
  // * Lookup class.
  // *
  // * @param string the string
  // * @return the id type
  // * @throws Exception the exception
  // */
  // @SuppressWarnings("static-method")
  // public IdType lookupIdType(String string) throws Exception {
  //
  // IdType objectType = null;
  //
  // switch (string) {
  // case "CODE_SOURCE":
  // objectType = IdType.CODE;
  // break;
  // case "SOURCE_CUI":
  // objectType = IdType.CONCEPT;
  // break;
  // case "SRC_ATOM_ID":
  // objectType = IdType.ATOM;
  // break;
  // case "SRC_REL_ID":
  // objectType = IdType.RELATIONSHIP;
  // break;
  // default:
  // throw new Exception("Unhandled IdType type: " + string);
  // }
  //
  // return objectType;
  // }

  // /**
  // * Lookup class.
  // *
  // * @param idType the id type
  // * @return the class<? extends component>
  // * @throws Exception the exception
  // */
  // public Class<? extends Component> lookupClass(IdType idType)
  // throws Exception {
  //
  // Class<? extends Component> objectClass = null;
  //
  // switch (idType) {
  // case CODE:
  // objectClass = CodeJpa.class;
  // break;
  // case CONCEPT:
  // objectClass = ConceptJpa.class;
  // break;
  // case ATOM:
  // objectClass = AtomJpa.class;
  // break;
  // default:
  // throw new Exception("Unhandled IdType type: " + idType);
  // }
  //
  // return objectClass;
  // }

  /**
   * Lookup workflow status.
   *
   * @param string the string
   * @return the workflow status
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
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
   * Lookup relationship type.
   *
   * @param string the string
   * @return the string
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public String lookupRelationshipType(String string) throws Exception {

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
      case "PAR":
        relationshipType = "PAR";
        break;
      case "CHD":
        relationshipType = "CHD";
        break;
      default:
        throw new Exception("Invalid relationship type: " + relationshipType);
    }

    return relationshipType;

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
    String algoName = getClass().getSimpleName();
    String shortName = algoName.substring(0, algoName.indexOf("Algorithm"));
    String objectType = algoName.substring(0, algoName.indexOf("Loader"));

    stepsCompleted++;

    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      fireProgressEvent(currentProgress,
          shortName.toUpperCase() + " progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }

    if (!transactionPerOperation) {
      logAndCommit("[" + shortName + "] " + objectType + " lines processed ",
          stepsCompleted, RootService.logCt, RootService.commitCt);
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
}