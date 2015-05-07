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

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.meta.AdditionalRelationshipTypeList;
import com.wci.umls.server.helpers.meta.AttributeNameList;
import com.wci.umls.server.helpers.meta.RelationshipTypeList;
import com.wci.umls.server.helpers.meta.TermTypeList;
import com.wci.umls.server.jpa.helpers.PrecedenceListJpa;
import com.wci.umls.server.jpa.helpers.meta.AdditionalRelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.AttributeNameListJpa;
import com.wci.umls.server.jpa.helpers.meta.RelationshipTypeListJpa;
import com.wci.umls.server.jpa.helpers.meta.TermTypeListJpa;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.TermType;
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
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getRelationshipTypes(java.
   * lang.String, java.lang.String)
   */
  @SuppressWarnings({
    "unchecked"
  })
  @Override
  public RelationshipTypeList getRelationshipTypes(String terminology,
    String version) throws Exception {
    Logger.getLogger(getClass()).info(
        "get relationship types - " + terminology + ", " + version);
    // Cache relationship types map
    if (relationshipTypesMap.isEmpty()) {
      Logger.getLogger(getClass()).info(
          "  rel types map is empty");
      javax.persistence.Query query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from ConceptRelationshipJpa a, RelationshipTypeJpa t "
                  + "where a.relationshipType = t.abbreviation");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!relationshipTypesMap.containsKey(result[0].toString() + result[1])) {
          relationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        Logger.getLogger(getClass()).info(
            "  add concept rel type: " + result[0].toString() + result[1] + ","
                + result[2]);
        relationshipTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
      }
      query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from DescriptorRelationshipJpa a, RelationshipTypeJpa t "
                  + "where a.relationshipType = t.abbreviation");
      results = query.getResultList();
      for (Object[] result : results) {
        if (!relationshipTypesMap.containsKey(result[0].toString() + result[1])) {
          relationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        Logger.getLogger(getClass()).info(
            "  add descriptor rel type: " + result[0].toString() + result[1]
                + "," + result[2]);
        relationshipTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
      }
      query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from CodeRelationshipJpa a, RelationshipTypeJpa t "
                  + "where a.relationshipType = t.abbreviation");
      results = query.getResultList();
      for (Object[] result : results) {
        if (!relationshipTypesMap.containsKey(result[0].toString() + result[1])) {
          relationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        Logger.getLogger(getClass()).info(
            "  add code rel type: " + result[0].toString() + result[1] + ","
                + result[2]);
        relationshipTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
      }

    } else {
      Logger.getLogger(getClass()).info(
          "  rel types map is NOT empty");

    }
    javax.persistence.Query query =
        manager.createQuery("SELECT t from RelationshipTypeJpa t "
            + " where terminology = :terminology "
            + "   and terminologyVersion = :version"
            + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", relationshipTypesMap.get(terminology + version));
    Logger.getLogger(getClass()).info(
        "  " + umlsTerminology + umlsVersion + ", "
            + relationshipTypesMap.get(terminology + version));
    RelationshipTypeList types = new RelationshipTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    Logger.getLogger(getClass()).info("  results = " + types.getObjects());
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAdditionalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AdditionalRelationshipTypeList getAdditionalRelationshipTypes(
    String terminology, String version) throws Exception {
    // Cache additional relationship types map
    if (additionalRelationshipTypesMap.isEmpty()) {
      javax.persistence.Query query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from ConceptRelationshipJpa a, AdditionalRelationshipTypeJpa t "
                  + "where a.additionalRelationshipType = t.abbreviation");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!additionalRelationshipTypesMap.containsKey(result[0].toString()
            + result[1])) {
          additionalRelationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        additionalRelationshipTypesMap.get(result[0].toString() + result[1])
            .add(result[2].toString());
      }
      query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from DescriptorRelationshipJpa a, AdditionalRelationshipTypeJpa t "
                  + "where a.additionalRelationshipType = t.abbreviation");
      results = query.getResultList();
      for (Object[] result : results) {
        if (!additionalRelationshipTypesMap.containsKey(result[0].toString()
            + result[1])) {
          additionalRelationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        additionalRelationshipTypesMap.get(result[0].toString() + result[1])
            .add(result[2].toString());
      }
      query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from CodeRelationshipJpa a, AdditionalRelationshipTypeJpa t "
                  + "where a.additionalRelationshipType = t.abbreviation");
      results = query.getResultList();
      for (Object[] result : results) {
        if (!additionalRelationshipTypesMap.containsKey(result[0].toString()
            + result[1])) {
          additionalRelationshipTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        additionalRelationshipTypesMap.get(result[0].toString() + result[1])
            .add(result[2].toString());
      }

    }
    javax.persistence.Query query =
        manager.createQuery("SELECT t from AdditionalRelationshipTypeJpa t "
            + " where terminology = :terminology "
            + "   and terminologyVersion = :version"
            + "   and abbreviation in (:list)");
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getAttributeNames(java.lang
   * .String, java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public AttributeNameList getAttributeNames(String terminology, String version)
    throws Exception {
    // Cache attribute name map
    if (attributeNamesMap.isEmpty()) {
      javax.persistence.Query query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from AttributeJpa a, AttributeNameJpa t "
                  + "where a.name = t.abbreviation");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!attributeNamesMap.containsKey(result[0].toString() + result[1])) {
          attributeNamesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        attributeNamesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
      }
    }
    javax.persistence.Query query =
        manager.createQuery("SELECT t from AttributeNameJpa t "
            + " where terminology = :terminology "
            + "   and terminologyVersion = :version"
            + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", attributeNamesMap.get(terminology + version));
    AttributeNameList types = new AttributeNameListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getTermTypes(java.lang.String,
   * java.lang.String)
   */
  @SuppressWarnings("unchecked")
  @Override
  public TermTypeList getTermTypes(String terminology, String version)
    throws Exception {
    // Cache term types map
    if (termTypesMap.isEmpty()) {
      javax.persistence.Query query =
          manager
              .createQuery("select distinct a.terminology, a.terminologyVersion, t.abbreviation "
                  + "from AtomJpa a, TermTypeJpa t "
                  + "where a.termType = t.abbreviation");
      List<Object[]> results = query.getResultList();
      for (Object[] result : results) {
        if (!termTypesMap.containsKey(result[0].toString() + result[1])) {
          termTypesMap.put(result[0].toString() + result[1],
              new HashSet<String>());
        }
        termTypesMap.get(result[0].toString() + result[1]).add(
            result[2].toString());
      }
    }
    javax.persistence.Query query =
        manager.createQuery("SELECT t from TermTypeJpa t "
            + " where terminology = :terminology "
            + "   and terminologyVersion = :version"
            + "   and abbreviation in (:list)");
    query.setParameter("terminology", umlsTerminology);
    query.setParameter("version", umlsVersion);
    query.setParameter("list", termTypesMap.get(terminology + version));
    TermTypeList types = new TermTypeListJpa();
    types.setObjects(query.getResultList());
    types.setTotalCount(types.getObjects().size());
    return types;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.jpa.services.helper.StandardMetadataServiceJpaHelper#getDefaultPrecedenceList(java.lang.String, java.lang.String)
   */
  @Override
  public PrecedenceList getDefaultPrecedenceList(String terminology,
    String version) throws Exception {
    javax.persistence.Query query =
        manager
            .createQuery("SELECT p from PrecedenceListJpa p"
                + " where defaultList = 1");

    PrecedenceList defaultList = (PrecedenceList) query.getSingleResult();
    // copy and prune to this terminology/version
    PrecedenceList list = new PrecedenceListJpa(defaultList);
    list.setId(null);

    // Get TTY values for this terminology/version
    TermTypeList ttyList = getTermTypes(terminology,version);
    Set<String> ttySet = new HashSet<>();
    for (TermType tty : ttyList.getObjects()) {
      ttySet.add(tty.getAbbreviation());
    }

    // Restrict default list to just those ttys matching this terminology
    KeyValuePairList defaultKvpl = list.getPrecedence();
    KeyValuePairList kvpl = new KeyValuePairList();
    for (KeyValuePair pair : defaultKvpl.getKeyValuePairList()) {
      if (ttySet.contains(pair.getKey())) {
        kvpl.addKeyValuePair(pair);
      }
    }

    // return the shorter list
    return list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#getHierarchicalRelationshipTypes
   * (java.lang.String, java.lang.String)
   */
  @Override
  public RelationshipTypeList getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    Logger.getLogger(getClass()).info("  childRel = " + chdRel);
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

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#isHierarchcialRelationship
   * (com.wci.umls.server.model.content.Relationship)
   */
  @Override
  public boolean isHierarchcialRelationship(Relationship<?, ?> relationship) {
    return relationship.getRelationshipType().equals("CHD");
  }

  @Override
  public void setProperties(Properties p) {
    umlsTerminology = p.getProperty("terminology");
    umlsVersion = p.getProperty("version");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.jpa.services.helper.StandardMetadataServiceJpaHelper
   * #refreshCaches()
   */
  @Override
  public void refreshCaches() throws Exception {
    termTypesMap = new HashMap<>();
    additionalRelationshipTypesMap = new HashMap<>();
    relationshipTypesMap = new HashMap<>();
    chdRel = null;
  }
}
