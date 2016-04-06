package com.wci.umls.server.jpa.algo;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.TerminologyLoaderAlgorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.meta.LogActivity;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractLoaderAlgorithm extends HistoryServiceJpa implements TerminologyLoaderAlgorithm  {

  /** LOADER constant for use as userName. */
  public final static String LOADER = "loader";
  
  private boolean cancelFlag = false;
  
  private String inputPath = null;
  
  private String terminology = null;
  
  private String version = null;

  /**
   * Instantiates an empty {@link AbstractLoaderAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractLoaderAlgorithm() throws Exception {
    // n/a
  }
  
  @Override
  public String getInputPath() {
    return this.inputPath;
  }
  
  @Override
  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }
  
  @Override
  public void setTerminology(String terminology) {
    this.terminology = terminology;
  }
  
  @Override
  public String getTerminology() {
    return this.terminology;
  }
  
  
  @Override
  public void setVersion(String version) {
    this.version = version;
  }
  
  @Override
  public String getVersion() {
    return this.version;
  }
  
  @Override
  public void computeTransitiveClosures() throws Exception {
    throw new Exception("Transitive closure computation must be overriden by non-abstract LoaderAlgorithm");
  }
  
  @Override 
  public void computeTreePositions() throws Exception {
    throw new Exception("Tree position computation must be overriden by non-abstract LoaderAlgorithm");
    
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
    
    if (cancelFlag == true) {
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
   */
  @Override
  public void cancel() {
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
