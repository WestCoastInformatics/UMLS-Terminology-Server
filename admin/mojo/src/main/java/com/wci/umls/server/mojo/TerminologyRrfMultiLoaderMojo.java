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
import com.wci.umls.server.jpa.algo.RrfLoaderAlgorithm;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Goal which loads a set of RRF into a database.
 * 
 * See admin/loader/pom.xml for sample usage
 */
@Mojo(name = "load-rrf-multi", defaultPhase = LifecyclePhase.PACKAGE)
public class TerminologyRrfMultiLoaderMojo extends AbstractLoaderMojo {

  /**
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
   * Instantiates a {@link TerminologyRrfMultiLoaderMojo} from the specified
   * parameters.
   * 
   */
  public TerminologyRrfMultiLoaderMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {

    try {
      getLog().info("RRF Multi Terminology Loader called via mojo.");
      getLog().info("  Input directory    : " + inputDir);
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
      service.close();

      if (!serverRunning) {
        getLog().info("Running directly");

        ContentServiceRestImpl contentService = new ContentServiceRestImpl();
        contentService.loadTerminologyRrf("", "",
            RrfLoaderAlgorithm.Style.MULTI.toString(),
            prefix == null ? "MR" : prefix, inputDir, authToken);

      } else {
        getLog().info("Running against server");

        // invoke the client
        ContentClientRest client = new ContentClientRest(properties);

        // load terminology
        client.loadTerminologyRrf(null, null,
            RrfLoaderAlgorithm.Style.MULTI.toString(),
            prefix == null ? "MR" : prefix, inputDir, authToken);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }
}
