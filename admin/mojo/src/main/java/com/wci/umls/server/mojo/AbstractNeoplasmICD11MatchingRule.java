package com.wci.umls.server.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.NeoplasmConceptSearcher;
import com.wci.umls.server.rest.client.ContentClientRest;

public abstract class AbstractNeoplasmICD11MatchingRule {
  static protected ContentClientRest client;

  static protected String sourceTerminology;

  static protected String sourceVersion;

  static protected String targetTerminology;

  static protected String targetVersion;

  static protected String authToken;

  /** The pfs limitless. */
  static protected PfsParameterJpa pfsLimitless = new PfsParameterJpa();

  /** The pfs limited. */
  static protected PfsParameterJpa pfsLimited = new PfsParameterJpa();

  /** The pfs minimal. */
  static protected PfsParameterJpa pfsMinimal = new PfsParameterJpa();

  protected SearchResultList icd11Targets = new SearchResultListJpa();

  protected PrintWriter termWriter;

  protected PrintWriter devWriter;

  /** The tc input file path. */
  static final protected String tcInputFilePath = "src//main//resources//01312019 core transative closures.txt";

  /** The already looked up token cache. */
  protected Map<String, SearchResultList> findingSiteCache = new HashMap<>();

  /** The trans closure map. */
  static protected Map<String, Map<String, Integer>> transClosureMap = new HashMap<>();

  /** The inverse trans closure map. */
  static protected Map<String, Map<String, Integer>> inverseTransClosureMap = new HashMap<>();

  /** The already queried regexes cache. */
  // protected Map<String, Integer> alreadyQueriedRegexesCache = new
  // HashMap<>();
  // protected Map<String, Integer> alreadyQueriedRegexesDepthCache = new
  // HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedRegexesResultsCache =
      new HashMap<>();

  protected Map<String, HashMap<String, String>> alreadyQueriedServerResultsCache = new HashMap<>();

  private static final Map<String, String> sctIcdMismapExceptions = new HashMap<String, String>() {
    {
      put("tongue", "oral");
      put("nose", "respiratory");
      put("nasal", "respiratory");
      put("urinary", "bladder");
      put("lacrimal", "eye");
    };
  };

  static protected NeoplasmConceptSearcher conceptSearcher;

  static protected FindingSiteUtility fsUtility;

  /** The already queried server calls cache. */
  // protected Map<String, Integer> alreadyQueriedServerCallsCache = new
  // HashMap<>();

  static {
    pfsMinimal.setStartIndex(0);
    pfsMinimal.setMaxResults(5);
    pfsLimited.setStartIndex(0);
    pfsLimited.setMaxResults(30);

    BufferedReader reader;
    try {
      reader = new BufferedReader(new FileReader(tcInputFilePath));
      String line = reader.readLine(); // Don't want header
      line = reader.readLine();
      while (line != null) {
        String[] columns = line.split("\t");

        // Process Line
        if (!transClosureMap.containsKey(columns[0])) {
          HashMap<String, Integer> subMap = new HashMap<>();
          transClosureMap.put(columns[0], subMap);
        }
        transClosureMap.get(columns[0]).put(columns[1], Integer.parseInt(columns[2]));

        if (!inverseTransClosureMap.containsKey(columns[1])) {
          HashMap<String, Integer> subMap = new HashMap<>();
          inverseTransClosureMap.put(columns[1], subMap);
        }
        inverseTransClosureMap.get(columns[1]).put(columns[0], Integer.parseInt(columns[2]));

        line = reader.readLine();
      }

      reader.close();

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public AbstractNeoplasmICD11MatchingRule(ContentClientRest contentClient, String st, String sv,
      String tt, String tv, String token) {
    client = contentClient;
    sourceTerminology = st;
    sourceVersion = sv;
    targetTerminology = tt;
    targetVersion = tv;
    authToken = token;
  }

  abstract public String executeRule(SctNeoplasmConcept sctCon, Set<String> findingSites,
    int counter) throws Exception;

  abstract protected String getEclExpression();

  abstract protected void identifyIcd11Targets() throws Exception;

  abstract protected boolean isRuleMatch(SearchResult result);

  abstract protected SearchResultList testMatchingFindingSite(String queryPortion) throws Exception;

  abstract protected String getRule();

  protected void matchNextConcept(Set<String> findingSites, SctNeoplasmConcept sctCon,
    int counter) {
    StringBuffer newConInfoStr = createSnomedConceptSearchedLine(findingSites, sctCon, counter++);

    System.out.println(newConInfoStr);
    devWriter.println(newConInfoStr);
    termWriter.println(newConInfoStr);
  }

  /**
   * Construct con info str.
   *
   * @param findingSites the finding sites
   * @param sctCon the sct con
   * @param counter the counter
   * @return the string buffer
   */
  private StringBuffer createSnomedConceptSearchedLine(Set<String> findingSites,
    SctNeoplasmConcept sctCon, int counter) {

    StringBuffer newConInfoStr = new StringBuffer();
    newConInfoStr.append("\n#" + counter + " Snomed Concept: " + sctCon.getName() + "\tSctId: "
        + sctCon.getConceptId() + "\twith");
    int fsCounter = findingSites.size();
    for (String site : findingSites) {
      newConInfoStr.append(" findingSite: " + site);
      if (--fsCounter > 0) {
        newConInfoStr.append("\tand");
      }
    }
    return newConInfoStr;
  }

  public PrintWriter getDevWriter() {
    return devWriter;
  }

  public PrintWriter getTermWriter() {
    return termWriter;
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
  protected void identifyBestMatch(Map<String, Integer> matchDepthMap,
    Map<String, String> matchResultMap, int ruleNumber, StringBuffer str) {
    int lowestDepth = 10000;
    Set<String> lowestDepthStrings = new HashSet<>();
    // String resultString = "\n" + ruleNumber + "\t" + icd11Con.getCodeId() +
    // "\t"
    // + icd11Con.getValue() + "\t" + depth + "\t" + token + "\t" +
    // icd11Con.getScore();

    for (String icdConId : matchDepthMap.keySet()) {
      if (matchDepthMap.get(icdConId) < lowestDepth) {
        lowestDepthStrings.clear();
        lowestDepthStrings.add(
            "\n" + ruleNumber + matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
        lowestDepth = matchDepthMap.get(icdConId);
      } else if (matchDepthMap.get(icdConId) == lowestDepth) {
        lowestDepthStrings.add(
            "\n" + ruleNumber + matchResultMap.get(icdConId) + "\t" + matchDepthMap.get(icdConId));
      }
    }

    // System.out.println("\n\nBut actually outputing:");
    for (String s : lowestDepthStrings) {
      // System.out.println(s);
      str.append(s);
    }
  }

  /**
   * Take a description and split it based on any number of stop-characters.
   *
   * @param str the str
   * @return the sets the
   */
  private Set<String> splitTokens(String str) {
    String[] splitString =
        FieldedStringTokenizer.split(str.toLowerCase(), " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
    Set<String> retStrings = new HashSet<>();

    Set<String> tokensToAdd = new HashSet<>();
    for (int i = 0; i < splitString.length; i++) {
      if (!splitString[i].trim().isEmpty() && splitString[i].trim().length() != 1) {

        if (sctIcdMismapExceptions.keySet().contains(splitString[i].trim().toLowerCase())) {
          tokensToAdd.add(sctIcdMismapExceptions.get(splitString[i].trim().toLowerCase()));
        }
        retStrings.add(splitString[i].trim());
      }
    }

    retStrings.addAll(tokensToAdd);
    return retStrings;
  }

  /**
   * Match approach 1.
   *
   * @param findingSites the finding sites
   * @param icd11Targets the icd 11 targets
   * @param str the str
   * @return true, if successful
   * @throws Exception the exception
   */
  protected void matchApproach1(Set<String> findingSites, StringBuffer str) throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    for (String site : findingSites) {
      identifyMatchesByToken(site, matchResultMap, matchDepthMap, lowestDepthMap, 0);
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 1111, str);
    }
  }

  protected void matchApproach2(Set<String> findingSites, StringBuffer str) throws Exception {
    Map<String, String> matchResultMap = new HashMap<>();
    Map<String, Integer> matchDepthMap = new HashMap<>();
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    for (String site : findingSites) {
      SctNeoplasmConcept fsConcept = conceptSearcher.getSctConceptFromDesc(site + "");

      identifyMatchesByConceptDescriptions(fsConcept, matchResultMap, matchDepthMap, lowestDepthMap,
          0);
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 2222, str);
    }
  }

  private void identifyMatchesByConceptDescriptions(SctNeoplasmConcept fsConcept,
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

  private void identifyMatchesByToken(String site, Map<String, String> matchMap,
    Map<String, Integer> matchDepthMap, Map<String, Integer> lowestDepthMap, int depth) {

    Set<String> tokens = splitTokens(site);

    for (String token : tokens) {
      if (!fsUtility.getNonFindingSiteStrings().contains(token)) {
        if (!alreadyQueriedRegexesResultsCache.keySet().contains(token)) {
          alreadyQueriedRegexesResultsCache.put(token, new HashMap<String, String>());

          for (SearchResult icd11Con : icd11Targets.getObjects()) {
            if (icd11Con.getValue().toLowerCase().matches(".*\\b" + token + "\\b.*")) {

              String resultString = "\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t"
                  + token + "\t" + icd11Con.getScore();

              // System.out.println(outputString);
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

  /**
   * Match approach 3.
   *
   * @param fsConcepts the fs concepts
   * @param icd11Targets the icd 11 targets
   * @param str the str
   * @return true, if successful
   */
  protected void matchApproach3(Set<SctNeoplasmConcept> fsConcepts, StringBuffer str) {
    // icdTarget to map of depth-to-output
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    // icdTarget to map of depth-to-output
    Map<String, String> matchResultMap = new HashMap<>();

    Map<String, Integer> matchDepthMap = new HashMap<>();

    for (SctNeoplasmConcept fsCon : fsConcepts) {
      String fsConId = fsCon.getConceptId();

      // Testing on ancestors of findingSite fsConId
      Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
      Map<SctNeoplasmConcept, Set<String>> potentialFSConTerms =
          fsUtility.getFindingSitePotentialTermsMapCache().get(fsConId);

      for (SctNeoplasmConcept testCon : potentialFSConTerms.keySet()) {
        Set<String> normalizedStrs = potentialFSConTerms.get(testCon);
        int depth = depthMap.get(testCon.getConceptId());

        for (String normalizedStr : normalizedStrs) {
          identifyMatchesByToken(normalizedStr, matchResultMap, matchDepthMap, lowestDepthMap,
              depth);
        }
      }
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 3333, str);
    }
  }

  /**
   * Match approach 4.
   *
   * @param fsConcepts the fs concepts
   * @param str the str
   * @return true, if successful
   * @throws Exception the exception
   */
  protected void matchApproach4(Set<SctNeoplasmConcept> fsConcepts, StringBuffer str)
    throws Exception {
    // icdTarget to map of depth-to-output
    Map<String, Integer> lowestDepthMap = new HashMap<>();
    // icdTarget to map of depth-to-output
    Map<String, String> matchResultMap = new HashMap<>();

    Map<String, Integer> matchDepthMap = new HashMap<>();

    for (SctNeoplasmConcept fsCon : fsConcepts) {
      String fsConId = fsCon.getConceptId();

      // Testing on ancestors of findingSite fsConId
      Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
      Map<SctNeoplasmConcept, Set<String>> potentialFSConTerms =
          fsUtility.getFindingSitePotentialTermsMapCache().get(fsConId);

      for (SctNeoplasmConcept fsConcept : potentialFSConTerms.keySet()) {
        int depth = depthMap.get(fsConcept.getConceptId());

        identifyMatchesByConceptDescriptions(fsConcept, matchResultMap, matchDepthMap,
            lowestDepthMap, depth);

      }
    }

    if (!matchResultMap.isEmpty()) {
      identifyBestMatch(matchDepthMap, matchResultMap, 4444, str);
    }
  }

  public void setDevWriter(PrintWriter writer) {
    devWriter = writer;
  }

  public void setTermWriter(PrintWriter writer) {
    termWriter = writer;
  }

  public static void setConceptSearcher(NeoplasmConceptSearcher searcher) {
    conceptSearcher = searcher;
  }

  public static void setFindingSiteUtility(FindingSiteUtility findingSiteUtility) {
    fsUtility = findingSiteUtility;
  }

  abstract public String getDefaultTarget();

  abstract protected Map<String, SctNeoplasmConcept> getConceptMap();
}