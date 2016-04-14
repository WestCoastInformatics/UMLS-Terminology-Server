/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import org.apache.maven.plugin.MojoFailureException;

import com.wci.umls.server.helpers.ConfigUtility;

/**
 * Goal which removes a terminology and corresponding source data from a
 * database.
 * 
 * See admin/pom.xml for sample usage
 * 
 * @goal remove-sd-terminology
 * 
 * @phase package
 */
public class SourceDataRemoverMojo extends SourceDataMojo {

  /**
   * Name of terminology to be removed.
   * @parameter
   * @required
   */
  private String terminology;

  /**
   * Version to remove.
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
   * Instantiates a {@link SourceDataRemoverMojo} from the specified parameters.
   * 
   */
  public SourceDataRemoverMojo() {
    // do nothing
  }

  /* see superclass */
  @Override
  public void execute() throws MojoFailureException {
    getLog().info("Starting removing terminology and source data");
    getLog().info("  terminology = " + terminology);
    getLog().info("  version = " + version);
    try {

      // Properties properties = ConfigUtility.getConfigProperties();
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
      // SecurityService service = new SecurityServiceJpa();
      // String authToken =
      // service.authenticate(properties.getProperty("admin.user"),
      // properties.getProperty("admin.password")).getAuthToken();

      if (!serverRunning) {
        getLog().info("Running directly");
        
        // TODO Get this working again

        /*final RemoveSourceDataAlgorithm algo = new RemoveSourceDataAlgorithm();
        final SourceDataService sdService = new SourceDataServiceJpa();
        try {

          SourceData sourceData = null;
          List<SourceData> data = sdService
              .findSourceDatasForQuery(
                  "nameSort:\"" + getName(terminology, version) + "\"", null)
              .getObjects();
          if (data.size() == 1) {
            sourceData = data.get(0);
          } else if (data.size() == 0) {
            // no source data, proceed
          } else {
            throw new Exception(
                "Unexpected number of results searching for source data: "
                    + data.size());
          }
          algo.setTerminology(terminology);
          algo.setVersion(version);
          algo.setSourceData(sourceData);
          algo.compute();
        } catch (Exception e) {
          throw e;
        } finally {
          sdService.close();
          algo.close();
        }*/

      } else {
        getLog().info("Running against server");

        throw new Exception(
            "Running against the server is not supported at this time.");
      }

      // service.close();

      getLog().info("done ...");
      System.exit(0);
    } catch (

    Exception e)

    {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

}
