/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;

/**
 * Copier for RF2 files. Takes a set of concepts and descriptions and subsets
 * RF2 files based on that.
 */
public class Rf2FileCopier {

  /** The active only. */
  private boolean activeOnly;

  /**
   * Instantiates an empty {@link Rf2FileCopier}.
   *
   * @throws Exception if anything goes wrong
   */
  public Rf2FileCopier() throws Exception {
    // do nothing
  }

  /**
   * Sort files.
   *
   * @param inputDir the input dir
   * @param outputDir the output dir
   * @param concepts the concepts
   * @param descriptions the descriptions
   * @throws Exception the exception
   */
  public void copyFiles(File inputDir, File outputDir, Set<String> concepts,
    Set<String> descriptions) throws Exception {
    Logger.getLogger(getClass()).info("Start copying files");

    // Remove and remake output dir
    Logger.getLogger(getClass()).info("  Remove and remake output dir");
    ConfigUtility.deleteDirectory(outputDir);
    if (!outputDir.mkdirs()) {
      throw new Exception("Problem making output dir: " + outputDir);
    }
    new File(outputDir, "Terminology").mkdirs();
    new File(outputDir, "Refset").mkdirs();
    new File(outputDir, "Refset/Content").mkdirs();
    new File(outputDir, "Refset/Language").mkdirs();
    new File(outputDir, "Refset/Map").mkdirs();
    new File(outputDir, "Refset/Metadata").mkdirs();

    // Check preconditions
    if (!inputDir.exists()) {
      throw new Exception("Input dir does not exist: " + inputDir);
    }

    Map<String, String> dirMap = new HashMap<>();
    dirMap.put("sct2_Concept_", "/Terminology");
    dirMap.put("sct2_Relationship_", "/Terminology");
    dirMap.put("sct2_StatedRelationship_", "/Terminology");
    dirMap.put("sct2_Description_", "/Terminology");
    dirMap.put("sct2_TextDefinition_", "/Terminology");
    dirMap.put("Refset_Simple", "/Refset/Content");
    dirMap.put("AttributeValue", "/Refset/Content");
    dirMap.put("AssociationReference", "/Refset/Content");
    dirMap.put("ComplexMap", "/Refset/Map");
    dirMap.put("ExtendedMap", "/Refset/Map");
    dirMap.put("SimpleMap", "/Refset/Map");
    dirMap.put("Language", "/Refset/Language");
    dirMap.put("RefsetDescriptor", "/Refset/Metadata");
    dirMap.put("ModuleDependency", "/Refset/Metadata");
    dirMap.put("DescriptionType", "/Refset/Metadata");

    Map<String, Integer> keyMap = new HashMap<>();
    keyMap.put("sct2_Concept_", 0);
    keyMap.put("sct2_Relationship_", 4);
    keyMap.put("sct2_StatedRelationship_", 4);
    keyMap.put("sct2_Description_", 0);
    keyMap.put("sct2_TextDefinition_", 4);
    keyMap.put("Refset_Simple", 5);
    keyMap.put("AttributeValue", 5);
    keyMap.put("AssociationReference", 5);
    keyMap.put("ComplexMap", 5);
    keyMap.put("ExtendedMap", 5);
    keyMap.put("SimpleMap", 5);
    keyMap.put("Language", 5);
    keyMap.put("RefsetDescriptor", 5);
    keyMap.put("ModuleDependency", 5);
    keyMap.put("DescriptionType", 5);

    // Sort files
    for (final String key : dirMap.keySet()) {
      Logger.getLogger(getClass()).info("  Copying for " + key);
      final File file = findFile(new File(inputDir + dirMap.get(key)), key);
      Logger.getLogger(getClass()).info("    input file = " + file);

      final File outputFile =
          new File(new File(outputDir + dirMap.get(key)), file.getName());
      Logger.getLogger(getClass()).info("    output file = " + file);

      // Now, iterate through input file and copy lines with headers
      // or where the "keyMap" field is in concepts/descriptions

      BufferedReader in = new BufferedReader(new FileReader(file));
      PrintWriter out = new PrintWriter(new FileWriter(outputFile));
      String line;
      int index = keyMap.get(key);
      while ((line = in.readLine()) != null) {
        final String[] fields = FieldedStringTokenizer.split(line, "\t");

        // If active only, skip inactive entries
        if (activeOnly && fields[1].equals("0")) {
          continue;
        }
        // write headers
        if (line.startsWith("id\t")) {
          out.println(line);
        }

        // Relationship requires both ends to be connected
        if (key.contains("Relationship")) {
          if (concepts.contains(fields[index]) && concepts.contains(fields[5])) {
            out.println(line);
          }
        }

        // otherwise, just check the indexed field
        else if (concepts.contains(fields[index])
            || descriptions.contains(fields[index])) {
          out.println(line);
        }
      }
      in.close();
      out.close();

    }

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
    for (final File f : dir.listFiles()) {
      if (f.getName().contains(prefix)) {
        if (file != null)
          throw new Exception("Multiple " + prefix + " files");
        file = f;
      }
    }

    Logger.getLogger(getClass()).info(
        "      " + prefix + " = " + file.toString() + " " + file.exists());
    return file;
  }

  /**
   * Indicates whether or not active only is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isActiveOnly() {
    return activeOnly;
  }

  /**
   * Sets the active only.
   *
   * @param activeOnly the active only
   */
  public void setActiveOnly(boolean activeOnly) {
    this.activeOnly = activeOnly;
  }

}
