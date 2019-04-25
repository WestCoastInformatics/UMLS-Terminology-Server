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
package com.wci.umls.server.mojo.analysis.matching.rules.neoplasm;

import java.util.HashMap;
import java.util.Map;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11NeoplasmMatchingRule4 extends AbstractNeoplasmICD11MatchingRule {

  public ICD11NeoplasmMatchingRule4(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleId() {
    return "rule4";
  }

  @Override
  public String getDescription() {
    return "Description Based: All descendents of 'Neoplastic disease' that contains a 'neoplasm' synonym and has some sort of 'uncertain behavior'";
  }

  @Override
  public String getEclExpression() {
    return null;
  }

  @Override
  public Map<String, ICD11MatcherSctConcept> getConceptMap() {
    Map<String, ICD11MatcherSctConcept> retMap = new HashMap<>();

    for (ICD11MatcherSctConcept con : conceptSearcher.getAllNeoplasmConcepts()) {
      for (SctNeoplasmDescription desc : con.getDescs()) {
        if (desc.getNeoplasmSynonym().toLowerCase().equals("neoplasm")
            && !desc.getUncertainty().isEmpty()) {
          retMap.put(con.getConceptId(), con);
          break;
        }
      }
    }

    return retMap;
  }

  @Override
  public String getDefaultTarget() {
    return "2F7Y\tNeoplasms of uncertain behaviour of other specified site";
  }

  @Override
  protected ICD11MatcherSctConcept getTopLevelConcept() {
    return conceptSearcher.getSctConcept("55342001");
  }

  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 2* OR (\"neopl\" AND ((\"uncertain\" AND \"behavi\") OR (\"uncertain\" AND \"behavi\"))))";
  }

  @Override
  public String getDefaultSkinMatch() {
    return "2F72.Y";
  }

  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if (!result.getCodeId().startsWith("X")
        && result.getValue().toLowerCase().matches(".*\\bneopl.*")
        && (result.getValue().toLowerCase().matches(".*\\buncertain\\b.*")
            || result.getValue().toLowerCase().matches(".*\\bunknown\\b.*"))
        && result.getValue().toLowerCase().matches(".*\\bbehavi.*")
        && !result.getTerminologyId().equals("2F7Y") && !result.getTerminologyId().equals("2F7Z")
        && result.isLeafNode()) {
      return true;
    }

    return false;
  }
}