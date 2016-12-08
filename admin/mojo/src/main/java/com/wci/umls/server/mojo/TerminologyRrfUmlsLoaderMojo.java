/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.util.Properties;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which loads a set of RRF into a database.
 * 
 * See admin/loader/pom.xml for sample usage
 */
@Mojo(name = "load-rrf-umls", defaultPhase = LifecyclePhase.PACKAGE)
public class TerminologyRrfUmlsLoaderMojo extends AbstractLoaderMojo {

  /**
   * Name of terminology to be loaded.
   */
  @Parameter
  private String terminology;

  /**
   * The version.
   */
  @Parameter
  private String version;

  /**
   * The version.
   */
  @Parameter
  private String prefix;

  /**
   * Input directory.
   */
  @Parameter
  private String inputDir;

  /**
   * Whether to run this mojo against an active server.
   */
  @Parameter
  private boolean server = false;

  /**
   * Whether to run this in edit mode.
   */
  @Parameter
  private boolean editMode = false;

  /**
   * Mode - for recreating db.
   */
  @Parameter
  private String mode = null;

  /**
   * Instantiates a {@link TerminologyRrfUmlsLoaderMojo} from the specified
   * parameters.
   * 
   */
  public TerminologyRrfUmlsLoaderMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("RRF UMLS Terminology Loader called via mojo.");
      getLog().info("  Terminology        : " + terminology);
      getLog().info("  version: " + version);
      getLog().info("  Input directory    : " + inputDir);
      getLog().info("  Edit Mode          : " + editMode);
      getLog().info("  Expect server up   : " + server);
      getLog().info("  Mode               : " + mode);

      Properties properties = ConfigUtility.getConfigProperties();

      // Rebuild the database
      if (mode != null && mode.equals("create")) {
        createDb(ConfigUtility.isServerActive());
      }

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

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

      if (!serverRunning) {
        getLog().info("Running directly");

        ContentServiceRestImpl contentService = new ContentServiceRestImpl();
        contentService.loadTerminologyRrf(terminology, version, false, editMode,
            true, prefix == null ? "MR" : prefix, inputDir, authToken);

      } else {
        getLog().info("Running against server");

        // invoke the client
        ContentClientRest client = new ContentClientRest(properties);

        // load terminology
        client.loadTerminologyRrf(terminology, version, false, editMode, true,
            prefix == null ? "MR" : prefix, inputDir, authToken);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
