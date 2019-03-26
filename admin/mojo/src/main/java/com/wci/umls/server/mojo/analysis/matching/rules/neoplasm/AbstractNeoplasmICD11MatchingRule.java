package com.wci.umls.server.mojo.analysis.matching.rules.neoplasm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatchingConstants;
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

  static protected FindingSiteUtility fsUtility;

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
   * @param str the str
   */
  private void identifyBestMatch(Map<String, Integer> matchDepthMap,
    Map<String, String> matchResultMap, StringBuffer str) {
    Set<String> lowestDepthStrings = new HashSet<>();

    for (String icdConId : matchDepthMap.keySet()) {
      lowestDepthStrings.add(
          "\n" + matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
    }

    for (String s : lowestDepthStrings) {
      str.append(s);
    }
  }

  private void identifyMatchesByToken(String desc, Map<String, String> matchMap,
    Map<String, Integer> matchDepthMap, Map<String, Integer> lowestDepthMap, int depth) {

    String normalizedSiteString = fsUtility.cleanNonFindingSiteString(desc.toLowerCase());

    Set<String> tokens = splitTokens(normalizedSiteString);

    for (String token : tokens) {
      if (!ICD11MatchingConstants.NON_MATCHING_TERMS.contains(token)) {

        if (!alreadyQueriedRegexesResultsCache.keySet().contains(token)) {
          alreadyQueriedRegexesResultsCache.put(token, new HashMap<String, String>());

          for (SearchResult icd11Con : icd11Targets.getObjects()) {
            if (icd11Con.getValue().toLowerCase().matches(".*\\b" + token + "\\b.*")) {

              String resultString = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t"
                  + token;

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

  protected void matchAgainstTargets(Set<ICD11MatcherSctConcept> findingSiteCons,
    Set<ICD11MatcherSctConcept> ancestorFindingSites, StringBuffer str) {
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();

    Map<Integer, Set<ICD11MatcherSctConcept>> conceptsToProcess = new HashMap<>();
    conceptsToProcess.put(ICD11MatchingConstants.PARENT_CONCEPTS, findingSiteCons);
    if (ancestorFindingSites != null) {
      conceptsToProcess.put(ICD11MatchingConstants.ANCESTOR_CONCEPTS, ancestorFindingSites);
    }

    for (Integer key : conceptsToProcess.keySet()) {
      for (ICD11MatcherSctConcept fsCon : conceptsToProcess.get(key)) {
        String fsConId = fsCon.getConceptId();
        Map<ICD11MatcherSctConcept, Set<String>> potentialFSConTerms = new HashMap<>();
        if (key == ICD11MatchingConstants.PARENT_CONCEPTS) {
          potentialFSConTerms.put(fsCon, new HashSet<String>(identifyDescs(fsCon)));
        } else {
          // Testing on ancestors of findingSite fsConId
          potentialFSConTerms = fsUtility.getFindingSitePotentialTermsMapCache().get(fsConId);
        }

        for (ICD11MatcherSctConcept testCon : potentialFSConTerms.keySet()) {
          Set<String> normalizedStrs = potentialFSConTerms.get(testCon);

          int depth = 0;
          // Default to PARENT CONCEPTS
          if (key == ICD11MatchingConstants.ANCESTOR_CONCEPTS) {
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
      identifyBestMatch(matchDepthMap, matchResultMap, str);
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

      for (String key : ICD11MatchingConstants.NON_MATCHING_TERMS) {
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

  @Override
  public Object executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception {

    matchNextConcept(sctCon, counter);
    StringBuffer str = new StringBuffer();

    Set<ICD11MatcherSctConcept> fsConcepts =
        fsUtility.identifyPotentialFSConcepts(findingSiteCons, devWriter);

    if (fsConcepts != null) {
      matchAgainstTargets(findingSiteCons, fsConcepts, str);
    }

    return str.toString();
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