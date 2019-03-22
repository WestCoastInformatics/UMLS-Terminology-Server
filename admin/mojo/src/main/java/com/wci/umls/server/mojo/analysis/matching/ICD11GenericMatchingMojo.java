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
package com.wci.umls.server.mojo.analysis.matching;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.mojo.analysis.matching.rules.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.rules.generic.AbstractGenericICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.rules.generic.ICD11GenericMatchingRule1;
import com.wci.umls.server.mojo.analysis.matching.rules.generic.ICD11GenericMatchingRule2;
import com.wci.umls.server.mojo.analysis.matching.rules.generic.ICD11GenericMatchingRule3;
import com.wci.umls.server.mojo.analysis.matching.rules.generic.ICD11GenericMatchingRule4;
import com.wci.umls.server.mojo.analysis.matching.rules.generic.ICD11GenericMatchingRule5;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.ICD11MatcherConceptSearcher;

@Mojo(name = "icd11-generic-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11GenericMatchingMojo extends AbstractICD11MatchingMojo {
  private FindingSiteUtility fsUtility;

  private NeoplasmMatchRules matchingRules;

  protected final String MATCHER_NAME = "icd11-generic-matcher";

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

  protected Set<AbstractICD11MatchingRule> setupProcess() {
    conceptSearcher.setup(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    AbstractGenericICD11MatchingRule.setConceptSearcher(conceptSearcher);

    fsUtility = new FindingSiteUtility(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    fsUtility.setConceptSearcher(conceptSearcher);

    AbstractGenericICD11MatchingRule.setFindingSiteUtility(fsUtility);
    matchingRules = new NeoplasmMatchRules(fsUtility);

    Set<AbstractICD11MatchingRule> rulesToProcess = defineRulesToProcess();

    return rulesToProcess;
  }

  protected void setupContentParsers(AbstractICD11MatchingRule rule) throws IOException {
    setupDescParser();

    // Generic
    boolean populatedFromFiles = false;

    try {
      populatedFromFiles = descParser.readDescsFromFile(rule.getRulePath(MATCHER_NAME));
      populatedFromFiles =
          populatedFromFiles && relParser.readRelsFromFile(rule.getRulePath(MATCHER_NAME));
    } catch (Exception e) {

    }

    ICD11MatcherConceptSearcher.canPopulateFromFiles = populatedFromFiles;
    ICD11MatcherConceptSearcher.setDescParser(descParser);
    ICD11MatcherConceptSearcher.setRelParser(relParser);

  }

  private Set<AbstractICD11MatchingRule> defineRulesToProcess() {
    Set<AbstractICD11MatchingRule> rulesToProcess = new HashSet<>();

    String[] rules = ruleList.split(",");

    for (int i = 0; i < rules.length; i++) {
      AbstractGenericICD11MatchingRule rule = null;
      if (Integer.parseInt(rules[i]) == 1) {
        rule = new ICD11GenericMatchingRule1(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 2) {
        rule = new ICD11GenericMatchingRule2(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 3) {
        rule = new ICD11GenericMatchingRule3(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 4) {
        rule = new ICD11GenericMatchingRule4(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 5) {
        rule = new ICD11GenericMatchingRule5(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      }

      if (rule != null) {
        rulesToProcess.add(rule);
      }
    }

    return rulesToProcess;
  }

  @SuppressWarnings("unchecked")
  protected void postTermProcessing(AbstractICD11MatchingRule rule, ICD11MatcherSctConcept sctCon,
    Object resultObj, List<String> noMatchList, int counter, int totalConcepts)
    throws Exception {
    Set<String> results = (Set<String>)resultObj;
    
    if (results != null && !results.isEmpty()) {

      String resultString = sortByScore(results);

      rule.getDevWriter().println(resultString);
      rule.getDevWriter().println();
      rule.getDevWriter().println();
    } else {
      // No matches
      String outputString =
          "\t" + rule.getDefaultTarget() + "\tNo direct match. Using default target\n\n";
      rule.getDevWriter().println(outputString);

      noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
    }

    if (counter % 10 == 0) {
      rule.getDevWriter().flush();
    }
    if (counter % 25 == 0) {
      System.out.println("Have completed " + counter + " out of " + totalConcepts);
    }

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
          scoreMap.put(score,  new HashSet<String>());
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

  protected String identifyProperResponse(ICD11MatcherSctConcept sctCon,
    AbstractICD11MatchingRule rule, String resultString) throws Exception {

    List<String> results = cleanResultsForTerminologist(resultString);
    System.out.println("\nMatches: ");
    for (String r : results) {
      System.out.println(r);
    }

    Set<ICD11MatcherSctConcept> findingSites =
        ((AbstractGenericICD11MatchingRule) rule).getFindingSiteCons();
    Set<String> findingSiteNames = new HashSet<>();
    for (ICD11MatcherSctConcept con : findingSites) {
      findingSiteNames.add(con.getName());
    }

    /* Multiple results, select one */
    return matchingRules.processAllMatchingRules(results, sctCon, findingSites, findingSiteNames);
  }
}