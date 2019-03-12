package com.wci.umls.server.mojo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.model.SctRelationship;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;

public abstract class AbstractNeoplasmICD11MatchingRule {
  static protected ContentClientRest client;

  static protected String sourceTerminology;

  static protected String sourceVersion;

  static protected String targetTerminology;

  static protected String targetVersion;

  static protected String authToken;

  static private SctRelationshipParser relParser;

  static private SctNeoplasmDescriptionParser descParser;

  /** The pfs limitless. */
  static protected PfsParameterJpa pfsLimitless = new PfsParameterJpa();

  /** The pfs limited. */
  static protected PfsParameterJpa pfsLimited = new PfsParameterJpa();

  /** The pfs minimal. */
  static protected PfsParameterJpa pfsMinimal = new PfsParameterJpa();

  protected SearchResultList icd11Targets = new SearchResultListJpa();

  protected PrintWriter termWriter;

  protected PrintWriter devWriter;

  public static boolean canPopulateFromFiles = false;

  /** The tc input file path. */
  static final protected String tcInputFilePath =
      "C:\\Code\\wci\\myTransClosureFile.csv";

  /** The already looked up token cache. */
  protected Map<String, SearchResultList> alreadyLookedUpTokenCache =
      new HashMap<>();

  /** The non finding site strings. */
  protected List<String> nonFindingSiteStrings = Arrays.asList("of", "part",
      "structure", "system", "and/or", "and", "region", "area", "or", "the");

  /** The top level body structure ids. */
  protected List<String> topLevelBodyStructureIds =
      Arrays.asList("86762007", "20139000", "39937001", "81745001", "387910009",
          "127882003", "64033007", "117590005", "21514008", "76752008",
          "113331007", "363667005", "31610004");

  /** The trans closure map. */
  static protected Map<String, Map<String, Integer>> transClosureMap =
      new HashMap<>();

  /** The inverse trans closure map. */
  static protected Map<String, Map<String, Integer>> inverseTransClosureMap =
      new HashMap<>();

  /** The finding site potential terms map cache. */
  protected Map<String, Map<SctNeoplasmConcept, Set<String>>> findingSitePotentialTermsMapCache =
      new HashMap<>();

  /** The already queried regexes cache. */
  protected Map<String, Integer> alreadyQueriedRegexesCache = new HashMap<>();

  /** The already queried server calls cache. */
  protected Map<String, Integer> alreadyQueriedServerCallsCache =
      new HashMap<>();

  /** The acronym expansion map. */
  private HashMap<String, SctNeoplasmConcept> conceptsFromDescsCache =
      new HashMap<>();

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
        transClosureMap.get(columns[0]).put(columns[1],
            Integer.parseInt(columns[2]));

        if (!inverseTransClosureMap.containsKey(columns[1])) {
          HashMap<String, Integer> subMap = new HashMap<>();
          inverseTransClosureMap.put(columns[1], subMap);
        }
        inverseTransClosureMap.get(columns[1]).put(columns[0],
            Integer.parseInt(columns[2]));

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

  public AbstractNeoplasmICD11MatchingRule(ContentClientRest client, String st,
      String sv, String tt, String tv, String authToken) {
    AbstractNeoplasmICD11MatchingRule.client = client;
    AbstractNeoplasmICD11MatchingRule.sourceTerminology = st;
    AbstractNeoplasmICD11MatchingRule.sourceVersion = sv;
    AbstractNeoplasmICD11MatchingRule.targetTerminology = tt;
    AbstractNeoplasmICD11MatchingRule.targetVersion = tv;
    AbstractNeoplasmICD11MatchingRule.authToken = authToken;
  }

  public static void setDescParser(SctNeoplasmDescriptionParser dp) {
    descParser = dp;
  }

  public static void setRelParser(SctRelationshipParser rp) {
    relParser = rp;
  }

  abstract public String executeRule(SctNeoplasmConcept sctCon,
    Set<String> findingSites, int counter) throws Exception;

  abstract protected String getEclExpression();

  abstract protected void identifyIcd11Targets() throws Exception;

  abstract protected boolean isRuleMatch(SearchResult result);

  abstract protected SearchResultList testMatchingFindingSite(
    String queryPortion) throws Exception;

  abstract protected String getRule();

  public void setDevWriter(PrintWriter writer) {
    devWriter = writer;
  }

  public void setTermWriter(PrintWriter writer) {
    termWriter = writer;
  }

  protected void matchNextConcept(Set<String> findingSites,
    SctNeoplasmConcept sctCon, int counter) {
    StringBuffer newConInfoStr =
        createSnomedConceptSearchedLine(findingSites, sctCon, counter++);

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
    newConInfoStr.append("\n# " + counter + " Snomed Concept: "
        + sctCon.getName() + "\tSctId: " + sctCon.getConceptId() + "\twith");
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
   * @param lowestDepthMap the lowest depth map
   * @param matchMap the match map
   * @param str the str
   */
  protected void identifyBestMatch(Map<String, Integer> lowestDepthMap,
    Map<String, String> matchMap, StringBuffer str) {
    int lowestDepth = 10000;
    Set<String> lowestDepthStrings = new HashSet<>();

    for (String icdConId : lowestDepthMap.keySet()) {
      if (lowestDepthMap.get(icdConId) < lowestDepth) {
        lowestDepthStrings.clear();
        lowestDepthStrings.add(matchMap.get(icdConId));
        lowestDepth = lowestDepthMap.get(icdConId);
      } else if (lowestDepthMap.get(icdConId) == lowestDepth) {
        lowestDepthStrings.add(matchMap.get(icdConId));
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
  protected Set<String> splitTokens(String str) {
    String[] splitString = FieldedStringTokenizer.split(str.toLowerCase(),
        " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
    Set<String> retStrings = new HashSet<>();

    for (int i = 0; i < splitString.length; i++) {
      if (!splitString[i].trim().isEmpty()
          && splitString[i].trim().length() != 1) {
        retStrings.add(splitString[i].trim());
      }
    }

    return retStrings;
  }

  /**
   * Identifies the concept based on a provided description.
   *
   * @param desc the desc
   * @return the sct concept from desc
   * @throws Exception the exception
   */
  protected SctNeoplasmConcept getSctConceptFromDesc(String desc)
    throws Exception {
    if (canPopulateFromFiles) {
      String conId = descParser.getConIdFromDesc(desc);
      return populateSctConcept(conId, desc);
    } else {
      if (!conceptsFromDescsCache.containsKey(desc)) {
        final SearchResultList possibleMatches =
            client.findConcepts(sourceTerminology, sourceVersion,
                "\"" + desc + "\"", pfsMinimal, authToken);

        for (SearchResult result : possibleMatches.getObjects()) {
          if (!result.isObsolete()) {
            SctNeoplasmConcept retConcept = populateSctConcept(
                result.getTerminologyId(), result.getValue());
            conceptsFromDescsCache.put(desc, retConcept);
          }
        }
      }
    }

    return conceptsFromDescsCache.get(desc);
  }

  /**
   * Based on a finding site, identify all the finding site's ancestors up to
   * levels specified by topLevelBodyStructureIds.
   *
   * @param findingSites the finding sites
   * @return the sets the
   * @throws Exception the exception
   */
  protected Set<SctNeoplasmConcept> identifyPotentialFSConcepts(
    Set<String> findingSites) throws Exception {
    Set<SctNeoplasmConcept> retConcepts = new HashSet<>();

    for (String site : findingSites) {
      // Get the finding site as a concept
      SctNeoplasmConcept fsConcept = getSctConceptFromDesc(site);
      retConcepts.add(fsConcept);

      if (findingSitePotentialTermsMapCache
          .containsKey(fsConcept.getConceptId())) {
        return retConcepts;
      }

      Map<SctNeoplasmConcept, Set<String>> potentialFSConTerms =
          new HashMap<>();
      findingSitePotentialTermsMapCache.put(fsConcept.getConceptId(),
          potentialFSConTerms);

      if (topLevelBodyStructureIds.contains(fsConcept.getConceptId())) {
        SctNeoplasmConcept mapCon = null;
        if (canPopulateFromFiles) {
          mapCon = populateSctConcept(fsConcept.getConceptId(), null);
        } else {
          Concept c = client.getConcept(fsConcept.getConceptId(),
              sourceTerminology, sourceVersion, null, authToken);
          mapCon = populateSctConcept(c.getTerminologyId(), null);
        }

        Set<String> bucket = new HashSet<>();
        potentialFSConTerms.put(mapCon, bucket);
      } else {
        // Get all fsCon's ancestors
        String topLevelSctId = null;
        final ConceptList ancestorResults = client.findAncestorConcepts(
            fsConcept.getConceptId(), sourceTerminology, sourceVersion, false,
            pfsLimitless, authToken);

        // Find the body structure hierarchy it falls under
        for (Concept ancestor : ancestorResults.getObjects()) {
          if (topLevelBodyStructureIds.contains(ancestor.getTerminologyId())) {
            topLevelSctId = ancestor.getTerminologyId();
            break;
          }
        }

        // Have list of possibleFindingSites. Test them for matches
        if (topLevelSctId == null) {
          System.out.println(
              "ERROR ERROR ERROR: Found a finding site without an identified top level BS ancestor: "
                  + fsConcept.getConceptId() + "---" + fsConcept.getName());
          return null;
        }

        // TODO: Because can't do ancestors via ECL, need this work around
        // Identify all descendants of top level bodyStructure concept
        pfsLimitless.setExpression("<< " + topLevelSctId);
        final SearchResultList descendentResults = client.findConcepts(
            sourceTerminology, sourceVersion, null, pfsLimitless, authToken);
        pfsLimitless.setExpression(null);

        // Create a list of concepts that are both ancestors of fsConcept and
        // descendents of topLevelBodyStructure Concept
        // TODO: This could be a Rest Call in of itself
        for (Concept ancestor : ancestorResults.getObjects()) {

          for (SearchResult potentialFindingSite : descendentResults
              .getObjects()) {
            if (ancestor.getTerminologyId()
                .equals(potentialFindingSite.getTerminologyId())) {
              SctNeoplasmConcept mapCon = null;
              if (canPopulateFromFiles) {
                mapCon = populateSctConcept(ancestor.getTerminologyId(), null);
              } else {
                Concept c = client.getConcept(ancestor.getTerminologyId(),
                    sourceTerminology, sourceVersion, null, authToken);
                mapCon = populateSctConcept(c.getTerminologyId(), null);
              }

              Set<String> bucket = new HashSet<>();
              potentialFSConTerms.put(mapCon, bucket);
              break;
            }
          }
        }
      }

      for (SctNeoplasmConcept testCon : potentialFSConTerms.keySet()) {
        for (SctNeoplasmDescription desc : testCon.getDescs()) {
          String normalizedStr = desc.getDescription().toLowerCase();
          for (String s : nonFindingSiteStrings) {
            normalizedStr =
                normalizedStr.replaceAll("\\b" + s + "s" + "\\b", " ").trim();
            normalizedStr =
                normalizedStr.replaceAll("\\b" + s + "\\b", " ").trim();
          }

          normalizedStr = normalizedStr.replaceAll(" {2,}", " ").trim();

          if (!potentialFSConTerms.get(testCon).contains(normalizedStr)) {
            potentialFSConTerms.get(testCon).add(normalizedStr);
          }
        }
      }
    }

    return retConcepts;
  }

  /**
   * Populate neoplasm sct concept.
   *
   * @param result the result
   * @return the sct neoplasm concept
   * @throws Exception the exception
   */
  protected SctNeoplasmConcept populateSctConcept(String conId, String name)
    throws Exception {
    SctNeoplasmConcept con = new SctNeoplasmConcept(conId, name);
    populateRelationships(con);
    populateDescriptions(con);

    if (name == null) {
      con.setName(con.getDescs().iterator().next().getDescription());
    }

    return con;
  }

  /**
   * Populate neoplasm relationships.
   *
   * @param con the con
   * @throws Exception the exception
   */
  protected static void populateRelationships(SctNeoplasmConcept con)
    throws Exception {

    if (canPopulateFromFiles) {
      con.setRels(relParser.getRelationships(con));
    } else {
      RelationshipList relsList =
          client.findConceptRelationships(con.getConceptId(), sourceTerminology,
              sourceVersion, null, pfsLimitless, authToken);

      for (final Relationship<?, ?> relResult : relsList.getObjects()) {
        SctRelationship rel = relParser.parse(con.getName(), relResult);
        if (rel != null) {
          con.getRels().add(rel);
        }
      }
    }
  }

  /**
   * Populate neoplasm descriptions.
   *
   * @param con the con
   * @throws Exception the exception
   */
  protected void populateDescriptions(SctNeoplasmConcept con) throws Exception {
    if (canPopulateFromFiles) {
      con.setDescs(descParser.getDescriptions(con));
    } else {
      Concept fullCon = client.getConcept(con.getConceptId(), sourceTerminology,
          sourceVersion, null, authToken);

      for (final Atom atom : fullCon.getAtoms()) {
        if (isValidDescription(atom)) {
          SctNeoplasmDescription desc = new SctNeoplasmDescription();
          desc.setDescription(atom.getName());
          con.getDescs().add(desc);
        }
      }
    }
  }

  /**
   * Indicates whether or not valid description for analysis purposes.
   *
   * @param atom the atom
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  static protected boolean isValidDescription(Atom atom) {
    return (!atom.isObsolete()
        && !atom.getTermType().equals("Fully specified name")
        && !atom.getTermType().equals("Definition"));
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
  protected boolean matchApproach1(Set<String> findingSites,
    SearchResultList icd11Targets, StringBuffer str) throws Exception {
    boolean foundMatch = false;

    for (String site : findingSites) {
      Set<String> tokens = splitTokens(site);
      for (String token : tokens) {
        if (!alreadyQueriedRegexesCache.keySet().contains(token)
            && !nonFindingSiteStrings.contains(token)) {
          alreadyQueriedRegexesCache.put(token, 0);

          for (SearchResult icd11Con : icd11Targets.getObjects()) {
            if (icd11Con.getValue().toLowerCase()
                .matches(".*\\b" + token + "\\b.*")) {
              str.append("\n111\t" + icd11Con.getCodeId() + "\t"
                  + icd11Con.getValue() + "\t0\t" + token);
              foundMatch = true;
            }
          }
        }
      }
    }

    return foundMatch;
  }

  /**
   * Match approach 2.
   *
   * @param findingSites the finding sites
   * @param str the str
   * @return true, if successful
   * @throws Exception the exception
   */
  protected boolean matchApproach2(Set<String> findingSites, StringBuffer str)
    throws Exception {
    boolean matchFound = false;

    for (String site : findingSites) {
      SctNeoplasmConcept fsConcept = getSctConceptFromDesc(site);

      for (SctNeoplasmDescription desc : fsConcept.getDescs()) {
        if (!alreadyQueriedServerCallsCache.keySet()
            .contains(desc.getDescription())) {
          alreadyQueriedServerCallsCache.put(desc.getDescription(), 0);

          SearchResultList results =
              testMatchingFindingSite(desc.getDescription());
          for (SearchResult result : results.getObjects()) {
            if (isRuleMatch(result)) {
              str.append("\n2222\t" + result.getCodeId() + "\t"
                  + result.getValue() + "\t0\t" + desc.getDescription());
              matchFound = true;
            }
          }
        }
      }
    }

    return matchFound;
  }

  /**
   * Match approach 3.
   *
   * @param fsConcepts the fs concepts
   * @param icd11Targets the icd 11 targets
   * @param str the str
   * @return true, if successful
   */
  protected boolean matchApproach3(Set<SctNeoplasmConcept> fsConcepts,
    SearchResultList icd11Targets, StringBuffer str) {
    Map<String, Integer> lowestDepthMap = new HashMap<>(); // icdTarget to map
                                                           // of depth-to-output
    Map<String, String> matchMap = new HashMap<>(); // icdTarget to map of
                                                    // depth-to-output

    for (SctNeoplasmConcept fsCon : fsConcepts) {
      String fsConId = fsCon.getConceptId();

      // Testing on ancestors of findingSite fsConId
      Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
      Map<SctNeoplasmConcept, Set<String>> potentialFSConTerms =
          findingSitePotentialTermsMapCache.get(fsConId);

      for (SctNeoplasmConcept testCon : potentialFSConTerms.keySet()) {
        Set<String> normalizedStrs = potentialFSConTerms.get(testCon);
        int depth = depthMap.get(testCon.getConceptId());

        for (String normalizedStr : normalizedStrs) {
          Set<String> tokens = splitTokens(normalizedStr);

          for (String token : tokens) {
            if (!nonFindingSiteStrings.contains(token)) {
              if (!alreadyQueriedRegexesCache.keySet().contains(token)
                  || (depth < alreadyQueriedRegexesCache.get(token))) {
                alreadyQueriedRegexesCache.put(token, depth);

                for (SearchResult icd11Con : icd11Targets.getObjects()) {
                  if (icd11Con.getValue().toLowerCase()
                      .matches(".*\\b" + token + "\\b.*")) {
                    String outputString = "\n3333\t" + icd11Con.getCodeId()
                        + "\t" + icd11Con.getValue() + "\t" + depth + "\t"
                        + token + "\t" + icd11Con.getScore();

                    // System.out.println(outputString);
                    if (!lowestDepthMap.containsKey(icd11Con.getCodeId())
                        || depth < lowestDepthMap.get(icd11Con.getCodeId())) {
                      lowestDepthMap.put(icd11Con.getCodeId(), depth);
                      matchMap.put(icd11Con.getCodeId(), outputString);
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (!matchMap.isEmpty()) {
      identifyBestMatch(lowestDepthMap, matchMap, str);
      return true;
    }

    return false;
  }

  /**
   * Match approach 4.
   *
   * @param fsConcepts the fs concepts
   * @param str the str
   * @return true, if successful
   * @throws Exception the exception
   */
  protected boolean matchApproach4(Set<SctNeoplasmConcept> fsConcepts,
    StringBuffer str) throws Exception {
    // icdTarget to map of depth-to-output
    Map<String, Integer> lowestDepthMap = new HashMap<>();

    // icdTarget to map of depth-to-output
    Map<String, String> matchMap = new HashMap<>();

    for (SctNeoplasmConcept fsCon : fsConcepts) {
      String fsConId = fsCon.getConceptId();

      // Testing on ancestors of findingSite fsConId
      Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
      Map<SctNeoplasmConcept, Set<String>> potentialFSConTerms =
          findingSitePotentialTermsMapCache.get(fsConId);

      for (SctNeoplasmConcept testCon : potentialFSConTerms.keySet()) {
        Set<String> normalizedStrs = potentialFSConTerms.get(testCon);
        int depth = depthMap.get(testCon.getConceptId());

        for (String normalizedStr : normalizedStrs) {
          if (!alreadyQueriedServerCallsCache.keySet().contains(normalizedStr)
              || (depth < alreadyQueriedServerCallsCache.get(normalizedStr))) {
            alreadyQueriedServerCallsCache.put(normalizedStr, depth);

            SearchResultList results = testMatchingFindingSite(normalizedStr);
            for (SearchResult result : results.getObjects()) {
              if (isRuleMatch(result)) {
                String outputString = "\n4444\t" + result.getCodeId() + "\t"
                    + result.getValue() + "\t" + depth + "\t" + normalizedStr
                    + "\t" + result.getScore();

                // System.out.println(outputString);
                if (!lowestDepthMap.containsKey(result.getCodeId())
                    || depth < lowestDepthMap.get(result.getCodeId())) {
                  lowestDepthMap.put(result.getCodeId(), depth);
                  matchMap.put(result.getCodeId(), outputString);
                }
              }
            }
          }
        }
      }
    }

    if (!matchMap.isEmpty()) {
      identifyBestMatch(lowestDepthMap, matchMap, str);
      return true;
    }

    return false;
  }
}