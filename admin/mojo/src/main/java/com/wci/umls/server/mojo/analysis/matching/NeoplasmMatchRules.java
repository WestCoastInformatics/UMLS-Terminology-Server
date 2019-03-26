package com.wci.umls.server.mojo.analysis.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.mojo.analysis.matching.rules.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.rules.neoplasm.AbstractNeoplasmICD11MatchingRule;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;

public class NeoplasmMatchRules {

  private FindingSiteUtility fsUtility;

  private Map<Integer, List<String>> resultTypeMap = new HashMap<>();

  private AbstractICD11MatchingRule rule;

  private static final int RETURN_SPECIFIED = 0;

  private static final int RETURN_UNSPECIFIED = 1;

  private static final Integer OTHER_OR_UNSPECIFIED = 0;

  private static final Integer OTHER_SPECIFIED = 1;

  private static final Integer UNSPECIFIED = 2;

  private static final Integer NOT_UNSPECIFIED = 3;

  private static HashSet<String> matchRulesSpecificNonMatchingTerms =
      new HashSet<>(ICD11MatchingConstants.NON_MATCHING_TERMS);

  static {
    matchRulesSpecificNonMatchingTerms.add("other");
    matchRulesSpecificNonMatchingTerms.add("specified");
    matchRulesSpecificNonMatchingTerms.add("unspecified");
  }

  public NeoplasmMatchRules(FindingSiteUtility fsUtility) {
    this.fsUtility = fsUtility;
  }

  public void setRule(AbstractICD11MatchingRule rule) {
    this.rule = rule;
  }

  public String processAllMatchingRules(List<String> results, ICD11MatcherSctConcept sctCon,
    Set<ICD11MatcherSctConcept> findingSites, Set<String> findingSiteNames) throws Exception {
    String result = null;

    if (results.size() == 1) {
      return results.iterator().next() + "\tSINGLE OPTION";
    }

    identifyResultType(results);

    if ((result =
        processInitialUnspecifiedTypes(sctCon, findingSites, findingSiteNames, results)) != null) {
      return result + "\tAAAA";
    } else if ((result = processSkinMatchingRule(sctCon, findingSites, results)) != null) {
      return result + "\tBBBB";
    } else if ((result = processFindingSiteWordInSingleResult(findingSites, sctCon,
        findingSiteNames, results)) != null) {
      return result + "\tCCCC";
    } else if ((result = processSingleResultInFindingSiteWord(findingSites, sctCon,
        findingSiteNames, results)) != null) {
      return result + "\tDDDD";
    } else if ((result = processDisorderConceptWordInSingleResult(findingSites, sctCon,
        findingSiteNames, results)) != null) {
      return result + "\tEEEE";
    } else if ((result = processOneSpecifiedOneNot(sctCon, results, RETURN_UNSPECIFIED)) != null) {
      return result + "\tFFFF";
    } else if ((result =
        processSpecifiedOneDepthLowerRest(sctCon, results, RETURN_UNSPECIFIED)) != null) {
      return result + "\tJJJ";
    } else if ((result =
        processTooNarrowResults(findingSites, sctCon, findingSiteNames, results)) != null) {
      return result + "\tGGGG";
      // } else if ((result =
      // processSingleDivergantPrefix(findingSites, sctCon, findingSiteNames,
      // results)) != null) {
      // return result;
    } else if ((result =
        processDepthCriteria(sctCon, findingSites, findingSiteNames, results)) != null) {
      return result + "\tHHHH";
    }
    return null;
  }

  private String processSpecifiedOneDepthLowerRest(ICD11MatcherSctConcept sctCon,
    List<String> results, int returnUnspecified) {
    boolean singleDepthLower = true;

    int lowestDepth = 100;
    String singleResult = null;

    for (String result : results) {
      if (getDepth(result) < lowestDepth) {
        singleDepthLower = true;
        singleResult = result;
        lowestDepth = getDepth(result);
      } else if (getDepth(result) == lowestDepth) {
        singleDepthLower = false;
      }
    }

    if (singleDepthLower) {
      return singleResult;
    }

    return null;
  }

  private String processDisorderConceptWordInSingleResult(Set<ICD11MatcherSctConcept> fsConcepts,
    ICD11MatcherSctConcept sctCon, Set<String> findingSiteNames, List<String> results)
    throws Exception {
    // If word in finding site exists in single result, return it
    // i.e. SctId: 92557009
    String matchedResult = null;
    Map<String, Integer> singleMatches = new HashMap<>();

    for (SctNeoplasmDescription fullDesc : sctCon.getDescs()) {
      Set<String> descsToProcess = identifyDescs(fullDesc.getDescription());

      for (String desc : descsToProcess) {
        String[] locationTokens =
            FieldedStringTokenizer.split(desc, " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

        for (int i = 0; i < locationTokens.length; i++) {
          String token = locationTokens[i].trim().toLowerCase();

          if (!token.isEmpty() && !matchRulesSpecificNonMatchingTerms.contains(token)) {
            int matches = 0;

            for (String result : results) {
              String icd11String = result.split("\t")[1];
              if (icd11String.toLowerCase().matches("\\b.*" + token + "\\b.*")) {
                if (matches++ > 0) {
                  break;
                }

                matchedResult = result;
              }
            }

            captureMatches(singleMatches, matches, matchedResult);
          }
        }
      }
    }
    return singleMatchCapture(singleMatches, sctCon, results);
  }

  private Set<String> identifyDescs(String description) {
    Set<String> descsToProcess = new HashSet<>();
    String originalDesc = description.trim().toLowerCase().trim();
    boolean matchFound = false;

    for (String key : AbstractNeoplasmICD11MatchingRule.snomedToIcdSynonymMap.keySet()) {
      if (originalDesc.matches(".*\\b" + key + "\\b.*")) {
        originalDesc = originalDesc.replaceAll(".*\\b" + key + "\\b.*",
            AbstractNeoplasmICD11MatchingRule.snomedToIcdSynonymMap.get(key));
        descsToProcess.add(originalDesc);
        matchFound = true;
      }
    }

    if (!matchFound) {
      descsToProcess.add(originalDesc);
    }

    return descsToProcess;
  }

  private String singleMatchCapture(Map<String, Integer> singleMatches,
    ICD11MatcherSctConcept sctCon, List<String> results) throws Exception {
    int mostMatchedResults = 0;
    String matchedResult = null;
    boolean singleMatch = false;

    for (String key : singleMatches.keySet()) {
      if (mostMatchedResults < singleMatches.get(key)) {
        mostMatchedResults = singleMatches.get(key);
        matchedResult = key;
        singleMatch = true;
      } else if (mostMatchedResults == singleMatches.get(key)) {
        singleMatch = false;
      }
    }

    if (singleMatch) {
      return matchedResult;
    } else if (matchedResult != null) {
      // Have multiple equal matches. See if one one is specific and other is
      // not
      return processOneSpecifiedOneNot(sctCon, results, RETURN_SPECIFIED);
    }

    return null;
  }

  private void captureMatches(Map<String, Integer> singleMatches, int matches,
    String matchedResult) {
    if (matches == 1) {
      if (singleMatches.containsKey(matchedResult)) {
        singleMatches.put(matchedResult, singleMatches.get(matchedResult) + 1);
      } else {
        singleMatches.put(matchedResult, 1);
      }
    }
  }

  /*
   * 3 or more are of single prefix while one prefix doesn't match
   *
   * private String processSingleDivergantPrefix(Set<SctNeoplasmConcept>
   * fsConcepts, SctNeoplasmConcept sctCon, Set<String> findingSites,
   * List<String> results) throws Exception { HashMap<String, Integer>
   * prefixCountMap = new HashMap<>();
   * 
   * for (String result : results) { String prefix = result.split("\t")[0]; if
   * (!prefixCountMap.keySet().contains(prefix)) { prefixCountMap.put(result,
   * 0); }
   * 
   * prefixCountMap.put(prefix, prefixCountMap.get(prefix) + 1); }
   * 
   * boolean hasPrefixMinimalSize = false; String singlePrefix = null; if
   * (prefixCountMap.keySet().size() == 2) { for (String prefix :
   * prefixCountMap.keySet()) { if (prefixCountMap.get(prefix) == 1) {
   * singlePrefix = prefix; } else { if (prefixCountMap.get(prefix) > 2) {
   * hasPrefixMinimalSize = true; } } }
   * 
   * if (hasPrefixMinimalSize && singlePrefix != null) { List<String>
   * newCallList = new ArrayList<>();
   * 
   * for (String result : results) { if
   * (!singlePrefix.equals(result.split("\t")[0])) { newCallList.add(result); }
   * }
   * 
   * return processAllMatchingRules(newCallList, sctCon, findingSites); } }
   * return null; }
   */

  // At this point, there may be too many narrow-matches returned (ie Lobular or
  // ductal carcinoma where the type isn't actually specified)
  /*
   * Lobular carcinoma in situ of breast Lobular carcinoma in situ of breast,
   * pleomorphic subtype Ductal carcinoma in situ of breast Ductal carcinoma in
   * situ of breast, comedo subtype Mixed ductal and lobular carcinoma in situ
   * of breast
   *
   * Rather than: Other specified carcinoma in situ of breast
   * 
   * See: 92540005
   */

  private String processTooNarrowResults(Set<ICD11MatcherSctConcept> fsConcepts,
    ICD11MatcherSctConcept sctCon, Set<String> findingSiteNames, List<String> results)
    throws Exception {
    Map<String, Set<String>> minimizedResults = new HashMap<>();
    boolean processingInitialList = true;
    Set<String> intersectionSet = new HashSet<>();
    Set<String> nonSpecificSet = new HashSet<>();
    Map<String, String> originalResults = new HashMap<>();

    // Only process if it's not the case of specified and one not specified
    // Will give false-positives to specified

    for (String result : results) {

      if (!resultTypeMap.get(NOT_UNSPECIFIED).contains(result)) {
        String desc = result.split("\t")[1];
        nonSpecificSet.add(desc);
        originalResults.put(desc, result);
      } else {

        String desc = result.split("\t")[1];
        originalResults.put(desc, result);

        minimizedResults.put(desc, new HashSet<String>());

        String[] tokens = FieldedStringTokenizer.split(desc.toLowerCase(),
            " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

        for (int i = 0; i < tokens.length; i++) {
          if (!tokens[i].isEmpty() && !matchRulesSpecificNonMatchingTerms.contains(tokens[i])) {
            if (processingInitialList) {
              intersectionSet.add(tokens[i]);
            }

            minimizedResults.get(desc).add(tokens[i]);
          }
        }

        processingInitialList = false;
      }
    }

    // Cannot define what can be narrower if only have specific content
    if (nonSpecificSet.isEmpty()) {
      return null;
    }

    if (nonSpecificSet.size() > minimizedResults.size()) {
      return null;
    }

    // Now have relevant words only. Find words in common
    for (Set<String> minimizedResultSet : minimizedResults.values()) {
      intersectionSet = Sets.intersection(intersectionSet, Sets.newHashSet(minimizedResultSet));
    }

    // Identify closest match
    List<String> leastResults = identifyLeastResult(minimizedResults, intersectionSet);

    for (String key : nonSpecificSet) {
      minimizedResults.put(key, new HashSet<String>());
      String[] tokens =
          FieldedStringTokenizer.split(key.toLowerCase(), " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

      for (int i = 0; i < tokens.length; i++) {
        if (!tokens[i].isEmpty() && !matchRulesSpecificNonMatchingTerms.contains(tokens[i])) {
          minimizedResults.get(key).add(tokens[i]);
        }
      }
    }

    leastResults = identifyLeastResult(minimizedResults, intersectionSet);

    List<String> retList = new ArrayList<>();

    for (String key : leastResults) {
      retList.add(originalResults.get(key));
    }

    if (retList.size() == originalResults.size()) {
      return null;
    }

    return processAllMatchingRules(retList, sctCon, fsConcepts, findingSiteNames);
  }

  private List<String> identifyLeastResult(Map<String, Set<String>> minimizedResults,
    Set<String> intersectionSet) {
    new HashSet<>();
    int leastExtra = 100;
    List<String> leastResult = new ArrayList<>();

    for (String key : minimizedResults.keySet()) {
      Set<String> minimizedResultSet = minimizedResults.get(key);
      int currentExtra = 0;

      for (String token : minimizedResultSet) {
        if (!token.trim().isEmpty() && !intersectionSet.contains(token.toLowerCase())) {
          currentExtra++;
        }
      }

      if (currentExtra == leastExtra) {
        leastResult.add(key);
      } else if (currentExtra < leastExtra) {
        leastExtra = currentExtra;
        leastResult.clear();
        leastResult.add(key);
      }
    }

    return leastResult;
  }

  private String processDepthCriteria(ICD11MatcherSctConcept sctCon,
    Set<ICD11MatcherSctConcept> fsConcepts, Set<String> findingSiteNames, List<String> results)
    throws Exception {
    /*
     * with stopping to concern b/w two different depths, just identify lowest
     * depth items // ProcessUnspecifiedHigherLevelThanSingleSpecific int
     * lowestUnspecifiedLevel = 100; int lowestSpecificLevel = 100; for (String
     * result : results) { int depth = Integer.parseInt(result.split("\t")[3]);
     * 
     * if (resultTypeMap.get(NOT_UNSPECIFIED).contains(result)) { if (depth <=
     * lowestSpecificLevel) { lowestSpecificLevel = depth; } } else { if (depth
     * <= lowestUnspecifiedLevel) { lowestUnspecifiedLevel = depth; } } }
     */

    // ProcessUnspecifiedHigherLevelThanSingleSpecific
    List<String> lowestMatches = new ArrayList<>();

    int lowestDepth = 100;
    int lowestMatchCount = 0;
    for (String result : results) {
      int depth = getDepth(result);

      if (depth < lowestDepth) {
        lowestDepth = depth;
        lowestMatches.clear();
        lowestMatches.add(result);
        lowestMatchCount = 1;
      } else if (depth == lowestDepth) {
        lowestMatches.add(result);
        lowestMatchCount++;
      }
    }

    // Commenting out b/c with new rules, this commented out check shouldn't be
    // an issue
    // if (lowestSpecificLevel < lowestUnspecifiedLevel) {
    if (lowestMatchCount != results.size()) {
      return processAllMatchingRules(lowestMatches, sctCon, fsConcepts, findingSiteNames);
    }

    return null;
  }

  private String processInitialUnspecifiedTypes(ICD11MatcherSctConcept sctCon,
    Set<ICD11MatcherSctConcept> fsConcepts, Set<String> findingSiteNames, List<String> results)
    throws Exception {
    // All are notUnspecified
    if (resultTypeMap.get(NOT_UNSPECIFIED).size() == results.size()) {
      return null;
    }

    // ProcessOnlyUnspecified
    if (resultTypeMap.get(NOT_UNSPECIFIED).isEmpty()) {
      if (resultTypeMap.get(OTHER_OR_UNSPECIFIED).size() == 1) {
        return resultTypeMap.get(OTHER_OR_UNSPECIFIED).iterator().next();
      } else if (resultTypeMap.get(OTHER_SPECIFIED).size() == 1) {
        return resultTypeMap.get(OTHER_SPECIFIED).iterator().next();
      }
    }

    return null;
  }

  private String processSingleResultInFindingSiteWord(Set<ICD11MatcherSctConcept> fsConcepts,
    ICD11MatcherSctConcept sctCon, Set<String> findingSites, List<String> results)
    throws Exception {
    // Inverse... If word in single result exists in finding site , return it
    // i.e. SctId: 92637002
    String matchedResult = null;
    Map<String, Integer> singleMatches = new HashMap<>();
    Set<String> descsToProcess = new HashSet<>();

    for (ICD11MatcherSctConcept con : fsConcepts) {
      for (SctNeoplasmDescription fullDesc : con.getDescs()) {
        descsToProcess.addAll(identifyDescs(fullDesc.getDescription()));
      }
    }

    if (fsConcepts != null) {
      for (String result : results) {
        int matches = 0;
        String icd11String = result.split("\t")[1];
        String[] locationTokens = FieldedStringTokenizer.split(icd11String.toLowerCase(),
            " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

        for (int i = 0; i < locationTokens.length; i++) {
          String token = locationTokens[i].trim().toLowerCase();

          if (!token.isEmpty() && !matchRulesSpecificNonMatchingTerms.contains(token)) {
            for (String desc : descsToProcess) {
              if (desc.contains(token)) {
                if (matches++ > 0) {
                  break;
                }
                matchedResult = result;
              }
            }

            captureMatches(singleMatches, matches, matchedResult);
          }
        }
      }
    }

    return singleMatchCapture(singleMatches, sctCon, results);
  }

  private String processFindingSiteWordInSingleResult(Set<ICD11MatcherSctConcept> fsConcepts,
    ICD11MatcherSctConcept sctCon, Set<String> findingSites, List<String> results)
    throws Exception {
    // If word in finding site exists in single result, return it
    // i.e. SctId: 92557009
    String matchedResult = null;
    Map<String, Integer> singleMatches = new HashMap<>();

    if (fsConcepts != null && !fsConcepts.isEmpty()) {
      for (ICD11MatcherSctConcept con : fsConcepts) {
        for (SctNeoplasmDescription fullDesc : con.getDescs()) {
          Set<String> descsToProcess = identifyDescs(fullDesc.getDescription());

          for (String desc : descsToProcess) {
            String[] locationTokens = FieldedStringTokenizer.split(desc.toLowerCase(),
                " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

            for (int i = 0; i < locationTokens.length; i++) {
              String token = locationTokens[i].trim().toLowerCase();

              if (!token.isEmpty() && !matchRulesSpecificNonMatchingTerms.contains(token)) {
                int matches = 0;

                for (String result : results) {
                  String icd11String = result.split("\t")[1];
                  if (icd11String.toLowerCase().contains(token)) {
                    if (matches++ > 0) {
                      break;
                    }
                    matchedResult = result;
                  }
                }

                captureMatches(singleMatches, matches, matchedResult);
              }
            }
          }
        }
      }
    }

    return singleMatchCapture(singleMatches, sctCon, results);
  }

  private String processSkinMatchingRule(ICD11MatcherSctConcept sctCon,
    Set<ICD11MatcherSctConcept> fsConcepts, List<String> results) throws Exception {

    // If findingSite contains "skin" likely 2E64.Y Other specified carcinoma in
    // situ of skin
    // Unless finding site exists in other result
    // i.e. SctId: 92717008
    boolean isSkin = false;
    Set<ICD11MatcherSctConcept> fsCons = fsUtility.identifyPotentialFSConcepts(fsConcepts, null);
    for (ICD11MatcherSctConcept fsCon : fsCons) {
      for (SctNeoplasmDescription desc : fsCon.getDescs()) {
        if (desc.getDescription().toLowerCase().matches(".*\\bskin\\b.*")) {
          isSkin = true;
          break;
        }
      }

      if (isSkin) {
        break;
      }
    }

    List<String> bodyMatches = new ArrayList<>();
    Set<String> retSet = new HashSet<>();

    // Identified Skin, return proper result
    if (isSkin) {
      for (SctNeoplasmDescription fullDesc : sctCon.getDescs()) {
        if (fullDesc.getSecondInfo() != null) {
          Set<String> descsToProcess = identifyDescs(fullDesc.getSecondInfo());

          for (String desc : descsToProcess) {

            String[] locationTokens = desc.split(" ");

            for (int i = 0; i < locationTokens.length; i++) {
              String token = locationTokens[i].trim().toLowerCase();

              if (!token.isEmpty() && !token.equals("skin")
                  && !matchRulesSpecificNonMatchingTerms.contains(token)) {
                for (String result : results) {
                  String icd11String = result.split("\t")[1];
                  if (icd11String.toLowerCase().contains(token) && result.contains("skin")) {
                    bodyMatches.add(result);
                  }
                }
              }
            }
          }
        }
      }

      for (String r : bodyMatches) {
        for (String synonym : ICD11MatchingConstants.NEOPLASM_SYNONYMS) {
          if (r.toLowerCase().contains(synonym.toLowerCase())) {
            String prefix = r.toLowerCase().substring(r.indexOf("\t")).trim();
            prefix = prefix.substring(0, prefix.indexOf(synonym.toLowerCase()));

            if (!prefix.trim().isEmpty()) {
              for (ICD11MatcherSctConcept fsCon : fsCons) {
                for (SctNeoplasmDescription desc : fsCon.getDescs()) {
                  if (desc.getDescription().toLowerCase().contains(prefix.trim().toLowerCase())) {
                    retSet.add(r);
                  }
                }
              }
            }
          }
        }
      }

      if (retSet.size() == 1) {
        return retSet.iterator().next();
      }

      for (String result : results) {
        if (result.contains(rule.getDefaultSkinMatch())) {
          return result;
        }
      }
    }

    return null;

  }

  private String processOneSpecifiedOneNot(ICD11MatcherSctConcept sctCon, List<String> results,
    int returnSpecified) throws Exception {

    if (returnSpecified == RETURN_SPECIFIED && resultTypeMap.get(NOT_UNSPECIFIED).size() == 1) {
      return resultTypeMap.get(NOT_UNSPECIFIED).iterator().next();
    } else if (returnSpecified == RETURN_UNSPECIFIED
        && (results.size() - resultTypeMap.get(NOT_UNSPECIFIED).size() == 1)) {
      for (String result : results) {
        if (!resultTypeMap.get(NOT_UNSPECIFIED).contains(result)) {
          return result;
        }
      }
    }

    return null;
  }

  private void identifyResultType(List<String> results) {
    resultTypeMap.put(OTHER_OR_UNSPECIFIED, new ArrayList<String>());
    resultTypeMap.put(OTHER_SPECIFIED, new ArrayList<String>());
    resultTypeMap.put(UNSPECIFIED, new ArrayList<String>());
    resultTypeMap.put(NOT_UNSPECIFIED, new ArrayList<String>());

    for (String result : results) {
      if (result.toLowerCase().contains("other or unspecified")) {
        resultTypeMap.get(OTHER_OR_UNSPECIFIED).add(result);
      } else if (result.toLowerCase().contains("other specified")) {
        resultTypeMap.get(OTHER_SPECIFIED).add(result);
      } else if (result.toLowerCase().contains("unspecified")) {
        resultTypeMap.get(UNSPECIFIED).add(result);
      } else {
        resultTypeMap.get(NOT_UNSPECIFIED).add(result);
      }
    }
  }

  private int getDepth(String result) {
    // TODO Auto-generated method stub
    return Integer.parseInt(result.split("\t")[3]);
  }
}