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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingMojo;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatcherConstants;
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

        int icd11Counter = 0;
        Map<ReverseMatchMapKey, Set<ReverseMatchMapTarget>> matchMaps = new HashMap<>();

        // Process Terms
        for (SearchResult icd11Con : icd11Concepts.getObjects()) {

          // if (counter >= 15) { break; }

          ICD11MatcherSctConcept ancestorSctCon =
              ((AbstractReverseDescendantICD11MatchingRule) rule)
                  .identifyEquivalentSctCon(icd11Con);

          if (ancestorSctCon != null) {
            // Return SctDesc via ECL
            pfsEcl.setExpression("<< " + ancestorSctCon.getConceptId());
            final SearchResultList descendents =
                client.findConcepts(sourceTerminology, sourceVersion, "", pfsEcl, authToken);
            pfsEcl.setExpression(null);

            for (SearchResult sctCon : descendents.getObjects()) {
              ReverseMatchMapKey mapKey = new ReverseMatchMapKey(sctCon);

              if (!matchMaps.containsKey(mapKey)) {
                matchMaps.put(mapKey, new HashSet<ReverseMatchMapTarget>());
              }

              matchMaps.get(mapKey).add(new ReverseMatchMapTarget(ancestorSctCon, icd11Con));
            }

          } else {
            noMatchList.add(icd11Con.getValue());
          }

          if (++icd11Counter == 1 || icd11Counter % 25 == 0) {
            getLog().info("Processed ICD11 Concept #" + icd11Counter + " out of a total of "
                + icd11Concepts.getTotalCount());
          }
        }

        printOutResults(rule, matchMaps);

        postRuleProcessing(rule, noMatchList);
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  private void printOutResults(AbstractICD11MatchingRule rule,
    Map<ReverseMatchMapKey, Set<ReverseMatchMapTarget>> matchMaps) {
    int sctCounter = 0;

    for (ReverseMatchMapKey sctCon : matchMaps.keySet()) {
      printResultHeader(rule, sctCon, matchMaps.get(sctCon), ++sctCounter);
      printResultMaps(rule, matchMaps.get(sctCon));

      rule.getDevWriter().println();
      rule.getDevWriter().println();
      rule.getTermWriter().println();
      rule.getTermWriter().println();

      rule.getDevWriter().flush();
      rule.getTermWriter().flush();
    }
  }

  private void printResultMaps(AbstractICD11MatchingRule rule,
    Set<ReverseMatchMapTarget> matchData) {
    HashSet<String> responses = new HashSet<>();
    for (ReverseMatchMapTarget match : matchData) {
      String response = "\t" + match.getIcd11Target().getCodeId() + "\t"
          + match.getIcd11Target().getValue() + match.getIcd11Target().getTerminologyId() + "\t"
          + "\tSct Ancestor: " + match.getAncestorSctCon().getName() + "\tAncestor SctId: "
          + match.getAncestorSctCon().getConceptId();

      responses.add(response);
    }

    for (String response : responses) {
      rule.getDevWriter().println(response);
      rule.getTermWriter().println(response);
    }
  }

  private void printResultHeader(AbstractICD11MatchingRule rule, ReverseMatchMapKey sctCon,
    Set<ReverseMatchMapTarget> matchData, int counter) {
    String newConInfo = "\n# " + counter + " Snomed Concept: " + sctCon.getSnomedDesc()
        + "\tSctId: " + sctCon.getSnomedId() + "\t";

    if (matchData.size() == 1) {
      newConInfo = newConInfo + "Single Result";
    } else {
      newConInfo = newConInfo + "Couldn't discern between the following options";
    }

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
    rule.setDevWriter(prepareResultsFile(rule, ICD11MatcherConstants.PRINT_WRITER_DEV_TYPE,
        "ICD11 Matching Results"));
    rule.setTermWriter(prepareResultsFile(rule, ICD11MatcherConstants.PRINT_WRITER_TERM_TYPE,
        "ICD11 Matching Results"));
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

  private class ReverseMatchMapTarget {
    private ICD11MatcherSctConcept ancestorSctCon;

    private SearchResult icd11Target;

    private ReverseMatchMapTarget(ICD11MatcherSctConcept sctCon, SearchResult icd11Con) {
      ancestorSctCon = sctCon;
      icd11Target = icd11Con;
    }

    public ICD11MatcherSctConcept getAncestorSctCon() {
      return ancestorSctCon;
    }

    public SearchResult getIcd11Target() {
      return icd11Target;
    }
  }

  private class ReverseMatchMapKey {
    private String sctId;

    private String desc;

    private ReverseMatchMapKey(SearchResult result) {
      sctId = result.getTerminologyId();
      desc = result.getValue();
    }

    public String getSnomedId() {
      return sctId;
    }

    public String getSnomedDesc() {
      return desc;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((sctId == null) ? 0 : sctId.hashCode());
      result = prime * result + ((desc == null) ? 0 : desc.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      ReverseMatchMapKey other = (ReverseMatchMapKey) obj;
      if (sctId == null) {
        if (other.sctId != null)
          return false;
      } else if (!sctId.equals(other.sctId))
        return false;
      if (desc == null) {
        if (other.desc != null)
          return false;
      } else if (!desc.equals(other.desc))
        return false;

      return true;
    }
  }
}