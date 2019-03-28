package com.wci.umls.server.mojo.analysis.matching.rules.reverse;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11ReverseDescendantRule4 extends AbstractReverseDescendantICD11MatchingRule {
  public ICD11ReverseDescendantRule4(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleId() {
    return "rule4";
  }

  @Override
  public String getDescription() {
    return "Reverse Rule for Viral infections of the central nervous system";
  }

  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 1C8*)";
  }

  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if (result.getCodeId().startsWith("1C8") && result.isLeafNode()) {
      return true;
    }

    return false;
  }
}
