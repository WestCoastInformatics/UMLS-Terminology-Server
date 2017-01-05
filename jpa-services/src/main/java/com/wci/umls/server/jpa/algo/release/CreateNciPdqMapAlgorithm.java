/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.Properties;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Algorithm for creating NCI-PDQ map.
 */
public class CreateNciPdqMapAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /**
   * Instantiates an empty {@link CreateNciPdqMapAlgorithm}.
   *
   * @throws Exception the exception
   */
  public CreateNciPdqMapAlgorithm() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting create NCI-PDQ map algorithm");
    fireProgressEvent(0, "Starting");

    // Algorithm (use molecular actions for id assignment).
    // 1. Find any concepts with PDQ/XM atoms
    // * make the atoms of that concept unpublishable
    // * make the codes of that atom unpublishable
    // * save the atom id (in executionInfo) to restore
    // * find and make the old map set unpublishable too
    // 2. Create a new map set concept with a single PDQ/XM atom
    // * "PDQ_$version to NCI_$version Mappings"
    // * codeId = 100001
    // * not obsolete, not suppressible, READY_FOR_PUBLICATION
    // * publishable, not published.
    // * save the atomId (in executionInfo) to remove
    // * create a code too and add atom to it
    // 3. Create a "Intellectual Product" semantic type for the concept
    // 4. Create atom attributes (e.g. from MRSAT) (name, terminology, version)
    // MAPSETVERSION|PDQ|2016_07_31
    // FROMVSAB|PDQ|PDQ_2016_07_31
    // TOVSAB|PDQ|NCI_2016_10E
    // MAPSETVSAB|PDQ|PDQ_2016_07_31
    // MTH_MAPSETCOMPLEXITY|PDQ|N_TO_N
    // TORSAB|PDQ|NCI
    // MTH_MAPTOCOMPLEXITY|PDQ|SINGLE SCUI
    // MAPSETRSAB|PDQ|PDQ
    // MTH_MAPTOEXHAUSTIVE|PDQ|N
    // FROMRSAB|PDQ|PDQ
    // MTH_MAPFROMCOMPLEXITY|PDQ|SINGLE SDUI
    // MTH_MAPFROMEXHAUSTIVE|PDQ|N
    // 5. Create a map set based on the information above
    // 6. Create mappings
    // * join PDQ->NCI in the same project concept, both publishable
    // * use mapRank=1 if term-types are PT->PT, PT->PSC, or PT->HT
    // * use mapRank=2 if term-types are different and from/to map doesn't
    // already exist
    // * this is an descriptorId => conceptId map

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // TODO: remove the map.
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

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      checkCancel();
      fireProgressEvent(currentProgress,
          "ASSIGN RELEASE IDS progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
