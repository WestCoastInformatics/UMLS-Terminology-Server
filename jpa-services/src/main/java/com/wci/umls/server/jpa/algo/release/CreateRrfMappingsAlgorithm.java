/*
 * Copyright 2024 Wci Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Wci Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * Wci Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Algorithm for creating RRF release statistics modeled over those provided by UMLS
 */
public class CreateRrfMappingsAlgorithm extends AbstractAlgorithm {

  /**  The path meta. */
  private File pathMeta = null;
  
  /**  The mappings dir. */
  private File mappingsDir = null;
  
  /**  The mappings writer. */
  private BufferedWriter mappingsWriter = null;
  
  /**  The readme writer. */
  private BufferedWriter readmeWriter = null;
  
  
  /**
   * Instantiates an empty {@link CreateRrfMappingsAlgorithm}.
   *
   * @throws Exception the exception
   */
  public CreateRrfMappingsAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RRFMAPPINGS");

  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    
    pathMeta =
        new File(path, "/" + getProcess().getVersion() + "/META");
    logInfo("  pathMeta " + pathMeta);


    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    
    // open output writers
    mappingsDir = new File(pathMeta, "mappings");
    if (!mappingsDir.exists()){
      mappingsDir.mkdir();
    }
    logInfo("mappings dir:" + mappingsDir);



    
    mappingsWriter = new BufferedWriter(new FileWriter(new File(mappingsDir, "NCIt_Metathesaurus_Mapping_" + getProcess().getVersion() + ".txt")));
    readmeWriter = new BufferedWriter(new FileWriter(new File(mappingsDir, "NCIt_Metathesaurus_Mapping.README.txt")));
    
    
    // create mapping report
    createMappings();
    createReadme();
    
    mappingsWriter.close();
    readmeWriter.close(); 
    
    logInfo("Finished " + getName());

  }
  
  private void createMappings() throws Exception {
	  
	  List<Object[]> results = executeQuery(
			  "select distinct c1.terminologyId 'NCI Meta CUI', c1.name 'NCI Meta Concept Name', a1.codeId 'NCI Code', a1.name 'NCI PT', "+
			  " a2.codeId 'Source Atom Code', a2.name 'Source Atom Name', a2.terminology Source, a2.version 'Version', a2.termType 'Source Term Type' " +
					  " from " +
					  "  concepts c1," +
					  "  atoms a1," +
					  "  concepts c2," +
					  "  atoms a2," +
					  "  concepts_atoms ca1," +
					  "  concepts_atoms ca2" +
					  " where" +
					  "  c1.terminology = 'NCIMTH'" +
					  "  and c2.terminology = 'NCIMTH'" +
					  "  and c1.id = ca1.concepts_id" +
					  "  and ca1.atoms_id = a1.id" +
					  "  and c2.id = ca2.concepts_id" +
					  "  and ca2.atoms_id = a2.id" +
					  "  and c1.id = c2.id" +
					  "  and a1.terminology = 'NCI'" +
					  "  and a1.termType = 'PT'" +
					  "  and a1.publishable = true" +
					  "  and a2.terminology in ('HCPCS', 'ICD10PCS', 'ICD9CM', 'ICDO', 'MDR', 'GO', 'HGNC', 'HPO', 'ICD10CM', 'ICD10', 'LNC', 'MED-RT', 'NCBI', 'RADLEX', 'SNOMEDCT_US')" +
					  "  and a2.publishable = true" +
					  "  order by c1.terminologyId",  
					  QueryType.SQL,			  
			  getDefaultQueryParams(this.getProject()), false);
	  
	  // write headers
	  mappingsWriter.write("NCI Meta CUI\tNCI Meta Concept Name\tNCI Code\tNCI PT\tSource Atom Code\tSource Atom Name\tSource\tVersion\tSource Term Type");
	  mappingsWriter.newLine();
	  
	  // write result rows
	  int ct = 0;
	  for (Object[] result : results) {
		  mappingsWriter.write(result[0].toString() + "\t");
		  mappingsWriter.write(result[1].toString() + "\t");
		  mappingsWriter.write(result[2].toString() + "\t");
		  mappingsWriter.write(result[3].toString() + "\t");
		  mappingsWriter.write(result[4].toString() + "\t");
		  mappingsWriter.write(result[5].toString() + "\t");
		  mappingsWriter.write(result[6].toString() + "\t");
		  mappingsWriter.write(result[7].toString() + "\t");
		  mappingsWriter.write(result[8].toString() );
		  mappingsWriter.newLine();
		  ct++;
		  if (ct % 5000 == 0) {
			mappingsWriter.flush();
		  	logInfo("mapping ct: " + ct);
		  }
	  }
  }
  
  private void createReadme() throws Exception {
	  
	  List<Object[]> results = executeQuery(
			  "select distinct terminology 'Source', version 'Version'" +
					  "  from terminologies " + 
					  "  where current and terminology in ('HCPCS', 'ICD10PCS', 'ICD9CM', 'ICDO', 'MDR', 'GO', 'HGNC', 'HPO', 'ICD10CM', 'ICD10', 'LNC', 'MED-RT', 'NCBI', 'RADLEX', 'SNOMEDCT_US')" +
					  "  order by terminology, version",  
					  QueryType.SQL,			  
			  getDefaultQueryParams(this.getProject()), false);
	  
	  // write headers
	  readmeWriter.write("Source\tVersion");
	  readmeWriter.newLine();
	  
	  // write result rows
	  for (Object[] result : results) {
		  readmeWriter.write(result[0].toString() + "\t");
		  readmeWriter.write(result[1].toString() );
		  readmeWriter.newLine();
	  }
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());

    
    logInfo("Finished RESET " + getName());
  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
