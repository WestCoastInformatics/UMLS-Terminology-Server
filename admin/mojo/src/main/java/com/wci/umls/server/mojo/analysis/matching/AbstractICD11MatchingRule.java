package com.wci.umls.server.mojo.analysis.matching;

import java.io.BufferedReader;
import java.io.File;
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
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.ICD11MatcherConceptSearcher;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;

public abstract class AbstractICD11MatchingRule {
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

  /** The trans closure map. */
  protected static Map<String, Map<String, Integer>> transClosureMap = new HashMap<>();

  /** The inverse trans closure map. */
  static protected Map<String, Map<String, Integer>> inverseTransClosureMap = new HashMap<>();

  /** The tc input file path. */
  static final protected String tcInputFilePath =
      "src//main//resources//01312019 core transative closures.txt";

  public static Set<String> topLevelConcepts = new HashSet<>();

  static protected ICD11MatcherConceptSearcher conceptSearcher;

  static protected FindingSiteUtility fsUtility;

  static {
    pfsMinimal.setStartIndex(0);
    pfsMinimal.setMaxResults(5);
    pfsLimited.setStartIndex(0);
    pfsLimited.setMaxResults(10);
  }

  abstract protected StringBuffer createSnomedConceptSearchedLine(ICD11MatcherSctConcept sctCon,
    int counter);

  abstract protected SctICD11SynonymProvider getSynonymProvider();

  abstract public boolean executeContentParsers(String MATCHER_NAME,
    SctNeoplasmDescriptionParser descParser, SctRelationshipParser relParser) throws IOException;

  public static void initializeTcTable() throws IOException {
    BufferedReader reader;
    topLevelConcepts.add(ICD11MatcherConstants.SNOMED_ROOT_CONCEPT);
    System.out
        .println("About to read in the Trans Closure file. This will take about 30 seconds...");

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

      // Include top level concepts as well as children of body strcture to
      // obtain better results
      if ((columns[0].equals(ICD11MatcherConstants.SNOMED_ROOT_CONCEPT)
          || (columns[0].equals(ICD11MatcherConstants.BODY_STRUCTURE))
          || (columns[0].equals(ICD11MatcherConstants.SEX_STRUCTURE_CONCEPT)))
          && Integer.parseInt(columns[2]) == 1) {
        topLevelConcepts.add(columns[1]);
      }

      if (!inverseTransClosureMap.containsKey(columns[1])) {
        HashMap<String, Integer> subMap = new HashMap<>();
        inverseTransClosureMap.put(columns[1], subMap);
      }
      inverseTransClosureMap.get(columns[1]).put(columns[0], Integer.parseInt(columns[2]));

      line = reader.readLine();
    }

    reader.close();
    System.out.println("Completed reading the Trans Closure file.");
  }

  abstract public String getDefaultTarget();

  abstract public Map<String, ICD11MatcherSctConcept> getConceptMap();

  public abstract String getDescription();

  abstract public String getEclExpression();

  abstract public String getDefaultSkinMatch();

  /**
   * Indicates whether or not neoplasm match is the case.
   *
   * @param result the result
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  abstract protected boolean isRuleMatch(SearchResult result);

  abstract public String getRuleId();

  abstract protected ICD11MatcherSctConcept getTopLevelConcept();

  public abstract Set<String> executeRule(ICD11MatcherSctConcept sctCon, int counter)
    throws Exception;

  abstract public void preTermProcessing(ICD11MatcherSctConcept sctCon) throws Exception;

  abstract protected String getRuleQueryString();

  abstract protected Set<String> getRuleBasedNonMatchTerms();

  protected String getEclTopLevelDesc() {
    return null;
  }

  public AbstractICD11MatchingRule(ContentClientRest contentClient, String st, String sv, String tt,
      String tv, String token) {
    client = contentClient;
    sourceTerminology = st;
    sourceVersion = sv;
    targetTerminology = tt;
    targetVersion = tv;
    authToken = token;
  }

  public void identifyIcd11Targets() throws Exception {
    if (getRuleQueryString() != null) {
      final SearchResultList fullStringResults = client.findConcepts(targetTerminology,
          targetVersion, getRuleQueryString(), pfsLimitless, authToken);
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
    } else {
      SearchResult topLevelEclConcept = null;

      final SearchResultList fullStringResults = client.findConcepts(targetTerminology,
          targetVersion, getEclTopLevelDesc(), pfsMinimal, authToken);

      for (SearchResult result : fullStringResults.getObjects()) {
        if (result.isLeafNode() && !result.isObsolete()
            && result.getValue().equals(getEclTopLevelDesc())) {
          topLevelEclConcept = result;
          break;
        }
      }

      if (topLevelEclConcept == null) {
        throw new Exception("Couldn't match ECL Concept: " + getEclTopLevelDesc());
      }

      PfsParameterJpa pfsEcl = new PfsParameterJpa();
      // Make ECL Query
      pfsEcl.setExpression("<< " + topLevelEclConcept.getTerminologyId());

      final SearchResultList eclResults =
          client.findConcepts(targetTerminology, targetVersion, null, pfsEcl, authToken);
      System.out.println("With ICD11 ECL, have: " + eclResults.getObjects().size());

      for (SearchResult result : eclResults.getObjects()) {
        // Get Desc
        icd11Targets.getObjects().add(result);
        icd11Targets.setTotalCount(icd11Targets.getTotalCount() + 1);
      }
    }
  }

  protected boolean printIcd11Targets() {
    return false;
  }

  /**
   * Take a description and split it based on any number of stop-characters.
   *
   * @param str the str
   * @return the sets the
   */
  protected Set<String> splitTokens(String str) {
    String[] splitString =
        FieldedStringTokenizer.split(str.toLowerCase(), " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
    Set<String> retStrings = new HashSet<>();

    Set<String> tokensToAdd = new HashSet<>();
    for (int i = 0; i < splitString.length; i++) {
      if (!splitString[i].trim().isEmpty() && splitString[i].trim().length() != 1) {
        retStrings.add(splitString[i].trim());
      }
    }

    retStrings.addAll(tokensToAdd);
    return retStrings;
  }

  protected void matchNextConcept(ICD11MatcherSctConcept sctCon, int counter) {
    StringBuffer newConInfoStr = createSnomedConceptSearchedLine(sctCon, counter++);

    System.out.println(newConInfoStr);
    devWriter.println(newConInfoStr);
    termWriter.println(newConInfoStr);
  }

  public void setDevWriter(PrintWriter writer) {
    devWriter = writer;
  }

  public void setTermWriter(PrintWriter writer) {
    termWriter = writer;
  }

  public static void setConceptSearcher(ICD11MatcherConceptSearcher searcher) {
    conceptSearcher = searcher;
  }

  public PrintWriter getDevWriter() {
    return devWriter;
  }

  public PrintWriter getTermWriter() {
    return termWriter;
  }

  public int getTargetSize() {
    return icd11Targets.size();
  }

  public String getRulePath(String matcherName) {
    return "results" + File.separator + matcherName + File.separator + getRuleId();
  }

  protected String cleanDescription(String origString, Set<String> ruleBasedTerms) {
    String desc = origString.toLowerCase();
    Set<String> termsToRemove = new HashSet<String>(ICD11MatcherConstants.NON_MATCHING_TERMS);
    termsToRemove.addAll(ruleBasedTerms);

    for (String key : termsToRemove) {
      if (desc.matches(".*\\b" + key + "es\\b.*")) {
        desc = desc.replaceAll(key + "es", "");
      }
      if (desc.matches(".*\\b" + key + "s\\b.*")) {
        desc = desc.replaceAll(key + "s", "");
      }
      if (desc.matches(".*\\b" + key + "\\b.*")) {
        desc = desc.replaceAll(key, "");
      }

      desc = desc.replaceAll(" {2,}", " ");
    }

    return desc.trim();
  }

  protected Set<String> createICD11SearchStrings(ICD11MatcherSctConcept sctCon) {
    Set<String> descsToProcess = new HashSet<>();

    for (SctNeoplasmDescription fullDesc : sctCon.getDescs()) {
      String desc = "";
      String[] splitString = FieldedStringTokenizer.split(fullDesc.getDescription().toLowerCase(),
          " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
      for (int i = 0; i < splitString.length; i++) {
        desc += splitString[i] + " ";
      }

      desc = cleanDescription(desc.toLowerCase().trim(), getRuleBasedNonMatchTerms());

      descsToProcess.addAll(getSynonymProvider().identifyReplacements(desc));
    }

    return descsToProcess;
  }

  public SearchResultList getIcd11Concepts() {
    return icd11Targets;
  }

}
