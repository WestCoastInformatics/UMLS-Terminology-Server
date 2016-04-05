/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.handlers;

import java.io.File;
import java.util.Properties;

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

/**
 * Converter for RxNorm files.
 */
public class Rf2SnapshotSourceDataHandler extends AbstractSourceDataHandler implements SourceDataHandler {

 

  /**
   * Instantiates an empty {@link Rf2SourceDataLoader}.
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
      throw new LocalException(
          "Source data directory is not a directory: " + inputDir);
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

    // instantiate service
    SourceDataService sourceDataService = new SourceDataServiceJpa();

    // Use content service rest because it has "loadRf2Terminology"
    final Properties config = ConfigUtility.getConfigProperties();
    final SecurityServiceRest securityService = new SecurityServiceRestImpl();
    final String adminAuthToken =
        securityService.authenticate(config.getProperty("admin.user"),
            config.getProperty("admin.password")).getAuthToken();
    final ContentServiceRest contentService = new ContentServiceRestImpl();
    try {
      sourceData.setStatus(SourceData.Status.LOADING);
      sourceDataService.updateSourceData(sourceData);
      contentService.loadTerminologyRf2Snapshot(sourceData.getTerminology(),
          sourceData.getVersion(), inputDir, adminAuthToken);
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
}
