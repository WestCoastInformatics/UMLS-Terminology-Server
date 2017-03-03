/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.code;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wci.umls.server.jpa.ModelUnitSupport;

/**
 * Unit testing to ensure equals methods of model classes do not ever use these
 * fields in equality testing:
 * 
 * <pre>
 * 1. id
 * 2. lastModified
 * 3. lastModifiedBy
 * 4. timestamp
 * </pre>
 */
public class SetIsTest extends ModelUnitSupport {

  /** The paths. */
  private static Set<Path> paths;

  /**
   * Setup class.
   * @throws IOException
   */
  @BeforeClass
  public static void setupClass() throws IOException {
    // Find all java model objects
    paths = Files
        .find(Paths.get("src/main/java"), Integer.MAX_VALUE,
            (filePath, fileAttr) -> filePath.toString().endsWith(".java"))
        .collect(Collectors.toSet());
  }

  /**
   * Setup.
   * @throws Exception
   */
  @Before
  public void setup() throws Exception {
    // n/a
  }

  /**
   * Test equals methods for offending fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testSetIs() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    for (final Path path : paths) {
      final String method = getMethodText(" setIs", path);
      if (!method.isEmpty()) {
        fail("No method should start with setIsXXX, use setXXX instead - "
            + path);
      }
    }

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
