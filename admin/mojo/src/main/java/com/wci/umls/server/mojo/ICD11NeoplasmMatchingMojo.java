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

  protected Set<AbstractNeoplasmICD11MatchingRule> rulesToProcess =
      new HashSet<>();

  private final String NON_MATCH_HEADER =
      "\n\n\nCouldn't Match the following: ";

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
      if (sourceTerminology == null || sourceTerminology.isEmpty()
          || targetTerminology == null || targetTerminology.isEmpty()) {
        throw new Exception(
            "Must define a source and target terminology to search against i.e. SNOMEDCT");
      }
      if (sourceVersion == null || sourceVersion.isEmpty()
          || targetVersion == null || targetVersion.isEmpty()) {
        throw new Exception(
            "Must define a source and target version to search against i.e. latest");
      }

      defineRulesToProcess();

      for (AbstractNeoplasmICD11MatchingRule rule : rulesToProcess) {
        getLog().info("ICD 11 " + rule.getRule() + " Matching Mojo");

        /*
         * Start Processing rule
         */
        rule.setDevWriter(prepareResultsFile(rule.getRule(), "developerResults",
            "ICD11 Matching Results"));
        rule.setTermWriter(prepareResultsFile(rule.getRule(),
            "terminologistResults", "ICD11 Matching Results"));

        // New rule processing preparation
        int counter = 0;
        Map<String, SctNeoplasmConcept> snomedConcepts = null;
        List<String> noMatchList = new ArrayList<>();

        if (!analysis) {
          // setup parser
          setupContentParsers();

          // Get ECL Results
          pfsEcl.setExpression(rule.getEclExpression());
          // concepts = processEclQuery(eclResults);
          snomedConcepts = processEclQueryFromFiles(rule);

          // Identify ICD11 Targets
          rule.identifyIcd11Targets();
          System.out.println(
              "Have " + snomedConcepts.size() + " concepts to process");

          // Process Terms
          for (SctNeoplasmConcept sctCon : snomedConcepts.values()) {
            Set<String> findingSites =
                identifyAssociatedMorphologyBasedFindingSites(sctCon);

            String resultString =
                rule.executeRule(sctCon, findingSites, ++counter);

            postTermProcessing(rule, sctCon, resultString, noMatchList, counter,
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

  private void setupContentParsers() throws IOException {
    setupDescParser();

    boolean populatedFromFiles = descParser.readAllNeoplasmDescsFromFile();
    populatedFromFiles =
        populatedFromFiles && descParser.readAllFindingSitesFromFile();
    populatedFromFiles =
        populatedFromFiles && relParser.readAllNeoplasmRelsFromFile();
    populatedFromFiles =
        populatedFromFiles && relParser.readAllFindingSitesFromFile();

    AbstractNeoplasmICD11MatchingRule.canPopulateFromFiles = populatedFromFiles;
    AbstractNeoplasmICD11MatchingRule.setDescParser(descParser);
    AbstractNeoplasmICD11MatchingRule.setRelParser(relParser);

  }

  private void defineRulesToProcess() {
    String[] rules = ruleList.split(",");

    for (int i = 0; i < rules.length; i++) {
      if (Integer.parseInt(rules[i]) == 1) {
        rulesToProcess.add(new ICD11MatchingRule1(client, sourceTerminology,
            sourceVersion, targetTerminology, targetVersion, authToken));
      } else if (Integer.parseInt(rules[i]) == 2) {
        rulesToProcess.add(new ICD11MatchingRule2(client, sourceTerminology,
            sourceVersion, targetTerminology, targetVersion, authToken));
      }
    }
  }

  /**
   * Construct con info str.
   *
   * @param findingSites the finding sites
   * @param sctCon the sct con
   * @param counter the counter
   * @return the string buffer
   */
  protected StringBuffer createSnomedConceptSearchedLine(
    Set<String> findingSites, SctNeoplasmConcept sctCon, int counter) {

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

  /**
   * Prints the out non isa rels.
   *
   * @param concepts the concepts
   * @throws Exception the exception
   */
  protected void printOutNonIsaRels(Map<String, SctNeoplasmConcept> concepts)
    throws Exception {
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
        getLog().info("Processed " + counter + " out of " + concepts.size()
            + " concepts");
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
  protected Map<String, SctNeoplasmConcept> populateConceptsFromFiles()
    throws IOException {
    // Populate Relationships
    String inputFilePath =
        "C:\\Users\\yishai\\Desktop\\Neoplasm\\Input Files\\nonIsaRelsRule1.txt";
    BufferedReader reader = new BufferedReader(new FileReader(inputFilePath));
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
    inputFilePath =
        "C:\\Users\\yishai\\Desktop\\Neoplasm\\Input Files\\allDescs.txt";
    reader = new BufferedReader(new FileReader(inputFilePath));

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
   * @param resultString the str
   * @return the string buffer
   */
  private String cleanResultsForTerminologist(String resultString) {
    String[] lines = resultString.toString().split("\n");

    Map<String, String> processedIdsMap = new TreeMap<>();
    for (String line : lines) {
      String[] columns = line.split("\t");
      if (columns.length > 4) {
        if (!processedIdsMap.containsKey(columns[1])) {
          String ouputToReturn = "\t" + columns[1] + "\t" + columns[2] + "\t"
              + columns[3] + "\t" + columns[4] + "\n";
          processedIdsMap.put(columns[1], ouputToReturn);
        }
      }
    }

    StringBuffer returnBuffer = new StringBuffer();
    for (String code : processedIdsMap.keySet()) {
      returnBuffer.append(processedIdsMap.get(code));
    }

    return returnBuffer.toString();
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

  private void postTermProcessing(AbstractNeoplasmICD11MatchingRule rule,
    SctNeoplasmConcept sctCon, String resultString, List<String> noMatchList,
    int counter, int totalConcepts) {
    if (resultString != null) {
      System.out.println(resultString);
      rule.getDevWriter().println(resultString);
      rule.getDevWriter().println();
      rule.getDevWriter().println();

      rule.getTermWriter().println(cleanResultsForTerminologist(resultString));
      rule.getTermWriter().println();
      rule.getTermWriter().println();
    } else {
      noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
    }

    if (counter % 25 == 0) {
      System.out
          .println("Have completed " + counter + " out of " + totalConcepts);
    }

  }
}