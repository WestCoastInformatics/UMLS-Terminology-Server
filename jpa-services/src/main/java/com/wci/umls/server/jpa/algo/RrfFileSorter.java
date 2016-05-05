/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.io.FileInputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;

/**
 * File sorter for RRF files. This creates files with standard file names in the
 * specified output directory. See the source code for details.
 */
public class RrfFileSorter {

  /** The file version. */
  private String fileVersion;

  /** The require all files. */
  private boolean requireAllFiles = false;

  /**
   * Instantiates an empty {@link RrfFileSorter}.
   *
   * @throws Exception if anything goes wrong
   */
  public RrfFileSorter() throws Exception {
    // do nothing
  }

  /**
   * Sets the require all files flag.
   *
   * @param requireAllFiles the require all files
   */
  public void setRequireAllFiles(boolean requireAllFiles) {
    this.requireAllFiles = requireAllFiles;
  }

  /**
   * Returns the file version.
   *
   * @return the file version
   */
  public String getFileVersion() {
    return fileVersion;
  }

  /**
   * Returns the file version.
   *
   * @param inputDir the input dir
   * @return the file version
   * @throws Exception the exception
   */
  public String getFileVersion(File inputDir) throws Exception {

    // Determine file version from filename
    String fileVersion = null;
    Properties p = new Properties();
    try {
      final File releasedat = findFile(inputDir, "release.dat");
      p.load(new FileInputStream(releasedat));
    } catch (Exception e) {
      // If requiring all files, throw exception
      if (requireAllFiles) {
        throw new Exception("Unable to resolve version from release.dat", e);
      }
      // otherwise, return null
      else {
        return null;
      }
    }
    fileVersion = p.getProperty("umls.release.name");
    if (fileVersion == null) {
      throw new Exception("Unable to determine file version");
    }
    return fileVersion;

  }

  /**
   * Sort files.
   *
   * @param inputDir the input dir
   * @param outputDir the output dir
   * @param prefix the prefix
   * @throws Exception the exception
   */
  public void sortFiles(File inputDir, File outputDir, String prefix)
    throws Exception {
    Logger.getLogger(getClass()).info("Start sorting files");

    // Remove and remake output dir
    Logger.getLogger(getClass()).info("  Remove and remake output dir");
    ConfigUtility.deleteDirectory(outputDir);
    if (!outputDir.mkdirs()) {
      throw new Exception("Problem making output dir: " + outputDir);
    }

    // Check preconditions
    if (!inputDir.exists()) {
      throw new Exception("Input dir does not exist: " + inputDir);
    }

    Map<String, String> dirMap = new HashMap<>();
    dirMap.put(prefix + "CONSO.RRF", "/");
    dirMap.put(prefix + "DEF.RRF", "/");
    dirMap.put(prefix + "DOC.RRF", "/");
    dirMap.put(prefix + "MAP.RRF", "/");
    dirMap.put(prefix + "RANK.RRF", "/");
    dirMap.put(prefix + "REL.RRF", "/");
    dirMap.put(prefix + "SAB.RRF", "/");
    dirMap.put(prefix + "SAT.RRF", "/");
    dirMap.put(prefix + "STY.RRF", "/");
    dirMap.put("SRDEF", "/");

    Map<String, Integer> sortByMap = new HashMap<>();
    sortByMap.put(prefix + "CONSO.RRF", 0);
    sortByMap.put(prefix + "DEF.RRF", 0);
    sortByMap.put(prefix + "DOC.RRF", 0);
    sortByMap.put(prefix + "MAP.RRF", 0);
    sortByMap.put(prefix + "RANK.RRF", 0);
    sortByMap.put(prefix + "REL.RRF", 0);
    sortByMap.put(prefix + "SAB.RRF", 0);
    sortByMap.put(prefix + "SAT.RRF", 0);
    sortByMap.put(prefix + "STY.RRF", 0);
    sortByMap.put("SRDEF", 0);

    Map<String, String> fileMap = new HashMap<>();
    fileMap.put(prefix + "CONSO.RRF", "consoByConcept.sort");
    fileMap.put(prefix + "DEF.RRF", "defByConcept.sort");
    fileMap.put(prefix + "DOC.RRF", "docByKey.sort");
    fileMap.put(prefix + "MAP.RRF", "mapByConcept.sort");
    fileMap.put(prefix + "RANK.RRF", "rankByRank.sort");
    fileMap.put(prefix + "REL.RRF", "relByConcept.sort");
    fileMap.put(prefix + "SAB.RRF", "sabBySab.sort");
    fileMap.put(prefix + "SAT.RRF", "satByConcept.sort");
    fileMap.put(prefix + "STY.RRF", "styByConcept.sort");
    fileMap.put("SRDEF", "srdef.sort");

    // Sort files
    int[] fields = null;
    for (String key : dirMap.keySet()) {
      Logger.getLogger(getClass()).info("  Sorting for " + key);
      final File file = findFile(new File(inputDir + dirMap.get(key)), key);
      Logger.getLogger(getClass()).info("    file = " + file);

      // Determine file version from filename
      if (fileVersion == null) {
        Properties p = new Properties();
        try {
          final File releasedat =
              findFile(new File(inputDir + dirMap.get(key)), "release.dat");
          p.load(new FileInputStream(releasedat));
        } catch (Exception e) {
          throw new Exception("Unable to resolve version from release.dat", e);
        }
        fileVersion = p.getProperty("umls.release.name");
        if (fileVersion == null) {
          throw new Exception("Unable to determine file version from "
              + file.getName());
        }
      }

      // Determine fields to sort by
      fields = new int[] {
        sortByMap.get(key)
      };

      // Sort the file
      if (file != null) {
        sortRrfFile(file, new File(outputDir + "/" + fileMap.get(key)), fields);
      } else {
        // otherwise just create an empty "sort" file
        new File(inputDir + dirMap.get(key) + "/" + fileMap.get(key))
            .createNewFile();
      }
    }

    Thread.sleep(1000);
    Logger.getLogger(getClass()).info("Done...");

  }

  /**
   * Find file.
   *
   * @param dir the dir
   * @param prefix the prefix
   * @return the file
   * @throws Exception the exception
   */
  public File findFile(File dir, String prefix) throws Exception {
    File file = null;
    // file
    for (File f : dir.listFiles()) {
      if (f.getName().contains(prefix)) {
        if (file != null)
          throw new Exception("Multiple " + prefix + " files");
        file = f;
      }
    }
    if (file == null) {
      if (requireAllFiles) {
        throw new Exception("Missing " + prefix + " file");
      } else {
        return null;
      }
    }
    Logger.getLogger(getClass()).info(
        "      " + prefix + " = " + file.toString() + " " + file.exists());
    return file;
  }

  /**
   * Helper function for sorting an individual file with column comparator.
   * 
   * @param fileIn the input file to be sorted
   * @param fileOut the resulting sorted file
   * @param sortColumns the columns ([0, 1, ...]) to compare by
   * @throws Exception the exception
   */
  private void sortRrfFile(File fileIn, File fileOut, final int[] sortColumns)
    throws Exception {
    Comparator<String> comp;
    // Comparator to split on | and sort by sortColumn
    comp = getComparator(sortColumns);

    StringBuilder columns = new StringBuilder();
    boolean first = true;
    for (final int sortColumn : sortColumns) {
      if (!first) {
        columns.append(", ");
        first = false;
      }
      columns.append(sortColumn);
    }
    Logger.getLogger(getClass()).info(
        "    Sorting " + fileIn.getName() + "  into " + fileOut.toString()
            + " by columns " + columns);
    FileSorter.sortFile(fileIn.toString(), fileOut.toString(), comp);

  }

  /**
   * Returns the comparator.
   *
   * @param sortColumns the sort columns
   * @return the comparator
   */
  @SuppressWarnings("static-method")
  private Comparator<String> getComparator(final int[] sortColumns) {
    return new Comparator<String>() {
      @Override
      public int compare(String s1, String s2) {
        String v1[] = s1.split("\\|");
        String v2[] = s2.split("\\|");
        for (final int sortColumn : sortColumns) {
          final int cmp = v1[sortColumn].compareTo(v2[sortColumn]);
          if (cmp != 0) {
            return cmp;
          }
        }
        return 0;
      }
    };
  }
}
