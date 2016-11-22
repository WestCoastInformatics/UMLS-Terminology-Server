/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.jpa;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.wci.umls.server.test.helpers.IntegrationUnitSupport;

/**
 * Test to run all of the insertion loader algorithm tests.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class InsertionLoaderAlgorithmsTest extends IntegrationUnitSupport {

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
    // Do nothing
  }

  /**
   * Test metadata loader normal use.
   *
   * @throws Exception the exception
   */
  @Test
  public void test1MetadataLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());

    MetadataLoaderAlgorithmTest algo = new MetadataLoaderAlgorithmTest();
    algo.setup();
    algo.testMetadataLoader();
    algo.teardown();    
  }
  
  /**
   * Test atom loader.
   *
   * @throws Exception the exception
   */
  @Test
  public void test2AtomLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    AtomLoaderAlgorithmTest algo2 = new AtomLoaderAlgorithmTest();
    algo2.setup();
    algo2.testAtomLoader();
    algo2.teardown();       
  }  

  /**
   * Test relationship loader.
   *
   * @throws Exception the exception
   */
  @Test
  public void test3RelationshipLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    RelationshipLoaderAlgorithmTest algo2 = new RelationshipLoaderAlgorithmTest();
    algo2.setup();
    algo2.testRelationshipLoader();
    algo2.teardown();       
  }    
  
  /**
   * Test context loader.
   *
   * @throws Exception the exception
   */
  @Test
  public void test4ContextLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    ContextLoaderAlgorithmTest algo2 = new ContextLoaderAlgorithmTest();
    algo2.setup();
    algo2.testContextLoader();
    algo2.teardown();       
  }    
  
  /**
   * Test semantic type loader.
   *
   * @throws Exception the exception
   */
  @Test
  public void test5SemanticTypeLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    SemanticTypeLoaderAlgorithmTest algo2 = new SemanticTypeLoaderAlgorithmTest();
    algo2.setup();
    algo2.testSemanticTypeLoader();
    algo2.teardown();       
  }    
  
  /**
   * Test attribute loader.
   *
   * @throws Exception the exception
   */
  @Test
  public void test6AttributeLoader() throws Exception {
    Logger.getLogger(getClass()).info("TEST " + name.getMethodName());
    
    AttributeLoaderAlgorithmTest algo2 = new AttributeLoaderAlgorithmTest();
    algo2.setup();
    algo2.testAttributeLoader();
    algo2.teardown();       
  }    
  
  /**
   * Teardown.
   *
   * @throws Exception the exception
   */
  @After
  public void teardown() throws Exception {
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
