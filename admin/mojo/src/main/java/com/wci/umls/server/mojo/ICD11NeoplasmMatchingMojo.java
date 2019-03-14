/**
 * Copyright 2018 West Coast Informatics, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wci.umls.server.mojo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.model.SctRelationship;
import com.wci.umls.server.mojo.processes.FindingSiteUtility;
import com.wci.umls.server.mojo.processes.NeoplasmConceptSearcher;

@Mojo(name = "icd11-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11NeoplasmMatchingMojo extends AbstractContentAnalysisMojo {

  @Parameter
  protected String ruleList;

  /** The output file path. */
  protected String outputFilePath;

  /** The analysis. */
  protected boolean analysis = false;

  /** The testing. */
  protected boolean testing = false;

  /** The max count. */
  protected int maxCount = 5;

  /** The limited pfs count. */
  protected Integer limitedPfsCount;

  /** Name of terminology to be loaded. */
  protected String st = "SNOMEDCT";

  /** The version. */
  protected String sv = "latest";

  /** Name of terminology to be loaded. */
  protected String tt = "ICD11";

  /** The version. */
  protected String tv = "201812";

  /** The output developer writer. */
  protected PrintWriter outputDeveloperWriter;

  /** The output terminologist writer. */
  protected PrintWriter outputTerminologistWriter;

  protected Set<SearchResult> icd11Targets;

  private final String NON_MATCH_HEADER = "\n\n\nCouldn't Match the following: ";

  private FindingSiteUtility fsUtility;

  private NeoplasmMatchRules matchingRules;

  private final String neoplasmDescriptionsFile = "src\\main\\resources\\allNeoplasmDescs.txt";

  private final String nonIsaNeoplasmRelsFile = "src\\main\\resources\\nonIsaNeoplasmRels.txt";

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  @Override
  public void execute() throws MojoFailureException {
    try {

      /*
       * Setup
       */
      setup("icd11-matcher", st, sv, tt, tv);
      getLog().info("  maxCount = " + maxCount);
      getLog().info("  analysis = " + analysis);

      /*
       * Error Checking
       */
      if (sourceTerminology == null || sourceTerminology.isEmpty() || targetTerminology == null
          || targetTerminology.isEmpty()) {
        throw new Exception(
            "Must define a source and target terminology to search against i.e. SNOMEDCT");
      }
      if (sourceVersion == null || sourceVersion.isEmpty() || targetVersion == null
          || targetVersion.isEmpty()) {
        throw new Exception(
            "Must define a source and target version to search against i.e. latest");
      }

      Set<AbstractNeoplasmICD11MatchingRule> rulesToProcess = setupProcess();

      for (AbstractNeoplasmICD11MatchingRule rule : rulesToProcess) {
        getLog().info("ICD 11 " + rule.getRule() + " Matching Mojo");

        /*
         * Start Processing rule
         */
        rule.setDevWriter(
            prepareResultsFile(rule.getRule(), "developerResults", "ICD11 Matching Results"));
        rule.setTermWriter(
            prepareResultsFile(rule.getRule(), "terminologistResults", "ICD11 Matching Results"));

        // New rule processing preparation
        int counter = 0;
        Map<String, SctNeoplasmConcept> snomedConcepts = new HashMap<>();
        List<String> noMatchList = new ArrayList<>();

        if (!analysis) {
          // setup parser
          setupContentParsers();

          // Get ECL Results
          if (rule.getEclExpression() != null) {
            pfsEcl.setExpression(rule.getEclExpression());
            // concepts = processEclQuery(eclResults);
             snomedConcepts = processEclQueryFromFiles(rule);
/*
             snomedConcepts = populateTestConcept(Arrays.asList(
                "92540005",
//                "92695008",
                "92666004"
//                ,"255140001"
                ));
*/
          } else {
            snomedConcepts = rule.getConceptMap();
          }
            
          // Identify ICD11 Targets
          rule.identifyIcd11Targets();
          System.out.println("Have " + snomedConcepts.size() + " concepts to process");

          // Process Terms
          for (SctNeoplasmConcept sctCon : snomedConcepts.values()) {
            Set<String> findingSites = fsUtility.identifyAssociatedMorphologyBasedFindingSites(sctCon);

            String resultString = rule.executeRule(sctCon, findingSites, ++counter);

            postTermProcessing(findingSites, rule, sctCon, resultString, noMatchList, counter,
                snomedConcepts.size());
          }
        } else {
          snomedConcepts = populateConceptsFromFiles();
          printOutNonIsaRels(snomedConcepts);
        }

        postRuleProcessing(rule, noMatchList);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  private Set<AbstractNeoplasmICD11MatchingRule> setupProcess() {
    conceptSearcher.setup(client, sourceTerminology, sourceVersion, targetTerminology,
        targetVersion, authToken);
    fsUtility = new FindingSiteUtility(client, sourceTerminology, sourceVersion,
        targetTerminology, targetVersion, authToken);
    AbstractNeoplasmICD11MatchingRule.setConceptSearcher(conceptSearcher);
    fsUtility.setConceptSearcher(conceptSearcher);
    AbstractNeoplasmICD11MatchingRule.setFindingSiteUtility(fsUtility);
    matchingRules = new NeoplasmMatchRules(fsUtility);

    Set<AbstractNeoplasmICD11MatchingRule> rulesToProcess = defineRulesToProcess();
    
    return rulesToProcess;
  }

  private void setupContentParsers() throws IOException {
    setupDescParser();

    boolean populatedFromFiles = descParser.readAllNeoplasmDescsFromFile();
    populatedFromFiles = populatedFromFiles && descParser.readAllFindingSitesFromFile();
    populatedFromFiles = populatedFromFiles && relParser.readAllNeoplasmRelsFromFile();
    populatedFromFiles = populatedFromFiles && relParser.readAllFindingSitesFromFile();

    NeoplasmConceptSearcher.canPopulateFromFiles = populatedFromFiles;
    NeoplasmConceptSearcher.setDescParser(descParser);
    NeoplasmConceptSearcher.setRelParser(relParser);

  }

  private Set<AbstractNeoplasmICD11MatchingRule> defineRulesToProcess() {
    Set<AbstractNeoplasmICD11MatchingRule> rulesToProcess = new HashSet<>();

    String[] rules = ruleList.split(",");
    
    for (int i = 0; i < rules.length; i++) {
      AbstractNeoplasmICD11MatchingRule rule = null;
      if (Integer.parseInt(rules[i]) == 1) {
        rule = new ICD11MatchingRule1(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 2) {
        rule = new ICD11MatchingRule2(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      } else if (Integer.parseInt(rules[i]) == 4) {
        rule = new ICD11MatchingRule4(client, sourceTerminology, sourceVersion, targetTerminology,
            targetVersion, authToken);
      }

      if (rule != null) {
        rulesToProcess.add(rule);
      }
    }
    
    return rulesToProcess;
  }

  /**
   * Construct con info str.
   *
   * @param findingSites the finding sites
   * @param sctCon the sct con
   * @param counter the counter
   * @return the string buffer
   */
  protected StringBuffer createSnomedConceptSearchedLine(Set<String> findingSites,
    SctNeoplasmConcept sctCon, int counter) {

    StringBuffer newConInfoStr = new StringBuffer();
    newConInfoStr.append("\n# " + counter + " Snomed Concept: " + sctCon.getName() + "\tSctId: "
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

  /**
   * Prints the out non isa rels.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  protected void printOutNonIsaRels(Map<String, SctNeoplasmConcept> concepts) throws Exception {
    // Setup File
    PrintWriter writer = prepareRelOutputFile("nonIsaRels", "ECL Analysis");

    int counter = 0;
    for (SctNeoplasmConcept con : concepts.values()) {
      for (SctRelationship rel : con.getRels()) {
        if (!rel.getRelationshipType().equals("Is a")) {
          exportRels(rel, con.getConceptId(), writer);
        }
      }

      if (counter++ % 100 == 0) {
        getLog().info("Processed " + counter + " out of " + concepts.size() + " concepts");
        writer.flush();
      }
    }

    writer.close();
  }

  /**
   * Populate concepts from files.
   *
   * @return the map
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected Map<String, SctNeoplasmConcept> populateConceptsFromFiles() throws IOException {
    // Populate Relationships
    BufferedReader reader = new BufferedReader(new FileReader(nonIsaNeoplasmRelsFile));
    Map<String, SctNeoplasmConcept> concepts = new HashMap<>();

    String line = reader.readLine(); // Don't want header
    line = reader.readLine();
    while (line != null) {
      String[] columns = line.split("\t");
      if (!concepts.containsKey(columns[0])) {
        SctNeoplasmConcept con = new SctNeoplasmConcept(columns[0], columns[1]);
        concepts.put(columns[0], con);
      }

      SctRelationship rel = new SctRelationship();
      rel.setDescription(columns[1]);
      rel.setRelationshipType(columns[2]);
      rel.setRelationshipDestination(columns[3]);
      rel.setRoleGroup(Integer.parseInt(columns[4]));

      concepts.get(columns[0]).getRels().add(rel);
      line = reader.readLine();
    }

    reader.close();

    // Populate Descriptions
    reader = new BufferedReader(new FileReader(neoplasmDescriptionsFile));

    line = reader.readLine(); // Don't want header
    line = reader.readLine();
    while (line != null) {
      String[] columns = line.split("\t");

      // Only bring in those descriptions which are in the "desired list" as
      // defined
      // by the rels
      if (concepts.containsKey(columns[0])) {
        SctNeoplasmDescription desc = new SctNeoplasmDescription();
        desc.setDescription(columns[1]);

        concepts.get(columns[0]).getDescs().add(desc);
      }

      line = reader.readLine();
    }

    reader.close();

    return concepts;
  }

  /**
   * Clean results for terminologist.
   * 
   * Only return one line per code and make that line the one with the lowest
   * depth
   * 
   * @param resultString the str
   * @return the string buffer
   */
  private List<String> cleanResultsForTerminologist(String resultString) {
    String[] lines = resultString.toString().split("\n");

    Map<String, Set<String>> sortedIdsMap = new TreeMap<>();
    // Sort lines by code
    for (String line : lines) {
      String[] columns = line.split("\t");

      if (columns.length > 4) {
        if (!sortedIdsMap.containsKey(columns[1])) {
          sortedIdsMap.put(columns[1], new HashSet<String>());
        }

        sortedIdsMap.get(columns[1]).add(line);
      }
    }

    List<String> linesToPrint = new ArrayList<>();
    for (String conId : sortedIdsMap.keySet()) {
      String lineToPrint = null;
      int lowestDepth = 1000;
      for (String line : sortedIdsMap.get(conId)) {
        String[] columns = line.split("\t");

        if (Integer.parseInt(columns[5]) < lowestDepth) {
          lowestDepth = Integer.parseInt(columns[5]);
          lineToPrint =
              "\t" + columns[1] + "\t" + columns[2] + "\t" + columns[3] + "\t" + columns[5] + "\n";
          ;
        }
      }

      linesToPrint.add(lineToPrint.trim());
    }

    return linesToPrint;
  }

  private void postRuleProcessing(AbstractNeoplasmICD11MatchingRule rule,
    List<String> noMatchList) {
    rule.getDevWriter().println(NON_MATCH_HEADER);
    rule.getTermWriter().println(NON_MATCH_HEADER);
    for (String s : noMatchList) {
      rule.getDevWriter().println(s);
      rule.getTermWriter().println(s);
    }

    getLog().info("");
    getLog().info("Finished processing rule(s)...");

    rule.getDevWriter().close();
    rule.getTermWriter().close();
  }

  private void postTermProcessing(Set<String> findingSites, AbstractNeoplasmICD11MatchingRule rule,
    SctNeoplasmConcept sctCon, String resultString, List<String> noMatchList, int counter,
    int totalConcepts) throws Exception {
    if (resultString != null && !resultString.isEmpty()) {
      System.out.println(resultString);

      rule.getDevWriter().println(resultString);
      rule.getDevWriter().println();
      rule.getDevWriter().println();

      String singleResponse = identifyProperResponse(sctCon, findingSites, resultString);
      
      if (singleResponse == null) {
        StringBuffer devBuf = new StringBuffer();
        StringBuffer termBuf = new StringBuffer();
        devBuf.append("\tCouldn't discern between the following options\n");
        termBuf.append("\tCouldn't discern between the following options\n");
        
        List<String> termResults = cleanResultsForTerminologist(resultString);

        String[] results = resultString.split("\n");
        for (int i = 0; i< results.length; i++) {
          devBuf.append("\t" + results[i] + "\n");
        }
        
        for (String r : termResults) {
          termBuf.append("\t" + r + "\n");
        }
        
        devBuf.append("\n");
        termBuf.append("\n");

        System.out.println(devBuf.toString());
        rule.getDevWriter().println(devBuf.toString());
        rule.getTermWriter().println(termBuf.toString());
      } else {
        if (!singleResponse.startsWith("\t")) {
          singleResponse = "\t" + singleResponse;
        }
        System.out.println("\n\nFinal Response: " + singleResponse);
        rule.getTermWriter().println(singleResponse);
        rule.getTermWriter().println();
        rule.getTermWriter().println();
      }
    } else {
      // No matches
      String outputString = "\t" + rule.getDefaultTarget() + "\tNo direct match. Using default target\n\n";
      rule.getDevWriter().println(outputString);
      rule.getTermWriter().println(outputString);

      noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
    }

    if (counter % 10 == 0) {
      rule.getDevWriter().flush();
      rule.getTermWriter().flush();
    }
    if (counter % 25 == 0) {
      System.out.println("Have completed " + counter + " out of " + totalConcepts);
    }

  }

  private String identifyProperResponse(SctNeoplasmConcept sctCon, Set<String> findingSites,
    String resultString) throws Exception {
    
    List<String> results = cleanResultsForTerminologist(resultString);
    System.out.println("\nMatches: ");
    for (String r : results) {
      System.out.println(r);
    }

    // Single result, just return
    if (results.size() == 1) {
      return results.iterator().next();
    }

    /* Multiple results, select one */
    return matchingRules.processAllMatchingRules(results, sctCon, findingSites);
  }
}