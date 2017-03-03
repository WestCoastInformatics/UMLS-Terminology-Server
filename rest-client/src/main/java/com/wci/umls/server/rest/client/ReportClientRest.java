package com.wci.umls.server.rest.client;

import java.net.URLEncoder;
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.log4j.Logger;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.helpers.WorkflowBinDefinitionList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.report.ReportJpa;
import com.wci.umls.server.jpa.report.ReportListJpa;
import com.wci.umls.server.jpa.services.rest.ReportServiceRest;
import com.wci.umls.server.model.meta.IdType;
import com.wci.umls.server.model.report.Report;
import com.wci.umls.server.model.report.ReportList;

/**
 * A client for connecting to a history REST service.
 */
public class ReportClientRest extends RootClientRest
    implements ReportServiceRest {

  /** The config. */
  private Properties config = null;

  /**
   * Instantiates a {@link ReportClientRest} from the specified parameters.
   *
   * @param config the config
   */
  public ReportClientRest(Properties config) {
    this.config = config;
  }

  /* see superclass */
  @Override
  public String getConceptReport(Long projectId, Long conceptId,
    String authToken) throws Exception {

    validateNotEmpty(conceptId, "conceptId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/report/concept/" + conceptId + "?projectId=" + projectId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    return resultString;
  }

  /* see superclass */
  @Override
  public String getDescriptorReport(Long projectId, Long descriptorId,
    String authToken) throws Exception {

    validateNotEmpty(descriptorId, "descriptorId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/report/descriptor/" + descriptorId + "?projectId=" + projectId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    return resultString;
  }

  /* see superclass */
  @Override
  public String getCodeReport(Long projectId, Long codeId, String authToken)
    throws Exception {

    validateNotEmpty(codeId, "codeId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/report/code/" + codeId + "?projectId=" + projectId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    return resultString;
  }

  @Override
  public WorkflowBinDefinitionList findReportDefinitions(Long projectId,
    String authToken) throws Exception {

    validateNotEmpty(projectId, "projectId");
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/report/definitions?projectId=" + projectId);
    Response response = target.request(MediaType.TEXT_PLAIN)
        .header("Authorization", authToken).get();
    if (response.getStatus() == 204) {
      return null;
    }
    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    }
    // handle null response
    else if (response.getStatus() == 204) {
      return null;
    } else {
      throw new Exception(response.toString());
    }

    WorkflowBinDefinitionList list = ConfigUtility
        .getGraphForString(resultString, WorkflowBinDefinitionList.class);
    return list;

  }

  @Override
  public ReportList findReports(Long projectId, String query,
    PfsParameterJpa pfs, String authToken) throws Exception {
    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/report/find"
            + "?query=" + URLEncoder.encode(query == null ? "" : query, "UTF-8")
                .replaceAll("\\+", "%20"));
    String pfsString = ConfigUtility
        .getStringForGraph(pfs == null ? new PfsParameterJpa() : pfs);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).post(Entity.xml(pfsString));

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReportList list =
        ConfigUtility.getGraphForString(resultString, ReportListJpa.class);
    return list;
  }

  @Override
  public Report getReport(Long id, String authToken) throws Exception {
    Logger.getLogger(getClass()).debug("Report Client - get report " + id);
    validateNotEmpty(id, "id");

    Client client = ClientBuilder.newClient();
    WebTarget target =
        client.target(config.getProperty("base.url") + "/report/" + id);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReportJpa report =
        ConfigUtility.getGraphForString(resultString, ReportJpa.class);
    return report;
  }

  @Override
  public Report generateReport(Long projectId, String name, String query,
    QueryType queryType, IdType resultType, String authToken) throws Exception {
    Logger.getLogger(getClass())
        .debug("Report Client - generate report " + projectId);
    validateNotEmpty(projectId, "projectId");

    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(config.getProperty("base.url")
        + "/report/generate/" + projectId + "?name=" + name + "&query=" + query
        + "&queryType=" + queryType + "&resultType=" + resultType);
    Response response = target.request(MediaType.APPLICATION_XML)
        .header("Authorization", authToken).get();

    String resultString = response.readEntity(String.class);
    if (response.getStatusInfo().getFamily() == Family.SUCCESSFUL) {
      // n/a
    } else {
      throw new Exception(response.toString());
    }

    // converting to object
    ReportJpa report =
        ConfigUtility.getGraphForString(resultString, ReportJpa.class);
    return report;
  }
}