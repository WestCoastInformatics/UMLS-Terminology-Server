/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.rest.client;

import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.wci.umls.server.Project;
import com.wci.umls.server.helpers.ConceptList;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.ProjectList;
import com.wci.umls.server.jpa.ProjectJpa;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.ProjectListJpa;
import com.wci.umls.server.jpa.services.rest.ProjectServiceRest;

/**
 * A client for connecting to a project REST service.
 */
public class ProjectClientRest implements ProjectServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ProjectClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ProjectClientRest(Properties config) {
    this.config = config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ProjectServiceRest#addProject(org.ihtsdo.otf.ts.
   * jpa.ProjectJpa, java.lang.String)
   */
  @Override
  public Project addProject(ProjectJpa project, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/project/add");

    String projectString =
        ConfigUtility.getStringForGraph(project == null ? new ProjectJpa()
            : project);
    Logger.getLogger(getClass()).debug(projectString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .put(ClientResponse.class, projectString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectJpa result =
        (ProjectJpa) ConfigUtility.getGraphForString(resultString,
            ProjectJpa.class);

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ProjectServiceRest#updateProject(org.ihtsdo.otf.
   * ts.jpa.ProjectJpa, java.lang.String)
   */
  @Override
  public void updateProject(ProjectJpa project, String authToken)
    throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/project/update");

    String projectString =
        ConfigUtility.getStringForGraph(project == null ? new ProjectJpa()
            : project);
    Logger.getLogger(getClass()).debug(projectString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, projectString);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ProjectServiceRest#removeProject(java.lang.String,
   * java.lang.String)
   */
  @Override
  public void removeProject(Long id, String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/project/remove/id/"
            + id);
    
    if (id == null)
    	return;

    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .delete(ClientResponse.class);

    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // do nothing, successful
    } else {
      throw new Exception("Unexpected status - " + response.getStatus());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ProjectServiceRest#getConceptsInScope(java.lang.
   * Long, java.lang.String)
   */
  @Override
  public ConceptList findConceptsInScope(Long projectId, PfsParameterJpa pfs,
    String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/project/scope/id/"
            + projectId);
    String pfsString =
        ConfigUtility.getStringForGraph(pfs == null ? new PfsParameterJpa()
            : pfs);
    Logger.getLogger(getClass()).debug(pfsString);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken)
            .header("Content-type", MediaType.APPLICATION_XML)
            .post(ClientResponse.class, pfsString);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    // TODO
//    ConceptListJpa list =
//        (ConceptListJpa) ConfigUtility.getGraphForString(resultString,
//            ConceptListJpa.class);
//    return list;
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.ihtsdo.otf.ts.rest.ProjectServiceRest#getProject(java.lang.Long,
   * java.lang.String)
   */
  @Override
  public Project getProject(Long id, String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/project/id/" + id);
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ProjectJpa project =
        (ProjectJpa) ConfigUtility.getGraphForString(resultString,
            ProjectJpa.class);
    return project;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.ihtsdo.otf.ts.rest.ProjectServiceRest#getProjects(java.lang.String)
   */
  @Override
  public ProjectList getProjects(String authToken) throws Exception {
    Client client = Client.create();
    WebResource resource =
        client.resource(config.getProperty("base.url") + "/project/projects");
    ClientResponse response =
        resource.accept(MediaType.APPLICATION_XML)
            .header("Authorization", authToken).get(ClientResponse.class);

    String resultString = response.getEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      Logger.getLogger(getClass()).debug(resultString);
    } else {
      throw new Exception(resultString);
    }

    // converting to object
    ProjectListJpa list =
        (ProjectListJpa) ConfigUtility.getGraphForString(resultString,
            ProjectListJpa.class);
    return list;
  }

}
