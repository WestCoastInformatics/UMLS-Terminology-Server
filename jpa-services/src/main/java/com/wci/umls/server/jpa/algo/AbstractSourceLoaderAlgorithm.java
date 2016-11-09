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
import java.util.Set;

import javax.persistence.Query;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;

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
   * @param regexFilter the regex filter
   * @return the list
   * @throws Exception the exception
   */
  public List<String> loadFileIntoStringList(File srcDirFile, String fileName,
    String regexFilter) throws Exception {
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
      if (ConfigUtility.isEmpty(regexFilter)) {
        lines.add(linePre);
      } else {
        if (linePre.matches(regexFilter)) {
          lines.add(linePre);
        }
      }
    }

    sources.close();

    return lines;
  }

  /**
   * Cache existing atoms' AUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingAtomIds(String terminology) throws Exception {

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from atoms a join atomjpa_alternateterminologyids b where a.id = b.AtomJpa_id AND a.terminology = '"
            + terminology + "' AND a.publishable = 1";

    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[SourceLoader] Loading atom AUIs from database for terminology "
          + terminology + ": " + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        atomIdCache.put(result[0].toString(),
            Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);

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

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from attributes a join attributejpa_alternateterminologyids b where terminology = '"
            + terminology
            + "' AND a.id = b.AttributeJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[SourceLoader] Loading attribute ATUIs from database for terminology "
          + terminology + ": "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        attributeIdCache.put(result[0].toString(),
            Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);

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

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from definitions a join definitionjpa_alternateterminologyids b where terminology = '"
            + terminology
            + "' AND a.id = b.DefinitionJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[SourceLoader] Loading definition ATUIs from database for terminology "
          + terminology + ": "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        definitionIdCache.put(result[0].toString(),
            Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);

    // Add this terminology to the cached set.
    definitionCachedTerms.add(terminology);
  }

  /**
   * Cache existing relationships' RUIs and IDs.
   *
   * @param terminology the terminology
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void cacheExistingRelationshipIds(String terminology)
    throws Exception {

    // Get RUIs for ConceptRelationships, CodeRelationships, and
    // ComponentInfoRelationships.

    int iteration = 0;
    int batchSize = 10000;

    String queryStr =
        "select b.alternateTerminologyIds, a.id from concept_relationships a join conceptrelationshipjpa_alternateterminologyids b where terminology = '"
            + terminology
            + "' AND a.id = b.ConceptRelationshipJpa_id AND a.publishable = 1"
            + " UNION ALL "
            + "select b.alternateTerminologyIds, a.id from code_relationships a join coderelationshipjpa_alternateterminologyids b where terminology = '"
            + terminology
            + "' AND a.id = b.CodeRelationshipJpa_id AND a.publishable = 1"
            + " UNION ALL "
            + "select b.alternateTerminologyIds, a.id from component_info_relationships a join componentinforelationshipjpa_alternateterminologyids b where terminology = '"
            + terminology
            + "' AND a.id = b.ComponentInfoRelationshipJpa_id AND a.publishable = 1";
    // Get and execute query (truncate any trailing semi-colon)
    final Query query = manager.createNativeQuery(queryStr);

    List<Object[]> objects = new ArrayList<>();
    do {
      query.setMaxResults(batchSize);
      query.setFirstResult(batchSize * iteration);

      logInfo("[SourceLoader] Loading relationship RUIs from database for terminology "
          + terminology + ": "
          + query.getFirstResult() + " - "
          + (query.getFirstResult() + batchSize));
      objects = query.getResultList();

      for (final Object[] result : objects) {
        relIdCache.put(result[0].toString(),
            Long.valueOf(result[1].toString()));
      }
      iteration++;
    } while (objects.size() > 0);

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

    // Pre-populate codeIdMap (for all terminologies from this insertion)
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.terminology, c.id "
            + "from CodeJpa c where terminology = :terminology AND publishable=1");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(1000);
    
    logInfo("[SourceLoader] Loading code Terminology Ids from database for terminology "
        + terminology);
    
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[2].toString());
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

    // Pre-populate conceptIdMap (for all terminologies from this insertion)
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.terminology, c.id "
            + "from ConceptJpa c where terminology = :terminology AND publishable=1");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(1000);

    logInfo("[SourceLoader] Loading concept Terminology Ids from database for terminology "
        + terminology);
    
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[2].toString());
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

    // Pre-populate descriptorIdMap (for all terminologies from this insertion)
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery =
        session.createQuery("select c.terminologyId, c.terminology, c.id "
            + "from DescriptorJpa c where terminology = :terminology AND publishable=1");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(1000);

    logInfo("[SourceLoader] Loading descriptor Terminology Ids from database for terminology "
        + terminology);
    
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final String terminologyId = results.get()[0].toString();
      final Long id = Long.valueOf(results.get()[2].toString());
      descriptorIdCache.put(terminologyId + terminology, id);
    }
    results.close();

    // Add this terminology to the cached set.
    descriptorCachedTerms.add(terminology);
  }

  /**
   * Returns the id.
   *
   * @param idType the id type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @return the id
   * @throws Exception the exception
   */
  public Long getId(Class<?> idType, String terminologyId, String terminology)
    throws Exception {

    if (idType.equals(AtomJpa.class)) {
      if (!atomCachedTerms.contains(terminology)) {
        cacheExistingAtomIds(terminology);
      }
      return atomIdCache.get(terminologyId);
    }

    else if (idType.equals(AttributeJpa.class)) {
      if (!attributeCachedTerms.contains(terminology)) {
        cacheExistingAttributeIds(terminology);
      }
      return attributeIdCache.get(terminologyId);
    }

    else if (idType.equals(CodeJpa.class)) {
      if (!codeCachedTerms.contains(terminology)) {
        cacheExistingCodeIds(terminology);
      }
      return codeIdCache.get(terminologyId + terminology);
    }

    else if (idType.equals(ConceptJpa.class)) {
      if (!conceptCachedTerms.contains(terminology)) {
        cacheExistingConceptIds(terminology);
      }
      return conceptIdCache.get(terminologyId + terminology);
    }

    else if (idType.equals(DefinitionJpa.class)) {
      if (!definitionCachedTerms.contains(terminology)) {
        cacheExistingDefinitionIds(terminology);
      }
      return definitionIdCache.get(terminologyId);
    }

    else if (idType.equals(DescriptorJpa.class)) {
      if (!descriptorCachedTerms.contains(terminology)) {
        cacheExistingDescriptorIds(terminology);
      }
      return descriptorIdCache.get(terminologyId + terminology);
    }

    else if (idType.equals(ConceptRelationshipJpa.class)
        || idType.equals(CodeRelationshipJpa.class)
        || idType.equals(DescriptorRelationshipJpa.class)) {
      if (!relCachedTerms.contains(terminology)) {
        cacheExistingRelationshipIds(terminology);
      }
      return relIdCache.get(terminologyId);
    }

    else {
      throw new Exception("ERROR: " + idType + " is an unhandled idType.");
    }

  }

  /**
   * Put id.
   *
   * @param idType the id type
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param id the id
   * @throws Exception the exception
   */
  public void putId(Class<?> idType, String terminologyId, String terminology,
    Long id) throws Exception {
    if (idType.equals(AtomJpa.class)) {
      atomIdCache.put(terminologyId, id);
    }

    else if (idType.equals(AttributeJpa.class)) {
      attributeIdCache.put(terminologyId, id);
    }

    else if (idType.equals(CodeJpa.class)) {
      codeIdCache.put(terminologyId + terminology, id);
    }

    else if (idType.equals(ConceptJpa.class)) {
      conceptIdCache.put(terminologyId + terminology, id);
    }

    else if (idType.equals(DefinitionJpa.class)) {
      definitionIdCache.put(terminologyId, id);
    }

    else if (idType.equals(DescriptorJpa.class)) {
      descriptorIdCache.put(terminologyId + terminology, id);
    }

    else if (idType.equals(ConceptRelationshipJpa.class)
        || idType.equals(CodeRelationshipJpa.class)
        || idType.equals(DescriptorRelationshipJpa.class)) {
      relIdCache.put(terminologyId, id);
    }

    else {
      throw new Exception("ERROR: " + idType + " is an unhandled idType.");
    }
  }

  /**
   * Returns the cached root terminologies.
   *
   * @return the cached root terminologies
   * @throws Exception
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
   * @throws Exception
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

    return cachedTerminologies.get(terminologyAndVersion);
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
   * Class lookup.
   *
   * @param string the string
   * @return the class<? extends hasid>
   * @throws Exception the exception
   */
  public Class<? extends Component> lookupClass(String string)
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
        throw new Exception("Invalid class type: " + string);
    }

    return objectClass;
  }

  /**
   * Lookup workflow status.
   *
   * @param string the string
   * @return the workflow status
   * @throws Exception the exception
   */
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
        throw new Exception(
            "Invalid workflowStatus type: " + string);
    }

    return workflowStatus;
  }
  
  
  /**
   * Clear out all of the caches.
   */
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