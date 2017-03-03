/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.helpers.CancelException;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.services.WorkflowServiceJpa;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Abstract support for loader algorithms.
 */
public abstract class AbstractAlgorithm extends WorkflowServiceJpa
    implements Algorithm {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The cancel flag. */
  private boolean cancelFlag = false;

  /** The terminology. */
  private String terminology = null;

  /** The version. */
  private String version = null;

  /** The activity id. */
  private String activityId;

  /** The work id. */
  private String workId = "ADMIN";

  /** The project. */
  private Project project;

  /** The process. */
  private ProcessExecution process;

  /**
   * Instantiates an empty {@link AbstractAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AbstractAlgorithm() throws Exception {
    // n/a
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

    if (objectCt % logCt == 0 && objectCt > 0) {
      if (getProject() != null) {
        addLogEntry(getLastModifiedBy(), getProject().getId(), null, activityId,
            workId, "    count = " + objectCt);
      } else {
        addLogEntry(getLastModifiedBy(), getTerminology(), getVersion(),
            activityId, workId, "    count = " + objectCt);
      }
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
    if (project != null) {
      addLogEntry(project.getId(), getLastModifiedBy(), getTerminology(),
          getVersion(), activityId, workId, message);
    } else {
      addLogEntry(getLastModifiedBy(), getTerminology(), getVersion(),
          activityId, workId, message);

    }
    Logger.getLogger(getClass()).info(message);
  }

  /**
   * Log warning to console and the database.
   *
   * @param message the message
   * @throws Exception the exception
   */
  public void logWarn(String message) throws Exception {
    if (project != null) {
      addLogEntry(project.getId(), getLastModifiedBy(), getTerminology(),
          getVersion(), activityId, workId, "WARNING: " + message);
    } else {
      addLogEntry(getLastModifiedBy(), getTerminology(), getVersion(),
          activityId, workId, "WARNING: " + message);
    }
    fireWarningEvent(message);
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
    if (project != null) {
      addLogEntry(project.getId(), getLastModifiedBy(), getTerminology(),
          getVersion(), activityId, workId, "ERROR: " + message);
    } else {
      addLogEntry(getLastModifiedBy(), getTerminology(), getVersion(),
          activityId, workId, "ERROR: " + message);
    }
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
   * Check cancel.
   *
   * @return true, if successful
   * @throws Exception the exception
   * @throws CancelException the cancel exception
   */
  public boolean checkCancel() throws Exception, CancelException {
    if (isCancelled()) {
      throw new CancelException("Operation cancelled");
    }
    return false;
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
    final ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    // don't write this to a log entry
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

  /**
   * Fire adusted progress event.
   *
   * @param pct the pct
   * @param step the step
   * @param steps the steps
   * @param note the note
   * @throws Exception the exception
   */
  public void fireAdjustedProgressEvent(int pct, int step, int steps,
    String note) throws Exception {
    final ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    logInfo(
        "    " + ((int) (((pct * 1.0) / steps) + ((step - 1) * 100.0 / steps)))
            + "% " + note);
  }

  /**
   * Fire warning event.
   *
   * @param note the note
   * @throws Exception the exception
   */
  public void fireWarningEvent(String note) throws Exception {
    ProgressEvent pe = new ProgressEvent(note, true);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
  }

  /* see superclass */
  @Override
  public void addProgressListener(ProgressListener l) {
    listeners.add(l);
  }

  /* see superclass */
  @Override
  public void removeProgressListener(ProgressListener l) {
    listeners.remove(l);
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
  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  /**
   * Returns the activity id.
   *
   * @return the activity id
   */
  public String getActivityId() {
    return activityId;
  }

  /**
   * Returns the work id.
   *
   * @return the work id
   */
  public String getWorkId() {
    return workId;
  }

  /* see superclass */
  @Override
  public void setWorkId(String workId) {
    this.workId = workId;
  }

  /* see superclass */
  @Override
  public Project getProject() {
    return project;
  }

  /* see superclass */
  @Override
  public void setProject(Project project) {
    this.project = project;
  }

  /* see superclass */
  @Override
  public String getName() {
    return ConfigUtility.getNameFromClass(getClass());
  }

  /**
   * Returns the process.
   *
   * @return the process
   */
  @Override
  public ProcessExecution getProcess() {
    return process;
  }

  /* see superclass */
  @Override
  public void setProcess(ProcessExecution process) {
    this.process = process;
  }

  /**
   * Sets the parameters.
   *
   * @param parameters the parameters
   * @throws Exception the exception
   */
  @Override
  public void setParameters(List<AlgorithmParameter> parameters)
    throws Exception {
    final Properties props = new Properties();
    for (final AlgorithmParameter param : parameters) {
      props.setProperty(param.getFieldName(), param.getValue());
    }
    setProperties(props);
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = new ArrayList<>();

    // Terminology/Version/Project/ActivityId/WorkId
    // are all set by the harness running the process.

    return params;
  }

  /**
   * Check required properties.
   *
   * @param required the required
   * @param p the p
   * @throws Exception the exception
   */
  @SuppressWarnings("static-method")
  public void checkRequiredProperties(String[] required, Properties p)
    throws Exception {
    if (p == null) {
      throw new LocalException("Algorithm properties must not be null");
    }
    for (final String prop : required) {
      if (prop != "" && !p.containsKey(prop)) {
        throw new LocalException("Required property " + prop + " missing");
      }
    }
  }

}
