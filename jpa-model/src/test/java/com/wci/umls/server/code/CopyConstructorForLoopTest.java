/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.code;

import static org.junit.Assert.assertFalse;

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
 * Unit testing to ensure copy constructors of model classes do not use "for ("
 * The current thinking is that collections are copied but contents are not.
 * Objects references are left unchanged. The alternative would be to ALWAYS
 * copy them (or use some variation of "deep" for that).
 */
public class CopyConstructorForLoopTest extends ModelUnitSupport {

  /** The paths. */
  private static Set<Path> paths;

  /**
   * Setup class.
   *
   * @throws IOException Signals that an I/O exception has occurred.
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
   *
   * @throws Exception the exception
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
  public void testEquals() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    boolean found = false;
    for (final Path path : paths) {
      String pathName = path.toString();
      String className = pathName.substring(
          Math.max(pathName.lastIndexOf("/"), pathName.lastIndexOf("\\")) + 1)
          .replace(".java", "");
      final String method = getMethodText(
          "public " + className + "(" + className.substring(0, 1), path);
      if (!method.isEmpty()) {
        // Assert id is not used
        if (method.contains("for (")) {
          found = true;
          Logger.getLogger(getClass()).info("  for loop = " + className);
        }
      }
    }
    assertFalse("Found problems in copy constructors, see log.", found);

  }

  /**
   * Test hashcode methods for offending fields.
   *
   * @throws Exception the exception
   */
  @Test
  public void testHashcode() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    for (final Path path : paths) {
      final String method = getMethodText("public int hashCode", path);
      if (!method.isEmpty()) {
        // Assert id is not used
        assertFalse(
            path.getFileName().toString()
                + " has an equals method that uses 'id' ",
            method.contains("id == null"));
        // Assert lastModified is not used
        assertFalse(
            path.getFileName().toString()
                + " has an equals method that uses 'id' ",
            method.contains("lastModified == null"));
        // Assert lastModified is not used
        assertFalse(
            path.getFileName().toString()
                + " has an equals method that uses 'lastModifiedBy' ",
            method.contains("lastModifiedBy == null"));
        // Assert id is not used
        assertFalse(
            path.getFileName().toString()
                + " has an equals method that uses 'timestamp' ",
            method.contains("timestamp == null"));
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
