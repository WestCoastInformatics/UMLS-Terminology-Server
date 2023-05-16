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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.plexus.util.FileUtils;

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
  
  /**  The concept sty map. */
  private Map<String, Set<String>> conceptStyMap = new HashMap<>();
  
  /**  The rel stats writer. */
  private BufferedWriter relStatsWriter = null;
  
  /**  The tty stats writer. */
  private BufferedWriter ttyStatsWriter = null;
  
  /**  The sty stats writer. */
  private BufferedWriter styStatsWriter = null;
  
  /**  The source overlap stats writer. */
  private BufferedWriter sourceOverlapStatsWriter = null;
  
  /**  The attribute stats writer. */
  private BufferedWriter attributeStatsWriter = null;
  
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
    sourceOverlapStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, "sourceOverlapStats.txt")));
    attributeStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, "attributeStats.txt")));
   
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
    sourceOverlapStatsWriter.close();
    attributeStatsWriter.close();
    
    logInfo("Finished " + getName());

  }
  
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
    
  }
  

  
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
    
  }
  
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

        // process current concept
        List<String> conceptSourcesList = new ArrayList<String>();
        conceptSourcesList.addAll(conceptSources);

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

    for (Entry<String, Set<Pair<String, Integer>>> entry : sourceSourceOverlapMap.entrySet()) {
      logInfo(entry.getKey() + " | " + entry.getValue());
      BufferedWriter overlapStatsWriter = new BufferedWriter(new FileWriter(new File(statsDir, entry.getKey() + ".txt")));
      for (Pair<String, Integer> pair : entry.getValue()) {
        overlapStatsWriter.write(pair.getLeft());
        overlapStatsWriter.write("|");
        overlapStatsWriter.write(pair.getRight().toString());
        overlapStatsWriter.newLine();
      }
      overlapStatsWriter.close();
    }
    atoms.close();
  }
  
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

  private void cacheConceptStys() throws Exception {

    final String stysFile = pathMeta + File.separator + "MRSTY.RRF";
    BufferedReader stys = null;
    try {
      stys = new BufferedReader(new FileReader(stysFile));
    } catch (Exception e) {
      throw new Exception("File not found: " + stysFile);
    }

    String linePre = null;
    String line = null;
    String[] fields = new String[6];
    
    // cache concept/sty combos for use later when iterating through MRCONSO
    while ((line = stys.readLine()) != null) {
      if (line != null) {
        FieldedStringTokenizer.split(line, "|", 6, fields);
        
        if (conceptStyMap.containsKey(fields[0])) {
          Set currentStys = conceptStyMap.get(fields[0]);
          currentStys.add(fields[1]);
          conceptStyMap.put(fields[0], currentStys);
        } else {
          Set<String> currentStys = new HashSet<>();
          currentStys.add(fields[1]);
          conceptStyMap.put(fields[0], currentStys);
        }
      }         
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());

    // Remove the output zip file
    final File path = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath());
    logInfo("  path " + path);

    final String filename = getProcess().getVersion() + ".zip";
    final File zipFile =
        new File(path, getProcess().getVersion() + "/" + filename);
    if (zipFile.exists()) {
      FileUtils.fileDelete(zipFile.getAbsolutePath());
    }
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
