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
package com.wci.umls.server.mojo.analysis.matching.rules.neoplasm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingMojo;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatcherConstants;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;

@Mojo(name = "icd11-neoplasm-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11NeoplasmMatchingMojo extends AbstractICD11MatchingMojo {
  private static final String MATCHER_NAME = "icd11-neoplasm-matcher";
  private FindingSiteUtility fsUtility;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoFailureException {
    try {
      match(MATCHER_NAME);

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  protected Set<AbstractICD11MatchingRule> setupProcess() throws IOException {
    conceptSearcher.setup(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    AbstractNeoplasmICD11MatchingRule.setConceptSearcher(conceptSearcher);
    AbstractNeoplasmICD11MatchingRule.initializeTcTable();

    fsUtility = new FindingSiteUtility(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    fsUtility.setConceptSearcher(conceptSearcher);

    AbstractNeoplasmICD11MatchingRule.setFindingSiteUtility(fsUtility);
    matchingRules = new NeoplasmMatchRules(fsUtility, getDepthLocation());

    Set<AbstractICD11MatchingRule> rulesToProcess = defineRulesToProcess();

    return rulesToProcess;
  }

  private Set<AbstractICD11MatchingRule> defineRulesToProcess() {
    Set<AbstractICD11MatchingRule> rulesToProcess = new HashSet<>();

    String[] rules = ruleList.split(",");

    for (int i = 0; i < rules.length; i++) {
      AbstractNeoplasmICD11MatchingRule rule = null;
      if (Integer.parseInt(rules[i]) == 1) {
        rule = new ICD11NeoplasmMatchingRule1(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 2) {
        rule = new ICD11NeoplasmMatchingRule2(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 4) {
        rule = new ICD11NeoplasmMatchingRule4(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 5) {
        rule = new ICD11NeoplasmMatchingRule5(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 6) {
        rule = new ICD11NeoplasmMatchingRule6(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 7) {
        rule = new ICD11NeoplasmMatchingRule7(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      }

      if (rule != null) {
        rulesToProcess.add(rule);
      }
    }

    return rulesToProcess;
  }

  /**
   * Clean results for terminologist.
   * 
   * Only return one line per code and make that line the one with the lowest
   * depth
   * 
   * @param results the str
   * @return the string buffer
   */
  protected List<String> cleanResultsForTerminologist(Set<String> results) {
    Map<String, Set<String>> sortedIdsMap = new TreeMap<>();
    // Sort lines by code
    for (String line : results) {
      String[] columns = line.split("\t");

      if (columns.length > getDepthLocation()) {
        if (!sortedIdsMap.containsKey(columns[1])) {
          sortedIdsMap.put(columns[1], new HashSet<String>());
        }

        sortedIdsMap.get(columns[1]).add(line);
      }
    }

    List<String> linesToPrint = new ArrayList<>();
    for (String conId : sortedIdsMap.keySet()) {
      String lineToPrint = null;
      int lowestDepth = 1000;
      for (String line : sortedIdsMap.get(conId)) {
        String[] columns = line.split("\t");

        if (Integer.parseInt(columns[getDepthLocation()]) < lowestDepth) {
          lowestDepth = Integer.parseInt(columns[getDepthLocation()]);
          
          lineToPrint =
              "\t" + columns[1] + "\t" + columns[2] + "\t" + columns[3] + "\t" + columns[4] + "\t" + columns[5];
          
          if (getDepthLocation() == 6) {
            lineToPrint = lineToPrint + "\t" + columns[6];
          }
          
          lineToPrint = lineToPrint + "\n";
        }
      }

      linesToPrint.add(lineToPrint.trim());
    }

    return linesToPrint;
  }

  @Override
  protected String identifySingleResult(ICD11MatcherSctConcept sctCon, AbstractICD11MatchingRule rule,
    Set<String> results) throws Exception {

    List<String> cleanedResults = cleanResultsForTerminologist(results);
    System.out.println("\nMatches: ");
    for (String r : cleanedResults) {
      System.out.println(r);
    }

    Set<ICD11MatcherSctConcept> findingSites =
        ((AbstractNeoplasmICD11MatchingRule) rule).getFindingSiteCons();
    Set<String> findingSiteNames = new HashSet<>();
    for (ICD11MatcherSctConcept con : findingSites) {
      findingSiteNames.add(con.getName());
    }

    /* Multiple cleanedResults, select one */
    return matchingRules.processAllMatchingRules(cleanedResults, sctCon, findingSites, findingSiteNames);
  }

  @Override
  public int getDepthLocation() {
    return  ICD11MatcherConstants.DEPTH_LOCATION_NEOPLASM;
  }
}