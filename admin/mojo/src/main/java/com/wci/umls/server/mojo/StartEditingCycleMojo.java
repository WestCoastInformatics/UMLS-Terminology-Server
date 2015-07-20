/*
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.rest.client.HistoryClientRest;
import com.wci.umls.server.rest.impl.HistoryServiceRestImpl;
import com.wci.umls.server.services.SecurityService;

/**
 * Admin tool for indicating the beginning of a release process.
 * 
 * See admin/release/pom.xml for sample usage.
 * 
 * @goal start-editing-cycle
 * @phase package
 */
public class StartEditingCycleMojo extends AbstractMojo {

  /**
   * The release version
   * 
   * @parameter releaseVersion
   */
  private String releaseVersion = null;

  /**
   * The terminology
   * 
   * @parameter terminology
   */
  private String terminology = null;

  /**
   * The terminology version
   * 
   * @parameter version
   */
  private String version = null;

  /**
   * Whether to run this mojo against an active server
   * @parameter
   */
  private boolean server = false;

  /* see superclass */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      // log start
      getLog().info("Starting editing cycle for: ");
      getLog().info("  releaseVersion = " + releaseVersion);
      getLog().info("  terminology = " + terminology);
      getLog().info("  version = " + version);

      // Check preconditions
      if (releaseVersion == null) {
        throw new Exception("A release version must be specified.");
      }

      if (terminology == null) {
        throw new Exception("A terminology must be specified.");
      }

      if (version == null) {
        throw new Exception("A terminology version must be specified.");
      }

      Properties properties = ConfigUtility.getConfigProperties();

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
              properties.getProperty("admin.password"));
      service.close();

      if (!serverRunning) {
        getLog().info("Running directly");

        HistoryServiceRestImpl historyService = new HistoryServiceRestImpl();
        historyService.startEditingCycle(releaseVersion, terminology, version,
            authToken);

      } else {
        getLog().info("Running against server");

        // invoke the client
        HistoryClientRest client = new HistoryClientRest(properties);
        client.startEditingCycle(releaseVersion, terminology, version,
            authToken);
      }

      getLog().info("...done");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoExecutionException("Performing release begin failed.", e);
    }

  }
}
