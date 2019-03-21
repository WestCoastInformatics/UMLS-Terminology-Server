package com.wci.umls.server.mojo.analysis.matching.rules;

import java.io.BufferedReader;
import java.io.File;
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
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.ICD11MatcherConceptSearcher;
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

  static final protected String synonymsInputFilePath = "src//main//resources//synonyms.txt";

  public static Set<String> topLevelConcepts = new HashSet<>();

  final static protected List<String> nonMatchingStrings = Arrays.asList("of", "part", "structure",
      "system", "and/or", "and", "region", "area", "or", "the", "in", "cavity", "tract", "organ",
      "duct", "canal", "genitalia", "genital", "adnexa", "due", "to", "disease", "by", "caused", "left", "right");// ,
                                                                          // "male",
                                                                          // "female");

  public static final String SNOMED_ROOT_CONCEPT = "138875005";

  public static final Map<String, String> snomedToIcdSynonymMap = new HashMap<String, String>();

  private static final int ICD_COLUMN = 0;

  private static final int SNOMED_COLUMN = 1;

  static protected ICD11MatcherConceptSearcher conceptSearcher;

  static protected FindingSiteUtility fsUtility;

  static {
    pfsMinimal.setStartIndex(0);
    pfsMinimal.setMaxResults(5);
    pfsLimited.setStartIndex(0);
    pfsLimited.setMaxResults(10);

    BufferedReader reader;
    topLevelConcepts.add(SNOMED_ROOT_CONCEPT);
    System.out.println("About to read in the Trans Closure file. This will take some time...");

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

        if (columns[0].equals(SNOMED_ROOT_CONCEPT) && Integer.parseInt(columns[2]) == 1) {
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

      reader = new BufferedReader(new FileReader(synonymsInputFilePath));
      line = reader.readLine(); // Don't want header
      line = reader.readLine();
      while (line != null) {
        String[] columns = line.trim().split("\\|");

        String icdStr = columns[ICD_COLUMN].trim();
        String snomedStr = columns[SNOMED_COLUMN].trim();

        if (icdStr.startsWith("#")) {
          icdStr = icdStr.substring(1);
        }
        snomedToIcdSynonymMap.put(snomedStr, icdStr);

        line = reader.readLine();
      }

      snomedToIcdSynonymMap.put("female mammary", "mammary");
      snomedToIcdSynonymMap.put("female breast", "breast");
      snomedToIcdSynonymMap.put("male mammary", "mammary");
      snomedToIcdSynonymMap.put("male breast", "breast");
      snomedToIcdSynonymMap.put("tongue", "oral");
      snomedToIcdSynonymMap.put("nose", "respiratory");
      snomedToIcdSynonymMap.put("nasal", "respiratory");
      snomedToIcdSynonymMap.put("urinary", "bladder");
      snomedToIcdSynonymMap.put("lacrimal", "eye");

      reader.close();

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  abstract protected StringBuffer createSnomedConceptSearchedLine(ICD11MatcherSctConcept sctCon,
    int counter);

  abstract public String getDefaultTarget();

  abstract public Map<String, ICD11MatcherSctConcept> getConceptMap();

  public abstract String getDescription();

  abstract public String getEclExpression();

  abstract public void identifyIcd11Targets() throws Exception;

  abstract protected boolean isRuleMatch(SearchResult result);

  abstract public String getRuleName();

  abstract protected ICD11MatcherSctConcept getTopLevelConcept();

  public abstract Object executeRule(ICD11MatcherSctConcept sctCon, int counter) throws Exception;

  abstract public void preTermProcessing(ICD11MatcherSctConcept sctCon) throws Exception;

  public AbstractICD11MatchingRule(ContentClientRest contentClient, String st, String sv, String tt,
      String tv, String token) {
    client = contentClient;
    sourceTerminology = st;
    sourceVersion = sv;
    targetTerminology = tt;
    targetVersion = tv;
    authToken = token;
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

        if (snomedToIcdSynonymMap.keySet().contains(splitString[i].trim().toLowerCase())) {
          tokensToAdd.add(snomedToIcdSynonymMap.get(splitString[i].trim().toLowerCase()));
        }
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
    return "results" + File.separator + matcherName + File.separator + getRuleName();
  }
}
