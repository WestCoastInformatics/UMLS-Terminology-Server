/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.services.helpers.ProgressEvent;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Unit testing for {@link ProgressEvent}.
 */
public class ProgressEventTest extends IntegrationUnitSupport {

  /** The helper object to test. */
  private ProgressEvent object;

  /** The note. */
  private String note = "";

  /** The percent. */
  private int percent = 0;

  /** The progress. */
  private long progress = 0;

  /** The source. */
  private Object source = new Object();

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   */
  @Before
  public void setup() {
    // n/a
  }

  /**
   * /** Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    note = "500 of 1000 completed";
    percent = 50;
    progress = 500;
    source = new Object();

    object = new ProgressEvent(source, percent, progress, note);
    assertEquals(object.getNote(), note);
    assertEquals(object.getPercent(), percent);
    assertEquals(object.getProgress(), progress);
    assertEquals(object.getSource(), source);
    assertEquals(object.getScaledPercent(50, 100), 75);
    assertEquals(object.getScaledPercent(80, 100), 90);
  }

  /*
   * Test degenerate use of the helper object.
   * 
   * @throws Exception the exception
   */
  /**
   * Test helper degenerate use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperDegenerateUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // note is null - TEST: no exceptions expected
    note = null;
    percent = 50;
    progress = 500;
    object = new ProgressEvent(source, percent, progress, note);
    assertEquals(object.getNote(), note);
    assertEquals(object.getPercent(), percent);
    assertEquals(object.getProgress(), progress);
    assertEquals(object.getSource(), source);
    assertEquals(object.getScaledPercent(50, 100), 75);

    // source is null - TEST: no exceptions expected
    note = null;
    source = null;
    percent = 50;
    progress = 500;
    object = new ProgressEvent(source, percent, progress, note);
    assertEquals(object.getNote(), note);
    assertEquals(object.getPercent(), percent);
    assertEquals(object.getProgress(), progress);
    assertEquals(object.getSource(), source);
    assertEquals(object.getScaledPercent(50, 100), 75);
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // lower bounds test - TEST: no exceptions expected
    note = "";
    percent = 0;
    progress = 0;
    source = new Object();
    object = new ProgressEvent(source, percent, progress, note);
    assertEquals(object.getNote(), note);
    assertEquals(object.getPercent(), percent);
    assertEquals(object.getProgress(), progress);
    assertEquals(object.getSource(), source);
    assertEquals(object.getScaledPercent(50, 100), 50);
    assertEquals(object.getScaledPercent(0, 50), 0);
    assertEquals(object.getScaledPercent(0, 0), 0);

    // upper bounds test - TEST: no exceptions expected
    note = "";
    percent = 100;
    progress = 1000;
    source = new Object();
    object = new ProgressEvent(source, percent, progress, note);
    assertEquals(object.getNote(), note);
    assertEquals(object.getPercent(), percent);
    assertEquals(object.getProgress(), progress);
    assertEquals(object.getSource(), source);
    assertEquals(object.getScaledPercent(50, 100), 100);
    assertEquals(object.getScaledPercent(0, 50), 50);
    assertEquals(object.getScaledPercent(100, 100), 100);
  }

  /**
   * Teardown.
   */
  @After
  public void teardown() {
    // do nothing
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
