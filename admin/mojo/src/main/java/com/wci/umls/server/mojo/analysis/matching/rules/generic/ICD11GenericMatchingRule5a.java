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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatcherConstants;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11GenericMatchingRule5a extends AbstractGenericICD11MatchingRule {

  public ICD11GenericMatchingRule5a(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleId() {
    return "rule5a";
  }

  @Override
  public String getRuleName() {
    return "Arbovirus Infections";
  }

  @Override
  public String getDescription() {
    return "ECL Based: All descendents of 'Arbovirus infection' connecting them to the ICD11 'Certain arthropod-borne viral fevers' i.e. any code starting with '1D4'";
  }

  @Override
  public String getEclExpression() {
    return "<< 40610006";
  }

  @Override
  public Map<String, ICD11MatcherSctConcept> getConceptMap() {
    return null;
  }

  @Override
  public String getDefaultTarget() {
    return "1D4Y\tOther specified arthropod-borne viral fevers\t921595235/other";
  }

  @Override
  protected ICD11MatcherSctConcept getTopLevelConcept() {
    return conceptSearcher.getSctConcept("86406008");
  }

  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 1D4*)";
  }

  @Override
  protected boolean printIcd11Targets() {
    return true;
  }

  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if (result.getCodeId().startsWith("1D4") && result.isLeafNode()) {
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
          getRuleQueryString() + queryPortion, pfsLimited, authToken);

      findingSiteCache.put(queryPortion, straightMatch);
    }

    return findingSiteCache.get(queryPortion);
  }

  @Override
  public Set<String> executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception {

    Set<String> results = new HashSet<>();
    matchNextConcept(sctCon, counter);

    results = matchApproachBaseMatch(sctCon, results, icd11Targets,
        ICD11MatcherConstants.FILTERED_RULE_TYPE);

    if (results.isEmpty()) {
      results = matchApproachBaseSearch(sctCon, results, icd11Targets,
          ICD11MatcherConstants.FILTERED_RULE_TYPE);
    }

    return results;
  }
}