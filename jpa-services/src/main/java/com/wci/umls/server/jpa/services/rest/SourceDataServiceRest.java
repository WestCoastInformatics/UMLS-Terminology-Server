/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.SourceDataFile;
import com.wci.umls.server.helpers.KeyValuePairList;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SourceDataFileList;
import com.wci.umls.server.helpers.SourceDataList;
import com.wci.umls.server.jpa.SourceDataFileJpa;
import com.wci.umls.server.jpa.SourceDataJpa;

/**
 * Represents a security available via a REST service.
 */
public interface SourceDataServiceRest {

  /**
   * Removes the source data file.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeSourceDataFile(Long id, String authToken) throws Exception;

  /**
   * Find source data files for query.
   *
   * @param query the query
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the source data file list
   * @throws Exception the exception
   */
  public SourceDataFileList findSourceDataFilesForQuery(String query,
    PfsParameter pfsParameter, String authToken) throws Exception;

  /**
   * Adds the source data file.
   *
   * @param sourceDataFile the source data file
   * @param authToken the auth token
   * @return the source data file
   * @throws Exception the exception
   */
  public SourceDataFile addSourceDataFile(SourceDataFileJpa sourceDataFile,
    String authToken) throws Exception;

  /**
   * Update source data file.
   *
   * @param sourceDataFile the source data file
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateSourceDataFile(SourceDataFileJpa sourceDataFile,
    String authToken) throws Exception;

  /**
   * Removes the source data.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeSourceData(Long id, String authToken) throws Exception;

  /**
   * Find source data source data objects for query.
   *
   * @param query the query
   * @param pfsParameter the pfs parameter
   * @param authToken the auth token
   * @return the source data source data list
   * @throws Exception the exception
   */
  public SourceDataList findSourceDataForQuery(String query,
    PfsParameter pfsParameter, String authToken) throws Exception;

  /**
   * Save source data.
   *
   * @param sourceData the source data
   * @param authToken the auth token
   * @return the string
   * @throws Exception the exception
   */
  public SourceData addSourceData(SourceDataJpa sourceData, String authToken)
    throws Exception;

  /**
   * Update source data.
   *
   * @param sourceData the source data
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateSourceData(SourceDataJpa sourceData, String authToken)
    throws Exception;

  /**
   * Gets the source data handler names.
   *
   * @param authToken the auth token
   * @return the source data handler names
   * @throws Exception the exception
   */
  public KeyValuePairList getSourceDataHandlerNames(String authToken)
    throws Exception;

  /**
   * Load from source data.
   *
   * @param sourceData the source data
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void loadFromSourceData(SourceDataJpa sourceData, String authToken)
    throws Exception;

  /**
   * Gets the source data.
   *
   * @param id the id
   * @param authToken the auth token
   * @return the source data
   * @throws Exception the exception
   */
  public SourceData getSourceData(Long id, String authToken) throws Exception;

  /**
   * Upload source data file.
   *
   * @param fileInputStream the file input stream
   * @param contentDispositionHeader the content disposition header
   * @param unzip the unzip
   * @param sourceDataId the source data id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  
  public void uploadSourceDataFile(InputStream fileInputStream,
    FormDataContentDisposition contentDispositionHeader, boolean unzip,
    Long sourceDataId, String authToken) throws Exception;

  /**
   * Remove from source data.
   *
   * @param sourceData the source data
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeFromSourceData(SourceDataJpa sourceData, String authToken)
    throws Exception;
}