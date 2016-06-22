package com.wci.umls.server.test.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.rules.TestName;

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
  @SuppressWarnings("static-method")
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
}