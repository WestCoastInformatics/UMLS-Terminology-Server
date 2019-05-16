/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.Properties;

import com.wci.umls.server.algo.TerminologyLoaderAlgorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractTerminologyLoaderAlgorithm
    extends AbstractAlgorithm implements TerminologyLoaderAlgorithm {

  /** LOADER constant for use as userName. */
  public final static String LOADER = "loader";

  /** The input path. */
  private String inputPath = null;

  /** The release version. */
  private String releaseVersion;

  /** By default, sort and delete temporary files. */
  private boolean sortFiles = true;

  /**
   * Instantiates an empty {@link AbstractTerminologyLoaderAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractTerminologyLoaderAlgorithm() throws Exception {
    setLastModifiedBy(LOADER);
    setWorkId("LOADER");
  }

  /* see superclass */
  @Override
  public String getInputPath() {
    return this.inputPath;
  }

  /* see superclass */
  @Override
  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  /**
   * Indicates whether or not sort files is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isSortFiles() {
    return sortFiles;
  }

  /* see superclass */
  @Override
  public void setSortFiles(boolean sortFiles) {
    this.sortFiles = sortFiles;
  }

  /**
   * Returns the release version.
   *
   * @return the release version
   */
  public String getReleaseVersion() {
    return releaseVersion;
  }

  /* see superclass */
  @Override
  public void setReleaseVersion(String releaseVersion) {
    this.releaseVersion = releaseVersion;
  }

  /* see superclass */
  @Override
  public void commitClearBegin() throws Exception {

    if (isCancelled()) {
      throw new CancelException("Cancel requested");
    }
    super.commitClearBegin();
  }

  /**
   * Log and commit.
   *
   * @param objectCt the object ct
   * @param logCt the log ct
   * @param commitCt the commit ct
   * @throws Exception the exception
   */
  @Override
  public void logAndCommit(int objectCt, int logCt, int commitCt)
    throws Exception {

    if (isCancelled()) {
      throw new CancelException("Cancel requested");
    }

    if (objectCt % logCt == 0) {
      addLogEntry(LOADER, getTerminology(), getVersion(), null, "LOADER",
          "    count = " + objectCt);
    }
    super.logAndCommit(objectCt, logCt, commitCt);
  }

  /**
   * Returns the total elapsed time str.
   *
   * @param time the time
   * @return the total elapsed time str
   */
  //@SuppressWarnings({"boxing"})
  protected static String getTotalElapsedTimeStr(long time) {
    Long resultnum = (System.nanoTime() - time) / 1000000000;
    String result = resultnum.toString() + "s";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "m";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "h";
    return result;
  }

  /**
   * Returns the configurable value.
   *
   * @param terminology the terminology
   * @param key the key
   * @return the configurable value
   * @throws Exception the exception
   */
  @Override
  public String getConfigurableValue(String terminology, String key)
    throws Exception {
    Properties p = ConfigUtility.getConfigProperties();
    String fullKey = getClass().getName() + "." + terminology + "." + key;
    if (p.containsKey(fullKey)) {
      return p.getProperty(fullKey);
    }
    return null;
  }
  
  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
