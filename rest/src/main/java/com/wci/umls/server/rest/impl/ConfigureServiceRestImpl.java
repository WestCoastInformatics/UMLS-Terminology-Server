/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.wci.umls.server.SourceData;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.LocalException;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.jpa.services.SourceDataServiceJpa;
import com.wci.umls.server.jpa.services.rest.ConfigureServiceRest;
import com.wci.umls.server.jpa.services.rest.HistoryServiceRest;
import com.wci.umls.server.services.MetadataService;
import com.wci.umls.server.services.SourceDataService;
import com.wci.umls.server.services.handlers.ExceptionHandler;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 * REST implementation for {@link HistoryServiceRest}.
 */
@Path("/configure")
@Api(value = "/configure", description = "Operations to configure application")
@Consumes({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML
})
public class ConfigureServiceRestImpl implements ConfigureServiceRest {

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

  private void validateProperty(String name, Properties props)
    throws Exception {
    if (props == null) {
      throw new Exception("Properties are null");
    }
    if (props.getProperty(name) == null || props.getProperty(name).isEmpty()) {
      throw new Exception("Property is empty: " + name);
    }

    if (props.getProperty(name).contains("${")) {
      throw new Exception("Configurable value " + name + " not set: "
          + props.getProperty(name));
    }
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
  @Override
  @Path("/configured")
  @ApiOperation(value = "Checks if application is configured", notes = "Returns true if application is configured, false if not", response = Boolean.class)
  public boolean isConfigured() throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (History): /configure/configured");

    try {

      String configFileName = ConfigUtility.getLocalConfigFile();

      boolean configured = ConfigUtility.getConfigProperties() != null
          || (new File(configFileName).exists());

      return configured;

    } catch (Exception e) {
      handleException(e, "checking if application is configured");
      return false;
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
  @Override
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

      // get the starting configuration
      InputStream in = ConfigureServiceRestImpl.class
          .getResourceAsStream("/config.properties.start");

      if (in == null) {
        throw new Exception("Could not open starting configuration file");
      }

      // construct name and check that the file does not already exist
      String configFileName = ConfigUtility.getLocalConfigFile();

      if (new File(configFileName).exists()) {
        throw new LocalException(
            "System is already configured from file: " + configFileName);
      }

      // get the starting properties
      Properties properties = new Properties();
      properties.load(in);

      // directly replace parameters by key
      for (String key : parameters.keySet()) {
        if (properties.containsKey(key)) {
          properties.setProperty(key, parameters.get(key));
        }
      }

      // replace config file property values based on replacement pattern ${...}
      for (Object key : new HashSet<>(properties.keySet())) {
        for (String param : parameters.keySet()) {

          if (properties.getProperty(key.toString())
              .contains("${" + param + "}")) {
            properties.setProperty(key.toString(),
                properties.getProperty(key.toString())
                    .replace("${" + param + "}", parameters.get(param)));
          }
        }
      }

      // validate the user-set properties
      validateProperty("source.data.dir", properties);
      validateProperty("hibernate.search.default.indexBase", properties);
      validateProperty("javax.persistence.jdbc.url", properties);
      validateProperty("javax.persistence.jdbc.user", properties);
      validateProperty("javax.persistence.jdbc.password", properties);

      // TODO Test database connection with supplied parameters
      // Current (commented) code throws SQL Exceptions regarding no driver found
      // e.g. No suitable driver found for jdbc:mysql://127.0.0.1:3306/sskdb?useUnicode=true&characterEncoding=UTF-8&rewriteBatchedStatements=true&useLocalSessionState=true
      // Check (1) existence, (2) credentials
    /*  try {
        java.sql.Connection con = DriverManager.getConnection(
            properties.getProperty("javax.persistence.jdbc.url"),
            properties.getProperty("javax.persistence.jdbc.user"),
            properties.getProperty("javax.persistence.jdbc.password"));
        con.getMetaData();
        
      } catch (SQLException e) {
        throw new LocalException("Could not establish connection to database. Please check database name and credentials.");
      }*/
      // create the local application folder
      File localFolder = new File(ConfigUtility.getLocalConfigFolder());
      if (!localFolder.exists()) {
        localFolder.mkdir();
      } else if (!localFolder.isDirectory()) {
        throw new LocalException("Could not create local directory "
            + ConfigUtility.getLocalConfigFolder());
      }

      // prerequisite: application directory exists
      File f = new File(parameters.get("app.dir").toString());
      if (!f.exists()) {
        throw new LocalException("Application directory does not exist: "
            + parameters.get("source.data.dir"));
      }

      Logger.getLogger(getClass())
          .info("Writing configuration file: " + configFileName);

      File configFile = new File(configFileName);

      Writer writer = new FileWriter(configFile);
      properties.store(writer, "User-configured settings");
      writer.close();

      // finally, reset the config properties and test retrieval
      System.setProperty("run.config." + ConfigUtility.getConfigLabel(),
          configFileName);
      if (ConfigUtility.getConfigProperties() == null) {
        throw new LocalException("Failed to retrieve newly written properties");
      }

      //
      // Create the database
      //
      MetadataService metadataService = null;
      ConfigUtility.getConfigProperties().setProperty("hibernate.hbm2ddl.auto",
          "create");
      try {
        metadataService = new MetadataServiceJpa();
      } catch (Exception e) {
        throw e;
      } finally {
        if (metadataService != null) {
          metadataService.close();
        }
        ConfigUtility.getConfigProperties()
            .setProperty("hibernate.hbm2ddl.auto", "update");

      }

    } catch (Exception e) {
      handleException(e, "checking if application is configured");
    } finally {
    }
  }

  /* see superclass */
  @DELETE
  @Override
  @Path("/destroy")
  @ApiOperation(value = "Destroys and rebuilds the database", notes = "Resets database to clean state and deletes any uploaded files", response = Boolean.class)
  public void destroy() throws Exception {
    Logger.getLogger(getClass())
        .info("RESTful call (History): /configure/destroy");

    // NOTE: Configure calls do not require authorization

    SourceDataService sourceDataService = null;
    try {

      sourceDataService = new SourceDataServiceJpa();

      // Check precondition: last source data object must have failed process
      List<SourceData> sourceDatas =
          sourceDataService.getSourceDatas().getObjects();

      if (sourceDatas.size() == 0) {
        throw new Exception(
            "Cannot destroy database: fail condition not detected");
      }

      // sort source datas by descending last modified
      Collections.sort(sourceDatas, new Comparator<SourceData>() {
        @Override
        public int compare(SourceData sd1, SourceData sd2) {
          return sd2.getLastModified().compareTo(sd1.getLastModified());
        }
      });

      switch (sourceDatas.get(0).getStatus()) {
        case CANCELLED:
        case LOADING_FAILED:
        case REMOVAL_FAILED:
          // do nothing
          break;
        default:
          throw new LocalException(
              "Cannot destroy database: fail condition not detected");

      }

      //
      // Create the database
      //
      MetadataService metadataService = null;
      ConfigUtility.getConfigProperties().setProperty("hibernate.hbm2ddl.auto",
          "create");
      try {
        metadataService = new MetadataServiceJpa();
        
        // close and re-open factory to trigger creation
        metadataService.closeFactory();
        metadataService.openFactory();
      } catch (Exception e) {
        throw e;
      } finally {
        if (metadataService != null) {
          metadataService.close();
        }
        
        // return mode to update
        ConfigUtility.getConfigProperties()
            .setProperty("hibernate.hbm2ddl.auto", "update");

      }

    } catch (Exception e) {
      handleException(e, "resetting the database");
    } finally {
      if (sourceDataService != null) {
        sourceDataService.close();
      }
    }
  }
}
