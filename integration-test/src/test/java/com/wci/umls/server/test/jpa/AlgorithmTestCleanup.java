/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class AlgorithmTestCleanup extends IntegrationUnitSupport {

  /**
   * Setup class.
   */
  @BeforeClass
  public static void setupClass() {
    // do nothing
  }

  /**
   * Setup.
   *
   * @throws Exception the exception
   */
  @Before
  public void setup() throws Exception {

  }

  /**
   * Clean out objects from other tests-gone-awry
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  @Test
  public void testCleanup() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    ContentService contentService = new ContentServiceJpa();
    contentService.setMolecularActionFlag(false);
    contentService.setLastModifiedBy("admin");

    //
    // Set the object Ids
    //

    List<Integer> addedAtomIds = new ArrayList<>(Arrays.asList());
    List<Integer> addedConceptIds = new ArrayList<>(Arrays.asList());
    List<Integer> addedMapsetIds = new ArrayList<>(Arrays.asList());
    List<Integer> addedAtomSubsetIds = new ArrayList<>(Arrays.asList(32770));
    List<Integer> addedConceptSubsetIds = new ArrayList<>(Arrays.asList(32768,
        32769, 32770, 32771, 32772, 32773, 32774, 32775, 32776));
    List<Integer> addedConceptSubsetMemberIds =
        new ArrayList<>(Arrays.asList());

    //
    // Load the objects
    //

    List<Atom> addedAtoms = new ArrayList<>();
    for (Integer id : addedAtomIds) {
      Long idLong = id.longValue();
      Atom atom = contentService.getAtom(idLong);
      if (atom != null) {
        addedAtoms.add(atom);
      }
    }

    List<Concept> addedConcepts = new ArrayList<>();
    for (Integer id : addedConceptIds) {
      Long idLong = id.longValue();
      Concept concept = contentService.getConcept(idLong);
      if (concept != null) {
        addedConcepts.add(concept);
      }
    }

    List<MapSet> addedMapSets = new ArrayList<>();
    for (Integer id : addedMapsetIds) {
      Long idLong = id.longValue();
      MapSet mapSet = contentService.getMapSet(idLong);
      if (mapSet != null) {
        addedMapSets.add(mapSet);
      }
    }

    List<Subset> addedSubsets = new ArrayList<>();
    for (Integer id : addedAtomSubsetIds) {
      Long idLong = id.longValue();
      Subset atomSubset = contentService.getSubset(idLong, AtomSubsetJpa.class);
      if (atomSubset != null) {
        addedSubsets.add(atomSubset);
      }
    }
    for (Integer id : addedConceptSubsetIds) {
      Long idLong = id.longValue();
      Subset conceptSubset =
          contentService.getSubset(idLong, ConceptSubsetJpa.class);
      if (conceptSubset != null) {
        addedSubsets.add(conceptSubset);
      }
    }

    List<ConceptSubsetMember> addedConceptSubsetMembers = new ArrayList<>();
    for (Integer id : addedConceptSubsetMemberIds) {
      Long idLong = id.longValue();
      ConceptSubsetMember conceptSubsetMember =
          (ConceptSubsetMember) contentService.getSubsetMember(idLong,
              ConceptSubsetMemberJpa.class);
      if (conceptSubsetMember != null) {
        addedConceptSubsetMembers.add(conceptSubsetMember);
      }
    }

    //
    // Remove the objects (removing their sub-objects first)
    //

    for (MapSet addedMapSet : addedMapSets) {
      List<Mapping> mappings = addedMapSet.getMappings();
      for (Mapping mapping : mappings) {
        contentService.removeMapping(mapping.getId());
      }
      addedMapSet.clearMappings();
      contentService.updateMapSet(addedMapSet);
      contentService.removeMapSet(addedMapSet.getId());
    }

    for (Subset addedSubset : addedSubsets) {
      if (addedSubset instanceof AtomSubsetJpa) {
        for (SubsetMember subsetMember : ((AtomSubsetJpa) addedSubset)
            .getMembers()) {
          subsetMember.getAttributes().clear();
          contentService.updateSubsetMember((AtomSubsetMemberJpa) subsetMember);
          for (Attribute attribute : subsetMember.getAttributes()) {
            contentService.removeAttribute(attribute.getId());
          }
          contentService.removeSubsetMember(subsetMember.getId(),
              AtomSubsetMemberJpa.class);
        }
        contentService.removeSubset(addedSubset.getId(), AtomSubsetJpa.class);
      }

      if (addedSubset instanceof ConceptSubsetJpa) {
        for (SubsetMember subsetMember : ((ConceptSubsetJpa) addedSubset)
            .getMembers()) {
          subsetMember.getAttributes().clear();
          contentService
              .updateSubsetMember((ConceptSubsetMemberJpa) subsetMember);
          for (Attribute attribute : subsetMember.getAttributes()) {
            contentService.removeAttribute(attribute.getId());
          }
          contentService.removeSubsetMember(subsetMember.getId(),
              ConceptSubsetMemberJpa.class);
        }
        contentService.removeSubset(addedSubset.getId(),
            ConceptSubsetJpa.class);
      }
    }

    for (final Atom addedAtom : addedAtoms) {
      Code code = contentService.getCode(addedAtom.getCodeId(),
          addedAtom.getTerminology(), addedAtom.getVersion(), Branch.ROOT);
      if (code != null) {
        code.getAtoms().remove(addedAtom);
        contentService.updateCode(code);
      }
      Descriptor descriptor =
          contentService.getDescriptor(addedAtom.getDescriptorId(),
              addedAtom.getTerminology(), addedAtom.getVersion(), Branch.ROOT);
      if (descriptor != null) {
        descriptor.getAtoms().remove(addedAtom);
        contentService.updateCode(code);
      }

      for (Concept addedConcept : new ArrayList<>(addedConcepts)) {
        if (addedConcept.getAtoms().contains(addedAtom)) {
          addedConcept.getAtoms().remove(addedAtom);
          contentService.updateConcept(addedConcept);
          contentService.removeConcept(addedConcept.getId());
          addedConcepts.remove(addedConcept);
        }
      }
      contentService.removeAtom(addedAtom.getId());
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {

  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
