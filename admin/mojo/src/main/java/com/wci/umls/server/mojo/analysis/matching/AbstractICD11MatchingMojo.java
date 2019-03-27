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
package com.wci.umls.server.mojo.analysis.matching;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.mojo.analysis.AbstractContentAnalysisMojo;
import com.wci.umls.server.mojo.analysis.SctConceptsContentAnalyzer;
import com.wci.umls.server.mojo.model.ICD11MatcherRelationship;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.ICD11MatcherConceptSearcher;

@Mojo(name = "icd11-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public abstract class AbstractICD11MatchingMojo extends AbstractContentAnalysisMojo {
  private static final String UNABLE_TO_MATCH_HEADER =
      "\tCouldn't discern between the following options\n";

  private final String neoplasmDescriptionsFile = "src\\main\\resources\\allNeoplasmDescs.txt";

  private final String nonIsaNeoplasmRelsFile = "src\\main\\resources\\nonIsaNeoplasmRels.txt";

  @Parameter
  protected String ruleList;

  /** The output file path. */
  protected String outputFilePath;

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

  protected AbstractMatchRules matchingRules;

  protected String matcher;

  abstract protected Set<AbstractICD11MatchingRule> setupProcess();

  abstract protected void setupContentParsers(AbstractICD11MatchingRule rule) throws IOException;

  abstract protected String identifySingleResult(ICD11MatcherSctConcept sctCon,
    AbstractICD11MatchingRule rule, Set<String> results) throws Exception;

  abstract protected int getDepthLocation();

  abstract protected List<String> cleanResultsForTerminologist(Set<String> results);

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.maven.plugin.Mojo#execute()
   */
  protected void match(String matcher) throws MojoFailureException {
    try {

      /*
       * Setup
       */
      this.matcher = matcher;
      setup(matcher, st, sv, tt, tv);
      getLog().info("  maxCount = " + maxCount);

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

      Set<AbstractICD11MatchingRule> rulesToProcess = setupProcess();

      for (AbstractICD11MatchingRule rule : rulesToProcess) {
        Map<String, ICD11MatcherSctConcept> snomedConcepts = preRuleProcessing(rule);
        List<String> noMatchList = new ArrayList<>();

        int counter = 0;

        // Process Terms
        for (ICD11MatcherSctConcept sctCon : snomedConcepts.values()) {

          // if (counter >= 15) { break; }

          rule.preTermProcessing(sctCon);

          Set<String> results = rule.executeRule(sctCon, ++counter);

          postTermProcessing(rule, sctCon, results, noMatchList, counter, snomedConcepts.size());
        }

        postRuleProcessing(rule, noMatchList);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new MojoFailureException("Unexpected exception:", e);
    }
  }

  private Map<String, ICD11MatcherSctConcept> preRuleProcessing(AbstractICD11MatchingRule rule)
    throws Exception {
    getLog().info("\n\n\n**************************\nNow Processing\n" + rule.getRuleId()
        + " Matching Mojo\n" + rule.getDescription() + "\n**************************\n");

    /*
     * Start Processing rule
     */
    rule.setDevWriter(
        prepareResultsFile(rule.getRuleId(), "developerResults", "ICD11 Matching Results"));
    rule.setTermWriter(
        prepareResultsFile(rule.getRuleId(), "terminologistResults", "ICD11 Matching Results"));

    // setup parser
    setupContentParsers(rule);

    Map<String, ICD11MatcherSctConcept> snomedConcepts = identifyContentToProcess(rule);

    // Identify ICD11 Targets
    rule.identifyIcd11Targets();
    System.out.println("Have " + snomedConcepts.size() + " SNOMED concepts to process");

    matchingRules.setRule(rule);

    return snomedConcepts;
  }

  private void generateFiles(Map<String, ICD11MatcherSctConcept> snomedConcepts, String rule)
    throws FileNotFoundException, UnsupportedEncodingException {
    SctConceptsContentAnalyzer analyzer = new SctConceptsContentAnalyzer(matcher, rule);

    analyzer.analyze(snomedConcepts.values(), targetTerminology, targetVersion, client, authToken);
  }

  private Map<String, ICD11MatcherSctConcept> populateContent(
    Map<String, ICD11MatcherSctConcept> snomedConcepts) throws Exception {
    ICD11MatcherConceptSearcher.canPopulateFromFiles = false;
    Map<String, ICD11MatcherSctConcept> populatedCons = new HashMap<>();

    int counter = 0;
    for (ICD11MatcherSctConcept con : snomedConcepts.values()) {
      ICD11MatcherSctConcept popCon = conceptSearcher.populateSctConcept(con.getConceptId(), null);

      populatedCons.put(con.getConceptId(), popCon);

      if (counter % 50 == 0) {
        if (counter == 0) {
          getLog().info("Calling terminology server to populate the "
              + snomedConcepts.values().size() + " SCT concepts.");
        } else {
          getLog().info("Have processed " + counter + " out of " + snomedConcepts.values().size()
              + " concepts");
        }
      }

      counter++;
    }

    ICD11MatcherConceptSearcher.canPopulateFromFiles = true;

    return populatedCons;
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
    ICD11MatcherSctConcept sctCon, int counter) {

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
  private void printOutNonIsaRels(Map<String, ICD11MatcherSctConcept> concepts) throws Exception {
    // Setup File
    PrintWriter writer = prepareRelOutputFile("nonIsaRels", "ECL Analysis");

    int counter = 0;
    for (ICD11MatcherSctConcept con : concepts.values()) {
      for (ICD11MatcherRelationship rel : con.getRels()) {
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
  private Map<String, ICD11MatcherSctConcept> populateConceptsFromFiles() throws IOException {
    // Populate Relationships
    BufferedReader reader = new BufferedReader(new FileReader(nonIsaNeoplasmRelsFile));
    Map<String, ICD11MatcherSctConcept> concepts = new HashMap<>();

    String line = reader.readLine(); // Don't want header
    line = reader.readLine();
    while (line != null) {
      String[] columns = line.split("\t");
      if (!concepts.containsKey(columns[0])) {
        ICD11MatcherSctConcept con = new ICD11MatcherSctConcept(columns[0], columns[1]);
        concepts.put(columns[0], con);
      }

      ICD11MatcherRelationship rel = new ICD11MatcherRelationship();
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

  protected void postTermProcessing(AbstractICD11MatchingRule rule, ICD11MatcherSctConcept sctCon,
    Set<String> results, List<String> noMatchList, int counter, int totalConcepts)
    throws Exception {
    if (results != null && !results.isEmpty()) {
      String singleResponse = identifySingleResult(sctCon, rule, results);

      if (singleResponse == null) {
        printWithNoSingleResponse(results, rule);
      } else {
        printWithSingleResponse(singleResponse, results, rule);
      }
    } else {
      printWithNoMatches(noMatchList, rule, sctCon);
    }

    if (counter % 5 == 0) {
      rule.getDevWriter().flush();
      rule.getTermWriter().flush();
    }
    if (counter % 25 == 0) {
      System.out.println("Have completed " + counter + " out of " + totalConcepts);
    }

  }

  private void printWithNoMatches(List<String> noMatchList, AbstractICD11MatchingRule rule,
    ICD11MatcherSctConcept sctCon) {
    // No matches
    String outputString =
        "\t" + rule.getDefaultTarget() + "\tNo direct match. Using default target\n\n";
    rule.getDevWriter().println(outputString);
    rule.getTermWriter().println(outputString);

    noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
  }

  private void printWithNoSingleResponse(Set<String> results, AbstractICD11MatchingRule rule) {
    StringBuffer devBuf = new StringBuffer();
    StringBuffer termBuf = new StringBuffer();
    devBuf.append(UNABLE_TO_MATCH_HEADER);
    termBuf.append(UNABLE_TO_MATCH_HEADER);

    List<String> termResults = cleanResultsForTerminologist(results);

    for (String result : results) {
      devBuf.append(result + "\n");
    }

    for (String result : termResults) {
      termBuf.append(result + "\n");
    }

    devBuf.append("\n");
    termBuf.append("\n");

    System.out.println(devBuf.toString());
    rule.getDevWriter().println(devBuf.toString());
    rule.getTermWriter().println(termBuf.toString());
  }

  private void printWithSingleResponse(String singleResponse, Set<String> results,
    AbstractICD11MatchingRule rule) {
    if (singleResponse.startsWith("\t")) {
      singleResponse = singleResponse.substring(1);
    }
    if (singleResponse.startsWith("\n")) {
      singleResponse = singleResponse.substring(1);
    }
    System.out.println("\n\nSelected Response: " + singleResponse);
    System.out.println();
    System.out.println();

    for (String result : results) {
      if (result.startsWith("\t")) {
        result = result.substring(1);
      }
      if (result.startsWith("\n")) {
        result = result.substring(1);
      }
      rule.getDevWriter().print("\n" + result);
    }
    rule.getDevWriter().println("\n\tSelected Response: " + singleResponse);
    rule.getDevWriter().println();
    rule.getDevWriter().println();

    rule.getTermWriter().println("\t" + singleResponse);
    rule.getTermWriter().println();
    rule.getTermWriter().println();
  }

  private void postRuleProcessing(AbstractICD11MatchingRule rule, List<String> noMatchList) {
    rule.getDevWriter().println(ICD11MatchingConstants.NON_MATCH_HEADER);
    rule.getTermWriter().println(ICD11MatchingConstants.NON_MATCH_HEADER);
    for (String s : noMatchList) {
      rule.getDevWriter().println(s);
      rule.getTermWriter().println(s);
    }

    getLog().info("");
    getLog().info("Finished processing rule(s)...");

    rule.getDevWriter().close();
    rule.getTermWriter().close();
  }

  private Map<String, ICD11MatcherSctConcept> identifyContentToProcess(
    AbstractICD11MatchingRule rule) throws Exception {
    Map<String, ICD11MatcherSctConcept> snomedConcepts = new HashMap<>();

    snomedConcepts = populateTestConcept(Arrays.asList( 
        // "92666004",
         "203282002", 
        "186200004"));

    if (snomedConcepts.isEmpty()) {
      // Get ECL Results
      if (rule.getEclExpression() != null) {
        pfsEcl.setExpression(rule.getEclExpression());
        // concepts = processEclQuery(eclResults);
        snomedConcepts = processEclQueryFromFiles(rule);
        if ((snomedConcepts.values().iterator().next().getDescs() == null)
            || (snomedConcepts.values().iterator().next().getRels() == null)) {
          // Call server for content
          snomedConcepts = populateContent(snomedConcepts);

          // Generate outputFile for future calls
          generateFiles(snomedConcepts, rule.getRuleId());
        }

      } else {
        snomedConcepts = rule.getConceptMap();
      }
    }
    return snomedConcepts;
  }
}