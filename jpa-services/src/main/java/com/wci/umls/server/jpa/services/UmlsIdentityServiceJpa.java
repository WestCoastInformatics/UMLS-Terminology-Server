/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.hibernate.search.SearchFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.jpa.meta.RelationshipIdentityJpa;
import com.wci.umls.server.jpa.services.helper.IndexUtility;
import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.RelationshipIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringClassIdentity;
import com.wci.umls.server.services.UmlsIdentityService;

/**
 * JPA and JAXB enabled implementation of {@link UmlsIdentityService}.
 */
public class UmlsIdentityServiceJpa extends MetadataServiceJpa
    implements UmlsIdentityService {

  /** The uncommited id map. */
  private static Map<Object, Long> uncommitedIdMap = new HashMap<>();

  /** The max ids. */
  private static Map<String, Long> maxIds = new HashMap<>();

  /**
   * Instantiates an empty {@link UmlsIdentityServiceJpa}.
   *
   * @throws Exception the exception
   */
  public UmlsIdentityServiceJpa() throws Exception {
    super();

  }

  /* see superclass */
  @Override
  public AttributeIdentity getAttributeIdentity(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get attribute identity " + id);
    return getObject(id, AttributeIdentity.class);
  }

  /* see superclass */
  @Override
  public Long getNextAttributeId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next attribute id");

    Long attId = 0L;
    Long styId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("ATUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from AttributeIdentityJpa a ");
        Long attId2 = (Long) query.getSingleResult();
        attId = attId2 != null ? attId2 : attId;
      } catch (NoResultException e) {
        attId = 0L;
      }
      try {
        final javax.persistence.Query query = manager.createQuery(
            "select max(a.id) from SemanticTypeComponentIdentityJpa a ");
        Long styId2 = (Long) query.getSingleResult();
        styId = styId2 != null ? styId2 : styId;
      } catch (NoResultException e) {
        styId = 0L;
      }
      // Set the max attribute/semantic class Id
      maxIds.put("ATUI", Math.max(attId, styId));
      Logger.getLogger(getClass())
          .info("Initializing max ATUI = " + Math.max(attId, styId));

    }
    final Long result = maxIds.get("ATUI") + 1;
    maxIds.put("ATUI", result);

    return result;
  }

  /* see superclass */
  @Override
  public AttributeIdentity getAttributeIdentity(AttributeIdentity identity)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get attribute identity " + identity);

    if (uncommitedIdMap.containsKey(identity)) {
      identity.setId(uncommitedIdMap.get(identity));
      return identity;
    }

    final List<String> clauses = new ArrayList<>();

    clauses.add(ConfigUtility.composeClause("terminology",
        identity.getTerminology(), false));
    clauses.add(ConfigUtility.composeClause("terminologyId",
        identity.getTerminologyId(), false));
    clauses.add(ConfigUtility.composeClause("componentId",
        identity.getComponentId(), false));
    clauses.add(ConfigUtility.composeClause("componentType",
        identity.getComponentType().toString(), false));
    clauses.add(ConfigUtility.composeClause("componentTerminology",
        identity.getComponentTerminology(), false));
    clauses.add(ConfigUtility.composeClause("name", identity.getName(), true));
    clauses.add(
        ConfigUtility.composeClause("hashcode", identity.getHashcode(), false));

    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    Long id = getIdentityId(identity.getClass(), fullQuery);

    // If no id found, return null.
    if (id == null) {
      return null;
    }

    // If id found, set object id, and return object.
    identity.setId(id);
    return identity;
  }

  /* see superclass */
  @Override
  public AttributeIdentity addAttributeIdentity(
    AttributeIdentity attributeIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - add attribute identity "
            + attributeIdentity.toString());
    final AttributeIdentity newIdentity = addObject(attributeIdentity);
    if (!getTransactionPerOperation()) {
      uncommitedIdMap.put(attributeIdentity, newIdentity.getId());
    }
    return newIdentity;
  }

  /* see superclass */
  @Override
  public void updateAttributeIdentity(AttributeIdentity attributeIdentity)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - update attribute identity "
            + attributeIdentity.toString());

    updateObject(attributeIdentity);
  }

  /* see superclass */
  @Override
  public void removeAttributeIdentity(Long attributeIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove attribute identity "
            + attributeIdentityId);

    AttributeIdentity identity = getAttributeIdentity(attributeIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(Long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get semanticTypeComponent identity " + id);
    return getObject(id, SemanticTypeComponentIdentity.class);
  }

  /* see superclass */
  @Override
  public Long getNextSemanticTypeComponentId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next semanticTypeComponent id");

    Long attId = 0L;
    Long styId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("ATUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from AttributeIdentityJpa a ");
        Long attId2 = (Long) query.getSingleResult();
        attId = attId2 != null ? attId2 : attId;
      } catch (NoResultException e) {
        attId = 0L;
      }
      try {
        final javax.persistence.Query query = manager.createQuery(
            "select max(a.id) from SemanticTypeComponentIdentityJpa a ");
        Long styId2 = (Long) query.getSingleResult();
        styId = styId2 != null ? styId2 : styId;
      } catch (NoResultException e) {
        styId = 0L;
      }
      // Set the max attribute/semantic class Id
      maxIds.put("ATUI", Math.max(attId, styId));
      Logger.getLogger(getClass())
          .info("Initializing max ATUI = " + Math.max(attId, styId));

    }
    final Long result = maxIds.get("ATUI") + 1;
    maxIds.put("ATUI", result);

    return result;
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity identity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get semanticTypeComponent identity "
            + identity);

    if (uncommitedIdMap.containsKey(identity)) {
      identity.setId(uncommitedIdMap.get(identity));
      return identity;
    }

    final List<String> clauses = new ArrayList<>();

    clauses.add(ConfigUtility.composeClause("terminology",
        identity.getTerminology(), false));
    clauses.add(ConfigUtility.composeClause("conceptTerminologyId",
        identity.getConceptTerminologyId(), false));
    clauses.add(ConfigUtility.composeClause("semanticType",
        identity.getSemanticType(), true));

    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    Long id = getIdentityId(identity.getClass(), fullQuery);

    // If no id found, return null.
    if (id == null) {
      return null;
    }

    // If id found, set object id, and return object.
    identity.setId(id);
    return identity;
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity addSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity semanticTypeComponentIdentity)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - add semanticTypeComponent identity "
            + semanticTypeComponentIdentity.toString());
    final SemanticTypeComponentIdentity newIdentity =
        addObject(semanticTypeComponentIdentity);
    if (!getTransactionPerOperation()) {
      uncommitedIdMap.put(semanticTypeComponentIdentity, newIdentity.getId());
    }
    return newIdentity;
  }

  /* see superclass */
  @Override
  public void updateSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity semanticTypeComponentIdentity)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - update semanticTypeComponent identity "
            + semanticTypeComponentIdentity.toString());

    updateObject(semanticTypeComponentIdentity);
  }

  /* see superclass */
  @Override
  public void removeSemanticTypeComponentIdentity(
    Long semanticTypeComponentIdentityId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove semanticTypeComponent identity "
            + semanticTypeComponentIdentityId);

    SemanticTypeComponentIdentity identity =
        getSemanticTypeComponentIdentity(semanticTypeComponentIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public AtomIdentity getAtomIdentity(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get atom identity " + id);
    return getObject(id, AtomIdentity.class);
  }

  /* see superclass */
  @Override
  public Long getNextAtomId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next atom id");

    Long atomId = 0L;
    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("AUI")) {
      try {
        final javax.persistence.Query query =
            manager.createQuery("select max(a.id) from AtomIdentityJpa a ");
        Long atomId2 = (Long) query.getSingleResult();
        atomId = atomId2 != null ? atomId2 : atomId;
      } catch (NoResultException e) {
        atomId = 0L;
      }
      // Set the max atom class Id
      maxIds.put("AUI", atomId);
      Logger.getLogger(getClass()).info("Initializing max AUI = " + atomId);
    }
    final Long result = maxIds.get("AUI") + 1;
    maxIds.put("AUI", result);

    return result;
  }

  /* see superclass */
  @Override
  public AtomIdentity getAtomIdentity(AtomIdentity identity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get atom identity " + identity);

    if (uncommitedIdMap.containsKey(identity)) {
      identity.setId(uncommitedIdMap.get(identity));
      return identity;
    }

    final List<String> clauses = new ArrayList<>();

    clauses.add(ConfigUtility.composeClause("stringClassId",
        identity.getStringClassId(), false));
    clauses.add(ConfigUtility.composeClause("terminology",
        identity.getTerminology(), false));
    clauses.add(ConfigUtility.composeClause("terminologyId",
        identity.getTerminologyId(), false));
    clauses.add(
        ConfigUtility.composeClause("termType", identity.getTermType(), false));
    clauses.add(
        ConfigUtility.composeClause("codeId", identity.getCodeId(), false));
    clauses.add(ConfigUtility.composeClause("conceptId",
        identity.getConceptId(), false));
    clauses.add(ConfigUtility.composeClause("descriptorId",
        identity.getDescriptorId(), false));

    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    Long id = getIdentityId(identity.getClass(), fullQuery);

    // If no id found, return null.
    if (id == null) {
      return null;
    }

    // If id found, set object id, and return object.
    identity.setId(id);
    return identity;
  }

  /* see superclass */
  @Override
  public AtomIdentity addAtomIdentity(AtomIdentity atomIdentity)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - add atom identity " + atomIdentity.toString());
    final AtomIdentity newIdentity = addObject(atomIdentity);
    if (!getTransactionPerOperation()) {
      uncommitedIdMap.put(atomIdentity, newIdentity.getId());
    }
    return newIdentity;
  }

  /* see superclass */
  @Override
  public void updateAtomIdentity(AtomIdentity atomIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - update atom identity "
            + atomIdentity.toString());

    updateObject(atomIdentity);
  }

  /* see superclass */
  @Override
  public void removeAtomIdentity(Long atomIdentityId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove atom identity " + atomIdentityId);

    AtomIdentity identity = getAtomIdentity(atomIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public StringClassIdentity getStringClassIdentity(Long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get string identity " + id);

    return getObject(id, StringClassIdentity.class);
  }

  /* see superclass */
  @Override
  public Long getNextStringClassId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next string id");

    Long stringId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("SUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from StringClassIdentityJpa a ");
        Long stringId2 = (Long) query.getSingleResult();
        stringId = stringId2 != null ? stringId2 : stringId;
      } catch (NoResultException e) {
        stringId = 0L;
      }
      // Set the max string class Id
      maxIds.put("SUI", stringId);
      Logger.getLogger(getClass()).info("Initializing max SUI = " + stringId);
    }
    final Long result = maxIds.get("SUI") + 1;
    maxIds.put("SUI", result);

    return result;
  }

  /* see superclass */
  @Override
  public StringClassIdentity getStringClassIdentity(
    StringClassIdentity identity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get string identity " + identity);

    if (uncommitedIdMap.containsKey(identity)) {
      identity.setId(uncommitedIdMap.get(identity));
      return identity;
    }

    final List<String> clauses = new ArrayList<>();

    clauses.add(
        ConfigUtility.composeClause("language", identity.getLanguage(), false));
    clauses.add(ConfigUtility.composeClause("name", identity.getName(), true));

    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    Long id = getIdentityId(identity.getClass(), fullQuery);

    // If no id found, return null.
    if (id == null) {
      return null;
    }

    // If id found, set object id, and return object.
    identity.setId(id);
    return identity;
  }

  /* see superclass */
  @Override
  public StringClassIdentity addStringClassIdentity(
    StringClassIdentity stringClassIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - add string class identity "
            + stringClassIdentity.toString());
    final StringClassIdentity newIdentity = addObject(stringClassIdentity);
    if (!getTransactionPerOperation()) {
      uncommitedIdMap.put(stringClassIdentity, newIdentity.getId());
    }
    return newIdentity;
  }

  /* see superclass */
  @Override
  public void updateStringClassIdentity(StringClassIdentity stringClassIdentity)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - update string identity "
            + stringClassIdentity.toString());

    updateObject(stringClassIdentity);
  }

  /* see superclass */
  @Override
  public void removeStringClassIdentity(Long stringClassIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove string identity "
            + stringClassIdentityId);

    StringClassIdentity identity =
        getStringClassIdentity(stringClassIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public LexicalClassIdentity getLexicalClassIdentity(Long id)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get lexical class identity " + id);
    return getObject(id, LexicalClassIdentity.class);
  }

  /* see superclass */
  @Override
  public Long getNextLexicalClassId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next lexicalClass id");

    Long lexicalId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("LUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from LexicalClassIdentityJpa a ");
        Long lexicalId2 = (Long) query.getSingleResult();
        lexicalId = lexicalId2 != null ? lexicalId2 : lexicalId;
      } catch (NoResultException e) {
        lexicalId = 0L;
      }
      // Set the max lexical class Id
      maxIds.put("LUI", lexicalId);
      Logger.getLogger(getClass()).info("Initializing max LUI = " + lexicalId);
    }
    final Long result = maxIds.get("LUI") + 1;
    maxIds.put("LUI", result);

    return result;
  }

  /* see superclass */
  @Override
  public LexicalClassIdentity getLexicalClassIdentity(
    LexicalClassIdentity identity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get lexicalClass identity " + identity);

    if (uncommitedIdMap.containsKey(identity)) {
      identity.setId(uncommitedIdMap.get(identity));
      return identity;
    }

    final List<String> clauses = new ArrayList<>();

    clauses.add(
        ConfigUtility.composeClause("language", identity.getLanguage(), false));
    clauses.add(ConfigUtility.composeClause("normalizedName",
        identity.getNormalizedName(), true));

    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    Long id = getIdentityId(identity.getClass(), fullQuery);

    // If no id found, return null.
    if (id == null) {
      return null;
    }

    // If id found, set object id, and return object.
    identity.setId(id);
    return identity;
  }

  /* see superclass */
  @Override
  public LexicalClassIdentity addLexicalClassIdentity(
    LexicalClassIdentity lexicalClassIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - add lexicalClass identity "
            + lexicalClassIdentity.toString());
    final LexicalClassIdentity newIdentity = addObject(lexicalClassIdentity);
    if (!getTransactionPerOperation()) {
      uncommitedIdMap.put(lexicalClassIdentity, newIdentity.getId());
    }
    return newIdentity;
  }

  /* see superclass */
  @Override
  public void updateLexicalClassIdentity(
    LexicalClassIdentity lexicalClassIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - update lexicalClass identity "
            + lexicalClassIdentity.toString());

    updateObject(lexicalClassIdentity);
  }

  /* see superclass */
  @Override
  public void removeLexicalClassIdentity(Long lexicalClassIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove lexicalClass identity "
            + lexicalClassIdentityId);

    LexicalClassIdentity identity =
        getLexicalClassIdentity(lexicalClassIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public RelationshipIdentity getRelationshipIdentity(Long id)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get relationship identity " + id);
    return getObject(id, RelationshipIdentity.class);
  }

  /* see superclass */
  @Override
  public RelationshipIdentity createInverseRelationshipIdentity(
    RelationshipIdentity identity, String inverseRelType,
    String inverseAdditionalRelType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - creating inverse of relationship identity "
            + identity);

    // Create an inverse of the relationship
    String version =
        getTerminologyLatestVersion(identity.getTerminology()).getVersion();

    RelationshipIdentity inverseIdentity =
        new RelationshipIdentityJpa(identity);
    inverseIdentity.setId(identity.getInverseId());
    inverseIdentity.setFromId(identity.getToId());
    inverseIdentity.setFromTerminology(identity.getToTerminology());
    inverseIdentity.setFromType(identity.getToType());
    inverseIdentity.setToId(identity.getFromId());
    inverseIdentity.setToTerminology(identity.getFromTerminology());
    inverseIdentity.setToType(identity.getFromType());
    inverseIdentity.setRelationshipType(inverseRelType);
    if (!ConfigUtility.isEmpty(identity.getAdditionalRelationshipType())) {
      inverseIdentity.setAdditionalRelationshipType(inverseAdditionalRelType);
    } else {
      inverseIdentity.setAdditionalRelationshipType("");
    }
    inverseIdentity.setInverseId(identity.getId());

    return inverseIdentity;
  }

  /* see superclass */
  @Override
  public Long getNextRelationshipId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next relationship id");

    Long relationshipId = 0L;
    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("RUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from RelationshipIdentityJpa a ");
        Long relationshipId2 = (Long) query.getSingleResult();
        relationshipId =
            relationshipId2 != null ? relationshipId2 : relationshipId;
      } catch (NoResultException e) {
        relationshipId = 0L;
      }
      // Set the max relationship class Id
      maxIds.put("RUI", relationshipId);
      Logger.getLogger(getClass())
          .info("Initializing max RUI = " + relationshipId);
    }

    final Long result = maxIds.get("RUI") + 1;
    maxIds.put("RUI", result);

    return result;
  }

  /* see superclass */
  @Override
  public RelationshipIdentity getRelationshipIdentity(
    RelationshipIdentity identity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get relationship identity " + identity);

    if (uncommitedIdMap.containsKey(identity)) {
      identity.setId(uncommitedIdMap.get(identity));
      return identity;
    }

    final List<String> clauses = new ArrayList<>();

    clauses.add(ConfigUtility.composeClause("terminology",
        identity.getTerminology(), false));
    clauses.add(ConfigUtility.composeClause("terminologyId",
        identity.getTerminologyId(), false));
    clauses.add(ConfigUtility.composeClause("relationshipType",
        identity.getRelationshipType(), false));
    clauses.add(ConfigUtility.composeClause("additionalRelationshipType",
        identity.getAdditionalRelationshipType(), false));
    clauses.add(
        ConfigUtility.composeClause("fromId", identity.getFromId(), false));
    clauses.add(ConfigUtility.composeClause("fromType",
        identity.getFromType().toString(), false));
    clauses.add(ConfigUtility.composeClause("fromTerminology",
        identity.getFromTerminology(), false));
    clauses.add(ConfigUtility.composeClause("toId", identity.getToId(), false));
    clauses.add(ConfigUtility.composeClause("toType",
        identity.getToType().toString(), false));
    clauses.add(ConfigUtility.composeClause("toTerminology",
        identity.getToTerminology(), false));

    String fullQuery = ConfigUtility.composeQuery("AND", clauses);

    Long id = getIdentityId(identity.getClass(), fullQuery);

    // If no id found, return null.
    if (id == null) {
      return null;
    }

    // If id found, set object id, and return object.
    identity.setId(id);
    return identity;
  }

  /* see superclass */
  @Override
  public RelationshipIdentity addRelationshipIdentity(
    RelationshipIdentity relationshipIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - add relationship identity "
            + relationshipIdentity.toString());
    final RelationshipIdentity newIdentity = addObject(relationshipIdentity);
    if (!getTransactionPerOperation()) {
      uncommitedIdMap.put(relationshipIdentity, newIdentity.getId());
    }
    return newIdentity;
  }

  /* see superclass */
  @Override
  public void updateRelationshipIdentity(
    RelationshipIdentity relationshipIdentity) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - update relationship identity "
            + relationshipIdentity.toString());

    updateObject(relationshipIdentity);
  }

  /* see superclass */
  @Override
  public void removeRelationshipIdentity(Long relationshipIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove relationship identity "
            + relationshipIdentityId);

    RelationshipIdentity identity =
        getRelationshipIdentity(relationshipIdentityId);
    removeObject(identity);
  }

  /**
   * Returns the identity id.
   *
   * @param objectClass the object class
   * @param query the query
   * @return the identity id
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public Long getIdentityId(Class<? extends HasId> objectClass, String query)
    throws Exception {

    // Set up the "full text query"
    final FullTextEntityManager fullTextEntityManager =
        Search.getFullTextEntityManager(manager);
    final SearchFactory searchFactory =
        fullTextEntityManager.getSearchFactory();
    final QueryParser queryParser = new MultiFieldQueryParser(IndexUtility
        .getIndexedFieldNames(objectClass, true).toArray(new String[] {}),
        searchFactory.getAnalyzer(objectClass));
    final Query luceneQuery = queryParser.parse(query);
    final FullTextQuery fullTextQuery =
        fullTextEntityManager.createFullTextQuery(luceneQuery, objectClass);

    // then use a projection
    fullTextQuery.setProjection("id");
    final List<Object[]> results = fullTextQuery.getResultList();
    if (results.isEmpty()) {
      return null;
    }
    // If more than one result returned, print up to 10, and then throw an
    // error.
    if (results.size() > 1) {
      int printCount = 0;
      for (Object[] object : results) {
        Logger.getLogger(UmlsIdentityServiceJpa.class)
            .info("Returned object " + ++printCount + ": " + object[0]);
        if (printCount > 10) {
          break;
        }
      }
      throw new Exception("Error: query returned more than one id: "
          + objectClass.getSimpleName() + ", " + query);
    }

    final Long id = Long.valueOf(results.get(0)[0].toString());
    return id;
  }

  @Override
  public void commit() throws Exception {
    super.commit();
    if (!getTransactionPerOperation()) {
      uncommitedIdMap = new HashMap<>();
    }
  }

  @Override
  public void rollback() throws Exception {
    super.rollback();
    if (!getTransactionPerOperation()) {
      uncommitedIdMap = new HashMap<>();
    }
  }
}
