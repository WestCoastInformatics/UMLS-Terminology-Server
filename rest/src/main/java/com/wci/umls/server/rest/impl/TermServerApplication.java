/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.ApplicationPath;

import org.apache.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.ibm.icu.util.Calendar;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.services.MetadataService;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.util.Json;

/**
 * The application (for jersey). Also serves the role of the initialization
 * listener.
 */
@ApplicationPath("/")
public class TermServerApplication extends ResourceConfig {

  /** The API_VERSION - also used in "swagger.htmL" */
  public final static String API_VERSION = "1.0.0";

  /** The timer. */
  public static Timer timer;

  /**
   * Instantiates an empty {@link TermServerApplication}.
   *
   * @throws Exception the exception
   */
  public TermServerApplication() throws Exception {
    // Register providers and features
    super(ObjectMapperProvider.class, JacksonFeature.class,
        MultiPartFeature.class);
    Logger.getLogger(getClass()).info("TERM SERVER APPLICATION START");

    // register REST implementations
    register(SecurityServiceRestImpl.class);
    register(ContentServiceRestImpl.class);
    register(SimpleEditServiceRestImpl.class);
    register(HistoryServiceRestImpl.class);
    register(MetadataServiceRestImpl.class);
    register(ProjectServiceRestImpl.class);

    register(ProcessServiceRestImpl.class);
    register(SourceDataServiceRestImpl.class);
    register(ConfigureServiceRestImpl.class);
    register(MetaEditingServiceRestImpl.class);
    register(WorkflowServiceRestImpl.class);
    register(ReportServiceRestImpl.class);
    register(InversionServiceRestImpl.class);

    // Enable this for logging
    // register(LoggingFeature.class);

    // Make integration test rest services available in dev environment
    try {
      if (!ConfigUtility.getConfigProperties().containsKey("deploy.mode")
          || !ConfigUtility.getConfigProperties().getProperty("deploy.mode")
              .contains("PROD")) {
        register(IntegrationTestServiceRestImpl.class);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    // register swagger classes
    register(io.swagger.jaxrs.listing.ApiListingResource.class);
    register(io.swagger.jaxrs.listing.SwaggerSerializers.class);

    // Instantiate bean config
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setTitle("Term Server API");
    beanConfig.setDescription("RESTful calls for terminology server");
    beanConfig.setVersion(API_VERSION);
    final URL url =
        new URL(ConfigUtility.getConfigProperties().getProperty("base.url"));
    final String host = url.getHost() + ":" + url.getPort();

    if (new ConfigureServiceRestImpl().isConfigured()) {
      beanConfig.setHost(host);
      beanConfig.setBasePath(url.getPath());
      beanConfig.setSchemes(new String[] {
          url.getProtocol()
      });
      beanConfig.setResourcePackage("com.wci.umls.server.rest.impl");
      beanConfig.setScan(true);
      beanConfig.setPrettyPrint(true);
    }

    // this makes Swagger honor JAXB annotations
    Json.mapper().registerModule(new JaxbAnnotationModule());

    // Set up a timer task to run at 2AM every day
    TimerTask task = new InitializationTask();
    timer = new Timer();
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 2);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    timer.scheduleAtFixedRate(task, today.getTime(), 6 * 60 * 60 * 1000);

  }

  /**
   * Initialization task.
   */
  class InitializationTask extends TimerTask {

    /* see superclass */
    @Override
    public void run() {
      try {

        // We need to "ping" the server to keep DB connections alive.
        // Do 4 times per day. Just get users list.
        Logger.getLogger(getClass()).info("  PING");
        if (new ConfigureServiceRestImpl().isConfigured()) {
          MetadataService service = new MetadataServiceJpa();
          service.getRootTerminologies();
          service.close();
        }

      } catch (Exception e) {
        timer.cancel();
        e.printStackTrace();
        Logger.getLogger(getClass()).error("Error running the process to xxx.");
      }
    }
  }

}
