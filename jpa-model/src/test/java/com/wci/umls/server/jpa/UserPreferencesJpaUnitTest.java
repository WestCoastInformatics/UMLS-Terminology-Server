/*
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa;

import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.User;
import com.wci.umls.server.UserPreferences;
import com.wci.umls.server.helpers.CopyConstructorTester;
import com.wci.umls.server.helpers.EqualsHashcodeTester;
import com.wci.umls.server.helpers.GetterSetterTester;
import com.wci.umls.server.helpers.ProxyTester;
import com.wci.umls.server.helpers.XmlSerializationTester;

/**
 * Unit testing for {@link UserPreferencesJpa}.
 */
public class UserPreferencesJpaUnitTest extends ModelUnitSupport {

  /** The model object to test. */
  private UserPreferencesJpa object;

  /** The u1. */
  private User u1;

  /** The u2. */
  private User u2;

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
    object = new UserPreferencesJpa();
    ProxyTester tester = new ProxyTester(new UserJpa());
    u1 = (User) tester.createObject(1);
    u2 = (User) tester.createObject(2);
  }

  /**
   * Test getter and setter methods of model object.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelGetSet() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    GetterSetterTester tester = new GetterSetterTester(object);
    tester.exclude("userName");
    tester.exclude("userId");
    tester.exclude("precedenceList");
    tester.test();
  }

  /**
   * Test equals and hascode methods.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelEqualsHashcode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    EqualsHashcodeTester tester = new EqualsHashcodeTester(object);
    tester.include("user");
    tester.include("feedbackEmail");
    tester.include("lastTab");
    tester.include("lastProjectId");
    tester.include("lastTerminology");

    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);

    assertTrue(tester.testNonIdentityFieldEquals());
    // the "setUserName" is actually changing the u1 object to have a username
    // of u2 so we need to recreate
    ProxyTester tester2 = new ProxyTester(new UserJpa());
    u1 = (UserJpa) tester2.createObject(1);
    u2 = (UserJpa) tester2.createObject(2);
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);
    assertTrue(tester.testIdentityFieldNotEquals());
    assertTrue(tester.testIdentityFieldHashcode());
    assertTrue(tester.testNonIdentityFieldHashcode());

    // the "setUserName" is actually changing the u1 object to have a username
    // of u2 so we need to recreate
    u1 = (UserJpa) tester2.createObject(1);
    u2 = (UserJpa) tester2.createObject(2);
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);
    assertTrue(tester.testIdentityFieldDifferentHashcode());
  }

  /**
   * Test copy constructor.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelCopy() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    CopyConstructorTester tester = new CopyConstructorTester(object);

    // Set up objects
    tester.proxy(User.class, 1, u1);
    tester.proxy(User.class, 2, u2);

    assertTrue(tester.testCopyConstructor(UserPreferences.class));
  }

  /**
   * Test XML serialization.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelXmlSerialization() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());
    XmlSerializationTester tester = new XmlSerializationTester(object);

    // Set up objects
    User u = new UserJpa();
    u.setId(1L);
    u.setUserName("1");
    tester.proxy(User.class, 1, u);

    assertTrue(tester.testXmlSerialization());
  }

  /**
   * Test not null fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testModelNotNullField() throws Exception {
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
