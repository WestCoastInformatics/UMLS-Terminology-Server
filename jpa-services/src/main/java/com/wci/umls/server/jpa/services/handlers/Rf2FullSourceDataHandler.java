/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.Rf2FullLoaderAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
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
   *
   * @throws Exception the exception
   */
  public Rf2FullSourceDataHandler() throws Exception {
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

    if (!new File(inputDir).isDirectory()) {
      throw new LocalException(
          "Source data directory is not a directory: " + inputDir);
    }

    // RF2 Loads require locating a base directory containing two folders
    // (Refset and Terminology)
    String revisedInputDir = null;

    // find the FULL file
    for (final File f : new File(inputDir).listFiles()) {
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

      // compute tree pos, compute transitive closure
      // TODO: reowrk this to operate around a "process" and source data service
      // can register the proccess - or we can fold this all into process
      // serviceP

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
      if (contentService.findConceptSearchResults(sourceData.getTerminology(),
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

  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

}
