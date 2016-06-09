/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.helpers.workflow;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;
import com.wci.umls.server.jpa.helpers.IndexedFieldTester;
import com.wci.umls.server.jpa.helpers.NullableFieldTester;
import com.wci.umls.server.jpa.worfklow.TrackingRecordJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowBinJpa;
import com.wci.umls.server.jpa.worfklow.WorkflowEpochJpa;
import com.wci.umls.server.model.workflow.TrackingRecord;
import com.wci.umls.server.model.workflow.WorkflowBin;
import com.wci.umls.server.model.workflow.WorkflowEpoch;

/**
 * Unit testing for {@link WorkflowEpochJpa}.
 */
public class WorkflowBinUnitTest {

  /** The model object to test. */
  private WorkflowBinJpa object;

  /** The a1. */
  private TrackingRecord a1;

  /** The a2. */
  private TrackingRecord a2;
  
  /** The b1. */
  private WorkflowEpoch b1;
  
  /** The b2. */
  private WorkflowEpoch b2;


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
    object = new WorkflowBinJpa();
    ProxyTester tester = new ProxyTester(new TrackingRecordJpa());
    a1 = (TrackingRecordJpa) tester.createObject(1);
    a2 = (TrackingRecordJpa) tester.createObject(2);
    
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelGetSet041");
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelEqualsHashcode041");
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);

    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.include("name");
    tester.include("description");
    tester.include("type");
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    tester.include("clusterId");
    tester.include("creationTime");
    tester.include("rank");
    tester.include("editable");
    tester.include("workflowClusterTypes");

    tester.proxy(TrackingRecord.class, 1, a1);
    tester.proxy(TrackingRecord.class, 2, a2);
      
    assertTrue(tester.testIdentityFieldEquals());
    assertTrue(tester.testNonIdentityFieldEquals());
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test deep copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelDeepCopy041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelDeepCopy041");

    CopyConstructorTester tester = new CopyConstructorTester(object);
    tester.proxy(TrackingRecord.class, 1, a1);
    tester.proxy(TrackingRecord.class, 2, a2);
    tester.proxy(WorkflowEpoch.class, 1, b1);
    tester.proxy(WorkflowEpoch.class, 2, b2);
    assertTrue(tester.testCopyConstructorDeep(WorkflowBin.class));

  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelXmlSerialization041");
    XmlSerializationTester tester = new XmlSerializationTester(object);
    // The proxy concepts can have only "id" and "term" set due to xml transient
    TrackingRecord tr1 = new TrackingRecordJpa();
    tr1.setId(1L);
    TrackingRecord tr2 = new TrackingRecordJpa();
    tr2.setId(2L);

    tester.proxy(TrackingRecord.class, 1, tr1);
    tester.proxy(TrackingRecord.class, 2, tr2);
    tester.proxy(TrackingRecord.class, 1, a1);
    tester.proxy(TrackingRecord.class, 2, a2);
    
    WorkflowEpoch we1 = (WorkflowEpoch) new WorkflowEpochJpa();
    we1.setId(1L);
    WorkflowEpoch we2 = (WorkflowEpoch) new WorkflowEpochJpa();
    we2.setId(2L);

    tester.proxy(WorkflowEpoch.class, 1, we1);
    tester.proxy(WorkflowEpoch.class, 2, we2);
    tester.proxy(WorkflowEpoch.class, 1, b1);
    tester.proxy(WorkflowEpoch.class, 2, b2);
    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField041() throws Exception {
    NullableFieldTester tester = new NullableFieldTester(object);
    tester.include("timestamp");
    tester.include("lastModified");
    tester.include("lastModifiedBy");
    
    tester.include("terminology");
    tester.include("terminologyId");
    tester.include("version");

    tester.include("name");
    tester.include("description");
    tester.include("type");
    tester.include("clusterId");
    tester.include("creationTime");
    tester.include("rank");
    tester.include("editable");
    tester.include("workflowClusterTypes");

    assertTrue(tester.testNotNullFields());
  }

  /**
   * Test field indexing.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelIndexedFields041() throws Exception {
    Logger.getLogger(getClass()).debug("TEST testModelIndexedFields041");

    // Test analyzed fields
    IndexedFieldTester tester = new IndexedFieldTester(object);
    tester.include("name");
    tester.include("description");
    assertTrue(tester.testAnalyzedIndexedFields());

    // Test non analyzed fields
    assertTrue(tester.testAnalyzedIndexedFields());
    tester = new IndexedFieldTester(object);
    tester.include("lastModifiedBy");
    tester.include("terminologyId");
    tester.include("terminology");
    tester.include("version");
    tester.include("type");
    tester.include("clusterId");

    assertTrue(tester.testNotAnalyzedIndexedFields());
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
//