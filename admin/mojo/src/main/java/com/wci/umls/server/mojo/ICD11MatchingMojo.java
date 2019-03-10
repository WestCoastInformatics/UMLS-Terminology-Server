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

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
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

	private int maxCount = 5;
	protected Integer limitedPfsCount;

	/** Name of terminology to be loaded. */
	private String st = "SNOMEDCT";

	/** The version. */
	private String sv = "latest";

	/** Name of terminology to be loaded. */
	private String tt = "ICD11";

	/** The version. */
	private String tv = "201812";

	private String tcInputFilePath = "C:\\Code\\wci\\myTransClosureFile.csv";

	private Map<String, Map<String, Integer>> transClosureMap = new HashMap<>();
	private Map<String, Map<String, Integer>> inverseTransClosureMap = new HashMap<>();

	private PrintWriter outputDeveloperWriter;
	private PrintWriter outputTerminologistWriter;

	private Map<String, SearchResultList> alreadyLookedUpTokenCache = new HashMap<>();

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

		int counter = 0;
		System.out.println("Have " + snomedConcepts.size() + " concepts to process");
		for (SctNeoplasmConcept sctCon : snomedConcepts.values()) {
			boolean foundMatch = false;

			// if (!sctCon.getConceptId().equals("92719006")) { continue; }

			Set<String> findingSites = identifyValidFindingSites(sctCon);
			StringBuffer newConInfoStr = constructConInfoStr(findingSites, sctCon, ++counter);

			System.out.println(newConInfoStr);
			outputDeveloperWriter.println(newConInfoStr);
			outputTerminologistWriter.println(newConInfoStr);

			StringBuffer str = new StringBuffer();

			Map<String, Integer> alreadyQueriedRegexes = new HashMap<>();
			Map<String, Integer> alreadyQueriedServerCalls = new HashMap<>();

			foundMatch = matchApproach1(findingSites, alreadyQueriedRegexes, icd11Targets, str);
			foundMatch = matchApproach2(findingSites, alreadyQueriedServerCalls, str) || foundMatch;

			Set<SctNeoplasmConcept> fsConcepts = identifyPotentialFSConcepts(findingSites);
			if (fsConcepts != null) {
				foundMatch = matchApproach3(fsConcepts, alreadyQueriedRegexes, icd11Targets, str) || foundMatch;
				foundMatch = matchApproach4(fsConcepts, alreadyQueriedServerCalls, str) || foundMatch;
			}

			if (counter % 25 == 0) {
				System.out.println("Have completed " + counter + " out of " + snomedConcepts.size());
			}

			if (foundMatch) {
				System.out.println(str.toString());
				outputDeveloperWriter.println(str);
				outputDeveloperWriter.println();
				outputDeveloperWriter.println();

				outputTerminologistWriter.println(cleanResultsForTerminologist(str));
				outputTerminologistWriter.println();
				outputTerminologistWriter.println();
			} else {
				noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
			}

			outputDeveloperWriter.flush();
			outputTerminologistWriter.flush();
		}

		System.out.println("\n\n\nCouldn't Match the following: ");
		for (String s : noMatchList) {
			outputDeveloperWriter.println(s);
			outputTerminologistWriter.println(s);
			System.out.println(s);
		}

		outputTerminologistWriter.close();
		outputDeveloperWriter.close();
	}

	private StringBuffer cleanResultsForTerminologist(StringBuffer str) {
		String[] lines = str.toString().split("\n");

		Map<String, String> processedIdsMap = new TreeMap<>();
		for (String line : lines) {
			String[] columns = line.split("\t");
			if (columns.length > 4) {
				if (!processedIdsMap.containsKey(columns[1])) {
					String ouputToReturn = "\t" + columns[1] + "\t" + columns[2] + "\t" + columns[3] + "\t" + columns[4]
							+ "\n";
					processedIdsMap.put(columns[1], ouputToReturn);
				}
			}
		}

		StringBuffer returnBuffer = new StringBuffer();
		for (String code : processedIdsMap.keySet()) {
			returnBuffer.append(processedIdsMap.get(code));
		}

		return returnBuffer;
	}

	private StringBuffer constructConInfoStr(Set<String> findingSites, SctNeoplasmConcept sctCon, int counter) {

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

	private boolean matchApproach3(Set<SctNeoplasmConcept> fsConcepts, Map<String, Integer> alreadyQueriedRegexes,
			Set<SearchResult> icd11Targets, StringBuffer str) {
		Map<String, Integer> lowestDepthMap = new HashMap<>(); // icdTarget to map of depth-to-output
		Map<String, String> matchMap = new HashMap<>(); // icdTarget to map of depth-to-output

		for (SctNeoplasmConcept fsCon : fsConcepts) {
			String fsConId = fsCon.getConceptId();

			// Testing on ancestors of findingSite fsConId
			Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
			Map<Concept, Set<String>> potentialFSConTerms = findingSitePotentialTermsMapCache.get(fsConId);

			for (Concept testCon : potentialFSConTerms.keySet()) {
				Set<String> normalizedStrs = potentialFSConTerms.get(testCon);
				int depth = depthMap.get(testCon.getTerminologyId());

				for (String normalizedStr : normalizedStrs) {
					Set<String> tokens = splitTokens(normalizedStr);

					for (String token : tokens) {
						if (!nonFindingSiteStrings.contains(token)) {
							if (!alreadyQueriedRegexes.keySet().contains(token)
									|| (depth < alreadyQueriedRegexes.get(token))) {
								alreadyQueriedRegexes.put(token, depth);

								for (SearchResult icd11Con : icd11Targets) {
									if (icd11Con.getValue().toLowerCase().matches(".*\\b" + token + "\\b.*")) {
										String outputString = "\n3333\t" + icd11Con.getCodeId() + "\t"
												+ icd11Con.getValue() + "\t" + depth + "\t" + token + "\t"
												+ icd11Con.getScore();

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
			generateStringOutput(lowestDepthMap, matchMap, str);
			return true;
		}

		return false;
	}

	private boolean matchApproach4(Set<SctNeoplasmConcept> fsConcepts, Map<String, Integer> alreadyQueriedServerCalls,
			StringBuffer str) throws Exception {
		Map<String, Integer> lowestDepthMap = new HashMap<>(); // icdTarget to map of depth-to-output
		Map<String, String> matchMap = new HashMap<>(); // icdTarget to map of depth-to-output

		for (SctNeoplasmConcept fsCon : fsConcepts) {
			String fsConId = fsCon.getConceptId();

			// Testing on ancestors of findingSite fsConId
			Map<String, Integer> depthMap = inverseTransClosureMap.get(fsConId);
			Map<Concept, Set<String>> potentialFSConTerms = findingSitePotentialTermsMapCache.get(fsConId);

			for (Concept testCon : potentialFSConTerms.keySet()) {
				Set<String> normalizedStrs = potentialFSConTerms.get(testCon);
				int depth = depthMap.get(testCon.getTerminologyId());

				for (String normalizedStr : normalizedStrs) {
					// System.out.println("\nWithin 4444 - Now try against: " + testCon.getName() +
					// " ("
					// + testCon.getTerminologyId() + ") using: " + normalizedStr);
					if (!alreadyQueriedServerCalls.keySet().contains(normalizedStr)
							|| (depth < alreadyQueriedServerCalls.get(normalizedStr))) {
						alreadyQueriedServerCalls.put(normalizedStr, depth);

						SearchResultList results = testRule1FindingSite(normalizedStr);
						for (SearchResult result : results.getObjects()) {
							if (isNeoplasmMatch(result)) {
								String outputString = "\n4444\t" + result.getCodeId() + "\t" + result.getValue() + "\t"
										+ depth + "\t" + normalizedStr + "\t" + result.getScore();

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
			generateStringOutput(lowestDepthMap, matchMap, str);
			return true;
		}

		return false;
	}

	private boolean matchApproach2(Set<String> findingSites, Map<String, Integer> alreadyQueriedServerCalls,
			StringBuffer str) throws Exception {
		boolean matchFound = false;

		for (String site : findingSites) {
			SctNeoplasmConcept fsConcept = getSctConceptFromDesc(site);

			for (SctNeoplasmDescription desc : fsConcept.getDescs()) {
				if (!alreadyQueriedServerCalls.keySet().contains(desc.getDescription())) {
					alreadyQueriedServerCalls.put(desc.getDescription(), 0);

					SearchResultList results = testRule1FindingSite(desc.getDescription());
					for (SearchResult result : results.getObjects()) {
						if (isNeoplasmMatch(result)) {
							str.append("\n2222\t" + result.getCodeId() + "\t" + result.getValue() + "\t0\t"
									+ desc.getDescription());
							matchFound = true;
						}
					}
				}
			}
		}

		return matchFound;
	}

	private boolean matchApproach1(Set<String> findingSites, Map<String, Integer> alreadyQueriedRegexes,
			Set<SearchResult> icd11Targets, StringBuffer str) throws Exception {
		boolean foundMatch = false;

		for (String site : findingSites) {
			Set<String> tokens = splitTokens(site);
			for (String token : tokens) {
				if (!alreadyQueriedRegexes.keySet().contains(token) && !nonFindingSiteStrings.contains(token)) {
					alreadyQueriedRegexes.put(token, 0);

					for (SearchResult icd11Con : icd11Targets) {
						if (icd11Con.getValue().toLowerCase().matches(".*\\b" + token + "\\b.*")) {
							str.append("\n111\t" + icd11Con.getCodeId() + "\t" + icd11Con.getValue() + "\t0\t" + token);
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

		outputDeveloperWriter = prepareOutputFile("developerResults", "ICD11 Matching Results");
		outputTerminologistWriter = prepareOutputFile("terminologistResults", "ICD11 Matching Results");

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
				"(atoms.codeId: XH* OR atoms.codeId: 2*) AND \"Carcinoma\" AND \"in situ\"", pfsLimitless, authToken);

		System.out.println("Have returned : " + fullStringResults.getTotalCount() + " objects");
		int matches = 0;
		for (SearchResult result : fullStringResults.getObjects()) {
			System.out.println(result.getCodeId() + "\t" + result.getValue());
		}

		Set<SearchResult> filteredIcd11List = new HashSet<>();
		System.out.println("\n\n\nNow Filtering");
		for (SearchResult result : fullStringResults.getObjects()) {
			if (isNeoplasmMatch(result)) {
				System.out.println(result.getCodeId() + "\t" + result.getValue());
				filteredIcd11List.add(result);
				matches++;
			}
		}
		System.out.println("Have actually found : " + matches + " matches");

		return filteredIcd11List;
	}

	private boolean isNeoplasmMatch(SearchResult result) {
		if ((result.getCodeId().startsWith("XH") || result.getCodeId().startsWith("2"))
				&& result.getValue().toLowerCase().matches(".*\\bcarcinoma\\b.*")
				&& result.getValue().toLowerCase().matches(".*\\bin situ\\b.*") && result.isLeafNode()) {
			return true;
		}

		return false;
	}

	private SearchResultList testRule1FindingSite(String queryPortion) throws Exception {
		if (!alreadyLookedUpTokenCache.containsKey(queryPortion)) {
			final SearchResultList straightMatch = client.findConcepts(targetTerminology, targetVersion,
					"(atoms.codeId: XH* OR atoms.codeId: 2*) AND \"Carcinoma\" AND \"in situ\" AND " + queryPortion,
					pfsLimited, authToken);

			alreadyLookedUpTokenCache.put(queryPortion, straightMatch);
		}

		return alreadyLookedUpTokenCache.get(queryPortion);
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
