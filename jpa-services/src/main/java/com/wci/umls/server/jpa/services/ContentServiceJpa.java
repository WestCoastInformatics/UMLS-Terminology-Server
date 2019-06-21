/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.Note;
import com.wci.umls.server.helpers.NoteList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DefinitionList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.GeneralConceptAxiomList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.actions.AtomicActionJpa;
import com.wci.umls.server.jpa.content.AbstractAtomClass;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AtomTreePositionJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeNoteJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ComponentInfoRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptNoteJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorNoteJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.content.GeneralConceptAxiomJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.MapSetJpa;
import com.wci.umls.server.jpa.content.MappingJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.AtomListJpa;
import com.wci.umls.server.jpa.helpers.content.AttributeListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DefinitionListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.GeneralConceptAxiomListJpa;
import com.wci.umls.server.jpa.helpers.content.LexicalClassListJpa;
import com.wci.umls.server.jpa.helpers.content.MapSetListJpa;
import com.wci.umls.server.jpa.helpers.content.MappingListJpa;
import com.wci.umls.server.jpa.helpers.content.NoteListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.StringClassListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.jpa.services.handlers.EclExpressionHandler;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.model.actions.AtomicAction;
import com.wci.umls.server.model.actions.MolecularAction;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentHasDefinitions;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.GeneralConceptAxiom;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.ExpressionHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * JPA enabled implementation of the content service.
 */
public class ContentServiceJpa extends MetadataServiceJpa
    implements ContentService {

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = false;

  /** The id handler map. */
  static Map<String, IdentifierAssignmentHandler> idHandlerMap =
      new HashMap<>();

  /** The query timeout. */
  static int queryTimeout = 1000;

  /** The pn handler map. */
  private static Map<String, ComputePreferredNameHandler> pnHandlerMap = null;

  /** The normalized string handler. */
  private static NormalizedStringHandler normalizedStringHandler = null;

  static {
    init();
  }

  /**
   * Inits the.
   */
  private static void init() {
    try {
      if (ConfigUtility.getConfigProperties()
          .containsKey("javax.persistence.query.timeout")) {
        queryTimeout = Integer.parseInt(ConfigUtility.getConfigProperties()
            .getProperty("javax.persistence.query.timeout"));
      }

      if (config == null)
        config = ConfigUtility.getConfigProperties();
      final String key = "identifier.assignment.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        final IdentifierAssignmentHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, IdentifierAssignmentHandler.class);
        idHandlerMap.put(handlerName, handlerService);
      }
      if (!idHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("identifier.assignment.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      idHandlerMap = null;
    }

    pnHandlerMap = new HashMap<>();

    try {
      config = ConfigUtility.getConfigProperties();
      final String key = "compute.preferred.name.handler";
      for (final String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        final ComputePreferredNameHandler handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, ComputePreferredNameHandler.class);
        pnHandlerMap.put(handlerName, handlerService);
      }
      if (!pnHandlerMap.containsKey(ConfigUtility.DEFAULT)) {
        throw new Exception("compute.preferred.name.handler."
            + ConfigUtility.DEFAULT + " expected and does not exist.");
      }
    } catch (Exception e) {
      e.printStackTrace();
      pnHandlerMap = null;
    }

    try {

      config = ConfigUtility.getConfigProperties();
      final String key = "normalized.string.handler";
      final String handlerName = config.getProperty(key);

      final NormalizedStringHandler handlerService =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, NormalizedStringHandler.class);
      normalizedStringHandler = handlerService;
    } catch (Exception e) {
      e.printStackTrace();
      normalizedStringHandler = null;
    }
  }

  /**
   * Instantiates an empty {@link ContentServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ContentServiceJpa() throws Exception {
    super();
    validateInit();
  }

  /* see superclass */
  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get concept " + id);

    Concept concept = getComponent(id, ConceptJpa.class);

    return concept;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get concepts "
        + terminologyId + "/" + terminology + "/" + version);
    final List<Concept> concepts =
        getComponents(terminologyId, terminology, version, ConceptJpa.class);
    if (concepts == null) {
      return null;
    }
    final ConceptList list = new ConceptListJpa();
    list.setTotalCount(concepts.size());
    list.setObjects(concepts);

    return list;
  }

  /* see superclass */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get concept "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);

    final Concept concept = getComponent(terminologyId, terminology, version,
        branch, ConceptJpa.class);

    return concept;
  }

  /* see superclass */
  @Override
  public Concept addConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add concept " + concept);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(concept.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + concept.getTerminology());
      }
      String id = idHandler.getTerminologyId(concept);
      concept.setTerminologyId(id);
    }

    // Add component
    return addComponent(concept);

  }

  /* see superclass */
  @Override
  public void updateConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update concept " + concept);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(concept.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowConceptIdChangeOnUpdate()) {
        final Concept concept2 = getConcept(concept.getId());
        if (!idHandler.getTerminologyId(concept)
            .equals(idHandler.getTerminologyId(concept2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set concept id on update
        concept.setTerminologyId(idHandler.getTerminologyId(concept));
      }
    }
    // update component
    updateComponent(concept);

  }

  /* see superclass */
  @Override
  public void removeConcept(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove concept " + id);
    // Remove the component
    removeComponent(id, ConceptJpa.class);

  }

  /* see superclass */
  @Override
  public Subset getSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - subset " + id);
    if (subsetClass != null) {
      return getComponent(id, subsetClass);
    } else {
      Subset subset = getComponent(id, AtomSubsetJpa.class);
      if (subset == null) {
        subset = getComponent(id, ConceptSubsetJpa.class);
      }
      return subset;
    }
  }

  /* see superclass */
  @Override
  public Subset getSubset(String terminologyId, String terminology,
    String version, String branch, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get subset "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    if (subsetClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          subsetClass);
    } else {
      Subset subset = getComponent(terminologyId, terminology, version, branch,
          AtomSubsetJpa.class);
      if (subset == null) {
        subset = getComponent(terminologyId, terminology, version, branch,
            ConceptSubsetJpa.class);
      }
      return subset;
    }

  }

  /* see superclass */
  @Override
  public SubsetList getAtomSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atom subsets " + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<Subset> m = query.getResultList();
      final SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());

      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public SubsetList getConceptSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concept subsets " + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetJpa a where "
            + "version = :version and terminology = :terminology");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<Subset> m = query.getResultList();
      final SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());
      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find atom subset members " + subsetId + "/"
            + terminology + "/" + version + ", query=" + query);
    // Prepare the query string

    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (subsetId != null && !subsetId.isEmpty()) {
      if (finalQuery.length() > 0) {
        finalQuery.append(" AND ");
      }
      finalQuery
          .append("subsetTerminologyId:" + QueryParserBase.escape(subsetId));
    }
    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    final SubsetMemberList list = new SubsetMemberListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, finalQuery.toString(), "memberNameSort",
        AtomSubsetMemberJpa.class, pfs, totalCt, manager));
    list.setTotalCount(totalCt[0]);
    return list;
  }

  /* see superclass */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  @Override
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find concept subset members " + subsetId + "/"
            + terminology + "/" + version + ", query=" + query);
    // Prepare the query string

    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (subsetId != null && !subsetId.isEmpty()) {
      if (finalQuery.length() > 0) {
        finalQuery.append(" AND ");
      }
      finalQuery
          .append("subsetTerminologyId:" + QueryParserBase.escape(subsetId));
    }

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    final SubsetMemberList list = new SubsetMemberListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, finalQuery.toString(), "memberNameSort",
        ConceptSubsetMemberJpa.class, pfs, totalCt, manager));
    list.setTotalCount(totalCt[0]);

    return list;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getAtomSubsetMembers(String atomId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass())
        .debug("Content Service - get subset members for atom " + atomId + "/"
            + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetMemberJpa a, "
            + " AtomJpa b where b.terminologyId = :atomId "
            + "and b.version = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      final SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("atomId", atomId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (final SubsetMember<? extends ComponentHasAttributesAndName> s :
       * list .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getConceptSubsetMembers(String conceptId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass())
        .debug("Content Service - get subset members for concept " + conceptId
            + "/" + terminology + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetMemberJpa a, "
            + " ConceptJpa b where b.terminologyId = :conceptId "
            + "and b.version = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      final SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("conceptId", conceptId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public SubsetList getAllSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get all subsets "
        + terminology + "/" + version + "/" + branch);
    assert branch != null;

    try {
      final SubsetList list = getAtomSubsets(terminology, version, branch);
      if (list == null) {
        return getConceptSubsets(terminology, version, branch);
      } else {
        list.getObjects().addAll(
            getConceptSubsets(terminology, version, branch).getObjects());
      }
      list.setTotalCount(list.getObjects().size());
      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public Definition getDefinition(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get definition " + id);
    return getComponent(id, DefinitionJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public DefinitionList getDefinitions(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get definitions "
        + terminologyId + "/" + terminology + "/" + version);
    final List<Definition> definitions =
        getComponents(terminologyId, terminology, version, DefinitionJpa.class);
    if (definitions == null) {
      return null;
    }
    final DefinitionList list = new DefinitionListJpa();
    list.setTotalCount(definitions.size());
    list.setObjects(definitions);
    return list;
  }

  /* see superclass */
  @Override
  public Definition getDefinition(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get definition "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        DefinitionJpa.class);
  }

  /* see superclass */
  @Override
  public Definition addDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add definition " + definition);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(definition.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + definition.getTerminology());
      }
      String id = idHandler.getTerminologyId(definition, component);
      definition.setTerminologyId(id);
    }

    // Add component
    return addComponent(definition);

  }

  /* see superclass */
  @Override
  public void updateDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update definition " + definition);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(definition.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Definition definition2 =
            getComponent(definition.getId(), DefinitionJpa.class);
        if (!idHandler.getTerminologyId(definition, component)
            .equals(idHandler.getTerminologyId(definition2, component))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set definition id on update
        definition.setTerminologyId(
            idHandler.getTerminologyId(definition, component));
      }
    }
    // update component
    updateComponent(definition);

  }

  /* see superclass */
  @Override
  public void removeDefinition(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove definition " + id);
    // Remove the component
    removeComponent(id, DefinitionJpa.class);

  }

  /* see superclass */
  @Override
  public SemanticTypeComponent getSemanticTypeComponent(Long id)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get semantic type component " + id);
    return getComponent(id, SemanticTypeComponentJpa.class);
  }

  /* see superclass */
  @Override
  public SemanticTypeComponent addSemanticTypeComponent(
    SemanticTypeComponent component, Concept concept) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add semanticTypeComponent " + component);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(component.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + component.getTerminology());
      }
      String id = idHandler.getTerminologyId(component, concept);
      component.setTerminologyId(id);
    }

    addComponent(component);

    // Add component
    return component;

  }

  /* see superclass */
  @Override
  public void updateSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent, Concept concept)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update semanticTypeComponent "
            + semanticTypeComponent);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(semanticTypeComponent.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        SemanticTypeComponent semanticTypeComponent2 = getComponent(
            semanticTypeComponent.getId(), SemanticTypeComponent.class);
        if (!idHandler.getTerminologyId(semanticTypeComponent, concept).equals(
            idHandler.getTerminologyId(semanticTypeComponent2, concept))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set semanticTypeComponent id on update
        semanticTypeComponent.setTerminologyId(
            idHandler.getTerminologyId(semanticTypeComponent, concept));
      }
    }
    // update component
    updateComponent(semanticTypeComponent);

  }

  /* see superclass */
  @Override
  public void removeSemanticTypeComponent(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove semanticTypeComponent " + id);

    // Remove the component
    removeComponent(id, SemanticTypeComponentJpa.class);

  }

  /* see superclass */
  @Override
  public Descriptor getDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get descriptor " + id);
    return getComponent(id, DescriptorJpa.class);
  }

  /* see superclass */

  @SuppressWarnings("unchecked")
  @Override
  public DescriptorList getDescriptors(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get descriptors "
        + terminologyId + "/" + terminology + "/" + version);
    final List<Descriptor> descriptors =
        getComponents(terminologyId, terminology, version, DescriptorJpa.class);
    if (descriptors == null) {
      return null;
    }
    DescriptorList list = new DescriptorListJpa();
    list.setTotalCount(descriptors.size());
    list.setObjects(descriptors);
    return list;
  }

  /* see superclass */
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get descriptor "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        DescriptorJpa.class);
  }

  /* see superclass */
  @Override
  public Descriptor addDescriptor(Descriptor descriptor) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add descriptor " + descriptor);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(descriptor.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + descriptor.getTerminology());
      }
      String id = idHandler.getTerminologyId(descriptor);
      descriptor.setTerminologyId(id);
    }

    // Add component
    return addComponent(descriptor);

  }

  /* see superclass */
  @Override
  public void updateDescriptor(Descriptor descriptor) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update descriptor " + descriptor);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(descriptor.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Descriptor descriptor2 = getDescriptor(descriptor.getId());
        if (!idHandler.getTerminologyId(descriptor)
            .equals(idHandler.getTerminologyId(descriptor2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set descriptor id on update
        descriptor.setTerminologyId(idHandler.getTerminologyId(descriptor));
      }
    }
    // update component
    updateComponent(descriptor);

  }

  /* see superclass */
  @Override
  public void removeDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove descriptor " + id);
    // Remove the component
    removeComponent(id, DescriptorJpa.class);

  }

  /* see superclass */
  @Override
  public Code getCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get code " + id);
    final Code c = manager.find(CodeJpa.class, id);
    return c;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public CodeList getCodes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get codes "
        + terminologyId + "/" + terminology + "/" + version);
    final List<Code> codes =
        getComponents(terminologyId, terminology, version, CodeJpa.class);
    if (codes == null) {
      return null;
    }
    final CodeList list = new CodeListJpa();
    list.setTotalCount(codes.size());
    list.setObjects(codes);
    return list;
  }

  /* see superclass */
  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get code "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        CodeJpa.class);
  }

  /* see superclass */
  @Override
  public Code addCode(Code code) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add code " + code);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(code.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + code.getTerminology());
      }
      String id = idHandler.getTerminologyId(code);
      code.setTerminologyId(id);
    }

    // Add component
    return addComponent(code);

  }

  /* see superclass */
  @Override
  public void updateCode(Code code) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update code " + code);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(code.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Code code2 = getCode(code.getId());
        if (!idHandler.getTerminologyId(code)
            .equals(idHandler.getTerminologyId(code2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set code id on update
        code.setTerminologyId(idHandler.getTerminologyId(code));
      }
    }
    // update component
    updateComponent(code);

  }

  /* see superclass */
  @Override
  public void removeCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove code " + id);
    // Remove the component
    removeComponent(id, CodeJpa.class);

  }

  /* see superclass */

  @Override
  public LexicalClass getLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get lexical class " + id);
    return getComponent(id, LexicalClassJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public LexicalClassList getLexicalClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get lexical classes "
        + terminologyId + "/" + terminology + "/" + version);
    final List<LexicalClass> luis = getComponents(terminologyId, terminology,
        version, LexicalClassJpa.class);
    if (luis == null) {
      return null;
    }
    final LexicalClassList list = new LexicalClassListJpa();
    list.setTotalCount(luis.size());
    list.setObjects(luis);
    return list;

  }

  /* see superclass */
  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get lexical class "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        LexicalClassJpa.class);
  }

  /* see superclass */
  @Override
  public LexicalClass addLexicalClass(LexicalClass lexicalClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add lexical class " + lexicalClass);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(lexicalClass.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + lexicalClass.getTerminology());
      }
      String id = idHandler.getTerminologyId(lexicalClass);
      lexicalClass.setTerminologyId(id);
    }

    // Add component
    return addComponent(lexicalClass);

  }

  /* see superclass */
  @Override
  public void updateLexicalClass(LexicalClass lexicalClass) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update lexical class " + lexicalClass);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(lexicalClass.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        LexicalClass lexicalClass2 = getLexicalClass(lexicalClass.getId());
        if (!idHandler.getTerminologyId(lexicalClass)
            .equals(idHandler.getTerminologyId(lexicalClass2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set lexicalClass id on update
        lexicalClass.setTerminologyId(idHandler.getTerminologyId(lexicalClass));
      }
    }
    // update component
    updateComponent(lexicalClass);

  }

  /* see superclass */
  @Override
  public void removeLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove lexical class " + id);
    // Remove the component

    removeComponent(id, LexicalClassJpa.class);

  }

  /* see superclass */
  @Override
  public StringClass getStringClass(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get string class " + id);
    return getComponent(id, StringClassJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public StringClassList getStringClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get string classes "
        + terminologyId + "/" + terminology + "/" + version);
    final List<StringClass> suis = getComponents(terminologyId, terminology,
        version, StringClassJpa.class);
    if (suis == null) {
      return null;
    }
    final StringClassList list = new StringClassListJpa();
    list.setTotalCount(suis.size());
    list.setObjects(suis);
    return list;
  }

  /* see superclass */
  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get string class "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        StringClass.class);
  }

  /* see superclass */
  @Override
  public StringClass addStringClass(StringClass stringClass) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add string class " + stringClass);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(stringClass.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + stringClass.getTerminology());
      }
      String id = idHandler.getTerminologyId(stringClass);
      stringClass.setTerminologyId(id);
    }

    // Add component
    return addComponent(stringClass);

  }

  /* see superclass */
  @Override
  public void updateStringClass(StringClass stringClass) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update string class " + stringClass);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(stringClass.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        StringClass stringClass2 = getStringClass(stringClass.getId());
        if (!idHandler.getTerminologyId(stringClass)
            .equals(idHandler.getTerminologyId(stringClass2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set stringClass id on update
        stringClass.setTerminologyId(idHandler.getTerminologyId(stringClass));
      }
    }
    // update component
    updateComponent(stringClass);

  }

  /* see superclass */
  @Override
  public void removeStringClass(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove string class " + id);
    // Remove the component
    removeComponent(id, StringClassJpa.class);

  }

  /* see superclass */
  @Override
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find descendant concepts " + terminologyId
            + ", " + terminology);
    final long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Concept> descendants =
        findDescendantsHelper(terminologyId, terminology, version, childrenOnly,
            branch, pfs, ConceptJpa.class, totalCt);
    final ConceptList list = new ConceptListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find ancestor concepts " + terminologyId
            + ", " + terminology);
    final long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Concept> ancestors =
        findAncestorsHelper(terminologyId, terminology, version, parentsOnly,
            branch, pfs, ConceptJpa.class, totalCt);
    final ConceptList list = new ConceptListJpa();
    list.setObjects(ancestors);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  @Override
  public AtomList findDescendantAtoms(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find descendant atoms " + terminologyId + ", "
            + terminology);
    final long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Atom> descendants =
        findDescendantsHelper(terminologyId, terminology, version, childrenOnly,
            branch, pfs, AtomJpa.class, totalCt);
    final AtomList list = new AtomListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  @Override
  public AtomList findAncestorAtoms(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - find ancestor atoms "
        + terminologyId + ", " + terminology);
    final long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Atom> ancestors = findAncestorsHelper(terminologyId, terminology,
        version, parentsOnly, branch, pfs, AtomJpa.class, totalCt);
    final AtomList list = new AtomListJpa();
    list.setObjects(ancestors);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /**
   * Find descendants helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param childrenOnly the children only
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @param totalCt the total ct
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  private List findDescendantsHelper(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs,
    Class<?> clazz, long[] totalCt) throws Exception {

    if (pfs != null && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    final String queryStr = "select a from "
        + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa") + " tr, "
        + clazz.getName() + " super, " + clazz.getName() + " a "
        + " where super.version = :version "
        + " and super.terminology = :terminology "
        + " and super.terminologyId = :terminologyId"
        + " and tr.superType = super" + " and tr.subType = a "
        + " and tr.superType != tr.subType"
        + (childrenOnly ? " and depth = 1" : "");
    final javax.persistence.Query query = applyPfsToJPQLQuery(queryStr, pfs);

    final javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " super, " + clazz.getName() + " a "
            + " where super.version = :version "
            + " and super.terminology = :terminology "
            + " and super.terminologyId = :terminologyId"
            + " and tr.superType = super" + " and tr.subType = a "
            + " and tr.superType != tr.subType"
            + (childrenOnly ? " and depth = 1" : ""));

    ctQuery.setParameter("terminology", terminology);
    ctQuery.setParameter("version", version);
    ctQuery.setParameter("terminologyId", terminologyId);
    totalCt[0] = ((Long) ctQuery.getSingleResult()).intValue();

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    query.setParameter("terminologyId", terminologyId);

    return query.getResultList();
  }

  /**
   * Find ancestors helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param parentsOnly the parents only
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @param totalCt the total ct
   * @return the list
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  private List findAncestorsHelper(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs,
    Class<?> clazz, long[] totalCt) throws Exception {

    if (pfs != null && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    final String queryStr = "select a from "
        + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa") + " tr, "
        + clazz.getName() + " sub, " + clazz.getName() + " a "
        + " where sub.version = :version "
        + " and sub.terminology = :terminology "
        + " and sub.terminologyId = :terminologyId" + " and tr.subType = sub"
        + " and tr.superType = a " + " and tr.subType != tr.superType"
        + (parentsOnly ? " and depth = 1" : "");
    final javax.persistence.Query query = applyPfsToJPQLQuery(queryStr, pfs);

    final javax.persistence.Query ctQuery =
        manager.createQuery("select count(*) from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " sub, " + clazz.getName() + " a "
            + " where sub.version = :version "
            + " and sub.terminology = :terminology "
            + " and sub.terminologyId = :terminologyId"
            + " and tr.subType = sub" + " and tr.superType = a "
            + " and tr.subType != tr.superType"
            + (parentsOnly ? " and depth = 1" : ""));

    ctQuery.setParameter("terminology", terminology);
    ctQuery.setParameter("version", version);
    ctQuery.setParameter("terminologyId", terminologyId);
    totalCt[0] = ((Long) ctQuery.getSingleResult()).intValue();

    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    query.setParameter("terminologyId", terminologyId);

    return query.getResultList();
  }

  /* see superclass */
  @Override
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find descendant descriptors " + terminologyId
            + ", " + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Descriptor> descendants =
        findDescendantsHelper(terminologyId, terminology, version, childrenOnly,
            branch, pfs, DescriptorJpa.class, totalCt);
    final DescriptorList list = new DescriptorListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find ancestor descriptors " + terminologyId
            + ", " + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Descriptor> ancestors =
        findAncestorsHelper(terminologyId, terminology, version, childrenOnly,
            branch, pfs, DescriptorJpa.class, totalCt);
    final DescriptorList list = new DescriptorListJpa();
    list.setObjects(ancestors);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find descendant codes " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Code> descendants =
        findDescendantsHelper(terminologyId, terminology, version, childrenOnly,
            branch, pfs, CodeJpa.class, totalCt);
    final CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - find ancestor codes "
        + terminologyId + ", " + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    final List<Code> descendants = findAncestorsHelper(terminologyId,
        terminology, version, parentsOnly, branch, pfs, CodeJpa.class, totalCt);
    final CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public Atom getAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get atom " + id);

    final Atom atom = getComponent(id, AtomJpa.class);

    return atom;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AtomList getAtoms(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get atoms "
        + terminologyId + "/" + terminology + "/" + version);
    final List<Atom> atoms =
        getComponents(terminologyId, terminology, version, AtomJpa.class);
    if (atoms == null) {
      return null;
    }
    final AtomList list = new AtomListJpa();
    list.setTotalCount(atoms.size());
    list.setObjects(atoms);

    return list;
  }

  /* see superclass */
  @Override
  public Atom getAtom(String terminologyId, String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get atom "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);

    Atom atom = getComponent(terminologyId, terminology, version, branch,
        AtomJpa.class);

    return atom;
  }

  /* see superclass */
  @Override
  public Atom addAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add atom " + atom);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(atom.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + atom.getTerminology());
      }
      atom.setTerminologyId(idHandler.getTerminologyId(atom));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception(
          "Unable to find id handler for " + atom.getTerminology());
    }

    // Add component
    return addComponent(atom);

  }

  /* see superclass */
  @Override
  public void updateAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update atom " + atom);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(atom.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      final Atom atom2 = getAtom(atom.getId());
      if (!idHandler.getTerminologyId(atom)
          .equals(idHandler.getTerminologyId(atom2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    updateComponent(atom);

  }

  /* see superclass */
  @Override
  public void removeAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove atom " + id);
    // Remove the component
    removeComponent(id, AtomJpa.class);

  }

  /* see superclass */
  @Override
  public void moveAtoms(Concept fromConcept, Concept toConcept,
    List<Atom> fromAtoms) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - move atoms " + fromAtoms + " from concept "
            + fromConcept + " to concept " + toConcept);

    // for each atom, remove from fromConcept and add toConcept
    for (Atom atm : fromAtoms) {
      if (fromConcept != null) {
        fromConcept.getAtoms().remove(atm);
      }
      if (toConcept != null) {
        toConcept.getAtoms().add(atm);
      }

      // check for molecular action flag
      if (isMolecularActionFlag()) {
        // Create an atomic action for each atom move
        final MolecularAction molecularAction = getMolecularAction();

        // construct the atomic action

        final AtomicAction atomicAction = new AtomicActionJpa();
        atomicAction.setField("concept");
        atomicAction.setIdType(IdType.getIdType(atm));
        atomicAction.setClassName(AtomJpa.class.getName());
        atomicAction.setMolecularAction(molecularAction);
        atomicAction.setOldValue(
            (fromConcept == null) ? null : fromConcept.getId().toString());
        atomicAction.setNewValue(
            (toConcept == null) ? null : toConcept.getId().toString());
        atomicAction.setObjectId(atm.getId());

        // persist the atomic action and add the persisted version to the
        // molecular action
        final AtomicAction newAtomicAction = addAtomicAction(atomicAction);

        molecularAction.getAtomicActions().add(newAtomicAction);
      }
    }
  }

  /* see superclass */
  @Override
  public ConceptRelationship createInverseConceptRelationship(
    ConceptRelationship relationship) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - create inverse of concept relationship "
            + relationship);
    if (relationship != null) {
      ConceptRelationship inverseRelationship =
          new ConceptRelationshipJpa(relationship, false);
      inverseRelationship.setId(null);
      inverseRelationship.setTerminologyId("");
      inverseRelationship.setFrom(relationship.getTo());
      inverseRelationship.setTo(relationship.getFrom());
      inverseRelationship.setPublishable(relationship.isPublishable());
      inverseRelationship.setRelationshipType(
          getRelationshipType(relationship.getRelationshipType(),
              relationship.getTerminology(), relationship.getVersion())
                  .getInverse().getAbbreviation());
      inverseRelationship
          .setAssertedDirection(!relationship.isAssertedDirection());

      return inverseRelationship;
    } else {

      return null;
    }
  }

  /* see superclass */
  @Override
  public RelationshipList getInverseRelationships(String terminology,
    String version,
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get the inverse of concept relationship "
            + relationship);

    // Relationship<? extends ComponentInfo, ? extends ComponentInfo>
    // inverseRelationship = null;

    if (relationship != null) {
      final String inverseRelType =
          getRelationshipType(relationship.getRelationshipType(), terminology,
              version).getInverse().getAbbreviation();

      final String inverseAdditionalRelType = getAdditionalRelationshipType(
          relationship.getAdditionalRelationshipType(), terminology, version)
              .getInverse().getAbbreviation();

      final RelationshipList relList =
          findRelationshipsForComponentHelper(null, null, null, Branch.ROOT,
              "fromId:" + relationship.getTo().getId() + " AND toId:"
                  + relationship.getFrom().getId() + " AND relationshipType:"
                  + inverseRelType
                  + (ConfigUtility.isEmpty(inverseAdditionalRelType)
                      ? " AND NOT additionalRelationshipType:[* TO *] "
                      : " AND additionalRelationshipType:"
                          + inverseAdditionalRelType),
              false, null, relationship.getClass());

      if (relList.size() == 0) {
        throw new Exception("Unexpected missing inverse relationship");
      } else {
        return relList;
      }

    } else {

      return null;
    }
  }

  /* see superclass */
  @Override
  public String getInverseRelationshipType(String terminology, String version,
    String relationshipType) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get the inverse for relationship type "
            + relationshipType);

    final String inverseRelType =
        getRelationshipType(relationshipType, terminology, version).getInverse()
            .getAbbreviation();

    if (inverseRelType == null) {
      throw new Exception(
          "No inverse relationship found for type " + relationshipType);
    } else {
      return inverseRelType;
    }
  }

  @Override
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> getInverseRelationship(
    String terminology, String version,
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception {

    RelationshipList relList =
        getInverseRelationships(terminology, version, relationship);

    // If there's only one inverse relationship returned, that's the one we
    // want.
    if (relList.size() == 1) {
      return relList.getObjects().get(0);
    }
    // If more than one inverse relationship is returned (can happen in the case
    // of demotions), return the appropriate one.
    else {
      if (relationship.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
        for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
            .getObjects()) {
          if (rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
            return rel;
          }
        }
      } else {
        for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList
            .getObjects()) {
          if (!rel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
            return rel;
          }
        }
      }

    }

    return null;
  }

  /* see superclass */
  @Override
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> getRelationship(
    Long id,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find relationship " + id);
    if (relationshipClass != null) {
      return getComponent(id, relationshipClass);
    } else {
      Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel =
          getComponent(id, ConceptRelationshipJpa.class);
      if (rel == null) {
        rel = getComponent(id, AtomRelationshipJpa.class);
      }
      if (rel == null) {
        rel = getComponent(id, CodeRelationshipJpa.class);
      }
      if (rel == null) {
        rel = getComponent(id, DescriptorRelationshipJpa.class);
      }
      return rel;
    }
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public RelationshipList getRelationships(String terminologyId,
    String terminology, String version,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - find relationships "
        + terminologyId + "/" + terminology + "/" + version);
    List<Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationships =
        null;
    if (relationshipClass != null) {
      relationships =
          getComponents(terminologyId, terminology, version, relationshipClass);
    } else {
      relationships = getComponents(terminologyId, terminology, version,
          ConceptRelationshipJpa.class);
      if (relationships == null) {
        relationships = getComponents(terminologyId, terminology, version,
            AtomRelationshipJpa.class);
      }
      if (relationships == null) {
        relationships = getComponents(terminologyId, terminology, version,
            CodeRelationshipJpa.class);
      }
      if (relationships == null) {
        relationships = getComponents(terminologyId, terminology, version,
            DescriptorRelationshipJpa.class);
      }
    }

    if (relationships == null) {
      return null;
    }
    final RelationshipList list = new RelationshipListJpa();
    list.setTotalCount(relationships.size());
    list.setObjects(relationships);
    return list;
  }

  /* see superclass */
  @Override
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> getRelationship(
    String terminologyId, String terminology, String version, String branch,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - find relationship "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    if (relationshipClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          relationshipClass);
    }
    final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel =
        getComponent(terminologyId, terminology, version, branch,
            ConceptRelationshipJpa.class);
    if (rel == null) {
      getComponent(terminologyId, terminology, version, branch,
          AtomRelationshipJpa.class);
    }
    if (rel == null) {
      getComponent(terminologyId, terminology, version, branch,
          CodeRelationshipJpa.class);
    }
    if (rel == null) {
      getComponent(terminologyId, terminology, version, branch,
          DescriptorRelationshipJpa.class);
    }
    return rel;
  }

  /* see superclass */
  @Override
  public Relationship<? extends ComponentInfo, ? extends ComponentInfo> addRelationship(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add relationship " + rel);
    // Assign id - this was moved to Release time.
    // IdentifierAssignmentHandler idHandler = null;
    // if (assignIdentifiersFlag) {
    // idHandler = getIdentifierAssignmentHandler(rel.getTerminology());
    // if (idHandler == null) {
    // throw new Exception(
    // "Unable to find id handler for " + rel.getTerminology());
    // }
    // String id = idHandler.getTerminologyId(rel);
    // rel.setTerminologyId(id);
    // }

    // Add component
    return addComponent(rel);

  }

  /* see superclass */
  @Override
  public void updateRelationship(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update relationship " + rel);

    // Id assignment should not change - Id assignment was moved to release
    // time.
    // final IdentifierAssignmentHandler idHandler =
    // getIdentifierAssignmentHandler(rel.getTerminology());
    // if (assignIdentifiersFlag) {
    // if (!idHandler.allowIdChangeOnUpdate()) {
    // @SuppressWarnings("unchecked")
    // Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel2 =
    // getComponent(rel.getId(), rel.getClass());
    // if (!idHandler.getTerminologyId(rel)
    // .equals(idHandler.getTerminologyId(rel2))) {
    // throw new Exception(
    // "Update cannot be used to change object identity.");
    // }
    // } else {
    // // set attribute id on update
    // rel.setTerminologyId(idHandler.getTerminologyId(rel));
    // }
    // }
    // update component
    updateComponent(rel);

  }

  /* see superclass */
  @Override
  public void removeRelationship(Long id,
    Class<? extends Relationship<? extends ComponentInfo, ? extends ComponentInfo>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove relationship " + id);
    // Remove the component
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel = null;
    if (relationshipClass != null) {
      removeComponent(id, relationshipClass);
    } else {
      rel = getRelationship(id, relationshipClass);
      removeComponent(id, rel.getClass());
    }

  }

  /* see superclass */
  @Override
  public TransitiveRelationship<? extends AtomClass> getTransitiveRelationship(
    Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get transitive relationship " + id);
    if (relationshipClass != null) {
      return getComponent(id, relationshipClass);
    } else {
      TransitiveRelationship<? extends AtomClass> rel =
          getComponent(id, ConceptTransitiveRelationshipJpa.class);
      if (rel == null) {
        rel = getComponent(id, CodeTransitiveRelationshipJpa.class);
      }
      if (rel == null) {
        rel = getComponent(id, DescriptorTransitiveRelationshipJpa.class);
      }
      return rel;
    }
  }

  /* see superclass */
  @Override
  public TransitiveRelationship<? extends ComponentHasAttributes> addTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add transitive relationship " + rel);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(rel.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + rel.getTerminology());
      }
      String id = idHandler.getTerminologyId(rel);
      rel.setTerminologyId(id);
    }

    // Add component
    final TransitiveRelationship<? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    return newRel;
  }

  /* see superclass */
  @Override
  public void updateTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update transitive relationship " + rel);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(rel.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        final TransitiveRelationship<? extends ComponentHasAttributes> rel2 =
            getComponent(rel.getId(), rel.getClass());
        if (!idHandler.getTerminologyId(rel)
            .equals(idHandler.getTerminologyId(rel2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        rel.setTerminologyId(idHandler.getTerminologyId(rel));
      }
    }
    // update component
    updateComponent(rel);

  }

  /* see superclass */
  @Override
  public void removeTransitiveRelationship(Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove transitive relationship " + id);

    final TransitiveRelationship<? extends ComponentHasAttributes> rel =
        getComponent(id, relationshipClass);
    removeComponent(id, rel.getClass());
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  public TreePosition<?> getTreePosition(Long id,
    Class<? extends TreePosition> treeposClass) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get tree position " + id);
    if (treeposClass != null) {
      return getComponent(id, treeposClass);
    } else {
      TreePosition<?> treepos = getComponent(id, ConceptTreePositionJpa.class);
      if (treepos == null) {
        treepos = getComponent(id, CodeTreePositionJpa.class);
      }
      if (treepos == null) {
        treepos = getComponent(id, DescriptorTreePositionJpa.class);
      }
      if (treepos == null) {
        treepos = getComponent(id, AtomTreePositionJpa.class);
      }
      return treepos;
    }
  }

  /* see superclass */
  @Override
  public TreePosition<?> addTreePosition(TreePosition<?> treepos)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add tree position " + treepos);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(treepos.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + treepos.getTerminology());
      }
      String id = idHandler.getTerminologyId(treepos);
      treepos.setTerminologyId(id);
    }

    // Add component
    final TreePosition<?> newTreepos = addComponent(treepos);

    return newTreepos;
  }

  /* see superclass */
  @Override
  public void updateTreePosition(TreePosition<?> treepos) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update tree position " + treepos);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(treepos.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        TreePosition<?> treepos2 =
            getComponent(treepos.getId(), treepos.getClass());
        if (!idHandler.getTerminologyId(treepos)
            .equals(idHandler.getTerminologyId(treepos2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        treepos.setTerminologyId(idHandler.getTerminologyId(treepos));
      }
    }
    // update component
    updateComponent(treepos);

  }

  /* see superclass */
  @SuppressWarnings({
      "rawtypes"
  })
  @Override
  public void removeTreePosition(Long id,
    Class<? extends TreePosition> treeposClass) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove tree position " + id);
    final TreePosition<?> treepos = getComponent(id, treeposClass);
    removeComponent(id, treepos.getClass());
  }

  /* see superclass */
  @Override
  public Subset addSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add subset " + subset);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(subset.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + subset.getTerminology());
      }
      subset.setTerminologyId(idHandler.getTerminologyId(subset));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception(
          "Unable to find id handler for " + subset.getTerminology());
    }

    // Add component
    return addComponent(subset);

  }

  /* see superclass */
  @Override
  public void updateSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update subset " + subset);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subset.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      final Subset subset2 = getSubset(subset.getId(), subset.getClass());
      if (!idHandler.getTerminologyId(subset)
          .equals(idHandler.getTerminologyId(subset2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    updateComponent(subset);

  }

  /* see superclass */

  @Override
  public void removeSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove subset " + id);
    Subset subset = null;
    // Remove the component
    if (subsetClass != null) {
      removeComponent(id, subsetClass);
    } else {
      subset = getSubset(id, subsetClass);
      removeComponent(id, subset.getClass());
    }

  }

  /* see superclass */

  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get subset member " + id);
    if (memberClass != null) {
      return getComponent(id, memberClass);
    } else {
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
          getComponent(id, AtomSubsetMemberJpa.class);
      if (member == null) {
        member = getComponent(id, ConceptSubsetMemberJpa.class);
      }
      return member;
    }

  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembers(String terminologyId,
    String terminology, String version,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get subset members "
        + terminologyId + "/" + terminology + "/" + version);
    List<SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> members =
        null;
    if (memberClass != null) {
      members =

          getComponents(terminologyId, terminology, version, memberClass);
    } else {
      members = getComponents(terminologyId, terminology, version,
          AtomSubsetMemberJpa.class);
      if (members == null) {
        members = getComponents(terminologyId, terminology, version,
            ConceptSubsetMemberJpa.class);

      }
    }
    if (members == null) {
      return null;
    }
    final SubsetMemberList list = new SubsetMemberListJpa();
    list.setTotalCount(members.size());
    list.setObjects(members);
    return list;
  }

  /* see superclass */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    String terminologyId, String terminology, String version, String branch,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get subset member "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    if (memberClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          memberClass);
    } else {
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
          getComponent(terminologyId, terminology, version, branch,
              AtomSubsetMemberJpa.class);
      if (member == null) {
        member = getComponent(terminologyId, terminology, version, branch,
            ConceptSubsetMemberJpa.class);
      }
      return member;
    }
  }

  /* see superclass */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> addSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add subset member " + subsetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(subsetMember.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + subsetMember.getTerminology());
      }
      subsetMember.setTerminologyId(idHandler.getTerminologyId(subsetMember));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception(
          "Unable to find id handler for " + subsetMember.getTerminology());
    }

    // Add component
    return addComponent(subsetMember);

  }

  /* see superclass */
  @Override
  public void updateSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update subsetMember " + subsetMember);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subsetMember.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      @SuppressWarnings("unchecked")
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember2 =
          getComponent(subsetMember.getId(), subsetMember.getClass());
      if (!idHandler.getTerminologyId(subsetMember)
          .equals(idHandler.getTerminologyId(subsetMember2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    updateComponent(subsetMember);

  }

  /* see superclass */
  @Override
  public void removeSubsetMember(Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove subsetMember " + id);
    // find and remove the component
    final SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
        getComponent(id, memberClass);
    removeComponent(id, member.getClass());

  }

  /* see superclass */
  @Override
  public Attribute getAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get attribute " + id);
    return getComponent(id, AttributeJpa.class);
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeList getAttributes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get attributes "
        + terminologyId + "/" + terminology + "/" + version);
    final List<Attribute> attributes =
        getComponents(terminologyId, terminology, version, AttributeJpa.class);
    if (attributes == null) {
      return null;
    }
    final AttributeList list = new AttributeListJpa();
    list.setTotalCount(attributes.size());
    list.setObjects(attributes);
    return list;
  }

  /* see superclass */
  @Override
  public Attribute getAttribute(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get attribute "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AttributeJpa.class);
  }

  /* see superclass */
  @Override
  public Attribute addAttribute(Attribute attribute, ComponentInfo component)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add attribute " + attribute);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(attribute.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + attribute.getTerminology());
      }
      String id = idHandler.getTerminologyId(attribute, component);
      attribute.setTerminologyId(id);
    }

    // Add component
    return addComponent(attribute);

  }

  /* see superclass */
  @Override
  public void updateAttribute(Attribute attribute, ComponentInfo component)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update attribute " + attribute);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(attribute.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Attribute attribute2 = getAttribute(attribute.getId());
        if (!idHandler.getTerminologyId(attribute, component)
            .equals(idHandler.getTerminologyId(attribute2, component))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        attribute
            .setTerminologyId(idHandler.getTerminologyId(attribute, component));
      }
    }
    // update component
    updateComponent(attribute);

  }

  /* see superclass */
  @Override
  public void removeAttribute(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove attribute " + id);
    // Remove the component
    removeComponent(id, AttributeJpa.class);

  }

  /* see superclass */
  @Override
  public SearchResultList findConceptSearchResults(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .info("Content Service - find concept search results " + terminology
            + "/" + version + "/" + query);
    final SearchResultList results = findSearchResultsForQueryHelper(
        terminology, version, branch, query, pfs, ConceptJpa.class);
    return results;
  }

  /* see superclass */
  @Override
  public ConceptList findConcepts(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find concepts "
        + terminology + "/" + version + "/" + query);
    int[] totalCt = new int[1];
    final List<ConceptJpa> results = findForQueryHelper(terminology, version,
        branch, query, pfs, totalCt, null, ConceptJpa.class);

    final ConceptList list = new ConceptListJpa();
    list.setObjects(
        results.stream().map(c -> (Concept) c).collect(Collectors.toList()));
    list.setTotalCount(totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - autocomplete concepts "
        + terminology + ", " + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm,
        ConceptJpa.class);
  }

  /* see superclass */
  @Override
  public SearchResultList findDescriptorSearchResults(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find descriptors "
        + terminology + "/" + version + "/" + query);
    final SearchResultList results = findSearchResultsForQueryHelper(
        terminology, version, branch, query, pfs, DescriptorJpa.class);
    for (final SearchResult result : results.getObjects()) {
      result.setType(IdType.DESCRIPTOR);
    }
    return results;
  }

  /* see superclass */
  @Override
  public DescriptorList findDescriptors(String terminology, String version,
    String branch, String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find concepts "
        + terminology + "/" + version + "/" + query);
    int[] totalCt = new int[1];
    final List<DescriptorJpa> results = findForQueryHelper(terminology, version,
        branch, query, pfs, totalCt, null, DescriptorJpa.class);

    final DescriptorList list = new DescriptorListJpa();
    list.setObjects(
        results.stream().map(c -> (Descriptor) c).collect(Collectors.toList()));
    list.setTotalCount(totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass())
        .info("Content Service - autocomplete descriptors " + terminology + ", "
            + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm,
        DescriptorJpa.class);
  }

  /**
   * Find search results for query helper.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  public <T extends AtomClass> SearchResultList findSearchResultsForQueryHelper(
    String terminology, String version, String branch, String query,
    PfsParameter pfs, Class<T> clazz) throws Exception {
    // Prepare results
    int[] totalCt = new int[1];
    Map<Long, Float> scoreMap = new HashMap<>();
    final List<T> results = findForQueryHelper(terminology, version, branch,
        query, pfs, totalCt, scoreMap, clazz);

    final SearchResultList list = new SearchResultListJpa();
    list.setTotalCount(totalCt[0]);

    // construct the search results (if any found)
    if (results != null) {
      for (final T r : results) {
        final SearchResult sr = new SearchResultJpa();
        sr.setId(r.getId());
        sr.setTerminology(r.getTerminology());
        sr.setVersion(r.getVersion());
        sr.setTerminologyId(r.getTerminologyId());
        sr.setValue(r.getName());
        sr.setScore(scoreMap.get(r.getId()));
        sr.setWorkflowStatus(r.getWorkflowStatus());
        sr.setType(r.getType());
        list.getObjects().add(sr);
      }
    }

    return list;
  }

  /**
   * Find for query helper.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @param totalCt the total ct
   * @param scoreMap the score map
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  public <T extends AtomClass> List<T> findForQueryHelper(String terminology,
    String version, String branch, String query, PfsParameter pfs,
    int[] totalCt, Map<Long, Float> scoreMap, Class<T> clazz) throws Exception {

    // construct return lists for lucene and expression results
    List<T> luceneResults = null;
    SearchResultList exprResults = null;

    // construct local pfs
    final PfsParameter localPfs =
        pfs == null ? new PfsParameterJpa() : new PfsParameterJpa(pfs);

    // declare search handler
    SearchHandler searchHandler = null;

    if (localPfs.getExpression() != null
        && !localPfs.getExpression().isEmpty()) {

      // get the results
      ExpressionHandler exprHandler =
          getExpressionHandler(terminology, version);
      exprResults = exprHandler.resolve(localPfs.getExpression());

      // if results found, constuct a query restriction
      if (exprResults.size() > 0) {
        String exprQueryRestr = (localPfs.getQueryRestriction() != null
            && !localPfs.getQueryRestriction().isEmpty() ? " AND " : "")
            + "terminologyId:(";
        for (final SearchResult exprResult : exprResults.getObjects()) {
          exprQueryRestr += exprResult.getTerminologyId() + " ";
        }
        // trim last space, close parenthesis and add boost based on count
        exprQueryRestr =
            exprQueryRestr.substring(0, exprQueryRestr.length() - 1) + ")^"
                + exprResults.size();
        localPfs.setQueryRestriction((localPfs.getQueryRestriction() != null
            ? localPfs.getQueryRestriction() : "") + exprQueryRestr);
      }
    }

    // if an atom class, use atom class
    if (AbstractAtomClass.class.isAssignableFrom(clazz)) {
      searchHandler = getSearchHandler(ConfigUtility.ATOMCLASS);
    }

    // otherwise look for terminology specific handlers (this condition may be
    // impossible)
    else {
      searchHandler = getSearchHandler(terminology);
    }

    // if no expression, or expression with results, perform lucene query
    if (exprResults == null || exprResults.size() > 0) {
      luceneResults = searchHandler.getQueryResults(terminology, version,
          branch, query, "atoms.nameSort", clazz, localPfs, totalCt, manager);
    }

    if (scoreMap != null) {
      scoreMap.putAll(searchHandler.getScoreMap());
    }
    return luceneResults;

  }

  /**
   * Find for general query helper.
   *
   * @param <T> the
   * @param luceneQuery the lucene query
   * @param JPQLQuery the JPQL query
   * @param branch the branch
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private <T extends AtomClass> SearchResultList findForGeneralQueryHelper(
    String luceneQuery, String JPQLQuery, String branch, PfsParameter pfs,
    Class<T> clazz) throws Exception {
    // Prepare results
    final SearchResultList results = new SearchResultListJpa();
    List<T> classes = null;
    final int totalCt[] = new int[1];

    // Perform Lucene search
    final List<T> luceneQueryClasses = new ArrayList<>();
    boolean luceneQueryFlag = false;
    if (luceneQuery != null && !luceneQuery.equals("")) {
      SearchHandler searchHandler = getSearchHandler("");
      luceneQueryClasses.addAll(searchHandler.getQueryResults(null, null,
          branch, luceneQuery, "atomsName.sort", clazz, pfs, totalCt, manager));
      luceneQueryFlag = true;
    }

    boolean JPQLQueryFlag = false;
    final List<T> JPQLQueryClasses = new ArrayList<>();
    if (JPQLQuery != null && !JPQLQuery.equals("")) {
      if (!JPQLQuery.toLowerCase().startsWith("select"))
        throw new Exception(
            "The JPQL query did not start with the keyword 'select'. "
                + JPQLQuery);
      if (JPQLQuery.contains(";"))
        throw new Exception(
            "The JPQL query must not contain the ';'. " + JPQLQuery);
      javax.persistence.Query hQuery = manager.createQuery(JPQLQuery);

      // Support for this is probably in Mysql 5.7.4
      // See http://mysqlserverteam.com/server-side-select-statement-timeouts/
      // It doesn't work with Mysql 5.6, seems to simply be ignored
      hQuery.setHint("javax.persistence.query.timeout", queryTimeout);
      try {
        final List<T> JPQLResults = hQuery.getResultList();
        for (final T r : JPQLResults) {
          JPQLQueryClasses.add(r);
        }
      } catch (ClassCastException e) {
        throw new Exception(
            "The JPQL query returned items of an unexpected type. ", e);
      }

      JPQLQueryFlag = true;
    }

    // Determine whether both query and criteria were used, or just one or the
    // other

    // Start with query results if they exist
    if (luceneQueryFlag) {
      classes = luceneQueryClasses;
    }

    if (JPQLQueryFlag) {

      if (luceneQueryFlag) {
        // Intersect the lucene and HQL results
        classes.retainAll(JPQLQueryClasses);
      } else {
        // Otherwise, just use JPQL classes
        classes = JPQLQueryClasses;
      }

      // Here we know the total size
      totalCt[0] = classes.size();

      // Apply PFS sorting manually
      if (pfs != null && pfs.getSortField() != null) {
        final Method getMethod = clazz
            .getMethod("get" + pfs.getSortField().substring(0, 1).toUpperCase()
                + pfs.getSortField().substring(1));
        if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        Collections.sort(classes, new Comparator<AtomClass>() {
          @SuppressWarnings("rawtypes")
          @Override
          public int compare(AtomClass o1, AtomClass o2) {
            try {
              final Comparable f1 =
                  (Comparable) getMethod.invoke(o1, new Object[] {});
              final Comparable f2 =
                  (Comparable) getMethod.invoke(o2, new Object[] {});
              return f1.compareTo(f2);
            } catch (Exception e) {
              // do nothing
            }
            return 0;
          }
        });
      }

      // Apply PFS paging manually
      if (pfs != null && pfs.getStartIndex() != -1) {
        int startIndex = pfs.getStartIndex();
        int toIndex = classes.size();
        toIndex = Math.min(toIndex, startIndex + pfs.getMaxResults());
        classes = classes.subList(startIndex, toIndex);
      }

    } else {
      // If criteria flag wasn't triggered, then PFS was already handled
      // by the query mechanism - which only applies PFS if criteria isn't
      // also used. Therefore, we are ready to go.

      // Manual PFS handling is in the section above.
    }

    // Some result has been found, even if empty
    if (classes == null)
      return results;

    // construct the search results
    for (final AtomClass atomClass : classes) {
      final SearchResult sr = new SearchResultJpa();
      sr.setId(atomClass.getId());
      sr.setTerminologyId(atomClass.getTerminologyId());
      sr.setTerminology(atomClass.getTerminology());
      sr.setVersion(atomClass.getVersion());
      sr.setValue(atomClass.getName());
      sr.setObsolete(atomClass.isObsolete());
      results.getObjects().add(sr);
    }

    results.setTotalCount(totalCt[0]);
    return results;

  }

  /**
   * Autocomplete helper.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param clazz the clazz
   * @return the string list
   * @throws Exception the exception
   */
  private <T extends AtomClass> StringList autocompleteHelper(
    String terminology, String version, String searchTerm, Class<T> clazz)
    throws Exception {

    if (terminology == null || version == null || searchTerm == null) {
      return new StringList();
    }
    final String TITLE_EDGE_NGRAM_INDEX = "atoms.edgeNGramName";
    final String TITLE_NGRAM_INDEX = "atoms.nGramName";

    final FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    final QueryBuilder titleQB = fullTextEntityManager.getSearchFactory()
        .buildQueryBuilder().forEntity(clazz).get();

    final Query query = titleQB.phrase().withSlop(2).onField(TITLE_NGRAM_INDEX)
        .andField(TITLE_EDGE_NGRAM_INDEX).boostedTo(5).andField("atoms.name")
        .boostedTo(5).sentence(searchTerm.toLowerCase()).createQuery();

    final Query term1 = new TermQuery(new Term("terminology", terminology));
    final Query term2 = new TermQuery(new Term("version", version));
    final Query term3 = new TermQuery(new Term("atoms.suppressible", "false"));
    final Query term4 = new TermQuery(new Term("suppressible", "false"));
    final BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(term1, BooleanClause.Occur.MUST);
    booleanQuery.add(term2, BooleanClause.Occur.MUST);
    booleanQuery.add(term3, BooleanClause.Occur.MUST);
    booleanQuery.add(term4, BooleanClause.Occur.MUST);
    // Only for concepts
    if (Concept.class.isAssignableFrom(clazz)) {
      final Query term5 = new TermQuery(new Term("anonymous", "false"));
      booleanQuery.add(term5, BooleanClause.Occur.MUST);
    }
    booleanQuery.add(query, BooleanClause.Occur.MUST);

    final FullTextQuery fullTextQuery =
        fullTextEntityManager.createFullTextQuery(booleanQuery, clazz);

    fullTextQuery.setMaxResults(20);

    @SuppressWarnings("unchecked")
    final List<AtomClass> results = fullTextQuery.getResultList();
    final StringList list = new StringList();
    list.setTotalCount(fullTextQuery.getResultSize());
    for (final AtomClass result : results) {
      // exclude duplicates
      if (!list.contains(result.getName()))
        list.getObjects().add(result.getName());
    }
    return list;
  }

  /* see superclass */
  @Override
  public SearchResultList findCodeSearchResults(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find codes "
        + terminology + "/" + version + "/" + query);
    final SearchResultList results = findSearchResultsForQueryHelper(
        terminology, version, branch, query, pfs, CodeJpa.class);
    for (final SearchResult result : results.getObjects()) {
      result.setType(IdType.CODE);
    }
    return results;
  }

  /* see superclass */
  @Override
  public CodeList findCodes(String terminology, String version, String branch,
    String query, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find concepts "
        + terminology + "/" + version + "/" + query);
    int[] totalCt = new int[1];
    final List<CodeJpa> results = findForQueryHelper(terminology, version,
        branch, query, pfs, totalCt, null, CodeJpa.class);

    final CodeList list = new CodeListJpa();
    list.setObjects(
        results.stream().map(c -> (Code) c).collect(Collectors.toList()));
    list.setTotalCount(totalCt[0]);
    return list;
  }

  /* see superclass */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - autocomplete codes "
        + terminology + ", " + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm, CodeJpa.class);
  }

  /* see superclass */
  @Override
  public ConceptList getAllConcepts(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug("Content Service - get all concepts "
        + terminology + "/" + version + "/" + branch);
    assert branch != null;

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from ConceptJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      final List<Concept> concepts = query.getResultList();
      final ConceptList conceptList = new ConceptListJpa();
      conceptList.setObjects(concepts);
      conceptList.setTotalCount(concepts.size());
      return conceptList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public List<Long> getAllConceptIds(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get all concept ids "
        + terminology + "/" + version + "/" + branch);
    assert branch != null;

    final Map<String, String> params = new HashMap<>();
    params.put("terminology", terminology);
    params.put("version", version);
    return executeSingleComponentIdQuery(null, QueryType.LUCENE, params,
        ConceptJpa.class, false);

  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public List<Long> getAmbiguousAtomIds(Concept concept) {
    try {
      // collect lower name hash values from all atoms
      final Set<String> lowerNameHashes = concept.getAtoms().stream()
          .map(a -> a.getLowerNameHash()).collect(Collectors.toSet());

      if (lowerNameHashes.isEmpty()) {
        return new ArrayList<>();
      }

      // Find lower name hashes that are ambiguous (e.g. in other concepts)
      final javax.persistence.Query query = manager.createQuery(
          "select distinct a.lowerNameHash from ConceptJpa c join c.atoms a "
              + "where c.version = :version and c.terminology = :terminology "
              + " and a.lowerNameHash in (:lowerNameHashes)"
              + " and c.id != :conceptId");
      query.setParameter("terminology", concept.getTerminology());
      query.setParameter("version", concept.getVersion());
      query.setParameter("conceptId", concept.getId());
      query.setParameter("lowerNameHashes", lowerNameHashes);
      final List<Object[]> results = query.getResultList();
      final Set<String> ambigLowerNameHashes = new HashSet<>();
      for (final Object result : results) {
        ambigLowerNameHashes.add(result.toString());
      }
      // Return atoms whose lower name hashes were in the set
      return concept.getAtoms().stream()
          .filter(a -> ambigLowerNameHashes.contains(a.getLowerNameHash()))
          .map(a -> a.getId()).collect(Collectors.toList());

    } catch (NoResultException e) {
      return new ArrayList<>();
    }
  }

  /* see superclass */
  @Override
  public DescriptorList getAllDescriptors(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug("Content Service - get all descriptors "
        + terminology + "/" + version + "/" + branch);
    assert branch != null;

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from DescriptorJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");

      @SuppressWarnings("unchecked")
      final List<Descriptor> descriptors = query.getResultList();
      final DescriptorList descriptorList = new DescriptorListJpa();
      descriptorList.setObjects(descriptors);
      descriptorList.setTotalCount(descriptors.size());
      return descriptorList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */

  @Override
  public List<Long> getAllDescriptorIds(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get all descriptor ids " + terminology + "/"
            + version + "/" + branch);
    assert branch != null;

    final Map<String, String> params = new HashMap<>();
    params.put("terminology", terminology);
    params.put("version", version);
    return executeSingleComponentIdQuery(null, QueryType.LUCENE, params,
        DescriptorJpa.class, false);
  }

  /* see superclass */
  @Override
  public CodeList getAllCodes(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug("Content Service - get all codes "
        + terminology + "/" + version + "/" + branch);
    assert branch != null;

    try {
      final javax.persistence.Query query =
          manager.createQuery("select a from CodeJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      final List<Code> codes = query.getResultList();
      final CodeList codeList = new CodeListJpa();
      codeList.setObjects(codes);
      codeList.setTotalCount(codes.size());
      return codeList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public List<Long> getAllCodeIds(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get all code ids "
        + terminology + "/" + version + "/" + branch);
    assert branch != null;

    final Map<String, String> params = new HashMap<>();
    params.put("terminology", terminology);
    params.put("version", version);
    return executeSingleComponentIdQuery(null, QueryType.LUCENE, params,
        CodeJpa.class, false);

  }

  /* see superclass */
  @Override
  public void clearTransitiveClosure(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass())
        .info("Content Service - Clear transitive closure data for "
            + terminology + ", " + version);
    try {
      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.begin();
      }

      javax.persistence.Query query =
          manager.createQuery("DELETE From ConceptTransitiveRelationshipJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass())
          .info("    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From DescriptorTransitiveRelationshipJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass())
          .info("    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query = manager.createQuery("DELETE From CodeTransitiveRelationshipJpa "
          + " c where terminology = :terminology " + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass())
          .info("    CodeTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.commit();
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void clearTreePositions(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass())
        .info("Content Service - Clear tree positions data for " + terminology
            + ", " + version);
    try {
      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.begin();
      }

      javax.persistence.Query query =
          manager.createQuery("DELETE From ConceptTreePositionJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      int deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass())
          .info("    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query = manager.createQuery("DELETE From DescriptorTreePositionJpa "
          + " c where terminology = :terminology " + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass())
          .info("    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query = manager.createQuery("DELETE From CodeTreePositionJpa "
          + " c where terminology = :terminology " + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass())
          .info("    CodeTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.commit();
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void clearBranch(String branch) {
    // TBD
  }

  /* see superclass */
  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    if (idHandlerMap.containsKey(terminology)) {
      return idHandlerMap.get(terminology);
    }
    return idHandlerMap.get(ConfigUtility.DEFAULT);

  }

  @Override
  public NormalizedStringHandler getNormalizedStringHandler() throws Exception {
    return normalizedStringHandler;
  }

  /* see superclass */
  @Override
  public IdentifierAssignmentHandler newIdentifierAssignmentHandler(
    String terminology) throws Exception {
    return ConfigUtility.newStandardHandlerInstanceWithConfiguration(
        "identifier.assignment.handler", terminology,
        IdentifierAssignmentHandler.class);
  }

  /* see superclass */
  @Override
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception {
    if (pnHandlerMap.containsKey(terminology)) {
      return pnHandlerMap.get(terminology);
    }
    return pnHandlerMap.get(ConfigUtility.DEFAULT);
  }

  /* see superclass */
  @Override
  public String getComputedPreferredName(AtomClass atomClass,
    PrecedenceList list) throws Exception {
    try {
      ComputePreferredNameHandler handler =
          pnHandlerMap.get(atomClass.getTerminology());
      // look for default if null
      if (handler == null) {
        handler = pnHandlerMap.get(ConfigUtility.DEFAULT);
      }
      if (handler == null) {
        throw new Exception(
            "Compute preferred name handler is not configured for DEFAULT or for "
                + atomClass.getTerminology());
      }
      final String pn =
          handler.computePreferredName(atomClass.getAtoms(), list);
      return pn;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /* see superclass */
  @Override
  public String getNormalizedString(String string) throws Exception {
    return normalizedStringHandler.getNormalizedString(string);
  }

  /* see superclass */
  @Override
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag) {
    this.assignIdentifiersFlag = assignIdentifiersFlag;
  }

  /* see superclass */
  @Override
  public Map<String, Integer> getComponentStats(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - getComponentStats");
    assert branch != null;
    final Map<String, Integer> stats = new TreeMap<>();
    for (final EntityType<?> type : manager.getMetamodel().getEntities()) {
      final String jpaTable = type.getName();
      // Logger.getLogger(getClass()).debug(" jpaTable = " + jpaTable);
      // Skip audit trail tables
      if (jpaTable.toUpperCase().indexOf("_AUD") != -1) {
        continue;
      }
      if (!AbstractAbbreviation.class
          .isAssignableFrom(type.getBindableJavaType())
          && !AbstractComponent.class
              .isAssignableFrom(type.getBindableJavaType())) {
        continue;
      }
      Logger.getLogger(getClass()).info("  " + jpaTable);
      javax.persistence.Query query = null;
      if (terminology != null) {
        query = manager.createQuery("select count(*) from " + jpaTable
            + " where terminology = :terminology " + "and version = :version ");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);

      } else {
        query = manager.createQuery("select count(*) from " + jpaTable);
      }
      int ct = ((Long) query.getSingleResult()).intValue();
      stats.put("Total " + jpaTable, ct);

      // Only compute active counts for components
      if (AbstractComponent.class
          .isAssignableFrom(type.getBindableJavaType())) {
        if (terminology != null) {

          query = manager.createQuery("select count(*) from " + jpaTable
              + " where obsolete = 0 and terminology = :terminology "
              + "and version = :version ");
          query.setParameter("terminology", terminology);
          query.setParameter("version", version);
        } else {
          query = manager.createQuery("select count(*) from " + jpaTable);
        }
        ct = ((Long) query.getSingleResult()).intValue();
        stats.put("Non-obsolete " + jpaTable, ct);
      }
    }
    return stats;
  }

  /**
   * Returns the components.
   *
   * @param <T> the
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param clazz the clazz
   * @return the components
   */
  @SuppressWarnings("rawtypes")
  protected <T extends Component> List getComponents(String terminologyId,
    String terminology, String version, Class<T> clazz) {
    try {
      final javax.persistence.Query query = manager.createQuery("select a from "
          + clazz.getName()
          + " a where terminologyId = :terminologyId and version = :version and terminology = :terminology");
      query.setParameter("terminologyId", terminologyId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   * Adds the component.
   *
   * @param <T> the
   * @param component the component
   * @return the t
   * @throws Exception the exception
   */
  protected <T extends Component> T addComponent(T component) throws Exception {

    // handle as a normal "has last modified"
    final T newComponent = addHasLastModified(component);

    // Component-specific handling
    // check for molecular action flag
    if (isMolecularActionFlag()) {
      final MolecularAction molecularAction = getMolecularAction();

      // construct the atomic action

      final AtomicAction atomicAction = new AtomicActionJpa();
      atomicAction.setField("id");
      atomicAction.setIdType(IdType.getIdType(newComponent));
      atomicAction.setClassName(newComponent.getClass().getName());
      atomicAction.setMolecularAction(molecularAction);
      atomicAction.setOldValue(null);
      atomicAction.setNewValue(newComponent.getId().toString());
      atomicAction.setObjectId(newComponent.getId());

      // persist the atomic action and add the persisted version to the
      // molecular action
      final AtomicAction newAtomicAction = addAtomicAction(atomicAction);

      molecularAction.getAtomicActions().add(newAtomicAction);

    }
    return newComponent;

  }

  /**
   * Update component.
   *
   * @param <T> the
   * @param newComponent the new component
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  protected <T extends Component> void updateComponent(T newComponent)
    throws Exception {
    // Component-specific handling

    // check for molecular action flag
    if (isMolecularActionFlag()) {
      final MolecularAction molecularAction = getMolecularAction();

      final T oldComponent = getComponent(newComponent.getId(),
          (Class<T>) newComponent.getClass());

      // Create an atomic action when old value is different from new value.
      // For fields annotated with @Column
      final List<Method> columnMethods =
          IndexUtility.getAllColumnGetMethods(oldComponent.getClass());

      // Iterate through "column" methods
      for (final Method m : columnMethods) {

        // Obtain previous and current values
        final Object oldObject = m.invoke(oldComponent, new Object[] {});
        final Object newObject = m.invoke(newComponent, new Object[] {});

        // Obtain string values
        final String oldValue = (oldObject == null ? ""
            : m.invoke(oldComponent, new Object[] {}).toString());
        final String newValue = (newObject == null ? ""
            : m.invoke(newComponent, new Object[] {}).toString());

        // If different, construct an atomic action and attach to molecular
        // action
        if (!oldValue.equals(newValue)) {
          final AtomicAction atomicAction = new AtomicActionJpa();
          atomicAction.setField(IndexUtility.getFieldNameFromMethod(m, null));
          atomicAction.setIdType(IdType.getIdType(oldComponent));
          atomicAction.setClassName(newComponent.getClass().getName());
          atomicAction.setMolecularAction(molecularAction);
          atomicAction.setOldValue(oldValue);
          atomicAction.setNewValue(newValue);
          atomicAction.setObjectId(oldComponent.getId());
          final AtomicAction newAtomicAction = addAtomicAction(atomicAction);
          molecularAction.getAtomicActions().add(newAtomicAction);
        }

      }

      // Create an atomic action for each element of a collection that is
      // different for fields with @OneToMany annotations
      final List<Method> oneToManyMethods =
          IndexUtility.getAllCollectionGetMethods(oldComponent.getClass());

      // Iterate through @OneToMan methods
      for (final Method m : oneToManyMethods) {

        // Obtain the old/new identifier lists
        final List<?> oldList = new ArrayList<>(
            (Collection<?>) m.invoke(oldComponent, new Object[] {}));
        final Set<Long> oldIds = (oldList.stream().map(x -> ((HasId) x).getId())
            .collect(Collectors.toSet()));

        final List<?> newList = new ArrayList<>(
            (Collection<?>) m.invoke(newComponent, new Object[] {}));
        final Set<Long> newIds = (newList.stream().map(x -> ((HasId) x).getId())
            .collect(Collectors.toSet()));

        // Get the collection class name
        String collectionClassName = null;
        if (oldList.size() > 0) {
          collectionClassName = oldList.iterator().next().getClass().getName();
        } else if (newList.size() > 0) {
          collectionClassName = newList.iterator().next().getClass().getName();
        }

        // Obtain (old MINUS new) and create "remove" actions
        for (final Long id : oldIds) {
          if (!newIds.contains(id)) {
            // Indicate movement of the object out of the component
            final AtomicAction atomicAction = new AtomicActionJpa();
            atomicAction.setField(IndexUtility.getFieldNameFromMethod(m, null));
            atomicAction.setIdType(IdType.getIdType(oldComponent));
            atomicAction.setClassName(newComponent.getClass().getName());
            atomicAction.setCollectionClassName(collectionClassName);
            atomicAction.setMolecularAction(molecularAction);
            atomicAction.setOldValue(id.toString());
            atomicAction.setNewValue(null);
            atomicAction.setObjectId(oldComponent.getId());
            final AtomicAction newAtomicAction = addAtomicAction(atomicAction);
            molecularAction.getAtomicActions().add(newAtomicAction);
          }
        }

        // Obtain (new MINUS old) and create "add" actions
        for (final Long id : newIds) {
          if (!oldIds.contains(id)) {
            // Indicate movement of the object intoof the component
            final AtomicAction atomicAction = new AtomicActionJpa();
            atomicAction.setField(IndexUtility.getFieldNameFromMethod(m, null));
            atomicAction.setIdType(IdType.getIdType(oldComponent));
            atomicAction.setClassName(newComponent.getClass().getName());
            atomicAction.setCollectionClassName(collectionClassName);
            atomicAction.setMolecularAction(molecularAction);
            atomicAction.setOldValue(null);
            atomicAction.setNewValue(id.toString());
            atomicAction.setObjectId(oldComponent.getId());
            final AtomicAction newAtomicAction = addAtomicAction(atomicAction);
            molecularAction.getAtomicActions().add(newAtomicAction);
          }
        }

      }
    }

    // handle as a normal "has last modified"
    updateHasLastModified(newComponent);

  }

  /**
   * Returns the component.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @return the component
   * @throws Exception the exception
   */
  protected <T extends Component> T getComponent(Long id, Class<T> clazz)
    throws Exception {
    if (id == null) {
      return null;
    }
    // Get transaction and object
    tx = manager.getTransaction();
    T component = manager.find(clazz, id);
    return component;
  }

  /**
   * Returns the component.
   *
   * @param <T> the
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param clazz the clazz
   * @return the component
   */
  @SuppressWarnings("unchecked")
  protected <T extends Component> T getComponent(String terminologyId,
    String terminology, String version, String branch, Class<T> clazz) {

    final List<T> results =
        getComponents(terminologyId, terminology, version, clazz);
    if (results.isEmpty()) {
      Logger.getLogger(getClass()).debug("  no " + clazz.getName());
      return null;
    }
    T defaultBranch = null;
    for (final T obj : results) {
      // handle default case
      if (obj.getBranch().equals(Branch.ROOT)) {
        defaultBranch = obj;
      }
      if (obj.getBranch().equals(branch)) {
        return obj;
      }
    }
    // If no matching branch is found, use default
    if (defaultBranch != null) {
      return defaultBranch;
    }
    // If nothing found, return null;
    return null;
  }

  /**
   * Removes the component.
   *
   * @param <T> the
   * @param id the id
   * @param clazz the clazz
   * @throws Exception the exception
   */
  protected <T extends Component> void removeComponent(Long id, Class<T> clazz)
    throws Exception {

    // check for molecular action flag
    if (isMolecularActionFlag()) {
      final MolecularAction molecularAction = getMolecularAction();

      // construct the atomic action
      final AtomicAction atomicAction = new AtomicActionJpa();
      atomicAction.setField("id");
      atomicAction.setIdType(IdType.getIdType(clazz));
      atomicAction.setClassName(clazz.getName());
      atomicAction.setMolecularAction(molecularAction);
      atomicAction.setObjectId(id);
      atomicAction.setOldValue(id.toString());
      atomicAction.setNewValue(null);
      final AtomicAction newAtomicAction = addAtomicAction(atomicAction);

      molecularAction.getAtomicActions().add(newAtomicAction);
    }

    removeHasLastModified(id, clazz);
  }

  /* see superclass */
  @Override
  public RelationshipList findConceptRelationships(String conceptId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find relationships for concept " + conceptId
            + "/" + terminology + "/" + version + "/" + branch + "/" + query
            + "/" + inverseFlag);

    return findRelationshipsForComponentHelper(conceptId, terminology, version,
        branch, query, inverseFlag, pfs, ConceptRelationshipJpa.class);
  }

  /* see superclass */
  @Override
  public RelationshipList findComponentInfoRelationships(String componentInfoId,
    String terminology, String version, IdType type, String branch,
    String query, boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find relationships for component info "
            + componentInfoId + "/" + terminology + "/" + version + "/" + branch
            + "/" + query + "/" + inverseFlag);

    if (inverseFlag) {
      return findRelationshipsForComponentHelper(componentInfoId, terminology,
          version, branch,
          ConfigUtility.isEmpty(query) ? "toType:" + type
              : query + " AND toType:" + type,
          inverseFlag, pfs, ComponentInfoRelationshipJpa.class);

    } else {
      return findRelationshipsForComponentHelper(componentInfoId, terminology,
          version, branch,
          ConfigUtility.isEmpty(query) ? "fromType:" + type
              : query + " AND fromType:" + type,
          inverseFlag, pfs, ComponentInfoRelationshipJpa.class);
    }
  }

  /* see superclass */
  @SuppressWarnings({
      "unchecked"
  })
  @Override
  public RelationshipList findConceptDeepRelationships(String conceptId,
    String terminology, String version, String branch, String filter,
    boolean inverseFlag, boolean includeConceptRels, boolean preferredOnly,
    boolean includeSelfReferential, PfsParameter pfs) throws Exception {

    // TODO: this could probably all be made faster with some more indexing
    Logger.getLogger(getClass())
        .debug("Content Service - find deep relationships for concept "
            + conceptId + "/" + terminology + "/" + version + "/" + filter);

    // Determine if skipping suppressible (and obsolete by definition)
    final String suppressibleClause = (pfs.getQueryRestriction() != null
        && pfs.getQueryRestriction().equals("suppressible:false"))
            ? " and a.suppressible = false " : "";

    final String relTypeClause = " and a.relationshipType not in ('AQ','QB')";

    // Verify no query restriction except for suppressible:false
    if (ConfigUtility.isEmpty(suppressibleClause) && pfs != null
        && pfs.getQueryRestriction() != null
        && !pfs.getQueryRestriction().isEmpty()) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    try {

      final Concept concept =
          getConcept(conceptId, terminology, version, branch);
      final List<Object[]> results = new ArrayList<>();

      String queryStr = null;
      javax.persistence.Query query = null;
      if (includeConceptRels) {
        queryStr = "select a.id, a.terminologyId, a.terminology, a.version, "
            + "a.relationshipType, a.additionalRelationshipType, "
            + (inverseFlag ? "a.from.terminologyId" : "a.to.terminologyId")
            + ", a.obsolete, a.suppressible, a.published, a.publishable, "
            + (inverseFlag ? "a.from.name " : "a.to.name ") + ", "
            + (inverseFlag ? "a.from.id " : "a.to.id ") + ", a.workflowStatus "
            + ", a.lastModifiedBy, a.lastModified "
            + "from ConceptRelationshipJpa a " + "where "
            + (inverseFlag ? "a.to" : "a.from") + ".id = :conceptId "
            + relTypeClause + suppressibleClause;
        query = manager.createQuery(queryStr);
        query.setParameter("conceptId", concept.getId());
        results.addAll(query.getResultList());
      }

      queryStr = "select a.id, a.terminologyId, a.terminology, a.version, "
          + "a.relationshipType, a.additionalRelationshipType, c2.terminologyId, "
          + "a.obsolete, a.suppressible, a.published, a.publishable, "
          // + (inverseFlag ? "a.from.name " : "a.to.name ") + ", c2.id "
          + "c2.name, c2.id " + ", a.workflowStatus "
          + ", a.lastModifiedBy, a.lastModified "
          + "from AtomRelationshipJpa a, ConceptJpa c2 join c2.atoms ca "
          + "where c2.terminology = :terminology and c2.version = :version and "
          + (inverseFlag ? "a.from.id in (ca.id) " : "a.to.id in (ca.id) ")
          + " and " + (inverseFlag ? "a.to" : "a.from") + ".id in (:atomIds)"
          + relTypeClause + suppressibleClause;
      query = manager.createQuery(queryStr);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      final Set<Long> atomIds = new HashSet<>();
      for (final Atom atom : concept.getAtoms()) {
        atomIds.add(atom.getId());
      }
      // If the concept has no atom ids, just put a bogus one so the query works
      if (atomIds.isEmpty()) {
        atomIds.add(-1L);
      }
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr = "select a.id, a.terminologyId, a.terminology, a.version, "
          + "a.relationshipType, a.additionalRelationshipType, c2.terminologyId,       "
          + "a.obsolete, a.suppressible, a.published, a.publishable, "
          // + (inverseFlag ? "a.from.name " : "a.to.name ") + ", c2.id "
          + "c2.name, c2.id " + ", a.workflowStatus "
          + ", a.lastModifiedBy, a.lastModified "
          + "from ConceptRelationshipJpa a, ConceptJpa b, AtomJpa c, "
          + "ConceptJpa d, AtomJpa e, ConceptJpa c2 join c2.atoms ca "
          + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
          + "and b.terminologyId = c.conceptId "
          + "and b.terminology = c.terminology and b.version = c.version "
          + "and b.name = c.name and c.id in (:atomIds) " + "and a."
          + (inverseFlag ? "from" : "to") + ".id = d.id "
          + "and d.terminologyId = e.conceptId "
          + "and d.terminology = e.terminology and d.version = e.version "
          + "and d.name = e.name "
          + "and c2.terminology = :terminology and c2.version = :version and "
          + (inverseFlag ? "e.id in (ca.id) " : "e.id in (ca.id) ")
          + relTypeClause + suppressibleClause;
      query = manager.createQuery(queryStr);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr = "select a.id, a.terminologyId, a.terminology, a.version, "
          + "a.relationshipType, a.additionalRelationshipType, c2.terminologyId,       "
          + "a.obsolete, a.suppressible, a.published, a.publishable, "
          // + (inverseFlag ? "a.from.name " : "a.to.name ") + ", c2.id "
          + "c2.name, c2.id " + ", a.workflowStatus "
          + ", a.lastModifiedBy, a.lastModified "
          + "from DescriptorRelationshipJpa a, DescriptorJpa b, AtomJpa c, "
          + "DescriptorJpa d, AtomJpa e, ConceptJpa c2 join c2.atoms ca "
          + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
          + "and b.terminologyId = c.descriptorId "
          + "and b.terminology = c.terminology and b.version = c.version "
          + "and b.name = c.name and c.id in (:atomIds) " + "and a."
          + (inverseFlag ? "from" : "to") + ".id = d.id "
          + "and d.terminologyId = e.descriptorId "
          + "and d.terminology = e.terminology and d.version = e.version "
          + "and d.name = e.name "
          + "and c2.terminology = :terminology and c2.version = :version and "
          + (inverseFlag ? "e.id in (ca.id) " : "e.id in (ca.id) ")
          + relTypeClause + suppressibleClause;
      query = manager.createQuery(queryStr);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr = "select a.id, a.terminologyId, a.terminology, a.version, "
          + "a.relationshipType, a.additionalRelationshipType, c2.terminologyId,       "
          + "a.obsolete, a.suppressible, a.published, a.publishable, "
          // + (inverseFlag ? "a.from.name " : "a.to.name ") + ", c2.id "
          + "c2.name, c2.id " + ", a.workflowStatus "
          + ", a.lastModifiedBy, a.lastModified "
          + "from CodeRelationshipJpa a, CodeJpa b, AtomJpa c, "
          + "CodeJpa d, AtomJpa e, ConceptJpa c2 join c2.atoms ca " + "where a."
          + (inverseFlag ? "to" : "from") + ".id = b.id "
          + "and b.terminologyId = c.codeId "
          + "and b.terminology = c.terminology and b.version = c.version "
          + "and b.name = c.name and c.id in (:atomIds) " + "and a."
          + (inverseFlag ? "from" : "to") + ".id = d.id "
          + "and d.terminologyId = e.codeId "
          + "and d.terminology = e.terminology and d.version = e.version "
          + "and d.name = e.name "
          + "and c2.terminology = :terminology and c2.version = :version and "
          + (inverseFlag ? "e.id in (ca.id) " : "e.id in (ca.id) ")
          + relTypeClause + suppressibleClause;
      query = manager.createQuery(queryStr);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      // Use a set to "uniq" them
      final Set<ConceptRelationship> conceptRels = new HashSet<>();
      for (final Object[] result : results) {
        final ConceptRelationship relationship = new ConceptRelationshipJpa();
        relationship.setId(Long.parseLong(result[0].toString()));
        final Concept relatedConcept = new ConceptJpa();
        relatedConcept.setTerminology(concept.getTerminology());
        relatedConcept.setVersion(concept.getVersion());
        relatedConcept.setTerminologyId(result[6].toString());
        relatedConcept.setId(Long.valueOf(result[12].toString()));
        relatedConcept.setName(result[11].toString());
        if (!inverseFlag) {
          relationship.setFrom(concept);
          relationship.setTo(relatedConcept);
        } else {
          relationship.setTo(concept);
          relationship.setFrom(relatedConcept);
        }
        relationship.setTerminologyId(result[1].toString());
        relationship.setTerminology(result[2].toString());
        relationship.setVersion(result[3].toString());
        relationship.setRelationshipType(result[4].toString());
        relationship.setHierarchical(result[4].toString().equals("CHD")
            || result[4].toString().equals("subClassOf"));
        relationship.setAdditionalRelationshipType(result[5].toString());
        relationship.setObsolete(result[7].toString().equals("true"));
        relationship.setSuppressible(result[8].toString().equals("true"));
        relationship.setPublished(result[9].toString().equals("true"));
        relationship.setPublishable(result[10].toString().equals("true"));
        relationship
            .setWorkflowStatus(WorkflowStatus.valueOf(result[13].toString()));
        // Force atom-rel demotions to not be equivalent to concept rels.
        // This is a hack, but is required for concept rels and atom rels to
        // both show up on ConceptReports
        if (relationship.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
          relationship.setTerminologyId(String.valueOf(relationship.getId()));
        }
        relationship.setLastModifiedBy(result[14].toString());
        relationship.setLastModified(
            new Date(((java.sql.Timestamp) result[15]).getTime()));

        // handle self-referential
        if (includeSelfReferential || !relationship.getFrom().getId()
            .equals(relationship.getTo().getId())) {
          conceptRels.add(relationship);
        }
      }

      List<ConceptRelationship> conceptRelList = new ArrayList<>();

      // Handle preferred only
      if (preferredOnly) {
        //
        final List<ConceptRelationship> tmpRelList =
            getComputePreferredNameHandler(terminology).sortRelationships(
                conceptRels, getPrecedenceList(terminology, version));
        final Set<Long> seen = new HashSet<>();
        for (final ConceptRelationship rel : tmpRelList) {
          if (rel.getWorkflowStatus() != WorkflowStatus.DEMOTION && !inverseFlag
              && seen.contains(rel.getTo().getId())) {
            continue;
          }
          if (rel.getWorkflowStatus() != WorkflowStatus.DEMOTION && inverseFlag
              && seen.contains(rel.getFrom().getId())) {
            continue;
          }
          seen.add(inverseFlag ? rel.getFrom().getId() : rel.getTo().getId());
          conceptRelList.add(rel);
        }

      } else {
        conceptRelList.addAll(conceptRels);
      }

      // set filter as query restriction for use in applyPfsToList
      final PfsParameter pfsLocal = new PfsParameterJpa(pfs);
      pfsLocal.setQueryRestriction(filter);

      final int[] totalCt = new int[1];
      conceptRelList = applyPfsToList(conceptRelList, ConceptRelationship.class,
          totalCt, pfsLocal);

      RelationshipList list = new RelationshipListJpa();
      list.setTotalCount(totalCt[0]);
      for (final ConceptRelationship cr : conceptRelList) {
        list.getObjects().add(cr);
      }

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public RelationshipList findDescriptorRelationships(String descriptorId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find relationships for descriptor "
            + descriptorId + "/" + terminology + "/" + version + "/" + branch
            + "/" + query + "/" + inverseFlag);

    return findRelationshipsForComponentHelper(descriptorId, terminology,
        version, branch, query, inverseFlag, pfs,
        DescriptorRelationshipJpa.class);

  }

  /* see superclass */
  @Override
  public RelationshipList findCodeRelationships(String codeId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find relationships for code " + codeId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query + "/"
            + inverseFlag);

    return findRelationshipsForComponentHelper(codeId, terminology, version,
        branch, query, inverseFlag, pfs, CodeRelationshipJpa.class);

  }

  /**
   * Find relationships for component helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param inverseFlag the inverse flag
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the relationship list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private RelationshipList findRelationshipsForComponentHelper(
    String terminologyId, String terminology, String version, String branch,
    String query, boolean inverseFlag, PfsParameter pfs,
    Class<? extends Relationship> clazz) throws Exception {

    final RelationshipList results = new RelationshipListJpa();

    final List<String> clauses = new ArrayList<>();
    // Parts to combine
    // 1. query
    clauses.add(query);

    // escape special chars
    // 2. to/fromTerminologyId
    if (inverseFlag && !ConfigUtility.isEmpty(terminologyId)) {
      clauses.add("toTerminologyId:" + QueryParserBase.escape(terminologyId));
    }
    if (!inverseFlag && !ConfigUtility.isEmpty(terminologyId)) {
      clauses.add("fromTerminologyId:" + QueryParserBase.escape(terminologyId));
    }

    // 3. to/fromTerminology
    if (inverseFlag && !ConfigUtility.isEmpty(terminology)) {
      clauses.add("toTerminology:" + terminology);
    }
    if (!inverseFlag && !ConfigUtility.isEmpty(terminology)) {
      clauses.add("fromTerminology:" + terminology);
    }

    // r. to/fromVersion clause
    if (inverseFlag && !ConfigUtility.isEmpty(version)) {
      clauses.add("toVersion:" + version);
    }
    if (!inverseFlag && !ConfigUtility.isEmpty(version)) {
      clauses.add("fromVersion:" + version);
    }

    final String finalQuery = ConfigUtility.composeQuery("AND", clauses);

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    // pass empty terminology/version because it's handled above
    results.setObjects((List) searchHandler.getQueryResults("", "", branch,
        finalQuery, "toNameSort", clazz, pfs, totalCt, manager));
    results.setTotalCount(totalCt[0]);

    // Removed due to NE-611 graph resolver was removing RUIs on inverse relationships
    // when approving concepts
    /*for (final Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : results
        .getObjects()) {
      getGraphResolutionHandler(terminology).resolve(rel);
    }*/
    return results;

  }

  /**
   * Find mappings for component helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @return the mapping list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private MappingList findMappingsForComponentHelper(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {

    if (terminologyId == null || terminologyId.isEmpty()) {
      throw new Exception("Terminology id is required");
    }

    final MappingList results = new MappingListJpa();

    // Prepare the query string
    final StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }

    finalQuery.append("fromTerminologyId:"
        + QueryParserBase.escape(terminologyId) + " AND fromTerminology:"
        + terminology + " AND fromVersion:" + version);

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    // pass empty terminology/version because it's handled above
    results.setObjects((List) searchHandler.getQueryResults("", "", branch,
        finalQuery.toString(), "fromNameSort", MappingJpa.class, pfs, totalCt,
        manager));
    results.setTotalCount(totalCt[0]);

    for (final Mapping mapping : results.getObjects()) {
      getGraphResolutionHandler(terminology).resolve(mapping);
    }
    return results;

  }

  /**
   * Find tree positions helper.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfs the pfs
   * @param clazz the clazz
   * @return the tree position list
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "unchecked", "rawtypes"
  })
  private TreePositionList findTreePositionsHelper(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs, Class<? extends TreePosition> clazz) throws Exception {

    // Prepare the query string
    final StringBuilder finalQuery = new StringBuilder();

    // add the query, if not null and not empty
    finalQuery.append(query == null || query.isEmpty() ? "" : query);
    if (terminologyId != null) {
      if (finalQuery.length() > 0) {
        finalQuery.append(" AND ");
      }
      finalQuery
          .append("nodeTerminologyId:" + QueryParserBase.escape(terminologyId));
    }

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];
    final TreePositionList list = new TreePositionListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, finalQuery.toString(), "nodeNameSort", clazz, pfs, totalCt,
        manager));
    list.setTotalCount(totalCt[0]);

    // If the list has <30 entries and all are roman numerals
    // and the sortField is "nodeTerminologyId" then use a roman numeral sort
    // This is a hack for roman numeral sorted top-level hierarchies
    if (list.size() < 30 && pfs != null && pfs.getSortField() != null
        && pfs.getSortField().equals("nodeTerminologyId")) {
      boolean nonRomanFound = false;
      for (final TreePosition treepos : list.getObjects()) {
        if (!ConfigUtility
            .isRomanNumeral(treepos.getNode().getTerminologyId())) {
          nonRomanFound = true;
          break;
        }
      }
      if (!nonRomanFound) {
        Collections.sort(list.getObjects(), new Comparator<TreePosition>() {
          @Override
          public int compare(TreePosition o1, TreePosition o2) {
            try {
              return ConfigUtility.toArabic(o1.getNode().getTerminologyId())
                  - ConfigUtility.toArabic(o2.getNode().getTerminologyId());
            } catch (Exception e) {
              // just return zero, don't worry about handling the error
              e.printStackTrace();
              return 0;
            }
          }

        });
      }
    }
    return list;

  }

  /* see superclass */
  @Override
  public SearchResultList findCodesForGeneralQuery(String luceneQuery,
    String JPQLQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find codes " + luceneQuery + "/" + JPQLQuery + "/");
    return findForGeneralQueryHelper(luceneQuery, JPQLQuery, branch, pfs,
        CodeJpa.class);
  }

  /* see superclass */

  @Override
  public SearchResultList findConceptsForGeneralQuery(String luceneQuery,
    String JPQLQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find concepts "
        + luceneQuery + "/" + JPQLQuery + "/");
    return findForGeneralQueryHelper(luceneQuery, JPQLQuery, branch, pfs,
        ConceptJpa.class);
  }

  /* see superclass */

  @Override
  public SearchResultList findDescriptorsForGeneralQuery(String luceneQuery,
    String JPQLQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - find descriptors "
        + luceneQuery + "/" + JPQLQuery + "/");
    return findForGeneralQueryHelper(luceneQuery, JPQLQuery, branch, pfs,
        DescriptorJpa.class);
  }

  /* see superclass */

  @SuppressWarnings("unchecked")
  @Override
  public Tree getTreeForTreePosition(TreePosition<?> treePosition)
    throws Exception {
    Logger.getLogger(getClass())
        .info("Content Service - get tree for tree position "
            + treePosition.getNode().getId() + ", "
            + treePosition.getAncestorPath());

    Long tpId = treePosition.getNode().getId();

    // Determine type
    Class<?> clazz = treePosition.getClass();
    Logger.getLogger(getClass()).debug("  type = " + clazz.getName());

    // tree to return
    Tree tree = null;

    // the current tree variables (ancestor path and local tree)
    // initially top-level
    String partAncPath = "";
    // initially the empty tree
    Tree parentTree = tree;

    // Prepare lucene
    final FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    final SearchFactory searchFactory =
        fullTextEntityManager.getSearchFactory();
    final QueryParser queryParser = new MultiFieldQueryParser(
        IndexUtility.getIndexedFieldNames(clazz, true).toArray(new String[] {}),
        searchFactory.getAnalyzer(clazz));
    final String fullAncPath = treePosition.getAncestorPath()
        + (treePosition.getAncestorPath().isEmpty() ? "" : "~") + tpId;
    // Iterate over ancestor path
    for (final String pathPart : fullAncPath.split("~")) {
      final Long partId = Long.parseLong(pathPart);

      final StringBuilder finalQuery = new StringBuilder();
      finalQuery.append("nodeId:" + partId + " AND terminology:"
          + treePosition.getTerminology() + " AND version:"
          + treePosition.getVersion() + " AND ");
      if (partAncPath.isEmpty()) {
        // query for empty value
        finalQuery.append("NOT ancestorPath:[* TO *]");
      } else {
        finalQuery.append("ancestorPath:\"" + partAncPath + "\"");
      }

      // Prepare the manager and lucene query

      final Query luceneQuery = queryParser.parse(finalQuery.toString());
      final FullTextQuery fullTextQuery =
          fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);

      // // projection approach -- don't want to have to instantiate node Jpa
      // object (could be faster)
      // fullTextQuery.setProjection("nodeId", "nodeTerminologyId", "nodeName",
      // "childCt", "ancestorPath");
      //
      // List<Object[]> results = fullTextQuery.getResultList();
      //
      // if (fullTextQuery.getResultSize() != 1) {
      // throw new Exception("Unexpected number of results: "
      // + fullTextQuery.getResultSize());
      // }
      // Object[] result = results.get(0);
      //
      // // fill in the tree object
      // partTree.setId((Long) result[0]);
      // partTree.setTerminologyId((String) result[1]);
      // partTree.setName((String) result[2]);
      // partTree.setChildCt((Integer) result[3]);
      // partTree.setAncestorPath((String) result[4]);
      // partTree.setTerminology(treePosition.getTerminology());
      // partTree.setVersion(treePosition.getVersion());

      // this is necessary because additionalRelationshipType is not indexed
      TreePosition<?> treepos = null;
      int ct = 0;
      final String treePositionRela =
          ConfigUtility.isEmpty(treePosition.getAdditionalRelationshipType())
              ? "" : treePosition.getAdditionalRelationshipType();
      for (final TreePosition<?> tp : (List<TreePosition<?>>) fullTextQuery
          .getResultList()) {
        final String tpRela =
            ConfigUtility.isEmpty(tp.getAdditionalRelationshipType()) ? ""
                : tp.getAdditionalRelationshipType();
        if (tpRela.equals(treePositionRela)) {
          ct++;
          treepos = tp;
        }
      }

      // original approach
      if (ct != 1) {
        throw new Exception("Unexpected number of results: " + ct + ", "
            + partId + ", " + partAncPath + ", " + clazz);
      }

      final Tree partTree = new TreeJpa(treepos);

      if (tree == null) {
        tree = partTree;
      }

      if (parentTree != null) {
        parentTree.addChild(partTree);
      }

      // set parent tree to the just constructed
      parentTree = partTree;

      partAncPath += (partAncPath.equals("") ? "" : "~");
      partAncPath += pathPart;
    }

    return tree;
  }

  /* see superclass */
  @SuppressWarnings("rawtypes")
  @Override
  public TreePositionList findTreePositions(String terminologyId,
    String terminology, String version, String branch, String query,
    Class<? extends TreePosition> clazz, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("Content Service - find concept tree positions " + terminologyId
            + "/" + terminology + "/" + version + ", " + query);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        query, pfs, clazz);
  }

  /* see superclass */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public TreePositionList findTreePositionChildren(Long nodeId,
    String terminologyId, String terminology, String version, String branch,
    Class<? extends TreePosition> clazz, PfsParameter pfs) throws Exception {

    Logger.getLogger(getClass())
        .info("Content Service - find children of a tree position " + nodeId
            + ", " + terminologyId + "/" + terminology + "/" + version);

    final PfsParameter childPfs = new PfsParameterJpa();
    childPfs.setStartIndex(0);
    childPfs.setMaxResults(1);
    // get a tree position for each child, for child ct
    TreePositionList tpList = null;
    if (nodeId != null) {
      tpList = findTreePositionsHelper(null, null, null, branch,
          "nodeId:" + nodeId, childPfs, clazz);
    } else {
      tpList = findTreePositionsHelper(terminologyId, terminology, version,
          branch, "", childPfs, clazz);
    }
    if (tpList.size() == 0) {
      return new TreePositionListJpa();
    }
    final TreePosition<?> treePosition = tpList.getObjects().get(0);

    // TODO: need to deal with terminologies like MSH that don't have computable
    // hierarchies
    final Long tpId = treePosition.getNode().getId();
    final String fullAncPath = treePosition.getAncestorPath()
        + (treePosition.getAncestorPath().isEmpty() ? "" : "~") + tpId;

    final String query = "ancestorPath:\"" + fullAncPath + "\"";

    final SearchHandler searchHandler = getSearchHandler(terminology);
    final int[] totalCt = new int[1];

    final TreePositionList list = new TreePositionListJpa();
    list.setObjects((List) searchHandler.getQueryResults(terminology, version,
        branch, query, null, clazz, pfs, totalCt, manager));
    list.setTotalCount(totalCt[0]);

    // If the list has <30 entries and all are roman numerals
    // and the sortField is "nodeTerminologyId" then use a roman numeral sort
    if (list.size() < 30 && pfs != null && pfs.getSortField() != null
        && pfs.getSortField().equals("nodeTerminologyId")) {
      boolean nonRomanFound = false;
      for (final TreePosition treepos : list.getObjects()) {
        if (!ConfigUtility
            .isRomanNumeral(treepos.getNode().getTerminologyId())) {
          nonRomanFound = true;
          break;
        }
      }
      if (!nonRomanFound) {
        Collections.sort(list.getObjects(), new Comparator<TreePosition>() {
          @Override
          public int compare(TreePosition o1, TreePosition o2) {
            try {
              return ConfigUtility.toArabic(o1.getNode().getTerminologyId())
                  - ConfigUtility.toArabic(o2.getNode().getTerminologyId());
            } catch (Exception e) {
              // just return zero, don't worry about handling the error
              e.printStackTrace();
              return 0;
            }
          }

        });
      }
    }

    return list;
  }

  /* see superclass */
  @Override
  public GeneralConceptAxiom addGeneralConceptAxiom(GeneralConceptAxiom axiom)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add axiom " + axiom);
    // No need to worry about assigning ids.

    return addComponent(axiom);

  }

  /* see superclass */
  @Override
  public void updateGeneralConceptAxiom(GeneralConceptAxiom axiom)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update axiom " + axiom);
    // update component
    updateComponent(axiom);

  }

  /* see superclass */
  @Override
  public void removeGeneralConceptAxiom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove axiom " + id);

    removeComponent(id, GeneralConceptAxiomJpa.class);

  }

  /* see superclass */
  @Override
  public GeneralConceptAxiomList getGeneralConceptAxioms(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get general concept axioms " + terminology
            + "/" + version);
    final javax.persistence.Query query =
        manager.createQuery("select a from GeneralConceptAxiomJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<GeneralConceptAxiom> m = query.getResultList();
      final GeneralConceptAxiomListJpa generalConceptAxiomList =
          new GeneralConceptAxiomListJpa();
      generalConceptAxiomList.setObjects(m);
      generalConceptAxiomList.setTotalCount(m.size());

      return generalConceptAxiomList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public ExpressionHandler getExpressionHandler(String terminology,
    String version) throws Exception {

    // NOTE: Only ECL expression searching currently supported.
    return new EclExpressionHandler(terminology, version);
  }

  /* see superclass */
  @Override
  public Mapping addMapping(Mapping mapping) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add mapping " + mapping);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(mapping.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + mapping.getTerminology());
      }
      String id = idHandler.getTerminologyId(mapping);
      mapping.setTerminologyId(id);
    }

    // Add component
    return addComponent(mapping);

  }

  /* see superclass */
  @Override
  public void updateMapping(Mapping mapping) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update mapping " + mapping);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(mapping.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Mapping mapping2 = getMapping(mapping.getId());
        if (!idHandler.getTerminologyId(mapping)
            .equals(idHandler.getTerminologyId(mapping2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set mapping id on update
        mapping.setTerminologyId(idHandler.getTerminologyId(mapping));
      }
    }
    // update component
    updateComponent(mapping);

  }

  /* see superclass */
  @Override
  public void removeMapping(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove mapping " + id);
    // Remove the component
    removeComponent(id, MappingJpa.class);

  }

  /* see superclass */
  @Override
  public MapSet getMapSet(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get mapSet " + id);
    return getComponent(id, MapSetJpa.class);
  }

  /* see superclass */
  @Override
  public MapSet getMapSet(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get mapset "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        MapSetJpa.class);
  }

  /* see superclass */
  @Override
  public MapSetList getMapSets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get mapsets " + terminology + "/" + version);
    javax.persistence.Query query;
    String queryStr = "select a from MapSetJpa a ";
    if (terminology != null && version != null) {
      queryStr += " where version = :version and terminology = :terminology";
      query = manager.createQuery(queryStr);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
    } else {
      query = manager.createQuery(queryStr);
    }
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      @SuppressWarnings("unchecked")
      final List<MapSet> m = query.getResultList();
      final MapSetListJpa mapSetList = new MapSetListJpa();
      mapSetList.setObjects(m);
      mapSetList.setTotalCount(m.size());

      return mapSetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public Mapping getMapping(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get mapping " + id);
    return getComponent(id, MappingJpa.class);
  }

  /* see superclass */
  @Override
  public Mapping getMapping(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get mapping "
        + terminologyId + "/" + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        MappingJpa.class);
  }

  /* see superclass */
  @SuppressWarnings({
      "unchecked"
  })
  @Override
  public MappingList findMappings(Long mapSetId, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find mappings " + mapSetId + ", query=" + query);

    final StringBuilder sb = new StringBuilder();
    if (query != null && !query.equals("")) {
      sb.append(query).append(" AND ");
    }
    if (mapSetId == null) {
      sb.append("mapSetId:[* TO *]");
    } else {
      sb.append("mapSetId:" + mapSetId);
    }

    int[] totalCt = new int[1];
    final List<Mapping> list = (List<Mapping>) getQueryResults(sb.toString(),
        MappingJpa.class, pfs, totalCt);
    final MappingList result = new MappingListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
    return result;
  }

  /* see superclass */
  @Override
  public MappingList findConceptMappings(String conceptId, String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find mappings for concept " + conceptId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query);

    return findMappingsForComponentHelper(conceptId, terminology, version,
        branch, query, pfs);
  }

  /* see superclass */
  @Override
  public MappingList findCodeMappings(String codeId, String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find mappings for code " + codeId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query);

    return findMappingsForComponentHelper(codeId, terminology, version, branch,
        query, pfs);
  }

  /* see superclass */
  @Override
  public MappingList findDescriptorMappings(String descriptorId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find mappings for descriptor " + descriptorId
            + "/" + terminology + "/" + version + "/" + branch + "/" + query);

    return findMappingsForComponentHelper(descriptorId, terminology, version,
        branch, query, pfs);
  }

  /* see superclass */
  @Override
  public MapSet addMapSet(MapSet mapSet) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add mapSet " + mapSet);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(mapSet.getTerminology());
      if (idHandler == null) {
        throw new Exception(
            "Unable to find id handler for " + mapSet.getTerminology());
      }
      String id = idHandler.getTerminologyId(mapSet);
      mapSet.setTerminologyId(id);
    }

    // Add component
    return addComponent(mapSet);
  }

  /* see superclass */
  @Override
  public void updateMapSet(MapSet mapSet) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - update mapSet " + mapSet);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(mapSet.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        MapSet mapSet2 = getMapSet(mapSet.getId());
        if (!idHandler.getTerminologyId(mapSet)
            .equals(idHandler.getTerminologyId(mapSet2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set mapSet id on update
        mapSet.setTerminologyId(idHandler.getTerminologyId(mapSet));
      }
    }
    // update component
    updateComponent(mapSet);

  }

  /* see superclass */
  @Override
  public void removeMapSet(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove mapSet " + id);
    // Remove the component
    removeComponent(id, MapSetJpa.class);

  }

  /* see superclass */
  @Override
  public Map<Long, String> getTerminologyIdMap(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get id to terminology id map " + terminology
            + "/" + version);

    Terminology terminologyObj = getTerminology(terminology, version);

    String tableName = null;
    switch (terminologyObj.getOrganizingClassType()) {
      case CODE:
        tableName = "CodeJpa";
        break;
      case CONCEPT:
        tableName = "ConceptJpa";
        break;
      case DESCRIPTOR:
        tableName = "DescriptorJpa";
        break;
      default:
        throw new Exception(
            "Could not determine organizing class type for terminology");

    }

    javax.persistence.Query query = manager.createQuery(
        "select a.id, a.terminologyId from " + tableName + " a where "
            + "version = :version and terminology = :terminology");

    Map<Long, String> idMap = new HashMap<>();

    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      final List<Object[]> rows = query.getResultList();
      for (final Object[] row : rows) {
        idMap.put((Long) row[0], row[1].toString());
      }

      return idMap;

    } catch (NoResultException e) {
      return null;
    }
  }

  /* see superclass */
  @Override
  public Note getNote(Long id, Class<? extends Note> noteClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get note " + id);
    return manager.find(noteClass, id);
  }

  /* see superclass */
  @Override
  public Note addNote(Note note) throws Exception {

    Logger.getLogger(getClass())
        .debug("Content Service - add userNote " + note.toString());

    // Add component
    Note newNote = addHasLastModified(note);

    // Note-specific handling
    // check that a molecular action exists
    MolecularAction molecularAction = null;
    try {
      molecularAction = getMolecularAction();
    } catch (Exception e) {
      // do nothing
    }
    if (molecularAction != null) {

      // construct the atomic action

      final AtomicAction atomicAction = new AtomicActionJpa();
      atomicAction.setField("id");
      atomicAction.setIdType(IdType.NOTE);
      atomicAction.setClassName(note.getClass().getName());
      atomicAction.setMolecularAction(molecularAction);
      atomicAction.setOldValue(null);
      atomicAction.setNewValue(note.getId().toString());
      atomicAction.setObjectId(note.getId());

      // persist the atomic action and add the persisted version to the
      // molecular action
      final AtomicAction newAtomicAction = addAtomicAction(atomicAction);

      molecularAction.getAtomicActions().add(newAtomicAction);

    }

    // do not inform listeners
    return newNote;
  }

  /* see superclass */
  @Override
  public void removeNote(Long id, Class<? extends Note> type) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove userNote " + id);

    // check that a molecular action exists
    MolecularAction molecularAction = null;
    try {
      molecularAction = getMolecularAction();
    } catch (Exception e) {
      // do nothing
    }
    if (molecularAction != null) {
      // construct the atomic action
      final AtomicAction atomicAction = new AtomicActionJpa();
      atomicAction.setField("id");
      atomicAction.setIdType(IdType.NOTE);
      atomicAction.setClassName(type.getName());
      atomicAction.setMolecularAction(molecularAction);
      atomicAction.setObjectId(id);
      atomicAction.setOldValue(id.toString());
      atomicAction.setNewValue(null);
      final AtomicAction newAtomicAction = addAtomicAction(atomicAction);

      molecularAction.getAtomicActions().add(newAtomicAction);
    }

    // Remove the note
    removeHasLastModified(id, type);

  }

  /* see superclass */
  @Override
  public NoteList findConceptNotes(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find concept notes " + query + ", " + pfs);
    final NoteList results = new NoteListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<ConceptNoteJpa> luceneResults = searchHandler.getQueryResults(
        null, null, "", query, "", ConceptNoteJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final ConceptNoteJpa note : luceneResults) {
      results.getObjects().add(note);
    }
    return results;
  }

  /* see superclass */
  @Override
  public NoteList findDescriptorNotes(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find description notes " + query + ", " + pfs);
    final NoteList results = new NoteListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<DescriptorNoteJpa> luceneResults =
        searchHandler.getQueryResults(null, null, "", query, "",
            DescriptorNoteJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final DescriptorNoteJpa note : luceneResults) {
      results.getObjects().add(note);
    }
    return results;
  }

  /* see superclass */
  @Override
  public NoteList findCodeNotes(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - find code notes " + query + ", " + pfs);
    final NoteList results = new NoteListJpa();
    final SearchHandler searchHandler = getSearchHandler(null);
    final int[] totalCt = new int[1];
    final List<CodeNoteJpa> luceneResults = searchHandler.getQueryResults(null,
        null, "", query, "", CodeNoteJpa.class, pfs, totalCt, manager);
    results.setTotalCount(totalCt[0]);
    for (final CodeNoteJpa note : luceneResults) {
      results.getObjects().add(note);
    }

    return results;
  }

  /* see superclass */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public TreePositionList findConceptDeepTreePositions(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {

    Logger.getLogger(getClass())
        .debug("Content Service - find tree positions for concept "
            + terminologyId + "/" + terminology + "/" + version + "/" + query);

    final Concept concept =
        this.getConcept(terminologyId, terminology, version, branch);
    if (concept == null) {
      return null;
    }

    List<TreePosition> treePositionList = new ArrayList<>();

    // Check for atom tree positions
    final List<String> clauses = concept.getAtoms().stream()
        .map(a -> "nodeId:" + a.getId()).collect(Collectors.toList());
    if (clauses.isEmpty()) {
      return new TreePositionListJpa();
    }
    final TreePositionList atomTrees = findTreePositions(null, null, null,
        Branch.ROOT, ConfigUtility.composeQuery("OR", clauses),
        AtomTreePositionJpa.class, null);
    final Set<Long> nodesSeen = new HashSet<>();
    final Set<String> terminologiesSeen = new HashSet<>();
    for (final TreePosition tp : atomTrees.getObjects()) {
      // keep the first one encountered
      if (!nodesSeen.contains(tp.getNode().getId())) {
        treePositionList.add(tp);
      }
      nodesSeen.add(tp.getNode().getId());
      terminologiesSeen.add(tp.getNode().getTerminology());
    }

    final Set<String> seen = new HashSet<>();
    // collect all unique terminology, version, terminologyId, type combos
    // from atoms in concept - ATOMS are in order
    for (final Atom atom : concept.getAtoms()) {

      // Skip things already processed for having atom trees
      if (terminologiesSeen.contains(atom.getTerminology())) {
        continue;
      }

      String lterminologyId = null;
      Class<? extends TreePosition> clazz;
      String type;
      // Try descriptor, then concept, then code
      if (!atom.getDescriptorId().equals("")) {
        type = "descriptor";
        lterminologyId = atom.getDescriptorId();
        clazz = DescriptorTreePositionJpa.class;
      } else if (!atom.getConceptId().equals("")) {
        type = "concept";
        lterminologyId = atom.getConceptId();
        clazz = ConceptTreePositionJpa.class;
      } else {
        type = "code";
        lterminologyId = atom.getCodeId();
        clazz = CodeTreePositionJpa.class;
      }

      final String entry = type + ":" + atom.getTerminology() + ":"
          + atom.getVersion() + ":" + lterminologyId;

      // Try to find it if we haven't seen one yet
      if (!seen.contains(entry)) {
        // Break if we've reached the limit
        if (treePositionList.size() >= 100) {
          break;
        }

        // See if there is a tree position
        final TreePosition<?> treePos = getFirstTreePosition(lterminologyId,
            atom.getTerminology(), atom.getVersion(), clazz);
        // Increment if so
        if (treePos != null) {
          // handle lazy init
          treePos.setAttributes(new ArrayList<>(0));
          treePositionList.add(treePos);
        }
      }
      seen.add(entry);
    }

    // set filter as query restriction for use in applyPfsToList
    final PfsParameter pfsLocal = new PfsParameterJpa(pfs);
    pfsLocal.setQueryRestriction(query);
    pfsLocal.setSortField("terminology");

    final int[] totalCt = new int[1];
    treePositionList =
        applyPfsToList(treePositionList, TreePosition.class, totalCt, pfsLocal);

    // need to copy list again
    final TreePositionList list = new TreePositionListJpa();
    list.setTotalCount(totalCt[0]);
    list.getObjects().addAll((List) treePositionList);

    return list;

  }

  /**
   * Returns the first tree position for the specified parameters.
   *
   * @param terminologyId the terminology id
   * @param terminology the terminology
   * @param version the version
   * @param clazz the clazz
   * @return the tree position
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  private TreePosition<?> getFirstTreePosition(String terminologyId,
    String terminology, String version, Class<? extends TreePosition> clazz)
    throws Exception {
    // for each unique entry, get all tree positions
    final PfsParameter singleResultPfs = new PfsParameterJpa();
    singleResultPfs.setStartIndex(0);
    singleResultPfs.setMaxResults(1);
    final TreePositionList list = findTreePositions(terminologyId, terminology,
        version, null, null, clazz, singleResultPfs);
    if (list.size() > 0) {
      return list.getObjects().get(0);
    }
    return null;

  }

  /* see superclass */
  @Override
  public ValidationResult validateConcept(List<String> validationChecks,
    Concept concept) {
    final ValidationResult result = new ValidationResultJpa();
    for (final String key : getValidationHandlersMap().keySet()) {
      if (validationChecks.contains(key)) {
        result.merge(getValidationHandlersMap().get(key).validate(concept));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public Set<Long> validateConcepts(Project project, String check,
    Set<Long> conceptIds) throws Exception {
    Logger.getLogger(getClass()).info("  Validate all concepts");
    final Set<Long> failures = new HashSet<>();
    for (final String key : getValidationHandlersMap().keySet()) {
      if (project.getValidationChecks().contains(key)
          && (check == null || check.equals(key))) {
        final Set<Long> failedCheck =
            getValidationHandlersMap().get(key).validateConcepts(conceptIds,
                project.getTerminology(), project.getVersion(), this);
        Logger.getLogger(getClass())
            .info("    " + key + " ct = " + failedCheck.size());
        failures.addAll(failedCheck);
      }
    }
    return failures;
  }

  /* see superclass */
  @Override
  public ValidationResult validateAtom(List<String> validationChecks,
    Atom atom) {
    final ValidationResult result = new ValidationResultJpa();
    for (final String key : getValidationHandlersMap().keySet()) {
      if (validationChecks.contains(key)) {
        result.merge(getValidationHandlersMap().get(key).validate(atom));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateDescriptor(List<String> validationChecks,
    Descriptor descriptor) {
    final ValidationResult result = new ValidationResultJpa();
    for (final String key : getValidationHandlersMap().keySet()) {
      if (validationChecks.contains(key)) {
        result.merge(getValidationHandlersMap().get(key).validate(descriptor));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public ValidationResult validateCode(List<String> validationChecks,
    Code code) {
    final ValidationResult result = new ValidationResultJpa();
    for (final String key : getValidationHandlersMap().keySet()) {
      if (validationChecks.contains(key)) {
        result.merge(getValidationHandlersMap().get(key).validate(code));
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    super.refreshCaches();
    init();
    validateInit();
  }

  /**
   * Validate init.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  private void validateInit() throws Exception {
    if (idHandlerMap == null) {
      throw new Exception(
          "Identifier assignment handler did not properly initialize, serious error.");
    }

    if (pnHandlerMap == null) {
      throw new Exception(
          "Preferred name handler did not properly initialize, serious error.");
    }

    if (normalizedStringHandler == null) {
      throw new Exception(
          "Normalized string handler did not properly initialize, serious error.");
    }

    if (!searchHandlerNames.contains(ConfigUtility.ATOMCLASS)) {
      throw new Exception("search.handler." + ConfigUtility.ATOMCLASS
          + " expected and does not exist.");
    }
  }

}
