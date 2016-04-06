/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.algo.Rf2DeltaLoaderAlgorithm;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Converter for RxNorm files.
 */
public class Rf2DeltaSourceDataHandler extends AbstractSourceDataHandler implements SourceDataHandler {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The source data. */
  private SourceData sourceData;

  /**
   * Instantiates an empty {@link Rf2DeltaSourceDataHandler}.
   */
  public Rf2DeltaSourceDataHandler() {
    // n/a
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return "RF2 Delta Source Data Loader";
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

    // find the data directory from the first sourceDataFile
    String inputDir = sourceData.getSourceDataFiles().get(0).getPath();

    if (!new File(inputDir).isDirectory()) {
      throw new LocalException("Source data directory is not a directory: "
          + inputDir);
    }

    SourceDataService sourceDataService = new SourceDataServiceJpa();
   
    sourceData.setStatus(SourceData.Status.LOADING);
    sourceDataService.updateSourceData(sourceData);
    
    try {

      // instantiate and set parameters for loader algorithm
      Rf2DeltaLoaderAlgorithm algo = new Rf2DeltaLoaderAlgorithm();
      algo.setTerminology(sourceData.getTerminology());
      algo.setVersion(sourceData.getTerminology());
      algo.setInputPath(inputDir);
     
      sourceData.setStatus(SourceData.Status.LOADING_COMPLETE);
     

    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.LOADING_FAILED);
      
      throw new Exception("Loading source data failed - " + sourceData, e);
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
    // n/a
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
