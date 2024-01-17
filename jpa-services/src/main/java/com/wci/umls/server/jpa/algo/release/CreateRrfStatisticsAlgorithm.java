/*
 * Copyright 2020 Wci Informatics - All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of Wci Informatics
 * The intellectual and technical concepts contained herein are proprietary to
 * Wci Informatics and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law.  Dissemination of this information
 * or reproduction of this material is strictly forbidden.
 */
package com.wci.umls.server.jpa.algo.release;

import static java.util.Collections.reverseOrder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;

/**
 * Algorithm for creating RRF release statistics modeled over those provided by UMLS
 */
public class CreateRrfStatisticsAlgorithm extends AbstractAlgorithm {

  /**  The path meta. */
  private File pathMeta = null;
  
  /**  The stats dir. */
  private File statsDir = null;
  
  /**  The report dir. */
  private File reportDir = null;
  
  /**  The concept sty map. */
  private Map<String, Set<String>> conceptStyMap = new HashMap<>();
  
  /**  The rel stats writer. */
  private BufferedWriter relStatsWriter = null;
  
  /**  The tty stats writer. */
  private BufferedWriter ttyStatsWriter = null;
  
  /**  The sty stats writer. */
  private BufferedWriter styStatsWriter = null;
 
  /**  The attribute stats writer. */
  private BufferedWriter attributeStatsWriter = null;
  
  /**  The counts writer. */
  private BufferedWriter countsWriter = null;
  
  /**  The source counts writer. */
  private BufferedWriter sourceCountsWriter = null;
  
  private int mrrelCount = 0;
  
  private int mrconsoCount = 0;
  
  private int mrsatCount = 0;
  
  /**
   * Instantiates an empty {@link CreateRrfStatisticsAlgorithm}.
   *
   * @throws Exception the exception
   */
  public CreateRrfStatisticsAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RRFSTATS");

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
    statsDir = new File(pathMeta, "stats");
    if (!statsDir.exists()){
      statsDir.mkdir();
    }
    logInfo("stats dir:" + statsDir);
    
    relStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, "relStats.txt")));
    ttyStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, "ttyStats.txt")));
    styStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, "styStats.txt")));
    attributeStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, "attributeStats.txt")));
   
    // create source overlap statistics
    createMRCONSOSourceOverlapStatistics();
    
    // cache data needed for semantic type statistics
    cacheConceptStys();
    
    // create attribute statistics
    createAttributeStatistics();
    
    // create relationship statistics
    createRelStatistics();
    
    // iterate through MRCONSO and create sty stats, tty stats and source overlap stats
    createMRCONSORelatedStatistics();    

    relStatsWriter.close();
    ttyStatsWriter.close();
    styStatsWriter.close();
    attributeStatsWriter.close();
    
    // make reports subdirectory
    reportDir = new File(statsDir, "reports");
    if (!reportDir.exists()){
    	reportDir.mkdir();
    }
    logInfo("report dir:" + reportDir);
    
    countsWriter = new BufferedWriter(new FileWriter(new File(reportDir, "counts.txt")));
    sourceCountsWriter = new BufferedWriter(new FileWriter(new File(reportDir, "sourceCounts.txt")));
    
    // create counts reports
    createCountReports();
    createSourceCounts();
    
    countsWriter.close();
    sourceCountsWriter.close();
    
    
    logInfo("Finished " + getName());

  }
  
  private void createCountReports() throws Exception {
	  countsWriter.write("Relationships" + "\t" + mrrelCount);
	  countsWriter.newLine();
	  countsWriter.write("Attributes" + "\t" + mrsatCount);
	  countsWriter.newLine();
	  countsWriter.write("Total terms" + "\t" + mrconsoCount);
	  countsWriter.newLine();
	  countsWriter.write("Concepts" + "\t" + conceptStyMap.keySet().size());
	  countsWriter.newLine();
	  countsWriter.flush();
  }
  

  
  /**
   * Creates the attribute statistics.
   *
   * @throws Exception the exception
   */
  private void createAttributeStatistics() throws Exception {
    final String attributesFile = pathMeta + File.separator + "MRSAT.RRF";
    BufferedReader attributes = null;
    try {
      attributes = new BufferedReader(new FileReader(attributesFile));
    } catch (Exception e) {
      if (attributes != null) {
        attributes.close();
      }
      throw new Exception("File not found: " + attributesFile);
    }

    String line = null;
    String[] fields = new String[13];
    Map<Pair<String, String>, Integer> sourceAttributesCtMap = new HashMap<>();
    
    // process attributes from MRSAT.RRF
    while ((line = attributes.readLine()) != null) {
        FieldedStringTokenizer.split(line, "|", 13, fields);
        final Pair<String, String> sourceAttributePair =
            new ImmutablePair<>(fields[9], fields[8]);
        if (sourceAttributesCtMap.containsKey(sourceAttributePair)) {
          Integer current = sourceAttributesCtMap.get(sourceAttributePair);
          sourceAttributesCtMap.put(sourceAttributePair, ++current);
        } else {
          sourceAttributesCtMap.put(sourceAttributePair, 1);
        } 
        mrsatCount++;
    }
    
    // sort the results
    Map<Pair<String, String>, Integer> result = sourceAttributesCtMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    logInfo(result.toString());
    
    // write the results to a file
    for (Pair<String, String> sourceAttributePair : result.keySet()) {
      attributeStatsWriter.write(sourceAttributePair.getKey() + "|" + sourceAttributePair.getValue() + "|" + sourceAttributesCtMap.get(sourceAttributePair).intValue());
      attributeStatsWriter.newLine();
      attributeStatsWriter.flush();
    }
    
    attributes.close();
  }
  
  private void createSourceCounts() throws Exception {
	    final String sourcesFile = pathMeta + File.separator + "MRSAB.RRF";
	    BufferedReader sources = null;
	    try {
	      sources = new BufferedReader(new FileReader(sourcesFile));
	    } catch (Exception e) {
	      throw new Exception("File not found: " + sourcesFile);
	    }

	    String line = null;
	    String[] fields = new String[25];
	    
	    Map<String, String> ncitSources = new TreeMap<>();
	    Map<String, String> otherSources = new TreeMap<>();

	    while ((line = sources.readLine()) != null) {
	        FieldedStringTokenizer.split(line, "|", 25, fields);
	        if (fields[6].matches("\\d{4}_\\d{2}\\D{1}")) {
	        	ncitSources.put(fields[3] + fields[6], fields[15] + "\t"  + fields[3] + "\t" + fields[4]);
	        } else {
	        	otherSources.put(fields[3] + fields[6], fields[15] + "\t" + fields[3] + "\t" + fields[4]);
	        }
	        
	    }

	    // sort and write out ncit source rows
	    SortedSet<String> keys = new TreeSet<>(ncitSources.keySet());
	    for (String key : keys) { 
	       String source = ncitSources.get(key);
	       sourceCountsWriter.write(source);
	       sourceCountsWriter.newLine();
	    }
	    
	    // sort and write out other source rows
	    keys = new TreeSet<>(otherSources.keySet());
		for (String key : keys) { 
		    String source = otherSources.get(key);
		    sourceCountsWriter.write(source);
	        sourceCountsWriter.newLine();
		}
	    
	    sources.close();
  }
  
  /**
   * Creates the rel statistics.
   *
   * @throws Exception the exception
   */
  private void createRelStatistics() throws Exception {
    final String relationshipsFile = pathMeta + File.separator + "MRREL.RRF";
    BufferedReader relationships = null;
    try {
      relationships = new BufferedReader(new FileReader(relationshipsFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + relationshipsFile);
    }

    String line = null;
    String[] fields = new String[16];
    Map<Pair<String, String>, Integer> sourceRelationshipsCtMap = new HashMap<>();
    

    while ((line = relationships.readLine()) != null) {
        FieldedStringTokenizer.split(line, "|", 16, fields);
        final Pair<String, String> sourceRelationshipPair =
            new ImmutablePair<>(fields[11], fields[3] + "/" + fields[7]);
        if (sourceRelationshipsCtMap.containsKey(sourceRelationshipPair)) {
          Integer current = sourceRelationshipsCtMap.get(sourceRelationshipPair);
          sourceRelationshipsCtMap.put(sourceRelationshipPair, ++current);
        } else {
          sourceRelationshipsCtMap.put(sourceRelationshipPair, 1);
        } 
        mrrelCount++;
    }
    
    // sort the results
    Map<Pair<String, String>, Integer> result = sourceRelationshipsCtMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    logInfo(result.toString());
    
    // write the results to a file
    for (Pair<String, String> sourceRelationshipPair : result.keySet()) {
      relStatsWriter.write(sourceRelationshipPair.getKey() + "|" + sourceRelationshipPair.getValue() + "|" + sourceRelationshipsCtMap.get(sourceRelationshipPair).intValue());
      relStatsWriter.newLine();
      relStatsWriter.flush();
    }
    
    relationships.close();
  }
  
  /**
   * Creates the MRCONSO source overlap statistics.
   *
   * @throws Exception the exception
   */
  private void createMRCONSOSourceOverlapStatistics() throws Exception {
    final String atomsFile = pathMeta + File.separator + "MRCONSO.RRF";
    BufferedReader atoms = null;
    try {
      atoms = new BufferedReader(new FileReader(atomsFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + atomsFile);
    }

    String line = null;
    String[] fields = new String[18];
    Map<String, Set<Pair<String, Integer>>> sourceSourceOverlapMap = new HashMap<>();
    Map<String, Integer> sourceTotalConceptsMap = new HashMap<>();
    Set<String> conceptSources = new HashSet<>();

    // initialize loop
    atoms.mark(2000);
    line = atoms.readLine();
    String currentId = line.substring(0, line.indexOf("|"));
    atoms.reset();
    atoms.mark(2000);

    while ((line = atoms.readLine()) != null) {
      FieldedStringTokenizer.split(line, "|", 18, fields);
      

      // collect sources with atoms contributing to this concept
      if (currentId.equals(fields[0])) {
        conceptSources.add(fields[11]);
      } else {
        atoms.reset();
        currentId = fields[0];


        List<String> conceptSourcesList = new ArrayList<String>();
        conceptSourcesList.addAll(conceptSources);
        
        // increment sourceTotalConceptsMap
        for (int i = 0; i < conceptSourcesList.size(); i++) {
          String src = conceptSourcesList.get(i);
          if (sourceTotalConceptsMap.containsKey(src)) {
            Integer conceptCt = sourceTotalConceptsMap.get(src);        
            sourceTotalConceptsMap.remove(src);
            sourceTotalConceptsMap.put(src, ++conceptCt);
          } else {
            sourceTotalConceptsMap.put(src, 1);
          }
        }
        
        // process current concept

        // for each source increment count for all other co-occuring sources
        for (int i = 0; i < conceptSourcesList.size(); i++) {
          String src = conceptSourcesList.get(i);
          
          for (int j = 0; j < conceptSourcesList.size(); j++) {
            String jSrc = conceptSourcesList.get(j);
            if (!src.equals(jSrc)) {
              Set<Pair<String, Integer>> sourceCtList = sourceSourceOverlapMap.get(src);
              Pair<String, Integer> sourceCtPair = null;
              // case when src is already tracked, check if jSrc is also tracked - increment counter if so
              if (sourceSourceOverlapMap.containsKey(src)) {
                try {
                  sourceCtPair = sourceCtList.stream().filter(s -> s.getLeft().equals(jSrc))
                      .collect(Collectors.toList()).get(0);

                  sourceCtList.remove(sourceCtPair);
                  sourceSourceOverlapMap.put(src, sourceCtList);
                  Integer incrementedCt = sourceCtPair.getValue() + 1;
                  sourceCtPair = new ImmutablePair<>(jSrc, incrementedCt);
                  sourceCtList.add(sourceCtPair);
                // jSrc not yet tracked, add it
                } catch (Exception e) {
                  sourceCtPair = new ImmutablePair<>(jSrc, 1);
                  sourceCtList.add(sourceCtPair);
                  sourceSourceOverlapMap.put(src, sourceCtList);
                }
              // src not yet tracked, add it
              } else {
                sourceCtPair = new ImmutablePair<>(jSrc, 1);
                sourceCtList = new HashSet<>();
                sourceCtList.add(sourceCtPair);
                sourceSourceOverlapMap.put(src, sourceCtList);
              }
            }
          }
        }

        // reset before processing next concept
        conceptSources = new HashSet<>();
        conceptSourcesList.clear();
      }

      atoms.mark(3000);
    }

    // write out txt files
    for (Entry<String, Set<Pair<String, Integer>>> entry : sourceSourceOverlapMap.entrySet()) {
      
      File srcDir = new File(statsDir, entry.getKey());
      if (!srcDir.exists()){
        srcDir.mkdir();
      }
      logInfo("src dir:" + srcDir);
      
      BufferedWriter overlapStatsWriter =
          new BufferedWriter(new FileWriter(new File(srcDir, entry.getKey() + ".txt")));

      List<Pair<String, Integer>> sortedResults = entry.getValue().stream()
          .sorted(reverseOrder(Map.Entry.comparingByValue()))
          .collect(Collectors.toList());

      logInfo(entry.getKey() + " | " + sortedResults.toString());
      
      StringBuilder report = new StringBuilder();
      for (Pair<String, Integer> pair : sortedResults) {
        report.append(pair.getLeft());
        report.append("|");
        report.append(pair.getRight().toString());
        report.append("/");
        report.append(sourceTotalConceptsMap.get(entry.getKey()));
        
        float total_marks = sourceTotalConceptsMap.get(entry.getKey());
        float scored = pair.getRight().floatValue();
        float percentage = (scored / total_marks) ;
        DecimalFormat decFormat = new DecimalFormat("#%");
        
        report.append("|");
        report.append(decFormat.format(percentage));
        report.append(System.getProperty("line.separator"));

      }

      overlapStatsWriter.write(report.toString());
      overlapStatsWriter.close();
    }
    
    
    /**for (Entry<String, Set<Pair<String, Integer>>> entry : sourceSourceOverlapMap.entrySet()) {
      logInfo(entry.getKey() + " | " + entry.getValue());
      BufferedWriter overlapStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, entry.getKey() + ".txt")));
      for (Pair<String, Integer> pair : entry.getValue()) {
        overlapStatsWriter.write(pair.getLeft());
        overlapStatsWriter.write("|");
        overlapStatsWriter.write(pair.getRight().toString());
        overlapStatsWriter.newLine();
      }
      overlapStatsWriter.close();
    }*/
    atoms.close();
  }
  
  /**
   * Creates the MRCONSO related statistics.
   *
   * @throws Exception the exception
   */
  private void createMRCONSORelatedStatistics() throws Exception {
    final String atomsFile = pathMeta + File.separator + "MRCONSO.RRF";
    BufferedReader atoms = null;
    try {
      atoms = new BufferedReader(new FileReader(atomsFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + atomsFile);
    }

    String line = null;
    String[] fields = new String[18];
    Map<Pair<String, String>, Integer> sourceTtysCtMap = new HashMap<>();
    Map<Pair<String, String>, Integer> sourceStysCtMap = new HashMap<>();   

    while ((line = atoms.readLine()) != null) {
    	mrconsoCount++;
    	
        FieldedStringTokenizer.split(line, "|", 18, fields);
        
        // process term types
        final Pair<String, String> sourceTtyPair =
            new ImmutablePair<>(fields[11], fields[12]);
        if (sourceTtysCtMap.containsKey(sourceTtyPair)) {
          Integer current = sourceTtysCtMap.get(sourceTtyPair);
          sourceTtysCtMap.put(sourceTtyPair, ++current);
        } else {
          sourceTtysCtMap.put(sourceTtyPair, 1);
        } 
        
        // process semantic types
        Set<String> stys = conceptStyMap.get(fields[0]);
        for (String sty : stys) {
          final Pair<String, String> sourceStyPair =
              new ImmutablePair<>(fields[11], sty);
          if (sourceStysCtMap.containsKey(sourceStyPair)) {
            Integer current = sourceStysCtMap.get(sourceStyPair);
            sourceStysCtMap.put(sourceStyPair, ++current);
          } else {
            sourceStysCtMap.put(sourceStyPair, 1);
          } 
        }
    }
    
    // sort the tty results
    Map<Pair<String, String>, Integer> result = sourceTtysCtMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    logInfo(result.toString());
    
    // write the tty results to a file
    for (Pair<String, String> sourceTtyPair : result.keySet()) {
      ttyStatsWriter.write(sourceTtyPair.getKey() + "|" + sourceTtyPair.getValue() + "|" + sourceTtysCtMap.get(sourceTtyPair).intValue());
      ttyStatsWriter.newLine();
      ttyStatsWriter.flush();
    }
    
    // sort the sty results
    result = sourceStysCtMap.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (oldValue, newValue) -> oldValue, LinkedHashMap::new));

    logInfo(result.toString());
    
    // write the sty results to a file
    for (Pair<String, String> sourceTtyPair : result.keySet()) {
      styStatsWriter.write(sourceTtyPair.getKey() + "|" + sourceTtyPair.getValue() + "|" + sourceStysCtMap.get(sourceTtyPair).intValue());
      styStatsWriter.newLine();
      styStatsWriter.flush();
    }
    
    atoms.close();
    
  }

  /**
   * Cache concept stys.
   *
   * @throws Exception the exception
   */
  private void cacheConceptStys() throws Exception {

    final String stysFile = pathMeta + File.separator + "MRSTY.RRF";
    BufferedReader stys = null;
    try {
      stys = new BufferedReader(new FileReader(stysFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + stysFile);
    }

    String line = null;
    String[] fields = new String[6];
    
    // cache concept/sty combos for use later when iterating through MRCONSO
    while ((line = stys.readLine()) != null) {
        FieldedStringTokenizer.split(line, "|", 6, fields);
        
        if (conceptStyMap.containsKey(fields[0])) {
          Set<String> currentStys = conceptStyMap.get(fields[0]);
          currentStys.add(fields[1]);
          conceptStyMap.put(fields[0], currentStys);
        } else {
          Set<String> currentStys = new HashSet<>();
          currentStys.add(fields[1]);
          conceptStyMap.put(fields[0], currentStys);
        }      
    }

    stys.close();
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
