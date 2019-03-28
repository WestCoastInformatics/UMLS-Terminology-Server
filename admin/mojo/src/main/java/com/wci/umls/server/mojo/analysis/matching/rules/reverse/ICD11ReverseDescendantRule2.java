package com.wci.umls.server.mojo.analysis.matching.rules.reverse;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.rest.client.ContentClientRest;

public class ICD11ReverseDescendantRule2 extends AbstractReverseDescendantICD11MatchingRule {
  public ICD11ReverseDescendantRule2(ContentClientRest client, String st, String sv, String tt,
      String tv, String authToken) {
    super(client, st, sv, tt, tv, authToken);
  }

  @Override
  public String getRuleId() {
    return "rule2";
  }

  @Override
  public String getDescription() {
    return "Reverse Rule for Mycobacterial diseases";
  }

  @Override
  protected String getRuleQueryString() {
    return "(atoms.codeId: 1B1* OR atoms.codeId: 1B2*)";
  }

  @Override
  protected boolean isRuleMatch(SearchResult result) {
    if ((result.getCodeId().startsWith("1B1") || result.getCodeId().startsWith("1B2"))
        && result.isLeafNode()) {
      return true;
    }

    return false;
  }

}
