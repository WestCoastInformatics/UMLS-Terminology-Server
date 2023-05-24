/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo;

import java.util.List;
import java.util.Properties;

import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.AlgorithmParameterJpa;
import com.wci.umls.server.jpa.ValidationResultJpa;

/**
 * Implementation of an algorithm to truncate files indicated
 */
public class TableTruncationAlgorithm extends AbstractAlgorithm {

  /** The tables that will be truncated. */
  private String truncateTables;

  /**
   * Instantiates an empty {@link TableTruncationAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public TableTruncationAlgorithm() throws Exception {
    super();
  }

  /**
   * Sets the tables to truncate
   *
   * @param truncateTables the tables to truncate
   */
  public void setTruncateTables(String truncateTables) {
    this.truncateTables = truncateTables;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());    
   
    truncateTables(truncateTables);
    // fullTextEntityManager.close();

    logInfo("Finished " + getName());    
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
   // n/a
  }

  /**
   * Truncate tables
   *
   * @param truncateTables the tables to be truncated
   * @throws Exception the exception
   */
  private void truncateTables(String truncateTables) throws Exception {

    // remove white-space and split by comma
    String[] tablesToTruncate = truncateTables.replaceAll(" ", "").split(",");

    Logger.getLogger(getClass()).info("Starting truncation for:");
    for (final String tableToTruncate : tablesToTruncate) {
      Logger.getLogger(getClass()).info("  " + tableToTruncate);

      Query query =
          getEntityManager().createNativeQuery("truncate table " + tableToTruncate);
      //query.setParameter("truncateTable", tableToTruncate);
      int returnValue = query.executeUpdate();
    }

    // Cleanup
    Logger.getLogger(getClass()).info("done ...");
  }



  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        "truncateTables"
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    if (p.getProperty("truncateTables") != null) {
      truncateTables = p.getProperty("truncateTables");
    }
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();
    final AlgorithmParameter param =
        new AlgorithmParameterJpa("Tables to Truncate", "truncateTables",
            "Comma-separated list of table names to be truncated.", "",
            255, AlgorithmParameter.Type.STRING, "");
    params.add(param);
    return params;

  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
