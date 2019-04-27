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
package com.wci.umls.server.mojo.analysis.matching.rules.generic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

@Mojo(name = "icd11-generic-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11GenericMatchingMojo extends AbstractICD11MatchingMojo {
  private FindingSiteUtility fsUtility;

  protected final String MATCHER_NAME = "icd11-generic-matcher";

  private final int SCORE_COLUMN = 5;

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
    AbstractGenericICD11MatchingRule.setConceptSearcher(conceptSearcher);
    // AbstractNeoplasmICD11MatchingRule.initializeTcTable();

    fsUtility = new FindingSiteUtility(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    fsUtility.setConceptSearcher(conceptSearcher);

    AbstractGenericICD11MatchingRule.setFindingSiteUtility(fsUtility);
    matchingRules = new GenericMatchRules(fsUtility);

    Set<AbstractICD11MatchingRule> rulesToProcess = defineRulesToProcess();

    return rulesToProcess;
  }

  private Set<AbstractICD11MatchingRule> defineRulesToProcess() {
    Set<AbstractICD11MatchingRule> rulesToProcess = new HashSet<>();

    String[] rules = ruleList.split(",");

    for (int i = 0; i < rules.length; i++) {
      AbstractGenericICD11MatchingRule rule = null;
      if (Integer.parseInt(rules[i]) == 1) {
        rule = new ICD11GenericMatchingRule1a(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 2) {
        rule = new ICD11GenericMatchingRule2a(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 3) {
        rule = new ICD11GenericMatchingRule3a(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 4) {
        rule = new ICD11GenericMatchingRule4a(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 5) {
        rule = new ICD11GenericMatchingRule5a(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      }

      if (rule != null) {
        rulesToProcess.add(rule);
      }
    }

    return rulesToProcess;
  }

  private String sortByScore(Set<String> results) {
    StringBuffer retStr = new StringBuffer();
    Map<Float, Set<String>> scoreMap = new TreeMap<>(Collections.reverseOrder());

    // Sort lines by code
    for (String line : results) {
      String[] columns = line.split("\t");

      if (columns.length > 3) {
        float score;

        if (columns[4].equals("N/A")) {
          score = 99;
        } else {
          score = Float.valueOf(columns[4]);
        }
        if (!scoreMap.keySet().contains(score)) {
          scoreMap.put(score, new HashSet<String>());
        }
        scoreMap.get(score).add(line);
      }
    }

    Set<String> observedMatches = new HashSet<>();
    for (Float score : scoreMap.keySet()) {
      for (String l : scoreMap.get(score)) {
        String icd11Code = l.split("\t")[1];

        if (score == 99) {
          retStr.append(l);
        } else {
          if (!observedMatches.contains(icd11Code)) {
            observedMatches.add(icd11Code);
            retStr.append(l);
          }
        }
      }
    }

    return retStr.toString();
  }

  protected String identifySingleResult(ICD11MatcherSctConcept sctCon,
    AbstractICD11MatchingRule rule, Set<String> results) throws Exception {

    List<String> cleanedResults = cleanResultsForTerminologist(results);
    System.out.println("\nMatches: ");
    for (String r : cleanedResults) {
      System.out.println(r);
    }

    Set<ICD11MatcherSctConcept> findingSites =
        ((AbstractGenericICD11MatchingRule) rule).getFindingSiteCons();
    Set<String> findingSiteNames = new HashSet<>();
    for (ICD11MatcherSctConcept con : findingSites) {
      findingSiteNames.add(con.getName());
    }

    /* Multiple cleanedResults, select one */
    return matchingRules.processAllMatchingRules(cleanedResults, sctCon, findingSites,
        findingSiteNames);
  }

  @Override
  protected int getDepthLocation() {
    return ICD11MatcherConstants.DEPTH_LOCATION_GENERIC;
  }

  @Override
  protected List<String> cleanResultsForTerminologist(Set<String> results) {
    Map<Float, Set<String>> scoreMap = new TreeMap<>(Collections.reverseOrder());

    // Sort lines by score
    for (String line : results) {
      String[] columns = line.split("\t");

      if (columns.length > SCORE_COLUMN - 1) {
        float score;

        if (columns[SCORE_COLUMN].equals("N/A")) {
          score = 99;
        } else {
          score = Float.valueOf(columns[SCORE_COLUMN]);
        }
        if (!scoreMap.keySet().contains(score)) {
          scoreMap.put(score, new HashSet<String>());
        }
        scoreMap.get(score).add(line);
      }
    }
    List<String> retList = new ArrayList<>();
    Set<String> observedMatches = new HashSet<>();
    for (Float score : scoreMap.keySet()) {
      for (String l : scoreMap.get(score)) {
        String icd11Code = l.split("\t")[1];

        if (score == 99) {
          retList.add(l);
        } else {
          if (!observedMatches.contains(icd11Code)) {
            observedMatches.add(icd11Code);
            retList.add(l);
          }
        }
      }
    }

    return retList;
  }
}