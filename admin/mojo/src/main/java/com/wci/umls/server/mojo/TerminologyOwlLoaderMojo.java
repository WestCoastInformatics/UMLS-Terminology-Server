package com.wci.umls.server.mojo;

import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.ContentServiceRest;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Converts owl data to RF2 objects.
 * 
 * See admin/loader/pom.xml for a sample execution.
 * 
 * @goal load-owl
 * @phase package
 */
public class TerminologyOwlLoaderMojo extends AbstractMojo {

  /** The date format. */
  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmdd");

  /**
   * The input file.
   *
   * @parameter
   * @required
   */
  String inputFile;

  /**
   * Name of terminology to be loaded.
   * @parameter
   * @required
   */
  String terminology;

  /**
   * Terminology version.
   *
   * @parameter
   * @required
   */
  String version;

  /**
   * Whether to run this mojo against an active server
   * @parameter
   */
  private boolean server = false;

  /**
   * Mode - for recreating db
   * @parameter
   */
  private String mode = null;

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("Starting removing terminology");
      getLog().info("  terminology = " + terminology);
      getLog().info("  version = " + version);
      getLog().info("  inputFile = " + inputFile);
      getLog().info("  Expect server up   : " + server);
      getLog().info("  Mode               : " + mode);

      Properties properties = ConfigUtility.getConfigProperties();

      if (mode != null && mode.equals("create")) {
        getLog().info("Recreate database");
        // This will trigger a rebuild of the db
        properties.setProperty("hibernate.hbm2ddl.auto", mode);
        // Trigger a JPA event
        new MetadataServiceJpa().close();
        properties.remove("hibernate.hbm2ddl.auto");

      }

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
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();

      if (!serverRunning) {
        getLog().info("Running directly");

        // Handle reindexing
        if (mode != null && mode.equals("create")) {
          ContentServiceRestImpl contentService = new ContentServiceRestImpl();
          contentService.luceneReindex(null, authToken);
        }

        getLog().info("  Remove concepts");
        ContentServiceRest contentService = new ContentServiceRestImpl();
        contentService.loadTerminologyOwl(terminology, version, inputFile,
            authToken);

      } else {
        getLog().info("Running against server");

        getLog().info("  Remove concepts");
        ContentClientRest contentService = new ContentClientRest(properties);

        // handle reindexing
        if (mode != null && mode.equals("create")) {
          contentService.luceneReindex(null, authToken);
        }

        contentService.loadTerminologyOwl(terminology, version, inputFile,
            authToken);

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