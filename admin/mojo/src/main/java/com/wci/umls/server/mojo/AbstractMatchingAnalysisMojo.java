package com.wci.umls.server.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.mojo.model.SctRelationship;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.services.SecurityService;

abstract public class AbstractMatchingAnalysisMojo extends AbstractMojo {
	protected String sourceTerminology;
	protected String sourceVersion;
	protected String targetTerminology;
	protected String targetVersion;

	protected File userFolder;

	protected String timestamp;

	protected String month;

	/** The user name. */
	@Parameter
	protected String userName;

	/** The user password. */
	@Parameter
	protected String userPassword;

	/** The partial df. */
	protected final DateTimeFormatter partialDf = DateTimeFormatter.ofPattern("_dd_HH-mm");

	final static protected SctRelationshipParser relParser = new SctRelationshipParser();
	static protected SctNeoplasmDescriptionParser descParser = null;


	protected ContentClientRest client;

	protected String authToken;

	protected PfsParameterJpa pfs;

	/**
	 * The max concepts per term.
	 */
	@Parameter
	protected Integer maxCount = 10;

	/** The run config file path. */
	@Parameter
	protected String runConfig;

	/** The acronym expansion map. */
	protected Map<String, Set<String>> acronymExpansionMap = new HashMap<>();

	protected void setup(String folderName) throws Exception {
		LocalDateTime now = LocalDateTime.now();
		timestamp = partialDf.format(now);
		month = now.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault());

		userFolder = new File(folderName);
		userFolder.mkdirs();

		Properties properties = setupProperties();

		client = new ContentClientRest(properties);
		final SecurityService service = new SecurityServiceJpa();
		authToken = service.authenticate(userName, userPassword).getAuthToken();
		service.close();

		pfs = new PfsParameterJpa();
		pfs.setStartIndex(0);
		pfs.setMaxResults(3 * maxCount);
		
	}

	protected void setupDescParser() {
		if (descParser == null) {
			descParser = new SctNeoplasmDescriptionParser();
		}
	}
	/**
	 * Setup properties.
	 *
	 * @return the properties
	 * @throws Exception
	 *             the exception
	 */
	protected Properties setupProperties() throws Exception {
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

/*
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
*/
		return properties;
	}

	protected PrintWriter prepareRelOutputFile(String filePrefix, String outputDescription)
			throws FileNotFoundException, UnsupportedEncodingException {
		File fd = new File(userFolder.getPath() + File.separator + filePrefix + "-" + month + timestamp + ".xls");
		getLog().info("Creating " + outputDescription + " file (" + filePrefix + ") at: " + fd.getAbsolutePath());

		final FileOutputStream fos = new FileOutputStream(fd);
		final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter pw = new PrintWriter(osw);
		pw.write("Concept Id");
		pw.write("\t");
		pw.print("Concept Name");
		pw.write("\t");
		pw.print("Relationship Type");
		pw.write("\t");
		pw.print("Relationship Destination");
		pw.write("\t");
		pw.print("Role Group");

		pw.println();
		return pw;
	}

	protected void exportRels(SctRelationship rel, String conId, PrintWriter outputFile) throws Exception {
		if (rel != null) {
			outputFile.print(conId);
			outputFile.print("\t");
			outputFile.print(rel.printForExcel());

			outputFile.println();
		}
	}
}
