/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Algorithm for packaging RRF release into a .zip file.
 */
public class PackageRrfReleaseAlgorithm extends AbstractAlgorithm {

  
  /** The path. */
  private File path;
  
  /** The filename. */
  private String filename;
  
  /** The zip file. */
  private File zipFile;
  
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
       
    path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    logInfo("  path " + path);
    
    filename = getProcess().getVersion() + ".zip";
    zipFile = new File(path, filename);
    logInfo("  zipFileName " + zipFile);
    
    
    if (zipFile.exists()) {
      throw new Exception(
          "File already exists = " + zipFile.getAbsolutePath());
    }
    
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
       
    final File pathMeta = new File(path, "/" + getProcess().getVersion() + "/META");
    logInfo("  pathMeta " + pathMeta);

    final File mmsysPath = new File(path, "/" + getProcess().getVersion() + "/MMSYS");
    logInfo("  mmsysPath " + mmsysPath);   

    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile)); 
    zipDirectory(pathMeta, out, pathMeta.getPath().length() + 1 - "/META".length());     
    zipDirectory(mmsysPath, out, mmsysPath.getPath().length() + 1);
    
    out.close(); 
    
  }

  public static void zipDirectory(File folder, ZipOutputStream zipOutputStream, int prefixLength)
    throws IOException { 
    for (final File file : folder.listFiles()) {
      if (file.isFile()) {
          final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
          zipOutputStream.putNextEntry(zipEntry);
          try (FileInputStream inputStream = new FileInputStream(file)) {
              IOUtils.copy(inputStream, zipOutputStream);
          }
          zipOutputStream.closeEntry();
      } else if (file.isDirectory()) {
          zipDirectory(file, zipOutputStream, prefixLength);
      }
    }

  }  
  
  /* see superclass */
  @Override
  public void reset() throws Exception {

    logInfo("Starting RESET " + getName());
    // n/a
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
