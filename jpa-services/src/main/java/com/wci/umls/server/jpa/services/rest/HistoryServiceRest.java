/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.rest;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.ReleaseInfoList;
import com.wci.umls.server.jpa.ReleaseInfoJpa;

/**
 * Represents a history services available via a REST service.
 */
public interface HistoryServiceRest {

  /**
   * Returns the release history.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @return the release history
   * @throws Exception the exception
   */
  public ReleaseInfoList getReleaseHistory(String terminology, String authToken)
    throws Exception;

  /**
   * Returns the current release info.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @return the current release info
   * @throws Exception the exception
   */
  public ReleaseInfo getCurrentReleaseInfo(String terminology, String authToken)
    throws Exception;

  /**
   * Returns the previous release info.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @return the previous release info
   * @throws Exception the exception
   */
  public ReleaseInfo getPreviousReleaseInfo(String terminology, String authToken)
    throws Exception;

  /**
   * Gets the planned release info.
   *
   * @param terminology the terminology
   * @param authToken the auth token
   * @return the planned release info
   * @throws Exception the exception
   */
  public ReleaseInfo getPlannedReleaseInfo(String terminology, String authToken)
    throws Exception;

  /**
   * Returns the release info.
   *
   * @param terminology the terminology
   * @param name the name
   * @param authToken the auth token
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo getReleaseInfo(String terminology, String name,
    String authToken) throws Exception;

  /**
   * Adds the release info.
   *
   * @param releaseInfo the release info
   * @param authToken the auth token
   * @return the release info
   * @throws Exception the exception
   */
  public ReleaseInfo addReleaseInfo(ReleaseInfoJpa releaseInfo, String authToken)
    throws Exception;

  /**
   * Updates release info.
   *
   * @param releaseInfo the release info
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void updateReleaseInfo(ReleaseInfoJpa releaseInfo, String authToken)
    throws Exception;

  /**
   * Removes the release info.
   *
   * @param id the id
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void removeReleaseInfo(Long id, String authToken) throws Exception;

  /**
   * Start editing cycle for a release.
   *
   * @param releaseVersion the release version
   * @param terminology the terminology
   * @param version the terminology version
   * @param authToken the auth token
   * @throws Exception the exception
   */
  public void startEditingCycle(String releaseVersion, String terminology,
    String version, String authToken) throws Exception;

}
