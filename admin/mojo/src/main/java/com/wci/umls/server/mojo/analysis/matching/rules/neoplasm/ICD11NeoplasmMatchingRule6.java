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

import java.util.Map;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11NeoplasmMatchingRule6 extends AbstractNeoplasmICD11MatchingRule {

  public ICD11NeoplasmMatchingRule6(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleName() {
    return "rule6";
  }

  @Override
  public String getDescription() {
    return "Description Based: All descendents of 'Malignant neoplastic disease'";
  }

  @Override
  public String getEclExpression() {
    return "<< 363346000";
  }

  @Override
  public Map<String, ICD11MatcherSctConcept> getConceptMap() {
    return null;
  }

  @Override
  public String getDefaultTarget() {
    return "2D4Z";
  }

  @Override
  protected ICD11MatcherSctConcept getTopLevelConcept() {
    return conceptSearcher.getSctConcept("363346000");
  }
  
  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 2* OR (\"malignant\" AND \"neopla\"))";
  }

  @Override
  protected boolean printIcd11Targets() {
    return true;
  }

  @Override
  public String getDefaultSkinMatch() {
    return "2C3Y";
  }
  
  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if (!result.getCodeId().startsWith("X")
        && result.getValue().toLowerCase().matches(".*\\bneopl.*")
        && result.getValue().toLowerCase().matches(".*\\bmalignant\\b.*")
        && !result.getTerminologyId().equals("2D4Z") 
        && result.isLeafNode()) {
      return true;
    }

    return false;
  }
}