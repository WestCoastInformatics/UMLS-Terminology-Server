/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.Identity;
import com.wci.umls.server.jpa.meta.AtomIdentityJpa;
import com.wci.umls.server.jpa.meta.LexicalClassIdentityJpa;
import com.wci.umls.server.jpa.meta.RelationshipIdentityJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeComponentIdentityJpa;
import com.wci.umls.server.jpa.meta.StringClassIdentityJpa;
import com.wci.umls.server.jpa.services.handlers.DefaultSearchHandler;
import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.RelationshipIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringClassIdentity;
import com.wci.umls.server.services.UmlsIdentityService;
import com.wci.umls.server.services.handlers.SearchHandler;

/**
 * JPA and JAXB enabled implementation of {@link UmlsIdentityService}.
 */
public class UmlsIdentityServiceJpa extends MetadataServiceJpa
    implements UmlsIdentityService {

  /** The uncommited id map. */
  private static Map<Object, Long> uncommitedIdMap = new HashMap<>();

  /** The max ids. */
  private static Map<String, Long> maxIds = new HashMap<>();

  /** The handler. */
  private SearchHandler handler = new DefaultSearchHandler();

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
  public AttributeIdentity getAttributeIdentity(long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get attribute identity " + id);
    return getObject(id, AttributeIdentity.class);
  }

  /* see superclass */
  @Override
  public long getNextAttributeId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next attribute id");

    long attId = 0L;
    long styId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("ATUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from AttributeIdentityJpa a ");
        final Long attId2 = (Long) query.getSingleResult();
        attId = attId2 != null ? attId2 : attId;
      } catch (NoResultException e) {
        attId = 0L;
      }
      try {
        final javax.persistence.Query query = manager.createQuery(
            "select max(a.id) from SemanticTypeComponentIdentityJpa a ");
        final Long styId2 = (Long) query.getSingleResult();
        styId = styId2 != null ? styId2 : styId;
      } catch (NoResultException e) {
        styId = 0L;
      }
      // Set the max attribute/semantic class Id
      maxIds.put("ATUI", Math.max(attId, styId));
      Logger.getLogger(getClass())
          .info("Initializing max ATUI = " + Math.max(attId, styId));

    }
    final long result = maxIds.get("ATUI") + 1;
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

    final long id = getIdentityId(identity);

    // If no id found, return null.
    if (id == -1) {
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
  public void removeAttributeIdentity(long attributeIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove attribute identity "
            + attributeIdentityId);

    final AttributeIdentity identity =
        getAttributeIdentity(attributeIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(long id)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - get semanticTypeComponent identity " + id);
    return getObject(id, SemanticTypeComponentIdentityJpa.class);
  }

  /* see superclass */
  @Override
  public long getNextSemanticTypeComponentId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next semanticTypeComponent id");

    long attId = 0L;
    long styId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("ATUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from AttributeIdentityJpa a ");
        final Long attId2 = (Long) query.getSingleResult();
        attId = attId2 != null ? attId2 : attId;
      } catch (NoResultException e) {
        attId = 0L;
      }
      try {
        final javax.persistence.Query query = manager.createQuery(
            "select max(a.id) from SemanticTypeComponentIdentityJpa a ");
        final Long styId2 = (Long) query.getSingleResult();
        styId = styId2 != null ? styId2 : styId;
      } catch (NoResultException e) {
        styId = 0L;
      }
      // Set the max attribute/semantic class Id
      maxIds.put("ATUI", Math.max(attId, styId));
      Logger.getLogger(getClass())
          .info("Initializing max ATUI = " + Math.max(attId, styId));

    }
    final long result = maxIds.get("ATUI") + 1;
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

    final long id = getIdentityId(identity);

    // If no id found, return null.
    if (id == -1) {
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
    long semanticTypeComponentIdentityId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove semanticTypeComponent identity "
            + semanticTypeComponentIdentityId);

    final SemanticTypeComponentIdentity identity =
        getSemanticTypeComponentIdentity(semanticTypeComponentIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public AtomIdentity getAtomIdentity(long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get atom identity " + id);
    return getObject(id, AtomIdentityJpa.class);
  }

  /* see superclass */
  @Override
  public long getNextAtomId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next atom id");

    long atomId = 0L;
    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("AUI")) {
      try {
        final javax.persistence.Query query =
            manager.createQuery("select max(a.id) from AtomIdentityJpa a ");
        final Long atomId2 = (Long) query.getSingleResult();
        atomId = atomId2 != null ? atomId2 : atomId;
      } catch (NoResultException e) {
        atomId = 0L;
      }
      // Set the max atom class Id
      maxIds.put("AUI", atomId);
      Logger.getLogger(getClass())
          .info("Initializing max AUI = " + (atomId + 1));
    }
    final long result = maxIds.get("AUI") + 1;
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

    final long id = getIdentityId(identity);

    // If no id found, return null.
    if (id == -1) {
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
  public void removeAtomIdentity(long atomIdentityId) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - remove atom identity " + atomIdentityId);

    final AtomIdentity identity = getAtomIdentity(atomIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public StringClassIdentity getStringClassIdentity(long id) throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get string identity " + id);

    return getObject(id, StringClassIdentityJpa.class);
  }

  /* see superclass */
  @Override
  public long getNextStringClassId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next string id");

    long stringId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("SUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from StringClassIdentityJpa a ");
        final Long stringId2 = (Long) query.getSingleResult();
        stringId = stringId2 != null ? stringId2 : stringId;
      } catch (NoResultException e) {
        stringId = 0L;
      }
      // Set the max string class Id
      maxIds.put("SUI", stringId);
      Logger.getLogger(getClass())
          .info("Initializing max SUI = " + (stringId + 1));
    }
    final long result = maxIds.get("SUI") + 1;
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

    final long id = getIdentityId(identity);

    // If no id found, return null.
    if (id == -1) {
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
  public void removeStringClassIdentity(long stringClassIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove string identity "
            + stringClassIdentityId);

    final StringClassIdentity identity =
        getStringClassIdentity(stringClassIdentityId);
    removeObject(identity);
  }

  /* see superclass */
  @Override
  public LexicalClassIdentity getLexicalClassIdentity(long id)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get lexical class identity " + id);
    return getObject(id, LexicalClassIdentityJpa.class);
  }

  /* see superclass */
  @Override
  public long getNextLexicalClassId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next lexicalClass id");

    long lexicalId = 0L;

    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("LUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from LexicalClassIdentityJpa a ");
        final Long lexicalId2 = (Long) query.getSingleResult();
        lexicalId = lexicalId2 != null ? lexicalId2 : lexicalId;
      } catch (NoResultException e) {
        lexicalId = 0L;
      }
      // Set the max lexical class Id
      maxIds.put("LUI", lexicalId);
      Logger.getLogger(getClass())
          .info("Initializing max LUI = " + (lexicalId + 1));
    }
    final long result = maxIds.get("LUI") + 1;
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

    final long id = getIdentityId(identity);

    // If no id found, return null.
    if (id == -1) {
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
  public void removeLexicalClassIdentity(long lexicalClassIdentityId)
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
  public RelationshipIdentity getRelationshipIdentity(long id)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get relationship identity " + id);
    return getObject(id, RelationshipIdentityJpa.class);
  }

  /* see superclass */
  @Override
  public RelationshipIdentity createInverseRelationshipIdentity(
    RelationshipIdentity identity, String inverseRelType,
    String inverseAdditionalRelType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Umls Identity Service - creating inverse of relationship identity "
            + identity);

    final RelationshipIdentity inverseIdentity =
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
  public long getNextRelationshipId() throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - get next relationship id");

    long relationshipId = 0L;
    // If this is the first time this is called, lookup max ID from the database
    if (!maxIds.containsKey("RUI")) {
      try {
        final javax.persistence.Query query = manager
            .createQuery("select max(a.id) from RelationshipIdentityJpa a ");
        final Long relationshipId2 = (Long) query.getSingleResult();
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

    final long result = maxIds.get("RUI") + 1;
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

    final long id = getIdentityId(identity);

    // If no id found, return null.
    if (id == -1) {
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
  public void removeRelationshipIdentity(long relationshipIdentityId)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Umls Identity Service - remove relationship identity "
            + relationshipIdentityId);

    final RelationshipIdentity identity =
        getRelationshipIdentity(relationshipIdentityId);
    removeObject(identity);
  }

  /**
   * Returns the identity id.
   *
   * @param identity the identity
   * @return the identity id
   * @throws Exception the exception
   */
  public long getIdentityId(Identity identity) throws Exception {

    // Set up the "full text query"
    final List<Long> results = handler.getIdResults(null, null, Branch.ROOT,
        "identityCode:\"" + identity.getIdentityCode() + "\"", null, identity.getClass(),
        null, new int[1], manager);

    if (results.isEmpty()) {
      return -1L;
    }

    if (results.size() > 1) {
      throw new Exception("Error: identity code returned more than one id: "
          + identity.getIdentityCode());
    }

    return results.get(0);
  }


  /* see superclass */
  @Override
  public void beginTransaction() throws Exception {
    super.beginTransaction();
    // Create a new handler for the new transaction
    handler = new DefaultSearchHandler();
  }
  
  /* see superclass */
  @Override
  public void commit() throws Exception {
    super.commit();
    if (!getTransactionPerOperation()) {
      uncommitedIdMap = new HashMap<>();
    }
  }

  /* see superclass */
  @Override
  public void rollback() throws Exception {
    super.rollback();
    if (!getTransactionPerOperation()) {
      uncommitedIdMap = new HashMap<>();
    }
  }

}
