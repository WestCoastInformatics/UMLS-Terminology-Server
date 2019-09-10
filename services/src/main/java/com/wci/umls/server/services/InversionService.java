/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;


import com.wci.umls.server.Project;
import com.wci.umls.server.model.inversion.SourceIdRange;

/**
 * Represents a service for accessing {@link SourceIdRange} information.
 */
public interface InversionService extends RootService {


  /**
   * Gets the source id range.
   *
   * @param id the id
   * @return the source id range
   */
  public SourceIdRange getSourceIdRange(Long id);


  /**
   * Add source id range.
   *
   * @param sourceIdRange the source id range
   * @return the source id range
   * @throws Exception the exception
   */
  public SourceIdRange addSourceIdRange(SourceIdRange sourceIdRange) throws Exception;


  /**
   * Update source id range.
   *
   * @param sourceIdRange the source id range
   * @throws Exception the exception
   */
  public void updateSourceIdRange(SourceIdRange sourceIdRange) throws Exception;


  /**
   * Remove source id range.
   *
   * @param sourceIdRangeId the source id range id
   * @throws Exception the exception
   */
  public void removeSourceIdRange(Long sourceIdRangeId) throws Exception;


  /**
   * Gets the source id range.
   *
   * @param project the project
   * @param terminology the terminology
   * @param version the version
   * @return the source id range
   * @throws Exception the exception
   */
  public SourceIdRange getSourceIdRange(Project project, String terminology,
    String version) throws Exception;

}