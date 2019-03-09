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

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.model.SctRelationship;

/**
 * Used to search the term-server database for concepts with a matching string.
 * 
 * See matcher/pom.xml for a sample invocation.
 *
 * @author Jesse Efron
 */
@Mojo(name = "icd11-matcher", defaultPhase = LifecyclePhase.PACKAGE)
public class ICD11MatchingMojo extends AbstractMatchingAnalysisMojo {

	/** The output file path. */
	private String outputFilePath;

	private boolean analysis = false;

	private boolean testing = true;

	private List<String> topLevelBodyStructureIds = Arrays.asList("86762007", "20139000", "39937001", "81745001",
			"387910009", "127882003", "64033007", "117590005", "21514008", "76752008", "113331007", "363667005",
			"31610004");

	private int maxCount = 5;
	protected Integer limitedPfsCount;

	/** Name of terminology to be loaded. */
	private String st = "SNOMEDCT";

	/** The version. */
	private String sv = "latest";

	/** Name of terminology to be loaded. */
	private String tt = "icd11-10";

	/** The version. */
	private String tv = "latest";

	private List<String> nonFindingSiteStrings = Arrays.asList("of", "part", "structure", "system", "and/or", "and",
			"region", "area");

	private String tcInputFilePath = "C:\\Code\\wci\\myTransClosureFile.csv";

	private Map<String, Map<String, Integer>> transClosureMap = new HashMap<>();
	private Map<String, Map<String, Integer>> inverseTransClosureMap = new HashMap<>();

	private PrintWriter outputWriter;

	private Map<String, Map<Concept, Set<String>>> findingSitePotentialTermsMapCache = new HashMap<>();

	@Override
	public void execute() throws MojoFailureException {
		try {
			getLog().info("ICD 11 Matching Mojo");

			/*
			 * Setup
			 */
			setup("icd11-matcher", st, sv, tt, tv);
			getLog().info("  maxCount = " + maxCount);
			getLog().info("  analysis = " + analysis);
			preProcessing();

			/*
			 * Error Checking
			 */
			if (sourceTerminology == null || sourceTerminology.isEmpty() || targetTerminology == null
					|| targetTerminology.isEmpty()) {
				throw new Exception("Must define a source and target terminology to search against i.e. SNOMEDCT");
			}
			if (sourceVersion == null || sourceVersion.isEmpty() || targetVersion == null || targetVersion.isEmpty()) {
				throw new Exception("Must define a source and target version to search against i.e. latest");
			}

			/*
			 * Start Processing
			 */
			// Get ECL Results

			Map<String, SctNeoplasmConcept> concepts = null;

			if (!testing) {
				pfsEcl.setExpression("<< 55342001 : 116676008 = 399919001");
				final SearchResultList eclResults = client.findConcepts(sourceTerminology, sourceVersion, null, pfsEcl,
						authToken);
				getLog().info("With ECL, have: " + eclResults.getObjects().size());

				concepts = processEclQuery(eclResults);
			} else {
				concepts = populateConceptsFromFiles();
			}

			if (analysis) {
				printOutNonIsaRels(concepts);
			} else {
				// Process Terms
				executeRule1(concepts);
			}

			getLog().info("");
			getLog().info("Finished processing...");
			getLog().info("Output avaiable at: " + outputFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException("Unexpected exception:", e);
		}
	}

	private void executeRule1(Map<String, SctNeoplasmConcept> snomedConcepts) throws Exception {

		Set<SearchResult> icd11Targets = getRule1Icd11Concepts();
		List<String> noMatchList = new ArrayList<>();
		PrintWriter loggingWriter = prepareOutputFile("logger", "Search Details Logger");

		int counter = 0;
		System.out.println("Have " + snomedConcepts.size() + " concepts to process");
		for (SctNeoplasmConcept sctCon : snomedConcepts.values()) {
			boolean foundMatch = false;

			if (!sctCon.getConceptId().equals("92540005")) {
				continue;
			}

			Set<String> findingSites = identifyValidFindingSites(sctCon);

			if (findingSites.size() > 1) {
				int a = 1;
			}

			StringBuffer newConInfoStr = constructConInfoStr(findingSites, sctCon, ++counter);

			System.out.println(newConInfoStr);
			loggingWriter.println(newConInfoStr);

			StringBuffer str = new StringBuffer();

			foundMatch = matchApproach1(findingSites, icd11Targets, str);
			foundMatch = matchApproach2(findingSites, str) || foundMatch;

			identifyPotentialFSConcepts(findingSites);
			foundMatch = matchApproach3(icd11Targets, str) || foundMatch;
			foundMatch = matchApproach4(str) || foundMatch;

			if (counter % 25 == 0) {
				System.out.println("Have completed " + counter + " out of " + snomedConcepts.size());
				outputWriter.flush();
				loggingWriter.flush();
			}

			if (foundMatch) {
				System.out.println(str.toString());
				outputWriter.println(str);
				outputWriter.println();
				outputWriter.println();
			} else {
				noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
			}

		}

		loggingWriter.close();

		System.out.println("\n\n\nCouldn't Match the following: ");
		for (String s : noMatchList) {
			outputWriter.println(s);
			System.out.println(s);
		}
	}

	private Set<String> identifyValidFindingSites(SctNeoplasmConcept sctCon) {
		Set<String> targets = new HashSet<>();

		Set<SctRelationship> amRels = getDestRels(sctCon, "Associated morphology");
		Set<SctRelationship> findingSites = getDestRels(sctCon, "Finding site");

		for (SctRelationship morphology : amRels) {
			for (SctRelationship site : findingSites) {
				if (site.getRoleGroup() == morphology.getRoleGroup()) {
					targets.add(site.getRelationshipDestination());
				}
			}

		}
		return targets;
	}

	private StringBuffer constructConInfoStr(Set<String> findingSites, SctNeoplasmConcept sctCon, int counter) {

		StringBuffer newConInfoStr = new StringBuffer();
		newConInfoStr.append("\n\n\n# " + counter + " Testing on sctCon: " + sctCon.getName() + "\twith Id: "
				+ sctCon.getConceptId() + "\twith");
		int fsCounter = findingSites.size();
		for (String site : findingSites) {
			newConInfoStr.append(" findingSite: " + site);
			if (--fsCounter > 0) {
				newConInfoStr.append(" and");
			}
		}
		return newConInfoStr;
	}

	private boolean matchApproach3(Set<SearchResult> icd11Targets, StringBuffer str) {
		Map<String, Integer> alreadyQueried = new HashMap<>();
		Map<String, Integer> lowestDepthMap = new HashMap<>(); // icdTarget to map of depth-to-output
		Map<String, String> matchMap = new HashMap<>(); // icdTarget to map of depth-to-output

		for (String fsConId : findingSitePotentialTermsMapCache.keySet()) {
			// Testing on ancestors of findingSite fsConId
			Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
			Map<Concept, Set<String>> potentialFSConTerms = findingSitePotentialTermsMapCache.get(fsConId);

			for (Concept testCon : potentialFSConTerms.keySet()) {
				Set<String> normalizedStrings = potentialFSConTerms.get(testCon);
				int depth = depthMap.get(testCon.getTerminologyId());

				for (String normalizedStr : normalizedStrings) {
					String[] tokens = normalizedStr.toLowerCase().split(" ");

					for (int i = 0; i < tokens.length; i++) {
						if (!nonFindingSiteStrings.contains(tokens[i])) {
							if (!alreadyQueried.keySet().contains(tokens[i])
									|| (depth < alreadyQueried.get(tokens[i]))) {
								alreadyQueried.put(tokens[i], depth);

								for (SearchResult icd11Con : icd11Targets) {
									if (icd11Con.getValue().toLowerCase().matches(".*\\b" + tokens[i] + "\\b.*")) {

										String outputString = "\n3333 Potential Match at depth: " + depth + " (on "
												+ tokens[i] + ") with score: " + icd11Con.getScore() + ": "
												+ icd11Con.getValue() + " with Id: " + icd11Con.getTerminologyId();
//										System.out.println(outputString);
										if (!lowestDepthMap.containsKey(icd11Con.getTerminologyId())
												|| depth < lowestDepthMap.get(icd11Con.getTerminologyId())) {
											lowestDepthMap.put(icd11Con.getTerminologyId(), depth);
											matchMap.put(icd11Con.getTerminologyId(), outputString);
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
			generateStringOutput(lowestDepthMap, matchMap, str);
			return true;
		}

		return false;
	}

	private boolean matchApproach4(StringBuffer str) throws Exception {
		boolean foundMatch = false;
		Map<String, Integer> alreadyQueried = new HashMap<>();
		Map<String, Integer> lowestDepthMap = new HashMap<>(); // icdTarget to map of depth-to-output
		Map<String, String> matchMap = new HashMap<>(); // icdTarget to map of depth-to-output

		for (String fsConId : findingSitePotentialTermsMapCache.keySet()) {
			// Testing on ancestors of findingSite fsConId
			Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
			Map<Concept, Set<String>> potentialFSConTerms = findingSitePotentialTermsMapCache.get(fsConId);

			for (Concept testCon : potentialFSConTerms.keySet()) {
				Set<String> normalizedStrings = potentialFSConTerms.get(testCon);
				int depth = depthMap.get(testCon.getTerminologyId());

				for (String normalizedStr : normalizedStrings) {
//					System.out.println("\nWithin 4444 - Now try against: " + testCon.getName() + " ("
//							+ testCon.getTerminologyId() + ") using: " + normalizedStr);
					if (!alreadyQueried.keySet().contains(normalizedStr)
							|| (depth < alreadyQueried.get(normalizedStr))) {
						alreadyQueried.put(normalizedStr, depth);

						SearchResultList results = testFindingSite(normalizedStr);
						for (SearchResult result : results.getObjects()) {
							if (isNeoplasmMatch(result)) {
								String outputString = "\n" + "4444 Potential Match at depth " + depth + " (on "
										+ normalizedStr + ") with score: " + result.getScore() + ": "
										+ result.getValue();
//								System.out.println(outputString);
								if (!lowestDepthMap.containsKey(result.getTerminologyId())
										|| depth < lowestDepthMap.get(result.getTerminologyId())) {
									lowestDepthMap.put(result.getTerminologyId(), depth);
									matchMap.put(result.getTerminologyId(), outputString);
								}
							}
						}
					}
				}
			}
		}

		if (!matchMap.isEmpty()) {
			generateStringOutput(lowestDepthMap, matchMap, str);
			return true;
		}

		return false;
	}

	private void generateStringOutput(Map<String, Integer> lowestDepthMap, Map<String, String> matchMap,
			StringBuffer str) {
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

//		System.out.println("\n\nBut actually outputing:");
		for (String s : lowestDepthStrings) {
//			System.out.println(s);
			str.append(s);
		}
	}

	private void identifyPotentialFSConcepts(Set<String> findingSites) throws Exception {
		for (String site : findingSites) {
			// Get the finding site as a concept
			SctNeoplasmConcept fsConcept = getSctConceptFromDesc(site);

			if (findingSitePotentialTermsMapCache.containsKey(fsConcept.getConceptId())) {
				return;
			}
			Map<Concept, Set<String>> potentialFSConTerms = new HashMap<>();
			findingSitePotentialTermsMapCache.put(fsConcept.getConceptId(), potentialFSConTerms);

			if (topLevelBodyStructureIds.contains(fsConcept.getConceptId())) {
				Concept mapCon = client.getConcept(fsConcept.getConceptId(), sourceTerminology, sourceVersion, null,
						authToken);
				Set<String> bucket = new HashSet<>();
				potentialFSConTerms.put(mapCon, bucket);
			} else {
				// Get all fsCon's ancestors
				String topLevelSctId = null;
				final ConceptList ancestorResults = client.findAncestorConcepts(fsConcept.getConceptId(),
						sourceTerminology, sourceVersion, false, pfsLimitless, authToken);

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
					return;
				}

				// TODO: Because can't do ancestors via ECL, need this work around
				// Identify all descendants of top level bodyStructure concept
				pfsEcl.setExpression("<< " + topLevelSctId);
				final SearchResultList descendentResults = client.findConcepts(sourceTerminology, sourceVersion, null,
						pfsEcl, authToken);

				// Create a list of concepts that are both ancestors of fsConcept and
				// descendents of topLevelBodyStructure Concept
				// TODO: This could be a Rest Call in of itself
				for (Concept ancestor : ancestorResults.getObjects()) {

					for (SearchResult potentialFindingSite : descendentResults.getObjects()) {
						if (ancestor.getTerminologyId().equals(potentialFindingSite.getTerminologyId())) {
							Concept mapCon = client.getConcept(ancestor.getTerminologyId(), sourceTerminology,
									sourceVersion, null, authToken);
							Set<String> bucket = new HashSet<>();
							potentialFSConTerms.put(mapCon, bucket);
							break;
						}
					}
				}
			}

			for (Concept testCon : potentialFSConTerms.keySet()) {
				for (Atom atom : testCon.getAtoms()) {
					if (isValidDescription(atom)) {
						String normalizedString = atom.getName().toLowerCase();
						for (String s : nonFindingSiteStrings) {
							normalizedString = normalizedString.replaceAll("\\b" + s + "s" + "\\b", " ").trim();
							normalizedString = normalizedString.replaceAll("\\b" + s + "\\b", " ").trim();
						}

						normalizedString = normalizedString.replaceAll(" {2,}", " ").trim();

						if (!potentialFSConTerms.get(testCon).contains(normalizedString)) {
							potentialFSConTerms.get(testCon).add(normalizedString);
						}
					}
				}
			}
		}

		return;
	}

	private boolean matchApproach2(Set<String> findingSites, StringBuffer str) throws Exception {
		boolean matchFound = false;

		for (String site : findingSites) {
			SctNeoplasmConcept fsConcept = getSctConceptFromDesc(site);

			for (SctNeoplasmDescription desc : fsConcept.getDescs()) {
				SearchResultList results = testFindingSite(desc.getDescription());
				for (SearchResult result : results.getObjects()) {
					if (isNeoplasmMatch(result)) {
						str.append("\n" + "2222 matched " + result.getTerminologyId() + "\t" + result.getValue());
						matchFound = true;
					}
				}
			}
		}

		return matchFound;
	}

	private boolean matchApproach1(Set<String> findingSites, Set<SearchResult> icd11Targets, StringBuffer str)
			throws Exception {
		boolean foundMatch = false;
		Set<String> alreadyQueried = new HashSet<>();

		for (String site : findingSites) {
			String[] tokens = site.toLowerCase().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				if (!alreadyQueried.contains(tokens[i]) && !nonFindingSiteStrings.contains(tokens[i])) {
					alreadyQueried.add(tokens[i]);

					for (SearchResult icd11Con : icd11Targets) {
						if (icd11Con.getValue().toLowerCase().matches(".*\\b" + tokens[i] + "\\b.*")) {
							str.append("\n1111 Potential Match (" + tokens[i] + ") : " + icd11Con.getValue()
									+ " with Id: " + icd11Con.getTerminologyId());
							foundMatch = true;
						}
					}
				}
			}
		}

		return foundMatch;
	}

	private void preProcessing() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(tcInputFilePath));

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

		outputWriter = prepareRelOutputFile("results", "ICD11 Matching Results");

		reader.close();
	}

	private void printOutNonIsaRels(Map<String, SctNeoplasmConcept> concepts) throws Exception {
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

	private Set<SearchResult> getRule1Icd11Concepts() throws Exception {
		final SearchResultList fullStringResults = client.findConcepts(targetTerminology, targetVersion,
				"(terminologyId: XH* OR terminologyId: 2*) AND \"Carcinoma\" AND \"in situ\"", pfsLimitless, authToken);

		System.out.println("Have returned : " + fullStringResults.getTotalCount() + " objects");
		int matches = 0;
		for (SearchResult result : fullStringResults.getObjects()) {
			System.out.println(result.getTerminologyId() + "\t" + result.getValue());
		}

		Set<SearchResult> filteredIcd11List = new HashSet<>();
		System.out.println("\n\n\nNow Filtering");
		for (SearchResult result : fullStringResults.getObjects()) {
			if (isNeoplasmMatch(result)) {
				System.out.println(result.getTerminologyId() + "\t" + result.getValue());
				filteredIcd11List.add(result);
				matches++;
			}
		}
		System.out.println("Have actually found : " + matches + " matches");

		return filteredIcd11List;
	}

	private boolean isNeoplasmMatch(SearchResult result) {
		if ((result.getTerminologyId().startsWith("XH") || result.getTerminologyId().startsWith("2"))
				&& result.getValue().toLowerCase().matches(".*\\bcarcinoma\\b.*")
				&& result.getValue().toLowerCase().matches(".*\\bin situ\\b.*")) {
			return true;
		}

		return false;
	}

	private SearchResultList testFindingSite(String queryPortion) throws Exception {
		final SearchResultList straightMatch = client.findConcepts(targetTerminology, targetVersion,
				"(terminologyId: XH* OR terminologyId: 2*) AND \"Carcinoma\" AND \"in situ\" AND " + queryPortion,
				pfsLimited, authToken);

		return straightMatch;
	}

	private Map<String, SctNeoplasmConcept> populateConceptsFromFiles() throws IOException {
		// Populate Relationships
		String inputFilePath = "C:\\Users\\yishai\\Desktop\\Neoplasm\\Input Files\\nonIsaRelsRule1.txt";
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
		inputFilePath = "C:\\Users\\yishai\\Desktop\\Neoplasm\\Input Files\\allDescs.txt";
		reader = new BufferedReader(new FileReader(inputFilePath));

		line = reader.readLine(); // Don't want header
		line = reader.readLine();
		while (line != null) {
			String[] columns = line.split("\t");

			// Only bring in those descriptions which are in the "desired list" as defined
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
	 * Write results to file.
	 *
	 * @param outputFile
	 *            the output file
	 * @param line
	 *            the line
	 * @param results
	 *            the results
	 * @param acronymExpanded
	 *            the acronym expanded
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeResultsToFile(PrintWriter outputFile, String line, SearchResultList results,
			boolean acronymExpanded) throws IOException {
		if (!acronymExpanded) {
			getLog().info("Found " + results.getTotalCount() + " results");
		} else {
			getLog().info("Found " + results.getTotalCount()
					+ " results which may be more than expected, but is due to acronym expansion.");
		}

		outputFile.write(line);
		outputFile.println();

		int counter = 0;
		float lastScore = 0;
		Set<String> seenResults = new HashSet<>();
		for (SearchResult singleResult : results.getObjects()) {
			if (limitedPfsCount != null && ++counter > limitedPfsCount && lastScore != singleResult.getScore()) {
				break;
			}

			// With acronym expansion, want to ensure don't list duplicate results
			if (seenResults.contains(singleResult.getTerminologyId())) {
				continue;
			}

			getLog().info("    match" + (counter) + " = " + singleResult.getTerminologyId() + " | "
					+ singleResult.getValue() + " | " + "with score: " + singleResult.getScore() + " | ");

			outputFile.write("\t");
			outputFile.write(singleResult.getTerminologyId());
			seenResults.add(singleResult.getTerminologyId());
			outputFile.write("\t");
			outputFile.print(singleResult.getValue());
			outputFile.write("\t");
			outputFile.println(singleResult.getScore());

			lastScore = singleResult.getScore();
		}

		outputFile.println();
		outputFile.flush();
	}
	/*
	 * private boolean matchApproach-Abandon1(String findingSite, Set<SearchResult>
	 * icd11Targets, List<String> noMatchList, StringBuffer str) throws Exception {
	 * boolean stop = false; boolean foundMatch = false; PfsParameter findParentsPfs
	 * = new PfsParameterJpa(); findParentsPfs.setMaxResults(10);
	 * 
	 * Set<SctNeoplasmConcept> testSet = new HashSet<>();
	 * testSet.add(getSctConceptFromDesc(findingSite));
	 * 
	 * while (!stop) { Set<SctNeoplasmConcept> nextTestSet = new HashSet<>();
	 * 
	 * for (SctNeoplasmConcept testingFindingSiteCon : testSet) {
	 * System.out.println("FS concept: " + testingFindingSiteCon.getName());
	 * foundMatch = foundMatch || testFindingSite(testingFindingSiteCon.getName(),
	 * str);
	 * 
	 * if (topLevelBodyStructureIds.contains(testingFindingSiteCon.getConceptId()))
	 * { stop = true; } else { for (SctRelationship rel :
	 * testingFindingSiteCon.getRels()) { if
	 * (rel.getRelationshipType().equals("Is a")) { PfsParameterJpa sctPfs = new
	 * PfsParameterJpa(); sctPfs.setExpression(rel.getRelationshipDestination());
	 * SearchResultList sctResults = client.findConcepts(sourceTerminology,
	 * sourceVersion, rel.getRelationshipDestination(), pfs, authToken);
	 * SctNeoplasmConcept newFindingCon = populateSctConcept(
	 * sctResults.getObjects().iterator().next()); nextTestSet.add(newFindingCon); }
	 * } } }
	 * 
	 * testSet.clear(); testSet.addAll(nextTestSet); }
	 * 
	 * return foundMatch; }
	 */
}

/*
 * for (SctNeoplasmConcept con : concepts.values()) { // Try the full string,
 * then just pathology final SearchResultList fullStringResults =
 * client.findConcepts(targetTerminology, targetVersion, con.getName(), pfs,
 * authToken);
 * 
 * // Try pathology. See how many per concept Set<String> conPathologies = new
 * HashSet<>(); for (SctNeoplasmDescription desc :
 * concepts.get(con.getConceptId()).getDescs()) {
 * conPathologies.add(desc.getPathology()); }
 * 
 * for (String pathology : conPathologies) { String query = pathology +
 * " AND concept.code: 2*"; final SearchResultList pathologyResults =
 * client.findConcepts(targetTerminology, targetVersion, query , pfs,
 * authToken); int i = 1; }
 * 
 * // Try histopathology. See how many per concept Set<String>
 * conHistopathologies = new HashSet<>(); for (SctNeoplasmDescription desc :
 * concepts.get(con.getConceptId()).getDescs()) {
 * conHistopathologies.add(desc.getBodyStructure()); }
 * 
 * for (String pathology : conHistopathologies) { String query = pathology +
 * " AND concept.code: xh*"; final SearchResultList histopathologyResults =
 * client.findConcepts(targetTerminology, targetVersion, query , pfs,
 * authToken); int i = 1; } }
 */
