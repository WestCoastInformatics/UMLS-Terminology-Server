package com.wci.umls.server.mojo;

import java.text.SimpleDateFormat;
import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
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
 */
@Mojo(name = "load-owl", defaultPhase = LifecyclePhase.PACKAGE)
public class TerminologyOwlLoaderMojo extends AbstractLoaderMojo {

  /** The date format. */
  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmdd");

  /**
   * The input file.
   */
  @Parameter
  String inputFile = null;

  /**
   * Name of terminology to be loaded.
   */
  @Parameter
  String terminology;

  /**
   * version.
   */
  @Parameter
  String version;

  /**
   * Whether to run this mojo against an active server
   */
  @Parameter
  private boolean server = false;

  /**
   * Mode - for recreating db
   */
  @Parameter
  private String mode = null;

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("Starting loading Owl");
      getLog().info("  terminology = " + terminology);
      getLog().info("  version = " + version);
      getLog().info("  inputFile = " + inputFile);
      getLog().info("  Expect server up   : " + server);
      getLog().info("  Mode               : " + mode);

      Properties properties = ConfigUtility.getConfigProperties();

      boolean serverRunning = ConfigUtility.isServerActive();

      getLog()
          .info("Server status detected:  " + (!serverRunning ? "DOWN" : "UP"));

      if (serverRunning && !server) {
        throw new MojoFailureException(
            "Mojo expects server to be down, but server is running");
      }

      if (!serverRunning && server) {
        throw new MojoFailureException(
            "Mojo expects server to be running, but server is down");
      }

      // Create the database
      if (mode != null && mode.equals("create")) {
        createDb(serverRunning);
      }

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();

      if (!serverRunning) {
        getLog().info("Running directly");

        getLog().info("  Remove concepts");
        ContentServiceRest contentService = new ContentServiceRestImpl();
        contentService.loadTerminologyOwl(terminology, version, inputFile,
            authToken);

      } else {
        getLog().info("Running against server");

        getLog().info("  Remove concepts");
        ContentClientRest contentService = new ContentClientRest(properties);

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