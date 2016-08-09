/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.algo.Rf2FileCopier;
import com.wci.umls.server.jpa.algo.Rf2FileSorter;
import com.wci.umls.server.jpa.algo.Rf2Readers;
import com.wci.umls.server.jpa.algo.Rf2SnapshotSamplerAlgorithm;

/**
 * Goal which samples an RF2 Snapshot of SNOMED CT data and outputs RF2.
 * 
 * See admin/loader/pom.xml for sample usage
 * 
 * @goal sample-rf2-snapshot
 * 
 * @phase package
 */
public class TerminologyRf2SnapshotSampler extends AbstractMojo {

  /**
   * Input directory.
   * @parameter
   * @required
   */
  private String inputDir;

  /**
   * Input concepts file.
   * @parameter
   * @required
   */
  private String inputFile = null;

  /**
   * Output directory.
   * @parameter
   * @required
   */
  private String outputDir;

  /**
   * Whether to run this mojo against an active server.
   *
   * @parameter
   */
  private boolean server = false;

  /**
   * Instantiates a {@link TerminologyRf2SnapshotSampler} from the specified
   * parameters.
   * 
   */
  public TerminologyRf2SnapshotSampler() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("RF2 Snapshot Terminology Sampler called via mojo.");
      getLog().info("  Input directory    : " + inputDir);
      getLog().info("  Input file    : " + inputFile);
      getLog().info("  Expect server up   : " + server);

      // Properties properties = ConfigUtility.getConfigProperties();

      boolean serverRunning = ConfigUtility.isServerActive();

      getLog()
          .info("Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));

      if (serverRunning && !server) {
        throw new MojoFailureException(
            "Mojo expects server to be down, but server is running");
      }

      if (!serverRunning && server) {
        throw new MojoFailureException(
            "Mojo expects server to be running, but server is down");
      }

      // Get initial concepts from file
      File inputFileFile = new File(inputFile);
      if (!inputFileFile.exists()) {
        throw new Exception("Specified input file does not exist");
      }

      Set<String> inputConcepts = new HashSet<>();
      BufferedReader in = new BufferedReader(new FileReader(inputFileFile));
      String line;
      while ((line = in.readLine()) != null) {
        inputConcepts.add(line);
      }
      in.close();
      getLog().info("  Input concepts = " + inputConcepts);

      // Check the input directory
      File inputDirFile = new File(inputDir);
      if (!inputDirFile.exists()) {
        throw new Exception("Specified input directory does not exist");
      }

      // Sort and open RF2 files
      getLog().info("  Sort RF2 Files");
      Rf2FileSorter sorter = new Rf2FileSorter();
      sorter = new Rf2FileSorter();
      sorter.setSortByEffectiveTime(true);
      sorter.setRequireAllFiles(true);
      File sortDir = new File(inputDirFile, "/RF2-sorted-temp/");
      // sorter.sortFiles(inputDirFile, sortDir);

      // Open readers
      getLog().info("  Open RF2 Readers");
      Rf2Readers readers = new Rf2Readers(sortDir);
      readers.openReaders();

      // Load initial snapshot - first release version
      getLog().info("  Run RF2 sampling algorithm");
      Rf2SnapshotSamplerAlgorithm algorithm = new Rf2SnapshotSamplerAlgorithm();
      algorithm.setLastModifiedBy("admin");
      algorithm.setKeepInferred(true);
      algorithm.setKeepDescendants(true);
      algorithm.setReaders(readers);
      algorithm.setInputConcepts(inputConcepts);
      algorithm.compute();
      algorithm.close();
      readers.closeReaders();

      // Copy Files
      Rf2FileCopier copier = new Rf2FileCopier();
      // Parameterize this!
      copier.setActiveOnly(false);
      copier.copyFiles(inputDirFile, new File(outputDir),
          algorithm.getOutputConcepts(), algorithm.getOutputDescriptions());

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
