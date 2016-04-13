package com.wci.umls.server.jpa.algo;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.TerminologyLoaderAlgorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.meta.LogActivity;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractTerminologyLoaderAlgorithm extends
    HistoryServiceJpa implements TerminologyLoaderAlgorithm {

  /** LOADER constant for use as userName. */
  public final static String LOADER = "loader";

  /** The cancel flag. */
  protected boolean cancelFlag = false;

  /** The input path. */
  protected String inputPath = null;

  /** The terminology. */
  protected String terminology = null;

  /** The version. */
  protected String version = null;

  /** By default, sort and delete temporary files. */
  protected boolean sortFiles = true;

  /**
   * Instantiates an empty {@link AbstractTerminologyLoaderAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractTerminologyLoaderAlgorithm() throws Exception {
    // n/a
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

  /* see superclass */
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }

  /* see superclass */
  @Override
  public String getTerminology() {
    return this.terminology;
  }

  /* see superclass */
  @Override
  public void setVersion(String version) {
    this.version = version;
  }

  /* see superclass */
  @Override
  public String getVersion() {
    return this.version;
  }

  /* see superclass */
  @Override
  public void setSortFiles(boolean sortFiles) {
    this.sortFiles = sortFiles;
  }

  /* see superclass */
  @Override
  public abstract void computeTransitiveClosures() throws Exception;

  /* see superclass */
  @Override
  public abstract void computeTreePositions() throws Exception;

  /* see superclass */
  @Override
  public void commitClearBegin() throws Exception {

    if (cancelFlag) {
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

    if (cancelFlag) {
      throw new CancelException("Cancel requested");
    }

    if (objectCt % logCt == 0) {
      addLogEntry(LOADER, getTerminology(), getVersion(), LogActivity.LOADER,
          "    count = " + objectCt);
    }
    super.logAndCommit(objectCt, logCt, commitCt);
  }

  /**
   * Log info to console and the database.
   *
   * @param message the message
   * @throws Exception the exception
   */
  @Override
  public void logInfo(String message) throws Exception {
    addLogEntry(LOADER, getTerminology(), getVersion(), LogActivity.LOADER,
        message);
    Logger.getLogger(getClass()).info(message);
    // TODO: commit here?
  }

  /**
   * Log warning to console and the database.
   *
   * @param message the message
   * @throws Exception the exception
   */
  @Override
  public void logWarn(String message) throws Exception {
    addLogEntry(LOADER, getTerminology(), getVersion(), LogActivity.LOADER,
        "WARNING: " + message);
    Logger.getLogger(getClass()).warn(message);
    commit();
  }

  /**
   * Log error to console and the database.
   *
   * @param message the message
   * @throws Exception the exception
   */
  @Override
  public void logError(String message) throws Exception {
    addLogEntry(LOADER, getTerminology(), getVersion(), LogActivity.LOADER,
        "ERROR: " + message);
    Logger.getLogger(getClass()).error(message);
    commit();
  }

  /**
   * Cancel.
   *
   * @throws Exception the exception
   */
  @Override
  public void cancel() throws Exception {
    cancelFlag = true;
  }

  /**
   * Returns the total elapsed time str.
   *
   * @param time the time
   * @return the total elapsed time str
   */
  @SuppressWarnings({
    "boxing"
  })
  protected static String getTotalElapsedTimeStr(long time) {
    Long resultnum = (System.nanoTime() - time) / 1000000000;
    String result = resultnum.toString() + "s";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "m";
    resultnum = resultnum / 60;
    result = result + " / " + resultnum.toString() + "h";
    return result;
  }

}
