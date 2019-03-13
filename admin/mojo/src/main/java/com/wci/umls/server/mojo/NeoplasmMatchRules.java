package com.wci.umls.server.mojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;

public class NeoplasmMatchRules {

  private FindingSiteUtility fsUtility;

  /** The non finding site strings. */
  final static protected List<String> nonSignificantMatchingStrings =
      Arrays.asList("of", "part", "structure", "system", "and/or", "and", "region", "area", "or",
          "the", "tract", "other", "specified", "unspecified");

  public NeoplasmMatchRules(FindingSiteUtility fsUtility) {
    this.fsUtility = fsUtility;
  }

  public String processAllMatchingRules(List<String> results, SctNeoplasmConcept sctCon,
    Set<String> findingSites) throws Exception {
    String result = null;
    Set<SctNeoplasmConcept> fsConcepts = fsUtility.identifyPotentialFSConcepts(findingSites);

    if (results.size() == 1) {
      return results.iterator().next();
    }

    if ((result = processUnspecifiedTypes(sctCon, findingSites, results)) != null) {
      return result;
    } else if ((result = processSkinMatchingRule(sctCon, findingSites, results)) != null) {
      return result;
    } else if ((result =
        processFindingSiteWordInSingleResult(fsConcepts, sctCon, findingSites, results)) != null) {
      return result;
    } else if ((result =
        processSingleResultInFindingSiteWord(fsConcepts, sctCon, findingSites, results)) != null) {
      return result;
    } else if ((result =
        processTooNarrowResults(fsConcepts, sctCon, findingSites, results)) != null) {
      return result;
    }
    return null;
  }

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

  private String processTooNarrowResults(Set<SctNeoplasmConcept> fsConcepts,
    SctNeoplasmConcept sctCon, Set<String> findingSites, List<String> results) throws Exception {
    Map<String, Set<String>> minimizedResults = new HashMap<>();
    boolean processingInitialList = true;
    Set<String> intersectionSet = new HashSet<>();
    Set<String> nonSpecificSet = new HashSet<>();
    Map<String, String> originalResults = new HashMap<>();

    for (String result : results) {

      if (result.toLowerCase().contains("other or unspecified")
          || result.toLowerCase().contains("other specified")
          || result.toLowerCase().contains("unspecified")) {
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
          if (!nonSignificantMatchingStrings.contains(tokens[i])) {
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

    // Now have relevant words only. Find words in common
    for (Set<String> minimizedResultSet : minimizedResults.values()) {
      intersectionSet = Sets.intersection(intersectionSet, Sets.newHashSet(minimizedResultSet));
    }

    // Identify closest match
    List<String> leastResults = identifyLeastResult(minimizedResults, intersectionSet);

    for (String key : nonSpecificSet) {
      minimizedResults.put(key, new HashSet<String>());
      String[] tokens = FieldedStringTokenizer.split(key.toLowerCase(),
          " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

      for (int i = 0; i < tokens.length; i++) {
        if (!nonSignificantMatchingStrings.contains(tokens[i])) {
          minimizedResults.get(key).add(tokens[i]);
        }
      }
    }

    leastResults = identifyLeastResult(minimizedResults, intersectionSet);

    List<String> retList = new ArrayList<>();

    for (String key : minimizedResults.keySet()) {
      retList.add(originalResults.get(key));
    }

    if (retList.size() == originalResults.size()) {
      return null;
    }
    
    return processAllMatchingRules(retList, sctCon, findingSites);
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

  private String processUnspecifiedTypes(SctNeoplasmConcept sctCon, Set<String> findingSites,
    List<String> results) throws Exception {
    List<String> otherOrUnspecifiedResult = new ArrayList<>();
    List<String> otherSpecifiedResult = new ArrayList<>();
    List<String> unspecifiedResult = new ArrayList<>();
    List<String> notUnspecifiedResults = new ArrayList<>();

    for (String result : results) {
      if (result.toLowerCase().contains("other or unspecified")) {
        otherOrUnspecifiedResult.add(result);
      } else if (result.toLowerCase().contains("other specified")) {
        otherSpecifiedResult.add(result);
      } else if (result.toLowerCase().contains("unspecified")) {
        unspecifiedResult.add(result);
      } else {
        notUnspecifiedResults.add(result);
      }
    }

    if (notUnspecifiedResults.size() == results.size()) {
      return null;
    }

    // ProcessOnlyUnspecified
    if (notUnspecifiedResults.isEmpty()) {
      if (otherOrUnspecifiedResult.size() == 1) {
        return otherOrUnspecifiedResult.iterator().next();
      } else if (otherSpecifiedResult.size() == 1) {
        return otherSpecifiedResult.iterator().next();
      }
    }

    // ProcessUnspecifiedHigherLevelThanSingleSpecific
    int lowestUnspecifiedLevel = 100;
    int lowestSpecificLevel = 100;
    for (String result : results) {
      int depth = Integer.parseInt(result.split("\t")[3]);

      if (notUnspecifiedResults.contains(result)) {
        if (depth <= lowestSpecificLevel) {
          lowestSpecificLevel = depth;
        }
      } else {
        if (depth <= lowestUnspecifiedLevel) {
          lowestUnspecifiedLevel = depth;
        }
      }
    }

    if (lowestSpecificLevel < lowestUnspecifiedLevel) {
      return processAllMatchingRules(notUnspecifiedResults, sctCon, findingSites);
    }

    return null;
  }

  private String processSingleResultInFindingSiteWord(Set<SctNeoplasmConcept> fsConcepts,
    SctNeoplasmConcept sctCon, Set<String> findingSites, List<String> results) {
    // Inverse... If word in single result exists in finding site , return it
    // i.e. SctId: 92637002
    String matchedResult = null;

    int matches = 0;
    for (String result : results) {
      String icd11String = result.split("\t")[1];
      String[] locationTokens = FieldedStringTokenizer.split(icd11String.toLowerCase(),
          " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

      for (int i = 0; i < locationTokens.length; i++) {
        if (!locationTokens[i].trim().isEmpty()
            && !fsUtility.getNonFindingSiteStrings().contains(locationTokens[i].toLowerCase())) {
          for (SctNeoplasmConcept con : fsConcepts) {
            boolean matchFound = false;
            for (SctNeoplasmDescription desc : con.getDescs()) {
              if (desc.getDescription().toLowerCase().contains(locationTokens[i].toLowerCase())) {
                matches++;
                matchedResult = result;
                matchFound = true;
                break;
              }
            }

            if (matchFound) {
              break;
            }
          }
        }
      }
    }

    if (matches == 1) {
      return matchedResult;
    }

    return null;
  }

  private String processFindingSiteWordInSingleResult(Set<SctNeoplasmConcept> fsConcepts,
    SctNeoplasmConcept sctCon, Set<String> findingSites, List<String> results) throws Exception {
    // If word in finding site exists in single result, return it
    // i.e. SctId: 92557009
    String matchedResult = null;

    for (SctNeoplasmConcept con : fsConcepts) {
      for (SctNeoplasmDescription desc : con.getDescs()) {
        String[] locationTokens = FieldedStringTokenizer.split(desc.getDescription().toLowerCase(),
            " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");

        for (int i = 0; i < locationTokens.length; i++) {
          if (!fsUtility.getNonFindingSiteStrings().contains(locationTokens[i].toLowerCase())) {
            int matches = 0;

            for (String result : results) {
              String icd11String = result.split("\t")[1];
              if (icd11String.toLowerCase().contains(locationTokens[i].toLowerCase())) {
                matches++;
                matchedResult = result;
              }
            }

            if (matches == 1) {
              return matchedResult;
            }
          }
        }
      }
    }
    return null;
  }

  private String processSkinMatchingRule(SctNeoplasmConcept sctCon, Set<String> findingSites,
    List<String> results) throws Exception {

    // If findingSite contains "skin" likely 2E64.Y Other specified carcinoma in
    // situ of skin
    // Unless finding site exists in other result
    // i.e. SctId: 92717008
    boolean isSkin = false;
    Set<SctNeoplasmConcept> fsCons = fsUtility.identifyPotentialFSConcepts(findingSites);
    for (SctNeoplasmConcept fsCon : fsCons) {
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

    // Identified Skin, return proper result
    if (isSkin) {
      for (SctNeoplasmDescription desc : sctCon.getDescs()) {
        if (desc.getSecondInfo() != null) {
          String[] locationTokens = desc.getSecondInfo().split(" ");

          for (int i = 0; i < locationTokens.length; i++) {
            if (!fsUtility.getNonFindingSiteStrings().contains(locationTokens[i].toLowerCase())) {
              for (String result : results) {
                String icd11String = result.split("\t")[1];
                if (icd11String.toLowerCase().contains(locationTokens[i].toLowerCase())) {
                  return result;
                }
              }
            }
          }
        }
      }

      for (String result : results) {
        if (result.contains("2E64.Y")) {
          return result;
        }
      }
    }
    return null;
  }
}