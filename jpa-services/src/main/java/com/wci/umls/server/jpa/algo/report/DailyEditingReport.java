/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.report;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractReportAlgorithm;
import com.wci.umls.server.model.actions.MolecularAction;

/**
 * Implementation of an algorithm to perform a recomputation of Metathesaurus
 * concept status based on component status and validation.
 */
public class DailyEditingReport extends AbstractReportAlgorithm {

  /** The concept ids. */
  public Set<Long> conceptIds = null;

  /**
   * Instantiates an empty {@link DailyEditingReport}.
   * @throws Exception if anything goes wrong
   */
  public DailyEditingReport() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("MATRIXINIT");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("Matrix initializer requires a project to be set");
    }
    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /**
   * Compute.
   *
   * @throws Exception the exception
   */
  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Generating daily editing report");
    logInfo("  email = " + getEmail());
    try {
      final StringBuilder report = new StringBuilder();

      final Long start = new Date().getTime();
      final Date yesterday =
          ConfigUtility.DATE_YYYYMMDD.parse(ConfigUtility.DATE_YYYYMMDD
              .format(new Date(start - 24 * 3600 * 1000)));
      final Date today = ConfigUtility.DATE_YYYYMMDD
          .parse(ConfigUtility.DATE_YYYYMMDD.format(new Date(start)));

      Query query =
          getEntityManager().createQuery("select m from MolecularActionJpa m "
              + "where lastModified >= :startDate and lastModified < :endDate");
      query.setParameter("startDate", yesterday);
      // TODO: query.setParameter("endDate", today);
      query.setParameter("endDate", new Date());
      @SuppressWarnings("unchecked")
      final List<MolecularAction> actions = query.getResultList();
      // int[] - total, approvals, rels, stys, splits, merges
      final Map<String, int[]> actionStats = new HashMap<>();
      final Map<String, Set<Long>> conceptStats = new HashMap<>();
      conceptStats.put("APPROVE", new HashSet<>());
      int actionCt = 0;
      for (final MolecularAction action : actions) {
        final String editor = action.getLastModifiedBy();
        // Skip non editor actions and undone actions
        if ((!editor.startsWith("E-") && !editor.startsWith("S-"))
            || action.isUndoneFlag()) {
          continue;
        }
        actionCt++;
        if (!actionStats.containsKey(editor)) {
          int[] stats = new int[] {
              0, 0, 0, 0, 0, 0, 0
          };
          actionStats.put(editor, stats);
          conceptStats.put(editor, new HashSet<>());
        }
        // concept stats
        conceptStats.get(editor).add(action.getComponentId());

        // Total stats[0]
        actionStats.get(editor)[0]++;

        // Approval stats[1]
        if (action.getName().equals("APPROVE")) {
          actionStats.get(editor)[1]++;
          conceptStats.get("APPROVE").add(action.getComponentId());
        }

        // Rels stats[2]
        if (action.getName().equals("ADD_RELATIONSHIP")) {
          actionStats.get(editor)[2]++;
        }

        // STY stats[3]
        if (action.getName().equals("ADD_SEMANTIC_TYPE")) {
          actionStats.get(editor)[3]++;
        }

        // SPLIT stats[4]
        if (action.getName().equals("SPLIT")) {
          actionStats.get(editor)[4]++;
        }

        // MERGE stats[5]
        if (action.getName().equals("MERGE")) {
          actionStats.get(editor)[5]++;
        }

      }

      // EMS v3 Daily Editing Report for Dec 01, 2016
      // Database: memestg
      // Time now: Fri Dec 2 06:02:22 EST 2016
      //
      // Concepts Approved this day: 105
      // Distinct: 105
      // Number of actions this day: 193
      //
      // Shown below are editing statistics for each authority. The E-{initials}
      // authority shows approvals done in the interface while the S-{initials}
      // authority counts batch or stamping approvals. The percentages show
      // the proportion of each, by editor.
      //
      // Authority Actions Concepts Approved Rels Inserted STYs Inserted Splits
      // Merges
      // --------- ------- ----------------- ------------- ------------- ------
      // ------
      // E-LAR 100 38 (100.0%) 19 1 1 15
      //
      // E-LLW 93 67 (100.0%) 4 4 2 0
      //
      // --------------------------------------------
      // For more detail, follow this link to the EMS
      report.append("EMS v3 Daily Editing Report for " + yesterday)
          .append("\n");
      report.append("Database : " + ConfigUtility.getConfigProperties()
          .getProperty("javax.persistence.jdbc.url")).append("\n");
      report.append("Time now: " + new Date(start)).append("\n");
      report.append("\n");
      report.append("Concepts Approved this day: " + actionStats.get("APPROVE"))
          .append("\n");
      report.append(
          "                  Distinct: " + conceptStats.get("APPROVE").size())
          .append("\n");
      report.append("Number of actions this day: " + actionCt).append("\n");
      report.append(
          "Shown below are editing statistics for each authority.  The E-{initials}\n");
      report.append(
          "authority shows approvals done in the interface while the S-{initials}\n");
      report.append(
          "authority counts batch or stamping approvals.  The percentages show\n");
      report.append("the proportion of each, by editor.\n");
      report.append("\n");
      report.append(
          "Authority  Actions  Concepts Approved  Rels Inserted  STYs Inserted  Splits  Merges\n");
      report.append(
          "---------  -------  -----------------  -------------  -------------  ------  ------\n");
      for (final String editor : actionStats.keySet()) {
        final int[] stats = actionStats.get(editor);
        report.append(
            String.format("%9s  %7d  %11d (%3d)  %13d  %12d  %7d  %6d\n",
                editor, stats[0], stats[1],
                (int) (stats[1] * 100.0 / conceptStats.get(editor).size()),
                stats[2], stats[3], stats[4], stats[5]));
      }

      report.append("--------------------------------------------\n");
      report.append("For more detail, follow this link to the EMS\n");

      // Send email if configured.
      if (!ConfigUtility.isEmpty(getEmail())) {
        String from = null;
        if (config.containsKey("mail.smtp.from")) {
          from = config.getProperty("mail.smtp.from");
        } else {
          from = config.getProperty("mail.smtp.user");
        }
        ConfigUtility.sendEmail(
            "MEME Daily Editing Report - "
                + ConfigUtility.DATE_YYYYMMDD.format(yesterday),
            from, getEmail(), report.toString(), config);
      }
      logInfo("  report = \n\n" + report);
      logInfo("Finished daily editing report");

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    return super.getParameters();
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Daily Editing Report";
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
