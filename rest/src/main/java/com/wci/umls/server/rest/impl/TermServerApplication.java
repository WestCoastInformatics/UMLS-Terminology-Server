/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import com.ibm.icu.util.Calendar;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.MetadataServiceJpa;
import com.wci.umls.server.services.MetadataService;
import com.wordnik.swagger.jaxrs.config.BeanConfig;

/**
 * The application (for jersey). Also serves the role of the initialization
 * listener.
 */
@ApplicationPath("/")
public class TermServerApplication extends Application {

  /** The API_VERSION - also used in "swagger.htmL" */
  public final static String API_VERSION = "1.0.0";

  /** The timer. */
  Timer timer;

  /**
   * Instantiates an empty {@link TermServerApplication}.
   *
   * @throws Exception the exception
   */
  public TermServerApplication() throws Exception {
    Logger.getLogger(getClass()).info("TERM SERVER APPLICATION START");
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setTitle("Term Server API");
    beanConfig.setDescription("RESTful calls for terminology server");
    beanConfig.setVersion(API_VERSION);
    
    if (new ConfigureServiceRestImpl().isConfigured()) {
      beanConfig.setBasePath(
          ConfigUtility.getConfigProperties().getProperty("base.url"));
      beanConfig.setResourcePackage("com.wci.umls.server.rest.impl");
      beanConfig.setScan(true);
    }

    // Set up a timer task to run at 2AM every day
    TimerTask task = new InitializationTask();
    timer = new Timer();
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 2);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    timer.scheduleAtFixedRate(task, today.getTime(), 6 * 60 * 60 * 1000);

    // Cache the "guest" user.
    // SecurityService service;
    // try {
    // service = new SecurityServiceJpa();
    // Properties config = ConfigUtility.getConfigProperties();
    // if (config.getProperty("security.handler").equals("DEFAULT")) {
    // service.authenticate("guest", "guest");
    // }
    // } catch (Exception e) {
    // try {
    // ExceptionHandler.handleException(e, "Cacheing guest user info");
    // } catch (Exception e1) {
    // // do nothing
    // e1.printStackTrace();
    // }
    // }
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

  /* see superclass */
  @Override
  public Set<Class<?>> getClasses() {
    final Set<Class<?>> classes = new HashSet<Class<?>>();

    // register REST implementations
    classes.add(SecurityServiceRestImpl.class);
    classes.add(ContentServiceRestImpl.class);
    classes.add(HistoryServiceRestImpl.class);
    classes.add(MetadataServiceRestImpl.class);
    classes.add(ProjectServiceRestImpl.class);
    classes.add(ValidationServiceRestImpl.class);
    classes.add(SourceDataServiceRestImpl.class);
    classes.add(ConfigureServiceRestImpl.class);
    classes.add(MetaEditingServiceRestImpl.class);

    // register file upload support classes
    classes.add(MultiPartFeature.class);

    // register swagger classes
    classes
        .add(com.wordnik.swagger.jersey.listing.ApiListingResourceJSON.class);
    classes.add(
        com.wordnik.swagger.jersey.listing.JerseyApiDeclarationProvider.class);
    classes.add(
        com.wordnik.swagger.jersey.listing.JerseyResourceListingProvider.class);
    return classes;
  }

  /* see superclass */
  @Override
  public Set<Object> getSingletons() {
    final Set<Object> instances = new HashSet<Object>();
    instances.add(new JacksonFeature());
    instances.add(new JsonProcessingFeature());

    // Enable for LOTS of logging of HTTP requests
    // instances.add(new LoggingFilter());
    return instances;
  }

}
