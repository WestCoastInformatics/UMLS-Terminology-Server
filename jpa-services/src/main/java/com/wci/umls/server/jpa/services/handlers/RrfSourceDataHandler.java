/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.algo.RrfLoaderAlgorithm;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * RRF source data handler.
 */
public class RrfSourceDataHandler extends AbstractSourceDataHandler {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The source data. */
  private SourceData sourceData;

  /** The input dir. */
  private String inputDir = null;

  /** The prefix. */
  private String prefix = null;

  /** The single mode. */
  private Boolean singleMode = null;

  /** The code flag. */
  private Boolean codeFlag = null;

  /** The props. */
  private Properties props;

  /**
   * Instantiates an empty {@link RrfSourceDataHandler}.
   */
  public RrfSourceDataHandler() {
    // n/a
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return "RRF Source Data Handler";
  }

  /**
   * Convert.
   *
   * @throws Exception the exception
   */
  @Override
  public void compute() throws Exception {

    // check pre-requisites
    if (sourceData.getSourceDataFiles().size() == 0) {
      throw new Exception(
          "No source data files specified for source data object "
              + sourceData.getName());
    }
    if (sourceData.getHandler().isEmpty()) {
      throw new Exception(
          "No source data loader specified for source data object "
              + sourceData.getName());
    }
    if (sourceData.getTerminology() == null
        || sourceData.getTerminology().isEmpty()) {
      throw new Exception("No terminology specified for source data object "
          + sourceData.getName());
    }
    if (sourceData.getVersion() == null || sourceData.getVersion().isEmpty()) {
      throw new Exception("No version specified for source data object "
          + sourceData.getName());
    }
    if (sourceData.getReleaseVersion() == null
        || sourceData.getReleaseVersion().isEmpty()) {
      throw new Exception("No releaseVersion specified for source data object "
          + sourceData.getName());
    }

    if (!new File(inputDir).isDirectory()) {
      throw new LocalException("Source data directory is not a directory: "
          + inputDir);
    }

    // instantiate service
    SourceDataService sourceDataService = new SourceDataServiceJpa();

    // instantiate and set parameters for loader algorithm
    final RrfLoaderAlgorithm algorithm = new RrfLoaderAlgorithm();
    algorithm.setTerminology(sourceData.getTerminology());
    algorithm.setVersion(sourceData.getVersion());
    if (codeFlag == null || codeFlag) {
      algorithm.setCodesFlag(true);
    } else {
      algorithm.setCodesFlag(false);
    }
    algorithm.setSingleMode(singleMode);
    algorithm.compute();
    algorithm.close();

    // set to loading status and update the source data
    sourceData.setStatus(SourceData.Status.LOADING);
    sourceDataService.updateSourceData(sourceData);

    try {
      algorithm.compute();
      sourceData.setStatus(SourceData.Status.LOADING_COMPLETE);
    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.LOADING_FAILED);
    } finally {
      sourceDataService.updateSourceData(sourceData);
      sourceDataService.close();
    }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /**
   * Fires a {@link ProgressEvent}.
   * @param pct percent done
   * @param note progress note
   */
  @Override
  public void fireProgressEvent(int pct, String note) {
    ProgressEvent pe = new ProgressEvent(this, pct, pct, note);
    for (int i = 0; i < listeners.size(); i++) {
      listeners.get(i).updateProgress(pe);
    }
    Logger.getLogger(getClass()).info("    " + pct + "% " + note);
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
    throw new UnsupportedOperationException("cannot cancel.");
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    props = new Properties();
    props.putAll(p);
    if (props.containsKey("prefix")) {
      prefix = props.getProperty("prefix");
    }
  }

  /* see superclass */
  @Override
  public void setSourceData(SourceData sourceData) {
    this.sourceData = sourceData;
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    // n/a
  }

  /**
   * Sets the single mode.
   *
   * @param singleMode the new single mode
   */
  public void setSingleMode(boolean singleMode) {
    this.singleMode = singleMode;
  }

  /**
   * Gets the single mode.
   *
   * @return the single mode
   */
  public Boolean getSingleMode() {
    return this.singleMode;
  }

  /**
   * Sets the prefix.
   *
   * @param prefix the new prefix
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  /**
   * Gets the prefix.
   *
   * @return the prefix
   */
  public String getPrefix() {
    return this.prefix;
  }

  /**
   * Sets the code flag.
   *
   * @param codeFlag the new code flag
   */
  public void setCodeFlag(Boolean codeFlag) {
    this.codeFlag = codeFlag;
  }

  /**
   * Gets the code flag.
   *
   * @return the code flag
   */
  public Boolean getCodeFlag() {
    return this.codeFlag;
  }

  /**
   * Sets the input dir.
   *
   * @param inputDir the new input dir
   */
  public void setInputDir(String inputDir) {
    this.inputDir = inputDir;
  }

  /**
   * Gets the input dir.
   *
   * @param inputDir the input dir
   * @return the input dir
   */
  public String getInputDir(String inputDir) {
    return this.inputDir;
  }

  @Override
  public boolean checkPreconditions() throws Exception {
    ContentService contentService = null;
    try {
      contentService = new ContentServiceJpa();

      // concepts must not exist with this terminology/version
      if (contentService.findConceptsForQuery(sourceData.getTerminology(),
          sourceData.getVersion(), Branch.ROOT, null, new PfscParameterJpa())
          .getTotalCount() == 0) {
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (contentService != null)
        contentService.close();
    }
  }

}
