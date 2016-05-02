/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.helper;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.LocalException;

/**
 * Utility class for handling source data files.
 */
public class SourceDataFileUtility {

  /** Size of the buffer to read/write data. */
  private static final int BUFFER_SIZE = 4096;

  /**
   * Function to directly write a file to a destination folder from an input
   * stream.
   *
   * @param fileInputStream the input stream
   * @param destinationFolder the destination folder
   * @param fileName the name of the file
   * @return the file
   * @throws Exception the exception
   */
  public static File writeSourceDataFile(InputStream fileInputStream,
    String destinationFolder, String fileName) throws Exception {

    Logger.getLogger(SourceDataFileUtility.class).info(
        "Writing file " + destinationFolder + File.separator + fileName);

    if (fileExists(destinationFolder, fileName)) {
      throw new LocalException("File " + fileName
          + " already exists. Write aborted.");

    }

    BufferedOutputStream bos =
        new BufferedOutputStream(new FileOutputStream(destinationFolder
            + File.separator + fileName));
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;
    while ((read = fileInputStream.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();

    return new File(destinationFolder + File.separator + fileName);
  }

  /**
   * Extract compressed source data file.
   *
   * @param fileInputStream the file input stream
   * @param destinationFolder the destination folder
   * @param fileName the file name
   * @return the list
   * @throws Exception the exception thrown
   */
  public static List<File> extractCompressedSourceDataFile(
    InputStream fileInputStream, String destinationFolder, String fileName)
    throws Exception {

    Logger.getLogger(SourceDataFileUtility.class).info(
        "Extracting zip file to " + destinationFolder);

    // normalize destination folder path separators
    String dest = destinationFolder.replace("\\", "/");

    String cumPath = "";
    for (String destPart : dest.split("/")) {
      cumPath += destPart + "/";
      File f = new File(cumPath);
      if (f.exists() && !f.isDirectory()) {
        throw new Exception("Folder path segment " + destPart
            + " exists and is not a directory");
      } else if (!f.exists()) {
        Logger.getLogger(SourceDataFileUtility.class).info(
            "Creating folder " + cumPath);
        f.mkdir();
      }

    }

    List<File> files = new ArrayList<>();

    // convert file stream to zip input stream and get first entry
    ZipInputStream zipIn = new ZipInputStream(fileInputStream);
    ZipEntry entry = zipIn.getNextEntry();

    if (entry == null) {
      throw new LocalException("Could not unzip file " + fileName
          + ": not a ZIP file");
    }

    Logger.getLogger(SourceDataFileUtility.class)
        .info("  Cycling over entries");

    try {

      // iterates over entries in the zip file
      while (entry != null) {

        // construct a name without the zip file's name in it
        // NOTE Clunky system detected because can't rely on server-side
        // file separator to match system separator.
        int index = -1;
        if (entry.getName().indexOf(File.separator) != -1) {
          index = entry.getName().indexOf(File.separator);
        } else if (entry.getName().indexOf("\\") != -1) {
          index = entry.getName().indexOf("\\");
        } else if (entry.getName().indexOf("/") != -1) {
          index = entry.getName().indexOf("/");
        }

        String shortName = entry.getName().substring(index + 1);

        Logger.getLogger(SourceDataFileUtility.class).info(
            "  Processing " + shortName);

        // construct local directory to match file structure
        if (entry.isDirectory()) {

          Logger.getLogger(SourceDataFileUtility.class).info(
              "    Directory detected, creating folder");
          if (fileExists(destinationFolder, shortName)) {
            throw new LocalException("Unzipped folder " + shortName
                + " already exists. Write aborted");
          }

          // create the directory
          File f = new File(destinationFolder + File.separator + shortName);
          f.mkdir();
        }

        // if not a directory, simply extract the file
        else {

          Logger.getLogger(SourceDataFileUtility.class).info(
              "    File detected, extracting");

          if (fileExists(destinationFolder, entry.getName())) {
            throw new LocalException("Unzipped file " + shortName
                + " already exists. Write aborted.");
          }

          // preserve archive name by replacing file separator with underscore
          File f =
              extractZipEntry(zipIn, destinationFolder + File.separator
                  + shortName);

          files.add(f);
        }

        // if not a valid directory, delete previously added files and throw
        // exception
        /*
         * else { for (File f : files) { f.delete(); } throw new LocalException(
         * "Compressed file " + fileName +
         * " contains subdirectories. Upload aborted"); }
         */
        zipIn.closeEntry();
        entry = zipIn.getNextEntry();
      }
      zipIn.close();

      return files;
    } catch (Exception e) {
      // TODO Delete any successfully extracted files on failed load
      if (e instanceof LocalException) {
        throw e;
      } else {
        throw new Exception(e);
      }
    }

  }

  /**
   * Private helper class. Extracts a zip entry (file entry)
   *
   * @param zipIn the zip in
   * @param filePath the file path
   * @return the file
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static File extractZipEntry(ZipInputStream zipIn, String filePath)
    throws IOException {

    Logger.getLogger(SourceDataFileUtility.class).info(
        "Extracting file " + filePath);

    BufferedOutputStream bos =
        new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();

    // return the newly created file
    return new File(filePath);

  }

  /**
   * File exists.
   *
   * @param folderPath the folder path
   * @param fileName the file name
   * @return true, if successful
   */
  private static boolean fileExists(String folderPath, String fileName) {
    File dir = new File(folderPath);
    File[] files = dir.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.getName().equals(fileName)) {
          return true;
        }
      }
    }
    return false;
  }
}
