/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.mojo;

import java.util.Properties;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.services.ProjectServiceJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.rest.impl.ContentServiceRestImpl;
import com.wci.umls.server.services.ProjectService;
import com.wci.umls.server.services.SecurityService;

/**
 * Holds the createDb code for all of the sub-mojos
 */
public abstract class AbstractLoaderMojo extends SourceDataMojo {

  /**
   * Creates the db.
   *
   * @param serverRunning the server running
   * @throws Exception the exception
   */
  public void createDb(boolean serverRunning) throws Exception {
    
    final Properties properties = ConfigUtility.getConfigProperties();
    
        getLog().info("Recreate database");
        // This will trigger a rebuild of the db
        properties.setProperty("hibernate.hbm2ddl.auto", "create");
        String autoRegisterProperty =
            properties.getProperty("hibernate.listeners.envers.autoRegister");
        properties.setProperty("hibernate.listeners.envers.autoRegister",
            "true");
        // Trigger a JPA event
        ProjectService projectService = new ProjectServiceJpa();
        projectService.close();
        projectService.closeFactory();
        
        properties.remove("hibernate.hbm2ddl.auto");
        if (autoRegisterProperty == null) {
          properties.remove("hibernate.listeners.envers.autoRegister");
        } else {
          properties.setProperty("hibernate.listeners.envers.autoRegister",
              autoRegisterProperty);
        }
        
        projectService.openFactory();
        
        // authenticate
        SecurityService service = new SecurityServiceJpa();
        String authToken =
            service.authenticate(properties.getProperty("admin.user"),
                properties.getProperty("admin.password")).getAuthToken();
        service.close();           
        
        if(serverRunning){
          ContentClientRest client = new ContentClientRest(properties);
          client.luceneReindex(null, authToken);
        }
        else{
          ContentServiceRestImpl contentService = new ContentServiceRestImpl();
          contentService.luceneReindex(null, authToken);
        }
      }
 
}