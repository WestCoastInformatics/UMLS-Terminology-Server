/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Test for closing and opening Jpa service factory.
 */
public class CloseReopenFactoryTest extends IntegrationUnitSupport {
  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Test UMLS stats.
   *
   * @throws Exception the exception
   */
  @Test
  public void testCloseOpen() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ProjectServiceJpa service = new ProjectServiceJpa();
    service.close();
    service.closeFactory();
    service.openFactory();
    service = new ProjectServiceJpa();
    service.getProjects();
    service.close();

    // Result is to just get through this all
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
