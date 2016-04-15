/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.SourceDataFile;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SourceDataFileList;
import com.wci.umls.server.helpers.SourceDataList;
import com.wci.umls.server.jpa.SourceDataFileJpa;
import com.wci.umls.server.jpa.SourceDataJpa;
import com.wci.umls.server.jpa.helpers.SourceDataFileListJpa;
import com.wci.umls.server.jpa.helpers.SourceDataListJpa;
import com.wci.umls.server.jpa.helpers.content.SubsetListJpa;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;

/**
 * Reference implementation of the {@link SecurityService}.
 */
public class SourceDataServiceJpa extends RootServiceJpa
    implements SourceDataService {
  
  
  /**  The active process map. */
  private static Map<Long, Algorithm> algorithmsRuning = new HashMap<>();

  /**
   * Instantiates a new source data service jpa.
   *
   * @throws Exception the exception
   */
  public SourceDataServiceJpa() throws Exception {
    super();
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#getSourceData(java.lang.Long)
   */
  /* see superclass */
  @Override
  public SourceData getSourceData(Long sourceDataId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Source Data Service - get source data " + sourceDataId);
    return getHasLastModified(sourceDataId, SourceDataJpa.class);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#addSourceData(com.wci.umls.server.SourceData)
   */
  /* see superclass */
  @Override
  public SourceData addSourceData(SourceData sourceData) throws Exception {
    Logger.getLogger(getClass())
        .debug("Source Data Service - add source data " + sourceData.getName());
    addHasLastModified(sourceData);

    return sourceData;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#updateSourceData(com.wci.umls.server.SourceData)
   */
  /* see superclass */
  @Override
  public void updateSourceData(SourceData sourceData) throws Exception {
    Logger.getLogger(getClass()).debug(
        "Source Data Service - update source data " + sourceData.getName());
    updateHasLastModified(sourceData);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#removeSourceData(java.lang.Long)
   */
  /* see superclass */
  @Override
  public void removeSourceData(Long sourceDataId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Source Data Service - remove source data " + sourceDataId);
    removeHasLastModified(sourceDataId, SourceDataJpa.class);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#findSourceDatasForQuery(java.lang.String, com.wci.umls.server.helpers.PfsParameter)
   */
  /* see superclass */
  @Override
  public SourceDataList findSourceDatasForQuery(String query, PfsParameter pfs)
    throws Exception {
    Logger.getLogger(getClass())
        .info("SourceData Service - find searchDatas " + query);

    int[] totalCt = new int[1];
    @SuppressWarnings("unchecked")
    List<SourceData> list = (List<SourceData>) getQueryResults(
        query == null || query.isEmpty() ? "id:[* TO *]" : query,
        SourceDataJpa.class, SourceDataJpa.class, pfs, totalCt);
    SourceDataList result = new SourceDataListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
  
    return result;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#getSourceDataFiles()
   */
  /* see superclass */
  @Override
  @SuppressWarnings("unchecked")
  public SourceDataFileList getSourceDataFiles() {
    Logger.getLogger(getClass())
        .debug("SourceData Service - get sourceDataFiles");
    javax.persistence.Query query =
        manager.createQuery("select a from SourceDataFileJpa a");
    try {
      List<SourceDataFile> sourceDataFiles = query.getResultList();
      SourceDataFileList sourceDataFileList = new SourceDataFileListJpa();
      sourceDataFileList.setObjects(sourceDataFiles);
      sourceDataFileList.setTotalCount(sourceDataFileList.getCount());
     
      return sourceDataFileList;
    } catch (NoResultException e) {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#getSourceDataFile(java.lang.Long)
   */
  /* see superclass */
  @Override
  public SourceDataFile getSourceDataFile(Long sourceDataFileId)
    throws Exception {
    Logger.getLogger(getClass()).debug(
        "Source Data Service - get source data file " + sourceDataFileId);
    return getHasLastModified(sourceDataFileId, SourceDataFileJpa.class);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#addSourceDataFile(com.wci.umls.server.SourceDataFile)
   */
  /* see superclass */
  @Override
  public SourceDataFile addSourceDataFile(SourceDataFile sourceDataFile)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Source Data Service - add source data file "
            + sourceDataFile.getName());
    return addHasLastModified(sourceDataFile);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#updateSourceDataFile(com.wci.umls.server.SourceDataFile)
   */
  /* see superclass */
  @Override
  public void updateSourceDataFile(SourceDataFile sourceDataFile)
    throws Exception {
    Logger.getLogger(getClass())
        .debug("Source Data Service - update source data file "
            + sourceDataFile.getName());
    updateHasLastModified(sourceDataFile);

  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#removeSourceDataFile(java.lang.Long)
   */
  /* see superclass */
  @Override
  public void removeSourceDataFile(Long sourceDataFileId) throws Exception {
    Logger.getLogger(getClass())
        .debug("Source Data Service - remove source data " + sourceDataFileId);
    removeHasLastModified(sourceDataFileId, SourceDataFileJpa.class);
  }

  /**
   * Find source data files for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the source data file list
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public SourceDataFileList findSourceDataFilesForQuery(String query,
    PfsParameter pfs) throws Exception {
    Logger.getLogger(getClass())
        .info("SourceDataFile Service - find searchDataFiles " + query);

    int[] totalCt = new int[1];
    @SuppressWarnings("unchecked")
    List<SourceDataFile> list = (List<SourceDataFile>) getQueryResults(
        query == null || query.isEmpty() ? "id:[* TO *]" : query,
        SourceDataFileJpa.class, SourceDataFileJpa.class, pfs, totalCt);
    SourceDataFileList result = new SourceDataFileListJpa();
    result.setTotalCount(totalCt[0]);
    result.setObjects(list);
   
    return result;
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#getSourceDataHandlerNames()
   */
  @Override
  // TODO Make this a static array initialized on name
  // TODO Change method name to reflect what it actually does
  public KeyValuePairList getSourceDataHandlerNames() throws Exception {
    KeyValuePairList keyValuePairList = new KeyValuePairList();

    String handlerNames =
        ConfigUtility.getConfigProperties().getProperty("source.data.handler");

    if (handlerNames == null || handlerNames.split(",").length == 0) {
      Logger.getLogger(getClass()).warn(
          "No source data handlers specified in config file (source.data.handler = "
              + handlerNames + ")");
      return keyValuePairList;
    }

    for (String handlerName : handlerNames.split(",")) {
      String handlerClassName = ConfigUtility.getConfigProperties()
          .getProperty("source.data.handler." + handlerName + ".class");
      if (handlerClassName == null) {
        Logger.getLogger(getClass()).warn("Source data handler " + handlerName
            + " has no class specified in config file");
      } else {

        SourceDataHandler handler = null;
        try {
          Class<?> handlerClass = Class.forName(handlerClassName);
          handler = (SourceDataHandler) handlerClass.newInstance();

        } catch (Exception e) {
          throw new Exception(handlerClassName + " could not be instantiated");
        }
        if (handler != null) {
          KeyValuePair keyValuePair = new KeyValuePair();
          keyValuePair.setKey(handler.getName());
          keyValuePair.setValue(handlerClassName);
          keyValuePairList.addKeyValuePair(keyValuePair);
        }
      }
    }
    return keyValuePairList;
  }
  

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#registerSourceDataLoader(java.lang.Long, com.wci.umls.server.algo.Algorithm)
   */
  @Override
  public void registerSourceDataAlgorithm(Long id, Algorithm algorithm) {
    SourceDataServiceJpa.algorithmsRuning.put(id,  algorithm);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.SourceDataService#unregisterSourceDataLoader(java.lang.Long)
   */
  @Override
  public void unregisterSourceDataAlgorithm(Long id) {
    SourceDataServiceJpa.algorithmsRuning.remove(id);
  }
  
  @Override
  public Map<Long, Algorithm> getRunningProcesses() {
    return SourceDataServiceJpa.algorithmsRuning;
  }
  
  @Override
  public Algorithm getRunningProcessForId(Long id) {
    return SourceDataServiceJpa.algorithmsRuning.get(id);
  }

  @Override
  public SourceDataList getSourceDatas() {
    Logger.getLogger(getClass()).debug(
        "Source Data Service - get all source datas");
    javax.persistence.Query query =
        manager.createQuery("select a from SourceDataJpa a");

    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      
      @SuppressWarnings("unchecked")
      List<SourceData> sds = query.getResultList();
      // lazy initialization
      for (SourceData sd : sds) {
        sd.getSourceDataFiles().size();
      }
      SourceDataListJpa sourceDataList = new SourceDataListJpa();
      sourceDataList.setObjects(sds);
      sourceDataList.setTotalCount(sds.size());
      return sourceDataList;

    } catch (NoResultException e) {
      return null;
    }
  }
}
