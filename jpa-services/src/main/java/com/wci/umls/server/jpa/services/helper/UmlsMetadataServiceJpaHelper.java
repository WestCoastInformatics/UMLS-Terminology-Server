/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.LanguageList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.LanguageListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.services.MetadataService;

/**
 * Default implementation of {@link MetadataService}.
 */
public class UmlsMetadataServiceJpaHelper extends
    StandardMetadataServiceJpaHelper {

  /** The UMLS terminology. */
  public String umlsTerminology;

  /** The UMLS version. */
  public String umlsVersion;

  /** The term types map. */
  public static Map<String, Set<String>> termTypesMap = new HashMap<>();

  /** The additional relationship types map. */
  public static Map<String, Set<String>> additionalRelationshipTypesMap =
      new HashMap<>();

  /** The relationship types map. */
  public static Map<String, Set<String>> relationshipTypesMap = new HashMap<>();

  /** The languages map. */
  public static Map<String, Set<String>> languagesMap = new HashMap<>();

  /** The chd rel. */
  public static RelationshipType chdRel = null;

  /** The attribute names map. */
  public static Map<String, Set<String>> attributeNamesMap = new HashMap<>();

  /**
   * Instantiates an empty {@link UmlsMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public UmlsMetadataServiceJpaHelper() throws Exception {
    super();
    refreshCachesHelper();
  }

  /* see superclass */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "get relationship types - " + terminology + ", " + version);
    // Cache relationship types map
    cacheRelationshipTypes();

    javax.persistence.Query query =
        manager.createQuery("SELECT t from RelationshipTypeJpa t "
            + " where terminology = :terminology "
            + "   and version = :version" + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", relationshipTypesMap.get(terminology + version));

    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    Logger.getLogger(getClass()).info("  results = " + types.getObjects());
    return types;
  }

  /* see superclass */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public LanguageList getLanguages(String terminology, String version)
    throws Exception {
    Logger.getLogger(getClass()).info(
        "get languages - " + terminology + ", " + version);
    // Cache languages map
    cacheAtoms();
    javax.persistence.Query query =
        manager.createQuery("SELECT t from LanguageJpa t "
            + " where terminology = :terminology "
            + "   and version = :version" + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", languagesMap.get(terminology + version));

    LanguageList types = new LanguageListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    Logger.getLogger(getClass()).info("  results = " + types.getObjects());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    // Cache additional relationship types map
    cacheRelationshipTypes();
    javax.persistence.Query query =
        manager.createQuery("SELECT t from AdditionalRelationshipTypeJpa t "
            + " where terminology = :terminology "
            + "   and version = :version" + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list",
        additionalRelationshipTypesMap.get(terminology + version));
    AdditionalRelationshipTypeList types =
        new AdditionalRelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeNameList getAttributeNames(String terminology, String version)
    throws Exception {
    // Cache attribute name map
    cacheAttributeNames();
    javax.persistence.Query query =
        manager.createQuery("SELECT t from AttributeNameJpa t "
            + " where terminology = :terminology "
            + "   and version = :version" + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", attributeNamesMap.get(terminology + version));
    AttributeNameList types = new AttributeNameListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception {
    // Cache term types map
    cacheAtoms();

    javax.persistence.Query query =
        manager.createQuery("SELECT t from TermTypeJpa t "
            + " where terminology = :terminology "
            + "   and version = :version" + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", termTypesMap.get(terminology + version));
    TermTypeList types = new TermTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* see superclass */
  @Override
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager.createQuery("SELECT p from PrecedenceListJpa p"
            + " where defaultList = 1 " + " and terminology = :terminology "
            + " and version = :version");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);

    PrecedenceList defaultList = (PrecedenceList) query.getSingleResult();
    // copy and prune to this terminology/version
    PrecedenceList list = new PrecedenceListJpa(defaultList);
    list.setId(null);

    // Restrict default list to just those ttys matching this terminology
    KeyValuePairList defaultKvpl = list.getPrecedence();
    KeyValuePairList kvpl = new KeyValuePairList();
    for (KeyValuePair pair : defaultKvpl.getKeyValuePairList()) {
      if (pair.getKey().equals(terminology)) {
        kvpl.addKeyValuePair(pair);
      }
    }

    list.setPrecedence(kvpl);
    // return the shorter list
    return list;
  }

  /* see superclass */
  @Override
  public RelationshipTypeList getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    // cache and check for the CHD rel
    if (chdRel == null) {
      RelationshipTypeList list = getRelationshipTypes(terminology, version);
      for (RelationshipType rel : list.getObjects()) {
        if (rel.getAbbreviation().equals("CHD")) {
          chdRel = rel;
          break;
        }
      }
    }
    RelationshipTypeList types = new RelationshipTypeListJpa();
    Logger.getLogger(getClass()).info("  childRel = " + chdRel);
    if (chdRel != null) {
      types.getObjects().add(chdRel);
    }
    return types;
  }

  /* see superclass */
  @Override
  public boolean isHierarchcialRelationship(Relationship<?, ?> relationship) {
    return relationship.getRelationshipType().equals("CHD");
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    umlsTerminology = p.getProperty("terminology");
    umlsVersion = p.getProperty("version");
  }

  /* see superclass */
  @Override
  public void refreshCaches() throws Exception {
    close();
    manager = factory.createEntityManager();
    refreshCachesHelper();
  }

  /**
   * Refresh caches helper.
   *
   * @throws Exception the exception
   */
  private void refreshCachesHelper() throws Exception {
    termTypesMap = new HashMap<>();
    additionalRelationshipTypesMap = new HashMap<>();
    relationshipTypesMap = new HashMap<>();
    languagesMap = new HashMap<>();
    attributeNamesMap = new HashMap<>();

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        cacheAtoms();
      }
    });
    t.start();
    t = new Thread(new Runnable() {
      @Override
      public void run() {
        cacheRelationshipTypes();
        chdRel = null;
      }
    });
    t.start();
    t = new Thread(new Runnable() {
      @Override
      public void run() {
        cacheAtoms();
      }
    });
    t.start();
    t = new Thread(new Runnable() {
      @Override
      public void run() {
        cacheAttributeNames();
      }
    });
    t.start();
    // Wait
    t.join();
  }

  /**
   * Cache relationship types.
   */
  @SuppressWarnings({
      "unchecked", "static-method"
  })
  void cacheRelationshipTypes() {
    if (relationshipTypesMap.isEmpty()) {
      EntityManager manager = factory.createEntityManager();
      javax.persistence.Query query =
          manager.createQuery("select distinct a.terminology, "
              + "a.version, a.relationshipType, "
              + "a.additionalRelationshipType "
              + "from ConceptRelationshipJpa a");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!relationshipTypesMap.containsKey(result[0].toString() + result[1])) {
          relationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        relationshipTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
        if (!additionalRelationshipTypesMap.containsKey(result[0].toString()
            + result[1])) {
          additionalRelationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        additionalRelationshipTypesMap.get(result[0].toString() + result[1])
            .add(result[3].toString());
      }
      query =
          manager.createQuery("select distinct a.terminology, "
              + "a.version, a.relationshipType, "
              + "a.additionalRelationshipType "
              + "from DescriptorRelationshipJpa a");
      results = query.getResultList();
      for (Object[] result : results) {
        if (!relationshipTypesMap.containsKey(result[0].toString() + result[1])) {
          relationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        relationshipTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
        if (!additionalRelationshipTypesMap.containsKey(result[0].toString()
            + result[1])) {
          additionalRelationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        additionalRelationshipTypesMap.get(result[0].toString() + result[1])
            .add(result[3].toString());
      }
      query =
          manager.createQuery("select distinct a.terminology, "
              + "a.version, a.relationshipType, "
              + "a.additionalRelationshipType from CodeRelationshipJpa a");
      results = query.getResultList();
      for (Object[] result : results) {
        if (!relationshipTypesMap.containsKey(result[0].toString() + result[1])) {
          relationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        relationshipTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
        if (!additionalRelationshipTypesMap.containsKey(result[0].toString()
            + result[1])) {
          additionalRelationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        additionalRelationshipTypesMap.get(result[0].toString() + result[1])
            .add(result[3].toString());
      }

      manager.close();
    }
  }

  /**
   * Cache attribute names.
   */
  @SuppressWarnings({
      "unchecked", "static-method"
  })
  void cacheAttributeNames() {
    if (attributeNamesMap.isEmpty()) {
      EntityManager manager = factory.createEntityManager();
      javax.persistence.Query query =
          manager.createQuery("select distinct a.terminology, a.version, "
              + "a.name from AttributeJpa a");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!attributeNamesMap.containsKey(result[0].toString() + result[1])) {
          attributeNamesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        attributeNamesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
      }
      manager.close();
    }
  }

  /**
   * Cache term types.
   */
  @SuppressWarnings({
      "unchecked", "static-method"
  })
  void cacheAtoms() {
    if (termTypesMap.isEmpty()) {
      EntityManager manager = factory.createEntityManager();
      javax.persistence.Query query =
          manager.createQuery("select distinct a.terminology, a.version, "
              + "a.termType, a.language from AtomJpa a");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!termTypesMap.containsKey(result[0].toString() + result[1])) {
          termTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        termTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
        if (!languagesMap.containsKey(result[0].toString() + result[1])) {
          languagesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        languagesMap.get(result[0].toString() + result[1]).add(
            result[3].toString());
      }
      manager.close();
    }
  }
}
