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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.mojo.analysis.matching.rules.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.AbstractNeoplasmICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.ICD11NeoplasmMatchingRule1;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.ICD11NeoplasmMatchingRule2;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.ICD11NeoplasmMatchingRule4;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.ICD11NeoplasmMatchingRule5;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.ICD11NeoplasmMatchingRule6;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.ICD11MatcherConceptSearcher;

@Mojo(name = "icd11-neoplasm-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11NeoplasmMatchingMojo extends AbstractICD11MatchingMojo {
  private FindingSiteUtility fsUtility;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoFailureException {
    try {
      match("icd11-neoplasm-matcher");

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  protected Set<AbstractICD11MatchingRule> setupProcess() {
    conceptSearcher.setup(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    AbstractNeoplasmICD11MatchingRule.setConceptSearcher(conceptSearcher);

    fsUtility = new FindingSiteUtility(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    fsUtility.setConceptSearcher(conceptSearcher);

    AbstractNeoplasmICD11MatchingRule.setFindingSiteUtility(fsUtility);
    matchingRules = new NeoplasmMatchRules(fsUtility);

    Set<AbstractICD11MatchingRule> rulesToProcess = defineRulesToProcess();

    return rulesToProcess;
  }

  @Override
  protected void setupContentParsers(AbstractICD11MatchingRule rule) throws IOException {
    setupDescParser();

    // Neoplasm
    boolean populatedFromFiles = descParser.readAllNeoplasmDescsFromFile();
    populatedFromFiles = populatedFromFiles && relParser.readAllNeoplasmRelsFromFile();

    // Finding Sites
    populatedFromFiles = populatedFromFiles && descParser.readAllFindingSitesFromFile();
    populatedFromFiles = populatedFromFiles && relParser.readAllFindingSitesFromFile();

    // ECL
    // populatedFromFiles = populatedFromFiles &&
    // descParser.readAllEclFromFile();
    // populatedFromFiles = populatedFromFiles &&
    // relParser.readAllEclFromFile();

    ICD11MatcherConceptSearcher.canPopulateFromFiles = populatedFromFiles;
    ICD11MatcherConceptSearcher.setDescParser(descParser);
    ICD11MatcherConceptSearcher.setRelParser(relParser);

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
      }

      if (rule != null) {
        rulesToProcess.add(rule);
      }
    }

    return rulesToProcess;
  }


  protected void postTermProcessing(AbstractICD11MatchingRule rule, ICD11MatcherSctConcept sctCon,
    Object resultObj, List<String> noMatchList, int counter, int totalConcepts)
    throws Exception {
    String resultString = (String)resultObj;
    
    if (resultString != null && !resultString.isEmpty()) {
      // System.out.println(resultString + "");

      String singleResponse = identifySingleResult(sctCon, rule, resultString);

      if (singleResponse == null) {
        StringBuffer devBuf = new StringBuffer();
        StringBuffer termBuf = new StringBuffer();
        devBuf.append("\tCouldn't discern between the following options");
        termBuf.append("\tCouldn't discern between the following options\n");

        List<String> termResults = cleanResultsForTerminologist(resultString);

        String[] results = resultString.split("\n");
        for (int i = 0; i < results.length; i++) {
          devBuf.append("\t" + results[i] + "\n");
        }

        for (String r : termResults) {
          termBuf.append("\t" + r + "\n");
        }

        devBuf.append("\n");
        termBuf.append("\n");

        System.out.println(devBuf.toString());
        rule.getDevWriter().println(devBuf.toString());
        rule.getTermWriter().println(termBuf.toString());
      } else {
        if (singleResponse.startsWith("\t")) {
          singleResponse = singleResponse.substring(1);
        }
        rule.getDevWriter().println(resultString);
        rule.getDevWriter().println("\tSelected Response: " + singleResponse);
        rule.getDevWriter().println();
        rule.getDevWriter().println();

        System.out.println("\n\nSelected Response: " + singleResponse);
        System.out.println();
        System.out.println();
        
        rule.getTermWriter().println("\t" + singleResponse);
        rule.getTermWriter().println();
        rule.getTermWriter().println();
      }
    } else {
      // No matches
      String outputString =
          "\t" + rule.getDefaultTarget() + "\tNo direct match. Using default target\n\n";
      rule.getDevWriter().println(outputString);
      rule.getTermWriter().println(outputString);

      noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
    }

    if (counter % 10 == 0) {
      rule.getDevWriter().flush();
      rule.getTermWriter().flush();
    }
    if (counter % 25 == 0) {
      System.out.println("Have completed " + counter + " out of " + totalConcepts);
    }

  }

  protected String identifySingleResult(ICD11MatcherSctConcept sctCon, AbstractICD11MatchingRule rule,
    String resultString) throws Exception {

    List<String> results = cleanResultsForTerminologist(resultString);
    System.out.println("\nMatches: ");
    for (String r : results) {
      System.out.println(r);
    }

    Set<ICD11MatcherSctConcept> findingSites =
        ((AbstractNeoplasmICD11MatchingRule) rule).getFindingSiteCons();
    Set<String> findingSiteNames = new HashSet<>();
    for (ICD11MatcherSctConcept con : findingSites) {
      findingSiteNames.add(con.getName());
    }

    /* Multiple results, select one */
    return matchingRules.processAllMatchingRules(results, sctCon, findingSites, findingSiteNames);
  }
}