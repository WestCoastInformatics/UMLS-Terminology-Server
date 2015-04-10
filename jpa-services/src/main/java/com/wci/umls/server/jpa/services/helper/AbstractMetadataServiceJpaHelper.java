/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.jpa.services.RootServiceJpa;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.IdentifierType;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.SemanticType;
import com.wci.umls.server.services.MetadataService;

/**
 * Default implementation of {@link MetadataService}.
 */
public abstract class AbstractMetadataServiceJpaHelper extends RootServiceJpa
    implements MetadataService {

  /**
   * Instantiates an empty {@link AbstractMetadataServiceJpaHelper}.
   *
   * @throws Exception the exception
   */
  public AbstractMetadataServiceJpaHelper() throws Exception {
    super();
  }

  //
  // Not needed for sub-handler

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    // n/a handled by superclass
    return null;
  }

  @Override
  public void clearMetadata(String terminology, String version)
    throws Exception {
    // n/a
  }

  @Override
  public void enableListeners() {
    // TODO Auto-generated method stub

  }

  @Override
  public void disableListeners() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isLastModifiedFlag() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setLastModifiedFlag(boolean lastModifiedFlag) {
    // TODO Auto-generated method stub

  }

  @Override
  public SemanticType addSemanticType(SemanticType semanticType)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateSemanticType(SemanticType semanticType) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeSemanticType(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public AttributeName addAttributeName(AttributeName AttributeName)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateAttributeName(AttributeName AttributeName) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeAttributeName(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public IdentifierType addIdentifierType(IdentifierType IdentifierType)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateIdentifierType(IdentifierType IdentifierType)
    throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public void removeIdentifierType(Long id) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public Language addLanguage(Language Language) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void updateLanguage(Language Language) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void removeLanguage(Long id) throws Exception {
    // TODO Auto-generated method stub
    
  }
}
