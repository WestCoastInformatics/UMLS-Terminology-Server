/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.algo.Algorithm;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.HistoryServiceJpa;
import com.wci.umls.server.services.HistoryService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * An algorithm for starting an editing cycle.
 * 
 * Mostly, this creates a {@link ReleaseInfo} for the upcoming release.
 */
public class StartEditingCycleAlgorithm extends ContentServiceJpa implements
    Algorithm {

  /** The release version. */
  private String releaseVersion = null;

  /** The terminology. */
  private String terminology = null;

  /** The version. */
  private String version = null;

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The request cancel flag. */
  boolean requestCancel = false;

  /** The user. */
  private String user;

  /**
   * Instantiates an empty {@link StartEditingCycleAlgorithm}.
   *
   * @param releaseVersion the release version
   * @param terminology the terminology
   * @param version the version
   * @throws Exception if anything goes wrong
   */
  public StartEditingCycleAlgorithm(String releaseVersion, String terminology,
      String version) throws Exception {
    super();
    this.releaseVersion = releaseVersion;
    this.terminology = terminology;
    this.version = version;
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing

  }

  /**
   * Returns the user.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets the user.
   *
   * @param user the user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass()).info(
        "Starting editing cycle for " + releaseVersion);

    // Check that there is a planned release info entry that has not yet been
    // started
    HistoryService service = new HistoryServiceJpa();
    ReleaseInfo info = service.getReleaseInfo(terminology, releaseVersion);
    if (info != null) {
      throw new Exception("Editing cycle already started for " + releaseVersion);
    }

    // Attempt to parse release revision for release date
    Logger.getLogger(getClass()).info("  Create release info");
    info = new ReleaseInfoJpa();
    info.setDescription("RF2 Release for " + releaseVersion);
    info.setName(releaseVersion);
    info.setPlanned(true);
    info.setPublished(false);
    info.setTerminology(terminology);
    info.setVersion(version);
    info.setLastModifiedBy(user);
    info.setReleaseBeginDate(new Date());
    service.addReleaseInfo(info);
    service.close();

    Logger.getLogger(getClass()).info("Done starting editing cycle");

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
  public void cancel() {
    requestCancel = true;
  }

  /**
   * Fire progress event.
   *
   * @param pct the pct
   * @param note the note
   */
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
  }

}
