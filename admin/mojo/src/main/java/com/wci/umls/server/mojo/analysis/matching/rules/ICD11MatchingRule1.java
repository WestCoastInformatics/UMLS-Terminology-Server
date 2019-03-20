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

public class ICD11MatchingRule1 extends AbstractNeoplasmICD11MatchingRule {

  public ICD11MatchingRule1(ContentClientRest client, String st, String sv, String tt, String tv,
      String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleName() {
    return "rule1";
  }

  @Override
  protected String getDescription() {
    return "ECL Based: All descendents of 'Neoplastic disease' where the assocMorph is 'Carcinoma in situ'";
  }

  @Override
  public String getEclExpression() {
    return "<< 55342001 : 116676008 = 399919001";
  }

  @Override
  public Map<String, SctNeoplasmConcept> getConceptMap() {
    return null;
  }

  @Override
  public String getDefaultTarget() {
    return "2E6Y\tCarcinoma in situ of other specified site";
  }

  @Override
  protected SctNeoplasmConcept getTopLevelConcept() {
    return conceptSearcher.getSctConcept("55342001");
  }

  /**
   * Execute rule 1.
   *
   * @param snomedConcepts the snomed concepts
   * @throws Exception the exception
   */
  @Override
  public String executeRule(SctNeoplasmConcept sctCon, Set<SctNeoplasmConcept> findingSites, int counter)
    throws Exception {

    StringBuffer str = new StringBuffer();
    matchNextConcept(findingSites, sctCon, counter);

    matchApproach1(findingSites, str);
    matchApproach2(findingSites, str);

    Set<SctNeoplasmConcept> ancestorFindingSites =
        fsUtility.identifyPotentialFSConcepts(findingSites, devWriter);
    
    if (ancestorFindingSites != null) {
      matchApproach3(ancestorFindingSites, str);
      matchApproach4(ancestorFindingSites, str);
    }

    return str.toString();
  }

  /**
   * Returns the rule 1 icd 11 concepts.
   *
   * @return the rule 1 icd 11 concepts
   * @throws Exception the exception
   */
  public void identifyIcd11Targets() throws Exception {
    final SearchResultList fullStringResults = client.findConcepts(targetTerminology, targetVersion,
        "(atoms.codeId: XH* OR atoms.codeId: 2*) AND \"Carcinoma\" AND \"in situ\"", pfsLimitless,
        authToken);

    System.out.println("Have returned : " + fullStringResults.getTotalCount() + " objects");
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
    if ((result.getCodeId().startsWith("XH") || result.getCodeId().startsWith("2"))
        && result.getValue().toLowerCase().matches(".*\\bcarcinoma\\b.*")
        && result.getValue().toLowerCase().matches(".*\\bin situ\\b.*")
        && !result.getTerminologyId().equals("2E6Y")
        && !result.getTerminologyId().equals("2E6Z")
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
  protected SearchResultList testMatchingFindingSite(String queryPortion) throws Exception {
    if (!findingSiteCache.containsKey(queryPortion)) {
      final SearchResultList straightMatch = client.findConcepts(targetTerminology, targetVersion,
          "(atoms.codeId: XH* OR atoms.codeId: 2*) AND \"Carcinoma\" AND \"in situ\" AND "
              + queryPortion,
          pfsLimited, authToken);

      findingSiteCache.put(queryPortion, straightMatch);
    }

    return findingSiteCache.get(queryPortion);
  }
}