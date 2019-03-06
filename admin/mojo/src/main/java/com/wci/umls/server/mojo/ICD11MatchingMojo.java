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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.model.SctRelationship;
import com.wci.umls.server.rest.client.ContentClientRest;

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

	/** Name of terminology to be loaded. */
	private String sourceTerminology = "SNOMEDCT";

	/** The version. */
	private String sourceVersion = "latest";

	/** Name of terminology to be loaded. */
	private String targetTerminology = "icd11";

	/** The version. */
	private String targetVersion = "latest";

	private boolean analysis = true;

	@Override
	public void execute() throws MojoFailureException {
		try {

			getLog().info("Matching Mojo");
			getLog().info("  runConfig = " + runConfig);
			getLog().info("  source terminology = " + sourceTerminology);
			getLog().info("  source version = " + sourceVersion);
			getLog().info("  target terminology = " + targetTerminology);
			getLog().info("  target version = " + targetVersion);
			getLog().info("  maxCount = " + maxCount);
			getLog().info("  userName = " + userName);
			getLog().info("  analysis = " + analysis);

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

			if (maxCount < 0) {
				throw new Exception("Must select a positive integer for max count.");
			}

			/*
			 * Setup
			 */
			setup("icd11-matcher");

			// Get ECL Results
			PfsParameterJpa eclPfs = new PfsParameterJpa();
			eclPfs.setExpression("<< 55342001 : 116676008 = 399919001");
			eclPfs.setStartIndex(0);
			eclPfs.setMaxResults(8000);

			final SearchResultList eclResults = client.findConcepts(sourceTerminology, sourceVersion, null, eclPfs, authToken);
			getLog().info("With ECL, have: " + eclResults.getObjects().size());
			Map<String, SctNeoplasmConcept> concepts = processEclQuery(eclResults);

			if (analysis) {
				printOutNonIsaRels(concepts);
			} else {
				// Process Terms
				executeRule1(concepts, client, pfs, authToken);
			}

			getLog().info("");
			getLog().info("Finished processing...");
			getLog().info("Output avaiable at: " + outputFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException("Unexpected exception:", e);
		}
	}

	private void printOutNonIsaRels(Map<String, SctNeoplasmConcept> concepts) throws Exception {
		// Setup File
		PrintWriter writer = prepareRelOutputFile("nonIsaRels", "ECL Analysis");

		for (SctNeoplasmConcept con : concepts.values()) {
			for (SctRelationship rel : con.getRels()) {
				if (rel.getRelationshipType() != "Is a") {
					exportRels(rel, con.getConceptId(), writer);
				}
			}
		}

		writer.close();
	}

	private void executeRule1(Map<String, SctNeoplasmConcept> concepts, ContentClientRest client, PfsParameterJpa pfs, String authToken)
			throws Exception {
		for (SctNeoplasmConcept con : concepts.values()) {
			// Try the full string, then just pathology
			final SearchResultList fullStringResults = client.findConcepts(targetTerminology, targetVersion, con.getName(), pfs, authToken);
			
			// Try pathology. See how many per concept
			Set<String> conPathologies = new HashSet<>();
			for (SctNeoplasmDescription desc : concepts.get(con.getConceptId()).getDescs()) {
				conPathologies.add(desc.getPathology());
			}
			
			for (String pathology : conPathologies) {
				String query = pathology + " AND concept.code: 2*";
				final SearchResultList pathologyResults = client.findConcepts(targetTerminology, targetVersion, query , pfs, authToken);
				int i = 1;
			}
			
			// Try histopathology. See how many per concept
			Set<String> conHistopathologies = new HashSet<>();
			for (SctNeoplasmDescription desc : concepts.get(con.getConceptId()).getDescs()) {
				conHistopathologies.add(desc.getBodyStructure());
			}
			
			for (String pathology : conHistopathologies) {
				String query = pathology + " AND concept.code: xh*";
				final SearchResultList histopathologyResults = client.findConcepts(targetTerminology, targetVersion, query , pfs, authToken);
				int i = 1;
			}
		}
	}

	private Map<String, SctNeoplasmConcept> processEclQuery(SearchResultList eclResults) throws Exception {
		Map<String, SctNeoplasmConcept> concepts = new HashMap<>();
		setupDescParser();

		for (SearchResult result : eclResults.getObjects()) {

			// Get Desc
			Concept clientConcept = client.getConcept(result.getId(), null, authToken);
			SctNeoplasmConcept con = new SctNeoplasmConcept(result.getTerminologyId(), result.getValue());

			for (Atom atom : clientConcept.getAtoms()) {
				if (!atom.isObsolete() && !atom.getTermType().equals("Fully specified name")
						&& !atom.getTermType().equals("Definition")) {
					SctNeoplasmDescription desc = descParser.parse(atom.getName());
					con.getDescs().add(desc);
				}
			}

			// Get Associated Rels
			RelationshipList relsList = client.findConceptRelationships(result.getTerminologyId(), sourceTerminology,
					sourceVersion, null, new PfsParameterJpa(), authToken);

			for (final Relationship<?, ?> relResult : relsList.getObjects()) {
				SctRelationship rel = relParser.parse(result.getValue(), relResult);
				if (rel != null) {
					con.getRels().add(rel);
				}
			}

			concepts.put(result.getTerminologyId(), con);
		}

		return concepts;
	}

	/**
	 * Find matches to input term and write results.
	 *
	 * @param client
	 *            the client
	 * @param outputFile
	 *            the output file
	 * @param terminology
	 *            the terminology
	 * @param version
	 *            the version
	 * @param line
	 *            the line
	 * @param pfs
	 *            the pfs
	 * @param authToken
	 *            the auth token
	 * @throws Exception
	 *             the exception
	 */
	private void findMatchesAndWriteResults(ContentClientRest client, PrintWriter outputFile, String terminology,
			String version, String line, PfsParameterJpa pfs, String authToken) throws Exception {
		if (!line.trim().isEmpty()) {
			getLog().info("");
			getLog().info("Processing on: " + line);

			String originalLine = line;

			// TODO: This needs a better solution as was workaround for some terminologies
			line = line.replaceAll(",", "");
			line = line.replaceAll("[(&)]", "");

			Set<String> linesToTest = applyAcronyms(line);

			SearchResultList accumilatedResults = new SearchResultListJpa();

			for (String term : linesToTest) {

				// Terminology is NCI-MTH, no further processing. Just return results.
				final SearchResultList results = client.findConcepts(terminology, version, term, pfs, authToken);

				accumilatedResults.getObjects().addAll(results.getObjects());
				accumilatedResults.setTotalCount(accumilatedResults.getTotalCount() + results.getTotalCount());
			}

			// Sort the results in order to handle acronym expansion
			accumilatedResults.sortBy((SearchResult o1, SearchResult o2) -> o2.getScore().compareTo(o1.getScore()));

			writeResultsToFile(outputFile, originalLine, accumilatedResults, linesToTest.size() > 1);
		}
	}

	/**
	 * Apply acronyms to each line.
	 *
	 * @param term
	 *            the term
	 * @return the sets the
	 * @throws Exception
	 *             the exception
	 */
	private Set<String> applyAcronyms(String term) throws Exception {
		Set<String> linesToExpand = new HashSet<>();
		Set<String> expanded = new HashSet<>();
		linesToExpand.add(term);

		for (String key : acronymExpansionMap.keySet()) {
			Pattern pattern = Pattern.compile("\\b" + key + "\\b");

			// Presumes single abbreviation of a given key per term. Otherwise, need to
			// enhance e.g. "AB condition with AB"
			for (String line : linesToExpand) {
				Matcher matcher = pattern.matcher(line);

				while (matcher.find()) {
					for (String expansion : acronymExpansionMap.get(key)) {
						expanded.add(line.substring(0, matcher.start()) + expansion + line.substring(matcher.end()));
					}
				}

				expanded.add(line);
			}

			linesToExpand = expanded;
		}

		// Provide the original if expansion shouldn't have been done.
		linesToExpand.add(term);

		if (linesToExpand.size() > 1) {
			// Have expansion, list them
			for (String expansion : linesToExpand) {
				if (!expansion.equals(term)) {
					getLog().info("Have expanded acronyms on term and will query with: " + expansion);
				}
			}

			getLog().info("Will also query with the original term: " + term);
		}

		return linesToExpand;
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
			if (maxCount != null && ++counter > maxCount && lastScore != singleResult.getScore()) {
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
}
