/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.services.ContentService;
import com.wci.umls.server.services.handlers.ExceptionHandler;

/**
 * QA Check for Properly Numbered Map Groups
 * 
 * See admin/qa/pom.xml for a sample execution.
 * 
 * @goal qa-database
 * @phase package
 */
public class QaDatabase extends AbstractMojo {

  /**
   * The queries
   * @parameter
   * @required
   */
  private Properties queries;

  /** The manager. */
  EntityManager manager;

  /**
   * Executes the plugin.
   * 
   * @throws MojoExecutionException the mojo execution exception
   * @throws MojoFailureException
   */
  @SuppressWarnings("unchecked")
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("Starting database QA");

    try {

      // Obtain an entity manager;
      ContentService service = new ContentServiceJpa() {
        {
          QaDatabase.this.manager = manager;
        }
      };

      Map<String, List<String>> errors = new HashMap<>();

      // Iterate through queries, execute and report
      for (Object property : queries.keySet()) {
        String queryStr =
            queries.getProperty(property.toString()).replace(";", "");
        getLog().info("  " + property);
        getLog().info("    " + queryStr);

        // Get and execute query (truncate any trailing semi-colon)
        Query query = manager.createNativeQuery(queryStr);
        query.setMaxResults(10);
        List<Object[]> objects = query.getResultList();

        // Expect zero count, any results are failures
        if (objects.size() > 0) {
          List<String> results = new ArrayList<>();
          for (Object[] array : objects) {
            StringBuilder sb = new StringBuilder();
            for (Object o : array) {
              sb.append((o != null ? o.toString() : "null")).append(",");
            }
            results.add(sb.toString().replace(",$", ""));
          }
          errors.put(property.toString(), results);
        }

      }

      // Check for errors and report the
      if (!errors.isEmpty()) {
        StringBuilder msg = new StringBuilder();
        msg.append("\r\n");
        msg.append("The automated database QA mojo has found some issues with the following checks:\r\n");
        msg.append("\r\n");

        for (String key : errors.keySet()) {
          msg.append("  CHECK: ").append(key).append("\r\n");
          msg.append("  QUERY: ").append(queries.getProperty(key))
              .append("\r\n");
          for (String result : errors.get(key)) {
            msg.append("    " + result).append("\r\n");
          }
          if (errors.get(key).size() > 9) {
            msg.append("    ... ");
            // the true count is not known because setMaxResults(10) is used.
          }

        }

        Properties config = ConfigUtility.getConfigProperties();
        if (config.getProperty("mail.enabled") != null
            && config.getProperty("mail.enabled").equals("true")
            && config.getProperty("mail.smtp.to") != null) {
          ConfigUtility.sendEmail("[Terminology Server] Database QA Results",
              config.getProperty("mail.smtp.user"),
              config.getProperty("mail.smtp.to"), msg.toString(), config,
              "true".equals(config.get("mail.smtp.auth")));
        }

        throw new Exception("Failure to pass QA checks. " + errors);
      } else {
        getLog().info("  NO errors");
      }

      // cleanup
      service.close();
      getLog().info("Done ...");

    } catch (Exception e) {
      // Send email if something went wrong
      try {
        ExceptionHandler.handleException(e, "Error running QA Checks");
      } catch (Exception e1) {
        e1.printStackTrace();
        throw new MojoFailureException(e.getMessage());
      }

    }

  }
}