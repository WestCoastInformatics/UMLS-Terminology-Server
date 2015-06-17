/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.NoResultException;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PfscParameter;
import com.wci.umls.server.helpers.SearchCriteria;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.AtomList;
import com.wci.umls.server.helpers.content.AttributeList;
import com.wci.umls.server.helpers.content.CodeList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.DescriptorList;
import com.wci.umls.server.helpers.content.LexicalClassList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.helpers.content.StringClassList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.content.AbstractComponent;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.CodeRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.CodeTreePositionJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.ConceptTreePositionJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.DescriptorRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTransitiveRelationshipJpa;
import com.wci.umls.server.jpa.content.DescriptorTreePositionJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.helpers.IndexUtility;
import com.wci.umls.server.jpa.helpers.SearchResultJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.helpers.content.AtomListJpa;
import com.wci.umls.server.jpa.helpers.content.AttributeListJpa;
import com.wci.umls.server.jpa.helpers.content.CodeListJpa;
import com.wci.umls.server.jpa.helpers.content.ConceptListJpa;
import com.wci.umls.server.jpa.helpers.content.DescriptorListJpa;
import com.wci.umls.server.jpa.helpers.content.LexicalClassListJpa;
import com.wci.umls.server.jpa.helpers.content.RelationshipListJpa;
import com.wci.umls.server.jpa.helpers.content.StringClassListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetMemberListJpa;
import com.wci.umls.server.jpa.helpers.content.TreeJpa;
import com.wci.umls.server.jpa.helpers.content.TreePositionListJpa;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
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
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ComputePreferredNameHandler;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * JPA enabled implementation of {@link ContentService}.
 */
public class ContentServiceJpa extends MetadataServiceJpa implements
    ContentService {

  /** The assign identifiers flag. */
  protected boolean assignIdentifiersFlag = false;

  /** The id assignment handler . */
  public static Map<String, IdentifierAssignmentHandler> idHandlerMap =
      new HashMap<>();
  
  /** The commit ct. */
  private int commitCt = 2000;
  
  /** The log ct. */
  private int logCt = 2000;

  
  static {

    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "identifier.assignment.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        IdentifierAssignmentHandler handlerService =
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
  }

  /** The helper map. */
  private static Map<String, ComputePreferredNameHandler> pnHandlerMap = null;
  static {
    pnHandlerMap = new HashMap<>();

    try {
      config = ConfigUtility.getConfigProperties();
      String key = "compute.preferred.name.handler";
      for (String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        ComputePreferredNameHandler handlerService =
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
  }

  /** The normalized string handler. */
  private static NormalizedStringHandler normalizedStringHandler = null;
  static {
    try {
      config = ConfigUtility.getConfigProperties();
      String key = "normalized.string.handler";
      String handlerName = config.getProperty(key);

      NormalizedStringHandler handlerService =
          ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
              handlerName, NormalizedStringHandler.class);
      normalizedStringHandler = handlerService;
    } catch (Exception e) {
      e.printStackTrace();
      normalizedStringHandler = null;
    }
  }

  /** The string field names map. */
  private static Map<Class<?>, Set<String>> stringFieldNames = new HashMap<>();

  /** The field names map. */
  private static Map<Class<?>, Set<String>> allFieldNames = new HashMap<>();

  static {

    try {

      Class<?>[] classes =
          new Class<?>[] {
              ConceptJpa.class, DescriptorJpa.class, CodeJpa.class,
              ConceptRelationshipJpa.class, ConceptSubsetMemberJpa.class,
              ConceptTreePositionJpa.class
          };

      for (Class<?> clazz : classes) {
        stringFieldNames.put(clazz,
            IndexUtility.getIndexedStringFieldNames(clazz, true));
        allFieldNames.put(clazz,
            IndexUtility.getIndexedStringFieldNames(clazz, false));
      }
      System.out.println("stringFieldNames = " + stringFieldNames);
      System.out.println("allFieldNames = " + allFieldNames);
    } catch (Exception e) {
      e.printStackTrace();
      stringFieldNames = null;
    }
  }

  /**
   * Instantiates an empty {@link ContentServiceJpa}.
   *
   * @throws Exception the exception
   */
  public ContentServiceJpa() throws Exception {
    super();

    if (listeners == null) {
      throw new Exception(
          "Listeners did not properly initialize, serious error.");
    }

    if (idHandlerMap == null) {
      throw new Exception(
          "Identifier assignment handler did not properly initialize, serious error.");
    }

    if (pnHandlerMap == null) {
      throw new Exception(
          "Preferred name handler did not properly initialize, serious error.");
    }

    if (stringFieldNames == null) {
      throw new Exception(
          "Concept indexed field names did not properly initialize, serious error.");
    }

    if (normalizedStringHandler == null) {
      throw new Exception(
          "Normalized string handler did not properly initialize, serious error.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getConcept(java.lang.Long)
   */
  @Override
  public Concept getConcept(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get concept " + id);
    return getComponent(id, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getConcepts(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public ConceptList getConcepts(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concepts " + terminologyId + "/" + terminology
            + "/" + version);
    List<Concept> concepts =
        getComponents(terminologyId, terminology, version, ConceptJpa.class);
    if (concepts == null) {
      return null;
    }
    ConceptList list = new ConceptListJpa();
    list.setTotalCount(concepts.size());
    list.setObjects(concepts);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getConcept(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Concept getConcept(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concept " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addConcept(com.wci.umls.server
   * .model.content.Concept)
   */
  @Override
  public Concept addConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add concept " + concept);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(concept.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + concept.getTerminology());
      }
      String id = idHandler.getTerminologyId(concept);
      concept.setTerminologyId(id);
    }

    // Add component
    Concept newConcept = addComponent(concept);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.conceptChanged(newConcept, WorkflowListener.Action.ADD);
      }
    }
    return newConcept;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateConcept(com.wci.umls.
   * server.model.content.Concept)
   */
  @Override
  public void updateConcept(Concept concept) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update concept " + concept);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(concept.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowConceptIdChangeOnUpdate()) {
        Concept concept2 = getConcept(concept.getId());
        if (!idHandler.getTerminologyId(concept).equals(
            idHandler.getTerminologyId(concept2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set concept id on update
        concept.setTerminologyId(idHandler.getTerminologyId(concept));
      }
    }
    // update component
    this.updateComponent(concept);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.conceptChanged(concept, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeConcept(java.lang.Long)
   */
  @Override
  public void removeConcept(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - remove concept " + id);
    // Remove the component
    Concept concept = removeComponent(id, ConceptJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.conceptChanged(concept, WorkflowListener.Action.REMOVE);
      }
    }
  }

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

  @Override
  public Subset getSubset(String terminologyId, String terminology,
    String version, String branch, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    if (subsetClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          subsetClass);
    } else {
      Subset subset =
          getComponent(terminologyId, terminology, version, branch,
              AtomSubsetJpa.class);
      if (subset == null) {
        subset =
            getComponent(terminologyId, terminology, version, branch,
                ConceptSubsetJpa.class);
      }
      return subset;
    }

  }

  @Override
  public SubsetList getAtomSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atom subsets " + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetJpa a where "
            + "version = :version and terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      List<Subset> m = query.getResultList();
      SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());

      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public SubsetList getConceptSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get concept subsets " + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetJpa a where "
            + "version = :version and terminology = :terminology");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      @SuppressWarnings("unchecked")
      List<Subset> m = query.getResultList();
      SubsetListJpa subsetList = new SubsetListJpa();
      subsetList.setObjects(m);
      subsetList.setTotalCount(m.size());
      return subsetList;

    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAtomSubsetMembers(java.
   * lang.String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList findAtomSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find atom subset members " + subsetId + "/"
            + terminology + "/" + version + ", query=" + query);
    // Prepare the query string

    StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }
    finalQuery.append("terminology:" + terminology + " AND version:" + version
        + " AND subsetTerminologyId:" + subsetId);

    FullTextQuery fullTextQuery =
        applyPfsToLuceneQuery(AtomSubsetMemberJpa.class,
            ConceptSubsetMemberJpa.class, finalQuery.toString(), pfs);

    SubsetMemberList list = new SubsetMemberListJpa();
    list.setTotalCount(fullTextQuery.getResultSize());
    list.setObjects(fullTextQuery.getResultList());

    return list;
  }

  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList findConceptSubsetMembers(String subsetId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find concept subset members " + subsetId + "/"
            + terminology + "/" + version + ", query=" + query);
    // Prepare the query string

    StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }
    finalQuery.append("terminology:" + terminology + " AND version:" + version
        + " AND subsetTerminologyId:" + subsetId);

    FullTextQuery fullTextQuery =
        applyPfsToLuceneQuery(ConceptSubsetMemberJpa.class,
            ConceptSubsetMemberJpa.class, finalQuery.toString(), pfs);

    SubsetMemberList list = new SubsetMemberListJpa();
    list.setTotalCount(fullTextQuery.getResultSize());
    list.setObjects(fullTextQuery.getResultList());

    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMembersForAtom(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembersForAtom(String atomId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members for atom " + atomId + "/"
            + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from AtomSubsetMemberJpa a, "
            + " AtomJpa b where b.terminologyId = :atomId "
            + "and b.version = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("atomId", atomId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (SubsetMember<? extends ComponentHasAttributesAndName> s : list
       * .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getSubsetMembersForConcept(
   * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembersForConcept(String conceptId,
    String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members for concept " + conceptId + "/"
            + terminology + "/" + version);
    javax.persistence.Query query =
        manager.createQuery("select a from ConceptSubsetMemberJpa a, "
            + " ConceptJpa b where b.terminologyId = :conceptId "
            + "and b.version = :version "
            + "and b.terminology = :terminology and a.member = b");

    try {
      SubsetMemberList list = new SubsetMemberListJpa();

      query.setParameter("conceptId", conceptId);
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      list.setObjects(query.getResultList());
      list.setTotalCount(list.getObjects().size());

      // account for lazy initialization
      /*
       * for (SubsetMember<? extends ComponentHasAttributesAndName> s : list
       * .getObjects()) { if (s.getAttributes() != null)
       * s.getAttributes().size(); }
       */
      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public SubsetList getAllSubsets(String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all subsets " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      SubsetList list = getAtomSubsets(terminology, version, branch);
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

  @Override
  public Definition addDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add definition " + definition);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(definition.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + definition.getTerminology());
      }
      String id = idHandler.getTerminologyId(definition, component);
      definition.setTerminologyId(id);
    }

    // Add component
    Definition newDefinition = addComponent(definition);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.definitionChanged(newDefinition, WorkflowListener.Action.ADD);
      }
    }
    return newDefinition;
  }

  @Override
  public void updateDefinition(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update definition " + definition);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(definition.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Definition definition2 =
            getComponent(definition.getId(), DefinitionJpa.class);
        if (!idHandler.getTerminologyId(definition, component).equals(
            idHandler.getTerminologyId(definition2, component))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set definition id on update
        definition.setTerminologyId(idHandler.getTerminologyId(definition,
            component));
      }
    }
    // update component
    this.updateComponent(definition);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.definitionChanged(definition, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeDefinition(java.lang.
   * Long)
   */
  @Override
  public void removeDefinition(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove definition " + id);
    // Remove the component
    Definition definition = removeComponent(id, DefinitionJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.definitionChanged(definition, WorkflowListener.Action.REMOVE);
      }
    }
  }

  @Override
  public SemanticTypeComponent addSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent, Concept concept)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add semanticTypeComponent " + semanticTypeComponent);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler =
          getIdentifierAssignmentHandler(semanticTypeComponent.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + semanticTypeComponent.getTerminology());
      }
      String id = idHandler.getTerminologyId(semanticTypeComponent, concept);
      semanticTypeComponent.setTerminologyId(id);
    }

    // Add component
    SemanticTypeComponent newSemanticTypeComponent =
        addComponent(semanticTypeComponent);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(newSemanticTypeComponent,
            WorkflowListener.Action.ADD);
      }
    }
    return newSemanticTypeComponent;
  }

  @Override
  public void updateSemanticTypeComponent(
    SemanticTypeComponent semanticTypeComponent, Concept concept)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update semanticTypeComponent "
            + semanticTypeComponent);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(semanticTypeComponent.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        SemanticTypeComponent semanticTypeComponent2 =
            getComponent(semanticTypeComponent.getId(),
                SemanticTypeComponent.class);
        if (!idHandler.getTerminologyId(semanticTypeComponent, concept).equals(
            idHandler.getTerminologyId(semanticTypeComponent2, concept))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set semanticTypeComponent id on update
        semanticTypeComponent.setTerminologyId(idHandler.getTerminologyId(
            semanticTypeComponent, concept));
      }
    }
    // update component
    this.updateComponent(semanticTypeComponent);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(semanticTypeComponent,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeSemanticTypeComponent
   * (java.lang.Long)
   */
  @Override
  public void removeSemanticTypeComponent(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove semanticTypeComponent " + id);
    // Remove the component
    SemanticTypeComponent semanticTypeComponent =
        removeComponent(id, SemanticTypeComponentJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.semanticTypeChanged(semanticTypeComponent,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getDescriptor(java.lang.Long)
   */
  @Override
  public Descriptor getDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - get descriptor " + id);
    return getComponent(id, DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getDescriptors(java.lang.String
   * , java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public DescriptorList getDescriptors(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get descriptors " + terminologyId + "/"
            + terminology + "/" + version);
    List<Descriptor> descriptors =
        getComponents(terminologyId, terminology, version, DescriptorJpa.class);
    if (descriptors == null) {
      return null;
    }
    DescriptorList list = new DescriptorListJpa();
    list.setTotalCount(descriptors.size());
    list.setObjects(descriptors);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getDescriptor(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Descriptor getDescriptor(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get descriptor " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addDescriptor(com.wci.umls.
   * server.model.content.Descriptor)
   */
  @Override
  public Descriptor addDescriptor(Descriptor descriptor) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add descriptor " + descriptor);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(descriptor.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + descriptor.getTerminology());
      }
      String id = idHandler.getTerminologyId(descriptor);
      descriptor.setTerminologyId(id);
    }

    // Add component
    Descriptor newDescriptor = addComponent(descriptor);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.descriptorChanged(newDescriptor, WorkflowListener.Action.ADD);
      }
    }
    return newDescriptor;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateDescriptor(com.wci.umls
   * .server.model.content.Descriptor)
   */
  @Override
  public void updateDescriptor(Descriptor descriptor) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update descriptor " + descriptor);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(descriptor.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Descriptor descriptor2 = getDescriptor(descriptor.getId());
        if (!idHandler.getTerminologyId(descriptor).equals(
            idHandler.getTerminologyId(descriptor2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set descriptor id on update
        descriptor.setTerminologyId(idHandler.getTerminologyId(descriptor));
      }
    }
    // update component
    this.updateComponent(descriptor);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.descriptorChanged(descriptor, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeDescriptor(java.lang.
   * Long)
   */
  @Override
  public void removeDescriptor(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove descriptor " + id);
    // Remove the component
    Descriptor descriptor = removeComponent(id, DescriptorJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.descriptorChanged(descriptor, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getCode(java.lang.Long)
   */
  @Override
  public Code getCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get code " + id);
    Code c = manager.find(CodeJpa.class, id);
    return c;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getCodes(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public CodeList getCodes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get codes " + terminologyId + "/" + terminology
            + "/" + version);
    List<Code> codes =
        getComponents(terminologyId, terminology, version, CodeJpa.class);
    if (codes == null) {
      return null;
    }
    CodeList list = new CodeListJpa();
    list.setTotalCount(codes.size());
    list.setObjects(codes);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getCode(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Code getCode(String terminologyId, String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get code " + terminologyId + "/" + terminology + "/"
            + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addCode(com.wci.umls.server
   * .model.content.Code)
   */
  @Override
  public Code addCode(Code code) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add code " + code);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(code.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + code.getTerminology());
      }
      String id = idHandler.getTerminologyId(code);
      code.setTerminologyId(id);
    }

    // Add component
    Code newCode = addComponent(code);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.codeChanged(newCode, WorkflowListener.Action.ADD);
      }
    }
    return newCode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateCode(com.wci.umls.server
   * .model.content.Code)
   */
  @Override
  public void updateCode(Code code) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update code " + code);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(code.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Code code2 = getCode(code.getId());
        if (!idHandler.getTerminologyId(code).equals(
            idHandler.getTerminologyId(code2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set code id on update
        code.setTerminologyId(idHandler.getTerminologyId(code));
      }
    }
    // update component
    this.updateComponent(code);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.codeChanged(code, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#removeCode(java.lang.Long)
   */
  @Override
  public void removeCode(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove code " + id);
    // Remove the component
    Code code = removeComponent(id, CodeJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.codeChanged(code, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getLexicalClass(java.lang.Long)
   */
  @Override
  public LexicalClass getLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical class " + id);
    return getComponent(id, LexicalClassJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getLexicalClasses(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public LexicalClassList getLexicalClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical classes " + terminologyId + "/"
            + terminology + "/" + version);
    List<LexicalClass> luis =
        getComponents(terminologyId, terminology, version,
            LexicalClassJpa.class);
    if (luis == null) {
      return null;
    }
    LexicalClassList list = new LexicalClassListJpa();
    list.setTotalCount(luis.size());
    list.setObjects(luis);
    return list;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getLexicalClass(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public LexicalClass getLexicalClass(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get lexical class " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        LexicalClassJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addLexicalClass(com.wci.umls
   * .server.model.content.LexicalClass)
   */
  @Override
  public LexicalClass addLexicalClass(LexicalClass lexicalClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add lexical class " + lexicalClass);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(lexicalClass.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + lexicalClass.getTerminology());
      }
      String id = idHandler.getTerminologyId(lexicalClass);
      lexicalClass.setTerminologyId(id);
    }

    // Add component
    LexicalClass newLexicalClass = addComponent(lexicalClass);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(newLexicalClass,
            WorkflowListener.Action.ADD);
      }
    }
    return newLexicalClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateLexicalClass(com.wci.
   * umls.server.model.content.LexicalClass)
   */
  @Override
  public void updateLexicalClass(LexicalClass lexicalClass) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update lexical class " + lexicalClass);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(lexicalClass.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        LexicalClass lexicalClass2 = getLexicalClass(lexicalClass.getId());
        if (!idHandler.getTerminologyId(lexicalClass).equals(
            idHandler.getTerminologyId(lexicalClass2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set lexicalClass id on update
        lexicalClass.setTerminologyId(idHandler.getTerminologyId(lexicalClass));
      }
    }
    // update component
    this.updateComponent(lexicalClass);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(lexicalClass,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeLexicalClass(java.lang
   * .Long)
   */
  @Override
  public void removeLexicalClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove lexical class " + id);
    // Remove the component
    LexicalClass lexicalClass = removeComponent(id, LexicalClassJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.lexicalClassChanged(lexicalClass,
            WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getStringClass(java.lang.Long)
   */
  @Override
  public StringClass getStringClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string class " + id);
    return getComponent(id, StringClassJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getStringClasses(java.lang.
   * String, java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public StringClassList getStringClasses(String terminologyId,
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string classes " + terminologyId + "/"
            + terminology + "/" + version);
    List<StringClass> suis =
        getComponents(terminologyId, terminology, version, StringClassJpa.class);
    if (suis == null) {
      return null;
    }
    StringClassList list = new StringClassListJpa();
    list.setTotalCount(suis.size());
    list.setObjects(suis);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getStringClass(java.lang.String
   * , java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public StringClass getStringClass(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get string class " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        StringClass.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addStringClass(com.wci.umls
   * .server.model.content.StringClass)
   */
  @Override
  public StringClass addStringClass(StringClass stringClass) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add string class " + stringClass);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(stringClass.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + stringClass.getTerminology());
      }
      String id = idHandler.getTerminologyId(stringClass);
      stringClass.setTerminologyId(id);
    }

    // Add component
    StringClass newStringClass = addComponent(stringClass);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(newStringClass, WorkflowListener.Action.ADD);
      }
    }
    return newStringClass;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateStringClass(com.wci.umls
   * .server.model.content.StringClass)
   */
  @Override
  public void updateStringClass(StringClass stringClass) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update string class " + stringClass);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(stringClass.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        StringClass stringClass2 = getStringClass(stringClass.getId());
        if (!idHandler.getTerminologyId(stringClass).equals(
            idHandler.getTerminologyId(stringClass2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set stringClass id on update
        stringClass.setTerminologyId(idHandler.getTerminologyId(stringClass));
      }
    }
    // update component
    this.updateComponent(stringClass);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(stringClass, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeStringClass(java.lang
   * .Long)
   */
  @Override
  public void removeStringClass(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove string class " + id);
    // Remove the component
    StringClass stringClass = removeComponent(id, StringClassJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener
            .stringClassChanged(stringClass, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescendantConcepts(java
   * .lang.String, java.lang.String, java.lang.String, boolean,
   * java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList findDescendantConcepts(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant concepts " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Concept> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, ConceptJpa.class, totalCt);
    ConceptList list = new ConceptListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAncestorConcepts(java.lang
   * .String, java.lang.String, java.lang.String, boolean, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public ConceptList findAncestorConcepts(String terminologyId,
    String terminology, String version, boolean parentsOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor concepts " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Concept> ancestors =
        this.findAncestorsHelper(terminologyId, terminology, version,
            parentsOnly, branch, pfs, ConceptJpa.class, totalCt);
    ConceptList list = new ConceptListJpa();
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

    if (pfs != null && pfs.getQueryRestriction() != null) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    String queryStr =
        "select a from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " super, " + clazz.getName() + " a "
            + " where super.version = :version "
            + " and super.terminology = :terminology "
            + " and super.terminologyId = :terminologyId"
            + " and tr.superType = super" + " and tr.subType = a "
            + " and tr.superType != tr.subType"
            + (childrenOnly ? " and depth = 1" : "");
    javax.persistence.Query query = applyPfsToHqlQuery(queryStr, pfs);

    javax.persistence.Query ctQuery =
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

    if (pfs != null && pfs.getQueryRestriction() != null) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }
    String queryStr =
        "select a from "
            + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
            + " tr, " + clazz.getName() + " sub, " + clazz.getName() + " a "
            + " where sub.version = :version "
            + " and sub.terminology = :terminology "
            + " and sub.terminologyId = :terminologyId"
            + " and tr.subType = sub" + " and tr.superType = a "
            + " and tr.subType != tr.superType"
            + (parentsOnly ? " and depth = 1" : "");
    javax.persistence.Query query = applyPfsToHqlQuery(queryStr, pfs);

    javax.persistence.Query ctQuery =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescendantDescriptors(java
   * .lang.String, java.lang.String, java.lang.String, boolean,
   * java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public DescriptorList findDescendantDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant descriptors " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Descriptor> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, DescriptorJpa.class, totalCt);
    DescriptorList list = new DescriptorListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAncestorDescriptors(java
   * .lang.String, java.lang.String, java.lang.String, boolean,
   * java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public DescriptorList findAncestorDescriptors(String terminologyId,
    String terminology, String version, boolean childrenOnly, String branch,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor descriptors " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Descriptor> ancestors =
        this.findAncestorsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, DescriptorJpa.class, totalCt);
    DescriptorList list = new DescriptorListJpa();
    list.setObjects(ancestors);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescendantCodes(java.lang
   * .String, java.lang.String, java.lang.String, boolean, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public CodeList findDescendantCodes(String terminologyId, String terminology,
    String version, boolean childrenOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find descendant codes " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Code> descendants =
        this.findDescendantsHelper(terminologyId, terminology, version,
            childrenOnly, branch, pfs, CodeJpa.class, totalCt);
    CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findAncestorCodes(java.lang
   * .String, java.lang.String, java.lang.String, boolean, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public CodeList findAncestorCodes(String terminologyId, String terminology,
    String version, boolean parentsOnly, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find ancestor codes " + terminologyId + ", "
            + terminology);
    long[] totalCt = new long[1];
    @SuppressWarnings("unchecked")
    List<Code> descendants =
        this.findAncestorsHelper(terminologyId, terminology, version,
            parentsOnly, branch, pfs, CodeJpa.class, totalCt);
    CodeList list = new CodeListJpa();
    list.setObjects(descendants);
    list.setTotalCount((int) totalCt[0]);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getAtom(java.lang.Long)
   */
  @Override
  public Atom getAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get atom " + id);
    return getComponent(id, AtomJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getAtoms(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AtomList getAtoms(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atoms " + terminologyId + "/" + terminology
            + "/" + version);
    List<Atom> atoms =
        getComponents(terminologyId, terminology, version, AtomJpa.class);
    if (atoms == null) {
      return null;
    }
    AtomList list = new AtomListJpa();
    list.setTotalCount(atoms.size());
    list.setObjects(atoms);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#getAtom(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Atom getAtom(String terminologyId, String terminology, String version,
    String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get atom " + terminologyId + "/" + terminology + "/"
            + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AtomJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addAtom(com.wci.umls.server
   * .model.content.Atom)
   */
  @Override
  public Atom addAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - add atom " + atom);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(atom.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + atom.getTerminology());
      }
      atom.setTerminologyId(idHandler.getTerminologyId(atom));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + atom.getTerminology());
    }

    // Add component
    Atom newAtom = addComponent(atom);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.atomChanged(newAtom, WorkflowListener.Action.ADD);
      }
    }
    return newAtom;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateAtom(com.wci.umls.server
   * .model.content.Atom)
   */
  @Override
  public void updateAtom(Atom atom) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - update atom " + atom);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(atom.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      Atom atom2 = getAtom(atom.getId());
      if (!idHandler.getTerminologyId(atom).equals(
          idHandler.getTerminologyId(atom2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(atom);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.atomChanged(atom, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.ContentService#removeAtom(java.lang.Long)
   */
  @Override
  public void removeAtom(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove atom " + id);
    // Remove the component
    Atom atom = removeComponent(id, AtomJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.atomChanged(atom, WorkflowListener.Action.REMOVE);
      }
    }
  }

  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    Long id,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationship " + id);
    if (relationshipClass != null) {
      return getComponent(id, relationshipClass);
    } else {
      Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
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

  @SuppressWarnings("unchecked")
  @Override
  public RelationshipList getRelationships(
    String terminologyId,
    String terminology,
    String version,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships " + terminologyId + "/"
            + terminology + "/" + version);
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationships =
        null;
    if (relationshipClass != null) {
      relationships =
          getComponents(terminologyId, terminology, version, relationshipClass);
    } else {
      relationships =
          getComponents(terminologyId, terminology, version,
              ConceptRelationshipJpa.class);
      if (relationships == null) {
        relationships =
            getComponents(terminologyId, terminology, version,
                AtomRelationshipJpa.class);
      }
      if (relationships == null) {
        relationships =
            getComponents(terminologyId, terminology, version,
                CodeRelationshipJpa.class);
      }
      if (relationships == null) {
        relationships =
            getComponents(terminologyId, terminology, version,
                DescriptorRelationshipJpa.class);
      }
    }

    if (relationships == null) {
      return null;
    }
    RelationshipList list = new RelationshipListJpa();
    list.setTotalCount(relationships.size());
    list.setObjects(relationships);
    return list;
  }

  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> getRelationship(
    String terminologyId,
    String terminology,
    String version,
    String branch,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationship " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    if (relationshipClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          relationshipClass);
    }
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addRelationship(com.wci.umls
   * .server.model.content.Relationship)
   */
  @Override
  public Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> addRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add relationship " + rel);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(rel.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + rel.getTerminology());
      }
      String id = idHandler.getTerminologyId(rel);
      rel.setTerminologyId(id);
    }

    // Add component
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.relationshipChanged(newRel, WorkflowListener.Action.ADD);
      }
    }
    return newRel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateRelationship(com.wci.
   * umls.server.model.content.Relationship)
   */
  @Override
  public void updateRelationship(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update relationship " + rel);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(rel.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel2 =
            getComponent(rel.getId(), rel.getClass());
        if (!idHandler.getTerminologyId(rel).equals(
            idHandler.getTerminologyId(rel2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        rel.setTerminologyId(idHandler.getTerminologyId(rel));
      }
    }
    // update component
    this.updateComponent(rel);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.relationshipChanged(rel, WorkflowListener.Action.UPDATE);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void removeRelationship(
    Long id,
    Class<? extends Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove relationship " + id);
    // Remove the component
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel =
        null;
    if (relationshipClass != null) {
      rel = removeComponent(id, relationshipClass);
    } else {
      rel = getRelationship(id, relationshipClass);
      rel = removeComponent(id, rel.getClass());
    }
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.relationshipChanged(rel, WorkflowListener.Action.REMOVE);
      }
    }
  }

  @Override
  public TransitiveRelationship<? extends AtomClass> getTransitiveRelationship(
    Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get transitive relationship " + id);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addTransitiveRelationship(com
   * .wci.umls.server.model.content.TransitiveRelationship)
   */

  @Override
  public TransitiveRelationship<? extends ComponentHasAttributes> addTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add transitive relationship " + rel);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(rel.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + rel.getTerminology());
      }
      String id = idHandler.getTerminologyId(rel);
      rel.setTerminologyId(id);
    }

    // Add component
    TransitiveRelationship<? extends ComponentHasAttributes> newRel =
        addComponent(rel);

    return newRel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateTransitiveRelationship
   * (com.wci.umls.server.model.content.TransitiveRelationship)
   */
  @Override
  public void updateTransitiveRelationship(
    TransitiveRelationship<? extends ComponentHasAttributes> rel)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update transitive relationship " + rel);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(rel.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        TransitiveRelationship<? extends ComponentHasAttributes> rel2 =
            getComponent(rel.getId(), rel.getClass());
        if (!idHandler.getTerminologyId(rel).equals(
            idHandler.getTerminologyId(rel2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        rel.setTerminologyId(idHandler.getTerminologyId(rel));
      }
    }
    // update component
    this.updateComponent(rel);

  }

  @Override
  public void removeTransitiveRelationship(
    Long id,
    Class<? extends TransitiveRelationship<? extends AtomClass>> relationshipClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove transitive relationship " + id);

    TransitiveRelationship<? extends ComponentHasAttributes> rel = null;
    rel = getComponent(id, relationshipClass);
    removeComponent(id, rel.getClass());
  }

  @Override
  public TreePosition<? extends AtomClass> getTreePosition(Long id,
    Class<? extends TreePosition<? extends AtomClass>> treeposClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get tree position " + id);
    if (treeposClass != null) {
      return getComponent(id, treeposClass);
    } else {
      TreePosition<? extends AtomClass> treepos =
          getComponent(id, ConceptTreePositionJpa.class);
      if (treepos == null) {
        treepos = getComponent(id, CodeTreePositionJpa.class);
      }
      if (treepos == null) {
        treepos = getComponent(id, DescriptorTreePositionJpa.class);
      }
      return treepos;
    }
  }

  @Override
  public TreePosition<? extends ComponentHasAttributesAndName> addTreePosition(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add tree position " + treepos);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(treepos.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + treepos.getTerminology());
      }
      String id = idHandler.getTerminologyId(treepos);
      treepos.setTerminologyId(id);
    }

    // Add component
    TreePosition<? extends ComponentHasAttributesAndName> newTreepos =
        addComponent(treepos);

    return newTreepos;
  }

  @Override
  public void updateTreePosition(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update tree position " + treepos);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(treepos.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        @SuppressWarnings("unchecked")
        TreePosition<? extends ComponentHasAttributesAndName> treepos2 =
            getComponent(treepos.getId(), treepos.getClass());
        if (!idHandler.getTerminologyId(treepos).equals(
            idHandler.getTerminologyId(treepos2))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        treepos.setTerminologyId(idHandler.getTerminologyId(treepos));
      }
    }
    // update component
    this.updateComponent(treepos);

  }

  @Override
  public void removeTreePosition(Long id,
    Class<? extends TreePosition<? extends AtomClass>> treeposClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove tree position " + id);
    TreePosition<? extends ComponentHasAttributesAndName> treepos =
        getComponent(id, treeposClass);
    removeComponent(id, treepos.getClass());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addSubset(com.wci.umls.server
   * .model.content.Subset)
   */
  @Override
  public Subset addSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass())
        .debug("Content Service - add subset " + subset);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(subset.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + subset.getTerminology());
      }
      subset.setTerminologyId(idHandler.getTerminologyId(subset));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + subset.getTerminology());
    }

    // Add component
    Subset newSubset = addComponent(subset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetChanged(newSubset, WorkflowListener.Action.ADD);
      }
    }
    return newSubset;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateSubset(com.wci.umls.server
   * .model.content.Subset)
   */
  @Override
  public void updateSubset(Subset subset) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update subset " + subset);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subset.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      Subset subset2 = getSubset(subset.getId(), subset.getClass());
      if (!idHandler.getTerminologyId(subset).equals(
          idHandler.getTerminologyId(subset2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(subset);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetChanged(subset, WorkflowListener.Action.UPDATE);
      }
    }
  }

  @Override
  public void removeSubset(Long id, Class<? extends Subset> subsetClass)
    throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - remove subset " + id);
    Subset subset = null;
    // Remove the component
    if (subsetClass != null) {
      subset = removeComponent(id, subsetClass);
    } else {
      subset = getSubset(id, subsetClass);
      removeComponent(id, subset.getClass());
    }

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetChanged(subset, WorkflowListener.Action.REMOVE);
      }
    }
  }

  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset member " + id);
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

  @SuppressWarnings("unchecked")
  @Override
  public SubsetMemberList getSubsetMembers(
    String terminologyId,
    String terminology,
    String version,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset members " + terminologyId + "/"
            + terminology + "/" + version);
    List<SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> members =
        null;
    if (memberClass != null) {
      members =

      getComponents(terminologyId, terminology, version, memberClass);
    } else {
      members =
          getComponents(terminologyId, terminology, version,
              AtomSubsetMemberJpa.class);
      if (members == null) {
        members =
            getComponents(terminologyId, terminology, version,
                ConceptSubsetMemberJpa.class);

      }
    }
    if (members == null) {
      return null;
    }
    SubsetMemberList list = new SubsetMemberListJpa();
    list.setTotalCount(members.size());
    list.setObjects(members);
    return list;
  }

  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> getSubsetMember(
    String terminologyId,
    String terminology,
    String version,
    String branch,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get subset member " + terminologyId + "/"
            + terminology + "/" + version + "/" + branch);
    if (memberClass != null) {
      return getComponent(terminologyId, terminology, version, branch,
          memberClass);
    } else {
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
          getComponent(terminologyId, terminology, version, branch,
              AtomSubsetMemberJpa.class);
      if (member == null) {
        member =
            getComponent(terminologyId, terminology, version, branch,
                ConceptSubsetMemberJpa.class);
      }
      return member;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#addSubsetMember(com.wci.umls
   * .server.model.content.SubsetMember)
   */
  @Override
  public SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> addSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add subset member " + subsetMember);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(subsetMember.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + subsetMember.getTerminology());
      }
      subsetMember.setTerminologyId(idHandler.getTerminologyId(subsetMember));
    }
    if (assignIdentifiersFlag && idHandler == null) {
      throw new Exception("Unable to find id handler for "
          + subsetMember.getTerminology());
    }

    // Add component
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> newSubsetMember =
        addComponent(subsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(newSubsetMember,
            WorkflowListener.Action.ADD);
      }
    }
    return newSubsetMember;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#updateSubsetMember(com.wci.
   * umls.server.model.content.SubsetMember)
   */
  @Override
  public void updateSubsetMember(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update subsetMember " + subsetMember);
    // Id assignment
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(subsetMember.getTerminology());
    if (!idHandler.allowIdChangeOnUpdate() && assignIdentifiersFlag) {
      @SuppressWarnings("unchecked")
      SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> subsetMember2 =
          getComponent(subsetMember.getId(), subsetMember.getClass());
      if (!idHandler.getTerminologyId(subsetMember).equals(
          idHandler.getTerminologyId(subsetMember2))) {
        throw new Exception("Update cannot be used to change object identity.");
      }
    }

    // update component
    this.updateComponent(subsetMember);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(subsetMember,
            WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeSubsetMember(java.lang
   * .Long)
   */
  @Override
  public void removeSubsetMember(
    Long id,
    Class<? extends SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset>> memberClass)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove subsetMember " + id);
    // find and remove the component
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member =
        getComponent(id, memberClass);
    removeComponent(id, member.getClass());

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.subsetMemberChanged(member, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAttribute(java.lang.Long)
   */
  @Override
  public Attribute getAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug("Content Service - get attribute " + id);
    return getComponent(id, AttributeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAttributes(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeList getAttributes(String terminologyId, String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get attributes " + terminologyId + "/" + terminology
            + "/" + version);
    List<Attribute> attributes =
        getComponents(terminologyId, terminology, version, AttributeJpa.class);
    if (attributes == null) {
      return null;
    }
    AttributeList list = new AttributeListJpa();
    list.setTotalCount(attributes.size());
    list.setObjects(attributes);
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAttribute(java.lang.String,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Attribute getAttribute(String terminologyId, String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - get attribute " + terminologyId + "/" + terminology
            + "/" + version + "/" + branch);
    return getComponent(terminologyId, terminology, version, branch,
        AttributeJpa.class);
  }

  @Override
  public Attribute addAttribute(Attribute attribute,
    ComponentHasAttributes component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - add attribute " + attribute);
    // Assign id
    IdentifierAssignmentHandler idHandler = null;
    if (assignIdentifiersFlag) {
      idHandler = getIdentifierAssignmentHandler(attribute.getTerminology());
      if (idHandler == null) {
        throw new Exception("Unable to find id handler for "
            + attribute.getTerminology());
      }
      String id = idHandler.getTerminologyId(attribute, component);
      attribute.setTerminologyId(id);
    }

    // Add component
    Attribute newAttribute = addComponent(attribute);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.attributeChanged(newAttribute, WorkflowListener.Action.ADD);
      }
    }
    return newAttribute;
  }

  @Override
  public void updateAttribute(Attribute attribute,
    ComponentHasAttributes component) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - update attribute " + attribute);

    // Id assignment should not change
    final IdentifierAssignmentHandler idHandler =
        getIdentifierAssignmentHandler(attribute.getTerminology());
    if (assignIdentifiersFlag) {
      if (!idHandler.allowIdChangeOnUpdate()) {
        Attribute attribute2 = getAttribute(attribute.getId());
        if (!idHandler.getTerminologyId(attribute, component).equals(
            idHandler.getTerminologyId(attribute2, component))) {
          throw new Exception(
              "Update cannot be used to change object identity.");
        }
      } else {
        // set attribute id on update
        attribute.setTerminologyId(idHandler.getTerminologyId(attribute,
            component));
      }
    }
    // update component
    this.updateComponent(attribute);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.attributeChanged(attribute, WorkflowListener.Action.UPDATE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#removeAttribute(java.lang.Long)
   */
  @Override
  public void removeAttribute(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - remove attribute " + id);
    // Remove the component
    Attribute attribute = removeComponent(id, AttributeJpa.class);

    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.attributeChanged(attribute, WorkflowListener.Action.REMOVE);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findConceptsForQuery(java.lang
   * .String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfscParameter)
   */
  @Override
  public SearchResultList findConceptsForQuery(String terminology,
    String version, String branch, String query, PfscParameter pfsc)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find concepts " + terminology + "/" + version + "/"
            + query);
    return findForQueryHelper(terminology, version, branch, query, pfsc,
        ConceptJpa.class, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#autocompleteConcepts(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteConcepts(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete concepts " + terminology + ", "
            + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm,
        ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescriptorsForQuery(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfscParameter)
   */
  @Override
  public SearchResultList findDescriptorsForQuery(String terminology,
    String version, String branch, String query, PfscParameter pfsc)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptors " + terminology + "/" + version
            + "/" + query);
    return findForQueryHelper(terminology, version, branch, query, pfsc,
        DescriptorJpa.class, DescriptorJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#autocompleteDescriptors(java
   * .lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteDescriptors(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete descriptors " + terminology + ", "
            + version + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm,
        DescriptorJpa.class);
  }

  /**
   * Computes whether the given query string and PFS parameter will lead to an
   * actual lucene query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @SuppressWarnings("static-method")
  private boolean isLuceneQueryInfo(String query, PfsParameter pfs) {
    return pfs.getQueryRestriction() != null || pfs.getActiveOnly()
        || pfs.getInactiveOnly() || (query != null && !query.isEmpty());
  }

  /**
   * Find for query helper.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param pfsc the pfsc
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  public <T extends AtomClass> SearchResultList findForQueryHelper(
    String terminology, String version, String branch, String query,
    PfscParameter pfsc, Class<?> fieldNamesKey, Class<T> clazz)
    throws Exception {
    // Prepare results
    SearchResultList results = new SearchResultListJpa();
    List<T> classes = null;
    int totalCt[] = new int[1];

    // Perform Lucene search (if there is anything to search for)
    List<T> queryClasses = new ArrayList<>();
    boolean queryFlag = false;
    if (isLuceneQueryInfo(query, pfsc)) {
      queryFlag = true;
      queryClasses =
          getLuceneQueryResults(terminology, version, branch, query,
              fieldNamesKey, clazz, pfsc, totalCt);
      Logger.getLogger(getClass()).debug(
          "    lucene result count = " + queryClasses.size());
    }

    boolean criteriaFlag = false;
    List<T> criteriaClasses = new ArrayList<>();
    if (pfsc != null) {
      boolean init = false;
      for (SearchCriteria criteria : pfsc.getSearchCriteria()) {
        criteriaFlag = true;
        if (!init) {
          criteriaClasses =
              getSearchCriteriaResults(terminology, version, criteria, clazz);
          init = true;
        } else {
          // Perform intersection operation (presume "AND" semantic between
          // multiple search criteria)
          criteriaClasses.retainAll(getSearchCriteriaResults(terminology,
              version, criteria, clazz));
        }
        Logger.getLogger(getClass()).debug(
            "    criteria result count = " + queryClasses.size());
      }
    }

    // Determine whether both query and criteria were used, or just one or the
    // other

    // Start with query results if they exist
    if (queryFlag) {
      classes = queryClasses;
      Logger.getLogger(getClass()).debug(
          "    combined count = " + queryClasses.size());
    }

    if (criteriaFlag) {

      if (queryFlag) {
        // Intersect the lucene and HQL results
        classes.retainAll(criteriaClasses);
      } else {
        // Otherwise, just use criteria classes
        classes = criteriaClasses;
      }
      Logger.getLogger(getClass()).debug(
          "    combined count = " + queryClasses.size());

      // Here we know the total size
      totalCt[0] = classes.size();

      // Apply PFS sorting manually
      if (pfsc != null && pfsc.getSortField() != null) {
        Logger.getLogger(getClass()).debug(
            "    sort results - " + pfsc.getSortField());
        final Method getMethod =
            clazz.getMethod("get"
                + pfsc.getSortField().substring(0, 1).toUpperCase()
                + pfsc.getSortField().substring(1));
        if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        Collections.sort(classes, new Comparator<T>() {
          @SuppressWarnings({
              "rawtypes", "unchecked"
          })
          @Override
          public int compare(T o1, T o2) {
            try {
              Comparable f1 =
                  (Comparable) getMethod.invoke(o1, new Object[] {});
              Comparable f2 =
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
      if (pfsc != null && pfsc.getStartIndex() != -1) {
        int startIndex = pfsc.getStartIndex();
        int toIndex = classes.size();
        Logger.getLogger(getClass()).debug(
            "    page results - " + startIndex + ", " + toIndex);
        toIndex = Math.min(toIndex, startIndex + pfsc.getMaxResults());
        classes = classes.subList(startIndex, toIndex);
      }

    } else {
      // If criteria flag wasn't triggered, then PFS was already handled
      // by the query mechanism - which only applies PFS if criteria isn't
      // also used. Therefore, we are ready to go.

      // Manual PFS handling is in the section above.
    }

    // Some result has been found, even if empty
    if (classes == null) {
      results.setTotalCount(0);
      return results;
    }

    // construct the search results
    for (AtomClass atomClass : classes) {
      SearchResult sr = new SearchResultJpa();
      sr.setId(atomClass.getId());
      sr.setTerminologyId(atomClass.getTerminologyId());
      sr.setTerminology(atomClass.getTerminology());
      sr.setVersion(atomClass.getVersion());
      sr.setValue(atomClass.getName());
      results.addObject(sr);
    }

    results.setTotalCount(totalCt[0]);
    return results;

  }

  /**
   * Find for general query helper.
   *
   * @param <T> the
   * @param luceneQuery the lucene query
   * @param hqlQuery the hql query
   * @param branch the branch
   * @param pfs the pfs
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @return the search result list
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private <T extends AtomClass> SearchResultList findForGeneralQueryHelper(
    String luceneQuery, String hqlQuery, String branch, PfsParameter pfs,
    Class<?> fieldNamesKey, Class<T> clazz) throws Exception {
    // Prepare results
    SearchResultList results = new SearchResultListJpa();
    List<T> classes = null;
    int totalCt[] = new int[1];

    // Perform Lucene search
    List<T> luceneQueryClasses = new ArrayList<>();
    boolean luceneQueryFlag = false;
    if (luceneQuery != null && !luceneQuery.equals("")) {
      luceneQueryClasses =
          getLuceneQueryResults("", "", branch, luceneQuery, fieldNamesKey,
              clazz, pfs, totalCt);
      luceneQueryFlag = true;
    }

    boolean hqlQueryFlag = false;
    List<T> hqlQueryClasses = new ArrayList<>();
    if (hqlQuery != null && !hqlQuery.equals("")) {
      if (!hqlQuery.toLowerCase().startsWith("select"))
        throw new Exception(
            "The hql query did not start with the keyword 'select'. "
                + hqlQuery);
      if (hqlQuery.contains(";"))
        throw new Exception("The hql query must not contain the ';'. "
            + hqlQuery);
      javax.persistence.Query hQuery = manager.createQuery(hqlQuery);
      hQuery.setHint("javax.persistence.query.timeout", 50);
      try {
        List<T> hqlResults = hQuery.getResultList();
        for (T r : hqlResults) {
          hqlQueryClasses.add(r);
        }
      } catch (ClassCastException e) {
        throw new Exception(
            "The hql query returned items of an unexpected type. ", e);
      }

      hqlQueryFlag = true;
    }

    // Determine whether both query and criteria were used, or just one or the
    // other

    // Start with query results if they exist
    if (luceneQueryFlag) {
      classes = luceneQueryClasses;
    }

    if (hqlQueryFlag) {

      if (luceneQueryFlag) {
        // Intersect the lucene and HQL results
        classes.retainAll(hqlQueryClasses);
      } else {
        // Otherwise, just use hql classes
        classes = hqlQueryClasses;
      }

      // Here we know the total size
      totalCt[0] = classes.size();

      // Apply PFS sorting manually
      if (pfs != null && pfs.getSortField() != null) {
        final Method getMethod =
            clazz.getMethod("get"
                + pfs.getSortField().substring(0, 1).toUpperCase()
                + pfs.getSortField().substring(1));
        if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        Collections.sort(classes, new Comparator<AtomClass>() {
          @SuppressWarnings("rawtypes")
          @Override
          public int compare(AtomClass o1, AtomClass o2) {
            try {
              Comparable f1 =
                  (Comparable) getMethod.invoke(o1, new Object[] {});
              Comparable f2 =
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
    for (AtomClass atomClass : classes) {
      SearchResult sr = new SearchResultJpa();
      sr.setId(atomClass.getId());
      sr.setTerminologyId(atomClass.getTerminologyId());
      sr.setTerminology(atomClass.getTerminology());
      sr.setVersion(atomClass.getVersion());
      sr.setValue(atomClass.getName());
      results.addObject(sr);
    }

    results.setTotalCount(totalCt[0]);
    return results;

    /*
     * if (results == null) throw new
     * Exception("Failed to retrieve results for query");
     * 
     * SearchResultList srl = new SearchResultListJpa(); for (Object result :
     * results) { SearchResult sr = new SearchResultJpa();
     * sr.setTerminology(((AtomClass)result).getTerminology());
     * sr.setVersion(((AtomClass)result).getVersion());
     * sr.setTerminologyId(((AtomClass)result).getTerminologyId());
     * sr.setId(((AtomClass)result).getId()); srl.addObject(sr); } return srl;
     */
  }

  /**
   * Returns the query results.
   *
   * @param <T> the
   * @param terminology the terminology
   * @param version the version
   * @param branch the branch
   * @param query the query
   * @param fieldNamesKey the field names key
   * @param clazz the clazz
   * @param pfs the pfs
   * @param totalCt the total ct
   * @return the query results
   * @throws Exception the exception
   */
  private <T extends Component> List<T> getLuceneQueryResults(
    String terminology, String version, String branch, String query,
    Class<?> fieldNamesKey, Class<T> clazz, PfsParameter pfs, int[] totalCt)
    throws Exception {

    // Prepare the query string
    StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);

    if (terminology != null && !terminology.equals("") && version != null
        && !version.equals("")) {
      finalQuery.append(finalQuery.toString().isEmpty() ? "" : " AND ");
      finalQuery.append("terminology:" + terminology + " AND version:"
          + version);
    }

    Logger.getLogger(getClass()).info(
        "query for " + clazz.getName() + ": " + finalQuery);

    FullTextQuery fullTextQuery =
        applyPfsToLuceneQuery(clazz, fieldNamesKey, finalQuery.toString(), pfs);

    // Apply paging and sorting parameters - if no search criteria
    if (pfs instanceof PfscParameter
        && ((PfscParameter) pfs).getSearchCriteria().isEmpty()) {
      // Get result size if we know it.
      totalCt[0] = fullTextQuery.getResultSize();
    } else {
      // If with search criteria, save paging
      fullTextQuery.setFirstResult(0);
      fullTextQuery.setMaxResults(Integer.MAX_VALUE);
      totalCt[0] = fullTextQuery.getResultSize();
    }

    // execute the query
    @SuppressWarnings("unchecked")
    List<T> classes = fullTextQuery.getResultList();
    return classes;
  }

  /**
   * Returns the search criteria results.
   *
   * @param terminology the terminology
   * @param version the version
   * @param criteria the criteria
   * @param clazz the clazz
   * @return the search criteria results
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private <T extends AtomClass> List<T> getSearchCriteriaResults(
    String terminology, String version, SearchCriteria criteria, Class<T> clazz)
    throws Exception {
    StringBuilder builder = new StringBuilder();
    builder.append("SELECT a FROM " + clazz.getName() + " a "
        + "WHERE terminology = :terminology " + "AND version = :version ");

    String terminologyId = null;

    // findDefinedOnly (applies to Concept only)
    if (criteria.getDefinedOnly()) {
      if (ConceptJpa.class.isAssignableFrom(clazz)) {
        builder.append("AND a.fullyDefined = 1 ");
      }
    }

    // findPrimitiveOnly (applies to Concept only)
    if (criteria.getPrimitiveOnly()) {
      if (ConceptJpa.class.isAssignableFrom(clazz)) {
        builder.append("AND a.fullyDefined = 0 ");
      }
    }

    // Find "to" end of a relationship
    // with a "from" id and optionally a "type"
    // and optionally find descendants of those things
    String relType = null;
    if (criteria.getRelationshipFromId() != null) {
      StringBuilder relBuilder = new StringBuilder();
      terminologyId = criteria.getRelationshipFromId();

      if (criteria.getRelationshipDescendantsFlag()) {
        relBuilder.append("SELECT DISTINCT b.to FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa")
            + " b, "
            + clazz.getName().replace("Jpa",
                "TransitiveRelationshipJpa" + " c, ") + clazz.getName() + " d "
            + "WHERE b.from = c.subType " + "AND c.superType = d "
            + "AND b.obsolete = 0 " + "AND d.terminology = :terminology "
            + "AND d.version = :version "
            + "AND d.terminologyId = :terminologyId");
      } else {
        relBuilder.append("SELECT b.to FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa") + " b, "
            + clazz.getName() + " c " + "WHERE b.from = c "
            + "AND b.obsolete = 0 " + "AND c.terminology = :terminology "
            + "AND c.version = :version "
            + "AND c.terminologyId = :terminologyId");
      }

      if (criteria.getRelationshipType() != null) {
        relType = criteria.getRelationshipType();
        relBuilder.append(" AND additionalRelationshipType = :type");
      }

      builder.append("AND a IN (").append(relBuilder.toString()).append(")");
    }

    // Find "from" end of a relationship
    // with a "to" id and optionally a "type"
    // and optionally find descendants of those things
    if (criteria.getRelationshipToId() != null) {
      StringBuilder relBuilder = new StringBuilder();
      terminologyId = criteria.getRelationshipToId();

      if (criteria.getRelationshipDescendantsFlag()) {
        relBuilder.append("SELECT DISTINCT b.from FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa")
            + " b, "
            + clazz.getName().replace("Jpa",
                "TransitiveRelationshipJpa" + " c, ") + clazz.getName() + " d "
            + "WHERE b.to = c.subType " + "AND c.superType = d "
            + "AND b.obsolete = 0 " + "AND d.terminology = :terminology "
            + "AND d.version = :version "
            + "AND d.terminologyId = :terminologyId");
      } else {
        relBuilder.append("SELECT b.from FROM "
            + clazz.getName().replace("Jpa", "RelationshipJpa") + " b, "
            + clazz.getName() + " c " + "WHERE b.to = c "
            + "AND b.obsolete = 0 " + "AND c.terminology = :terminology "
            + "AND c.version = :version "
            + "AND c.terminologyId = :terminologyId");
      }

      if (criteria.getRelationshipType() != null) {
        relType = criteria.getRelationshipType();
        relBuilder.append(" AND additionalRelationshipType = :type");
      }

      builder.append("AND a IN (").append(relBuilder.toString()).append(")");
    }

    // wrapper around query to findDescendants of results and self (unless
    // specified)
    if (criteria.getFindDescendants()) {
      StringBuilder descBuilder = new StringBuilder();
      descBuilder
          .append(
              "SELECT t.subType FROM "
                  + clazz.getName().replace("Jpa", "TransitiveRelationshipJpa")
                  + " t " + " WHERE t.superType IN (")
          .append(builder.toString()).append(")");

      if (!criteria.getFindSelf()) {
        // Not self.
        descBuilder.append(" AND t.superType != t.subType ");
      }

      builder = descBuilder;
    }

    // findByRelationshipTypeId on its own
    if (criteria.getRelationshipType() != null && relType == null) {
      throw new Exception(
          "Unexpected use of relationship type criteria without "
              + "specifying a from or to relationship id");
    }

    // Run the final query
    Logger.getLogger(getClass()).debug("  query = " + builder);
    javax.persistence.Query query = manager.createQuery(builder.toString());
    query.setParameter("terminology", terminology);
    query.setParameter("version", version);
    if (terminologyId != null) {
      query.setParameter("terminologyId", terminologyId);
    }
    if (relType != null) {
      query.setParameter("type", relType);
    }
    List<T> classes = query.getResultList();

    return classes;

  }

  /**
   * Autocomplete helper.
   *
   * @param terminology the terminology
   * @param version the version
   * @param searchTerm the search term
   * @param clazz the clazz
   * @return the string list
   * @throws Exception the exception
   */
  private StringList autocompleteHelper(String terminology, String version,
    String searchTerm, Class<?> clazz) throws Exception {

    if (terminology == null || version == null || searchTerm == null) {
      return new StringList();
    }
    final String TITLE_EDGE_NGRAM_INDEX = "atoms.edgeNGramName";
    final String TITLE_NGRAM_INDEX = "atoms.nGramName";

    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    QueryBuilder titleQB =
        fullTextEntityManager.getSearchFactory().buildQueryBuilder()
            .forEntity(clazz).get();

    Query query =
        titleQB.phrase().withSlop(2).onField(TITLE_NGRAM_INDEX)
            .andField(TITLE_EDGE_NGRAM_INDEX).boostedTo(5)
            .sentence(searchTerm.toLowerCase()).createQuery();

    Query term1 = new TermQuery(new Term("terminology", terminology));
    Query term2 = new TermQuery(new Term("version", version));
    BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(term1, BooleanClause.Occur.MUST);
    booleanQuery.add(term2, BooleanClause.Occur.MUST);
    booleanQuery.add(query, BooleanClause.Occur.MUST);

    FullTextQuery fullTextQuery =
        fullTextEntityManager.createFullTextQuery(booleanQuery, clazz);

    fullTextQuery.setMaxResults(20);

    @SuppressWarnings("unchecked")
    List<AtomClass> results = fullTextQuery.getResultList();
    StringList list = new StringList();
    list.setTotalCount(fullTextQuery.getResultSize());
    for (AtomClass result : results) {
      // exclude duplicates
      if (!list.contains(result.getName()))
        list.addObject(result.getName());
    }
    return list;
  }

  /**
   * Returns the name analyzed pairs from annotation.
   *
   * @param clazz the clazz
   * @param sortField the sort field
   * @return the name analyzed pairs from annotation
   * @throws NoSuchMethodException the no such method exception
   * @throws SecurityException the security exception
   */
  @SuppressWarnings("static-method")
  private Map<String, Boolean> getNameAnalyzedPairsFromAnnotation(
    Class<?> clazz, String sortField) throws NoSuchMethodException,
    SecurityException {

    // initialize the name->analyzed pair map
    Map<String, Boolean> nameAnalyzedPairs = new HashMap<>();

    Method m =
        clazz.getMethod("get" + sortField.substring(0, 1).toUpperCase()
            + sortField.substring(1), new Class<?>[] {});

    Set<org.hibernate.search.annotations.Field> annotationFields =
        new HashSet<>();

    // check for Field annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
      annotationFields.add(m
          .getAnnotation(org.hibernate.search.annotations.Field.class));
    }

    // check for Fields annotation
    if (m.isAnnotationPresent(org.hibernate.search.annotations.Fields.class)) {
      // add all specified fields
      for (org.hibernate.search.annotations.Field f : m.getAnnotation(
          org.hibernate.search.annotations.Fields.class).value()) {
        annotationFields.add(f);
      }
    }

    // cycle over discovered fields and put name and analyze == YES into map
    for (org.hibernate.search.annotations.Field f : annotationFields) {
      nameAnalyzedPairs.put(f.name(), f.analyze().equals(Analyze.YES) ? true
          : false);
    }

    return nameAnalyzedPairs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findCodesForQuery(java.lang
   * .String, java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findCodesForQuery(String terminology, String version,
    String branch, String query, PfscParameter pfsc) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find codes " + terminology + "/" + version + "/"
            + query);
    return findForQueryHelper(terminology, version, branch, query, pfsc,
        CodeJpa.class, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#autocompleteCodes(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public StringList autocompleteCodes(String terminology, String version,
    String searchTerm) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - autocomplete codes " + terminology + ", " + version
            + ", " + searchTerm);
    return autocompleteHelper(terminology, version, searchTerm, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllConcepts(java.lang.String
   * , java.lang.String, java.lang.String)
   */
  @Override
  public ConceptList getAllConcepts(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all concepts " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from ConceptJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      List<Concept> concepts = query.getResultList();
      ConceptList conceptList = new ConceptListJpa();
      conceptList.setObjects(concepts);
      conceptList.setTotalCount(concepts.size());
      return conceptList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllDescriptors(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public DescriptorList getAllDescriptors(String terminology, String version,
    String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all descriptors " + terminology + "/" + version
            + "/" + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from DescriptorJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");

      @SuppressWarnings("unchecked")
      List<Descriptor> descriptors = query.getResultList();
      DescriptorList descriptorList = new DescriptorListJpa();
      descriptorList.setObjects(descriptors);
      descriptorList.setTotalCount(descriptors.size());
      return descriptorList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getAllCodes(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public CodeList getAllCodes(String terminology, String version, String branch) {
    Logger.getLogger(getClass()).debug(
        "Content Service - get all codes " + terminology + "/" + version + "/"
            + branch);
    assert branch != null;

    try {
      javax.persistence.Query query =
          manager.createQuery("select a from CodeJpa a "
              + "where version = :version " + "and terminology = :terminology "
              + "and (branch = :branch or branchedTo not like :branchMatch)");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      query.setParameter("branch", branch);
      query.setParameter("branchMatch", "%" + branch + Branch.SEPARATOR + "%");
      @SuppressWarnings("unchecked")
      List<Code> codes = query.getResultList();
      CodeList codeList = new CodeListJpa();
      codeList.setObjects(codes);
      codeList.setTotalCount(codes.size());
      return codeList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearTransitiveClosure(java
   * .lang.String, java.lang.String)
   */
  @Override
  public void clearTransitiveClosure(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - Clear transitive closure data for " + terminology
            + ", " + version);
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
      Logger.getLogger(getClass()).info(
          "    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager
              .createQuery("DELETE From DescriptorTransitiveRelationshipJpa "
                  + " c where terminology = :terminology "
                  + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From CodeTransitiveRelationshipJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    CodeTransitiveRelationshipJpa records deleted = "
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearTreePositions(java.lang
   * .String, java.lang.String)
   */
  @Override
  public void clearTreePositions(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - Clear tree positions data for " + terminology + ", "
            + version);
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
      Logger.getLogger(getClass()).info(
          "    ConceptTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From DescriptorTreePositionJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    DescriptorTransitiveRelationshipJpa records deleted = "
              + deleteRecords);

      query =
          manager.createQuery("DELETE From CodeTreePositionJpa "
              + " c where terminology = :terminology "
              + " and version = :version");
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      deleteRecords = query.executeUpdate();
      Logger.getLogger(getClass()).info(
          "    CodeTransitiveRelationshipJpa records deleted = "
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearContent(java.lang.String,
   * java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public void clearContent(String terminology, String version) throws Exception {

    Logger.getLogger(getClass()).info("Content service - clear content");

    setTransactionPerOperation(false);
    beginTransaction();

      // remove concept subset members
      javax.persistence.Query query = manager.createQuery("SELECT a.id FROM ConceptSubsetMemberJpa a WHERE terminology = :terminology "
                + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> csmIds = query.getResultList();
      for (int ct=0; ct<csmIds.size(); ct++) {
        removeSubsetMember(csmIds.get(ct), ConceptSubsetMemberJpa.class);
        logAndCommit(ct, "remove subset members");
      }
      commitClearBegin();

      // remove concept subsets
      query = manager.createQuery("SELECT a.id FROM ConceptSubsetJpa a WHERE terminology = :terminology "
                + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> csIds = query.getResultList();
      for (int ct=0; ct<csIds.size(); ct++) {
        removeSubset(csIds.get(ct), ConceptSubsetJpa.class);
        logAndCommit(ct, "remove concept subsets");
      }
      commitClearBegin();
      
      // remove atom subset members
      query = manager.createQuery("SELECT a.id FROM AtomSubsetMemberJpa a WHERE terminology = :terminology "
                + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> asmIds = query.getResultList();
      for (int ct=0; ct<asmIds.size(); ct++) {
        removeSubsetMember(asmIds.get(ct), AtomSubsetMemberJpa.class);
        logAndCommit(ct, "remove atom subset members");
      }
      commitClearBegin();
      
      // remove atom subsets
      query = manager.createQuery("SELECT a.id FROM AtomSubsetJpa a WHERE terminology = :terminology "
                + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> amIds = query.getResultList();
      for (int ct=0; ct<amIds.size(); ct++) {
        removeSubset(amIds.get(ct), AtomSubsetJpa.class);
        logAndCommit(ct, "remove atom subsets");
      }   
      commitClearBegin();   
      
      // remove concept relationships
      query = manager.createQuery("SELECT a.id FROM ConceptRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> crIds = query.getResultList();
      for (int ct=0; ct<crIds.size(); ct++) {
        removeRelationship(crIds.get(ct), ConceptRelationshipJpa.class);
        logAndCommit(ct, "remove concept relationships");
      }    
      commitClearBegin(); 
      
  
 
      // remove definitions from the concepts; definitions cannot be removed yet, because they are used by atoms
      query = manager.createQuery("SELECT a.id FROM ConceptJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> cIds = query.getResultList();
      for (int ct=0; ct<cIds.size(); ct++) {
        Concept c = getConcept(cIds.get(ct));
        c.setDefinitions(new ArrayList<Definition>());
        updateConcept(c);
        logAndCommit(ct, "remove definitions from concepts");
      }   
      commitClearBegin();
      
      // remove the concept transitive relationships
      query = manager.createQuery("SELECT a.id FROM ConceptTransitiveRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> ctrIds = query.getResultList();
      for (int ct=0; ct<ctrIds.size(); ct++) {
        removeTransitiveRelationship(ctrIds.get(ct), ConceptTransitiveRelationshipJpa.class);
        logAndCommit(ct, "remove concept transitive relationships");
      }   
      commitClearBegin();
      
      // remove the concept tree positions
      query = manager.createQuery("SELECT a.id FROM ConceptTreePositionJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> ctpIds = query.getResultList();
      for (int ct=0; ct<ctpIds.size(); ct++) {
        removeTreePosition(ctpIds.get(ct), ConceptTreePositionJpa.class);
        logAndCommit(ct, "remove concept tree positions");
      }   
      commitClearBegin();
      
      // remove the concepts
      for (int ct=0; ct<cIds.size(); ct++) {
        removeConcept(cIds.get(ct));
        logAndCommit(ct, "remove concepts");
      }   
      commitClearBegin();
      
      // go through all remaining concepts and remove atoms with matching terminology and version
      // concepts may have UMLS terminology and matching atoms wouldn't otherwise be removed
      // not using this code bc of invalid field names
      /*SearchResultList results = findConceptsForQuery(null, null, Branch.ROOT, 
          "atoms.terminology:" + terminology + " atoms.version:" + version, 
          new PfscParameterJpa());
      for (SearchResult result : results.getObjects()) {
        Concept concept = getConcept(result.getId());
      */
      query = manager.createQuery("SELECT a.id FROM ConceptJpa a");
      List<Long> allConceptIds = query.getResultList();
      for (int ct=0; ct<allConceptIds.size(); ct++) {
        Concept concept = getConcept(allConceptIds.get(ct));
        List<Atom> keepAtoms = new ArrayList<Atom>();
        for (Atom atom : concept.getAtoms()) {
          if (!atom.getTerminology().equals(terminology) || !atom.getVersion().equals(version)) {
            keepAtoms.add(atom);   
          }
        }
        concept.setAtoms(keepAtoms);
        updateConcept(concept);
        logAndCommit(ct, "remove atoms from remaining concepts");
      }
      commitClearBegin();


      // remove definitions from the atoms
      query = manager.createQuery("SELECT a.id FROM AtomJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> aIds = query.getResultList();
      for (int ct=0; ct<aIds.size(); ct++) {
        Atom a = getAtom(aIds.get(ct));
        a.setDefinitions(new ArrayList<Definition>());
        updateAtom(a);
        logAndCommit(ct, "remove definitions from atoms");
      }   
      commitClearBegin();
      

      
      // remove atom relationships
      query = manager.createQuery("SELECT a.id FROM AtomRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> arIds = query.getResultList();
      for (int ct=0; ct<arIds.size(); ct++) {
        removeRelationship(arIds.get(ct), AtomRelationshipJpa.class);
        logAndCommit(ct, "remove atom relationships");
      }    
      commitClearBegin();       
    
      // remove descriptor relationships
      query = manager.createQuery("SELECT a.id FROM DescriptorRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> drIds = query.getResultList();
      for (int ct=0; ct<drIds.size(); ct++) {
        removeRelationship(drIds.get(ct), DescriptorRelationshipJpa.class);
        logAndCommit(ct, "remove descriptor relationships");
      }    
      commitClearBegin();   
      
      // remove the descriptor transitive relationships
      query = manager.createQuery("SELECT a.id FROM DescriptorTransitiveRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> dtrIds = query.getResultList();
      for (int ct=0; ct<dtrIds.size(); ct++) {
        removeTransitiveRelationship(dtrIds.get(ct), DescriptorTransitiveRelationshipJpa.class);
        logAndCommit(ct, "remove descriptor transitive relationships");
      }   
      commitClearBegin();
      
      // remove the descriptor tree positions
      query = manager.createQuery("SELECT a.id FROM DescriptorTreePositionJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> dtpIds = query.getResultList();
      for (int ct=0; ct<dtpIds.size(); ct++) {
        removeTreePosition(dtpIds.get(ct), DescriptorTreePositionJpa.class);
        logAndCommit(ct, "remove descriptor tree positions");
      }   
      commitClearBegin();
      
      // remove descriptors
      query = manager.createQuery("SELECT a.id FROM DescriptorJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> dscIds = query.getResultList();
      for (int ct=0; ct<dscIds.size(); ct++) {
        removeDescriptor(dscIds.get(ct));
        logAndCommit(ct, "remove descriptors");
      }    
      commitClearBegin();       
      
      // remove code relationships
      query = manager.createQuery("SELECT a.id FROM CodeRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> cdrIds = query.getResultList();
      for (int ct=0; ct<cdrIds.size(); ct++) {
        removeRelationship(cdrIds.get(ct), CodeRelationshipJpa.class);
        logAndCommit(ct, "remove code relationships");
      }    
      commitClearBegin();    
      
      // remove the code transitive relationships
      query = manager.createQuery("SELECT a.id FROM CodeTransitiveRelationshipJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> cdtrIds = query.getResultList();
      for (int ct=0; ct<cdtrIds.size(); ct++) {
        removeTransitiveRelationship(cdtrIds.get(ct), CodeTransitiveRelationshipJpa.class);
        logAndCommit(ct, "remove code transitive relationships");
      }   
      commitClearBegin();
      
      // remove the code tree positions
      query = manager.createQuery("SELECT a.id FROM CodeTreePositionJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> cdtpIds = query.getResultList();
      for (int ct=0; ct<cdtpIds.size(); ct++) {
        removeTreePosition(cdtpIds.get(ct), CodeTreePositionJpa.class);
        logAndCommit(ct, "remove tree positions");
      }   
      commitClearBegin();
      
      // remove codes
      query = manager.createQuery("SELECT a.id FROM CodeJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> c = query.getResultList();
      for (int ct=0; ct<c.size(); ct++) {
        removeCode(c.get(ct));
        logAndCommit(ct, "remove codes");
      }    
      commitClearBegin();       
      
      
      
      // remove atoms - don't do this until after removing codes
      for (int ct=0; ct<aIds.size(); ct++) {
        removeAtom(aIds.get(ct));
        logAndCommit(ct, "remove atoms");
      }   
      commitClearBegin(); 
      
      // remove semantic type components, definitions and attributes last
      query = manager.createQuery("SELECT a.id FROM SemanticTypeComponentJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> stIds = query.getResultList();
      for (int ct=0; ct<stIds.size(); ct++) {
        removeSemanticTypeComponent(stIds.get(ct));
        logAndCommit(ct, "remove semantic type components");
      }   
      commitClearBegin();   
      
      // remove the definitions
      query = manager.createQuery("SELECT a.id FROM DefinitionJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> dIds = query.getResultList();
      for (int ct=0; ct<dIds.size(); ct++) {
        removeDefinition(dIds.get(ct));
        logAndCommit(ct, "remove definitions");
      }   
      commitClearBegin();
      
      // remove the attributes
      query = manager.createQuery("SELECT a.id FROM AttributeJpa a WHERE terminology = :terminology "
          + " AND version = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
      List<Long> attIds = query.getResultList();
      for (int ct=0; ct<attIds.size(); ct++) {
        removeAttribute(attIds.get(ct));
        logAndCommit(ct, "remove attributes");
      }   
      commitClearBegin();
      
      commit();
      clear();

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#clearBranch(java.lang.String)
   */
  @Override
  public void clearBranch(String branch) {
    // TODO: part of implementing branching
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getIdentifierAssignmentHandler
   * (java.lang.String)
   */
  @Override
  public IdentifierAssignmentHandler getIdentifierAssignmentHandler(
    String terminology) throws Exception {
    if (idHandlerMap.containsKey(terminology)) {
      return idHandlerMap.get(terminology);
    }
    return idHandlerMap.get(ConfigUtility.DEFAULT);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getComputePreferredNameHandler
   * (java.lang.String)
   */
  @Override
  public ComputePreferredNameHandler getComputePreferredNameHandler(
    String terminology) throws Exception {
    if (pnHandlerMap.containsKey(terminology)) {
      return pnHandlerMap.get(terminology);
    }
    return pnHandlerMap.get(ConfigUtility.DEFAULT);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getComputedPreferredName(com
   * .wci.umls.server.model.content.AtomClass)
   */
  @Override
  public String getComputedPreferredName(AtomClass atomClass) throws Exception {
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
      final String pn = handler.computePreferredName(atomClass.getAtoms());
      return pn;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getNormalizedString(java.lang
   * .String)
   */
  @Override
  public String getNormalizedString(String string) throws Exception {
    return normalizedStringHandler.getNormalizedString(string);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.services.ContentService#isLastModifiedFlag()
   */
  /**
   * Indicates whether or not last modified flag is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.MetadataServiceJpa#setLastModifiedFlag
   * (boolean)
   */
  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#setAssignIdentifiersFlag(boolean
   * )
   */
  @Override
  public void setAssignIdentifiersFlag(boolean assignIdentifiersFlag) {
    this.assignIdentifiersFlag = assignIdentifiersFlag;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#getComponentStats(java.lang
   * .String, java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, Integer> getComponentStats(String terminology,
    String version, String branch) throws Exception {
    Logger.getLogger(getClass()).info("Content Service - getComponentStats");
    assert branch != null;
    Map<String, Integer> stats = new TreeMap<>();
    for (EntityType<?> type : manager.getMetamodel().getEntities()) {
      String jpaTable = type.getName();
      // Logger.getLogger(getClass()).debug("  jpaTable = " + jpaTable);
      // Skip audit trail tables
      if (jpaTable.toUpperCase().indexOf("_AUD") != -1) {
        continue;
      }
      if (!AbstractAbbreviation.class.isAssignableFrom(type
          .getBindableJavaType())
          && !AbstractComponent.class.isAssignableFrom(type
              .getBindableJavaType())) {
        continue;
      }
      Logger.getLogger(getClass()).info("  " + jpaTable);
      javax.persistence.Query query = null;
      if (terminology != null) {
        query =
            manager.createQuery("select count(*) from " + jpaTable
                + " where terminology = :terminology "
                + "and version = :version ");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);

      } else {
        query = manager.createQuery("select count(*) from " + jpaTable);
      }
      int ct = ((Long) query.getSingleResult()).intValue();
      stats.put("Total " + jpaTable, ct);

      // Only compute active counts for components
      if (AbstractComponent.class.isAssignableFrom(type.getBindableJavaType())) {
        if (terminology != null) {

          query =
              manager.createQuery("select count(*) from " + jpaTable
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
  private <T extends Component> List getComponents(String terminologyId,
    String terminology, String version, Class<T> clazz) {
    try {
      javax.persistence.Query query =
          manager
              .createQuery("select a from "
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
  private <T extends Component> T addComponent(T component) throws Exception {
    try {
      // Set last modified date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(component);
        tx.commit();
      } else {
        manager.persist(component);
      }
      return component;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Update component.
   *
   * @param <T> the
   * @param component the component
   * @throws Exception the exception
   */
  private <T extends Component> void updateComponent(T component)
    throws Exception {
    try {
      // Set modification date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(component);
        tx.commit();
      } else {
        manager.merge(component);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

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
  private <T extends Component> T getComponent(Long id, Class<T> clazz)
    throws Exception {
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
  private <T extends Component> T getComponent(String terminologyId,
    String terminology, String version, String branch, Class<T> clazz) {

    List<T> results = getComponents(terminologyId, terminology, version, clazz);
    if (results.isEmpty()) {
      Logger.getLogger(getClass()).debug("  no " + clazz.getName());
      return null;
    }
    T defaultBranch = null;
    for (T obj : results) {
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
   * @return the t
   * @throws Exception the exception
   */
  private <T extends Component> T removeComponent(Long id, Class<T> clazz)
    throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      T component = manager.find(clazz, id);

      // Set modification date
      if (lastModifiedFlag) {
        component.setLastModified(new Date());
      }

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(component)) {
          manager.remove(component);
        } else {
          manager.remove(manager.merge(component));
        }
        tx.commit();
      } else {
        if (manager.contains(component)) {
          manager.remove(component);
        } else {
          manager.remove(manager.merge(component));
        }
      }
      return component;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForConcept
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for concept " + conceptId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query + "/"
            + inverseFlag);

    return findRelationshipsForComponentHelper(conceptId, terminology, version,
        branch, query, inverseFlag, pfs, ConceptRelationshipJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDeepRelationshipsForConcept
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @SuppressWarnings({
      "rawtypes", "unchecked"
  })
  @Override
  public RelationshipList findDeepRelationshipsForConcept(String conceptId,
    String terminology, String version, String branch, boolean inverseFlag,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find deep relationships for concept " + conceptId
            + "/" + terminology + "/" + version);

    if (pfs != null && pfs.getQueryRestriction() != null) {
      throw new IllegalArgumentException(
          "Query restriction is not implemented for this call: "
              + pfs.getQueryRestriction());
    }

    try {

      Concept concept = getConcept(conceptId, terminology, version, branch);
      List<Object[]> results = new ArrayList<>();
      String queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, a.to.terminologyId, "
              + "a.obsolete, a.suppressible, a.published, a.publishable "
              + "from ConceptRelationshipJpa a " + "where "
              + (inverseFlag ? "a.to" : "a.from") + ".id = :conceptId ";
      javax.persistence.Query query = manager.createQuery(queryStr);
      query.setParameter("conceptId", concept.getId());
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable "
              + "from AtomRelationshipJpa a join a.to.conceptTerminologyIds cui2 "
              + "where key(cui2) = '" + concept.getTerminology() + "' and "
              + (inverseFlag ? "a.to" : "a.from") + ".id in (:atomIds) ";
      query = manager.createQuery(queryStr);
      final Set<Long> atomIds = new HashSet<>();
      for (final Atom atom : concept.getAtoms()) {
        atomIds.add(atom.getId());
      }
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable "
              + "from DescriptorRelationshipJpa a, DescriptorJpa b, AtomJpa c, "
              + "DescriptorJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.descriptorId "
              + "and b.terminology = c.terminology "
              + "and b.version = c.version "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.descriptorId "
              + "and d.terminology = e.terminology "
              + "and d.version = e.version " + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable "
              + "from ConceptRelationshipJpa a, ConceptJpa b, AtomJpa c, "
              + "ConceptJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.conceptId "
              + "and b.terminology = c.terminology "
              + "and b.version = c.version "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.conceptId "
              + "and d.terminology = e.terminology "
              + "and d.version = e.version " + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      queryStr =
          "select a.id, a.terminologyId, a.terminology, a.version, "
              + "a.relationshipType, a.additionalRelationshipType, value(cui2), "
              + "a.obsolete, a.suppressible, a.published, a.publishable "
              + "from CodeRelationshipJpa a, CodeJpa b, AtomJpa c, "
              + "CodeJpa d, AtomJpa e join e.conceptTerminologyIds cui2 "
              + "where a." + (inverseFlag ? "to" : "from") + ".id = b.id "
              + "and b.terminologyId = c.codeId "
              + "and b.terminology = c.terminology "
              + "and b.version = c.version "
              + "and b.name = c.name and c.id in (:atomIds) " + "and a."
              + (inverseFlag ? "from" : "to") + ".id = d.id "
              + "and d.terminologyId = e.codeId "
              + "and d.terminology = e.terminology "
              + "and d.version = e.version " + "and d.name = e.name ";
      query = manager.createQuery(queryStr);
      query.setParameter("atomIds", atomIds);
      results.addAll(query.getResultList());

      // Use a set to "uniq" them
      Set<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> conceptRels =
          new HashSet<>();
      for (final Object[] result : results) {
        final ConceptRelationship relationship = new ConceptRelationshipJpa();
        final Concept toConcept = new ConceptJpa();
        toConcept.setTerminology(concept.getTerminology());
        toConcept.setVersion(concept.getVersion());
        toConcept.setTerminologyId(result[6].toString());
        relationship.setId(Long.parseLong(result[0].toString()));
        relationship.setFrom(concept);
        relationship.setTerminologyId(result[1].toString());
        relationship.setTerminology(result[2].toString());
        relationship.setVersion(result[3].toString());
        relationship.setRelationshipType(result[4].toString());
        relationship.setAdditionalRelationshipType(result[5].toString());
        relationship.setObsolete(result[7].toString().equals("1"));
        relationship.setSuppressible(result[8].toString().equals("1"));
        relationship.setPublished(result[9].toString().equals("1"));
        relationship.setPublishable(result[10].toString().equals("1"));
        relationship.setTo(toConcept);
        conceptRels.add(relationship);
      }
      List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> conceptRelList =
          new ArrayList<>(conceptRels);

      // Apply PFS sorting manually
      if (pfs != null && pfs.getSortField() != null) {
        final Method getMethod =
            ConceptRelationshipJpa.class.getMethod("get"
                + pfs.getSortField().substring(0, 1).toUpperCase()
                + pfs.getSortField().substring(1));
        if (getMethod.getReturnType().isAssignableFrom(Comparable.class)) {
          throw new Exception("Referenced sort field is not comparable");
        }
        Collections.sort(conceptRelList, new Comparator<Relationship>() {
          @Override
          public int compare(Relationship o1, Relationship o2) {
            try {
              Comparable f1 =
                  (Comparable) getMethod.invoke(o1, new Object[] {});
              Comparable f2 =
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
        int toIndex = conceptRelList.size();
        toIndex = Math.min(toIndex, startIndex + pfs.getMaxResults());
        conceptRelList = conceptRelList.subList(startIndex, toIndex);
      }

      RelationshipList list = new RelationshipListJpa();
      list.setTotalCount(conceptRels.size());
      list.setObjects(conceptRelList);

      return list;
    } catch (NoResultException e) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForDescriptor
   * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForDescriptor(String descriptorId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for descriptor " + descriptorId
            + "/" + terminology + "/" + version + "/" + branch + "/" + query
            + "/" + inverseFlag);

    return findRelationshipsForComponentHelper(descriptorId, terminology,
        version, branch, query, inverseFlag, pfs,
        DescriptorRelationshipJpa.class);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findRelationshipsForCode(java
   * .lang.String, java.lang.String, java.lang.String, java.lang.String,
   * boolean, com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public RelationshipList findRelationshipsForCode(String codeId,
    String terminology, String version, String branch, String query,
    boolean inverseFlag, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find relationships for code " + codeId + "/"
            + terminology + "/" + version + "/" + branch + "/" + query + "/"
            + inverseFlag);

    return findRelationshipsForComponentHelper(codeId, terminology, version,
        branch, query, inverseFlag, pfs, CodeRelationshipJpa.class);

  }

  /**
   * Find relationships helper.
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
  @SuppressWarnings("unchecked")
  private RelationshipList findRelationshipsForComponentHelper(
    String terminologyId, String terminology, String version, String branch,
    String query, boolean inverseFlag, PfsParameter pfs, Class<?> clazz)
    throws Exception {

    RelationshipList results = new RelationshipListJpa();

    // Prepare the query string
    StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }

    // add id/terminology/version constraints baesd on inverse flag
    if (inverseFlag == true) {
      finalQuery.append("toTerminologyId:" + terminologyId
          + " AND toTerminology:" + terminology + " AND toVersion:" + version);
    } else {
      finalQuery.append("fromTerminologyId:" + terminologyId
          + " AND fromTerminology:" + terminology + " AND fromVersion:"
          + version);
    }

    FullTextQuery fullTextQuery =
        applyPfsToLuceneQuery(clazz, ConceptRelationshipJpa.class,
            finalQuery.toString(), pfs);

    // Get result size
    results.setTotalCount(fullTextQuery.getResultSize());

    // execute the query
    List<Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes>> relationships =
        fullTextQuery.getResultList();
    results.setObjects(relationships);

    for (Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> rel : results
        .getObjects()) {
      getGraphResolutionHandler(terminology).resolve(rel);
    }
    return results;

  }

  @Override
  public TreePositionList findTreePositionsForConcept(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find tree positions for concept " + terminologyId
            + "/" + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        "", pfs, ConceptTreePositionJpa.class);
  }

  @Override
  public TreePositionList findTreePositionsForDescriptor(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find tree positionss for descriptor "
            + terminologyId + "/" + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        "", pfs, DescriptorTreePositionJpa.class);
  }

  @Override
  public TreePositionList findTreePositionsForCode(String terminologyId,
    String terminology, String version, String branch, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Content Service - find tree positions for code " + terminologyId + "/"
            + terminology + "/" + version);
    return findTreePositionsHelper(terminologyId, terminology, version, branch,
        "", pfs, CodeTreePositionJpa.class);

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
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private TreePositionList findTreePositionsHelper(String terminologyId,
    String terminology, String version, String branch, String query,
    PfsParameter pfs, Class<?> clazz) throws Exception {

    // Prepare the query string

    StringBuilder finalQuery = new StringBuilder();
    finalQuery.append(query == null ? "" : query);
    if (!finalQuery.toString().isEmpty()) {
      finalQuery.append(" AND ");
    }
    finalQuery.append("terminology:" + terminology + " AND version:" + version);
    if (terminologyId != null) {
      finalQuery.append(" AND nodeTerminologyId:" + terminologyId);
    }
    FullTextQuery fullTextQuery =
        applyPfsToLuceneQuery(clazz, ConceptTreePositionJpa.class,
            finalQuery.toString(), pfs);

    // Apply paging and sorting parameters - if no search criteria
    TreePositionList list = new TreePositionListJpa();
    list.setTotalCount(fullTextQuery.getResultSize());
    list.setObjects(fullTextQuery.getResultList());

    return list;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findCodesForGeneralQuery(java
   * .lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findCodesForGeneralQuery(String luceneQuery,
    String hqlQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find codes " + luceneQuery + "/" + hqlQuery + "/");
    return findForGeneralQueryHelper(luceneQuery, hqlQuery, branch, pfs,
        CodeJpa.class, CodeJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findConceptsForGeneralQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findConceptsForGeneralQuery(String luceneQuery,
    String hqlQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info(
            "Content Service - find concepts " + luceneQuery + "/" + hqlQuery
                + "/");
    return findForGeneralQueryHelper(luceneQuery, hqlQuery, branch, pfs,
        ConceptJpa.class, ConceptJpa.class);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.ContentService#findDescriptorsForGeneralQuery
   * (java.lang.String, java.lang.String, java.lang.String,
   * com.wci.umls.server.helpers.PfsParameter)
   */
  @Override
  public SearchResultList findDescriptorsForGeneralQuery(String luceneQuery,
    String hqlQuery, String branch, PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptors " + luceneQuery + "/" + hqlQuery
            + "/");
    return findForGeneralQueryHelper(luceneQuery, hqlQuery, branch, pfs,
        DescriptorJpa.class, DescriptorJpa.class);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Tree getTreeForTreePosition(TreePosition<? extends AtomClass> treePosition)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - get tree for tree position");
    
    Long tpId = treePosition.getNode().getId();

    // Determine type
    Class<?> clazz = null;
    if (manager.find(ConceptJpa.class, tpId) != null) {
      clazz = ConceptTreePositionJpa.class;
    } else if (manager.find(DescriptorJpa.class, tpId) != null) {
      clazz = DescriptorTreePositionJpa.class;
    } else if (manager.find(CodeJpa.class, tpId) != null) {
      clazz = CodeTreePositionJpa.class;
    } else {
      throw new Exception("Unknown tree position type.");
    }
    Logger.getLogger(getClass()).debug("  type = " + clazz.getName());

    // tree to return
    Tree tree = new TreeJpa();
    
    // the current tree variables (ancestor path and local tree)
    String partAncPath = "";    // initially top-level
    Tree partTree = tree;       // initially the empty tree

    // Prepare lucene
    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();
    QueryParser queryParser =
        new MultiFieldQueryParser(stringFieldNames.get(
            ConceptTreePositionJpa.class).toArray(new String[] {}),
            searchFactory.getAnalyzer(clazz));
    String fullAncPath =
        treePosition.getAncestorPath() + (treePosition.getAncestorPath().isEmpty() ? "" : "~") + tpId;
    
    // Iterate over ancestor path
    for (String pathPart : fullAncPath.split("~")) {
      Long partId = Long.parseLong(pathPart);
      Logger.getLogger(getClass()).debug("  nodeId = " + partId);
      Logger.getLogger(getClass()).debug("  ancestorPath = " + partAncPath);

      StringBuilder finalQuery = new StringBuilder();
      finalQuery.append("nodeId:" + partId + " AND ");
      if (partAncPath.isEmpty()) {
        // query for empty value
        finalQuery.append("-ancestorPath:[* TO *]");
      } else {
        finalQuery.append("ancestorPath:\"" + partAncPath + "\"");
      }
      // Prepare the manager and lucene query
      Query luceneQuery = queryParser.parse(finalQuery.toString());
      FullTextQuery fullTextQuery =
          fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);
      
      // projection approach -- don't want to have to instantiate node Jpa object
      fullTextQuery.setProjection("nodeId", "nodeTerminologyId", "nodeName", "childCt", "ancestorPath");
      
      List<Object[]> results = fullTextQuery.getResultList();
      
      if (fullTextQuery.getResultSize() != 1) {
        throw new Exception("Unexpected number of results: "
            + fullTextQuery.getResultSize());
      }
      Object[] result = results.get(0);

      // fill in the tree object
      partTree.setId((Long) result[0]); 
      partTree.setTerminologyId((String) result[1]);
      partTree.setName((String) result[2]);
      partTree.setChildCt((Integer) result[3]);
      partTree.setAncestorPath((String) result[4]);
      partTree.setTerminology(treePosition.getTerminology());
      partTree.setVersion(treePosition.getVersion());


/*
      // original approach
      if (fullTextQuery.getResultSize() != 1) {
        throw new Exception("Unexpected number of results: "
            + fullTextQuery.getResultSize());
      }

      TreePosition<? extends AtomClass> treepos =
          (TreePosition<? extends AtomClass>) fullTextQuery.getResultList()
              .get(0);

      partTree.setFromTreePosition(treepos);*/
      
      Tree nextPart = new TreeJpa();
      
      // if not end of sequence, add the new blank object as a child)
      if (!partId.equals(tpId)) {
        partTree.addChild(nextPart);
      }  
      
      // if the terminal node, check for sibling and children requests
      else {
        
      }
      
      // set current tree to the just constructed (blank) tree
      partTree = nextPart;

      partAncPath += (partAncPath.equals("") ? "" : "~");
      partAncPath += pathPart;
    }
    
    
    
    return tree;
  }

  /**
   * Apply pfs to lucene query2.
   *
   * @param clazz the clazz
   * @param fieldNamesKey the field names key
   * @param query the query
   * @param pfs the pfs
   * @return the full text query
   * @throws Exception the exception
   */
  protected FullTextQuery applyPfsToLuceneQuery(Class<?> clazz,
    Class<?> fieldNamesKey, String query, PfsParameter pfs) throws Exception {

    FullTextQuery fullTextQuery = null;

    // Build up the query
    StringBuilder pfsQuery = new StringBuilder();
    pfsQuery.append(query);
    if (pfs != null) {
      if (pfs.getActiveOnly()) {
        pfsQuery.append(" AND obsolete:false");
      }
      if (pfs.getInactiveOnly()) {
        pfsQuery.append(" AND obsolete:true");
      }
      if (pfs.getQueryRestriction() != null
          && !pfs.getQueryRestriction().isEmpty()) {
        pfsQuery.append(" AND " + pfs.getQueryRestriction());
      }
    }

    // Set up the "full text query"
    FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    SearchFactory searchFactory = fullTextEntityManager.getSearchFactory();

    Query luceneQuery;
    try {
      QueryParser queryParser =
          new MultiFieldQueryParser(stringFieldNames.get(fieldNamesKey)
              .toArray(new String[] {}), searchFactory.getAnalyzer(clazz));
      Logger.getLogger(getClass()).info("query = " + pfsQuery);
      luceneQuery = queryParser.parse(pfsQuery.toString());

      // Validate query terms
      luceneQuery =
          luceneQuery.rewrite(fullTextEntityManager.getSearchFactory()
              .getIndexReaderAccessor().open(clazz));
      Set<Term> terms = new HashSet<>();
      luceneQuery.extractTerms(terms);
      for (Term t : terms) {
        if (t.field() != null && !t.field().isEmpty()
            && !allFieldNames.get(fieldNamesKey).contains(t.field())) {
          throw new Exception("Query references invalid field name "
              + t.field() + ", " + allFieldNames.get(fieldNamesKey));
        }
      }

    } catch (ParseException e) {
      throw new LocalException(
          "The specified search terms cannot be parsed.  Please check syntax and try again.");
    }
    fullTextQuery =
        fullTextEntityManager.createFullTextQuery(luceneQuery, clazz);

    if (pfs != null) {
      // if start index and max results are set, set paging
      if (pfs.getStartIndex() >= 0 && pfs.getMaxResults() >= 0) {
        fullTextQuery.setFirstResult(pfs.getStartIndex());
        fullTextQuery.setMaxResults(pfs.getMaxResults());
      }

      // if sort field is specified, set sort key
      if (pfs.getSortField() != null && !pfs.getSortField().isEmpty()) {
        Map<String, Boolean> nameToAnalyzedMap =
            this.getNameAnalyzedPairsFromAnnotation(clazz, pfs.getSortField());
        String sortField = null;

        if (nameToAnalyzedMap.size() == 0) {
          throw new Exception(clazz.getName()
              + " does not have declared, annotated method for field "
              + pfs.getSortField());
        }

        // first check the default name (rendered as ""), if not analyzed, use
        // this as sort
        if (nameToAnalyzedMap.get("") != null
            && nameToAnalyzedMap.get("").equals(false)) {
          sortField = pfs.getSortField();
        }

        // otherwise check explicit [name]Sort index
        else if (nameToAnalyzedMap.get(pfs.getSortField() + "Sort") != null
            && nameToAnalyzedMap.get(pfs.getSortField() + "Sort").equals(false)) {
          sortField = pfs.getSortField() + "Sort";
        }

        // if none, throw exception
        if (sortField == null) {
          throw new Exception(
              "Could not retrieve a non-analyzed Field annotation for get method for variable name "
                  + pfs.getSortField());
        }

        Sort sort =
            new Sort(new SortField(sortField, SortField.Type.STRING,
                !pfs.isAscending()));
        fullTextQuery.setSort(sort);
      }
    }
    return fullTextQuery;
  }

  /**
   * Apply pfs to query.
   *
   * @param queryStr the query str
   * @param pfs the pfs
   * @return the javax.persistence. query
   */
  protected javax.persistence.Query applyPfsToHqlQuery(String queryStr,
    PfsParameter pfs) {
    StringBuilder localQueryStr = new StringBuilder();
    localQueryStr.append(queryStr);

    // Query restriction assumes a driving table called "a"
    if (pfs != null) {
      if (pfs.getQueryRestriction() != null) {
        localQueryStr.append(" AND ").append(pfs.getQueryRestriction());
      }

      if (pfs.getActiveOnly()) {
        localQueryStr.append("  AND a.obsolete = 0 ");
      }
      if (pfs.getInactiveOnly()) {
        localQueryStr.append("  AND a.obsolete = 1 ");
      }

      // add an order by clause to end of the query, assume driving table
      // called
      // "a"
      if (pfs.getSortField() != null) {
        localQueryStr.append(" order by a.").append(pfs.getSortField());
      }
    }

    Logger.getLogger(getClass()).info("query = " + localQueryStr);
    javax.persistence.Query query =
        manager.createQuery(localQueryStr.toString());
    if (pfs != null && pfs.getStartIndex() > -1 && pfs.getMaxResults() > -1) {
      query.setFirstResult(pfs.getStartIndex());
      query.setMaxResults(pfs.getMaxResults());
    }
    return query;
  }

  @Override
  public TreePositionList findConceptTreePositionsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find concept tree positions " + terminology + "/"
            + version + "/" + query);
    return this.findTreePositionsHelper(null, terminology, version, branch,
        query, pfs, ConceptTreePositionJpa.class);
  }

  @Override
  public TreePositionList findDescriptorTreePositionsForQuery(
    String terminology, String version, String branch, String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find descriptor tree positions " + terminology + "/"
            + version + "/" + query);
    return this.findTreePositionsHelper(null, terminology, version, branch,
        query, pfs, DescriptorTreePositionJpa.class);
  }

  @Override
  public TreePositionList findCodeTreePositionsForQuery(String terminology,
    String version, String branch, String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "Content Service - find code tree positions " + terminology + "/"
            + version + "/" + query);
    return this.findTreePositionsHelper(null, terminology, version, branch,
        query, pfs, CodeTreePositionJpa.class);
  }
  
  /**
   * Commit clear begin transaction.
   *
   * @throws Exception the exception
   */
  private void commitClearBegin() throws Exception {
    commit();
    clear();
    beginTransaction();
  }

  /**
   * Log and commit.
   *
   * @param objectCt the object ct
   * @throws Exception the exception
   */
  private void logAndCommit(int objectCt, String msg) throws Exception {
    // log at regular intervals
    if (objectCt % logCt == 0) {
      Logger.getLogger(getClass()).info("    count = " + objectCt + " " + msg);
    }
    if (objectCt % commitCt == 0) {
      commitClearBegin();
    }
  }
}
