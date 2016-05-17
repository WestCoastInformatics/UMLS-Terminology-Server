package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.HasTerminology;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.model.meta.LogActivity;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractTerminologyAlgorithm extends HistoryServiceJpa
    implements Algorithm, HasTerminology {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** LOADER constant for use as userName. */
  public final static String LOADER = "loader";

  /** The cancel flag. */
  private boolean cancelFlag = false;

  /** The terminology. */
  private String terminology = null;

  /** The version. */
  private String version = null;

  /**
   * Instantiates an empty {@link AbstractTerminologyAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractTerminologyAlgorithm() throws Exception {
    // n/a
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
  public void logInfo(String message) throws Exception {
    addLogEntry(LOADER, getTerminology(), getVersion(), LogActivity.LOADER,
        message);
    Logger.getLogger(getClass()).info(message);
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
    commitClearBegin();
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
    // Attempt to commit the error -though sometimes this doesn't work
    // because of a rollback or other reason why the transaction doesn't exist
    try {
      commitClearBegin();
    } catch (Exception e) {
      // do nothihg
    }
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
   * Indicates whether or not cancelled is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isCancelled() {
    return cancelFlag;
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

  /**
   * Fires a {@link ProgressEvent}.
   *
   * @param pct percent done
   * @param note progress note
   * @throws Exception the exception
   */
  public void fireProgressEvent(int pct, String note) throws Exception {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    logInfo("    " + pct + "% " + note);
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
  }

}
