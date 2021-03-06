/**
 * Copyright 2016 West Coast Informatics, LLC
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
 * Goal which makes ECL indexes based on component objects
 * 
 * See admin/lucene/pom.xml for sample usage
 */
@Mojo(name = "reindex-ecl", defaultPhase = LifecyclePhase.PACKAGE)
public class LuceneReindexEclMojo extends AbstractMojo {

  /**
   * The terminology.
   */
  @Parameter
  private String terminology;
  
  /**
   * The version.
   */
  @Parameter
  private String version;

  /**
   * Whether to run this mojo against an active server.
   */
  @Parameter
  private boolean server = false;

  /**
   * Instantiates a {@link LuceneReindexEclMojo} from the specified parameters.
   */
  public LuceneReindexEclMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    try {
      getLog().info("ECL indexing called via mojo.");
      getLog().info("  Terminology : " + terminology);
      getLog().info("  Version     : " + version);
      getLog().info("  Expect server up: " + server);
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
              properties.getProperty("admin.password")).getAuthToken();
      service.close();

      if (!serverRunning) {
        getLog().info("Running directly");

        ContentServiceRestImpl contentService = new ContentServiceRestImpl();
        contentService.computeExpressionIndexes(terminology, version, authToken);

      } else {
        getLog().info("Running against server");

        // invoke the client
        ContentClientRest client = new ContentClientRest(properties);
        client.computeExpressionIndexes(terminology, version, authToken);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }

  }

}
