package com.wci.umls.server.mojo.analysis.matching;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;

public abstract class AbstractMatchRules {

  protected FindingSiteUtility fsUtility;

  protected Map<Integer, List<String>> resultTypeMap = new HashMap<>();

  protected AbstractICD11MatchingRule rule;

  protected static final int RETURN_SPECIFIED = 0;

  protected static final int RETURN_UNSPECIFIED = 1;

  protected static final Integer OTHER_OR_UNSPECIFIED = 0;

  protected static final Integer OTHER_SPECIFIED = 1;

  protected static final Integer UNSPECIFIED = 2;

  protected static final Integer NOT_UNSPECIFIED = 3;

  protected static HashSet<String> matchRulesSpecificNonMatchingTerms =
      new HashSet<>(ICD11MatchingConstants.NON_MATCHING_TERMS);

  public abstract String processAllMatchingRules(List<String> results, ICD11MatcherSctConcept sctCon,
    Set<ICD11MatcherSctConcept> findingSites, Set<String> findingSiteNames) throws Exception;
  
  static {
    matchRulesSpecificNonMatchingTerms.add("other");
    matchRulesSpecificNonMatchingTerms.add("specified");
    matchRulesSpecificNonMatchingTerms.add("unspecified");
  }
  

  public void setRule(AbstractICD11MatchingRule rule) {
    this.rule = rule;
  }


}
