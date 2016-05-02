/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.helpers;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.wci.umls.server.jpa.services.ecl.EclUtility;

/**
 * Helper testing class for PfsParameter concept tests.
 */
public class EclUtilityTest {

  /**
   * Test find concepts for query.
   *
   * @throws Exception the exception
   */
  @Test
  public void testQuery() throws Exception {
    // Self 404684003 |clinical finding| Yes
    try {
      Logger.getLogger(getClass())
          .info(EclUtility.parse("404684003 |clinical finding|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }
    // Descendant Of < 404684003 |clinical finding| Yes
    try {
      Logger.getLogger(getClass())
          .info(EclUtility.parse("< 404684003 |clinical finding|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }
    // Descendant Or Self Of << 73211009 |diabetes mellitus| Yes
    try {
      Logger.getLogger(getClass())
          .info(EclUtility.parse("<< 73211009 |diabetes mellitus|"));
    } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }

    // Nested Attribute < 404684003 |clinical finding|: 47429007 |associated
    // with| = (< 404684003 |clinical finding|: 116676008 |associated
    // morphology| = << 55641003 |infarct|) No
    try {
      Logger.getLogger(getClass()).info(EclUtility.parse(
          "< 19829001 |disorder of lung|: 116676008 |associated morphology| = 79654002 |edema|"));
      } catch (Exception e) {
      Logger.getLogger(getClass()).info(e.getMessage());
    }
  }

}
