/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
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
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.jpa.ProcessExecutionJpa;
import com.wci.umls.server.jpa.algo.insert.SubsetLoaderAlgorithm;
import com.wci.umls.server.jpa.content.AtomSubsetJpa;
import com.wci.umls.server.jpa.content.AtomSubsetMemberJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetJpa;
import com.wci.umls.server.jpa.content.ConceptSubsetMemberJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.jpa.services.ProcessServiceJpa;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Sample test to get auto complete working.
 */
public class SubsetLoaderAlgorithmTest extends IntegrationUnitSupport {

  /** The subset member algorithm. */
  SubsetLoaderAlgorithm subsetMemberAlgo = null;

  /** The process execution. */
  ProcessExecution processExecution = null;

  /** The process service. */
  ProcessServiceJpa processService = null;

  /** The content service. */
  ContentServiceJpa contentService = null;

  /** The project. */
  Project project = null;

  /** The temporary attributes.src file. */
  private File attributesOutputFile = null;

  private List<Subset> addedSubsets = new ArrayList<>();

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
    processExecution.setInputPath("terminologies/SAMPLE_SNOMEDCT_US/src");// <-
                                                                          // Set
                                                                          // this
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

    // Create and populate attributes.src document in the
    // /temp temporary subfolder
    attributesOutputFile = new File(tempSrcDir, "attributes.src");

    PrintWriter out = new PrintWriter(new FileWriter(attributesOutputFile));
    out.println(
        "209|447562003|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTEDESCRIPTION~900000000000500006|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d4759302-3afb-565d-b627-17c78e060134|c1d1b595e43df215db2927eafc46e59d|");
    out.println(
        "210|447562003|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTETYPE~900000000000461009|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d4759302-3afb-565d-b627-17c78e060134|a4cb3743753e194774a6ca2de5f79e4f|");
    out.println(
        "211|447562003|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTEORDER~0|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d4759302-3afb-565d-b627-17c78e060134|62fdb3cf5b09a68a28efcd60d3e2bd00|");
    out.println(
        "215|447562003|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTEDESCRIPTION~900000000000503008|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d7428912-7544-5a88-abda-ef7c1e44c786|bd4d8f01dda9a083844ca15301ec01d2|");
    out.println(
        "216|447562003|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTETYPE~900000000000465000|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d7428912-7544-5a88-abda-ef7c1e44c786|4466c28c59377b866b2a70dbab37c9f7|");
    out.println(
        "217|447562003|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTEORDER~3|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d7428912-7544-5a88-abda-ef7c1e44c786|959a0b645c4defb47525f1dd0d8a3c5b|");
    out.println(
        "218|6011000124106|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTEDESCRIPTION~447247004|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d9f6fb4b-1b3e-5d3f-8faa-afa66fffaf13|0dea2aef19fcb6a2af9c13ad22410874|");
    out.println(
        "219|6011000124106|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTETYPE~900000000000461009|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d9f6fb4b-1b3e-5d3f-8faa-afa66fffaf13|a4cb3743753e194774a6ca2de5f79e4f|");
    out.println(
        "220|6011000124106|S|SUBSET_MEMBER|900000000000456007~ATTRIBUTEORDER~6|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|d9f6fb4b-1b3e-5d3f-8faa-afa66fffaf13|cef0b5247cbeefcc3087cab55b40a2e9|");
    out.println(
        "6426467|372453002|S|SUBSET_MEMBER|442311000124105|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_CUI|SNOMEDCT_US_2016_09_01|009e3891-100f-54c9-ae84-ca01fba65953|3325fc4937746daeb8184cc8b1f7484e|");
    out.println(
        "3932356|900000000001183011|S|SUBSET_MEMBER|900000000000508004~ACCEPTABILITYID~900000000000548007|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_AUI|SNOMEDCT_US_2016_09_01|009c6780-97ff-5298-8c6d-37df7b41838e|e9f81bbe9248e12bf1370ff5bb433a83|");
    out.println(
        "3942521|900000000000438011|S|SUBSET_MEMBER|900000000000508004~ACCEPTABILITYID~900000000000548007|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_AUI|SNOMEDCT_US_2016_09_01|01a78d22-ad0b-5e76-8fd4-9fed481e5de5|e9f81bbe9248e12bf1370ff5bb433a83|");
    out.println(
        "3950940|900000000001089013|S|SUBSET_MEMBER|900000000000508004~ACCEPTABILITYID~900000000000549004|SNOMEDCT_US_2016_09_01|R|Y|N|N|SOURCE_AUI|SNOMEDCT_US_2016_09_01|02884866-6748-5552-99d0-4b5ec06843a8|c5220a0a23445ebacfff70ec21344853|");
    out.close();

    // Create and configure the mapping algorithm
    subsetMemberAlgo = new SubsetLoaderAlgorithm();

    // Configure the algorithm (need to do either way)
    subsetMemberAlgo.setLastModifiedBy("admin");
    subsetMemberAlgo.setLastModifiedFlag(true);
    subsetMemberAlgo.setProcess(processExecution);
    subsetMemberAlgo.setProject(processExecution.getProject());
    subsetMemberAlgo.setTerminology(processExecution.getTerminology());
    subsetMemberAlgo.setVersion(processExecution.getVersion());
  }

  /**
   * Test subset member loader.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings({
      "rawtypes"
  })
  @Test
  public void testSubsetMemberLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    // Get the time before the algorithm starts
    Date preAlgoDateTime = new Date();

    // Run the SUBSETMEMBERLOADING algorithm
    try {

      subsetMemberAlgo.setTransactionPerOperation(false);
      subsetMemberAlgo.beginTransaction();
      //
      // Check prerequisites
      //
      ValidationResult validationResult = subsetMemberAlgo.checkPreconditions();
      // if prerequisites fail, return validation result
      if (!validationResult.getErrors().isEmpty()
          || (!validationResult.getWarnings().isEmpty())) {
        // rollback -- unlocks the concept and closes transaction
        subsetMemberAlgo.rollback();
      }
      assertTrue(validationResult.getErrors().isEmpty());

      //
      // Perform the algorithm
      //
      subsetMemberAlgo.compute();

      // Make sure the subsets were created
      contentService = new ContentServiceJpa();
      SubsetList postAlgoSubsets = contentService.getAllSubsets("SNOMEDCT_US",
          "2016_09_01", Branch.ROOT);

      for (Subset postAlgoSubset : postAlgoSubsets.getObjects()) {
        if (postAlgoSubset.getTimestamp().after(preAlgoDateTime)) {
          addedSubsets.add(postAlgoSubset);
        }
      }

      assertEquals(3, addedSubsets.size());

      // Make sure subsets contain the appropriate members, and
      // make sure members have the appropriate attributes
      for (Subset subset : addedSubsets) {
        if (subset.getTerminologyId().equals("900000000000456007")) {
          assertEquals(3, ((ConceptSubsetJpa) subset).getMembers().size());
          for (SubsetMember subsetMember : ((ConceptSubsetJpa) subset)
              .getMembers()) {
            assertEquals(3, subsetMember.getAttributes().size());
          }
        }
        if (subset.getTerminologyId().equals("442311000124105")) {
          assertEquals(1, ((ConceptSubsetJpa) subset).getMembers().size());
          for (SubsetMember subsetMember : ((ConceptSubsetJpa) subset)
              .getMembers()) {
            assertEquals(1, subsetMember.getAttributes().size());
          }
        }
        if (subset.getTerminologyId().equals("900000000000508004")) {
          assertEquals(3, ((AtomSubsetJpa) subset).getMembers().size());
          for (SubsetMember subsetMember : ((AtomSubsetJpa) subset)
              .getMembers()) {
            assertEquals(1, subsetMember.getAttributes().size());
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected exception thrown - please review stack trace.");
    } finally {
      subsetMemberAlgo.close();
    }
  }

  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("rawtypes")
  @After
  public void teardown() throws Exception {

    contentService = new ContentServiceJpa();
    contentService.setMolecularActionFlag(false);
    contentService.setLastModifiedBy("admin");

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
