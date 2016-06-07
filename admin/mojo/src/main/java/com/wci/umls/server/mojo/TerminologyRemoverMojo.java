/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.ReleaseInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.jpa.services.rest.HistoryServiceRest;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.rest.client.HistoryClientRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.rest.impl.HistoryServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which removes a terminology from a database.
 * 
 * See admin/remover/pom.xml for sample usage
 * 
 * @goal remove-terminology
 * 
 * @phase package
 */
public class TerminologyRemoverMojo extends AbstractMojo {

  /**
   * Name of terminology to be removed.
   * @parameter
   * @required
   */
  private String terminology;

  /**
   * version to remove.
   * @parameter
   * @required
   */
  private String version;

  /**
   * Whether to run this mojo against an active server
   * @parameter
   */
  private boolean server = false;

  /**
   * Instantiates a {@link TerminologyRemoverMojo} from the specified
   * parameters.
   * 
   */
  public TerminologyRemoverMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Starting removing terminology");
    getLog().info("  terminology = " + terminology);
    getLog().info("  version = " + version);
    try {

      final Properties properties = ConfigUtility.getConfigProperties();

      boolean serverRunning = ConfigUtility.isServerActive();

      getLog().info(
          "Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));

      if (serverRunning && !server) {
        throw new MojoFailureException(
            "Mojo expects server to be down, but server is running");
      }

      if (!serverRunning && server) {
        throw new MojoFailureException(
            "Mojo expects server to be running, but server is down");
      }

      // authenticate
      final SecurityService service = new SecurityServiceJpa();
      final String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();

      if (!serverRunning) {
        getLog().info("Running directly");

        getLog().info("  Remove concepts");
        final ContentServiceRest contentService = new ContentServiceRestImpl();
        contentService.removeTerminology(terminology, version, authToken);

        getLog().info("  Remove release info");
        final HistoryServiceRest historyService = new HistoryServiceRestImpl();
        for (final ReleaseInfo info : historyService.getReleaseHistory(
            terminology, authToken).getObjects()) {
          // Need to open a second one to reopen security service
          HistoryServiceRest historyService2 = new HistoryServiceRestImpl();
          if (info.getTerminology().equals(terminology)
              && info.getVersion().equals(version)) {
            historyService2.removeReleaseInfo(info.getId(), authToken);
          }
        }

      } else {
        getLog().info("Running against server");

        getLog().info("  Remove concepts");
        final ContentClientRest contentService =
            new ContentClientRest(properties);
        contentService.removeTerminology(terminology, version, authToken);

        getLog().info("  Remove release info");
        final HistoryClientRest historyService =
            new HistoryClientRest(properties);
        for (final ReleaseInfo info : historyService.getReleaseHistory(
            terminology, authToken).getObjects()) {
          if (info.getTerminology().equals(terminology)
              && info.getVersion().equals(version)) {
            historyService.removeReleaseInfo(info.getId(), authToken);
          }
        }
      }
      service.close();

      getLog().info("done ...");
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
