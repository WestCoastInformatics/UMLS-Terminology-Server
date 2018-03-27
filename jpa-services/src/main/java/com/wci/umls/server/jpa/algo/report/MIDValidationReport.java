/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.report;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractReportAlgorithm;
import com.wci.umls.server.model.workflow.WorkflowBinDefinition;
import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * Implementation of an algorithm to perform a recomputation of Metathesaurus
 * concept status based on component status and validation.
 */
public class MIDValidationReport extends AbstractReportAlgorithm {

  /** The concept ids. */
  public Set<Long> conceptIds = null;

  /**
   * Instantiates an empty {@link MIDValidationReport}.
   * @throws Exception if anything goes wrong
   */
  public MIDValidationReport() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("MIDVALIDATION");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("MID validation requires a project to be set");
    }
    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting database QA");

    final Map<String, List<String>> errors = new HashMap<>();
    final Map<String, String> queries = new TreeMap<>();

    // collect queries from "MID_VALIDATION" workflow config
    final WorkflowConfig workflowConfig =
        getWorkflowConfig(getProject(), "MID_VALIDATION");
    if (workflowConfig != null) {
      for (final WorkflowBinDefinition definition : workflowConfig
          .getWorkflowBinDefinitions()) {
        if (definition.getQueryType() != QueryType.SQL) {
          throw new Exception(
              "MID Validation report can only handle queries with query type SQL");
        }
        if (queries.containsKey(definition.getName())) {
          throw new Exception(
              "Check with duplicate name: " + definition.getName());
        }
        queries.put(definition.getName(), definition.getQuery());
      }
    }
    final WorkflowConfig workflowConfig2 =
        getWorkflowConfig(getProject(), "MID_VALIDATION_OTHER");
    if (workflowConfig2 != null) {
      for (final WorkflowBinDefinition definition : workflowConfig2
          .getWorkflowBinDefinitions()) {
        if (definition.getQueryType() != QueryType.SQL) {
          throw new Exception(
              "MID Validation report can only handle queries with query type SQL");
        }
        if (queries.containsKey(definition.getName())) {
          throw new Exception(
              "Check with duplicate name: " + definition.getName());
        }
        queries.put(definition.getName(), definition.getQuery());
      }
    }

    // Iterate through queries, execute and report
    int ct = 0;
    int totalCt = queries.keySet().size();
    for (final String name : queries.keySet()) {
      String queryStr = queries.get(name);
      logInfo("  " + name + " = " + queryStr);
      checkCancel();
      fireProgressEvent((int) (ct * 100.0 / totalCt), "Running " + name);
      try {
        // Get and execute query (truncate any trailing semi-colon)
        final Query query = manager.createNativeQuery(queryStr);
        //query.setParameter("terminology", getProject().getTerminology());
        if (queryStr.contains(":terminology")) {
          query.setParameter("terminology", getProject().getTerminology());
        }
        if (queryStr.contains(":version")) {
          query.setParameter("version", getProject().getVersion());
        }
        if (queryStr.contains(":projectId")) {
          query.setParameter("projectId", getProject().getId());
        }
        query.setMaxResults(10);
        final List<Object[]> objects = query.getResultList();

        // Expect zero count, any results are failures
        if (objects.size() > 0) {
          final List<String> results = new ArrayList<>();
          for (final Object[] array : objects) {
            StringBuilder sb = new StringBuilder();
            for (final Object o : array) {
              sb.append((o != null ? o.toString() : "null")).append(",");
            }
            results.add(sb.toString().replace(",$", ""));
          }
          errors.put(name, results);
        }
      } catch (Exception e) {
        errors.put(name, new ArrayList<>());
        errors.get(name).add("Unexpected error executing query: " + e);
        // If the query failed, just go to the next one
      }

      commitClearBegin();
    }

    // Check for errors and report the
    if (!errors.isEmpty()) {
      final StringBuilder msg = new StringBuilder();
      msg.append("\r\n");
      msg.append(
          "MID Validation has found some issues with the following checks:\r\n");
      msg.append("\r\n");

      for (final String key : errors.keySet()) {
        msg.append("  CHECK: ").append(key).append("\r\n");
        msg.append("  QUERY: ").append(queries.get(key)).append("\r\n");
        for (final String result : errors.get(key)) {
          msg.append("    " + result).append("\r\n");
        }
        if (errors.get(key).size() > 9) {
          msg.append("    ... ");
          // the true count is not known because setMaxResults(10) is used.
        }

      }
      logInfo("  SEND EMAIL");
      
      // Send email if configured.
      if (!ConfigUtility.isEmpty(getEmail())) {
        String from = null;
        if (config.containsKey("mail.smtp.from")) {
          from = config.getProperty("mail.smtp.from");
        } else {
          from = config.getProperty("mail.smtp.user");
        }
        try {
          ConfigUtility.sendEmail(
              "MEME Mid Validation Report - "
                  + ConfigUtility.DATE_YYYYMMDD.format(new Date()),
              from, getEmail(), msg.toString(), config);
        } catch (Exception e) {
          e.printStackTrace();
          // do nothing - this just means email couldn't be sent
        }
      }

    } else {
      logInfo("  NO errors");
      
      // Send email if configured.
      if (!ConfigUtility.isEmpty(getEmail())) {
        String from = null;
        if (config.containsKey("mail.smtp.from")) {
          from = config.getProperty("mail.smtp.from");
        } else {
          from = config.getProperty("mail.smtp.user");
        }
        try {
          ConfigUtility.sendEmail(
              "MEME Mid Validation Report - "
                  + ConfigUtility.DATE_YYYYMMDD.format(new Date()),
              from, getEmail(), "No errors found!", config);
        } catch (Exception e) {
          e.printStackTrace();
          // do nothing - this just means email couldn't be sent
        }
      }      
    }

    logInfo("Done ...");

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    return super.getParameters();
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "MID Validation Report";
  }

  /**
   * Sets the concept ids.
   *
   * @param conceptIds the concept ids
   */
  public void setConceptIds(Set<Long> conceptIds) {
    this.conceptIds = conceptIds;
  }
}
