package com.wci.umls.server.mojo.analysis.matching.rules.reverse;

import java.util.HashSet;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11ReverseDescendantRule1 extends AbstractReverseDescendantICD11MatchingRule {
  public ICD11ReverseDescendantRule1(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleId() {
    return "rule1";
  }

  @Override
  public String getDescription() {
    return "Reverse Rule for Human immunodeficiency virus disease";
  }

  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 1C6*)";
  }

  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if (result.getCodeId().startsWith("1C6")
        && result.isLeafNode()) {
      return true;
    }

    return false;
  }

  @Override
  protected Set<String> getRuleBasedNonMatchTerms() {
    Set<String> retSet = new HashSet<>();
    
    retSet.add("hiv");
    retSet.add("human immunodeficiency virus");
    retSet.add("infection");

    return retSet;
  }
}
