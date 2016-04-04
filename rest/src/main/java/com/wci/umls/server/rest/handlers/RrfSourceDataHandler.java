/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.SecurityServiceRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.rest.impl.SecurityServiceRestImpl;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Converter for RxNorm files.
 */
public class RrfSourceDataHandler implements SourceDataHandler {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The source data. */
  private SourceData sourceData;

  /** The terminology. */
  private String terminology;

  /** The version. */
  private String version;

  /** The prefix. */
  private String prefix;

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
    if (sourceData.getTerminology() == null || sourceData.getTerminology().isEmpty()) {
      throw new Exception("No terminology specified for source data object " + sourceData.getName());
    }
    if (sourceData.getVersion() == null || sourceData.getVersion().isEmpty()) {
      throw new Exception("No version specified for source data object " + sourceData.getName());
    }
    if (sourceData.getReleaseVersion() == null || sourceData.getReleaseVersion().isEmpty()) {
      throw new Exception("No releaseVersion specified for source data object " + sourceData.getName());
    }

    // find the data directory from the first sourceDataFile
    String inputDir = sourceData.getSourceDataFiles().get(0).getPath();

    if (!new File(inputDir).isDirectory()) {
      throw new LocalException(
          "Source data directory is not a directory: " + inputDir);
    }

    SourceDataService sourceDataService = new SourceDataServiceJpa();
    sourceDataService.updateSourceData(sourceData);

    // Use content service rest because it has "loadRrfTerminology"
    final Properties config = ConfigUtility.getConfigProperties();
    final SecurityServiceRest securityService = new SecurityServiceRestImpl();
    final String adminAuthToken =
        securityService.authenticate(config.getProperty("admin.user"),
            config.getProperty("admin.password")).getAuthToken();
    final ContentServiceRest contentService = new ContentServiceRestImpl();
    try {
      sourceData.setStatus(SourceData.Status.LOADING);
      sourceDataService.updateSourceData(sourceData);
      contentService.loadTerminologyRrf(terminology, version, false, false,
          prefix, inputDir, adminAuthToken);
      sourceData.setStatus(SourceData.Status.LOADING_COMPLETE);
      sourceDataService.updateSourceData(sourceData);

    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.LOADING_FAILED);
      sourceDataService.updateSourceData(sourceData);
      throw new Exception("Loading source data failed - " + sourceData, e);
    } finally {
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


 
}
