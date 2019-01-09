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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.services.SecurityService;

/**
 * Used to search the term-server database for concepts with a matching string.
 * 
 * See matcher/pom.xml for a sample invocation.
 *
 * @author ${author}
 */
@Mojo(name = "match-term", defaultPhase = LifecyclePhase.PACKAGE)
public class CommandLineMatchingMojo extends AbstractMojo {

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
	private Integer maxCount;

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

	/**  The user name. */
	@Parameter
	private String userName;

	/**  The user password. */
	@Parameter
	private String userPassword;

	/** The partial df. */
	private final DateTimeFormatter partialDf = DateTimeFormatter.ofPattern("_dd_HH-mm");

	/** The output file path. */
	private String outputFilePath;

	/* see superclass */
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
			if (searchTerm != null && !searchTerm.isEmpty() && searchTermsFilepath != null && !searchTermsFilepath.isEmpty()) {
				throw new Exception("Must either specify a search term or a search file path, but not both");
			}

			if ((searchTerm == null || searchTerm.isEmpty()) && (searchTermsFilepath == null || searchTermsFilepath.isEmpty())) {
				throw new Exception(
						"Must either specify a search term or a search file path, but neither were defined");
			}

			/*
			 * Setup
			 */
			// Handle creating the database if the mode parameter is set
			if (runConfig != null && !runConfig.isEmpty()) {
				System.setProperty("run.config." + ConfigUtility.getConfigLabel(), runConfig);
			}
			final Properties properties = ConfigUtility.getConfigProperties();
			final ContentClientRest client = new ContentClientRest(properties);

			// authenticate
			if (userName == null || userPassword == null) {
				userName = properties.getProperty("viewer.user");
				userPassword = properties.getProperty("viewer.password");
			}

			final SecurityService service = new SecurityServiceJpa();
			final String authToken = service.authenticate(userName, userPassword).getAuthToken();
			service.close();
			
		    PfsParameterJpa pfs = new PfsParameterJpa();
		    pfs.setStartIndex(0);
		    pfs.setMaxResults(maxCount*2);

			/*
			 * Make the call
			 */
			PrintWriter outputFile = prepareOutputFile();

			if (searchTerm != null && !searchTerm.isEmpty()) {
				findConceptsAndProcessResults(client, outputFile, terminology, version, searchTerm, pfs, authToken);
			} else if (searchTermsFilepath != null && !searchTermsFilepath.isEmpty()) {
				final BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(searchTermsFilepath)));

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					findConceptsAndProcessResults(client, outputFile, terminology, version, line, pfs, authToken);
				}

				bufferedReader.close();
			}

			outputFile.close();

			getLog().info("Output avaiable at: " + outputFilePath);
			getLog().info("done ...");
		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException("Unexpected exception:", e);
		}
	}

	/**
	 * Find concepts and process results.
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
	 * @param authToken
	 *            the auth token
	 * @throws Exception
	 *             the exception
	 */
	private void findConceptsAndProcessResults(ContentClientRest client, PrintWriter outputFile, String terminology,
			String version, String line, PfsParameterJpa pfs, String authToken) throws Exception {
		getLog().info("Processing on: " + line);

		// TODO: This needs a better solution as was workaround for some terminologies 
		line = line.replaceAll(",", "");
		line = line.replaceAll("[(&)]", "");

		// TODO: This needs to use the acronyms file
		line = applyAcronyms(line);
		
		final SearchResultList results = client.findConcepts(terminology, version, line, pfs, authToken);

		if (maxCount == null) {
			getLog().info("Found " + results.getTotalCount() + " results and outputing all");
		} else {
			getLog().info(
					"Found " + results.getTotalCount() + " results and per request, outputing at most " + maxCount);
		}

		writeResultsToFile(outputFile, line, results);
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
		File f = new File(userFolder.getPath() + File.separator + "matcherOutput-" + terminology + "-" + month + timestamp + ".xls");
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

	/**
	 * Write results to file.
	 *
	 * @param outputFile
	 *            the output file
	 * @param line
	 *            the line
	 * @param results
	 *            the results
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void writeResultsToFile(PrintWriter outputFile, String line, SearchResultList results) throws IOException {
		outputFile.write(line);

		int counter = 0;
		float lastScore = 0;
		for (SearchResult singleResult : results.getObjects()) {
			if (maxCount != null && ++counter > maxCount && lastScore != singleResult.getScore()) {
				break;
			}

			getLog().info("    match" + (counter) + " = " + singleResult.getTerminologyId() + " | "
					+ singleResult.getValue() + " | " + "with score: " + singleResult.getScore() + " | ");

			outputFile.write("\t");
			outputFile.write(singleResult.getTerminologyId());
			outputFile.write("\t");
			outputFile.print(singleResult.getValue());
			outputFile.write("\t");
			outputFile.println(singleResult.getScore());

			lastScore = singleResult.getScore();
		}

		outputFile.println();
		outputFile.flush();
	}

	private String applyAcronyms(String line) {
		boolean found = true;
		
		while (found) {
			found = false;
			
			if (line.contains("FTP")) {
				line = replace(line, "FTP", "Flexible Transgastric Peritoneoscopy");
				found = true;
			} 
			if (line.contains("ATAC")) {
				line = replace(line, "ATAC", "Assay Transposase Accessible Chromatin");
				found = true;
			} 
			if (line.contains("EFVPTC")) {
				line = replace(line, "EFVPTC", "Encapsulated Follicular Variant of Papillary Thyroid Carcinoma");
				found = true;
			} 
			if (line.contains("IDH")) {
				line = replace(line, "IDH", "Isocitrate Dehydrogenase");
				found = true;
			} 
		}	
		
		return line;
	}

	private String replace(String line, String acronym, String expanded) {
		int beginIdx = line.indexOf(acronym);
		int endIdx = beginIdx + acronym.length();
		
		return line.substring(0, beginIdx) + expanded + line.substring(endIdx);
	}
}
