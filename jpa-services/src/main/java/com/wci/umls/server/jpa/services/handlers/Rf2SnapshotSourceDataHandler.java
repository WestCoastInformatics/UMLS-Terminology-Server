/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.File;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.algo.Rf2SnapshotLoaderAlgorithm;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.SourceDataHandler;

/**
 * Converter for RxNorm files.
 */
public class Rf2SnapshotSourceDataHandler extends AbstractSourceDataHandler implements SourceDataHandler {

 

  /**
   * Instantiates an empty {@link Rf2SnapshotSourceDataHandler}.
   */
  public Rf2SnapshotSourceDataHandler() {
    // n/a
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  @Override
  public String getName() {
    return "RF2 Snapshot Source Data Loader";
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
    // TODO Must find Refset and Terminology in SNAPSHOT folder
    // TODO UPdate the delta and full loaders
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

    // instantiate service
    SourceDataService sourceDataService = new SourceDataServiceJpa();

    // instantiate and set parameters for loader algorithm
    Rf2SnapshotLoaderAlgorithm algo = new Rf2SnapshotLoaderAlgorithm();
    algo.setTerminology(sourceData.getTerminology());
    algo.setVersion(sourceData.getTerminology());
    algo.setInputPath(inputDir);
    
    // update the source data
    sourceData.setStatus(SourceData.Status.LOADING);
    // TODO Require that source data has id and throw intelligent error/fail
    sourceDataService.updateSourceData(sourceData);
    
    try {
      // perform main load
      algo.compute();
      
      // compute transitive closures and tree positions
      algo.computeTreePositions();
      algo.computeTransitiveClosures();
      
      sourceData.setStatus(SourceData.Status.LOADING_COMPLETE);
    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.LOADING_FAILED);
    } finally {
      sourceDataService.updateSourceData(sourceData);
      sourceDataService.close();
    }
  }
  
}
