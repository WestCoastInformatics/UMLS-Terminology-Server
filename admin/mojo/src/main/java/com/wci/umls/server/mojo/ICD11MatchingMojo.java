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
			"387910009", "127882003", "64033007", "117590005", "21514008");

	private List<String> knownMissingTopLevelBSCons = Arrays.asList("89837001", "57222008", "87953007");

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

	private List<String> nonFindingSiteStrings = Arrays.asList("of", "part", "structure", "system", "and/or", "and", "region", "area");

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
			pfsEcl.setExpression("<< 55342001 : 116676008 = 399919001");

			Map<String, SctNeoplasmConcept> concepts = null;
			if (!testing) {
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
		for (SctNeoplasmConcept sctCon : snomedConcepts.values()) {

			StringBuffer titleStr = new StringBuffer();
			String findingSite = getRelDestination(sctCon, "Finding site");

			String newConInfoStr = "\n\n\n# " + ++counter + " Testing on sctCon: " + sctCon.getName() + "\twith Id: "
					+ sctCon.getConceptId() + "\twith findingSite: " + findingSite;

			// if (!sctCon.getConceptId().equals("92672004")) { continue; }

			if (counter > 2) {
				break;
			}

			
			titleStr.append(newConInfoStr);
			loggingWriter.println(newConInfoStr);

			StringBuffer str = new StringBuffer();
			Map<Concept, Set<String>> potentialFSConTerms = identifyPotentialFSConcepts(findingSite);

			boolean foundMatch = matchApproach1(findingSite, icd11Targets, str);
			foundMatch = matchApproach2(findingSite, str) || foundMatch;

			if (potentialFSConTerms != null) {
				foundMatch = matchApproach3(icd11Targets, potentialFSConTerms, str, loggingWriter)
						|| foundMatch;
				foundMatch = matchApproach4(potentialFSConTerms, str, loggingWriter) || foundMatch;
			}

			loggingWriter.flush();
			if (foundMatch) {
				System.out.print(titleStr.toString());
				System.out.println(str.toString());
			} else {
				System.out.println(titleStr.toString());
				noMatchList.add(sctCon.getConceptId() + "\t" + sctCon.getName());
			}

		}

		loggingWriter.close();

		System.out.println("\n\n\nCouldn't Match the following: ");
		for (String s : noMatchList) {
			System.out.println(s);
		}
	}

	private boolean matchApproach3(Set<SearchResult> icd11Targets,
			Map<Concept, Set<String>> potentialFSConTerms, StringBuffer str, PrintWriter loggingWriter) {
		boolean foundMatch = false;
		Set<String> alreadyQueried = new HashSet<>();

		for (Concept testCon : potentialFSConTerms.keySet()) {
			Set<String> normalizedStrings = potentialFSConTerms.get(testCon);
			
			for (String normalizedStr : normalizedStrings) {
				String[] tokens = normalizedStr.toLowerCase().split(" ");

				for (int i = 0; i < tokens.length; i++) {
					if (!alreadyQueried.contains(tokens[i]) && !nonFindingSiteStrings.contains(tokens[i])) {
						alreadyQueried.add(tokens[i]);

						for (SearchResult icd11Con : icd11Targets) {
							if (icd11Con.getValue().toLowerCase().matches(".*\\b" + tokens[i] + "\\b.*")) {
								str.append("\n3333 Potential Match (" + tokens[i] + ") : " + icd11Con.getValue() + " with Id: "
										+ icd11Con.getTerminologyId());
								foundMatch = true;
							}
						}
					}
				}
			}
		}

		return foundMatch;
	}

	private boolean matchApproach4(Map<Concept, Set<String>> potentialFSConTerms, StringBuffer str,
			PrintWriter loggingWriter) throws Exception {
		boolean foundMatch = false;

		for (Concept testCon : potentialFSConTerms.keySet()) {
			Set<String> normalizedStrings = potentialFSConTerms.get(testCon);
			for (String normalizedStr : normalizedStrings) {
				loggingWriter.print("\nWithin 4444 - Now try against: " + testCon.getName() + " ("
						+ testCon.getTerminologyId() + ") using: " + normalizedStr);

				foundMatch = testFindingSite("4444", normalizedStr, str) || foundMatch;
			}
		}

		return foundMatch;
	}

	private Map<Concept, Set<String>> identifyPotentialFSConcepts(String findingSite) throws Exception {

		// Get the finding site as a concept
		SctNeoplasmConcept fsConcept = getSctConceptFromDesc(findingSite);

		// Get all fsCon's ancestors
		final ConceptList ancestorResults = client.findAncestorConcepts(fsConcept.getConceptId(), sourceTerminology,
				sourceVersion, false, pfsLimitless, authToken);

		// Find the body structure hierarchy it falls under
		String topLevelSctId = null;
		for (Concept ancestor : ancestorResults.getObjects()) {
			if (topLevelBodyStructureIds.contains(ancestor.getTerminologyId())) {
				topLevelSctId = ancestor.getTerminologyId();
				break;
			}
		}

		// Have list of possibleFindingSites. Test them for matches
		if (topLevelSctId == null) {
			if (!knownMissingTopLevelBSCons.contains(fsConcept.getConceptId())) {
				System.out
						.println("ERROR ERROR ERROR: Found a finding site without an identified top level BS ancestor: "
								+ fsConcept.getConceptId() + "---" + fsConcept.getName());
			}

			return null;
		}

		// TODO: Because can't do ancestors via ECL, need this work around
		// Identify all descendants of top level bodyStructure concept
		pfsEcl.setExpression("<< " + topLevelSctId);
		final SearchResultList descendentResults = client.findConcepts(sourceTerminology, sourceVersion, null, pfsEcl,
				authToken);

		// Create a list of concepts that are both ancestors of fsConcept and
		// descendents of topLevelBodyStructure Concept
		// TODO: This could be a Rest Call in of itself
		Map<Concept, Set<String>> potentialFSConTerms = new HashMap<>();
		for (Concept ancestor : ancestorResults.getObjects()) {
			for (SearchResult potentialFindingSite : descendentResults.getObjects()) {
				if (ancestor.getTerminologyId().equals(potentialFindingSite.getTerminologyId())) {
					Concept mapCon = client.getConcept(ancestor.getTerminologyId(), sourceTerminology, sourceVersion,
							null, authToken);
					Set<String> bucket = new HashSet<>();
					potentialFSConTerms.put(mapCon, bucket);
					break;
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

					if (normalizedString.equals("vas")) {
						int a = 1;
					}
					if (!potentialFSConTerms.get(testCon).contains(normalizedString)) {
						potentialFSConTerms.get(testCon).add(normalizedString);
					}
				}
			}
		}

		return potentialFSConTerms;

	}

	private boolean matchApproach2(String findingSite, StringBuffer str) throws Exception {
		SctNeoplasmConcept fsConcept = getSctConceptFromDesc(findingSite);

		boolean matchFound = false;
		for (SctNeoplasmDescription desc : fsConcept.getDescs()) {
			matchFound = testFindingSite("2222", desc.getDescription(), str) || matchFound;

		}

		return matchFound;
	}

	private boolean matchApproach1(String findingSite, Set<SearchResult> icd11Targets, StringBuffer str)
			throws Exception {
		boolean foundMatch = false;

		for (SearchResult icd11Con : icd11Targets) {
			String[] tokens = findingSite.toLowerCase().split(" ");
			for (int i = 0; i < tokens.length; i++) {
				if (!nonFindingSiteStrings.contains(tokens[i])) {
					if (icd11Con.getValue().toLowerCase().matches(".*\\b" + tokens[i] + "\\b.*")) {
						str.append("\n1111 Potential Match (\" + normalizedStr + \") : " + icd11Con.getValue() + " with Id: "
								+ icd11Con.getTerminologyId());
						foundMatch = true;
					}
				}
			}
		}

		return foundMatch;
	}

	private void preProcessing() {
		for (String sctId : topLevelBodyStructureIds) {

		}
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
			if ((result.getTerminologyId().startsWith("XH") || result.getTerminologyId().startsWith("2"))
					&& result.getValue().toLowerCase().contains("carcinoma")
					&& (result.getValue().toLowerCase().contains("in situ ")
							|| result.getValue().toLowerCase().contains("in situ"))) {
				System.out.println(result.getTerminologyId() + "\t" + result.getValue());
				filteredIcd11List.add(result);
				matches++;
			}
		}
		System.out.println("Have actually found : " + matches + " matches");

		return filteredIcd11List;
	}

	private boolean testFindingSite(String testPrefix, String queryPortion, StringBuffer str) throws Exception {
		boolean foundMatch = false;

		final SearchResultList straightMatch = client.findConcepts(targetTerminology, targetVersion,
				"(terminologyId: XH* OR terminologyId: 2*) AND \"Carcinoma\" AND \"in situ\" AND " + "\"" + queryPortion
						+ "\"",
				pfsLimited, authToken);

		for (SearchResult result : straightMatch.getObjects()) {
			if ((result.getTerminologyId().startsWith("XH") || result.getTerminologyId().startsWith("2"))
					&& result.getValue().toLowerCase().contains("carcinoma")
					&& (result.getValue().toLowerCase().contains("in situ ")
							|| result.getValue().toLowerCase().contains("in situ"))) {
				str.append("\n" + testPrefix + " with score: " + result.getScore() + " matched "
						+ result.getTerminologyId() + "\t" + result.getValue());
				foundMatch = true;
			}
		}

		return foundMatch;
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
