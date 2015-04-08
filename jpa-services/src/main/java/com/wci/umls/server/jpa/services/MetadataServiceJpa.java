package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.TermType;
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
