/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Helper utility for sorting files.
 */
public class FileSorter {

  /**
   * Performs merge sort on a file.
   * 
   * @param inputFile the file_in_str
   * @param outputFile the file_out_str
   * @param comparator the comp
   * @throws Exception if anything goes wrong.
   */
  public static void sortFile(String inputFile, String outputFile,
    Comparator<String> comparator) throws Exception {

    // Split the input file into chunks and sort each section
    Logger.getLogger(FileSorter.class).info("  Split " + inputFile);
    List<String> splitFiles =
        splitFile(inputFile, new File(outputFile).getParent(), comparator,
            32 * 1024 * 1024);

    // Iteratively merge split files
    while (splitFiles.size() > 1) {

      // merge from the end
      String merged_file =
          mergeToTempFile(splitFiles.get(splitFiles.size() - 1),
              splitFiles.get(splitFiles.size() - 2), comparator);

      // remove last two elements of list
      String toRemove = splitFiles.remove(splitFiles.size() - 1);
      new File(toRemove).delete();
      toRemove = splitFiles.remove(splitFiles.size() - 1);
      new File(toRemove).delete();

      // add merged file to beginning of the list
      splitFiles.add(merged_file);
    }

    // rename resultant file (expects only one split file to remain)
    if (splitFiles.size() == 1) {

      File fileSorted = new File(splitFiles.get(0));
      File fileOut = new File(outputFile);
      Files.move(fileSorted.toPath(), fileOut.toPath());
      Logger.getLogger(FileSorter.class).info(
          "Moved file " + fileSorted.getName() + " to " + fileOut.getName());

      // Verify sort was successful
      checkSortedFile(fileOut, comparator);

    } else {
      throw new IllegalStateException(
          "Error sorting file, multiple split files remain");
    }

  }

  /**
   * Check sorted file.
   * 
   * @param file the file
   * @param comparator the comp
   * @return true, if successful
   * @throws Exception the exception
   */
  public static boolean checkSortedFile(File file, Comparator<String> comparator)
    throws Exception {

    String line, prevLine;
    int ctLines = 0;

    // open file
    BufferedReader in = new BufferedReader(new FileReader(file));

    prevLine = in.readLine();

    // loop until file empty
    while ((line = in.readLine()) != null) {

      // if line fails test, return false
      if (comparator.compare(prevLine, line) > 0) {
        in.close();
        Logger.getLogger(FileSorter.class).info(
            "SORT FAILED after " + Integer.toString(ctLines) + " lines: "
                + file.getName());
        return false;
      }
      prevLine = line;
    }

    // close file
    in.close();
    Logger.getLogger(FileSorter.class).info(
        "      Sort successful: " + file.getName());

    // if we've made it this far, things are good
    return true;
  }

  /**
   * Delete sorted files.
   * 
   * @param file the file
   */
  public static void deleteSortedFiles(File file) {
    // Check if file is directory/folder
    if (file.isDirectory()) {
      // Get all files in the folder
      File[] files = file.listFiles();

      for (int i = 0; i < files.length; i++) {
        // Delete each file in the folder
        deleteSortedFiles(files[i]);
      }
      // Delete the folder
      file.delete();
    } else {
      // Delete the file if it is not a folder
      file.delete();
    }
  }

  /**
   * Merge to temp file.
   * 
   * @param filename1 the file1_str
   * @param filename2 the file2_str
   * @param comparator the comp
   * @return the string
   * @throws Exception if anything goes wrong
   */
  private static String mergeToTempFile(String filename1, String filename2,
    Comparator<String> comparator) throws Exception {

    File file1 = new File(filename1);
    File file2 = new File(filename2);

    BufferedReader reader1 = new BufferedReader(new FileReader(file1));
    BufferedReader reader2 = new BufferedReader(new FileReader(file2));

    File fileOut =
        File.createTempFile("merged_", ".tmp", file1.getParentFile());
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileOut));

    String line1 = reader1.readLine();
    String line2 = reader2.readLine();

    String line;

    // debug testing
    int ctLine1 = 0;
    int ctLine2 = 0;

    Logger.getLogger(FileSorter.class).info(
        "   Merging files: " + filename1 + ", " + filename2 + " into "
            + fileOut.getName());

    // cycle until both files are empty
    while (!(line1 == null && line2 == null)) {

      // if first line empty, write second line
      if (line1 == null) {
        line = line2;
        line2 = reader2.readLine();
        ctLine2++;

        // if second line empty, write first line
      } else if (line2 == null) {
        line = line1;
        line1 = reader1.readLine();
        ctLine1++;

        // if line 1 precedes line 2
      } else if (comparator.compare(line1, line2) < 0) {

        line = line1;
        line1 = reader1.readLine();
        ctLine1++;

        // otherwise line 2 precedes or is equal to line 1
      } else {

        line = line2;
        line2 = reader2.readLine();
        ctLine2++;
      }

      // if a header line, do not write
      if (!line.startsWith("id")) {

        writer.write(line);
        writer.newLine();
      }
    }

    writer.close();
    reader1.close();
    reader2.close();

    Logger.getLogger(FileSorter.class).info(
        "     " + Integer.toString(ctLine1) + " lines from file 1 came first");
    Logger.getLogger(FileSorter.class).info(
        "     " + Integer.toString(ctLine2) + " lines from file 2 came first");

    return fileOut.getAbsolutePath();
  }

  /**
   * Splits a file given a line comparator.
   * 
   * @param inputFile the String giving the path to the file to be split
   * @param dir the directory
   * @param comparator the comparator function by which to compare lines
   * @param segmentSize the segment size
   * @return a String list of the split filenames
   * @throws Exception the exception
   */
  private static List<String> splitFile(String inputFile, String dir,
    Comparator<String> comparator, int segmentSize) throws Exception {

    // counter for current file size
    int currentSize = 0;
    // current file line
    String line;
    List<String> lines = new ArrayList<>(10000);

    List<String> splitFiles = new ArrayList<>();

    // open file
    File fileIn = new File(inputFile);
    BufferedReader reader = new BufferedReader(new FileReader(fileIn));

    // cycle until end of file
    while ((line = reader.readLine()) != null) {
      currentSize += line.length();
      lines.add(line);

      // if above segment size, sort and write the array
      if (currentSize > segmentSize) {

        // sort array
        Collections.sort(lines, comparator);

        // write file
        splitFiles.add(createSplitFile(lines, fileIn, new File(dir)));

        // reset line array and size tracker
        lines.clear();
        currentSize = 0;
      }
    }

    // write remaining lines to file
    Collections.sort(lines, comparator);
    splitFiles.add(createSplitFile(lines, fileIn, new File(dir)));

    reader.close();
    return splitFiles;
  }

  /**
   * Creates a temporary file from an array of lines.
   * 
   * @param lines the lines
   * @param fileIn the file_in
   * @param outputDir
   * @return the string
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private static String createSplitFile(List<String> lines, File fileIn,
    File outputDir) throws IOException {
    // write to array
    File fileTemp =
        File.createTempFile("split_" + fileIn.getName() + "_", ".tmp",
            outputDir);
    FileWriter fileWriter = new FileWriter(fileTemp);
    BufferedWriter writer = new BufferedWriter(fileWriter);

    for (int i = 0; i < lines.size(); i++) {
      writer.write(lines.get(i));
      writer.newLine();
    }
    writer.flush();
    writer.close();
    fileWriter.close();
    Logger.getLogger(FileSorter.class).info(
        "   Created split file: " + fileTemp.getName());
    return fileTemp.getAbsolutePath();
  }

}
