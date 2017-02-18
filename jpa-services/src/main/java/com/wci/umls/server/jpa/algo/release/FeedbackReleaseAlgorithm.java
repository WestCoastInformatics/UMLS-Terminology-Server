/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.model.content.Atom;

/**
 * Algorithm for updating atoms based on release feedback.
 */
public class FeedbackReleaseAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /** The update count. */
  private int updateCount = 0;

  private File mrDirFile = null;

  /**
   * Instantiates an empty {@link FeedbackReleaseAlgorithm}.
   *
   *
   * @throws Exception the exception
   */
  public FeedbackReleaseAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("FEEDBACKRELEASE");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    final ValidationResult result = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Algorithm requires a project to be set");
    }

    // Check the mr directory
    String mrPath = config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion()
        + "/META";

    mrDirFile = new File(mrPath);
    if (mrDirFile.exists()) {
      throw new Exception(
          "Specified input directory does not exist = " + mrPath);
    }

    return result;
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    fireProgressEvent(0, "Starting");

    //
    // Load the MRCONSO.RRF file in the mr/[version]/META folder
    //
    final List<String> lines =
        loadFileIntoStringList(mrDirFile, "MRCONSO.RRF", null, null);

    // Set the number of steps to the number of lines to be processed
    setSteps(lines.size());

    final String fields[] = new String[19];

    for (final String line : lines) {

      // Check for a cancelled call once every 100 lines
      if (getStepsCompleted() % 100 == 0) {
        checkCancel();
      }

      FieldedStringTokenizer.split(line, "|", 19, fields);

      // Field Description
      // 0 CUI
      // 1 LAT
      // 2 TS
      // 3 LUI
      // 4 STT
      // 5 SUI
      // 6 ISPREF
      // 7 AUI
      // 8 SAUI
      // 9 SCUI
      // 10 SDUI
      // 11 SAB
      // 12 TTY
      // 13 CODE
      // 14 STR
      // 15 SRL
      // 16 SUPPRESS
      // 17 CVF
      //
      // e.g.
      // C0000005|ENG|P|L0000005|PF|S0007492|Y|A7755565||M0019694|D012711|MSH|PEN|D012711|(131)I-Macroaggregated
      // Albumin|0|N|256|

      // Get the atom
      final Atom atom = (Atom) getComponent("AUI", fields[7], fields[12], null);

      boolean atomChanged = false;

      // Update the alternate concept id
      if (!fields[0].equals(
          atom.getConceptTerminologyIds().get(getProject().getTerminology()))) {
        atom.getConceptTerminologyIds().put(getProject().getTerminology(),
            fields[0]);
        atomChanged = true;
      }

      // Update the last release rank
      String ts = fields[2];
      String stt = fields[4];
      if (ts.equals("P") && stt.equals("PF")
          && !atom.getLastPublishedRank().equals("4")) {
        atom.setLastPublishedRank("4");
        atomChanged = true;
      } else if (ts.equals("S") && stt.equals("PF")
          && !atom.getLastPublishedRank().equals("2")) {
        atom.setLastPublishedRank("2");
        atomChanged = true;
      } else if (ts.equals("P") && stt.startsWith("V")
          && !atom.getLastPublishedRank().equals("3")) {
        atom.setLastPublishedRank("3");
        atomChanged = true;
      } else if (ts.equals("S") && stt.startsWith("V")
          && !atom.getLastPublishedRank().equals("1")) {
        atom.setLastPublishedRank("1");
        atomChanged = true;
      }

      // Update the atom if anything was changed
      if (atomChanged) {
        updateAtom(atom);
        updateCount++;
      }

      // Update the progress
      updateProgress();
    }

    commitClearBegin();

    fireProgressEvent(100, "Finished - 100%");
    logInfo("  atoms updated = " + updateCount);
    logInfo("Finished " + getName());

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // No reset, this can be safely re-run
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
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    return params;
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
