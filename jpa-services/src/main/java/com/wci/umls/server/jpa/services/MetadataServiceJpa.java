/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.management.relation.RelationType;
import javax.persistence.metamodel.EntityType;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.meta.AbstractAbbreviation;
import com.wci.umls.server.jpa.meta.AttributeNameJpa;
import com.wci.umls.server.jpa.meta.IdentifierTypeJpa;
import com.wci.umls.server.jpa.meta.LanguageJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeJpa;
import com.wci.umls.server.model.meta.Abbreviation;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.IdentifierType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * Implementation of {@link MetadataService} that redirects to
 * terminology-specific implemlentations.
 */
public class MetadataServiceJpa extends RootServiceJpa implements
    MetadataService {

  /** The config properties. */
  protected static Properties config = null;

  /** The listeners enabled. */
  protected boolean listenersEnabled = true;

  /** The last modified flag. */
  protected boolean lastModifiedFlag = false;

  /** The listener. */
  protected static List<WorkflowListener> listeners = null;
  static {
    listeners = new ArrayList<>();
    try {
      if (config == null)
        config = ConfigUtility.getConfigProperties();
      String key = "workflow.listener.handler";
      for (String handlerName : config.getProperty(key).split(",")) {
        if (handlerName.isEmpty())
          continue;
        // Add handlers to map
        WorkflowListener handlerService =
            ConfigUtility.newStandardHandlerInstanceWithConfiguration(key,
                handlerName, WorkflowListener.class);
        listeners.add(handlerService);
      }

    } catch (Exception e) {
      e.printStackTrace();
      listeners = null;
    }
  }

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
    if (listeners == null) {
      throw new Exception(
          "Listeners did not properly initialize, serious error.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#enableListeners()
   */
  @Override
  public void enableListeners() {
    listenersEnabled = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.MetadataService#disableListeners()
   */
  @Override
  public void disableListeners() {
    listenersEnabled = false;
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
        Logger.getLogger(getClass()).info("  Remove " + jpaTable);
        javax.persistence.Query query = null;

        // Handle case of no terminology version
        if (RootTerminology.class.isAssignableFrom(type.getBindableJavaType())) {
          query =
              manager.createQuery("DELETE FROM " + jpaTable
                  + " WHERE terminology = :terminology ");
          query.setParameter("terminology", terminology);
        }

        // Handle case of terminology version
        else {
          query =
              manager.createQuery("DELETE FROM " + jpaTable
                  + " WHERE terminology = :terminology "
                  + " AND terminologyVersion = :version");
          query.setParameter("terminology", terminology);
          query.setParameter("version", version);
        }
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

  @Override
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add semanticType " + semanticType.getValue());

    // Add component
    SemanticType newSemanticType = addAbbreviation(semanticType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newSemanticType;
  }

  @Override
  public void updateSemanticType(SemanticType semanticType) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update semantic type " + semanticType.getValue());
    updateAbbreviation(semanticType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public void removeSemanticType(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove semantic type " + id);
    // Remove the component
    removeAbbreviation(id, SemanticTypeJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public AttributeName addAttributeName(AttributeName attributeName)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add attributeName "
            + attributeName.getAbbreviation());

    // Add component
    AttributeName newAttributeName = addAbbreviation(attributeName);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newAttributeName;
  }

  @Override
  public void updateAttributeName(AttributeName attributeName) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update attributeName "
            + attributeName.getAbbreviation());
    updateAbbreviation(attributeName);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public void removeAttributeName(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove attributeName " + id);
    // Remove the component
    removeAbbreviation(id, AttributeNameJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public IdentifierType addIdentifierType(IdentifierType identifierType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add identifierType "
            + identifierType.getAbbreviation());

    // Add component
    IdentifierType newIdentifierType = addAbbreviation(identifierType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newIdentifierType;
  }

  @Override
  public void updateIdentifierType(IdentifierType identifierType)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update identifierType "
            + identifierType.getAbbreviation());
    updateAbbreviation(identifierType);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public void removeIdentifierType(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove identifierType" + id);
    // Remove the component
    removeAbbreviation(id, IdentifierTypeJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public Language addLanguage(Language language) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - add language " + language.getAbbreviation());

    // Add component
    Language newLanguage = addAbbreviation(language);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
    return newLanguage;
  }

  @Override
  public void updateLanguage(Language language) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - update language " + language.getAbbreviation());
    updateAbbreviation(language);

    // Inform listeners
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  @Override
  public void removeLanguage(Long id) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Metadata Service - remove language" + id);
    // Remove the component
    removeAbbreviation(id, LanguageJpa.class);
    if (listenersEnabled) {
      for (WorkflowListener listener : listeners) {
        listener.metadataChanged();
      }
    }
  }

  /**
   * Adds the abbreviation.
   *
   * @param <T> the
   * @param abbreviation the component
   * @return the t
   * @throws Exception the exception
   */
  private <T extends Abbreviation> T addAbbreviation(T abbreviation)
    throws Exception {
    try {
      // Set last modified date
      if (lastModifiedFlag) {
        abbreviation.setLastModified(new Date());
      }

      // add
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.persist(abbreviation);
        tx.commit();
      } else {
        manager.persist(abbreviation);
      }
      return abbreviation;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  /**
   * Update abbreviation.
   *
   * @param <T> the generic type
   * @param abbreviation the abbreviation
   * @throws Exception the exception
   */
  private <T extends Abbreviation> void updateAbbreviation(T abbreviation)
    throws Exception {
    try {
      // Set modification date
      if (lastModifiedFlag) {
        abbreviation.setLastModified(new Date());
      }

      // update
      if (getTransactionPerOperation()) {
        tx = manager.getTransaction();
        tx.begin();
        manager.merge(abbreviation);
        tx.commit();
      } else {
        manager.merge(abbreviation);
      }
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }

  }

  /**
   * Removes the abbreviation.
   *
   * @param <T> the generic type
   * @param id the id
   * @param clazz the clazz
   * @return the abbreviation
   * @throws Exception the exception
   */
  private <T extends Abbreviation> T removeAbbreviation(Long id, Class<T> clazz)
    throws Exception {
    try {
      // Get transaction and object
      tx = manager.getTransaction();
      T abbreviation = manager.find(clazz, id);

      // Set modification date
      if (lastModifiedFlag) {
        abbreviation.setLastModified(new Date());
      }

      // Remove
      if (getTransactionPerOperation()) {
        // remove refset member
        tx.begin();
        if (manager.contains(abbreviation)) {
          manager.remove(abbreviation);
        } else {
          manager.remove(manager.merge(abbreviation));
        }
        tx.commit();
      } else {
        if (manager.contains(abbreviation)) {
          manager.remove(abbreviation);
        } else {
          manager.remove(manager.merge(abbreviation));
        }
      }
      return abbreviation;
    } catch (Exception e) {
      if (tx.isActive()) {
        tx.rollback();
      }
      throw e;
    }
  }

  @Override
  public boolean isLastModifiedFlag() {
    return lastModifiedFlag;
  }

  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    this.lastModifiedFlag = lastModifiedFlag;
  }

}
