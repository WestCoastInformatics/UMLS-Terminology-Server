/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which loads a set of RRF into a database.
 * 
 * See admin/loader/pom.xml for sample usage
 * 
 * @goal load-rrf-single
 * 
 * @phase package
 */
public class TerminologyRrfSingleLoaderMojo extends AbstractMojo {

  /**
   * Name of terminology to be loaded.
   * @parameter
   * @required
   */
  private String terminology;

  /**
   * The terminology version.
   * @parameter
   * @required
   */
  private String version;

  /**
   * The terminology version.
   * @parameter
   */
  private String prefix;

  /**
   * Input directory.
   * @parameter
   * @required
   */
  private String inputDir;

  /**
   * Whether to run this mojo against an active server.
   *
   * @parameter
   */
  private boolean server = false;

  /**
   * Mode - for recreating db
   * @parameter
   */
  private String mode = null;

  /**
   * Instantiates a {@link TerminologyRrfSingleLoaderMojo} from the specified
   * parameters.
   * 
   */
  public TerminologyRrfSingleLoaderMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("RRF Single Terminology Loader called via mojo.");
      getLog().info("  Terminology        : " + terminology);
      getLog().info("  Terminology Version: " + version);
      getLog().info("  Input directory    : " + inputDir);
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

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      if (!serverRunning) {
        getLog().info("Running directly");

        // Handle reindexing
        if (mode != null && mode.equals("create")) {
          ContentServiceRestImpl contentService = new ContentServiceRestImpl();
          contentService.luceneReindex(null, authToken);
        }

        ContentServiceRestImpl contentService = new ContentServiceRestImpl();
        contentService.loadTerminologyRrf(terminology, version, true, true,
            prefix == null ? "MR" : prefix, inputDir, authToken);

      } else {
        getLog().info("Running against server");

        // invoke the client
        ContentClientRest client = new ContentClientRest(properties);

        // handle reindexing
        if (mode != null && mode.equals("create")) {
          client.luceneReindex(null, authToken);
        }

        client.loadTerminologyRrf(terminology, version, true, true, inputDir,
            prefix == null ? "MR" : prefix, authToken);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
