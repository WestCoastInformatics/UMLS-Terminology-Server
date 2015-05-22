/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;

/**
 * Sample test to get auto complete working
 */
public class ComponentStatsTest {

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
   * Test UMLS stats.
   *
   * @throws Exception the exception
   */
  @Test
  public void testUmlsComponentStats() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Map<String, Integer> umlsStats =
        service.getComponentStats("UMLS", "latest", Branch.ROOT);

    Map<String, Integer> cmpStats = new TreeMap<>();
    cmpStats.put("Non-obsolete AtomJpa", 0);
    cmpStats.put("Non-obsolete AtomRelationshipJpa", 0);
    cmpStats.put("Non-obsolete AtomSubsetJpa", 0);
    cmpStats.put("Non-obsolete AtomSubsetMemberJpa", 0);
    cmpStats.put("Non-obsolete AttributeJpa", 0);
    cmpStats.put("Non-obsolete CodeJpa", 0);
    cmpStats.put("Non-obsolete CodeRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ConceptJpa", 2013);
    cmpStats.put("Non-obsolete ConceptRelationshipJpa", 0);
    cmpStats.put("Non-obsolete ConceptSubsetJpa", 0);
    cmpStats.put("Non-obsolete ConceptSubsetMemberJpa", 0);
    cmpStats.put("Non-obsolete ConceptTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete ConceptTreePositionJpa", 0);
    cmpStats.put("Non-obsolete DefinitionJpa", 0);
    cmpStats.put("Non-obsolete DescriptorJpa", 0);
    cmpStats.put("Non-obsolete DescriptorRelationshipJpa", 0);
    cmpStats.put("Non-obsolete DescriptorTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete DescriptorTreePositionJpa", 0);
    cmpStats.put("Non-obsolete LexicalClassJpa", 0);
    cmpStats.put("Non-obsolete SemanticTypeComponentJpa", 2265);
    cmpStats.put("Non-obsolete StringClassJpa", 0);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 82);
    cmpStats.put("Total AtomJpa", 0);
    cmpStats.put("Total AtomRelationshipJpa", 0);
    cmpStats.put("Total AtomSubsetJpa", 0);
    cmpStats.put("Total AtomSubsetMemberJpa", 0);
    cmpStats.put("Total AttributeJpa", 0);
    cmpStats.put("Total AttributeNameJpa", 98);
    cmpStats.put("Total CodeJpa", 0);
    cmpStats.put("Total CodeRelationshipJpa", 0);
    cmpStats.put("Total CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Total CodeTreePositionJpa", 0);
    cmpStats.put("Total ConceptJpa", 2013);
    cmpStats.put("Total ConceptRelationshipJpa", 0);
    cmpStats.put("Total ConceptSubsetJpa", 0);
    cmpStats.put("Total ConceptSubsetMemberJpa", 0);
    cmpStats.put("Total ConceptTransitiveRelationshipJpa", 0);
    cmpStats.put("Total ConceptTreePositionJpa", 0);
    cmpStats.put("Total DefinitionJpa", 0);
    cmpStats.put("Total DescriptorJpa", 0);
    cmpStats.put("Total DescriptorRelationshipJpa", 0);
    cmpStats.put("Total DescriptorTransitiveRelationshipJpa", 0);
    cmpStats.put("Total DescriptorTreePositionJpa", 0);
    cmpStats.put("Total GeneralMetadataEntryJpa", 255);
    cmpStats.put("Total LanguageJpa", 21);
    cmpStats.put("Total LexicalClassJpa", 0);
    cmpStats.put("Total PropertyChainJpa", 0);
    cmpStats.put("Total RelationshipTypeJpa", 9);
    cmpStats.put("Total SemanticTypeComponentJpa", 2265);
    cmpStats.put("Total SemanticTypeJpa", 133);
    cmpStats.put("Total StringClassJpa", 0);
    cmpStats.put("Total TermTypeJpa", 47);

    assertEquals(cmpStats.keySet().size(), umlsStats.keySet().size());
    for (final String key : cmpStats.keySet()) {
      Logger.getLogger(getClass()).info("    checking " + key);
      assertEquals(cmpStats.get(key), umlsStats.get(key));
    }
  }

  /**
   * Test SNOMEDCT_US stats.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSnomedComponentStats() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Map<String, Integer> snomedStats =
        service.getComponentStats("SNOMEDCT_US", "2014_09_01", Branch.ROOT);
    Map<String, Integer> cmpStats = new TreeMap<>();

    cmpStats.put("Non-obsolete AtomJpa", 5660);
    cmpStats.put("Non-obsolete AtomRelationshipJpa", 52);
    cmpStats.put("Non-obsolete AtomSubsetJpa", 3);
    cmpStats.put("Non-obsolete AtomSubsetMemberJpa", 20107);
    cmpStats.put("Non-obsolete AttributeJpa", 93818);
    cmpStats.put("Non-obsolete CodeJpa", 3902);
    cmpStats.put("Non-obsolete CodeRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ConceptJpa", 3902);
    cmpStats.put("Non-obsolete ConceptRelationshipJpa", 7530);
    cmpStats.put("Non-obsolete ConceptSubsetJpa", 13);
    cmpStats.put("Non-obsolete ConceptSubsetMemberJpa", 14294);
    cmpStats.put("Non-obsolete ConceptTransitiveRelationshipJpa", 2142);
    cmpStats.put("Non-obsolete ConceptTreePositionJpa", 1233);
    cmpStats.put("Non-obsolete DefinitionJpa", 9);
    cmpStats.put("Non-obsolete DescriptorJpa", 0);
    cmpStats.put("Non-obsolete DescriptorRelationshipJpa", 0);
    cmpStats.put("Non-obsolete DescriptorTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete DescriptorTreePositionJpa", 0);
    cmpStats.put("Non-obsolete LexicalClassJpa", 0);
    cmpStats.put("Non-obsolete SemanticTypeComponentJpa", 0);
    cmpStats.put("Non-obsolete StringClassJpa", 0);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 0);
    cmpStats.put("Total AtomJpa", 13196);
    cmpStats.put("Total AtomRelationshipJpa", 52);
    cmpStats.put("Total AtomSubsetJpa", 3);
    cmpStats.put("Total AtomSubsetMemberJpa", 25429);
    cmpStats.put("Total AttributeJpa", 143254);
    cmpStats.put("Total AttributeNameJpa", 0);
    cmpStats.put("Total CodeJpa", 3902);
    cmpStats.put("Total CodeRelationshipJpa", 0);
    cmpStats.put("Total CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Total CodeTreePositionJpa", 0);
    cmpStats.put("Total ConceptJpa", 3902);
    cmpStats.put("Total ConceptRelationshipJpa", 18412);
    cmpStats.put("Total ConceptSubsetJpa", 14);
    cmpStats.put("Total ConceptSubsetMemberJpa", 15514);
    cmpStats.put("Total ConceptTransitiveRelationshipJpa", 2142);
    cmpStats.put("Total ConceptTreePositionJpa", 1233);
    cmpStats.put("Total DefinitionJpa", 12);
    cmpStats.put("Total DescriptorJpa", 0);
    cmpStats.put("Total DescriptorRelationshipJpa", 0);
    cmpStats.put("Total DescriptorTransitiveRelationshipJpa", 0);
    cmpStats.put("Total DescriptorTreePositionJpa", 0);
    cmpStats.put("Total GeneralMetadataEntryJpa", 0);
    cmpStats.put("Total LanguageJpa", 0);
    cmpStats.put("Total LexicalClassJpa", 0);
    cmpStats.put("Total PropertyChainJpa", 0);
    cmpStats.put("Total RelationshipTypeJpa", 0);
    cmpStats.put("Total SemanticTypeComponentJpa", 0);
    cmpStats.put("Total SemanticTypeJpa", 0);
    cmpStats.put("Total StringClassJpa", 0);
    cmpStats.put("Total TermTypeJpa", 0);

    assertEquals(cmpStats.keySet().size(), snomedStats.keySet().size());
    for (final String key : cmpStats.keySet()) {
      Logger.getLogger(getClass()).info("    checking " + key);
      assertEquals(cmpStats.get(key), snomedStats.get(key));
    }
  }

  /**
   * Test MSH stats.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMshComponentStats() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    Map<String, Integer> mshStats =
        service.getComponentStats("MSH", "2015_2014_09_08", Branch.ROOT);
    Map<String, Integer> cmpStats = new TreeMap<>();

    cmpStats.put("Non-obsolete AtomJpa", 6096);
    cmpStats.put("Non-obsolete AtomRelationshipJpa", 6584);
    cmpStats.put("Non-obsolete AtomSubsetJpa", 0);
    cmpStats.put("Non-obsolete AtomSubsetMemberJpa", 0);
    cmpStats.put("Non-obsolete AttributeJpa", 17681);
    cmpStats.put("Non-obsolete CodeJpa", 997);
    cmpStats.put("Non-obsolete CodeRelationshipJpa", 30);
    cmpStats.put("Non-obsolete CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ConceptJpa", 1027);
    cmpStats.put("Non-obsolete ConceptRelationshipJpa", 86);
    cmpStats.put("Non-obsolete ConceptSubsetJpa", 0);
    cmpStats.put("Non-obsolete ConceptSubsetMemberJpa", 0);
    cmpStats.put("Non-obsolete ConceptTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete ConceptTreePositionJpa", 0);
    cmpStats.put("Non-obsolete DefinitionJpa", 816);
    cmpStats.put("Non-obsolete DescriptorJpa", 997);
    cmpStats.put("Non-obsolete DescriptorRelationshipJpa", 43368);
    cmpStats.put("Non-obsolete DescriptorTransitiveRelationshipJpa", 1054);
    cmpStats.put("Non-obsolete DescriptorTreePositionJpa", 591);
    cmpStats.put("Non-obsolete LexicalClassJpa", 0);
    cmpStats.put("Non-obsolete SemanticTypeComponentJpa", 0);
    cmpStats.put("Non-obsolete StringClassJpa", 0);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 0);
    cmpStats.put("Total AtomJpa", 6096);
    cmpStats.put("Total AtomRelationshipJpa", 6584);
    cmpStats.put("Total AtomSubsetJpa", 0);
    cmpStats.put("Total AtomSubsetMemberJpa", 0);
    cmpStats.put("Total AttributeJpa", 17681);
    cmpStats.put("Total AttributeNameJpa", 0);
    cmpStats.put("Total CodeJpa", 997);
    cmpStats.put("Total CodeRelationshipJpa", 30);
    cmpStats.put("Total CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Total CodeTreePositionJpa", 0);
    cmpStats.put("Total ConceptJpa", 1027);
    cmpStats.put("Total ConceptRelationshipJpa", 86);
    cmpStats.put("Total ConceptSubsetJpa", 0);
    cmpStats.put("Total ConceptSubsetMemberJpa", 0);
    cmpStats.put("Total ConceptTransitiveRelationshipJpa", 0);
    cmpStats.put("Total ConceptTreePositionJpa", 0);
    cmpStats.put("Total DefinitionJpa", 816);
    cmpStats.put("Total DescriptorJpa", 997);
    cmpStats.put("Total DescriptorRelationshipJpa", 43368);
    cmpStats.put("Total DescriptorTransitiveRelationshipJpa", 1054);
    cmpStats.put("Total DescriptorTreePositionJpa", 591);
    cmpStats.put("Total GeneralMetadataEntryJpa", 0);
    cmpStats.put("Total LanguageJpa", 0);
    cmpStats.put("Total LexicalClassJpa", 0);
    cmpStats.put("Total PropertyChainJpa", 0);
    cmpStats.put("Total RelationshipTypeJpa", 0);
    cmpStats.put("Total SemanticTypeComponentJpa", 0);
    cmpStats.put("Total SemanticTypeJpa", 0);
    cmpStats.put("Total StringClassJpa", 0);
    cmpStats.put("Total TermTypeJpa", 0);

    assertEquals(cmpStats.keySet().size(), mshStats.keySet().size());
    for (final String key : cmpStats.keySet()) {
      Logger.getLogger(getClass()).info("    checking " + key);
      assertEquals(cmpStats.get(key), mshStats.get(key));
    }
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
