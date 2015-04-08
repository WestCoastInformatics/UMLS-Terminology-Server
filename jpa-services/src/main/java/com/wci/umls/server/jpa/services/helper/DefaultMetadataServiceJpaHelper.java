package com.wci.umls.server.jpa.services.helper;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.services.MetadataService;

/**
 * Implementation of {@link MetadataService} for SNOMEDCT.
 */
public class DefaultMetadataServiceJpaHelper implements
    MetadataService {

  @Override
  public void openFactory() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void closeFactory() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean getTransactionPerOperation() throws Exception {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setTransactionPerOperation(boolean transactionPerOperation)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void commit() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void rollback() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void beginTransaction() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void close() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void clear() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<String> getTerminologies() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getVersions(String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLatestVersion(String terminology) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getTerminologyLatestVersions() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Map<String, String>> getAllMetadata(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getRelationTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getAdditionalRelationTypes(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getAttributeNames(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getIdentifierTypes(String terminology,
    String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getSemanticTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getTermTypes(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getMapSets(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> Subsets(String terminology, String version)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getHierarchicalRelationshipTypes(
    String terminology, String version) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, String> getNonGroupingRelationshipTypes(
    String terminology, String version) throws Exception {
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


}
