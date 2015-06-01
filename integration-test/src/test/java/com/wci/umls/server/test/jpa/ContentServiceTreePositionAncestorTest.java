/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.content.Tree;
import com.wci.umls.server.helpers.content.TreePositionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.AtomClass;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.services.ContentService;

/**
 * Sample test to get auto complete working
 */
public class ContentServiceTreePositionAncestorTest {

  /** The service. */
  ContentService service = null;

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
    service = new ContentServiceJpa();
  }

  /**
   * Test ability to extract a tree based on the ancestor path and id.
   *
   * @throws Exception the exception
   */
  @Test
  public void testAncestorPathConceptTreePosition() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // Start by obtaining tree positions for a concept
    TreePositionList list =
        service.findTreePositionsForConcept("10944007", "SNOMEDCT_US",
            "2014_09_01", Branch.ROOT, new PfsParameterJpa());

    TreePosition<? extends AtomClass> treepos = list.getObjects().get(0);
    Logger.getLogger(getClass()).info(
        " first treepos = " + treepos.getNode().getId() + ", "
            + treepos.getAncestorPath());

    Tree tree =
        service.getTreeForAncestorPath(treepos.getAncestorPath(), treepos
            .getNode().getId());
    Logger.getLogger(getClass()).debug("  tree = " + tree);

    treepos = list.getObjects().get(1);
    Tree tree2 =
        service.getTreeForAncestorPath(treepos.getAncestorPath(), treepos
            .getNode().getId());
    Logger.getLogger(getClass()).debug("  tree2 = " + tree2);

    tree.mergeTree(tree2);
    Logger.getLogger(getClass()).debug("  tree = " + tree);

    // merged tree is bigger
    assertTrue(tree.toString().length() > tree2.toString().length());

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
