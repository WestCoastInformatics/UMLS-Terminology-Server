/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.AtomRelationshipJpa;
import com.wci.umls.server.jpa.content.AttributeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.content.DefinitionJpa;
import com.wci.umls.server.jpa.content.SemanticTypeComponentJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.services.helpers.ReportHelper;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Unit testing for {@link ReportHelper}.
 */
public class ReportHelperTest extends IntegrationUnitSupport {

  /** The concept. */
  private Concept concept;

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
    concept = new ConceptJpa();
  }

  /**
   * /** Test normal use of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperNormalUse() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // create concept and test if concept report has correct elements
    concept.setId(1L);
    concept.setTerminologyId("1");
    concept.setName("1");

    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("STY = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("DEF ="));

    SemanticTypeComponent sty = new SemanticTypeComponentJpa();
    sty.setId(1L);
    sty.setTerminologyId("1");
    sty.setSemanticType("Clinical Drug");
    concept.getSemanticTypes().add(sty);
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("DEF ="));

    Atom atom = new AtomJpa();
    atom.setId(1L);
    atom.setTerminologyId("1");
    atom.setName("1");
    atom.setTermType("1");
    atom.setConceptId(concept.getTerminologyId());
    concept.getAtoms().add(atom);

    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("DEF ="));

    ConceptRelationship r = new ConceptRelationshipJpa();
    r.setId(1L);
    r.setTerminologyId("1");
    r.setRelationshipType("1");
    r.setFrom(concept);
    r.setTo(concept);
    concept.getRelationships().add(r);
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("DEF ="));

    Definition def = new DefinitionJpa();
    def.setId(1L);
    def.setTerminologyId("1");
    def.setValue("1");
    concept.getDefinitions().add(def);
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("DEF ="));

    concept.getDefinitions().remove(def);
    atom.getDefinitions().add(def);
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));

    assertTrue(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertFalse(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("DEF ="));

    Attribute att = new AttributeJpa();
    att.setId(1L);
    att.setTerminologyId("1");
    att.setValue("1");
    concept.getAttributes().add(att);
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));

    assertTrue(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("DEF ="));

    concept.getAttributes().add(att);
    atom.getAttributes().add(att);
    Logger.getLogger(getClass()).info(ReportHelper.getConceptReport(concept));
    assertTrue(ReportHelper.getConceptReport(concept).contains("CONCEPT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("STY = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("ATOM = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("ATT = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("REL = "));
    assertTrue(ReportHelper.getConceptReport(concept).contains("DEF ="));

    AtomRelationship ar = new AtomRelationshipJpa();
    ar.setId(1L);
    ar.setTerminologyId("1");
    ar.setRelationshipType("1");
    ar.setFrom(atom);
    ar.setTo(atom);
    atom.getRelationships().add(ar);
    Logger.getLogger(getClass()).info(ReportHelper.getAtomReport(atom));
    assertTrue(ReportHelper.getAtomReport(atom).contains("ATOM = "));
    assertTrue(ReportHelper.getAtomReport(atom).contains("ATT = "));
    assertTrue(ReportHelper.getAtomReport(atom).contains("REL = "));
    assertTrue(ReportHelper.getAtomReport(atom).contains("DEF ="));

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
    // provide a null concept
    try {
      ReportHelper.getConceptReport(null);
      fail("Attempt to getConceptReport() on null concept should throw an error.");
    } catch (Exception e) {
      // do nothing
    }

    try {
      ReportHelper.getAtomReport(null);
      fail("Attempt to getAtomReport() on null atom should throw an error.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test edge cases of the helper object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHelperEdgeCases() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    // n/a
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
