package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.relation.RelationType;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdentifierType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;

/**
 * Implementation of {@link MetadataService} that redirects to
 * terminology-specific implemlentations.
 */
public class MetadataServiceJpa extends RootServiceJpa implements
    MetadataService {

  /** The helper map. */
  private static Map<String, MetadataService> helperMap = null;
  static {
    helperMap = new HashMap<>();
    Properties config;
    try {
      config = ConfigUtility.getConfigProperties();
      String key = "metadata.service.handler";
      for (String handlerName : config.getProperty(key).split(",")) {

        // Add handlers to map
        MetadataService handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, MetadataService.class);
        helperMap.put(handlerName, handlerService);
      }
    } catch (Exception e) {
      e.printStackTrace();
      helperMap = null;
    }
  }

  /**
   * Instantiates an empty {@link MetadataServiceJpa}.
   *
   * @throws Exception the exception
   */
  public MetadataServiceJpa() throws Exception {
    super();

    if (helperMap == null) {
      throw new Exception("Helper map not properly initialized, serious error.");
    }
  }

  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public List<GeneralMetadataEntry> getGeneralMetadataEntries(
    String terminology, String version) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TermType> getTermTypePrecedenceList(String terminology,
    String version) {
    // TODO Auto-generated method stub
    return null;    
  }
  

  @Override
  public List<RootTerminology> getTerminologies() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Terminology> getVersions(String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Terminology getLatestVersion(String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<RootTerminology, Terminology> getTerminologyLatestVersions()
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RelationType> getRelationTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<AdditionalRelationshipType> getAdditionalRelationTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<AttributeName> getAttributeNames(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IdentifierType> getIdentifierTypes(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<SemanticType> getSemanticTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<TermType> getTermTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RelationshipType> getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<RelationshipType> getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.MetadataService#clearMetadata(java.lang.String
   * , java.lang.String)
   */
  @Override
  public void clearMetadata(String terminology, String version)
    throws Exception {
    try {
      if (getTransactionPerOperation()) {
        // remove simple ref set member
        tx.begin();
      }

      for (EntityType<?> type : manager.getMetamodel().getEntities()) {
        String jpaTable = type.getName();
        // Skip audit trail tables
        if (jpaTable.endsWith("_AUD")) {
          continue;
        }
        // remove all abstract abbreviations
        if (!AbstractAbbreviation.class.isAssignableFrom(type
            .getBindableJavaType())
            && !Terminology.class.isAssignableFrom(type.getBindableJavaType())
            && !RootTerminology.class.isAssignableFrom(type
                .getBindableJavaType())) {
          continue;
        }
        Logger.getLogger(getClass()).info("  Remove " + type);
        javax.persistence.Query query =
            manager.createQuery("DELETE FROM " + jpaTable
                + " WHERE terminology = :terminology "
                + " AND terminologyVersion = :version");
        query.setParameter("terminology", terminology);
        query.setParameter("version", version);
        int deleteRecords = query.executeUpdate();
        Logger.getLogger(getClass()).info(
            "    " + jpaTable + " records deleted: " + deleteRecords);

      }

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


}
