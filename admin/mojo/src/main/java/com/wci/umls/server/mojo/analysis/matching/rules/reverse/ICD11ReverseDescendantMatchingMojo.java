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
package com.wci.umls.server.mojo.analysis.matching.rules.reverse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingMojo;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;

@Mojo(name = "icd11-reverse-descendant-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11ReverseDescendantMatchingMojo extends AbstractICD11MatchingMojo {
  protected final String MATCHER_NAME = "icd11-reverse-descendant-matcher";

  final static protected SctRelationshipParser relParser = new SctRelationshipParser();

  @Override
  public void execute() throws MojoFailureException {
    try {

      setup(MATCHER_NAME, st, sv, tt, tv);
      getLog().info("  maxCount =  " + maxCount);

      /*
       * Error Checking
       */
      if (sourceTerminology == null || sourceTerminology.isEmpty() || targetTerminology == null
          || targetTerminology.isEmpty()) {
        throw new Exception(
            "Must define a source and target terminology to search against i.e. SNOMEDCT");
      }
      if (sourceVersion == null || sourceVersion.isEmpty() || targetVersion == null
          || targetVersion.isEmpty()) {
        throw new Exception(
            "Must define a source and target version to search against i.e. latest");
      }

      Set<AbstractICD11MatchingRule> rulesToProcess = setupProcess();

      for (AbstractICD11MatchingRule rule : rulesToProcess) {
        SearchResultList icd11Concepts = preReverseRuleProcessing(rule);
        List<String> noMatchList = new ArrayList<>();

        int sctCounter = 0;
        int icd11Counter = 0;

        // Process Terms
        for (SearchResult icd11Con : icd11Concepts.getObjects()) {

          // if (counter >= 15) { break; }
          ICD11MatcherSctConcept ancestorSctCon =
              ((AbstractReverseDescendantICD11MatchingRule) rule)
                  .identifyEquivalentSctCon(icd11Con);

          getLog().info("Processing ICD11 Concept #" + ++icd11Counter + " out of a total of " + icd11Concepts.getTotalCount());
          
          if (ancestorSctCon != null) {
            // Return SctDesc via ECL
            pfsEcl.setExpression("<< " + ancestorSctCon.getConceptId());
            final SearchResultList descendents =
                client.findConcepts(sourceTerminology, sourceVersion, "", pfsEcl, authToken);
            pfsEcl.setExpression(null);

            sctCounter = postReverseTermProcessing(rule, icd11Con, ancestorSctCon, descendents, noMatchList, sctCounter);
          } else {
            noMatchList.add(icd11Con.getValue());
          }
        }

        postRuleProcessing(rule, noMatchList);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  private int postReverseTermProcessing(AbstractICD11MatchingRule rule, SearchResult icd11Con,
    ICD11MatcherSctConcept ancestorSctCon, SearchResultList descendants, List<String> noMatchList, int counter) {
    for (SearchResult sctCon : descendants.getObjects()) {

      matchNextConcept(rule, ancestorSctCon, sctCon, ++counter);

      String singleResponse = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\tSct Ancestor: " + ancestorSctCon.getName();
      HashSet<String> responses = new HashSet<>();
      responses.add(singleResponse);
      
      printWithSingleResponse(singleResponse, responses, rule);
      
      rule.getDevWriter().flush();
      rule.getTermWriter().flush();
    }

    return counter;
  }

  private void matchNextConcept(AbstractICD11MatchingRule rule, ICD11MatcherSctConcept ancestorSctCon, SearchResult sctCon, int counter) {
    String newConInfo = "\n# " + counter + " Snomed Concept: " + sctCon.getValue() + "\tSctId: "
        + sctCon.getTerminologyId() + "\tbased on ancestor: " + ancestorSctCon.getConceptId();

    System.out.println(newConInfo);
    rule.getDevWriter().println(newConInfo);
    rule.getTermWriter().println(newConInfo);
  }

  private SearchResultList preReverseRuleProcessing(AbstractICD11MatchingRule rule)
    throws Exception {
    getLog().info("\n\n\n**************************\nNow Processing\n" + rule.getRuleId()
        + " Matching Mojo\n" + rule.getDescription() + "\n**************************\n");
  
    setupContentParsers(rule);

    /*
     * Start Processing rule
     */
    rule.setDevWriter(
        prepareResultsFile(rule.getRuleId(), "developerResults", "ICD11 Matching Results"));
    rule.setTermWriter(
        prepareResultsFile(rule.getRuleId(), "terminologistResults", "ICD11 Matching Results"));
    rule.identifyIcd11Targets();

    return rule.getIcd11Concepts();
  }

  protected Set<AbstractICD11MatchingRule> setupProcess() {
    conceptSearcher.setup(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    AbstractReverseDescendantICD11MatchingRule.setConceptSearcher(conceptSearcher);

    Set<AbstractICD11MatchingRule> rulesToProcess = defineRulesToProcess();

    return rulesToProcess;
  }

  private Set<AbstractICD11MatchingRule> defineRulesToProcess() {
    Set<AbstractICD11MatchingRule> rulesToProcess = new HashSet<>();

    String[] rules = ruleList.split(",");

    for (int i = 0; i < rules.length; i++) {
      AbstractICD11MatchingRule rule = null;
      if (Integer.parseInt(rules[i]) == 1) {
        rule = new ICD11ReverseDescendantRule1(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 2) {
        rule = new ICD11ReverseDescendantRule2(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 3) {
        rule = new ICD11ReverseDescendantRule3(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 4) {
        rule = new ICD11ReverseDescendantRule4(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 5) {
        rule = new ICD11ReverseDescendantRule5(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 99) {
        rule = new ICD11ReverseDescendantRuleAll(client, sourceTerminology, sourceVersion,
            targetTerminology, targetVersion, authToken);
      }

      if (rule != null) {
        rulesToProcess.add(rule);
      }
    }

    return rulesToProcess;
  }

  @Override
  protected String identifySingleResult(ICD11MatcherSctConcept sctCon,
    AbstractICD11MatchingRule rule, Set<String> results) throws Exception {
    throw new UnsupportedOperationException();
  }

  @Override
  protected int getDepthLocation() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected List<String> cleanResultsForTerminologist(Set<String> results) {
    throw new UnsupportedOperationException();
  }
}