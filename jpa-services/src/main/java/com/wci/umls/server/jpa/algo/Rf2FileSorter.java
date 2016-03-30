/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.common.io.Files;
import com.wci.umls.server.helpers.ConfigUtility;

/**
 * File sorter for RF2 files. This creates files with standard file names in the
 * specified output directory. See the source code for details.
 */
public class Rf2FileSorter {

  /** The sort by effective time. */
  private boolean sortByEffectiveTime = false;

  /** The file version. */
  private String fileVersion;

  /** The require all files. */
  private boolean requireAllFiles = false;

  /**
   * Instantiates an empty {@link Rf2FileSorter}.
   *
   * @throws Exception if anything goes wrong
   */
  public Rf2FileSorter() throws Exception {
    // do nothing
  }

  /**
   * Sets the sort by effective time.
   *
   * @param sortByEffectiveTime the sort by effective time
   */
  public void setSortByEffectiveTime(boolean sortByEffectiveTime) {
    this.sortByEffectiveTime = sortByEffectiveTime;
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
   * Sort files.
   *
   * @param inputDir the input dir
   * @param outputDir the output dir
   * @throws Exception the exception
   */
  public void sortFiles(File inputDir, File outputDir) throws Exception {
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

    Map<String, Integer> sortByMap = new HashMap<>();
    sortByMap.put("sct2_Concept_", 0);
    sortByMap.put("sct2_Relationship_", 4);
    sortByMap.put("sct2_StatedRelationship_", 4);
    sortByMap.put("merge_Relationship", 4);
    sortByMap.put("sct2_Description_", 4);
    sortByMap.put("sct2_TextDefinition_", 4);
    sortByMap.put("merge_Description", 4);
    sortByMap.put("Refset_Simple", 5);
    sortByMap.put("AttributeValue", 5);
    sortByMap.put("AssociationReference", 5);
    sortByMap.put("ComplexMap", 5);
    sortByMap.put("ExtendedMap", 5);
    sortByMap.put("SimpleMap", 5);
    sortByMap.put("Language", 5);
    sortByMap.put("RefsetDescriptor", 4);
    sortByMap.put("ModuleDependency", 4);
    sortByMap.put("DescriptionType", 4);

    Map<String, String> fileMap = new HashMap<>();
    fileMap.put("sct2_Concept_", "conceptsByConcept.sort");
    fileMap.put("sct2_Relationship_", "relationshipsBySourceConcept.sort");
    fileMap.put("sct2_StatedRelationship_",
        "statedRelationshipsBySourceConcept.sort");
    fileMap.put("sct2_Description_", "descriptionsByConcept.sort");
    fileMap.put("sct2_TextDefinition_", "definitionsByConcept.sort");
    fileMap.put("Refset_Simple", "simpleRefsetsByConcept.sort");
    fileMap.put("AttributeValue", "attributeValueRefsetsByRefCompId.sort");
    fileMap.put("AssociationReference",
        "associationReferenceRefsetsByRefCompId.sort");
    fileMap.put("ComplexMap", "complexMapRefsetsByConcept.sort");
    fileMap.put("ExtendedMap", "extendedMapRefsetsByConcept.sort");
    fileMap.put("SimpleMap", "simpleMapRefsetsByConcept.sort");
    fileMap.put("Language", "languageRefsetsByDescription.sort");
    fileMap.put("RefsetDescriptor", "refsetDescriptorByRefset.sort");
    fileMap.put("ModuleDependency", "moduleDependencyByRefset.sort");
    fileMap.put("DescriptionType", "descriptionTypeByRefset.sort");

    // Sort files
    int[] fields = null;
    for (String key : dirMap.keySet()) {
      Logger.getLogger(getClass()).info("  Sorting for " + key);
      final File file = findFile(new File(inputDir + dirMap.get(key)), key);
      Logger.getLogger(getClass()).info("    file = " + file);

      // Determine file version from filename
      // Skip null filenames (e.g. for nonexistent files)
      if (fileVersion == null && file != null) {
        Matcher matcher =
            Pattern.compile("\\d+").matcher(
                file.getName().substring(file.getName().lastIndexOf('_')));
        matcher.find();
        fileVersion = matcher.group();
        if (fileVersion == null) {
          throw new Exception("Unable to determine file version from "
              + file.getName());
        }
      }

      // Determine fields to sort by
      if (sortByEffectiveTime) {
        fields = new int[] {
            1, sortByMap.get(key)
        };
      } else {
        fields = new int[] {
          sortByMap.get(key)
        };
      }
      // Sort the file
      if (file != null) {
        sortRf2File(file, new File(outputDir + "/" + fileMap.get(key)), fields);
      } else {
        // otherwise just create an empty "sort" file
        new File(inputDir + dirMap.get(key) + "/" + fileMap.get(key))
            .createNewFile();
      }
    }

    // Merge relationship files
    Logger.getLogger(getClass()).info("  Merging relationship files...");
    File relationshipsFile =
        new File(outputDir + "/" + fileMap.get("sct2_Relationship_"));
    File statedRelationshipsFile =
        new File(outputDir + "/" + fileMap.get("sct2_StatedRelationship_"));
    // Determine fields to sort by
    if (sortByEffectiveTime) {
      fields = new int[] {
          1, sortByMap.get("merge_Relationship")
      };
    } else {
      fields = new int[] {
        sortByMap.get("merge_Relationship")
      };
    }

    File mergedRel =
        ConfigUtility.mergeSortedFiles(relationshipsFile,
            statedRelationshipsFile, getComparator(fields), outputDir, "");

    // rename the temporary file
    Files.move(mergedRel, new File(outputDir + "/"
        + "relationshipsAllBySourceConcept.sort"));

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
   * Helper function for sorting an individual file with colum comparator.
   * 
   * @param fileIn the input file to be sorted
   * @param fileOut the resulting sorted file
   * @param sortColumns the columns ([0, 1, ...]) to compare by
   * @throws Exception the exception
   */
  private void sortRf2File(File fileIn, File fileOut, final int[] sortColumns)
    throws Exception {
    Comparator<String> comp;
    // Comparator to split on \t and sort by sortColumn
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
        String v1[] = s1.split("\t");
        String v2[] = s2.split("\t");
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
