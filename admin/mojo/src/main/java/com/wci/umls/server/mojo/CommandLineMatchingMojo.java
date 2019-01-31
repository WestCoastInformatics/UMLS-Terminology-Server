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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.helpers.SearchResultListJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.services.SecurityService;

/**
 * Used to search the term-server database for concepts with a matching string.
 * 
 * See matcher/pom.xml for a sample invocation.
 *
 * @author Jesse Efron
 */
@Mojo(name = "match-term", defaultPhase = LifecyclePhase.PACKAGE)
public class CommandLineMatchingMojo extends AbstractMojo {

	private static final String NCI_MTH = "NCIMTH";

	private static final String NCI_MTH_VERSION = "latest";

	/** The run config file path. */
	@Parameter
	private String runConfig;

	/**
	 * Name of terminology to be loaded.
	 */
	@Parameter
	private String terminology;

	/**
	 * The version.
	 */
	@Parameter
	private String version;

	/**
	 * The max concepts per term.
	 */
	@Parameter
	private Integer maxCount = 10;

	/**
	 * The searchTerm.
	 */
	@Parameter
	private String searchTerm;

	/**
	 * The searchFile.
	 */
	@Parameter
	private String searchTermsFilepath;

	/** The user name. */
	@Parameter
	private String userName;

	/** The user password. */
	@Parameter
	private String userPassword;

	/** The partial df. */
	private final DateTimeFormatter partialDf = DateTimeFormatter.ofPattern("_dd_HH-mm");

	/** The output file path. */
	private String outputFilePath;

	/** The acronym expansion map. */
	private Map<String, Set<String>> acronymExpansionMap = new HashMap<>();

	@Override
	public void execute() throws MojoFailureException {
		try {

			getLog().info("Matching Mojo");
			getLog().info("  runConfig = " + runConfig);
			getLog().info("  terminology = " + terminology);
			getLog().info("  version = " + version);
			getLog().info("  maxCount = " + maxCount);
			getLog().info("  searchTerm = " + searchTerm);
			getLog().info("  userName = " + userName);

			/*
			 * Error Checking
			 */
			if (terminology == null || terminology.isEmpty()) {
				throw new Exception("Must define a terminology to search against i.e. SNOMEDCT");
			}
			if (version == null || version.isEmpty()) {
				throw new Exception("Must define a version to search against i.e. latest");
			}
			if (searchTerm != null && !searchTerm.isEmpty() && searchTermsFilepath != null
					&& !searchTermsFilepath.isEmpty()) {
				throw new Exception("Must either specify a search term or a search file path, but not both");
			}

			if ((searchTerm == null || searchTerm.isEmpty())
					&& (searchTermsFilepath == null || searchTermsFilepath.isEmpty())) {
				throw new Exception(
						"Must either specify a search term or a search file path, but neither were defined");
			}

			if (maxCount < 0) {
				throw new Exception("Must select a positive integer for max count.");
			}

			/*
			 * Setup
			 */
			Properties properties = setupProperties();

			final ContentClientRest client = new ContentClientRest(properties);
			final SecurityService service = new SecurityServiceJpa();
			final String authToken = service.authenticate(userName, userPassword).getAuthToken();
			service.close();

			PfsParameterJpa pfs = new PfsParameterJpa();
			pfs.setStartIndex(0);
			pfs.setMaxResults(3 * maxCount);

			PrintWriter outputFile = prepareOutputFile();

			/*
			 * Process Terms
			 */
			if (searchTerm != null && !searchTerm.isEmpty()) {
				findMatchesAndWriteResults(client, outputFile, terminology, version, searchTerm, pfs, authToken);
			} else if (searchTermsFilepath != null && !searchTermsFilepath.isEmpty()) {
				final BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(searchTermsFilepath)));

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					findMatchesAndWriteResults(client, outputFile, terminology, version, line, pfs, authToken);
				}

				bufferedReader.close();
			}

			outputFile.close();

			getLog().info("");
			getLog().info("Finished processing...");
			getLog().info("Output avaiable at: " + outputFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException("Unexpected exception:", e);
		}
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
				
				if (terminology.equals(NCI_MTH)) {
					// Terminology is NCI-MTH, no further processing. Just return results.
					final SearchResultList results = client.findConcepts(NCI_MTH.toString(), NCI_MTH_VERSION, term, pfs, authToken);

					accumilatedResults.getObjects().addAll(results.getObjects());
					accumilatedResults.setTotalCount(accumilatedResults.getTotalCount() + results.getTotalCount());
				} else {
					// Terminology is not NCI-MTH, need to ensure that concepts considered matches contain at least one atom from the requested terminology
					final String query = term + " AND atoms.terminology: " + terminology;
					final SearchResultList results2 = client.findConcepts(NCI_MTH.toString(), NCI_MTH_VERSION, query, pfs, authToken);
					
					accumilatedResults.getObjects().addAll(results2.getObjects());
					accumilatedResults.setTotalCount(accumilatedResults.getTotalCount() + results2.getTotalCount());
				}
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

	/**
	 * Setup properties.
	 *
	 * @return the properties
	 * @throws Exception
	 *             the exception
	 */
	private Properties setupProperties() throws Exception {
		// Handle creating the database if the mode parameter is set
		if (runConfig != null && !runConfig.isEmpty()) {
			System.setProperty("run.config." + ConfigUtility.getConfigLabel(), runConfig);
		}
		final Properties properties = ConfigUtility.getConfigProperties();

		// authenticate
		if (userName == null || userPassword == null) {
			userName = properties.getProperty("viewer.user");
			userPassword = properties.getProperty("viewer.password");
		}

		if (properties.containsKey("search.handler.ATOMCLASS.acronymsFile")) {
			final BufferedReader in = new BufferedReader(
					new FileReader(new File(properties.getProperty("search.handler.ATOMCLASS.acronymsFile"))));
			String fileLine;
			while ((fileLine = in.readLine()) != null) {
				String[] tokens = FieldedStringTokenizer.split(fileLine, "\t");
				if (!acronymExpansionMap.containsKey(tokens[0])) {
					acronymExpansionMap.put(tokens[0], new HashSet<String>(2));
				}
				acronymExpansionMap.get(tokens[0]).add(tokens[1]);
			}
			in.close();
		} else {
			throw new Exception("Required property acronymsFile not present.");
		}

		return properties;
	}

	/**
	 * Prepare output file.
	 *
	 * @return the prints the writer
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws UnsupportedEncodingException
	 *             the unsupported encoding exception
	 */
	private PrintWriter prepareOutputFile() throws FileNotFoundException, UnsupportedEncodingException {

		final LocalDateTime now = LocalDateTime.now();
		final String timestamp = partialDf.format(now);
		final String month = now.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());

		File userFolder = new File("results" + File.separator + userName);
		userFolder.mkdirs();
		File f = new File(userFolder.getPath() + File.separator + "matcherOutput-" + terminology + "-" + month
				+ timestamp + ".xls");
		outputFilePath = f.getAbsolutePath();
		getLog().info("Creating file at: " + outputFilePath);

		final FileOutputStream fos = new FileOutputStream(f);
		final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");

		PrintWriter pw = new PrintWriter(osw);
		pw.write("Search Term");
		pw.write("\t");
		pw.write("Concept Id");
		pw.write("\t");
		pw.print("Concept Description");
		pw.write("\t");
		pw.println("Score");

		return pw;
	}
}
