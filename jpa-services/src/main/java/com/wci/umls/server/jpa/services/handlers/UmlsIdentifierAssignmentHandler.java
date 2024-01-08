/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.meta.AtomIdentityJpa;
import com.wci.umls.server.jpa.meta.AttributeIdentityJpa;
import com.wci.umls.server.jpa.meta.LexicalClassIdentityJpa;
import com.wci.umls.server.jpa.meta.RelationshipIdentityJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeComponentIdentityJpa;
import com.wci.umls.server.jpa.meta.StringClassIdentityJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentHasDefinitions;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
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
import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.RelationshipIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringClassIdentity;
import com.wci.umls.server.services.UmlsIdentityService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Default implementation of {@link IdentifierAssignmentHandler}. This supports
 * "application-managed" identifier assignment.
 */
public class UmlsIdentifierAssignmentHandler extends AbstractConfigurable
    implements IdentifierAssignmentHandler {

  /** The service. */
  private UmlsIdentityService service = null;

  /** The lock. */
  private static String LOCK = "lock";

  /** The ui prefixes. */
  private Map<String, String> prefixMap = new HashMap<>();

  /** The ui lengths. */
  private Map<String, Integer> lengthMap = new HashMap<>();

  /** The project terminology. */
  private String projectTerminology = null;

  /** The max concept id. */
  private long maxConceptId = -1;

  private boolean cachesEnabled = false;

  /**
   * Flag to be able to look up identifiers WITHOUT creating a new one if none
   * found. Only used in very rare cases (e.g. umlscui.txt handling in
   * AtomLoader)
   */
  private boolean createFlag = true;

  /**
   * String=attribute identity code, Long=id
   */
  private Map<String, Long> attributeIdentityCache = new HashMap<>();

  /**
   * String=atom identity code, Long=id
   */
  private Map<String, Long> atomIdentityCache = new HashMap<>();

  /**
   * String=string class identity code, Long=id
   */
  private Map<String, Long> stringClassIdentityCache = new HashMap<>();

  /**
   * String=lexical class identity code, Long=id
   */
  private Map<String, Long> lexicalClassIdentityCache = new HashMap<>();

  /**
   * String=relationship identity code, Long=id
   */
  private Map<String, Long> relationshipIdentityCache = new HashMap<>();

  /**
   * The terminologies that have already had their attributes identities loaded
   * and cached.
   */
  private Set<String> attributeIdentityCachedTerms = new HashSet<>();

  /**
   * The terminologies that have already had their atom identities loaded and
   * cached.
   */
  private Set<String> atomIdentityCachedTerms = new HashSet<>();

  /**
   * The terminologies that have already had their relationship identities
   * loaded and cached.
   */
  private Set<String> relationshipIdentityCachedTerms = new HashSet<>();

  /**
   * Sets the creates the flag.
   *
   * @param createFlag the creates the flag
   */
  public void setCreateFlag(boolean createFlag) {
    this.createFlag = createFlag;
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p != null) {
      if (p.containsKey("aui.length")) {
        lengthMap.put("AUI", Integer.valueOf(p.getProperty("aui.length")));
      }
      if (p.containsKey("aui.prefix")) {
        prefixMap.put("AUI", p.getProperty("aui.prefix"));
      }
      if (p.containsKey("cui.length")) {
        lengthMap.put("CUI", Integer.valueOf(p.getProperty("cui.length")));
      }
      if (p.containsKey("cui.prefix")) {
        prefixMap.put("CUI", p.getProperty("cui.prefix"));
      }
      if (p.containsKey("atui.length")) {
        lengthMap.put("ATUI", Integer.valueOf(p.getProperty("atui.length")));
      }
      if (p.containsKey("atui.prefix")) {
        prefixMap.put("ATUI", p.getProperty("atui.prefix"));
      }
      if (p.containsKey("lui.length")) {
        lengthMap.put("LUI", Integer.valueOf(p.getProperty("lui.length")));
      }
      if (p.containsKey("lui.prefix")) {
        prefixMap.put("LUI", p.getProperty("lui.prefix"));
      }
      if (p.containsKey("rui.length")) {
        lengthMap.put("RUI", Integer.valueOf(p.getProperty("rui.length")));
      }
      if (p.containsKey("rui.prefix")) {
        prefixMap.put("RUI", p.getProperty("rui.prefix"));
      }
      if (p.containsKey("sui.length")) {
        lengthMap.put("SUI", Integer.valueOf(p.getProperty("sui.length")));
      }
      if (p.containsKey("sui.prefix")) {
        prefixMap.put("SUI", p.getProperty("sui.prefix"));
      }
      // Also set project terminology string
      if (p.containsKey("projectTerminology")) {
        projectTerminology = p.getProperty("projectTerminology");
      }
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    // Unpublishable concepts don't get assigned ids
    if (!concept.isPublishable()) {
      return concept.getTerminologyId();
    }

    // Return the id if it's already a CUI
    if (concept.getTerminologyId() != null
        && concept.getTerminologyId().startsWith(prefixMap.get("CUI"))) {
      return concept.getTerminologyId();
    }
    long conceptId = 0L;
    // If this is the first time this is called, lookup max ID from the database
    if (maxConceptId == -1) {
      final ContentServiceJpa service = new ContentServiceJpa();
      try {
        final javax.persistence.Query query = service.getEntityManager()
            .createQuery("select max(terminologyId) from ConceptJpa "
                + "  where terminology = :terminology "
                + "  and version = :version "
                + "  and terminologyId like :prefix "
                + "  and length(terminologyId) = "
                + "  (SELECT MAX(LENGTH(terminologyId)) FROM  ConceptJpa"
                + "  where terminology = :terminology "
                + "  and version = :version "
                + "  and terminologyId like :prefix)");
        query.setParameter("terminology", concept.getTerminology());
        query.setParameter("version", concept.getVersion());
        query.setParameter("prefix", prefixMap.get("CUI") + "%");
        final Long conceptId2 = new Long(query.getSingleResult().toString()
            .substring(prefixMap.get("CUI").length()));

        conceptId = conceptId2 != null ? conceptId2 : conceptId;
      } catch (NoResultException e) {
        conceptId = 0L;
      } 
      
      // Set the maxConceptId
      maxConceptId = conceptId;
      
      // Try looking up concept with CUI = CUIprefix + (maxConceptId + 1), to
      // ensure that maxConceptId is truly the max
      final Concept testConcept =
          service.getConcept(prefixMap.get("CUI") + (maxConceptId + 1),
              concept.getTerminology(), concept.getVersion(), Branch.ROOT);

      service.close();
      
      if (testConcept != null) {
        throw new Exception("max CUI lookup failure: " + prefixMap.get("CUI")
            + maxConceptId + " is not the maximum CUI");
      }

      Logger.getLogger(getClass())
          .info("Initializing max CUI = " + maxConceptId);
    }

    final long result = ++maxConceptId;
    return convertId(result, "CUI");
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Descriptor descriptor) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Code code) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(StringClass stringClass) throws Exception {

    if (!stringClass.isPublishable()) {
      return stringClass.getTerminologyId();
    }
    // Return the id if it's already a SUI
    if (stringClass.getTerminologyId() != null
        && stringClass.getTerminologyId().startsWith(prefixMap.get("SUI"))) {
      return stringClass.getTerminologyId();
    }

    UmlsIdentityService localService = getService();
    try {
      // Block between getting next id and saving the id value
      synchronized (LOCK) {
        // Create StringClassIdentity and populate from the stringClass.
        final StringClassIdentity identity = new StringClassIdentityJpa();
        identity.setName(stringClass.getName());
        identity.setLanguage(stringClass.getLanguage());

        Long identityId = null;
        // If caches are enabled (e.g. insertion processes), lookup identity id
        // from cache
        if (cachesEnabled) {
          // If this is the first time this has been called,
          // populate the cache
          if (stringClassIdentityCache.isEmpty()) {
            cacheExistingStringClassIdentities();
          }

          // Check if this identity exists in cache
          if (stringClassIdentityCache
              .containsKey(identity.getIdentityCode())) {
            identityId =
                stringClassIdentityCache.get(identity.getIdentityCode());
          }
        }
        // If caches aren't enable (e.g. action performed by user via the UI),
        // lookup identity id via database query
        else {
          final StringClassIdentity identity2 =
              localService.getStringClassIdentity(identity);

          // Reuse existing id
          if (identity2 != null) {
            identityId = identity2.getId();
          }
        }

        // if id found, return
        if (identityId != null) {
          return convertId(identityId, "SUI");
        }
        // else generate a new one and add it
        else {
          if (!createFlag) {
            return null;
          }
          // Get next id
          final Long nextId = localService.getNextStringClassId();

          // Add new identity object
          identity.setId(nextId);
          localService.addStringClassIdentity(identity);
          // Add identity to cache, if caches enabled
          if (cachesEnabled) {
            stringClassIdentityCache.put(identity.getIdentityCode(),
                identity.getId());
          }
          return convertId(nextId, "SUI");
        }
      }

    } catch (

    Exception e) {
      throw e;
    } finally {
      closeService(localService);
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {

    if (!lexicalClass.isPublishable()) {
      return lexicalClass.getTerminologyId();
    }

    // Return the id if it's already a LUI
    if (lexicalClass.getTerminologyId() != null
        && lexicalClass.getTerminologyId().startsWith(prefixMap.get("LUI"))) {
      return lexicalClass.getTerminologyId();
    }

    UmlsIdentityService localService = getService();
    try {
      // Block between getting next id and saving the id value
      synchronized (LOCK) {
        // Create LexicalClassIdentity and populate from the lexicalClass.
        final LexicalClassIdentity identity = new LexicalClassIdentityJpa();
        identity.setLanguage(lexicalClass.getLanguage());
        identity.setNormalizedName(lexicalClass.getNormalizedName());

        Long identityId = null;
        // If caches are enabled (e.g. insertion processes), lookup identity id
        // from cache
        if (cachesEnabled) {
          // If this is the first time this has been called,
          // populate the cache
          if (lexicalClassIdentityCache.isEmpty()) {
            cacheExistingLexicalClassIdentities();
          }

          // Check if this identity exists in cache
          if (lexicalClassIdentityCache
              .containsKey(identity.getIdentityCode())) {
            identityId =
                lexicalClassIdentityCache.get(identity.getIdentityCode());
          }
        }
        // If caches aren't enable (e.g. action performed by user via the UI),
        // lookup identity id via database query
        else {
          final LexicalClassIdentity identity2 =
              localService.getLexicalClassIdentity(identity);

          // Reuse existing id
          if (identity2 != null) {
            identityId = identity2.getId();
          }
        }

        // if id found, return
        if (identityId != null) {
          return convertId(identityId, "LUI");
        }
        // else generate a new one and add it
        else {
          // Get next id
          final Long nextId = localService.getNextLexicalClassId();
          // Add new identity object
          identity.setId(nextId);
          localService.addLexicalClassIdentity(identity);
          // Add identity to cache
          lexicalClassIdentityCache.put(identity.getIdentityCode(),
              identity.getId());
          return convertId(nextId, "LUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      closeService(localService);
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Atom atom) throws Exception {

    if (!atom.isPublishable()) {
      return atom.getTerminologyId();
    }
    // Return the id if it's already a AUI
    if (atom.getTerminologyId() != null
        && atom.getTerminologyId().startsWith(prefixMap.get("AUI"))) {
      return atom.getTerminologyId();
    }

    UmlsIdentityService localService = getService();
    try {
      // Block between getting next id and saving the id value
      synchronized (LOCK) {
        // Create AtomIdentity and populate from the atom.
        final AtomIdentity identity = new AtomIdentityJpa();
        identity.setCodeId(atom.getCodeId());
        identity.setConceptId(atom.getConceptId());
        identity.setDescriptorId(atom.getDescriptorId());
        identity.setStringClassId(atom.getStringClassId());
        identity.setTerminology(atom.getTerminology());
        identity.setTerminologyId(atom.getTerminologyId());
        identity.setTermType(atom.getTermType());

        Long identityId = null;

        // If caches are enabled (e.g. insertion processes), lookup identity id
        // from cache
        if (cachesEnabled) {
          // If this is the first time this has been called for this
          // terminology, populate the cache
          if (!atomIdentityCachedTerms.contains(identity.getTerminology())) {
            cacheExistingAtomIdentities(identity.getTerminology());
          }

          // Check if this identity exists in cache
          if (atomIdentityCache.containsKey(identity.getIdentityCode())) {
            identityId = atomIdentityCache.get(identity.getIdentityCode());
          }
        }
        // If caches aren't enable (e.g. action performed by user via the UI),
        // lookup identity id via database query
        else {
          final AtomIdentity identity2 = localService.getAtomIdentity(identity);

          // Reuse existing id
          if (identity2 != null) {
            identityId = identity2.getId();
          }
        }

        // if id found, return
        if (identityId != null) {
          return convertId(identityId, "AUI");
        }
        // else generate a new one and add it
        else {
          if (!createFlag) {
            return null;
          }
          // Get next id
          final Long nextId = localService.getNextAtomId();
          // Add new identity object
          identity.setId(nextId);
          localService.addAtomIdentity(identity);
          // Add identity to cache, if caches enabled
          if (cachesEnabled) {
            atomIdentityCache.put(identity.getIdentityCode(), identity.getId());
          }
          return convertId(nextId, "AUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      closeService(localService);
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Attribute attribute, ComponentInfo component)
    throws Exception {

    if (!attribute.isPublishable()) {
      return attribute.getTerminologyId();
    }
    // Return the id if it's already a ATUI
    if (attribute.getTerminologyId().startsWith(prefixMap.get("ATUI"))) {
      return attribute.getTerminologyId();
    }

    UmlsIdentityService localService = getService();

    try {
      // Create AttributeIdentity and populate from the attribute.
      final AttributeIdentity identity = new AttributeIdentityJpa();
      identity.setHashcode(ConfigUtility.getMd5(attribute.getValue()));
      identity.setName(attribute.getName());
      if (component instanceof Atom) {
        identity.setComponentId(((Atom) component).getAlternateTerminologyIds()
            .get(projectTerminology));
      } else if (component instanceof Relationship) {
        identity.setComponentId(((Relationship<?, ?>) component)
            .getAlternateTerminologyIds().get(projectTerminology));
      } else {
        identity.setComponentId(component.getTerminologyId());
      }
      if (identity.getComponentId() == null) {
        throw new Exception("unexpected null terminology id " + component);
      }
      identity.setComponentTerminology(component.getTerminology());
      identity.setComponentType(component.getType());
      identity.setTerminology(attribute.getTerminology());
      identity.setTerminologyId(attribute.getTerminologyId());

      Long identityId = null;
      // If caches are enabled (e.g. insertion processes), lookup identity id
      // from cache
      if (cachesEnabled) {
        // If this is the first time this has been called for this terminology
        // and
        // name,
        // populate the cache
        if (!attributeIdentityCachedTerms
            .contains(identity.getTerminology() + identity.getName())) {
          cacheExistingAttributeIdentities(identity.getTerminology(),
              identity.getName());
        }

        // Check if this identity exists in the cache
        if (attributeIdentityCache.containsKey(identity.getIdentityCode())) {
          identityId = attributeIdentityCache.get(identity.getIdentityCode());
        }
      }
      // If caches aren't enable (e.g. action performed by user via the UI),
      // lookup identity id via database query
      else {
        final AttributeIdentity identity2 =
            localService.getAttributeIdentity(identity);

        // Reuse existing id
        if (identity2 != null) {

          identityId = identity2.getId();
        }
      }

      // if id found, return
      if (identityId != null) {
        return convertId(identityId, "ATUI");
      }
      // else generate a new one and add it
      else {
        synchronized (LOCK) {
          // Block between getting next id and saving the id value
          // Get next id
          final Long nextId = localService.getNextAttributeId();
          // Add new identity object
          identity.setId(nextId);
          attributeIdentityCache.put(identity.getIdentityCode(), nextId);
          localService.addAttributeIdentity(identity);
          // Add identity to cache, if caches enabled
          if (cachesEnabled) {
            attributeIdentityCache.put(identity.getIdentityCode(),
                identity.getId());
          }
          return convertId(nextId, "ATUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      closeService(localService);
    }
  }

  private void cacheExistingAttributeIdentities(String terminology, String name)
    throws Exception {

    Logger.getLogger(getClass())
        .info("Loading Attribute Identities for terminology = " + terminology
            + ", name = " + name);

    final Session session =
        getService().getEntityManager().unwrap(Session.class);
    final org.hibernate.Query hQuery = session.createSQLQuery(
        "select id, componentId, componentTerminology, hashCode, terminologyId from attribute_identity "
            + "where terminology = :terminology and name = :name");
    hQuery.setParameter("terminology", terminology);
    hQuery.setParameter("name", name);
    hQuery.setReadOnly(true).setFetchSize(100000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {

      final Long id = ((BigInteger) results.get()[0]).longValue();
      final String componentId = (String) results.get()[1];
      final String componentTerminology = (String) results.get()[2];
      final String hashcode = (String) results.get()[3];
      final String terminologyId = (String) results.get()[4];
      final String identityCode = componentId + componentTerminology + hashcode
          + name + terminology + terminologyId;
      attributeIdentityCache.put(identityCode, id);
    }
    results.close();

    // Add this terminology and name to the cached set.
    attributeIdentityCachedTerms.add(terminology + name);
  }

  private void cacheExistingAtomIdentities(String terminology)
    throws Exception {

    Logger.getLogger(getClass())
        .info("Loading Atom Identities for terminology = " + terminology);

    final Session session =
        getService().getEntityManager().unwrap(Session.class);
    final org.hibernate.Query hQuery = session.createSQLQuery(
        "select id, stringClassId, terminologyId, termType, codeId, conceptId, descriptorId from atom_identity "
            + "where terminology = :terminology");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(100000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {

      final Long id = ((BigInteger) results.get()[0]).longValue();
      final String stringClassId = (String) results.get()[1];
      final String terminologyId = (String) results.get()[2];
      final String termType = (String) results.get()[3];
      final String codeId = (String) results.get()[4];
      final String conceptId = (String) results.get()[5];
      final String descriptorId = (String) results.get()[6];
      final String identityCode = stringClassId + terminology + terminologyId
          + termType + codeId + conceptId + descriptorId;
      atomIdentityCache.put(identityCode, id);
    }
    results.close();

    // Add this terminology to the cached set.
    atomIdentityCachedTerms.add(terminology);
  }

  private void cacheExistingStringClassIdentities() throws Exception {

    Logger.getLogger(getClass()).info("Loading String Class Identities");

    final Session session =
        getService().getEntityManager().unwrap(Session.class);
    final org.hibernate.Query hQuery = session
        .createSQLQuery("select id, name, language from string_class_identity");
    hQuery.setReadOnly(true).setFetchSize(100000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {

      final Long id = ((BigInteger) results.get()[0]).longValue();
      final String name = (String) results.get()[1];
      final String language = (String) results.get()[2];
      final String identityCode = name + language;
      stringClassIdentityCache.put(identityCode, id);
    }
    results.close();
  }

  private void cacheExistingLexicalClassIdentities() throws Exception {

    Logger.getLogger(getClass()).info("Loading Lexical Class Identities");

    final Session session =
        getService().getEntityManager().unwrap(Session.class);
    final org.hibernate.Query hQuery = session.createSQLQuery(
        "select id, language, normalizedName from lexical_class_identity");
    hQuery.setReadOnly(true).setFetchSize(100000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {

      final Long id = ((BigInteger) results.get()[0]).longValue();
      final String language = (String) results.get()[1];
      final String normalizedName = (String) results.get()[2];
      final String identityCode = language + normalizedName;
      lexicalClassIdentityCache.put(identityCode, id);
    }
    results.close();
  }

  private void cacheExistingRelationshipIdentities(String terminology)
    throws Exception {

    Logger.getLogger(getClass()).info(
        "Loading Relationship Identities for terminology = " + terminology);

    final Session session =
        getService().getEntityManager().unwrap(Session.class);
    final org.hibernate.Query hQuery = session.createSQLQuery(
        "select id, additionalRelationshipType, fromId, fromTerminology, fromType, "
            + "relationshipType, terminologyId, toId, toTerminology, "
            + "toType from relationship_identity "
            + "where terminology = :terminology");
    hQuery.setParameter("terminology", terminology);
    hQuery.setReadOnly(true).setFetchSize(100000).setCacheable(false);
    final ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {

      final Long id = ((BigInteger) results.get()[0]).longValue();
      final String additionalRelationshipType = (String) results.get()[1];
      final String fromId = (String) results.get()[2];
      final String fromTerminology = (String) results.get()[3];
      final String fromType = (String) results.get()[4];
      final String relationshipType = (String) results.get()[5];
      final String terminologyId = (String) results.get()[6];
      final String toId = (String) results.get()[7];
      final String toTerminology = (String) results.get()[8];
      final String toType = (String) results.get()[9];
      final String identityCode = additionalRelationshipType + fromId
          + fromTerminology + fromType + relationshipType + terminology
          + terminologyId + toId + toTerminology + toType;
      relationshipIdentityCache.put(identityCode, id);
    }
    results.close();

    // Add this terminology to the cached set.
    relationshipIdentityCachedTerms.add(terminology);
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    final Attribute attribute = new AttributeJpa();
    attribute.setName("DEFINITION");
    attribute.setValue(definition.getValue());
    attribute.setObsolete(definition.isObsolete());
    attribute.setSuppressible(definition.isSuppressible());
    attribute.setPublishable(definition.isPublishable());
    attribute.setPublished(definition.isPublished());
    attribute.setTerminologyId(definition.getTerminologyId());
    attribute.setTerminology(definition.getTerminology());
    attribute.setVersion(definition.getVersion());
    return getTerminologyId(attribute, component);
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship,
    String inverseRelType, String inverseAdditionalRelType) throws Exception {

    if (!relationship.isPublishable() && !inverseRelType.startsWith("B")) {
      return "";
    }
    // Return the id if it's already a RUI
    if (relationship.getTerminologyId().startsWith(prefixMap.get("RUI"))) {
      return relationship.getTerminologyId();
    }

    UmlsIdentityService localService = getService();
    try {
      // Block between getting next id and saving the id value
      synchronized (LOCK) {
        // Create RelationshipIdentity and populate from the relationship.
        final RelationshipIdentity identity = new RelationshipIdentityJpa();
        identity.setId(relationship.getId());
        identity.setTerminology(relationship.getTerminology());
        identity.setTerminologyId(relationship.getTerminologyId());
        identity.setRelationshipType(relationship.getRelationshipType());
        identity.setAdditionalRelationshipType(
            relationship.getAdditionalRelationshipType());
        identity.setFromTerminology(relationship.getFrom().getTerminology());
        identity.setFromType(relationship.getFrom().getType());
        identity.setToTerminology(relationship.getTo().getTerminology());
        identity.setToType(relationship.getTo().getType());
        // If to/from objects are atoms, to/fromId need to be the AUI. Otherwise
        // set to the terminologyId
        if (relationship.getFrom() instanceof Atom) {
          identity.setFromId(((Atom) relationship.getFrom())
              .getAlternateTerminologyIds().get(projectTerminology));
        } else {
          identity.setFromId(relationship.getFrom().getTerminologyId());
        }
        if (relationship.getTo() instanceof Atom) {
          identity.setToId(((Atom) relationship.getTo())
              .getAlternateTerminologyIds().get(projectTerminology));
        } else {
          identity.setToId(relationship.getTo().getTerminologyId());
        }

        Long identityId = null;
        // If caches are enabled (e.g. insertion processes), lookup identity id
        // from cache
        if (cachesEnabled) {
          // If this is the first time this has been called for this
          // terminology,
          // populate the cache
          if (!relationshipIdentityCachedTerms
              .contains(identity.getTerminology())) {
            cacheExistingRelationshipIdentities(identity.getTerminology());
          }

          // Check if this identity exists in cache
          if (relationshipIdentityCache
              .containsKey(identity.getIdentityCode())) {
            identityId =
                relationshipIdentityCache.get(identity.getIdentityCode());
          }
        }
        // If caches aren't enable (e.g. action performed by user via the UI),
        // lookup identity id via database query
        else {
          final RelationshipIdentity identity2 =
              localService.getRelationshipIdentity(identity);

          // Reuse existing id
          if (identity2 != null) {
            identityId = identity2.getId();
          }
        }

        // if id found, return
        if (identityId != null) {
          return convertId(identityId, "RUI");
        }
        // else generate a new one and add it
        else {
          // Get next id and inverse ID
          final Long nextId = localService.getNextRelationshipId();

          // Set ID for the relationship. Set inverseId to bogus number for now
          // - it will be updated later.
          identity.setId(nextId);
          identity.setInverseId(0L);

          // Add new identity object
          localService.addRelationshipIdentity(identity);

          // Create inverse Relationship identity
          final RelationshipIdentity inverseIdentity =
              localService.createInverseRelationshipIdentity(identity,
                  inverseRelType, inverseAdditionalRelType);

          // Get next id for inverse relationship
          final Long nextIdInverse = localService.getNextRelationshipId();

          // Set ID and inverse IDs for the inverse Id
          inverseIdentity.setId(nextIdInverse);
          inverseIdentity.setInverseId(nextId);

          // Add inverse identity objects
          localService.addRelationshipIdentity(inverseIdentity);

          // Update the identity objects with the true InverseId
          identity.setInverseId(nextIdInverse);
          localService.updateRelationshipIdentity(identity);

          // Add identities to cache, if caches enabled
          if (cachesEnabled) {
            relationshipIdentityCache.put(identity.getIdentityCode(),
                identity.getId());
            relationshipIdentityCache.put(inverseIdentity.getIdentityCode(),
                inverseIdentity.getId());
          }

          // return ID for called relationship (inverse can get called later)
          return convertId(nextId, "RUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      closeService(localService);
    }
  }

  /* see superclass */
  @Override
  public String getInverseTerminologyId(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship,
    String inverseRelType, String inverseAdditionalRelType) throws Exception {

    if (!relationship.isPublishable()) {
      return "";
    }

    if (service == null) {
      service = new UmlsIdentityServiceJpa();
    }
    try {
      // Create RelationshipIdentity and populate from the relationship.
      final RelationshipIdentity identity = new RelationshipIdentityJpa();
      // identity.setId(relationship.getId());
      identity.setTerminology(relationship.getTerminology());
      identity.setTerminologyId(relationship.getTerminologyId());
      identity.setRelationshipType(relationship.getRelationshipType());
      identity.setAdditionalRelationshipType(
          relationship.getAdditionalRelationshipType());
      identity.setFromId(relationship.getFrom().getTerminologyId());
      identity.setFromTerminology(relationship.getFrom().getTerminology());
      identity.setFromType(relationship.getFrom().getType());
      identity.setToId(relationship.getTo().getTerminologyId());
      identity.setToTerminology(relationship.getTo().getTerminology());
      identity.setToType(relationship.getTo().getType());

      final RelationshipIdentity inverseIdentity =
          service.createInverseRelationshipIdentity(identity, inverseRelType,
              inverseAdditionalRelType);

      final RelationshipIdentity identity2 =
          service.getRelationshipIdentity(inverseIdentity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "RUI");
      }
      // else generate a new one and add it
      else {
        throw new Exception(
            "Unexpected missing inverse of relationship " + relationship);
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (getTransactionPerOperation()) {
        service.close();
      }
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Subset subset) throws Exception {
    // Map sets are represented by CUIs -> at release time
    // when the concept is assigned a CUI, the corresponding map set should
    // be as well
    return null;
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> member)
    throws Exception {
    // Subset members don't themselves get ATUIs, their attributes do
    // but with the SUBSET_MEMBER=12345~ATN~atv attribute format
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent,
    Concept concept) throws Exception {

    if (!semanticTypeComponent.isPublishable()) {
      return "";
    }

    // Return the id if it's already a ATUI
    if (semanticTypeComponent.getTerminologyId()
        .startsWith(prefixMap.get("ATUI"))) {
      return semanticTypeComponent.getTerminologyId();
    }

    final UmlsIdentityService localService = getService();
    try {
      // Block between getting next id and saving the id value
      synchronized (LOCK) {

        // Create semanticTypeIdentity and populate from the semanticType.
        final SemanticTypeComponentIdentity identity =
            new SemanticTypeComponentIdentityJpa();
        identity.setConceptTerminologyId(concept.getTerminologyId());
        identity.setSemanticType(semanticTypeComponent.getSemanticType());
        identity.setTerminology(semanticTypeComponent.getTerminology());

        final SemanticTypeComponentIdentity identity2 =
            localService.getSemanticTypeComponentIdentity(identity);

        // Reuse existing id
        if (identity2 != null) {
          return convertId(identity2.getId(), "ATUI");
        }
        // else generate a new one and add it
        else {
          // Get next id
          final Long nextId = localService.getNextSemanticTypeComponentId();
          // Add new identity object
          identity.setId(nextId);
          localService.addSemanticTypeComponentIdentity(identity);
          return convertId(nextId, "ATUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      closeService(localService);
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Mapping mapping) throws Exception {
    // mappings don't themselves get ATUIs, their XMAPFROM, XMAP, and XMAPTO
    // renderings do which populate various ids of the mapping
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(MapSet mapSet) throws Exception {
    // Map sets are represented by CUIs -> at release time
    // when the concept is assigned a CUI, the corresponding map set should
    // be as well
    return "";
  }

  /* see superclass */
  @Override
  public boolean allowIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "UMLS Id Assignment Algorithm";
  }

  /**
   * Convert id.
   *
   * @param id the id
   * @param type the type
   * @return the string
   * @throws Exception the exception
   */
  public String convertId(Long id, String type) throws Exception {
    if (!prefixMap.containsKey(type) && !lengthMap.containsKey(type)) {
      throw new Exception("Identifier type " + type + " is not configured");
    }
    final int length = lengthMap.get(type);
    if (id.toString().length() < length) {
      final String idStr = id.toString();
      final int startIndex = idStr.length() + 19 - length;
      final String convertedId = prefixMap.get(type)
          + ("0000000000000000000" + idStr).substring(startIndex);
      return convertedId;
    } else {
      return prefixMap.get(type) + id;
    }

  }

  /**
   * Returns the service.
   *
   * @return the service
   * @throws Exception the exception
   */
  public UmlsIdentityService getService() throws Exception {
    if (service != null && !service.getTransactionPerOperation()) {
      return service;
    } else if (service == null) {
      return new UmlsIdentityServiceJpa();
    } else {
      throw new Exception(
          "Illegal state - service not null with transactionPerOperation.");
    }
  }

  /**
   * Close service.
   *
   * @param aService the a service
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public void closeService(UmlsIdentityService aService) throws Exception {
    if (aService != null && !aService.getTransactionPerOperation()) {
      // N/A
    } else if (aService != null) {
      aService.close();
    } else {
      throw new Exception(
          "Illegal state - service not null with transactionPerOperation.");
    }
  }

  /* see superclass */
  @Override
  public boolean getTransactionPerOperation() throws Exception {
    return service.getTransactionPerOperation();
  }

  /* see superclass */
  @Override
  public void setTransactionPerOperation(boolean transactionPerOperation)
    throws Exception {
    if (!transactionPerOperation) {
      service = new UmlsIdentityServiceJpa();
      service.setTransactionPerOperation(transactionPerOperation);
      cachesEnabled = true;
    } else {
      service = null;
    }
  }

  /* see superclass */
  @Override
  public void commit() throws Exception {
    service.commit();
  }

  /* see superclass */
  @Override
  public void rollback() throws Exception {
    service.rollback();
  }

  /* see superclass */
  @Override
  public void beginTransaction() throws Exception {
    service.beginTransaction();
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    clearCaches();
    service.close();
  }

  /* see superclass */
  @Override
  public void clear() throws Exception {
    service.clear();
  }

  /* see superclass */
  @Override
  public void commitClearBegin() throws Exception {
    service.commitClearBegin();
  }

  /* see superclass */
  @Override
  public void logAndCommit(int objectCt, int logCt, int commitCt)
    throws Exception {
    service.logAndCommit(objectCt, logCt, commitCt);
  }

  /* see superclass */
  @Override
  public void silentIntervalCommit(int objectCt, int logCt, int commitCt)
    throws Exception {
    service.silentIntervalCommit(objectCt, logCt, commitCt);
  }

  /**
   * Clear out all of the caches.
   */
  @SuppressWarnings("static-method")
  public void clearCaches() {
    attributeIdentityCache.clear();
    attributeIdentityCachedTerms.clear();
    atomIdentityCache.clear();
    atomIdentityCachedTerms.clear();
    relationshipIdentityCache.clear();
    relationshipIdentityCachedTerms.clear();
    stringClassIdentityCache.clear();
    lexicalClassIdentityCache.clear();
  }
}
