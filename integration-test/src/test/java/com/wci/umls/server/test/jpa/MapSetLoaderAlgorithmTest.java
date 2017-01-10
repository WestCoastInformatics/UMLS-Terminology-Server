/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

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
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.helpers.content.MappingList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.MapSetLoaderAlgorithm;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class MapSetLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The mapping algorithm. */
  MapSetLoaderAlgorithm mappingAlgo = null;

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

    // Create and populate attributes.src documents in the
    // /temp temporary subfolder

    attributesOutputFile = new File(tempSrcDir, "attributes.src");

    PrintWriter out = new PrintWriter(new FileWriter(attributesOutputFile));
    out.println(
        "13340556|381548367|S|MAPSETSID|447562003|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||c1bb150020d064227a154e6a6fceaeea|");
    out.println(
        "13340557|381548367|S|MAPSETNAME|ICD-10 complex map reference set|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||f8c0f246705f3b3267c2aa75455c1223|");
    out.println(
        "13340558|381548367|S|MAPSETVERSION|20160901|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||1c7dbfbc9a039cdb9da1ab565cb177de|");
    out.println(
        "13340559|381548367|S|MAPSETXRTARGETID|100051|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||3898b7be8009532088697f0b7fb2990f|");
    out.println(
        "13340560|381548367|S|MAPSETRSAB|NCI|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||884109cc354de5898f20a682dc37ad20|");
    out.println(
        "13340561|381548367|S|MAPSETVSAB|SNOMEDCT_US_2016_09_01|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||4e149dcf5076232e888b08c64a8fa8d4|");
    out.println(
        "13340562|381548367|S|FROMRSAB|SNOMEDCT_US|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||884109cc354de5898f20a682dc37ad20|");
    out.println(
        "13340563|381548367|S|FROMVSAB|SNOMEDCT_US_2016_09_01|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||4e149dcf5076232e888b08c64a8fa8d4|");
    out.println(
        "13340564|381548367|S|TORSAB|ICD10|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||40cdcac3a2c1865ee15aeb762ff7aebb|");
    out.println(
        "13340565|381548367|S|TOVSAB|ICD10_2010|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||e7f011a514b6d05aeeaa12e928501572|");
    out.println(
        "13340567|381548367|S|MTH_MAPSETCOMPLEXITY|N_TO_N|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||7c33c2eebe9172af97a34f119bb4b3b1|");
    out.println(
        "13340568|381548367|S|MTH_MAPFROMCOMPLEXITY|SINGLE SCUI|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||2c9947e02d6b6c19ec71a812323431ea|");
    out.println(
        "13340569|381548367|S|MTH_MAPTOCOMPLEXITY|SINGLE SDUI, MULTIPLE SDUI|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||019b9ce93dff919f115fd8474dd02d17|");
    out.println(
        "13340570|381548367|S|MTH_MAPFROMEXHAUSTIVE|N|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||8d9c307cb7f3c4a32822a51922d1ceaa|");
    out.println(
        "13340571|381548367|S|MTH_MAPTOEXHAUSTIVE|N|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||8d9c307cb7f3c4a32822a51922d1ceaa|");
    out.println(
        "13340579|381548367|S|XMAP|1~1~109006~RT~mapped_to~F41.9~TRUE~447637006~ACTIVE~1~9e7d5fa1-c489-5ba4-a1df-b153527c5423~ALWAYS F41.9|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||d1f221fa60439d18734b8a534e0534a1|");
    out.println(
        "13340580|381548367|S|XMAPFROM|109006~~109006~SCUI~Test From Rule~Test From Res|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||3e766148ef891e9a500c5367851c5a9d|");
    out.println(
        "13340581|381548367|S|XMAPTO|F41.9~~F41.9~SDUI~Test To Rule~Test To Res|SNOMEDCT_US_2016_09_01|R|Y|N|N|SRC_ATOM_ID|||c5343982680e1c8f21b40f04b35a6bde|");
    out.close();

    // Create and configure the mapping algorithm
    mappingAlgo = new MapSetLoaderAlgorithm();

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

      // Check the mapping's alternate terminonlogy Ids
      final Map<String, String> mappingAltIds =
          mapping.getAlternateTerminologyIds();
      assertNotNull(mappingAltIds.get(project.getTerminology()));
      assertTrue(mappingAltIds.get(project.getTerminology()).startsWith("AT"));
      assertEquals("109006",
          mappingAltIds.get(project.getTerminology() + "-FROMID"));
      assertNull(mappingAltIds.get(project.getTerminology() + "-FROMSID"));
      assertEquals("F41.9",
          mappingAltIds.get(project.getTerminology() + "-TOID"));
      assertNull(mappingAltIds.get(project.getTerminology() + "-TOSID"));

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
      assertEquals("SNOMEDCT_US", addedMapSet.getTerminology());
      assertEquals("2016_09_01", addedMapSet.getVersion());
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

    if (addedMapSet != null) {
      List<Mapping> mappings = addedMapSet.getMappings();
      for (Mapping mapping : mappings) {
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
