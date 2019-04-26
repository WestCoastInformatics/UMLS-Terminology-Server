package com.wci.umls.server.mojo.analysis.matching.rules.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatcherConstants;
import com.wci.umls.server.mojo.analysis.matching.SctICD11SynonymProvider;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;

public abstract class AbstractGenericICD11MatchingRule extends AbstractICD11MatchingRule {

  protected Map<String, SearchResultList> findingSiteCache = new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedRegexesResultsCache =
      new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedServerResultsCache = new HashMap<>();

  protected Set<ICD11MatcherSctConcept> findingSiteCons = new HashSet<>();

  private static SearchResultList icd11AllTargets = null;

  protected static FindingSiteUtility fsUtility;

  abstract protected SearchResultList testMatchingFindingSite(String queryPortion) throws Exception;

  protected static SctICD11SynonymProvider synonymProvider =
      new SctICD11SynonymProvider(ICD11MatcherConstants.SNOMED_TO_ICD11);

  @Override
  public String getEclTopLevelDesc() {
    return null;
  }

  @Override
  public String getDefaultSkinMatch() {
    return null;
  }

  @Override
  public String getRuleName() {
    return getRuleId();
  }

  protected SctICD11SynonymProvider getSynonymProvider() {
    return synonymProvider;
  }

  public AbstractGenericICD11MatchingRule(ContentClientRest contentClient, String st, String sv,
      String tt, String tv, String token) {
    super(contentClient, st, sv, tt, tv, token);
  }

  /**
   * Construct con info str.
   *
   * @param findingSites the finding sites
   * @param sctCon the sct con
   * @param counter the counter
   * @return the string buffer
   */
  protected StringBuffer createSnomedConceptSearchedLine(ICD11MatcherSctConcept sctCon,
    int counter) {

    StringBuffer newConInfoStr = new StringBuffer();
    newConInfoStr.append("\n#" + counter + " Snomed Concept: " + sctCon.getName() + "\tSctId: "
        + sctCon.getConceptId() + "\twith");
    if (findingSiteCons != null) {
      int fsCounter = findingSiteCons.size();
      for (ICD11MatcherSctConcept site : findingSiteCons) {
        newConInfoStr.append(" findingSite: " + site.getName());
        if (--fsCounter > 0) {
          newConInfoStr.append("\tand");
        }
      }
    }

    return newConInfoStr;
  }

  /**
   * Identify best match to the concept in question.
   * 
   * The lower the depth, the closer it is. Depth 0 means the actual concept
   * matched
   *
   * @param matchDepthMap the lowest depth map
   * @param matchResultMap the match map
   * @param ruleNumber
   * @param results the str
   * @return
   */
  private Set<String> identifyBestMatch(Map<String, Integer> matchDepthMap,
    Map<String, String> matchResultMap, String ruleType) {
    int lowestDepth = 10000;
    Set<String> lowestDepthStrings = new HashSet<>();

    for (String icdConId : matchDepthMap.keySet()) {
      if (matchDepthMap.get(icdConId) < lowestDepth) {
        /*
         * Right now, all are of rule-type "filtered" so no need to print out
         * 
         * lowestDepthStrings.add( "\n" + ruleType +
         * matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
         * 
         */
        lowestDepthStrings.add(matchResultMap.get(icdConId));
      }
    }

    Set<String> results = new HashSet<>();
    for (String s : lowestDepthStrings) {
      results.add(s);
    }

    return results;
  }

  protected Set<String> matchApproachBaseSearch(ICD11MatcherSctConcept sctCon,
    Set<String> alreadyProcessedResults, SearchResultList targets, String ruleType)
    throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    int depth = 0;

    Set<String> descsToProcess = createICD11SearchStrings(sctCon);

    for (String desc : descsToProcess) {
      if (!alreadyQueriedServerResultsCache.keySet().contains(desc + "")) {
        alreadyQueriedServerResultsCache.put(desc, new HashMap<String, String>());

        SearchResultList matches = testMatchingFindingSite(desc);
        for (SearchResult match : matches.getObjects()) {
          if (!alreadyMatched(match.getCodeId(), alreadyProcessedResults)) {
            if (ruleType.equals(ICD11MatcherConstants.FILTERED_RULE_TYPE) && isRuleMatch(match)) {
              processBaseSearch(match, desc, depth, matchResultMap, matchDepthMap, lowestDepthMap);
            } else if (!ruleType.equals(ICD11MatcherConstants.FILTERED_RULE_TYPE)) {
              processBaseSearch(match, desc, depth, matchResultMap, matchDepthMap, lowestDepthMap);
            }
          }
        }
      } else {
        for (String icd11ConId : alreadyQueriedServerResultsCache.get(desc).keySet()) {
          if (!alreadyMatched(icd11ConId, alreadyProcessedResults)) {
            if (!lowestDepthMap.keySet().contains(icd11ConId)
                || depth < lowestDepthMap.get(icd11ConId)) {
              lowestDepthMap.put(icd11ConId, depth);
              matchResultMap.put(icd11ConId,
                  alreadyQueriedServerResultsCache.get(desc).get(icd11ConId));
              matchDepthMap.put(icd11ConId, depth);
            }
          }
        }
      }
    }

    if (!matchResultMap.isEmpty()) {
      return identifyBestMatch(matchDepthMap, matchResultMap, ruleType);
    }

    return new HashSet<String>();
  }

  private boolean alreadyMatched(String icd11ConId, Set<String> results) {
    for (String result : results) {
      if (result.toLowerCase().contains(icd11ConId.toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  private String processBaseSearch(SearchResult result, String desc, int depth,
    Map<String, String> matchResultMap, Map<String, Integer> matchDepthMap,
    Map<String, Integer> lowestDepthMap) {
    String resultString = "\t" + result.getCodeId() + "\t" + result.getValue() + "\t" + desc + "\t"
        + result.getScore();

    if (!lowestDepthMap.keySet().contains(result.getCodeId())
        || depth < lowestDepthMap.get(result.getCodeId())) {
      lowestDepthMap.put(result.getCodeId(), depth);
      matchResultMap.put(result.getCodeId(), resultString);
      matchDepthMap.put(result.getCodeId(), depth);
    }

    alreadyQueriedServerResultsCache.get(desc).put(result.getCodeId(), resultString);

    return resultString;
  }

  protected Set<String> matchApproachBaseMatch(ICD11MatcherSctConcept sctCon,
    Set<String> alreadyProcessedResults, SearchResultList targets, String ruleType) {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchTokensMap = new HashMap<>();

    for (String desc : createICD11SearchStrings(sctCon)) {
      Set<String> tokens = splitTokens(desc);
      for (SearchResult icd11Con : targets.getObjects()) {
        if (!alreadyMatched(icd11Con.getCodeId(), alreadyProcessedResults)) {
          int matches = 0;

          for (String token : tokens) {
            if (icd11Con.getValue().toLowerCase().matches(".*\\b" + token + "\\b.*")) {
              matches++;
            }
          }

          // Only add it to the return results if all tokens found in
          // icd11Concept
          if (matches >= ((tokens.size() / 2) + (tokens.size() % 2))) {
            String resultString = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t"
                + desc + "\t" + "N/A";

            if (!matchTokensMap.keySet().contains(icd11Con.getCodeId())
                || matchTokensMap.get(icd11Con.getCodeId()) < matches) {
              matchResultMap.put(icd11Con.getCodeId(), resultString);
              matchTokensMap.put(icd11Con.getCodeId(), matches);
            }
          }
        }
      }
    }

    return new HashSet<String>(matchResultMap.values());
  }

  public SearchResultList getAllIcd11Targets() throws Exception {
    if (icd11AllTargets == null) {
      icd11AllTargets =
          client.findConcepts(targetTerminology, targetVersion, "", pfsLimitless, authToken);
      System.out.println("Have returned : " + icd11AllTargets.getTotalCount() + " objects");
    }

    return icd11AllTargets;
  }

  public void preTermProcessing(ICD11MatcherSctConcept sctCon) throws Exception {

    if (usesFindingSites()) {
      findingSiteCons = fsUtility.identifyAssociatedMorphologyBasedFindingSites(sctCon);
    }

  }

  @Override
  public boolean executeContentParsers(String matcherName, SctNeoplasmDescriptionParser descParser,
    SctRelationshipParser relParser) {
    // Generic
    boolean populatedFromFiles = false;

    try {
      populatedFromFiles = descParser.readDescsFromFile(getRulePath(matcherName));
      populatedFromFiles =
          populatedFromFiles && relParser.readRelsFromFile(getRulePath(matcherName));
    } catch (Exception e) {

    }

    return populatedFromFiles;
  }

  protected void setFindingSites(Set<ICD11MatcherSctConcept> findingSites) {
    findingSiteCons = findingSites;
  }

  public Set<ICD11MatcherSctConcept> getFindingSiteCons() {
    return findingSiteCons;
  }

  public static void setFindingSiteUtility(FindingSiteUtility findingSiteUtility) {
    fsUtility = findingSiteUtility;
  }

  public boolean usesFindingSites() {
    return false;
  }

  @Override
  protected Set<String> getRuleBasedNonMatchTerms() {
    return new HashSet<>();
  }

  /**
   * Execute generic rule.
   *
   * @param snomedConcepts the snomed concepts
   * @throws Exception the exception
   */
  @Override
  public Set<String> executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception {

    Set<String> results = new HashSet<>();
    matchNextConcept(sctCon, counter);

    matchApproachBaseMatch(sctCon, results, icd11Targets, ICD11MatcherConstants.FILTERED_RULE_TYPE);
    matchApproachBaseSearch(sctCon, results, icd11Targets,
        ICD11MatcherConstants.FILTERED_RULE_TYPE);

    matchApproachBaseMatch(sctCon, results, getAllIcd11Targets(),
        ICD11MatcherConstants.ALL_LEAFS_RULE_TYPE);
    matchApproachBaseSearch(sctCon, results, getAllIcd11Targets(),
        ICD11MatcherConstants.ALL_LEAFS_RULE_TYPE);

    return results;
  }
}