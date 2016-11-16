/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;

/**
 * Implementation of an algorithm to import RF2 snapshot data.
 */
public class Rf2FullLoaderAlgorithm extends AbstractTerminologyLoaderAlgorithm {

  /**
   * Instantiates an empty {@link Rf2FullLoaderAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public Rf2FullLoaderAlgorithm() throws Exception {
    super();
  }

  /* see superclass */
  @Override
  public String getFileVersion() throws Exception {
    Rf2FileSorter sorter = new Rf2FileSorter();
    sorter.setInputDir(getInputPath());
    return sorter.getFileVersion();
  }

  /* see superclass */
  @SuppressWarnings("resource")
  @Override
  public void compute() throws Exception {

    logInfo("Start loading full");
    logInfo("  terminology = " + getTerminology());
    logInfo("  version = " + getVersion());
    logInfo("  inputPath = " + getInputPath());

    // check preconditions
    if (getTerminology() == null) {
      throw new Exception("Terminology name must be specified");
    }
    if (getVersion() == null) {
      throw new Exception("Terminology getVersion() must be specified");
    }
    if (getInputPath() == null) {
      throw new Exception("Input directory must be specified");
    }

    // Check the input directory
    File inputDirFile = new File(getInputPath());
    if (!inputDirFile.exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    //
    // Look through files to obtain ALL release versions
    //
    Logger.getLogger(getClass()).info("  Get release getVersion()s");
    Rf2FileSorter sorter = new Rf2FileSorter();
    final File conceptsFile = sorter
        .findFile(new File(getInputPath(), "Terminology"), "sct2_Concept");
    final Set<String> releaseSet = new HashSet<>();
    BufferedReader reader = new BufferedReader(new FileReader(conceptsFile));
    String line;
    while ((line = reader.readLine()) != null) {
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[1].equals("effectiveTime")) {
        try {
          ConfigUtility.DATE_FORMAT.parse(fields[1]);
        } catch (Exception e) {
          throw new Exception("Improperly formatted date found: " + fields[1]);
        }
        releaseSet.add(fields[1]);
      }
    }
    reader.close();
    final File complexMapFile = sorter.findFile(
        new File(getInputPath(), "Refset/Map"), "der2_iissscRefset_ComplexMap");
    reader = new BufferedReader(new FileReader(complexMapFile));
    while ((line = reader.readLine()) != null) {
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[1].equals("effectiveTime")) {
        try {
          ConfigUtility.DATE_FORMAT.parse(fields[1]);
        } catch (Exception e) {
          throw new Exception("Improperly formatted date found: " + fields[1]);
        }
        releaseSet.add(fields[1]);
      }
    }
    File extendedMapFile =
        sorter.findFile(new File(getInputPath(), "Refset/Map"),
            "der2_iisssccRefset_ExtendedMap");
    reader = new BufferedReader(new FileReader(extendedMapFile));
    while ((line = reader.readLine()) != null) {
      final String fields[] = FieldedStringTokenizer.split(line, "\t");
      if (!fields[1].equals("effectiveTime")) {
        try {
          ConfigUtility.DATE_FORMAT.parse(fields[1]);
        } catch (Exception e) {
          throw new Exception("Improperly formatted date found: " + fields[1]);
        }
        releaseSet.add(fields[1]);
      }
    }
    reader.close();
    final List<String> releases = new ArrayList<>(releaseSet);
    Collections.sort(releases);

    // check that release info does not already exist
    Logger.getLogger(getClass()).info("  Releases to process");
    for (final String release : releases) {
      Logger.getLogger(getClass()).info("    release = " + release);
      ReleaseInfo releaseInfo = getReleaseInfo(getTerminology(), release);
      if (releaseInfo != null) {
        throw new Exception("A release info already exists for " + release);
      }
    }

    // Remove any old sorting dir
    ConfigUtility
        .deleteDirectory(new File(getInputPath(), "/RF2-sorted-temp/"));

    // Sort files
    Logger.getLogger(getClass()).info("  Sort RF2 Files");
    sorter = new Rf2FileSorter();
    sorter.setSortByEffectiveTime(true);
    sorter.setRequireAllFiles(true);
    sorter.setInputDir(getInputPath());
    sorter.setOutputDir(getInputPath() + "/RF2-sorted-temp/");
    sorter.compute();

    // Readers will be opened here
    File outputDir = new File(inputDirFile, "/RF2-sorted-temp/");
    final Rf2Readers readers = new Rf2Readers(outputDir);
    readers.openReaders();

    // Load initial snapshot, pass in initial release version
    // and readers and indicate to avoid sorting files
    final Rf2SnapshotLoaderAlgorithm algorithm =
        new Rf2SnapshotLoaderAlgorithm();
    algorithm.setTerminology(getTerminology());
    algorithm.setVersion(getVersion());
    algorithm.setInputPath(getInputPath());
    algorithm.setReleaseVersion(releases.get(0));
    algorithm.setReaders(readers);
    algorithm.setSortFiles(false);
    algorithm.compute();
    algorithm.close();

    // Load deltas
    for (final String release : releases) {

      if (release.equals(releases.get(0))) {
        continue;
      }

      // Run loader on each subsequent release
      // Pass in the release version and the readers
      Rf2DeltaLoaderAlgorithm algorithm2 = new Rf2DeltaLoaderAlgorithm();
      algorithm2.setTerminology(getTerminology());
      algorithm.setInputPath(getInputPath());
      algorithm2.setVersion(getVersion());
      algorithm2.setReleaseVersion(release);
      algorithm2.setReaders(readers);
      algorithm2.setSortFiles(false);
      algorithm2.setInputPath(getInputPath());
      algorithm2.compute();
      algorithm2.close();
      algorithm2 = null;

    }

    // Remove sort directory if sorting was done locally
    ConfigUtility
        .deleteDirectory(new File(getInputPath(), "/RF2-sorted-temp/"));

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // do nothing
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    super.close();
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    checkRequiredProperties(new String[] {
        "inputDir"
    }, p);

    if (p.getProperty("inputDir") != null) {
      setInputPath(p.getProperty("inputDir"));
    }

  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() {
    final List<AlgorithmParameter> params = super.getParameters();
    AlgorithmParameter param = new AlgorithmParameterJpa("Input Dir",
        "inputDir", "Input RF2 Full directory to load", "", 255,
        AlgorithmParameter.Type.DIRECTORY);
    params.add(param);
    return params;

  }
}
