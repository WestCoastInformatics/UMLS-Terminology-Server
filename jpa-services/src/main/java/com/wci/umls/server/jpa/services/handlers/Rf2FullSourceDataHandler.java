/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.File;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.algo.Rf2FullLoaderAlgorithm;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SourceDataService;

/**
 * Converter for RxNorm files.
 */
public class Rf2FullSourceDataHandler extends AbstractSourceDataHandler {

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
    Logger.getLogger(getClass()).info(
        "Loading RF2 Delta for "
            + (sourceData == null ? "null" : sourceData.getName()));

    // check pre-requisites
    if (sourceData == null) {
      throw new Exception("Source data is null");
    }
    if (sourceData.getId() == null) {
      throw new Exception("Source data has no assigned id");
    }
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
    String revisedInputDir = null;

    // find the FULL file
    for (File f : new File(inputDir).listFiles()) {
      if (f.getName().equals("Full")) {
        revisedInputDir = f.getAbsolutePath();
      }
    }

    if (revisedInputDir == null) {
      throw new LocalException(
          "Uploaded files must contain Full folder containing full release");
    }

    // instantiate service
    SourceDataService sourceDataService = new SourceDataServiceJpa();

    // update the source data
    sourceData.setStatus(SourceData.Status.LOADING);
    sourceDataService.updateSourceData(sourceData);

    // instantiate and set parameters for loader algorithm
    Rf2FullLoaderAlgorithm algo = new Rf2FullLoaderAlgorithm();
    algo.setTerminology(sourceData.getTerminology());
    algo.setVersion(sourceData.getVersion());
    algo.setInputPath(revisedInputDir);
    sourceDataService.registerSourceDataAlgorithm(sourceData.getId(), algo);

    Logger.getLogger(getClass()).info("  Prerequisites satisfied, computing");

    try {
      // perform main load
      algo.compute();

      // compute transitive closures and tree positions
      algo.computeTreePositions();
      algo.computeTransitiveClosures();

      sourceData.setStatus(SourceData.Status.LOADING_COMPLETE);
    } catch (Exception e) {
      sourceData.setStatus(SourceData.Status.LOADING_FAILED);
      throw new Exception(e);
    } finally {
      sourceDataService.unregisterSourceDataAlgorithm(sourceData.getId());
      sourceDataService.updateSourceData(sourceData);
      sourceDataService.close();
    }
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

  @Override
  public void reset() throws Exception {
    // do nothing

  }

}
