/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.FileUtils;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Algorithm for packaging RRF release into a .zip file.
 */
public class PackageRrfReleaseAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link PackageRrfReleaseAlgorithm}.
   *
   * @throws Exception the exception
   */
  public PackageRrfReleaseAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("PACKAGERRF");

  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    logInfo("  path " + path);

    final String filename = getProcess().getVersion() + ".zip";
    final File zipFile =
        new File(path, getProcess().getVersion() + "/" + filename);
    logInfo("  zipFileName " + zipFile);

    if (zipFile.exists()) {
      throw new Exception("File already exists = " + zipFile.getAbsolutePath());
    }

    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    final String filename = getProcess().getVersion() + ".zip";
    final File zipFile =
        new File(path, getProcess().getVersion() + "/" + filename);

    final File pathMeta =
        new File(path, "/" + getProcess().getVersion() + "/META");
    logInfo("  pathMeta " + pathMeta);

    final File mmsysPath =
        new File(path, "/" + getProcess().getVersion() + "/MMSYS");
    logInfo("  mmsysPath " + mmsysPath);

    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
    logInfo("  Process META");
    zipDirectory(pathMeta, out,
        pathMeta.getPath().length() + 1 - "/META".length());
    logInfo("  Process MMSYS");
    zipDirectory(mmsysPath, out, mmsysPath.getPath().length() + 1);

    out.close();
    logInfo("Finished " + getName());

  }

  /**
   * Zip directory.
   *
   * @param folder the folder
   * @param zipOutputStream the zip output stream
   * @param prefixLength the prefix length
   * @throws Exception the exception
   */
  public void zipDirectory(File folder, ZipOutputStream zipOutputStream,
    int prefixLength) throws Exception {
    for (final File file : folder.listFiles()) {
      if (file.isFile()) {
        logInfo("    " + new File(folder, file.getName()).getName());
        final ZipEntry zipEntry =
            new ZipEntry(file.getPath().substring(prefixLength));
        zipOutputStream.putNextEntry(zipEntry);
        try (FileInputStream inputStream = new FileInputStream(file)) {
          IOUtils.copy(inputStream, zipOutputStream);
        }
        zipOutputStream.closeEntry();
        commitClearBegin();
      } else if (file.isDirectory()) {
        zipDirectory(file, zipOutputStream, prefixLength);
      }
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());

    // Remove the output zip file
    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    logInfo("  path " + path);

    final String filename = getProcess().getVersion() + ".zip";
    final File zipFile =
        new File(path, getProcess().getVersion() + "/" + filename);
    if (zipFile.exists()) {
      FileUtils.fileDelete(zipFile.getAbsolutePath());
    }
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
