package com.wci.umls.server.mojo.analysis.matching.rules.reverse;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11ReverseDescendantRuleAll extends AbstractReverseDescendantICD11MatchingRule {
  public ICD11ReverseDescendantRuleAll(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleId() {
    return "ruleAll";
  }

  @Override
  public String getDescription() {
    return "Reverse Rule for All ICD11 Concepts";
  }

  @Override
  protected String getRuleQueryString() {
    return "";
  }

  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if (!result.getCodeId().startsWith("V") && !result.getCodeId().startsWith("X")
        && result.isLeafNode()) {
      return true;
    }

    return false;
  }

  @Override
  protected boolean printIcd11Targets() {
    return false;
  }
}
