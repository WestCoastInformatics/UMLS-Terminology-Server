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
package com.wci.umls.server.mojo;

import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11MatchingRule2 extends AbstractNeoplasmICD11MatchingRule {

  public ICD11MatchingRule2(ContentClientRest client, String st, String sv,
      String tt, String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String executeRule(SctNeoplasmConcept sctCon, Set<String> findingSites,
    int counter) throws Exception {

    matchNextConcept(findingSites, sctCon, counter);
    StringBuffer str = new StringBuffer();

    matchApproach1(findingSites, str);
    matchApproach2(findingSites, str);

    Set<SctNeoplasmConcept> fsConcepts =
        fsUtility.identifyPotentialFSConcepts(findingSites);
    if (fsConcepts != null) {
      matchApproach3(fsConcepts, str);
      matchApproach4(fsConcepts, str);
    }

    return str.toString();
  }

  /**
   * Returns the rule 2 icd 11 concepts.
   *
   * @return the rule 2 icd 11 concepts
   * @throws Exception the exception
   */
  public void identifyIcd11Targets() throws Exception {
    final SearchResultList fullStringResults = client.findConcepts(
        targetTerminology, targetVersion,
        "(atoms.codeId: XH* OR atoms.codeId: 2*) AND \"Melanoma\" AND \"in situ\"",
        pfsLimitless, authToken);

    System.out.println(
        "Have returned : " + fullStringResults.getTotalCount() + " objects");
    int matches = 0;
    for (SearchResult result : fullStringResults.getObjects()) {
      System.out.println(result.getCodeId() + "\t" + result.getValue());
    }

    System.out.println("\n\n\nNow Filtering");
    for (SearchResult result : fullStringResults.getObjects()) {
      if (isRuleMatch(result)) {
        System.out.println(result.getCodeId() + "\t" + result.getValue());
        icd11Targets.getObjects().add(result);
        icd11Targets.setTotalCount(icd11Targets.getTotalCount() + 1);
        matches++;
      }
    }
    System.out.println("Have actually found : " + matches + " matches");
  }

  /**
   * Indicates whether or not neoplasm match is the case.
   *
   * @param result the result
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isRuleMatch(SearchResult result) {
    if ((result.getCodeId().startsWith("XH")
        || result.getCodeId().startsWith("2"))
        && result.getValue().toLowerCase().matches(".*\\bmelanoma\\b.*")
        && result.getValue().toLowerCase().matches(".*\\bin situ\\b.*")
        && result.isLeafNode()) {
      return true;
    }

    return false;
  }

  /**
   * Test rule 1 finding site.
   *
   * @param queryPortion the query portion
   * @return the search result list
   * @throws Exception the exception
   */
  public SearchResultList testMatchingFindingSite(String queryPortion)
    throws Exception {
    if (!alreadyLookedUpTokenCache.containsKey(queryPortion)) {
      final SearchResultList straightMatch = client.findConcepts(
          targetTerminology, targetVersion,
          "(atoms.codeId: XH* OR atoms.codeId: 2*) AND \"Melanoma\" AND \"in situ\" AND "
              + queryPortion,
          pfsLimited, authToken);

      alreadyLookedUpTokenCache.put(queryPortion, straightMatch);
    }

    return alreadyLookedUpTokenCache.get(queryPortion);
  }

  @Override
  public String getEclExpression() {
    return "<< 55342001 : 116676008 = 77986002";
  }

  @Override
  public String getRule() {
    return "rule2";
  }
}