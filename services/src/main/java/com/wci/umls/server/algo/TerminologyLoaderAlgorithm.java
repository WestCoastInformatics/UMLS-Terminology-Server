package com.wci.umls.server.algo;

import com.wci.umls.server.helpers.HasTerminology;

/**
 * The Interface LoaderAlgorithm.
 */
public interface TerminologyLoaderAlgorithm extends Algorithm, HasTerminology  {

  
  /**
   * Set input path.
   * @param inputPath 
   */
  public void setInputPath(String inputPath);
  
  /**
   * Compute transitive closures.
   * @throws Exception 
   */
  public void computeTransitiveClosures() throws Exception;
  
  /**
   * Compute tree positions.
   * @throws Exception 
   */
  public void computeTreePositions() throws Exception;
  
  /**
   * Gets the input path.
   *
   * @return the input path
   */
  public String getInputPath();

  /**
   * Log info.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logInfo(String message) throws Exception;

  /**
   * Log error.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logError(String message) throws Exception;

  /**
   * Log warn.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logWarn(String message) throws Exception;
}
