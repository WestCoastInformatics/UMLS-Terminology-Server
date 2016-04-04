package com.wci.umls.server.jpa.algo;

import org.apache.log4j.Logger;

import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.meta.LogActivity;

/**
 * The Class AbstractLoaderAlgorithm.
 */
public abstract class AbstractLoaderAlgorithm extends HistoryServiceJpa {

  
  /**
   * Instantiates a new abstract loader algorithm.
   *
   * @throws Exception the exception
   */
  public AbstractLoaderAlgorithm() throws Exception {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public abstract String getTerminology();
  
  /**
   * Gets the version.
   *
   * @return the version
   */
  public abstract String getVersion();
  
  /**
   * Commit clear begin.
   *
   * @throws Exception the exception
   */
  @Override
  public void commitClearBegin() throws Exception {
    addLogEntry("loader", getTerminology(), getVersion(),
        "Commit Clear Begin", LogActivity.LOADER);
    
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
  public void logAndCommit(int objectCt, int logCt, int commitCt) throws Exception {
    if (objectCt % logCt == 0) {
      addLogEntry("loader", getTerminology(), getVersion(),
          "Commit: count = " + objectCt, LogActivity.LOADER);
    }
    
    super.logAndCommit(objectCt, logCt, commitCt);    
  }

  /**
   * Log info.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logInfo(String message)
    throws Exception {
    addLogEntry("loader", getTerminology(), getVersion(), message,
        LogActivity.LOADER);
    Logger.getLogger(getClass()).info(message);
    // TODO: commit here?
  }

  /**
   * Log warn.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logWarn(String message)
    throws Exception {
    addLogEntry("loader", getTerminology(), getVersion(), "WARNING: " + message,
        LogActivity.LOADER);
    Logger.getLogger(getClass()).warn(message);
    commit();
  }

  /**
   * Log error.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logError(String message)
    throws Exception {
    addLogEntry("loader", getTerminology(), getVersion(), "ERROR: " + message,
        LogActivity.LOADER);
    Logger.getLogger(getClass()).error(message);
    commit();
  }


}
