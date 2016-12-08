/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.mojo;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
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
 * Goal which loads an RF2 Delta of SNOMED CT data
 * 
 * See admin/loader/pom.xml for sample usage
 */
@Mojo(name = "load-rf2-delta", defaultPhase = LifecyclePhase.PACKAGE)
public class TerminologyRf2DeltaLoader extends AbstractMojo {

  /**
   * Name of terminology to be loaded.
   */
  @Parameter
  private String terminology;

  /**
   * The input directory
   */
  @Parameter
  private String inputDir;

  /** Whether to run this mojo against an active server. */
  @Parameter
  private boolean server = false;

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("RF2 Snapshot Terminology Loader called via mojo.");
      getLog().info("  Terminology        : " + terminology);
      getLog().info("  Input directory    : " + inputDir);
      getLog().info("  Expect server up   : " + server);

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

      // authenticate
      SecurityService service = new SecurityServiceJpa();
      String authToken =
          service.authenticate(properties.getProperty("admin.user"),
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      if (!serverRunning) {
        getLog().info("Running directly");

        ContentServiceRestImpl contentService = new ContentServiceRestImpl();
        contentService.loadTerminologyRf2Delta(terminology, inputDir,
            authToken);
      } else {
        getLog().info("Running against server");

        // invoke the client
        ContentClientRest client = new ContentClientRest(properties);
        client.loadTerminologyRf2Delta(terminology, inputDir, authToken);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
