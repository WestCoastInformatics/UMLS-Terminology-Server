package com.wci.umls.server.algo;

import com.wci.umls.server.helpers.HasTerminology;

/**
 * Represents an algorithm for loading a terminology.
 */
public interface TerminologyLoaderAlgorithm extends Algorithm, HasTerminology {

  /**
   * Set input path.
   *
   * @param inputPath the input path
   */
  public void setInputPath(String inputPath);

  /**
   * Gets the input path.
   *
   * @return the input path
   */
  public String getInputPath();

  /**
   * Indicates whether file sorting should take place, in case this is done
   * externally.
   *
   * @param sortFiles the new sort files
   */
  public void setSortFiles(boolean sortFiles);

  /**
   * Gets the file version. Forces each loader type to define how the file
   * version is computed.
   *
   * @return the file version
   * @throws Exception the exception
   */
  public String getFileVersion() throws Exception;

  /**
   * Sets the release version, in case it is computed externally.
   *
   * @param releaseVersion the release version
   */
  public void setReleaseVersion(String releaseVersion);

  /**
   * Returns the configurable value.
   *
   * @param terminology the terminology
   * @param key the key
   * @return the configurable value
   * @throws Exception the exception
   */
  public String getConfigurableValue(String terminology, String key)
    throws Exception;
}
