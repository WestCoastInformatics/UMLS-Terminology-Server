/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ReleaseInfoJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;

/**
 * An algorithm for starting an editing cycle.
 * 
 * Mostly, this creates a {@link ReleaseInfo} for the upcoming release.
 */
public class StartEditingCycleAlgorithm extends AbstractAlgorithm {

  /** The release version. */
  private String releaseVersion = null;

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
    setTerminology(terminology);
    setVersion(version);
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing

  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    Logger.getLogger(getClass())
        .info("Starting editing cycle for " + releaseVersion);

    // Check that there is a planned release info entry that has not yet been
    // started
    ReleaseInfo info = getReleaseInfo(getTerminology(), releaseVersion);
    if (info != null) {
      throw new Exception(
          "Editing cycle already started for " + releaseVersion);
    }

    // Attempt to parse release revision for release date
    Logger.getLogger(getClass()).info("  Create release info");
    info = new ReleaseInfoJpa();
    info.setDescription("RF2 Release for " + releaseVersion);
    info.setName(releaseVersion);
    info.setPlanned(true);
    info.setPublished(false);
    info.setTerminology(getTerminology());
    info.setVersion(getVersion());
    info.setReleaseBeginDate(new Date());
    info.setTimestamp(new Date());
    addReleaseInfo(info);

    Logger.getLogger(getClass()).info("Done starting editing cycle");

  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    return super.getParameters();
  }

}
