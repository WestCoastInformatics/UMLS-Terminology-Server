/*
 * Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.test.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.wci.umls.server.helpers.StringList;
import com.wci.umls.server.helpers.content.SubsetList;
import com.wci.umls.server.helpers.content.SubsetMemberList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.PfscParameterJpa;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;

/**
 * Implementation of the "Content Service REST Degenerate Use" Test Cases.
 */
public class ContentServiceRestDegenerateUseTest extends ContentServiceRestTest {

  /** The auth token. */
  private static String authToken;

  /** The test test id. */
  private String testId;

  /** The test terminology. */
  private String testTerminology;

  /** The test version. */
  private String testVersion;
  
  /** The snomed terminology. */
  private String snomedTerminology = "SNOMEDCT_US";

  /** The snomed version. */
  private String snomedVersion = "2014_09_01";

  /** The msh terminology. */
  private String mshTerminology = "MSH";

  /** The msh version. */
  private String mshVersion = "2015_2014_09_08";

  /** The umls terminology. */
  private String umlsTerminology = "UMLS";

  /** The umls version. */
  private String umlsVersion = "latest";


  /** The concept used in testing. */
  @SuppressWarnings("unused")
  private Concept concept;

  /** The valid parameters used for reflection testing. */
  @SuppressWarnings("unused")
  private Object[] validParameters;

  /**
   * Create test fixtures per test.
   *
   * @throws Exception the exception
   */
  @Override
  @Before
  public void setup() throws Exception {

    // authentication
    authToken = securityService.authenticate(testUser, testPassword);

    // set terminology and version
    testTerminology = "SNOMEDCT_US";
    testVersion = "2014_09_01";
    testId = "102466009";

    // get test concept
    concept =
        contentService.getConcept(testId, testTerminology, testVersion,
            authToken);

  }

  /**
   * Test Get and Find methods for concepts.
   *
   * @throws Exception the exception
   */
  //@Test
/*  public void testDegenerateUseRestContent001() throws Exception {


    // get concepts
    validParameters = new Object[] {
        testId, testTerminology, testVersion, authToken
    };

    DegenerateUseMethodTestHelper.testDegenerateArguments(
        contentService,
        contentService.getClass().getMethod("getConcept",
            getParameterTypes(validParameters)), validParameters,

        // String fields will fail on empty strings, return no results on null
        // (correct behavior)
        new ExpectedFailure[] {
            ExpectedFailure.STRING_INVALID_EXCEPTION_NULL_NO_RESULTS,
            ExpectedFailure.STRING_INVALID_EXCEPTION_NULL_NO_RESULTS,
            ExpectedFailure.STRING_INVALID_EXCEPTION_NULL_NO_RESULTS,
            ExpectedFailure.EXCEPTION
        });    
    
  }*/

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent001() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");


    // Test with null terminologyId
    Logger.getLogger(getClass()).info(
        "TEST1 - " + "null, MSH, 2015_2014_09_08, " + authToken);
    try {
      contentService.getConcept(null, mshTerminology, mshVersion,
            authToken);
      fail("Exception should be thrown when trying to get a concept with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid terminologyId
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "-1, SNOMEDCT, 2014_09_01, " + authToken);
    try {
       contentService.getConcept("-1", snomedTerminology, snomedVersion,
            authToken);
       fail("Exception should be thrown when trying to get a concept with invalid terminologyId.");
     } catch (Exception e) {
       // do nothing
     }
    
    // Test with empty string terminologyId
    Logger.getLogger(getClass()).info(
        "TEST3 - " + ", UMLS, latest, " + authToken);
    try {
        contentService.getConcept("", umlsTerminology, umlsVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST4 - " + "M0028634, null, 2015_2014_09_08, " + authToken);
    try {
        contentService.getConcept("M0028634", null, mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with null terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST5 - " + "M0028634, TTT, 2015_2014_09_08, " + authToken);
    try {
        contentService.getConcept("M0028634", "TTT", mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST6 - " + "M0028634, , 2015_2014_09_08, " + authToken);
    try {
        contentService.getConcept("M0028634", "", mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST7 - " + "M0028634, MSH, null, " + authToken);
    try {
        contentService.getConcept("M0028634", mshTerminology, null,
            authToken);
        fail("Exception should be thrown when trying to get a concept with null version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST8 - " + "M0028634, MSH, TTT , " + authToken);
    try {
        contentService.getConcept("M0028634", "MSH", "TTT",
            authToken);
        fail("Exception should be thrown when trying to get a concept with invalid version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST9 - " + "M0028634, MSH, , " + authToken);
    try {
        contentService.getConcept("M0028634", "MSH", "",
            authToken);
        fail("Exception should be thrown when trying to get a concept with empty string version.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - " + "M0028634, MSH, 2015_2014_09_08, " );
    try {
        contentService.getConcept("M0028634", "MSH", "2015_2014_09_08",
            null);
        fail("Exception should be thrown when trying to get a concept with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - " + "M0028634, MSH, 2015_2014_09_08, TTT" );
    try {
        contentService.getConcept("M0028634", "MSH", "2015_2014_09_08",
            "TTT");
        fail("Exception should be thrown when trying to get a concept with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST12 - " + "M0028634, MSH, 2015_2014_09_08, TTT" );
    try {
        contentService.getConcept("M0028634", "MSH", "2015_2014_09_08",
            "");
        fail("Exception should be thrown when trying to get a concept with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" methods for descriptors.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent002() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // Test MSH descriptor

    Logger.getLogger(getClass()).info(
        "TEST1 - " + "null, MSH, 2015_2014_09_08, " + authToken);
    try {
      contentService.getDescriptor(null, mshTerminology, mshVersion,
            authToken);
      fail("Exception should be thrown when trying to get a descriptor with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test SNOMEDCT_US descriptor
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "-1, SNOMEDCT, 2014_09_01, " + authToken);
    try {
       contentService.getDescriptor("-1", snomedTerminology, snomedVersion,
            authToken);
       fail("Exception should be thrown when trying to get a descriptor with invalid terminologyId.");
     } catch (Exception e) {
       // do nothing
     }
    
    // Test UMLS descriptor

    Logger.getLogger(getClass()).info(
        "TEST3 - " + ", UMLS, latest, " + authToken);
    try {
        contentService.getDescriptor("", umlsTerminology, umlsVersion,
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST4 - " + "M0028634, null, 2015_2014_09_08, " + authToken);
    try {
        contentService.getDescriptor("M0028634", null, mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with null terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST5 - " + "M0028634, TTT, 2015_2014_09_08, " + authToken);
    try {
        contentService.getDescriptor("M0028634", "TTT", mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST6 - " + "M0028634, , 2015_2014_09_08, " + authToken);
    try {
        contentService.getDescriptor("M0028634", "", mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST7 - " + "M0028634, MSH, null, " + authToken);
    try {
        contentService.getDescriptor("M0028634", mshTerminology, null,
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with null version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST8 - " + "M0028634, MSH, TTT , " + authToken);
    try {
        contentService.getDescriptor("M0028634", "MSH", "TTT",
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with invalid version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST9 - " + "M0028634, MSH, , " + authToken);
    try {
        contentService.getDescriptor("M0028634", "MSH", "",
            authToken);
        fail("Exception should be thrown when trying to get a descriptor with empty string version.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - " + "M0028634, MSH, 2015_2014_09_08, " );
    try {
        contentService.getDescriptor("M0028634", "MSH", "2015_2014_09_08",
            null);
        fail("Exception should be thrown when trying to get a descriptor with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - " + "M0028634, MSH, 2015_2014_09_08, TTT" );
    try {
        contentService.getDescriptor("M0028634", "MSH", "2015_2014_09_08",
            "TTT");
        fail("Exception should be thrown when trying to get a descriptor with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST12 - " + "M0028634, MSH, 2015_2014_09_08, TTT" );
    try {
        contentService.getDescriptor("M0028634", "MSH", "2015_2014_09_08",
            "");
        fail("Exception should be thrown when trying to get a descriptor with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

  /**
   * Test "get" methods for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent003() throws Exception {
    Logger.getLogger(getClass()).debug("Start test");

    // Test MSH code

    Logger.getLogger(getClass()).info(
        "TEST1 - " + "null, MSH, 2015_2014_09_08, " + authToken);
    try {
      contentService.getCode(null, mshTerminology, mshVersion,
            authToken);
      fail("Exception should be thrown when trying to get a code with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test SNOMEDCT_US code
    Logger.getLogger(getClass()).info(
        "TEST2 - " + "-1, SNOMEDCT, 2014_09_01, " + authToken);
    try {
       contentService.getCode("-1", snomedTerminology, snomedVersion,
            authToken);
       fail("Exception should be thrown when trying to get a code with invalid terminologyId.");
     } catch (Exception e) {
       // do nothing
     }
    
    // Test UMLS code

    Logger.getLogger(getClass()).info(
        "TEST3 - " + ", UMLS, latest, " + authToken);
    try {
        contentService.getCode("", umlsTerminology, umlsVersion,
            authToken);
        fail("Exception should be thrown when trying to get a code with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST4 - " + "M0028634, null, 2015_2014_09_08, " + authToken);
    try {
        contentService.getCode("M0028634", null, mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a code with null terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST5 - " + "M0028634, TTT, 2015_2014_09_08, " + authToken);
    try {
        contentService.getCode("M0028634", "TTT", mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a code with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST6 - " + "M0028634, , 2015_2014_09_08, " + authToken);
    try {
        contentService.getCode("M0028634", "", mshVersion,
            authToken);
        fail("Exception should be thrown when trying to get a code with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST7 - " + "M0028634, MSH, null, " + authToken);
    try {
        contentService.getCode("M0028634", mshTerminology, null,
            authToken);
        fail("Exception should be thrown when trying to get a code with null version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST8 - " + "M0028634, MSH, TTT , " + authToken);
    try {
        contentService.getCode("M0028634", "MSH", "TTT",
            authToken);
        fail("Exception should be thrown when trying to get a code with invalid version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST9 - " + "M0028634, MSH, , " + authToken);
    try {
        contentService.getCode("M0028634", "MSH", "",
            authToken);
        fail("Exception should be thrown when trying to get a code with empty string version.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - " + "M0028634, MSH, 2015_2014_09_08, " );
    try {
        contentService.getCode("M0028634", "MSH", "2015_2014_09_08",
            null);
        fail("Exception should be thrown when trying to get a code with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - " + "M0028634, MSH, 2015_2014_09_08, TTT" );
    try {
        contentService.getCode("M0028634", "MSH", "2015_2014_09_08",
            "TTT");
        fail("Exception should be thrown when trying to get a code with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST12 - " + "M0028634, MSH, 2015_2014_09_08, TTT" );
    try {
        contentService.getCode("M0028634", "MSH", "2015_2014_09_08",
            "");
        fail("Exception should be thrown when trying to get a code with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }

 
  /**
   * Test "get" method for lexical classes.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent004() throws Exception {
    // n/a
  }

  /**
   * Test "get" method for string classes.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent005() throws Exception {
    // n/a
  }
  
  /**
   * Test "get" methods for atom subsets
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent006() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " + "null, 2014_09_01, " + authToken );
    try {
      contentService.getAtomSubsets(null, snomedVersion,
          authToken);      
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " + "TTT, 2014_09_01, " + authToken );
    try {
      contentService.getAtomSubsets("TTT", snomedVersion,
          authToken);      
      fail("Exception should be thrown when trying to get an atom subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " + ", 2014_09_01, " + authToken );
    try {
      contentService.getAtomSubsets("", snomedVersion,
          authToken);      
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " + "SNOMEDCT_US, null, " + authToken );
    try {
      contentService.getAtomSubsets(snomedTerminology, null,
          authToken);      
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " + "SNOMEDCT_US, TTT, " + authToken );
    try {
      contentService.getAtomSubsets(snomedTerminology, "TTT",
          authToken);      
      fail("Exception should be thrown when trying to get an atom subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " + "SNOMEDCT_US, , " + authToken );
    try {
      contentService.getAtomSubsets(snomedTerminology, "",
          authToken);      
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    
    SubsetList list =
        contentService.getAtomSubsets(snomedTerminology, snomedVersion,
            authToken);

    // Test negative start index - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST7");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(-20);
    Subset subset = list.getObjects().get(0);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find atom subset members with negative pfs start index.");
    }  
    
    // Test invalid pfs max results - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST8");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(-20);
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find atom subset members with negative pfs max results.");
    }   

    // Test invalid pfs sort field
    Logger.getLogger(getClass()).info("TEST9");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setSortField("TTT");
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find atom subset members with empty string sort field.");
    } catch (Exception e) {
      // do nothing
    }  
    
    // Test invalid query restriction
    // TODO: why does this not throw a ParseException? don't do for now
    /*Logger.getLogger(getClass()).info("TEST10");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setQueryRestriction("TTT:TTT");
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find atom subset members with invalid query restriction.");
    } catch (Exception e) {
      // do nothing
    }  */
    
    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST11 - " + "null, 2014_09_01, " + authToken );
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), null, snomedVersion,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST12 - " + "TTT, 2014_09_01, " + authToken );
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), "TTT", snomedVersion,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an atom subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST13 - " + ", 2014_09_01, " + authToken );
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), "", snomedVersion,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST14 - " + "SNOMEDCT_US, null, " + authToken );
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), snomedTerminology, null,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an atom subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST15 - " + "SNOMEDCT_US, TTT, " + authToken );
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), snomedTerminology, "TTT",
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an atom subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST16 - " + "SNOMEDCT_US, , " + authToken );
    try {
      contentService.findAtomSubsetMembers(subset.getTerminologyId(), snomedTerminology, "",
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an atom subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    } 
  }
  
  /**
   * Test "get" methods for concept subsets
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent007() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " + "null, 2014_09_01, " + authToken );
    try {
      contentService.getConceptSubsets(null, snomedVersion,
          authToken);      
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " + "TTT, 2014_09_01, " + authToken );
    try {
      contentService.getConceptSubsets("TTT", snomedVersion,
          authToken);      
      fail("Exception should be thrown when trying to get an concept subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " + ", 2014_09_01, " + authToken );
    try {
      contentService.getConceptSubsets("", snomedVersion,
          authToken);      
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " + "SNOMEDCT_US, null, " + authToken );
    try {
      contentService.getConceptSubsets(snomedTerminology, null,
          authToken);      
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " + "SNOMEDCT_US, TTT, " + authToken );
    try {
      contentService.getConceptSubsets(snomedTerminology, "TTT",
          authToken);      
      fail("Exception should be thrown when trying to get an concept subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " + "SNOMEDCT_US, , " + authToken );
    try {
      contentService.getConceptSubsets(snomedTerminology, "",
          authToken);      
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    
    SubsetList list =
        contentService.getConceptSubsets(snomedTerminology, snomedVersion,
            authToken);

    // Test negative start index - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST7");
    PfsParameterJpa pfs = new PfsParameterJpa();
    pfs.setStartIndex(-20);
    Subset subset = list.getObjects().get(0);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find concept subset members with negative pfs start index.");
    }  
    
    // Test invalid pfs max results - indicates not to use paging
    Logger.getLogger(getClass()).info("TEST8");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(-20);
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to find concept subset members with negative pfs max results.");
    }   

    // Test invalid pfs sort field
    Logger.getLogger(getClass()).info("TEST9");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setSortField("TTT");
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find concept subset members with empty string sort field.");
    } catch (Exception e) {
      // do nothing
    }  
    
    // Test invalid query restriction
    // TODO: why does this not throw a ParseException?
    /*Logger.getLogger(getClass()).info("TEST10");
    pfs = new PfsParameterJpa();
    pfs.setStartIndex(0);
    pfs.setMaxResults(20);
    pfs.setQueryRestriction("TTT:TTT");
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(),
       snomedTerminology, snomedVersion, null, pfs, authToken);
      fail("Exception should be thrown when trying to find concept subset members with invalid query restriction.");
    } catch (Exception e) {
      // do nothing
    }  */
    
    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST11 - " + "null, 2014_09_01, " + authToken );
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), null, snomedVersion,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST12 - " + "TTT, 2014_09_01, " + authToken );
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), "TTT", snomedVersion,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an concept subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST13 - " + ", 2014_09_01, " + authToken );
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), "", snomedVersion,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST14 - " + "SNOMEDCT_US, null, " + authToken );
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), snomedTerminology, null,
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an concept subset with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST15 - " + "SNOMEDCT_US, TTT, " + authToken );
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), snomedTerminology, "TTT",
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an concept subset with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST16 - " + "SNOMEDCT_US, , " + authToken );
    try {
      contentService.findConceptSubsetMembers(subset.getTerminologyId(), snomedTerminology, "",
          null, new PfsParameterJpa(), authToken);      
      fail("Exception should be thrown when trying to get an concept subset with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    } 
  }
  
  /**
   * Test find concepts for query.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent008() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " );
    try {
      contentService.findConceptsForQuery(null, snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " );
    try {
      contentService.findConceptsForQuery("TTT", snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " );
    try {
      contentService.findConceptsForQuery("", snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " );
    try {
      contentService.findConceptsForQuery(snomedTerminology, null, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " );
    try {
      contentService.findConceptsForQuery(snomedTerminology, "TTT", "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " );
    try {
      contentService.findConceptsForQuery(snomedTerminology, "", "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test query is null
    Logger.getLogger(getClass()).info(
    "TEST7 - " );
    try {
      contentService.findConceptsForQuery(snomedTerminology, snomedVersion, null, new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test query is empty string
    Logger.getLogger(getClass()).info(
    "TEST8 - " );
    try {
      contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find concepts for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST9 - ");
    try {
      contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          null);
        fail("Exception should be thrown when trying to find concepts for query with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - ");
    try {
      contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          "TTT");
        fail("Exception should be thrown when trying to find concepts for query with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - "  );
    try {
      contentService.findConceptsForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          "");
        fail("Exception should be thrown when trying to find concepts for query with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }
  
  /**
   * Test find descriptors for query.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent009() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " );
    try {
      contentService.findDescriptorsForQuery(null, snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " );
    try {
      contentService.findDescriptorsForQuery("TTT", snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " );
    try {
      contentService.findDescriptorsForQuery("", snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " );
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, null, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " );
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, "TTT", "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " );
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, "", "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test query is null
    Logger.getLogger(getClass()).info(
    "TEST7 - " );
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion, null, new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test query is empty string
    Logger.getLogger(getClass()).info(
    "TEST8 - " );
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion, "", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descriptors for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST9 - ");
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          null);
        fail("Exception should be thrown when trying to find descriptors for query with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - ");
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          "TTT");
        fail("Exception should be thrown when trying to find descriptors for query with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - "  );
    try {
      contentService.findDescriptorsForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          "");
        fail("Exception should be thrown when trying to find descriptors for query with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }
  
  /**
   * Test find codes for query.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent010() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " );
    try {
      contentService.findCodesForQuery(null, snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " );
    try {
      contentService.findCodesForQuery("TTT", snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " );
    try {
      contentService.findCodesForQuery("", snomedVersion, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " );
    try {
      contentService.findCodesForQuery(snomedTerminology, null, "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " );
    try {
      contentService.findCodesForQuery(snomedTerminology, "TTT", "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " );
    try {
      contentService.findCodesForQuery(snomedTerminology, "", "care", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test query is null
    Logger.getLogger(getClass()).info(
    "TEST7 - " );
    try {
      contentService.findCodesForQuery(snomedTerminology, snomedVersion, null, new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test query is empty string
    Logger.getLogger(getClass()).info(
    "TEST8 - " );
    try {
      contentService.findCodesForQuery(snomedTerminology, snomedVersion, "", new PfscParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find codes for query with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST9 - ");
    try {
      contentService.findCodesForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          null);
        fail("Exception should be thrown when trying to find codes for query with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - ");
    try {
      contentService.findCodesForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          "TTT");
        fail("Exception should be thrown when trying to find codes for query with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - "  );
    try {
      contentService.findCodesForQuery(snomedTerminology, snomedVersion, "care", new PfscParameterJpa(),
          "");
        fail("Exception should be thrown when trying to find codes for query with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }
  

  /**
   * Test find descendant concepts.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent011() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " );
    try {
      contentService.findDescendantConcepts("105590001", null, snomedVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descendant concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " );
    try {
      contentService.findDescendantConcepts("105590001", "TTT", snomedVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descendant concepts with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " );
    try {
      contentService.findDescendantConcepts("105590001", "", snomedVersion, false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find descendant concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " );
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, null, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descendant concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " );
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, "TTT", false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find descendant concepts with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " );
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, "", false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find descendant concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test self id is null
    Logger.getLogger(getClass()).info(
    "TEST7 - " );
    try {
      contentService.findDescendantConcepts(null, snomedTerminology, snomedVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find descendant concepts with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test self id is empty string
    Logger.getLogger(getClass()).info(
    "TEST8 - " );
    try {
      contentService.findDescendantConcepts("", snomedTerminology, snomedVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find descendant concepts with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST9 - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, snomedVersion, false, new PfsParameterJpa(),
          null); 
        fail("Exception should be thrown when trying to find descendant concepts with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - ");
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, null, false, new PfsParameterJpa(),
          "TTT"); 
        fail("Exception should be thrown when trying to find descendant concepts with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - "  );
    try {
      contentService.findDescendantConcepts("105590001", snomedTerminology, null, false, new PfsParameterJpa(),
          ""); 
        fail("Exception should be thrown when trying to find descendant concepts with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
   
    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST12 - " );
    try {
      contentService.findAncestorConcepts("105590001", null, snomedVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find ancestor concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST13 - " );
    try {
      contentService.findAncestorConcepts("105590001", "TTT", snomedVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find ancestor concepts with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST14 - " );
    try {
      contentService.findAncestorConcepts("105590001", "", snomedVersion, false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find ancestor concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST15 - " );
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, null, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find ancestor concepts with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST16 - " );
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, "TTT", false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find ancestor concepts with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST17 - " );
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, "", false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find ancestor concepts with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test self id is null
    Logger.getLogger(getClass()).info(
    "TEST18 - " );
    try {
      contentService.findAncestorConcepts(null, snomedTerminology, snomedVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find ancestor concepts with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test self id is empty string
    Logger.getLogger(getClass()).info(
    "TEST19 - " );
    try {
      contentService.findAncestorConcepts("", snomedTerminology, snomedVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find ancestor concepts with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST20 - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, snomedVersion, false, new PfsParameterJpa(),
          null); 
        fail("Exception should be thrown when trying to find ancestor concepts with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST21 - ");
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, null, false, new PfsParameterJpa(),
          "TTT"); 
        fail("Exception should be thrown when trying to find ancestor concepts with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST22 - "  );
    try {
      contentService.findAncestorConcepts("105590001", snomedTerminology, null, false, new PfsParameterJpa(),
          ""); 
        fail("Exception should be thrown when trying to find ancestor concepts with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }  
  
  /**
   * Test find descendant descriptors.
   * @throws Exception
   */
  @Test
  public void testDegenerateUseRestContent012() throws Exception {

    Logger.getLogger(getClass()).debug("Start test");

    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST1 - " );
    try {
      contentService.findDescendantDescriptors("D000005", null, mshVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descendant descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST2 - " );
    try {
      contentService.findDescendantDescriptors("D000005", "TTT", mshVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descendant descriptors with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST3 - " );
    try {
      contentService.findDescendantDescriptors("D000005", "", mshVersion, false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find descendant descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST4 - " );
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, null, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find descendant descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST5 - " );
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, "TTT", false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find descendant descriptors with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST6 - " );
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, "", false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find descendant descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test self id is null
    Logger.getLogger(getClass()).info(
    "TEST7 - " );
    try {
      contentService.findDescendantDescriptors(null, mshTerminology, mshVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find descendant descriptors with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test self id is empty string
    Logger.getLogger(getClass()).info(
    "TEST8 - " );
    try {
      contentService.findDescendantDescriptors("", mshTerminology, mshVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find descendant descriptors with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST9 - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, mshVersion, false, new PfsParameterJpa(),
          null); 
        fail("Exception should be thrown when trying to find descendant descriptors with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST10 - ");
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, null, false, new PfsParameterJpa(),
          "TTT"); 
        fail("Exception should be thrown when trying to find descendant descriptors with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST11 - "  );
    try {
      contentService.findDescendantDescriptors("D000005", mshTerminology, null, false, new PfsParameterJpa(),
          ""); 
        fail("Exception should be thrown when trying to find descendant descriptors with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
   
    // Test terminology is null    
    Logger.getLogger(getClass()).info(
    "TEST12 - " );
    try {
      contentService.findAncestorDescriptors("D000005", null, mshVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find ancestor descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test terminology is invalid
    Logger.getLogger(getClass()).info(
    "TEST13 - " );
    try {
      contentService.findAncestorDescriptors("D000005", "TTT", mshVersion, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find ancestor descriptors with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test terminology is empty string
    Logger.getLogger(getClass()).info(
    "TEST14 - " );
    try {
      contentService.findAncestorDescriptors("D000005", "", mshVersion, false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is null    
    Logger.getLogger(getClass()).info(
    "TEST15 - " );
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, null, false, new PfsParameterJpa(),
          authToken);      
      fail("Exception should be thrown when trying to find ancestor descriptors with null terminology.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test version is invalid
    Logger.getLogger(getClass()).info(
    "TEST16 - " );
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, "TTT", false, new PfsParameterJpa(),
          authToken);     
      fail("Exception should be thrown when trying to find ancestor descriptors with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test version is empty string
    Logger.getLogger(getClass()).info(
    "TEST17 - " );
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, "", false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string terminology.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test self id is null
    Logger.getLogger(getClass()).info(
    "TEST18 - " );
    try {
      contentService.findAncestorDescriptors(null, mshTerminology, mshVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find ancestor descriptors with null terminology id.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test self id is empty string
    Logger.getLogger(getClass()).info(
    "TEST19 - " );
    try {
      contentService.findAncestorDescriptors("", mshTerminology, mshVersion, false, new PfsParameterJpa(),
          authToken);       
      fail("Exception should be thrown when trying to find ancestor descriptors with empty string terminology id.");
    } catch (Exception e) {
      // do nothing
    }   
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST20 - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, mshVersion, false, new PfsParameterJpa(),
          null); 
        fail("Exception should be thrown when trying to find ancestor descriptors with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST21 - ");
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, null, false, new PfsParameterJpa(),
          "TTT"); 
        fail("Exception should be thrown when trying to find ancestor descriptors with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST22 - "  );
    try {
      contentService.findAncestorDescriptors("D000005", mshTerminology, null, false, new PfsParameterJpa(),
          ""); 
        fail("Exception should be thrown when trying to find ancestor descriptors with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }  
  
  @Test
  public void testDegenerateUseRestContent013() throws Exception {
    // n/a - no code ancestors or descendants
    // TODO: consider sample data from SAMPLE_2014AB
  }

 
  /**
   * Test "get" subset members for atom or concept.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent014() throws Exception {
    /*Logger.getLogger(getClass()).info("Start test");

    Logger.getLogger(getClass()).info("  Test get subset members for concept");
    list =
        contentService.getSubsetMembersForConcept("10123006",
            snomedTerminology, snomedVersion, authToken);
    Logger.getLogger(getClass()).info(
        "    totalCount = " + list.getTotalCount());
    assertEquals(5, list.getTotalCount());
    assertEquals(5, list.getCount());
*/
    
    Logger.getLogger(getClass()).debug("Start test");


    // Test with null terminologyId
    Logger.getLogger(getClass()).info(
        "TEST1");
    try {
      contentService.getSubsetMembersForAtom(null, snomedTerminology, snomedVersion,
            authToken);
      fail("Exception should be thrown when trying to get a concept with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid terminologyId
    // TODO: why is this not throwing NoResultException?
    /*Logger.getLogger(getClass()).info(
        "TEST2 ");
    try {
       contentService.getSubsetMembersForAtom("-1", snomedTerminology, snomedVersion,
            authToken);
       fail("Exception should be thrown when trying to get a concept with invalid terminologyId.");
     } catch (Exception e) {
       // do nothing
     }*/
    
    // Test with empty string terminologyId
    Logger.getLogger(getClass()).info(
        "TEST3 ");
    try {
        contentService.getSubsetMembersForAtom("", umlsTerminology, umlsVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST4");
    try {
        contentService.getSubsetMembersForAtom("166113012", null, snomedVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with null terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST5 " );
    try {
        contentService.getSubsetMembersForAtom("166113012", "TTT", snomedVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST6 " );
    try {
        contentService.getSubsetMembersForAtom("166113012", "", snomedVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST7");
    try {
        contentService.getSubsetMembersForAtom("166113012", snomedTerminology, null,
            authToken);
        fail("Exception should be thrown when trying to get a concept with null version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST8" );
    try {
        contentService.getSubsetMembersForAtom("166113012", "MSH", "TTT",
            authToken);
        fail("Exception should be thrown when trying to get a concept with invalid version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST9");
    try {
        contentService.getSubsetMembersForAtom("166113012", "MSH", "",
            authToken);
        fail("Exception should be thrown when trying to get a concept with empty string version.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST10" );
    try {
        contentService.getSubsetMembersForAtom("166113012", "MSH", "2015_2014_09_08",
            null);
        fail("Exception should be thrown when trying to get a concept with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST11" );
    try {
        contentService.getSubsetMembersForAtom("166113012", "MSH", "2015_2014_09_08",
            "TTT");
        fail("Exception should be thrown when trying to get a concept with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST12 " );
    try {
        contentService.getSubsetMembersForAtom("166113012", "MSH", "2015_2014_09_08",
            "");
        fail("Exception should be thrown when trying to get a concept with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null terminologyId
    Logger.getLogger(getClass()).info(
        "TEST13");
    try {
      contentService.getSubsetMembersForConcept(null, snomedTerminology, snomedVersion,
            authToken);
      fail("Exception should be thrown when trying to get a concept with null terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with invalid terminologyId
    // TODO: why is this not throwing NoResultException?
    /*Logger.getLogger(getClass()).info(
        "TEST14 ");
    try {
       contentService.getSubsetMembersForConcept("-1", snomedTerminology, snomedVersion,
            authToken);
       fail("Exception should be thrown when trying to get a concept with invalid terminologyId.");
     } catch (Exception e) {
       // do nothing
     }*/
    
    // Test with empty string terminologyId
    Logger.getLogger(getClass()).info(
        "TEST15 ");
    try {
        contentService.getSubsetMembersForConcept("", umlsTerminology, umlsVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with empty string terminologyId.");
    } catch (Exception e) {
      // do nothing
    }

    // Test with null terminology
    Logger.getLogger(getClass()).info(
        "TEST16");
    try {
        contentService.getSubsetMembersForConcept("10123006", null, snomedVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with null terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid terminology
    Logger.getLogger(getClass()).info(
        "TEST17 " );
    try {
        contentService.getSubsetMembersForConcept("10123006", "TTT", snomedVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with invalid terminology.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string terminology
    Logger.getLogger(getClass()).info(
        "TEST18 " );
    try {
        contentService.getSubsetMembersForConcept("10123006", "", snomedVersion,
            authToken);
        fail("Exception should be thrown when trying to get a concept with emtpy string terminology.");
    } catch (Exception e) {
      // do nothing
    }    
    
    // Test with null version
    Logger.getLogger(getClass()).info(
        "TEST19");
    try {
        contentService.getSubsetMembersForConcept("10123006", snomedTerminology, null,
            authToken);
        fail("Exception should be thrown when trying to get a concept with null version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with invalid version
    Logger.getLogger(getClass()).info(
        "TEST20" );
    try {
        contentService.getSubsetMembersForConcept("10123006", "MSH", "TTT",
            authToken);
        fail("Exception should be thrown when trying to get a concept with invalid version.");
    } catch (Exception e) {
      // do nothing
    }        
    
    // Test with empty string version
    Logger.getLogger(getClass()).info(
        "TEST21");
    try {
        contentService.getSubsetMembersForConcept("10123006", "MSH", "",
            authToken);
        fail("Exception should be thrown when trying to get a concept with empty string version.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null authToken
    Logger.getLogger(getClass()).info(
        "TEST22" );
    try {
        contentService.getSubsetMembersForConcept("10123006", "MSH", "2015_2014_09_08",
            null);
        fail("Exception should be thrown when trying to get a concept with null authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid authToken
    Logger.getLogger(getClass()).info(
        "TEST23" );
    try {
        contentService.getSubsetMembersForConcept("10123006", "MSH", "2015_2014_09_08",
            "TTT");
        fail("Exception should be thrown when trying to get a concept with invalid authToken.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with empty string authToken
    Logger.getLogger(getClass()).info(
        "TEST24 " );
    try {
        contentService.getSubsetMembersForConcept("10123006", "MSH", "2015_2014_09_08",
            "");
        fail("Exception should be thrown when trying to get a concept with empty string authToken.");
    } catch (Exception e) {
      // do nothing
    }
  }
  
 
  /**
   * Test autocomplete for concepts.
   *
   * @throws Exception the exception
   */
  @Test
  public void testDegenerateUseRestContent015() throws Exception {
    Logger.getLogger(getClass()).info("Start test");

    // Test with empty string searchTerm
    Logger.getLogger(getClass()).info(
        "TEST1 " );
    // TODO: should an exception be thrown here?
    try {
      contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
          "", authToken);
        fail("Exception should be thrown when trying to autocomplete with empty string searchTerm.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with null searchTerm
    Logger.getLogger(getClass()).info(
        "TEST2 " );
    try {
      contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
          null, authToken);
        fail("Exception should be thrown when trying to autocomplete with null searchTerm.");
    } catch (Exception e) {
      // do nothing
    }
    
    // Test with invalid searchTerm
    Logger.getLogger(getClass()).info(
        "TEST3 " );
    try {
      contentService.autocompleteConcepts(snomedTerminology, snomedVersion,
          "qrs", authToken);
    } catch (Exception e) {
      fail("Exception should NOT be thrown when trying to autocomplete with invalid searchTerm.");
    }
  }
  
  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @Override
  @After
  public void teardown() throws Exception {

    // logout
    securityService.logout(authToken);
  }

  /**
   * Returns the parameter types.
   *
   * @param parameters the parameters
   * @return the parameter types
   */
  @SuppressWarnings("static-method")
  public Class<?>[] getParameterTypes(Object[] parameters) {
    Class<?>[] types = new Class<?>[parameters.length];
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].toString().contains("oolean"))
        types[i] = boolean.class;
      else
        types[i] = parameters[i].getClass();
      
    }
    return types;
  }

}
