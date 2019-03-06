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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * @author Jesse Efron
 */
@Mojo(name = "ecl-output", defaultPhase = LifecyclePhase.PACKAGE)
public class BasicEclMojo extends AbstractMojo {

	/** The run config file path. */
	@Parameter
	private String runConfig;

	/**
	 * Name of terminology to be loaded.
	 */
	private String terminology = "SNOMEDCT";

	/**
	 * The version.
	 */
	private String version = "latest";

	/**
	 * The searchTerm.
	 */
	@Parameter
//	private String eclDesc = "<< 55342001 | Neoplastic Disease : ( 116676008 | Associated Morphology | =   << 109355002 | Carcinoma in situ | ";
	private String eclDesc = "<< 55342001 : 116676008 = 399919001";

	/** The user name. */
	@Parameter
	private String userName;

	/** The user password. */
	@Parameter
	private String userPassword;

	/** The partial df. */
	private final DateTimeFormatter partialDf = DateTimeFormatter.ofPattern("_dd_HH-mm");

	/** The output file path for results. */
	private String outputFilePath;

	@Override
	public void execute() throws MojoFailureException {
		try {

			getLog().info("ECL Mojo");
			getLog().info("  runConfig = " + runConfig);
			getLog().info("  terminology = " + terminology);
			getLog().info("  version = " + version);
			getLog().info("  ecl = " + eclDesc);
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
			if (eclDesc == null || eclDesc.isEmpty()) {
				throw new Exception("Must specify an ecl expression");
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

			PrintWriter outputFile = prepareOutputFile();

			pfs.setExpression(eclDesc);

			final SearchResultList results = client.findConcepts(terminology, version, null, pfs, authToken);
			getLog().info("Have " + results.getTotalCount() + " ECL Results");

			writeResults(results, outputFile);
			outputFile.close();

			getLog().info("");
			getLog().info("Finished processing...");
			getLog().info("Output avaiable at: " + outputFilePath);

		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException("Unexpected exception:", e);
		}
	}

	private void writeResults(SearchResultList results, PrintWriter outputFile) throws Exception {
		for (SearchResult result : results.getObjects()) {
			outputFile.write(result.getTerminologyId());
			outputFile.write("\t");
			outputFile.print(result.getValue());
			outputFile.println();

			System.out.println(result.getTerminologyId() + "\t" + result.getValue());
		}
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

		File userFolder = new File("ecl-results");
		userFolder.mkdirs();

		// Setup Description File
		File fd = new File(userFolder.getPath() + File.separator + "eclOutput-" + month + timestamp + ".xls");
		outputFilePath = fd.getAbsolutePath();
		getLog().info("Creating file at: " + outputFilePath);

		final FileOutputStream fos = new FileOutputStream(fd);
		final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter pw = new PrintWriter(osw);

		pw.write("Concept Id");
		pw.write("\t");
		pw.print("Concept Description");
		pw.write("\t");

		pw.println();
		return pw;
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

		return properties;
	}
}
