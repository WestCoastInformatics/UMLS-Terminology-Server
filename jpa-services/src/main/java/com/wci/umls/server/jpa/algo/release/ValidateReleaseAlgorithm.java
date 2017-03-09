/*
 *    Copyright 2017 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;

/**
 * Algorithm to write the RRF index files.
 */
public class ValidateReleaseAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /** The error flag. */
  boolean errorFlag = false;

  /** Log bridge for collecting output */
  private PrintWriter logBridge = new PrintWriter(new StringWriter()) {

    /* see superclass */
    @Override
    public void println(String line) {
      try {
        logInfo("    " + line);
        if (line.startsWith("ERROR")) {
          errorFlag = true;
        } else if (line.startsWith("WARNING")) {
          ValidateReleaseAlgorithm.this.fireWarningEvent(line);
        }
        commitClearBegin();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  /**
   * Instantiates an empty {@link ValidateReleaseAlgorithm}.
   *
   * @throws Exception the exception
   */
  public ValidateReleaseAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("VALIDATERELEASE");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();

    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    final File pathRelease = new File(path, getProcess().getVersion());

    // Expect that "path/$release/META exists"
    if (!new File(pathRelease, "META").exists()) {
      throw new Exception("Unexpected missing file = " + pathRelease + "/META");
    }

    // Expect that "path/$release/METASUBSET exists"
    if (!new File(pathRelease, "METASUBSET").exists()) {
      throw new Exception(
          "Unexpected missing file = " + pathRelease + "/METASUBSET");
    }

    return result;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    // Run "qa_checks.csh"
    logInfo("  Validate target files");
    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    final File pathRelease = new File(path, getProcess().getVersion());

    final ReleaseInfo previousRelease =
        getPreviousReleaseInfo(getProcess().getTerminology());
    final String binDir = ConfigUtility.getHomeDirs().get("bin");
    final String cmd = binDir + "/qa_checks.csh";
    final String meta = pathRelease.getPath() + "/META";
    final String prevMeta = config.getProperty("source.data.dir") + "/mr/"
        + previousRelease.getVersion() + "/META";
    final String[] targets = {
        "MRAUI", "AMBIG", "MRHIST", "MRMAP", "MRCONSO", "MRCUI", "MRHIER",
        "MRDEF", "MRFILESCOLS", "MRRANK", "MRREL", "MRSAB", "MRSAT", "MRSTY",
        "MRDOC", "MRX"
    };
    for (final String target : targets) {
      logInfo("  VALIDATE " + target);
      ConfigUtility.exec(new String[] {
          cmd, meta, target, prevMeta
      }, new String[] {}, false, binDir, logBridge, true);
    }
    if (errorFlag) {
      throw new Exception("Unexpected qa error.");
    }
    logInfo("Finishing " + getName());
    commitClearBegin();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());

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
