/**
 * Copyright 2018 West Coast Informatics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wci.umls.server.mojo.analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Used to search the term-server database for concepts with a matching string.
 * 
 * See matcher/pom.xml for a sample invocation.
 *
 * @author Rick Wood
 */
@Mojo(name = "neoplasm-frequencies", defaultPhase = LifecyclePhase.PACKAGE)
public class NeoplasmFrequenciesMojo extends AbstractMojo {

  /** The input file path for descriptions. */
  private String descInputFilePath =
      "C:\\Users\\rwood\\Desktop\\temp\\ICD11 Neoplasms\\Input Files\\Neoplasm Descriptions v6.txt";

  /** The input file path for relationships. */
  private String relInputFilePath =
      "C:\\Users\\rwood\\Desktop\\temp\\ICD11 Neoplasms\\Input Files\\Neoplasm Relationships v3.txt";

  /** The input file path for the white list. */
  private String whiteListInputFilePath =
      "C:\\Users\\rwood\\Desktop\\temp\\ICD11 Neoplasms\\Input Files\\whiteList.txt";

  /** The output file location. */
  private String outputFilePath =
      "C:\\Users\\rwood\\Desktop\\temp\\ICD11 Neoplasms\\Output Files\\";

  /** The minimum count required for printing and searching. */
  private int minCount = 100;

  private int maxComboSize = 14;

  private int totalCombos = 0;

  private List<List<String>> descriptionData = new ArrayList<>();

  private Map<String, List<NeoplasmRelationship>> relationshipData =
      new HashMap<>();

  private Map<String, Map<String, Integer>> headerValueCountMap =
      new HashMap<>();

  private Map<String, Integer> headerHighestCountMap = new HashMap<>();

  private Map<String, Integer> valueCountMap = new HashMap<>();

  @Override
  public void execute() throws MojoFailureException {
    try {

      getLog().info("Neoplasms Frequencies Mojo");

      /*
       * Error Checking
       */
      if (!new File(descInputFilePath).exists()) {
        throw new Exception(
            "Specified input file doesn't exist: " + descInputFilePath);
      }
      if (!new File(relInputFilePath).exists()) {
        throw new Exception(
            "Specified input file doesn't exist: " + relInputFilePath);
      }
      if (!new File(outputFilePath).exists()) {
        throw new Exception(
            "Specified output folder doesn't exist: " + outputFilePath);
      }

      // Read the whiteList into a Set
      Set<String> whiteListConcepts = new HashSet<>();

      BufferedReader reader =
          new BufferedReader(new FileReader(whiteListInputFilePath));

      String line = reader.readLine();

      // Save each concept Id to the set
      while (line != null) {
        whiteListConcepts.add(line.trim());
        line = reader.readLine();
      }
      reader.close();

      /*
       * Read in Descriptions file The data will be saved as a list of lists.
       * Each sub-list representing one column E.x. Header1 Header2 Header3
       * DataA DataB DataC DataD DataE DataF DataG DataH DataI
       * 
       * List0 = {List1, List2, List3} List1 = {Header1, DataA, DataD, DataG}
       * List2 = {Header2, DataB, DataE, DataH} List3 = {Headet3, DataC, DataF,
       * DataI}
       * 
       */

      reader = new BufferedReader(new FileReader(descInputFilePath));

      line = reader.readLine();
      String[] data = line.split("\\|", -1);

      // Initialize all of the sub-lists
      for (int i = 0; i < data.length; i++) {
        descriptionData.add(new ArrayList<>(Arrays.asList(data[i])));
      }

      // Save all of the data to their appropriate list
      line = reader.readLine();
      while (line != null) {
        data = line.split("\\|", -1);

        // Only add this concepts' data if it's not on the whiteList
        if (!whiteListConcepts.contains(data[0])) {
          for (int i = 0; i < data.length; i++) {
            descriptionData.get(i).add(data[i]);
          }
        }
        line = reader.readLine();
      }
      reader.close();

      // Determine which indexes in the lists should be skipped due to their
      // concepts being on the white list

      /*
       * Read in Relationships file
       */

      // reader = new BufferedReader(new FileReader(relInputFilePath));
      //
      // line = reader.readLine();
      // data = line.split("\\|", -1);
      //
      // // Initialize the map for new conceptId
      // if (!relationshipData.containsKey(data[0])) {
      // relationshipData.put(data[0], new ArrayList<>());
      // }
      //
      // // Create relationship objects, and and them to their concept's list
      // line = reader.readLine();
      // while (line != null) {
      // data = line.split("\\|", -1);
      //
      // NeoplasmRelationship rel = new NeoplasmRelationship();
      // rel.setRelationshipType(data[2]);
      // rel.setRelationshipDestination(data[3]);
      // rel.setRoleGroup(data[4]);
      //
      // relationshipData.get(data[0]).add(rel);
      //
      // line = reader.readLine();
      // }
      // reader.close();

      /*
       * Generate the counts
       */

      // For the rest of the columns, count the occurrences of each value
      // Don't count the first two columns - ConceptId and ConceptName. They are
      // skipped.
      totalCombos = getTotalCombos(descriptionData.size() - 2, maxComboSize);

      getLog().info("Total combinations to be calculated = " + totalCombos);

      generateCounts(new ArrayList<>(), new ArrayList<>(), 2, maxComboSize);

      identifyHighestCounts();

      getLog().info(
          "Total combinations calculated = " + headerValueCountMap.size());

      StringBuilder singleSb = new StringBuilder();
      StringBuilder multiSb = new StringBuilder();

      // Sort the headers by the max count
      Map<String, Integer> sortedHeaderHighestCountMap =
          sortByValue(headerHighestCountMap);

      System.out.println(sortedHeaderHighestCountMap);

      for (String header : sortedHeaderHighestCountMap.keySet()) {
        boolean headerPrinted = false;

        Map<String, Integer> sortedValueCountMap =
            sortByValue(headerValueCountMap.get(header));

        for (String value : sortedValueCountMap.keySet()) {
          Integer count = headerValueCountMap.get(header).get(value);
          if (count > minCount) {
            if (!headerPrinted) {
              if (header.contains("/")) {
                multiSb.append("--------------------\n");
                multiSb.append(header + "\n\n");
              } else {
                singleSb.append("--------------------\n");
                singleSb.append(header + "\n\n");
              }
              headerPrinted = true;
            }
            if (header.contains("/")) {
              multiSb.append(value + " = " + count + "\n");
            } else {
              singleSb.append(value + " = " + count + "\n");
            }

          }
        }
      }

      getLog().info(singleSb);
      getLog().info(multiSb);

      // Write the string builder to file

      File file = new File(
          outputFilePath + "NeoplasmFrequencies_SingleCriteria_MinCount"
              + minCount + "_" + new Date().getTime() + ".txt");
      File multifile =
          new File(outputFilePath + "NeoplasmFrequencies_MultiCriteria_MinCount"
              + minCount + "_" + new Date().getTime() + ".txt");
      BufferedWriter writer = null;
      try {
        writer = new BufferedWriter(new FileWriter(file));
        writer.write(singleSb.toString());
      } finally {
        if (writer != null)
          writer.close();
      }

      try {
        writer = new BufferedWriter(new FileWriter(multifile));
        writer.write(multiSb.toString());
      } finally {
        if (writer != null)
          writer.close();
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  private void generateCounts(List<Integer> comboColumns,
    List<String> comboValues, Integer startAtColumn, int comboCount) {

    for (int i = startAtColumn.intValue(); i < descriptionData.size(); i++) {

      // Initialize the maps for each unique header
      StringBuilder header = new StringBuilder();
      for (Integer columnIndex : comboColumns) {
        header.append(descriptionData.get(columnIndex).get(0) + " / ");
      }
      header.append(descriptionData.get(i).get(0));

      if (!headerValueCountMap.containsKey(header.toString())) {
        headerValueCountMap.put(header.toString(), new HashMap<>());
        getLog().info("processing " + headerValueCountMap.size() + " out of "
            + totalCombos + " total combinations: " + header.toString());
      }

      // Calculate the counts for each unique value or combination of values
      Map<String, Integer> stringToCountMap = new HashMap<>();
      for (int row = 1; row < descriptionData.get(i).size(); row++) {

        // Only count empty values if they're by themselves.
        if (comboValues.size() > 0
            && descriptionData.get(i).get(row).equals("")) {
          continue;
        }

        String comboValue = String.join(" / ", comboValues);

        if (comboValue.equals("")) {
          comboValue = descriptionData.get(i).get(row);
        } else {
          comboValue = comboValue + " / " + descriptionData.get(i).get(row);
        }

        // Check if this row matches for all of the passed in columns/values
        boolean everythingMatches = true;

        for (int j = 0; j < comboColumns.size(); j++) {
          Integer index = comboColumns.get(j);
          String value = comboValues.get(j);
          if (!value.equals(descriptionData.get(index).get(row))) {
            everythingMatches = false;
            break;
          }
        }

        if (everythingMatches) {
          if (!stringToCountMap.containsKey(comboValue)) {
            stringToCountMap.put(comboValue, 1);
          } else {
            stringToCountMap.put(comboValue,
                stringToCountMap.get(comboValue) + 1);
          }
        }
      }

      for (String str : stringToCountMap.keySet()) {
        String value = str.equals("") ? "NULL" : str;
        Integer count = stringToCountMap.get(str);
        if (count >= minCount) {
          headerValueCountMap.get(header.toString()).put(value, count);
        }
      }
    }

    if (comboCount > 1) {
      for (int i = startAtColumn; i < descriptionData.size(); i++) {

        StringBuilder header = new StringBuilder();
        for (Integer columnIndex : comboColumns) {
          header.append(descriptionData.get(columnIndex).get(0) + " / ");
        }
        header.append(descriptionData.get(i).get(0));

        ArrayList<Integer> subComboColumns = new ArrayList<>(comboColumns);
        subComboColumns.add(i);

        Set<String> uniqueValues = new HashSet<>();

        for (int row = 1; row < descriptionData.get(i).size(); row++) {
          String value = descriptionData.get(i).get(row);
          // Find combinations for every unique, non-empty value.
          if (!value.equals("") && !uniqueValues.contains(value)) {
            uniqueValues.add(value);
            ArrayList<String> subComboValues = new ArrayList<>(comboValues);
            subComboValues.add(value);

            String comboValue = String.join(" / ", comboValues);

            if (comboValue.equals("")) {
              comboValue = descriptionData.get(i).get(row);
            } else {
              comboValue = comboValue + " / " + descriptionData.get(i).get(row);
            }

            Integer valueCount =
                headerValueCountMap.get(header.toString()).get(comboValue);

            if (valueCount == null || valueCount < minCount) {
              if (!headerValueCountMap.containsKey(header.toString())) {
                headerValueCountMap.put(header.toString(), new HashMap<>());
              }
              if (!headerValueCountMap.get(header.toString())
                  .containsKey(header.toString())) {
                Map<String, Integer> tempMap = new HashMap<>();
                tempMap.put(comboValue, 0);
              }
            } else {
              generateCounts(subComboColumns, subComboValues, i + 1,
                  comboCount - 1);
            }
          }
        }
      }
    }
  }

  private Integer getTotalCombos(Integer totalColumns,
    Integer maxCombinationSize) {

    if (maxCombinationSize == 0) {
      return 0;
    }

    Integer numerator = 1;
    Integer denominator = 1;

    for (int i = 0; i < maxCombinationSize; i++) {
      numerator = numerator * (totalColumns - i);
      denominator = denominator * (maxCombinationSize - i);
    }

    return (numerator / (denominator))
        + getTotalCombos(totalColumns, maxCombinationSize - 1);
  }

  private static Map<String, Integer> sortByValue(
    Map<String, Integer> unsortMap) {

    // 1. Convert Map to List of Map
    List<Map.Entry<String, Integer>> list =
        new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

    // 2. Sort list with Collections.sort(), provide a custom Comparator
    // Try switch the o1 o2 position for a different order
    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
      public int compare(Map.Entry<String, Integer> o1,
        Map.Entry<String, Integer> o2) {
        return (o2.getValue()).compareTo(o1.getValue());
      }
    });

    // 3. Loop the sorted list and put it into a new insertion order Map
    // LinkedHashMap
    Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
    for (Map.Entry<String, Integer> entry : list) {
      sortedMap.put(entry.getKey(), entry.getValue());
    }

    /*
     * //classic iterator example for (Iterator<Map.Entry<String, Integer>> it =
     * list.iterator(); it.hasNext(); ) { Map.Entry<String, Integer> entry =
     * it.next(); sortedMap.put(entry.getKey(), entry.getValue()); }
     */

    return sortedMap;

  }

  private void identifyHighestCounts() {
    // Take the generated headerValueCountMap, and tag each header with its
    // highest count.

    for (String header : headerValueCountMap.keySet()) {

      Map<String, Integer> sortedValueCountMap =
          sortByValue(headerValueCountMap.get(header));

      for (String value : sortedValueCountMap.keySet()) {
        if (!headerHighestCountMap.containsKey(header)) {
          headerHighestCountMap.put(header, sortedValueCountMap.get(value));
        }
      }
    }

  }

  /**
   * The Class AtomContents.
   */
  class NeoplasmRelationship {

    /** The relationship type. */
    private String relationshipType;

    /** The relationship destination. */
    private String relationshipDestination;

    /** The role group. */
    private String roleGroup;

    public String getRelationshipType() {
      return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
      this.relationshipType = relationshipType;
    }

    public String getRelationshipDestination() {
      return relationshipDestination;
    }

    public void setRelationshipDestination(String relationshipDestination) {
      this.relationshipDestination = relationshipDestination;
    }

    public String getRoleGroup() {
      return roleGroup;
    }

    public void setRoleGroup(String roleGroup) {
      this.roleGroup = roleGroup;
    }

  }

}
