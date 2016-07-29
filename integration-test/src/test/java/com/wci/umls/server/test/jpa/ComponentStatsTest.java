/*
 *    Copyright 2016 West Coast Informatics, LLC
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
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working
 */
public class ComponentStatsTest extends IntegrationUnitSupport {

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
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Map<String, Integer> umlsStats =
        service.getComponentStats("UMLS", "latest", Branch.ROOT);

    Map<String, Integer> cmpStats = new TreeMap<>();
    cmpStats.put("Non-obsolete AtomJpa", 0);
    cmpStats.put("Non-obsolete AtomRelationshipJpa", 0);
    cmpStats.put("Non-obsolete AtomSubsetJpa", 0);
    cmpStats.put("Non-obsolete AtomSubsetMemberJpa", 0);
    cmpStats.put("Non-obsolete AttributeJpa", 17135);
    cmpStats.put("Non-obsolete CodeJpa", 0);
    cmpStats.put("Non-obsolete CodeRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ConceptJpa", 5596);
    cmpStats.put("Non-obsolete ConceptRelationshipJpa", 3754);
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
    cmpStats.put("Non-obsolete SemanticTypeComponentJpa", 7390);
    cmpStats.put("Non-obsolete StringClassJpa", 0);
    cmpStats.put("Non-obsolete AtomTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete AtomTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ComponentHistoryJpa", 0);
    cmpStats.put("Non-obsolete ComponentInfoRelationshipJpa", 0);
    cmpStats.put("Non-obsolete GeneralConceptAxiomJpa", 0);
    cmpStats.put("Non-obsolete MapSetJpa", 1);
    cmpStats.put("Non-obsolete MappingJpa", 52);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 242);
    cmpStats.put("Total AtomJpa", 0);
    cmpStats.put("Total AtomRelationshipJpa", 0);
    cmpStats.put("Total AtomSubsetJpa", 0);
    cmpStats.put("Total AtomSubsetMemberJpa", 0);
    cmpStats.put("Total AttributeJpa", 17135);
    cmpStats.put("Total AttributeNameJpa", 447);
    cmpStats.put("Total CodeJpa", 0);
    cmpStats.put("Total CodeRelationshipJpa", 0);
    cmpStats.put("Total CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Total CodeTreePositionJpa", 0);
    cmpStats.put("Total ConceptJpa", 5596);
    cmpStats.put("Total ConceptRelationshipJpa", 3754);
    cmpStats.put("Total ConceptSubsetJpa", 0);
    cmpStats.put("Total ConceptSubsetMemberJpa", 0);
    cmpStats.put("Total ConceptTransitiveRelationshipJpa", 0);
    cmpStats.put("Total ConceptTreePositionJpa", 0);
    cmpStats.put("Total DefinitionJpa", 0);
    cmpStats.put("Total DescriptorJpa", 0);
    cmpStats.put("Total DescriptorRelationshipJpa", 0);
    cmpStats.put("Total DescriptorTransitiveRelationshipJpa", 0);
    cmpStats.put("Total DescriptorTreePositionJpa", 0);
    cmpStats.put("Total GeneralMetadataEntryJpa", 258);
    cmpStats.put("Total LanguageJpa", 25);
    cmpStats.put("Total LexicalClassJpa", 0);
    cmpStats.put("Total PropertyChainJpa", 0);
    cmpStats.put("Total RelationshipTypeJpa", 11);
    cmpStats.put("Total SemanticTypeComponentJpa", 7390);
    cmpStats.put("Total SemanticTypeJpa", 133);
    cmpStats.put("Total StringClassJpa", 0);
    cmpStats.put("Total TermTypeJpa", 174);
    cmpStats.put("Total AtomTransitiveRelationshipJpa", 0);
    cmpStats.put("Total AtomTreePositionJpa", 0);
    cmpStats.put("Total ComponentHistoryJpa", 0);
    cmpStats.put("Total ComponentInfoRelationshipJpa", 0);
    cmpStats.put("Total GeneralConceptAxiomJpa", 0);
    cmpStats.put("Total LabelSetJpa", 0);
    cmpStats.put("Total MapSetJpa", 1);
    cmpStats.put("Total MappingJpa", 52);

    // System.out.println("umls keys");
    // umlsStats.entrySet().stream().map(k -> k.getKey() + ", " + k.getValue())
    // .forEach(System.out::println);
    // System.out.println("cmp keys");
    // cmpStats.keySet().stream().forEach(System.out::println);
    assertEquals(cmpStats.keySet().size(), umlsStats.keySet().size());
    for (final String key : cmpStats.keySet()) {
      Logger.getLogger(getClass()).info("    checking " + key);
      // if (!cmpStats.get(key).equals(umlsStats.get(key))) {
      // System.out.println("DIFF: " + key + " " + cmpStats.get(key) + " -> "
      // + umlsStats.get(key));
      // }

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
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Map<String, Integer> snomedStats =
        service.getComponentStats("SNOMEDCT_US", "2016_03_01", Branch.ROOT);
    Map<String, Integer> cmpStats = new TreeMap<>();

    cmpStats.put("Non-obsolete AtomJpa", 5660);
    cmpStats.put("Non-obsolete AtomRelationshipJpa", 52);
    cmpStats.put("Non-obsolete AtomSubsetJpa", 4);
    cmpStats.put("Non-obsolete AtomSubsetMemberJpa", 24981);
    cmpStats.put("Non-obsolete AttributeJpa", 100219);
    cmpStats.put("Non-obsolete CodeJpa", 3902);
    cmpStats.put("Non-obsolete CodeRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete CodeTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ConceptJpa", 3902);
    cmpStats.put("Non-obsolete ConceptRelationshipJpa", 7530);
    cmpStats.put("Non-obsolete ConceptSubsetJpa", 16);
    cmpStats.put("Non-obsolete ConceptSubsetMemberJpa", 14714);
    cmpStats.put("Non-obsolete ConceptTransitiveRelationshipJpa", 2729);
    cmpStats.put("Non-obsolete ConceptTreePositionJpa", 1233);
    cmpStats.put("Non-obsolete DefinitionJpa", 9);
    cmpStats.put("Non-obsolete DescriptorJpa", 0);
    cmpStats.put("Non-obsolete DescriptorRelationshipJpa", 0);
    cmpStats.put("Non-obsolete DescriptorTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete DescriptorTreePositionJpa", 0);
    cmpStats.put("Non-obsolete LexicalClassJpa", 0);
    cmpStats.put("Non-obsolete SemanticTypeComponentJpa", 929);
    cmpStats.put("Non-obsolete StringClassJpa", 0);
    cmpStats.put("Non-obsolete AtomTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete AtomTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ComponentHistoryJpa", 0);
    cmpStats.put("Non-obsolete ComponentInfoRelationshipJpa", 1550);
    cmpStats.put("Non-obsolete GeneralConceptAxiomJpa", 0);
    cmpStats.put("Non-obsolete MapSetJpa", 1);
    cmpStats.put("Non-obsolete MappingJpa", 28);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 0);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 0);
    cmpStats.put("Total AtomJpa", 13196);
    cmpStats.put("Total AtomRelationshipJpa", 52);
    cmpStats.put("Total AtomSubsetJpa", 4);
    cmpStats.put("Total AtomSubsetMemberJpa", 31429);
    cmpStats.put("Total AttributeJpa", 150795);
    cmpStats.put("Total AttributeNameJpa", 0);
    cmpStats.put("Total CodeJpa", 3902);
    cmpStats.put("Total CodeRelationshipJpa", 0);
    cmpStats.put("Total CodeTransitiveRelationshipJpa", 0);
    cmpStats.put("Total CodeTreePositionJpa", 0);
    cmpStats.put("Total ConceptJpa", 3902);
    cmpStats.put("Total ConceptRelationshipJpa", 18412);
    cmpStats.put("Total ConceptSubsetJpa", 17);
    cmpStats.put("Total ConceptSubsetMemberJpa", 15948);
    cmpStats.put("Total ConceptTransitiveRelationshipJpa", 2729);
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
    cmpStats.put("Total SemanticTypeComponentJpa", 929);
    cmpStats.put("Total SemanticTypeJpa", 194);
    cmpStats.put("Total StringClassJpa", 0);
    cmpStats.put("Total TermTypeJpa", 0);
    cmpStats.put("Total AtomTransitiveRelationshipJpa", 0);
    cmpStats.put("Total AtomTreePositionJpa", 0);
    cmpStats.put("Total ComponentHistoryJpa", 0);
    cmpStats.put("Total ComponentInfoRelationshipJpa", 1552);
    cmpStats.put("Total GeneralConceptAxiomJpa", 0);
    cmpStats.put("Total LabelSetJpa", 0);
    cmpStats.put("Total MapSetJpa", 1);
    cmpStats.put("Total MappingJpa", 334);

    // System.out.println("snomed keys");
    // snomedStats.entrySet().stream().map(k -> k.getKey() + ", " +
    // k.getValue())
    // .forEach(System.out::println);
    // System.out.println("cmp keys");
    // cmpStats.keySet().stream().forEach(System.out::println);
    assertEquals(cmpStats.keySet().size(), snomedStats.keySet().size());
    for (final String key : cmpStats.keySet()) {
      Logger.getLogger(getClass()).info("    checking " + key);
      // if (!cmpStats.get(key).equals(snomedStats.get(key))) {
      // System.out.println("DIFF: " + key + " " + cmpStats.get(key) + " -> "
      // + snomedStats.get(key));
      // }
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
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    Map<String, Integer> mshStats =
        service.getComponentStats("MSH", "2016_2016_02_26", Branch.ROOT);
    Map<String, Integer> cmpStats = new TreeMap<>();

    cmpStats.put("Non-obsolete AtomJpa", 6096);
    cmpStats.put("Non-obsolete AtomRelationshipJpa", 6584);
    cmpStats.put("Non-obsolete AtomSubsetJpa", 0);
    cmpStats.put("Non-obsolete AtomSubsetMemberJpa", 0);
    cmpStats.put("Non-obsolete AttributeJpa", 17663);
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
    cmpStats.put("Non-obsolete DescriptorTransitiveRelationshipJpa", 1354);
    cmpStats.put("Non-obsolete DescriptorTreePositionJpa", 591);
    cmpStats.put("Non-obsolete LexicalClassJpa", 0);
    cmpStats.put("Non-obsolete SemanticTypeComponentJpa", 0);
    cmpStats.put("Non-obsolete StringClassJpa", 0);
    cmpStats.put("Non-obsolete AtomTransitiveRelationshipJpa", 0);
    cmpStats.put("Non-obsolete AtomTreePositionJpa", 0);
    cmpStats.put("Non-obsolete ComponentHistoryJpa", 0);
    cmpStats.put("Non-obsolete ComponentInfoRelationshipJpa", 226);
    cmpStats.put("Non-obsolete GeneralConceptAxiomJpa", 0);
    cmpStats.put("Non-obsolete MapSetJpa", 0);
    cmpStats.put("Non-obsolete MappingJpa", 0);
    cmpStats.put("Total AdditionalRelationshipTypeJpa", 0);
    cmpStats.put("Total AtomJpa", 6096);
    cmpStats.put("Total AtomRelationshipJpa", 6584);
    cmpStats.put("Total AtomSubsetJpa", 0);
    cmpStats.put("Total AtomSubsetMemberJpa", 0);
    cmpStats.put("Total AttributeJpa", 17663);
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
    cmpStats.put("Total DescriptorTransitiveRelationshipJpa", 1354);
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
    cmpStats.put("Total AtomTransitiveRelationshipJpa", 0);
    cmpStats.put("Total AtomTreePositionJpa", 0);
    cmpStats.put("Total ComponentHistoryJpa", 0);
    cmpStats.put("Total ComponentInfoRelationshipJpa", 226);
    cmpStats.put("Total GeneralConceptAxiomJpa", 0);
    cmpStats.put("Total LabelSetJpa", 0);
    cmpStats.put("Total MapSetJpa", 0);
    cmpStats.put("Total MappingJpa", 0);

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
