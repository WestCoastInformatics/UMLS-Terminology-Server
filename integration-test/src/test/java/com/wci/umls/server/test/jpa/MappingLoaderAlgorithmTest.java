/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.ProcessExecution;
import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.MapSetList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.AtomLoaderAlgorithm;
import com.wci.umls.server.jpa.algo.insert.MappingLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class MappingLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The atom algorithm. */
  AtomLoaderAlgorithm atomAlgo = null;

  /** The mapping algorithm. */
  MappingLoaderAlgorithm mappingAlgo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessServiceJpa processService = null;

  /** The content service. */
  ContentServiceJpa contentService = null;

  /** The project. */
  Project project = null;

  /** The temporary classes_atoms.src file. */
  private File atomOutputFile = null;

  /** The temporary attributes.src file. */
  private File attributesOutputFile = null;

  /** The added atom. */
  private Atom addedAtom = null;

  /** The added concept. */
  private Concept addedConcept = null;

  /** The added concept 2. */
  private Concept addedConcept2 = null;

  /** The added map set. */
  private MapSet addedMapSet = null;

  /** The added mappings. */
  private MappingList addedMappings = null;

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

    processService = new ProcessServiceJpa();
    contentService = new ContentServiceJpa();

    // load the project (should be only one)
    ProjectList projects = processService.getProjects();
    assertTrue(projects.size() > 0);
    project = projects.getObjects().get(0);

    // Create a dummy process execution, to store some information the algorithm
    // needs (specifically input Path)
    processExecution = new ProcessExecutionJpa();
    processExecution.setProject(project);
    processExecution.setTerminology(project.getTerminology());
    processExecution.setVersion(project.getVersion());
    processExecution.setInputPath("terminologies/NCI_INSERT/src");// <- Set this
                                                                  // to
    // the standard
    // folder
    // location

    // Create the /temp subdirectory
    final File tempSrcDir = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + processExecution.getInputPath() + File.separator
            + "temp");
    FileUtils.mkdir(tempSrcDir.toString());

    // Reset the processExecution input path to /src/temp
    processExecution.setInputPath(
        processExecution.getInputPath() + File.separator + "temp");

    // Create and populate classes_atoms.src and attributes.src documents in the
    // /temp
    // temporary subfolder
    atomOutputFile = new File(tempSrcDir, "classes_atoms.src");

    PrintWriter out = new PrintWriter(new FileWriter(atomOutputFile));
    out.println(
        "381548367|NCI_2016_05E|NCI_2016_05E/XM|447562003|N|Y|N|SNOMEDCT_US_2016_09_01 to ICD10_2010 Mappings|N||447562003||ENG|381548367|");
    out.close();

    attributesOutputFile = new File(tempSrcDir, "attributes.src");

    out = new PrintWriter(new FileWriter(attributesOutputFile));
    out.println(
        "13340556|381548367|S|MAPSETSID|447562003|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||c1bb150020d064227a154e6a6fceaeea|");
    out.println(
        "13340557|381548367|S|MAPSETNAME|ICD-10 complex map reference set|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||f8c0f246705f3b3267c2aa75455c1223|");
    out.println(
        "13340558|381548367|S|MAPSETVERSION|20160901|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||1c7dbfbc9a039cdb9da1ab565cb177de|");
    out.println(
        "13340559|381548367|S|MAPSETXRTARGETID|100051|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||3898b7be8009532088697f0b7fb2990f|");
    out.println(
        "13340560|381548367|S|MAPSETRSAB|NCI|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||884109cc354de5898f20a682dc37ad20|");
    out.println(
        "13340561|381548367|S|MAPSETVSAB|NCI_2016_05E|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||4e149dcf5076232e888b08c64a8fa8d4|");
    out.println(
        "13340562|381548367|S|FROMRSAB|SNOMEDCT_US|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||884109cc354de5898f20a682dc37ad20|");
    out.println(
        "13340563|381548367|S|FROMVSAB|SNOMEDCT_US_2016_09_01|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||4e149dcf5076232e888b08c64a8fa8d4|");
    out.println(
        "13340564|381548367|S|TORSAB|ICD10|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||40cdcac3a2c1865ee15aeb762ff7aebb|");
    out.println(
        "13340565|381548367|S|TOVSAB|ICD10_2010|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||e7f011a514b6d05aeeaa12e928501572|");
    out.println(
        "13340567|381548367|S|MTH_MAPSETCOMPLEXITY|N_TO_N|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||7c33c2eebe9172af97a34f119bb4b3b1|");
    out.println(
        "13340568|381548367|S|MTH_MAPFROMCOMPLEXITY|SINGLE SCUI|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||2c9947e02d6b6c19ec71a812323431ea|");
    out.println(
        "13340569|381548367|S|MTH_MAPTOCOMPLEXITY|SINGLE SDUI, MULTIPLE SDUI|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||019b9ce93dff919f115fd8474dd02d17|");
    out.println(
        "13340570|381548367|S|MTH_MAPFROMEXHAUSTIVE|N|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||8d9c307cb7f3c4a32822a51922d1ceaa|");
    out.println(
        "13340571|381548367|S|MTH_MAPTOEXHAUSTIVE|N|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||8d9c307cb7f3c4a32822a51922d1ceaa|");
    out.println(
        "13340579|381548367|S|XMAP|1~1~109006~RT~mapped_to~F41.9~TRUE~447637006~ACTIVE~1~9e7d5fa1-c489-5ba4-a1df-b153527c5423~ALWAYS F41.9|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||d1f221fa60439d18734b8a534e0534a1|");
    out.println(
        "13340580|381548367|S|XMAPFROM|109006~~109006~SCUI~Test From Rule~Test From Res|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||3e766148ef891e9a500c5367851c5a9d|");
    out.println(
        "13340581|381548367|S|XMAPTO|F41.9~~F41.9~SDUI~Test To Rule~Test To Res|NCI_2016_05E|R|Y|N|N|SRC_ATOM_ID|||c5343982680e1c8f21b40f04b35a6bde|");
    out.close();

    // Create and configure the atom algorithm
    atomAlgo = new AtomLoaderAlgorithm();

    // Configure the algorithm (need to do either way)
    atomAlgo.setLastModifiedBy("admin");
    atomAlgo.setLastModifiedFlag(true);
    atomAlgo.setProcess(processExecution);
    atomAlgo.setProject(processExecution.getProject());
    atomAlgo.setTerminology(processExecution.getTerminology());
    atomAlgo.setVersion(processExecution.getVersion());

    // Create and configure the mapping algorithm
    mappingAlgo = new MappingLoaderAlgorithm();

    // Configure the algorithm (need to do either way)
    mappingAlgo.setLastModifiedBy("admin");
    mappingAlgo.setLastModifiedFlag(true);
    mappingAlgo.setProcess(processExecution);
    mappingAlgo.setProject(processExecution.getProject());
    mappingAlgo.setTerminology(processExecution.getTerminology());
    mappingAlgo.setVersion(processExecution.getVersion());
  }

  /**
   * Test mapping loader normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void testMappingLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Run the ATOMLOADER algorithm
    try {

      // Get the mapSets that exist prior to the ATOMLOADER run.
      final MapSetList existingMapSets =
          contentService.getMapSets("",
              "", Branch.ROOT);

      atomAlgo.setTransactionPerOperation(false);
      atomAlgo.beginTransaction();
      //
      // Check prerequisites
      //
      ValidationResult validationResult = atomAlgo.checkPreconditions();
      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty())) {
        // rollback -- unlocks the concept and closes transaction
        atomAlgo.rollback();
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      atomAlgo.compute();

      // Make sure the atom in the temporary input file were added.
      contentService = new ContentServiceJpa();
      SearchResultList list =
          contentService.findConcepts(processExecution.getTerminology(),
              processExecution.getVersion(), Branch.ROOT,
              "atoms.nameSort:\"SNOMEDCT_US_2016_09_01 to ICD10_2010 Mappings\"",
              null);
      assertEquals(1, list.size());

      addedConcept =
          contentService.getConcept(list.getObjects().get(0).getId());
      for (Atom atom : addedConcept.getAtoms()) {
        if (atom.getName()
            .equals("SNOMEDCT_US_2016_09_01 to ICD10_2010 Mappings")) {
          addedAtom = atom;
          break;
        }
      }
      assertNotNull(addedAtom);

      // atomAlgo will create two concepts - one for project terminology, and
      // the other for the atom's terminology. Load the other one as well.
      list = contentService.findConcepts("NCI", "2016_05E", Branch.ROOT,
          "atoms.nameSort:\"SNOMEDCT_US_2016_09_01 to ICD10_2010 Mappings\"",
          null);
      assertEquals(1, list.size());
      addedConcept2 =
          contentService.getConcept(list.getObjects().get(0).getId());

      // Make sure a new mapSet was added.
      MapSetList mapSetList = contentService.getMapSets("",
              "", Branch.ROOT);

      for (MapSet mapSet : mapSetList.getObjects()) {
        if (!existingMapSets.contains(mapSet)) {
          addedMapSet = mapSet;
          break;
        }
      }
      assertNotNull(addedMapSet);

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      atomAlgo.close();
    }

    // Run the MAPPINGLOADER algorithm
    try {

      mappingAlgo.setTransactionPerOperation(false);
      mappingAlgo.beginTransaction();
      //
      // Check prerequisites
      //
      ValidationResult validationResult = mappingAlgo.checkPreconditions();
      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty())) {
        // rollback -- unlocks the concept and closes transaction
        mappingAlgo.rollback();
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      mappingAlgo.compute();

      // Make sure the mapping in the temporary input file was added.
      contentService = new ContentServiceJpa();
      addedMappings =
          contentService.findMappings(addedMapSet.getId(), null, null);
      assertEquals(1, addedMappings.size());

      final Mapping mapping = addedMappings.getObjects().get(0);
      assertEquals("109006", mapping.getFromTerminologyId());
      assertEquals(IdType.CONCEPT, mapping.getFromIdType());
      assertEquals("F41.9", mapping.getToTerminologyId());
      assertEquals(IdType.DESCRIPTOR, mapping.getToIdType());
      
      // Make sure the mapping attributes were set
      final List<Attribute> mappingAttributes = mapping.getAttributes();
      assertEquals(4, mappingAttributes.size());
      for (Attribute attribute : mappingAttributes) {
        if (attribute.getName().equals("FROMRULE")) {
          assertEquals("Test From Rule", attribute.getValue());
        }
        if (attribute.getName().equals("FROMRES")) {
          assertEquals("Test From Res", attribute.getValue());
        }
        if (attribute.getName().equals("TORULE")) {
          assertEquals("Test To Rule", attribute.getValue());
        }
        if (attribute.getName().equals("TORES")) {
          assertEquals("Test To Res", attribute.getValue());
        }
      }      

      // Make sure the mapset was updated
      addedMapSet = contentService.getMapSet(addedMapSet.getId());
      assertEquals("ICD-10 complex map reference set", addedMapSet.getName());
      assertEquals("NCI", addedMapSet.getTerminology());
      assertEquals("2016_05E", addedMapSet.getVersion());
      assertEquals("SNOMEDCT_US", addedMapSet.getFromTerminology());
      assertEquals("2016_09_01", addedMapSet.getFromVersion());
      assertEquals("ICD10", addedMapSet.getToTerminology());
      assertEquals("2010", addedMapSet.getToVersion());

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      mappingAlgo.close();
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {

    contentService = new ContentServiceJpa();
    contentService.setMolecularActionFlag(false);
    contentService.setLastModifiedBy("admin");

    if (addedAtom != null) {
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
      if (addedConcept != null) {
        addedConcept.getAtoms().remove(addedAtom);
        contentService.updateConcept(addedConcept);
        contentService.removeConcept(addedConcept.getId());
      }
      if (addedConcept2 != null) {
        addedConcept2.getAtoms().remove(addedAtom);
        contentService.updateConcept(addedConcept2);
        contentService.removeConcept(addedConcept2.getId());
      }

      contentService.removeAtom(addedAtom.getId());
    }
    
    if (addedMapSet != null) {
      List<Mapping> mappings = addedMapSet.getMappings();
      for(Mapping mapping : mappings){
        contentService.removeMapping(mapping.getId());
      }      
      addedMapSet.clearMappings();
      contentService.updateMapSet(addedMapSet);
      contentService.removeMapSet(addedMapSet.getId());
    }

    FileUtils.forceDelete(atomOutputFile);
    FileUtils.forceDelete(attributesOutputFile);

    File testDirectory = new File(
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + processExecution.getInputPath());

    FileUtils.deleteDirectory(testDirectory);

    processService.close();
    contentService.close();
  }

  /**
   * Teardown class.
   */
  @AfterClass
  public static void teardownClass() {
    // do nothing
  }

}
