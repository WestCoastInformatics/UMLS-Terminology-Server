/*
 * Copyright 2016 West Coast Informatics, LLC
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
public class EqualsHashcodeBadFieldTest extends ModelUnitSupport {

  /** The paths. */
  private static Set<Path> paths;

  /**
   * Setup class.
   * @throws IOException
   */
  @BeforeClass
  public static void setupClass() throws IOException {
    // Find all java model objects
    paths =
        Files.find(Paths.get("src/main/java"), Integer.MAX_VALUE,
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
  public void testEquals() throws Exception {
    Logger.getLogger(getClass()).debug("TEST " + name.getMethodName());

    for (final Path path : paths) {
      final String method = getMethodText("public boolean equals", path);
      if (!method.isEmpty()) {
        // Assert id is not used
        assertFalse(path.getFileName().toString()
            + " has an equals method that uses 'id' ",
            method.contains("id == null"));
        // Assert lastModified is not used
        assertFalse(path.getFileName().toString()
            + " has an equals method that uses 'lastModified' ",
            method.contains("lastModified == null"));
        // Assert lastModified is not used
        assertFalse(path.getFileName().toString()
            + " has an equals method that uses 'lastModifiedBy' ",
            method.contains("lastModifiedBy == null"));
        // Assert id is not used
        assertFalse(path.getFileName().toString()
            + " has an equals method that uses 'timestamp' ",
            method.contains("timestamp == null"));
        // Assert id is not used
        assertFalse(path.getFileName().toString()
            + " has an equals method that uses 'alternateTerminologyIds' ",
            method.contains("alternateTerminologyIds == null"));
      }
    }

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
        assertFalse(path.getFileName().toString()
            + " has an hashcode method that uses 'id' ",
            method.contains("id == null"));
        // Assert lastModified is not used
        assertFalse(path.getFileName().toString()
            + " has an hashcode method that uses 'lastModified' ",
            method.contains("lastModified == null"));
        // Assert lastModified is not used
        assertFalse(path.getFileName().toString()
            + " has an hashcode method that uses 'lastModifiedBy' ",
            method.contains("lastModifiedBy == null"));
        // Assert id is not used
        assertFalse(path.getFileName().toString()
            + " has an hashcode method that uses 'timestamp' ",
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
