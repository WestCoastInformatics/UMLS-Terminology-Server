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
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;
import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.services.helpers.ProgressListener;

/**
 * Converter for RxNorm files.
 */
public class Rf2FullSourceDataHandler extends AbstractSourceDataHandler implements SourceDataHandler {

  /** Listeners. */
  private List<ProgressListener> listeners = new ArrayList<>();

  /** The source data. */
  private SourceData sourceData;

  /**
   * Instantiates an empty {@link Rf2FullSourceDataHandler}.
   */
  public Rf2FullSourceDataHandler() {
    // n/a
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return "RF2 Full Source Data Loader";
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

    // find directory path based on upload directory and id
    String inputDir =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + sourceData.getId().toString();

    if (!new File(inputDir).isDirectory()) {
      throw new LocalException("Source data directory is not a directory: "
          + inputDir);
    }

    // RF2 Loads require locating a base directory containing two folders
    // (Refset and Terminology)
    String[] files = new File(inputDir).list();
    String revisedInputDir = null;

    // flags for whether refset and terminology folders were found
    boolean refsetFound = false;
    boolean terminologyFound = false;

    // check the input directory for existence of Refset and Terminology folders
    for (File f : new File(inputDir).listFiles()) {
      if (f.getName().equals("Refset")) {
        refsetFound = true;
      }
      if (f.getName().equals("Terminology")) {
        terminologyFound = true;
      }
      if (refsetFound && terminologyFound) {
        revisedInputDir = inputDir;
        break;
      }
    }

    // otherwise, cycle over subdirectories
    for (String f : files) {
      File file = new File(f);

      if (revisedInputDir != null)
        break;

      // only want to check directories
      if (file.isDirectory()) {
        refsetFound = false;
        terminologyFound = false;
        for (File f2 : file.listFiles()) {
          if (f2.getName().equals("Refset")) {
            refsetFound = true;
          }
          if (f2.getName().equals("Terminology")) {
            terminologyFound = true;
          }
          if (refsetFound && terminologyFound) {
            revisedInputDir = f2.getAbsolutePath();
            break;
          }
        }
      }
    }

    if (revisedInputDir == null) {
      throw new LocalException(
          "Uploaded files do not contain a directory with both RefSet and Terminology files");
    }

    // ensure that source data is up to date in database
    SourceDataService sourceDataService = new SourceDataServiceJpa();
    sourceDataService.updateSourceData(sourceData);

    
    sourceData.setStatus(SourceData.Status.LOADING);
    sourceDataService.updateSourceData(sourceData);
   
    // Use content service rest because it has "loadRf2Terminology"
    try {
      // TODO algo.compute()
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
