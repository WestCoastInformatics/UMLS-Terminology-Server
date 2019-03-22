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

import java.util.Map;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11GenericMatchingRule1 extends AbstractGenericICD11MatchingRule {

  public ICD11GenericMatchingRule1(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleName() {
    return "rule1";
  }

  @Override
  public String getDescription() {
    return "ECL Based: All descendents of 'Human immunodeficiency virus infection' connecting them to the ICD11 'Human immunodeficiency virus disease' i.e. andything starting with '1C6*', contains 'HIV', or contains 'Human immunodeficiency virus'";
  }

  @Override
  public String getEclExpression() {
    return "<< 86406008";
  }

  @Override
  public Map<String, ICD11MatcherSctConcept> getConceptMap() {
    return null;
  }

  @Override
  public String getDefaultTarget() {
    return "2E6Y\tCarcinoma in situ of other specified site";
  }

  @Override
  protected ICD11MatcherSctConcept getTopLevelConcept() {
    return conceptSearcher.getSctConcept("86406008");
  }

  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 1C6*) OR (\"hiv\" OR \"human immunodeficiency virus\")";
  }

  @Override
  protected boolean printIcd11Targets() {
    return true;
  }

  /**
   * Indicates whether or not generic match is the case.
   *
   * @param result the result
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  protected boolean isRuleMatch(SearchResult result) {
    if ((result.getCodeId().startsWith("1C6")
        || result.getValue().toLowerCase().matches(".*\\bhiv\\b.*")
        || result.getValue().toLowerCase().matches(".*\\bhuman immunodeficiency virus\\b.*"))
        && !result.getCodeId().startsWith("X")
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
          getRuleQueryString() + queryPortion, pfsLimited, authToken);

      findingSiteCache.put(queryPortion, straightMatch);
    }

    return findingSiteCache.get(queryPortion);
  }
}