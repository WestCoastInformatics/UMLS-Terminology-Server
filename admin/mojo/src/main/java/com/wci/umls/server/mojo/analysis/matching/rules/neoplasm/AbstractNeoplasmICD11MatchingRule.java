package com.wci.umls.server.mojo.analysis.matching.rules.neoplasm;

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

public abstract class AbstractNeoplasmICD11MatchingRule extends AbstractICD11MatchingRule {

  protected Map<String, SearchResultList> findingSiteCache = new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedRegexesResultsCache =
      new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedServerResultsCache = new HashMap<>();

  protected Set<ICD11MatcherSctConcept> findingSiteCons;

  private static final Integer PARENT_CONCEPTS = 1;

  private static final Integer ANCESTOR_CONCEPTS = 2;

  static protected FindingSiteUtility fsUtility;

  abstract public Object executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception;

  abstract protected SearchResultList testMatchingFindingSite(String queryPortion) throws Exception;

  public AbstractNeoplasmICD11MatchingRule(ContentClientRest contentClient, String st, String sv,
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
   * @param str the str
   */
  private void identifyBestMatch(Map<String, Integer> matchDepthMap,
    Map<String, String> matchResultMap, int ruleNumber, StringBuffer str) {
    int lowestDepth = 10000;
    Set<String> lowestDepthStrings = new HashSet<>();
    // String resultString = "\n" + ruleNumber + "\t" + icd11Con.getCodeId() +
    // "\t"
    // + icd11Con.getValue() + "\t" + depth + "\t" + token + "\t" +
    // icd11Con.getScore();

    for (String icdConId : matchDepthMap.keySet()) {
      if (matchDepthMap.get(icdConId) < lowestDepth) {
        // lowestDepthStrings.clear();
        lowestDepthStrings.add(
            "\n" + ruleNumber + matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
        /*
         * lowestDepth = matchDepthMap.get(icdConId); } else if
         * (matchDepthMap.get(icdConId) == lowestDepth) {
         * lowestDepthStrings.add( "\n" + ruleNumber +
         * matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
         */ }
    }

    // System.out.println("\n\nBut actually outputing:");
    for (String s : lowestDepthStrings) {
      // System.out.println(s);
      str.append(s);
    }
  }

  protected void matchApproachBaseSearch(ICD11MatcherSctConcept sctCon, StringBuffer str)
    throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    int depth = 0;

    Set<String> descsToProcess = identifyDescs(sctCon);

    for (String desc : descsToProcess) {
      if (!alreadyQueriedServerResultsCache.keySet().contains(desc)) {
        alreadyQueriedServerResultsCache.put(desc, new HashMap<String, String>());

        SearchResultList results = testMatchingFindingSite(desc);
        for (SearchResult result : results.getObjects()) {
          if (isRuleMatch(result)) {

            String resultString = "\t" + result.getCodeId() + "\t" + result.getValue() + "\t" + desc
                + "\t" + result.getScore();

            if (!lowestDepthMap.keySet().contains(result.getCodeId())
                || depth < lowestDepthMap.get(result.getCodeId())) {
              lowestDepthMap.put(result.getCodeId(), depth);
              matchResultMap.put(result.getCodeId(), resultString);
              matchDepthMap.put(result.getCodeId(), depth);
            }
            alreadyQueriedServerResultsCache.get(desc).put(result.getCodeId(), resultString);
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
      identifyBestMatch(matchDepthMap, matchResultMap, 1111, str);
    }
  }

  protected void matchApproachBaseMatch(ICD11MatcherSctConcept sctCon, StringBuffer str) {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    int depth = 0;

    Set<String> descsToProcess = identifyDescs(sctCon);

    for (String desc : descsToProcess) {
      for (SearchResult icd11Con : icd11Targets.getObjects()) {
        if (icd11Con.getValue().toLowerCase().matches(".*\\b" + desc + "\\b.*")) {

          String resultString = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t"
              + desc + "\t" + icd11Con.getScore();

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
      identifyBestMatch(matchDepthMap, matchResultMap, 1111, str);
    }
  }

  /**
   * Match approach 1.
   * @param str the str
   * @param icd11Targets the icd 11 targets
   *
   * @return true, if successful
   * @throws Exception the exception
   */
  protected void matchApproach1(StringBuffer str) throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    for (ICD11MatcherSctConcept site : findingSiteCons) {
      for (SctNeoplasmDescription desc : site.getDescs()) {
        identifyMatchesByToken(desc.getDescription(), matchResultMap, matchDepthMap, lowestDepthMap,
            0);
      }
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 1111, str);
    }
  }

  protected void matchApproach2(StringBuffer str) throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    for (ICD11MatcherSctConcept site : findingSiteCons) {
      identifyMatchesByConceptDescriptions(site, matchResultMap, matchDepthMap, lowestDepthMap, 0);
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 2222, str);
    }
  }

  private void identifyMatchesByConceptDescriptions(ICD11MatcherSctConcept fsConcept,
    Map<String, String> matchMap, Map<String, Integer> matchDepthMap,
    Map<String, Integer> lowestDepthMap, int depth) throws Exception {

    for (SctNeoplasmDescription fullDesc : fsConcept.getDescs()) {
      String desc = fullDesc.getDescription();
      desc = fsUtility.cleanNonFindingSiteString(desc);

      if (!alreadyQueriedServerResultsCache.keySet().contains(desc)) {
        alreadyQueriedServerResultsCache.put(desc, new HashMap<String, String>());

        SearchResultList results = testMatchingFindingSite(desc);
        for (SearchResult result : results.getObjects()) {
          if (isRuleMatch(result)) {

            String resultString = "\t" + result.getCodeId() + "\t" + result.getValue() + "\t" + desc
                + "\t" + result.getScore();

            if (!lowestDepthMap.keySet().contains(result.getCodeId())
                || depth < lowestDepthMap.get(result.getCodeId())) {
              lowestDepthMap.put(result.getCodeId(), depth);
              matchMap.put(result.getCodeId(), resultString);
              matchDepthMap.put(result.getCodeId(), depth);
            }
            alreadyQueriedServerResultsCache.get(desc).put(result.getCodeId(), resultString);
          }
        }
      } else {
        for (String icd11ConId : alreadyQueriedServerResultsCache.get(desc).keySet()) {
          if (!lowestDepthMap.keySet().contains(icd11ConId)
              || depth < lowestDepthMap.get(icd11ConId)) {
            lowestDepthMap.put(icd11ConId, depth);
            matchMap.put(icd11ConId, alreadyQueriedServerResultsCache.get(desc).get(icd11ConId));
            matchDepthMap.put(icd11ConId, depth);
          }
        }
      }
    }

    return;
  }

  private void identifyMatchesByToken(String desc, Map<String, String> matchMap,
    Map<String, Integer> matchDepthMap, Map<String, Integer> lowestDepthMap, int depth) {

    String normalizedSiteString = fsUtility.cleanNonFindingSiteString(desc.toLowerCase());

    Set<String> tokens = splitTokens(normalizedSiteString);

    for (String token : tokens) {
      if (!fsUtility.getNonFindingSiteStrings().contains(token)) {

        if (!alreadyQueriedRegexesResultsCache.keySet().contains(token)) {
          alreadyQueriedRegexesResultsCache.put(token, new HashMap<String, String>());

          for (SearchResult icd11Con : icd11Targets.getObjects()) {
            if (icd11Con.getValue().toLowerCase().matches(".*\\b" + token + "\\b.*")) {

              String resultString = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t"
                  + token + "\t" + icd11Con.getScore();

              if (!lowestDepthMap.keySet().contains(icd11Con.getCodeId())
                  || depth < lowestDepthMap.get(icd11Con.getCodeId())) {
                lowestDepthMap.put(icd11Con.getCodeId(), depth);
                matchMap.put(icd11Con.getCodeId(), resultString);
                matchDepthMap.put(icd11Con.getCodeId(), depth);
              }
              alreadyQueriedRegexesResultsCache.get(token).put(icd11Con.getCodeId(), resultString);
            }
          }
        } else {
          for (String icd11ConId : alreadyQueriedRegexesResultsCache.get(token).keySet()) {
            if (!lowestDepthMap.keySet().contains(icd11ConId)
                || depth < lowestDepthMap.get(icd11ConId)) {
              lowestDepthMap.put(icd11ConId, depth);
              matchMap.put(icd11ConId,
                  alreadyQueriedRegexesResultsCache.get(token).get(icd11ConId));
              matchDepthMap.put(icd11ConId, depth);
            }
          }
        }
      }
    }

    return;
  }

  protected void matchApproach3(Set<ICD11MatcherSctConcept> findingSiteCons,
    Set<ICD11MatcherSctConcept> ancestorFindingSites, StringBuffer str) {
    // icdTarget to map of depth-to-output
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    // icdTarget to map of depth-to-output
    Map<String, String> matchResultMap = new HashMap<>();

    Map<String, Integer> matchDepthMap = new HashMap<>();

    Map<Integer, Set<ICD11MatcherSctConcept>> conceptsToProcess = new HashMap<>();
    conceptsToProcess.put(PARENT_CONCEPTS, findingSiteCons);
    conceptsToProcess.put(ANCESTOR_CONCEPTS, ancestorFindingSites);

    for (Integer key : conceptsToProcess.keySet()) {
      for (ICD11MatcherSctConcept fsCon : conceptsToProcess.get(key)) {
        String fsConId = fsCon.getConceptId();
        Map<ICD11MatcherSctConcept, Set<String>> potentialFSConTerms = new HashMap<>();
        if (key == PARENT_CONCEPTS) {
          potentialFSConTerms.put(fsCon, new HashSet<String>());
          for (SctNeoplasmDescription desc : fsCon.getDescs()) {
            potentialFSConTerms.get(fsCon).add(desc.getDescription());
          }
        } else {
          // Testing on ancestors of findingSite fsConId
          potentialFSConTerms = fsUtility.getFindingSitePotentialTermsMapCache().get(fsConId);
        }

        for (ICD11MatcherSctConcept testCon : potentialFSConTerms.keySet()) {
          Set<String> normalizedStrs = potentialFSConTerms.get(testCon);

          int depth = 0;
          // Default to PARENT CONCEPTS
          if (key == ANCESTOR_CONCEPTS) {
            Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
            depth = depthMap.get(testCon.getConceptId());
          }

          for (String normalizedStr : normalizedStrs) {
            identifyMatchesByToken(normalizedStr, matchResultMap, matchDepthMap, lowestDepthMap,
                depth);
          }
        }
      }
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 3333, str);
    }
  }

  /*  *//**
         * Match approach 3.
         *
         * @param fsConcepts the fs concepts
         * @param icd11Targets the icd 11 targets
         * @param str the str
         * @return true, if successful
         *//*
            * protected void matchApproach3(Set<ICD11MatcherSctConcept>
            * fsConcepts, StringBuffer str) { // icdTarget to map of
            * depth-to-output Map<String, Integer> lowestDepthMap = new
            * HashMap<>(); // icdTarget to map of depth-to-output Map<String,
            * String> matchResultMap = new HashMap<>();
            * 
            * Map<String, Integer> matchDepthMap = new HashMap<>();
            * 
            * for (ICD11MatcherSctConcept fsCon : fsConcepts) { String fsConId =
            * fsCon.getConceptId();
            * 
            * // Testing on ancestors of findingSite fsConId Map<String,
            * Integer> depthMap = inverseTransClosureMap.get(fsConId);
            * Map<ICD11MatcherSctConcept, Set<String>> potentialFSConTerms =
            * fsUtility.getFindingSitePotentialTermsMapCache().get(fsConId);
            * 
            * for (ICD11MatcherSctConcept testCon :
            * potentialFSConTerms.keySet()) { Set<String> normalizedStrs =
            * potentialFSConTerms.get(testCon);
            * 
            * int depth = depthMap.get(testCon.getConceptId());
            * 
            * for (String normalizedStr : normalizedStrs) {
            * identifyMatchesByToken(normalizedStr, matchResultMap,
            * matchDepthMap, lowestDepthMap, depth); } } }
            * 
            * if (!matchResultMap.isEmpty()) { identifyBestMatch(matchDepthMap,
            * matchResultMap, 3333, str); } }
            */

  /**
   * Match approach 4.
   *
   * @param fsConcepts the fs concepts
   * @param str the str
   * @return true, if successful
   * @throws Exception the exception
   */
  protected void matchApproach4(Set<ICD11MatcherSctConcept> fsConcepts, StringBuffer str)
    throws Exception {
    // icdTarget to map of depth-to-output
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    // icdTarget to map of depth-to-output
    Map<String, String> matchResultMap = new HashMap<>();

    Map<String, Integer> matchDepthMap = new HashMap<>();

    for (ICD11MatcherSctConcept fsCon : fsConcepts) {
      String fsConId = fsCon.getConceptId();

      // Testing on ancestors of findingSite fsConId
      Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
      Map<ICD11MatcherSctConcept, Set<String>> potentialFSConTerms =
          fsUtility.getFindingSitePotentialTermsMapCache().get(fsConId);

      for (ICD11MatcherSctConcept fsConcept : potentialFSConTerms.keySet()) {
        int depth = depthMap.get(fsConcept.getConceptId());

        identifyMatchesByConceptDescriptions(fsConcept, matchResultMap, matchDepthMap,
            lowestDepthMap, depth);

      }
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 4444, str);
    }
  }

  public static void setFindingSiteUtility(FindingSiteUtility findingSiteUtility) {
    fsUtility = findingSiteUtility;
  }

  public boolean usesFindingSites() {
    return true;
  }

  private Set<String> identifyDescs(ICD11MatcherSctConcept sctCon) {
    Set<String> descsToProcess = new HashSet<>();

    for (SctNeoplasmDescription fullDesc : sctCon.getDescs()) {
      String desc = fullDesc.getDescription().toLowerCase();
      desc = fsUtility.cleanNonFindingSiteString(desc);
      descsToProcess.add(desc);

      for (String key : nonMatchingStrings) {
        if (desc.matches(".*\\b" + key + "\\b.*")) {
          desc = desc.replaceAll(".*\\b" + key + "\\b.*", "");
        }
      }

      for (String key : snomedToIcdSynonymMap.keySet()) {
        if (desc.matches(".*\\b" + key + "\\b.*")) {
          desc = desc.replaceAll(".*\\b" + key + "\\b.*", snomedToIcdSynonymMap.get(key));
          descsToProcess.add(desc);
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
}