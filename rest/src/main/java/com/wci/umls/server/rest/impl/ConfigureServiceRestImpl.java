/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.jpa.services.rest.HistoryServiceRest;
import com.wci.umls.server.services.SecurityService;
import com.wci.umls.server.services.handlers.ExceptionHandler;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link HistoryServiceRest}.
 */
@Path("/configure")
@Api(value = "/configure", description = "Operations to retrieve historical RF2 content for a terminology")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ConfigureServiceRestImpl {

  /**
   * Instantiates an empty {@link ConfigureServiceRestImpl}.
   *
   * @throws Exception the exception
   */
  public ConfigureServiceRestImpl() throws Exception {
  }

  /**
   * Handle exception. TODO This is duplicate content from RootServiceRestImpl
   *
   * @param e the e
   * @param whatIsHappening the what is happening, consider
   */
  @SuppressWarnings("static-method")
  public void handleException(Exception e, String whatIsHappening) {
    try {
      ExceptionHandler.handleException(e, whatIsHappening, "");
    } catch (Exception e1) {
      // do nothing
    }

    // Ensure message has quotes.
    // When migrating from jersey 1 to jersey 2, messages no longer
    // had quotes around them when returned to client and angular
    // could not parse them as json.
    String message = e.getMessage();
    if (message != null && !message.startsWith("\"")) {
      message = "\"" + message + "\"";
    }
    // throw the local exception as a web application exception
    if (e instanceof LocalException) {
      throw new WebApplicationException(
          Response.status(500).entity(message).build());
    }

    // throw the web application exception as-is, e.g. for 401 errors
    if (e instanceof WebApplicationException) {
      throw new WebApplicationException(message, e);
    }
    throw new WebApplicationException(
        Response
            .status(500).entity("\"Unexpected error trying to "
                + whatIsHappening + ". Please contact the administrator.\"")
        .build());

  }

  /**
   * Checks if application is configured
   *
   * @param authToken the auth token
   * @return the release history
   * @throws Exception the exception
   */
  /* see superclass */
  @GET
  @Path("/configured")
  @ApiOperation(value = "Checks if application is configured", notes = "Returns true if application is configured, false if not", response = Boolean.class)
  public Boolean isConfigured() throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (History): /configure/configured");

    try {

      String configFileName = System.getProperty("catalina.base")
          + "/wtpwebapps/term-server-rest/WEB-INF/config.properties";

      return (new File(configFileName).exists());

    } catch (Exception e) {
      handleException(e, "checking if application is configured");
      return null;
    } finally {

    }
  }

  /**
   * @param parameters
   * @param authToken
   * @throws Exception
   */
  /* see superclass */
  @POST
  @Path("/configure")
  @ApiOperation(value = "Checks if application is configured", notes = "Returns true if application is configured, false if not", response = Boolean.class)
  public void configure(
    @ApiParam(value = "Configuration parameters as JSON string", required = true) HashMap<String, String> parameters)
      throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (History): /configure/configure with parameters "
            + parameters.toString());

    // NOTE: Configure calls do not require authorization

    try {

      // prerequisite: database name
      if (parameters.get("javax.persistence.jdbc.url") == null) {
        throw new LocalException("Database name must be specified");
      }

      // prerequisite: database user
      if (parameters.get("javax.persistence.jdbc.user") == null) {
        throw new LocalException("Database user must be specified");
      }

      // prerequisite: database user password
      if (parameters.get("javax.persistence.jdbc.password") == null) {
        throw new LocalException("Database user password must be specified");
      }

      // prerequisite: application upload directory
      if (parameters.get("source.data.dir") == null) {
        throw new LocalException("Application directory must be specified");
      }

      // prerequisite: application directory exists
      File f = new File(parameters.get("source.data.dir"));
      if (!f.exists()) {
        throw new LocalException("Application directory does not exist: "
            + parameters.get("source.data.dir"));
      }
      if (!f.isDirectory()) {
        throw new LocalException(
            "Application directory specified is not a directory: "
                + parameters.get("source.data.dir"));
      }

      // get the starting properties
      Properties properties = ConfigUtility.getStartingConfigProperties();
      for (String key : parameters.keySet()) {
        properties.setProperty(key, parameters.get(key));
      }

      // jdbc:mysql://127.0.0.1:3306/umlsdb?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&useLocalSessionState=true

      String configFileName = System.getProperty("catalina.base")
          + "/wtpwebapps/term-server-rest/WEB-INF/config.properties";
      Logger.getLogger(getClass())
          .info("Writing configuration file: " + configFileName);

      File configFile = new File(configFileName);
      try {
        Writer writer = new FileWriter(configFile);
        properties.store(writer, "User-configured settings");
        writer.close();
      } catch (FileNotFoundException ex) {
        throw new LocalException("Could not open configuration file");
      } catch (IOException ex) {
        throw new LocalException("Error writing configuration file");
      } catch (Exception e) {
        handleException(e, "checking if application is configured");
      }

      // finally, reset the config properties and test retrieval
      ConfigUtility.resetConfigProperties();
      if (ConfigUtility.getConfigProperties() == null) {
        throw new LocalException("Failed to retrieve newly written properties");
      }
    } catch (Exception e) {
      handleException(e, "checking if application is configured");
    } finally {
    }
  }
}
