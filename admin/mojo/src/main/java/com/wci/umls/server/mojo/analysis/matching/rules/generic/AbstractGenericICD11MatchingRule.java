package com.wci.umls.server.mojo.analysis.matching.rules.generic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.analysis.matching.rules.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.rest.client.ContentClientRest;

public abstract class AbstractGenericICD11MatchingRule extends AbstractICD11MatchingRule {

  protected Map<String, SearchResultList> findingSiteCache = new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedRegexesResultsCache =
      new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedServerResultsCache = new HashMap<>();

  protected Set<ICD11MatcherSctConcept> findingSiteCons;

  private static SearchResultList icd11AllTargets = null;

  protected static FindingSiteUtility fsUtility;

  abstract protected SearchResultList testMatchingFindingSite(String queryPortion) throws Exception;
  
  abstract protected String getRuleQueryString();
  
  protected static final String FILTERED_RULE_TYPE = "filtered";
  protected static final String ALL_LEAFS_RULE_TYPE = "all leaf nodes";


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
  protected StringBuffer createSnomedConceptSearchedLine(ICD11MatcherSctConcept sctCon, int counter) {

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
   */
  private void identifyBestMatch(Map<String, Integer> matchDepthMap,
    Map<String, String> matchResultMap, String ruleType, Set<String> results) {
    int lowestDepth = 10000;
    Set<String> lowestDepthStrings = new HashSet<>();

    for (String icdConId : matchDepthMap.keySet()) {
      if (matchDepthMap.get(icdConId) < lowestDepth) {
        lowestDepthStrings.add(
            "\n" + ruleType + matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
      }
    }

    for (String s : lowestDepthStrings) {
      results.add(s);
    }
  }

  protected void matchApproachBaseSearch(ICD11MatcherSctConcept sctCon, Set<String> results, SearchResultList targets, String ruleType)
    throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    int depth = 0;

    Set<String> descsToProcess = identifyDescs(sctCon);

    for (String desc : descsToProcess) {
      if (!alreadyQueriedServerResultsCache.keySet().contains(desc)) {
        alreadyQueriedServerResultsCache.put(desc, new HashMap<String, String>());

        SearchResultList matches = testMatchingFindingSite(desc);
        for (SearchResult match : matches.getObjects()) {
          
          if (ruleType.equals(FILTERED_RULE_TYPE) && isRuleMatch(match)) {
            processBaseSearch(match, desc, depth, matchResultMap, matchDepthMap, lowestDepthMap);
          } else if (!ruleType.equals(FILTERED_RULE_TYPE)) {
            processBaseSearch(match, desc, depth, matchResultMap, matchDepthMap, lowestDepthMap);
          }
        }
      } else {
        for (String icd11ConId : alreadyQueriedServerResultsCache.get(desc).keySet()) {
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

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, ruleType, results);
    }
  }

  private String processBaseSearch(SearchResult result, String desc, int depth, Map<String, String> matchResultMap, Map<String, Integer> matchDepthMap, Map<String, Integer> lowestDepthMap) {
    String resultString = "\t" + result.getCodeId() + "\t" + result.getValue() + "\t" + desc
        + "\t" + result.getScore();

    if (!lowestDepthMap.keySet().contains(result.getCodeId())
        || depth < lowestDepthMap.get(result.getCodeId())) {
      lowestDepthMap.put(result.getCodeId(), depth);
      matchResultMap.put(result.getCodeId(), resultString);
      matchDepthMap.put(result.getCodeId(), depth);
    }
    
    alreadyQueriedServerResultsCache.get(desc).put(result.getCodeId(), resultString);

    return resultString;
  }

  protected void matchApproachBaseMatch(ICD11MatcherSctConcept sctCon, Set<String> results, SearchResultList targets, String ruleType) {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    int depth = 0;

    Set<String> descsToProcess = identifyDescs(sctCon);

    for (String desc : descsToProcess) {
      for (SearchResult icd11Con : targets.getObjects()) {
        if (icd11Con.getValue().toLowerCase().matches(".*\\b" + desc + "\\b.*")) {

          String resultString = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t"
              + desc + "\t" + "N/A";

          // System.out.println(outputString);
          if (!lowestDepthMap.keySet().contains(icd11Con.getCodeId())
              || depth < lowestDepthMap.get(icd11Con.getCodeId())) {
            lowestDepthMap.put(icd11Con.getCodeId(), depth);
            matchResultMap.put(icd11Con.getCodeId(), resultString);
            matchDepthMap.put(icd11Con.getCodeId(), depth);
          }
        }
      }
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, ruleType, results);
    }
  }

  public void identifyIcd11Targets() throws Exception {
    final SearchResultList fullStringResults = client.findConcepts(targetTerminology, targetVersion,
        getRuleQueryString(), pfsLimitless, authToken);
    System.out.println("Have returned : " + fullStringResults.getTotalCount() + " objects");

    if (printIcd11Targets()) {
      for (SearchResult result : fullStringResults.getObjects()) {
        System.out.println(result.getCodeId() + "\t" + result.getValue());
      }
    }
    
    int matches = 0;
    System.out.println("\n\n\nNow Filtering");
    
    for (SearchResult result : fullStringResults.getObjects()) {
      if (isRuleMatch(result)) {
        if (printIcd11Targets()) {
          System.out.println(result.getCodeId() + "\t" + result.getValue());
        }
        
        icd11Targets.getObjects().add(result);
        icd11Targets.setTotalCount(icd11Targets.getTotalCount() + 1);
        matches++;
      }
    }
    System.out.println("Have actually found : " + matches + " matches");
  }

  public SearchResultList getAllIcd11Targets() throws Exception {
    if (icd11AllTargets == null) {
      icd11AllTargets = client.findConcepts(targetTerminology, targetVersion,
          "", pfsLimitless, authToken);
      System.out.println("Have returned : " + icd11AllTargets.getTotalCount() + " objects");
    }
    
    return icd11AllTargets;  
  }

  private Set<String> identifyDescs(ICD11MatcherSctConcept sctCon) {
    Set<String> descsToProcess = new HashSet<>();

    for (SctNeoplasmDescription fullDesc : sctCon.getDescs()) {
      String desc = fullDesc.getDescription().toLowerCase();
      desc = fsUtility.cleanNonFindingSiteString(desc);

      for (String key : nonMatchingStrings) {
        if (desc.matches(".*\\b" + key + "es\\b.*")) {
          desc = desc.replaceAll(key + "es", "");
        }
        if (desc.matches(".*\\b" + key + "s\\b.*")) {
          desc = desc.replaceAll(key + "s", "");
        }
        if (desc.matches(".*\\b" + key + "\\b.*")) {
          desc = desc.replaceAll(key, "");
        }
      }
      desc = desc.replaceAll("\\s{2,}", " ");
      descsToProcess.add(desc.trim());

      for (String key : snomedToIcdSynonymMap.keySet()) {
        if (desc.matches(".*\\b" + key + "\\b.*")) {
          desc = desc.replaceAll(".*\\b\\w{0,}" + key + "\\w{0,}\\b.*", snomedToIcdSynonymMap.get(key));
          descsToProcess.add(desc.trim());
        }
      }
    }
    return descsToProcess;
  }

  public void preTermProcessing(ICD11MatcherSctConcept sctCon) throws Exception {

    if (usesFindingSites()) {
      findingSiteCons = fsUtility.identifyAssociatedMorphologyBasedFindingSites(sctCon);
    }

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

  protected boolean printIcd11Targets() {
    return false;
  }

  /**
   * Execute generic rule.
   *
   * @param snomedConcepts the snomed concepts
   * @throws Exception the exception
   */
  @Override
  public Object executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception {

    Set<String> results = new HashSet<>();
    matchNextConcept(sctCon, counter);

    matchApproachBaseMatch(sctCon, results, icd11Targets, FILTERED_RULE_TYPE);
    matchApproachBaseSearch(sctCon, results, icd11Targets, FILTERED_RULE_TYPE);
    
    matchApproachBaseMatch(sctCon, results, getAllIcd11Targets(), ALL_LEAFS_RULE_TYPE);
    matchApproachBaseSearch(sctCon, results, getAllIcd11Targets(), ALL_LEAFS_RULE_TYPE);

    return results;
  }
}