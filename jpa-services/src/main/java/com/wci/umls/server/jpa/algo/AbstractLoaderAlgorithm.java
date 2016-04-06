package com.wci.umls.server.jpa.algo;

import org.apache.log4j.Logger;

import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.meta.LogActivity;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractLoaderAlgorithm extends HistoryServiceJpa {

  /** LOADER constant for use as userName. */
  public final static String LOADER = "loader";

  /**
   * Instantiates an empty {@link AbstractLoaderAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractLoaderAlgorithm() throws Exception {
    // n/a
  }

  /**
   * Returns the terminology.
   *
   * @return the terminology
   */
  public abstract String getTerminology();

  /**
   * Returns the version.
   *
   * @return the version
   */
  public abstract String getVersion();

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
  public void logError(String message) throws Exception {
    addLogEntry(LOADER, getTerminology(), getVersion(), LogActivity.LOADER,
        "ERROR: " + message);
    Logger.getLogger(getClass()).error(message);
    commit();
  }

}
