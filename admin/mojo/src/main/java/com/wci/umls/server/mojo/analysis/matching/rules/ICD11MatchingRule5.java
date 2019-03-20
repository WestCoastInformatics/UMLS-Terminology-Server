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
package com.wci.umls.server.mojo.analysis.matching.rules;

import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11MatchingRule5 extends AbstractNeoplasmICD11MatchingRule {

  public ICD11MatchingRule5(ContentClientRest client, String st, String sv,
      String tt, String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }
  
  @Override
  public String getRuleName() {
    return "rule5";
  }
  
  @Override
  protected String getDescription() {
    return "Search: Occurrence = Congential, Pathological process = 'Pathological Development Process', and AssocMorp = Desc of 'Morphologically Abnormal"
        + " Structure'\nNo filtering of ICD11 concepts.\nDo not use finding sites";
  }

  @Override
  public String getEclExpression() {
    return "<< 404684003  : ( 246454002 = 255399007 AND 370135005 = 308490002 AND 116676008 = << 49755003)";
  }

  @Override
  public Map<String, SctNeoplasmConcept> getConceptMap() {
    return null;
  }

  @Override
  public String getDefaultTarget() {
    // TODO
    return null;
  }
  
  @Override
  protected SctNeoplasmConcept getTopLevelConcept() {
    // TODO
    return null;
  }


  /**
   * Execute rule 5
   *
   * @param snomedConcepts the snomed concepts
   * @throws Exception the exception
   */
  @Override
  public String executeRule(SctNeoplasmConcept sctCon, Set<SctNeoplasmConcept> findingSites,
    int counter) throws Exception {

    StringBuffer str = new StringBuffer();
    matchNextConcept(findingSites, sctCon, counter);

    matchApproachBaseSearch(sctCon, str);
    matchApproachBaseMatch(sctCon, str);
/*    matchApproach1(findingSites, str);
    matchApproach2(findingSites, str);

    Set<SctNeoplasmConcept> fsConcepts =
        fsUtility.identifyPotentialFSConcepts(findingSites, devWriter);
    if (fsConcepts != null) {
      matchApproach3(fsConcepts, str);
      matchApproach4(fsConcepts, str);
    }
*/
    return str.toString();
  }
  
  @Override
  public boolean usesFindingSites() {
    return false;
  }

  /**
   * Returns the rule 1 icd 11 concepts.
   *
   * @return the rule 1 icd 11 concepts
   * @throws Exception the exception
   */
  public void identifyIcd11Targets() throws Exception {
    final SearchResultList fullStringResults = client.findConcepts(
        targetTerminology, targetVersion,
        null,
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
  protected boolean isRuleMatch(SearchResult result) {
    if (result.isLeafNode()) {
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
  protected SearchResultList testMatchingFindingSite(String queryPortion)
    throws Exception {
    queryPortion = queryPortion.replaceAll(" - ", " ");
    queryPortion = queryPortion.replaceAll(",", "");
    queryPortion = queryPortion.replaceAll("\\(", "");
    queryPortion = queryPortion.replaceAll("\\)", "");
    if (!findingSiteCache.containsKey(queryPortion)) {
      final SearchResultList straightMatch = client.findConcepts(
          targetTerminology, targetVersion,
          "\"" + queryPortion + "\"",
          pfsLimited, authToken);

      findingSiteCache.put(queryPortion, straightMatch);
    }

    return findingSiteCache.get(queryPortion);
  }
}