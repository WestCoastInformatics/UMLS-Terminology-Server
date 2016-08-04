/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.Rf2SnapshotLoaderAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.SourceDataService;

/**
 * Converter for RxNorm files.
 */
public class Rf2SnapshotSourceDataHandler extends AbstractSourceDataHandler {

  /**
   * Instantiates an empty {@link Rf2SnapshotSourceDataHandler}.
   *
   * @throws Exception the exception
   */
  public Rf2SnapshotSourceDataHandler() throws Exception {
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

    Logger.getLogger(getClass()).info("Loading RF2 Delta for "
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

    Logger.getLogger(getClass())
        .info("  Source data base directory: " + inputDir);

    if (!new File(inputDir).isDirectory()) {
      throw new LocalException(
          "Source data directory is not a directory: " + inputDir);
    }

    // find the SNAPSHOT directory
    String revisedInputDir = null;
    boolean terminologyFound = false;
    boolean refsetFound = false;
    List<File> filesToCheck =
        new ArrayList<>(Arrays.asList(new File(inputDir).listFiles()));
    while (!filesToCheck.isEmpty()) {
      File f = filesToCheck.get(0);
      if (f.isDirectory()) {
        if (f.getName().toLowerCase().equals("snapshot")) {
          revisedInputDir = f.getAbsolutePath();
          break;
        } else if (f.getName().toLowerCase().equals("terminology")) {
          terminologyFound = true;
        } else if (f.getName().toLowerCase().equals("refset")) {
          refsetFound = true;
        } else {
          filesToCheck.addAll(new ArrayList<>(Arrays.asList(f.listFiles())));
        }
      }
      filesToCheck.remove(0);
    }

    // Check for top level Refset/Terminology
    if (terminologyFound && refsetFound) {
      revisedInputDir = new File(inputDir).getAbsolutePath();
    }

    if (revisedInputDir == null) {
      throw new LocalException(
          "Uploaded files must contain 'Snapshot' folder containing snapshot release");
    }

    Logger.getLogger(getClass())
        .info("  Source data 'Snapshot' directory: " + revisedInputDir);

    // instantiate service
    SourceDataService sourceDataService = new SourceDataServiceJpa();

    // update the source data
    sourceData.setStatus(SourceData.Status.LOADING);
    sourceDataService.updateSourceData(sourceData);

    // instantiate and set parameters for loader algorithm
    Rf2SnapshotLoaderAlgorithm algo = new Rf2SnapshotLoaderAlgorithm();
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
      algo.close();
    }
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    final ValidationResult result = new ValidationResultJpa();
    ContentService contentService = null;
    try {
      contentService = new ContentServiceJpa();

      // concepts must exist with this terminology/version
      if (contentService.findConcepts(sourceData.getTerminology(),
          sourceData.getVersion(), Branch.ROOT, null, new PfsParameterJpa())
          .getTotalCount() > 0) {
        result.addError("Unexpected lack of concepts for "
            + sourceData.getTerminology() + ", " + sourceData.getVersion());
      }

      return result;
    } catch (Exception e) {
      throw e;
    } finally {
      if (contentService != null)
        contentService.close();
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

}
