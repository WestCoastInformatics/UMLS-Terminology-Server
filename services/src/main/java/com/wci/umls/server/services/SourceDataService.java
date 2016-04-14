/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services;

import java.util.Map;

import com.wci.umls.server.Project;
import com.wci.umls.server.SourceData;
import com.wci.umls.server.SourceDataFile;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SourceDataFileList;
import com.wci.umls.server.helpers.SourceDataList;

/**
 * Generically represents a service for accessing {@link Project} information.
 */
public interface SourceDataService extends RootService {

  /**
   * Gets the source data file.
   *
   * @param sourceDataFileId the source data file id
   * @return the source data file
   * @throws Exception the exception
   */
  public SourceDataFile getSourceDataFile(Long sourceDataFileId)
    throws Exception;

  /**
   * Adds the source data file.
   *
   * @param sourceDataFile the source data file
   * @return the source data file
   * @throws Exception the exception
   */
  public SourceDataFile addSourceDataFile(SourceDataFile sourceDataFile)
    throws Exception;

  /**
   * Update source data file.
   *
   * @param sourceDataFile the source data file
   * @throws Exception the exception
   */
  public void updateSourceDataFile(SourceDataFile sourceDataFile)
    throws Exception;

  /**
   * Removes the source data file.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSourceDataFile(Long id) throws Exception;

  /**
   * Find source data files for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the source data file list
   * @throws Exception the exception
   */
  public SourceDataFileList findSourceDataFilesForQuery(String query,
    PfsParameter pfs) throws Exception;

  /**
   * Gets the source data files.
   *
   * @return the source data files
   */
  public SourceDataFileList getSourceDataFiles();

  /**
   * Gets the source data.
   *
   * @param sourceDataId the source data id
   * @return the source data
   * @throws Exception the exception
   */
  public SourceData getSourceData(Long sourceDataId) throws Exception;

  /**
   * Adds the source data.
   *
   * @param sourceData the source data
   * @return the source data
   * @throws Exception the exception
   */
  public SourceData addSourceData(SourceData sourceData) throws Exception;

  /**
   * Update source data.
   *
   * @param sourceData the source data
   * @throws Exception the exception
   */
  public void updateSourceData(SourceData sourceData) throws Exception;

  /**
   * Removes the source data.
   *
   * @param id the id
   * @throws Exception the exception
   */
  public void removeSourceData(Long id) throws Exception;

  /**
   * Find source datas for query.
   *
   * @param query the query
   * @param pfs the pfs
   * @return the source data list
   * @throws Exception the exception
   */
  public SourceDataList findSourceDatasForQuery(String query, PfsParameter pfs)
    throws Exception;



  /**
   * Gets the handler names.
   *
   * @return the handler names
   * @throws Exception the exception
   */
  public KeyValuePairList getSourceDataHandlerNames() throws Exception;

  /**
   * Register source data loader.
   *
   * @param id the id
   * @param algorithm the algorithm
   */
  public void registerSourceDataAlgorithm(Long id, Algorithm algorithm);

  /**
   * Unregister source data loader.
   *
   * @param id the id
   */
  public void unregisterSourceDataAlgorithm(Long id);

  /**
   * Gets the running processes.
   *
   * @return the running processes
   */
  public Map<Long, Algorithm> getRunningProcesses();

  /**
   * Gets the running process for id.
   *
   * @param id the id
   * @return the running process for id
   */
  public Algorithm getRunningProcessForId(Long id);

  /**
   * Gets the source datas.
   *
   * @return the source datas
   */
  public SourceDataList getSourceDatas();
}