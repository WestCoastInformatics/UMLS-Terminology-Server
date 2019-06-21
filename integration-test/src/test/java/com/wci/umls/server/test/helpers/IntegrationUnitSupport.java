/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.test.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.rules.TestName;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;

/**
 * Support for integration tests
 */
public class IntegrationUnitSupport {

  /** The name. */
  @Rule
  public TestName name = new TestName();

  /**
   * Returns the method.
   *
   * @param match the match
   * @param file the file
   * @return the method
   * @throws IOException Signals that an I/O exception has occurred.
   */
  
  public String getMethodText(String match, Path file) throws IOException {
    final List<String> lines = Files.lines(file).collect(Collectors.toList());
    final StringBuilder sb = new StringBuilder();
    boolean inMethod = false;
    for (final String line : lines) {

      // Method start
      if (line.contains(match)) {
        inMethod = true;
      }

      if (inMethod) {
        sb.append(line);
      }
      // Method end
      // TODO: this could be better, relies on formatter
      if (inMethod && line.startsWith("  }")) {
        inMethod = false;
        break;
      }

    }
    return sb.toString();

  }

  /**
   * Test action preconditions.
   *
   * @param action the action
   * @return the validation result
   * @throws Exception the exception
   */
  
  public ValidationResult checkActionPreconditions(
    AbstractMolecularAction action) throws Exception {

    action.beginTransaction();
    action.initialize(action.getProject(), action.getConceptId(),
        action.getConceptId2(), action.getLastModified(), false);
    final ValidationResult validationResult = action.checkPreconditions();
    action.rollback();
    action.close();

    return validationResult;

  }
}