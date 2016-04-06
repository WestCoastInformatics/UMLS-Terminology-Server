package com.wci.umls.server.jpa;

import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * Support for integration tests
 */
public class ModelUnitSupport {

  /** The name. */
  @Rule
  public TestName name = new TestName();

}