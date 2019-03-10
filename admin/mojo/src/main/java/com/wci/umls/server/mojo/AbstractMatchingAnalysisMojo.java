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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.ConceptList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.SctNeoplasmConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
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

	protected PfsParameterJpa pfsEcl = new PfsParameterJpa();
	protected PfsParameterJpa pfsLimitless = new PfsParameterJpa();
	protected PfsParameterJpa pfsLimited = new PfsParameterJpa();
	protected PfsParameterJpa pfsMinimal = new PfsParameterJpa();
	protected PfsParameterJpa pfsCustom = null;

	/** The run config file path. */
	@Parameter
	protected String runConfig;

	/** The acronym expansion map. */
	protected Map<String, Set<String>> acronymExpansionMap = new HashMap<>();
	protected HashMap<String, SctNeoplasmConcept> conceptsFromDescs = new HashMap<>();
	protected Map<String, Map<Concept, Set<String>>> findingSitePotentialTermsMapCache = new HashMap<>();

	protected List<String> topLevelBodyStructureIds = Arrays.asList("86762007", "20139000", "39937001", "81745001",
			"387910009", "127882003", "64033007", "117590005", "21514008", "76752008", "113331007", "363667005",
			"31610004");

	protected List<String> nonFindingSiteStrings = Arrays.asList("of", "part", "structure", "system", "and/or", "and",
			"region", "area", "or", "the");

	protected void setup(String folderName, String st, String sv, String tt, String tv) throws Exception {
		sourceTerminology = st;
		sourceVersion = sv;
		targetTerminology = tt;
		targetVersion = tv;

		getLog().info("  runConfig = " + runConfig);
		getLog().info("  source terminology = " + sourceTerminology);
		getLog().info("  source version = " + sourceVersion);
		getLog().info("  target terminology = " + targetTerminology);
		getLog().info("  target version = " + targetVersion);
		getLog().info("  userName = " + userName);

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

		pfsLimited.setStartIndex(0);
		pfsLimited.setMaxResults(30);
		pfsMinimal.setStartIndex(0);
		pfsMinimal.setMaxResults(5);
	}

	protected void setup(String folderName, String st, String sv, String tt, String tv, int maxCount) throws Exception {
		sourceTerminology = st;
		sourceVersion = sv;
		targetTerminology = tt;
		targetVersion = tv;

		setup(folderName, st, sv, tt, tv);
		pfsCustom = new PfsParameterJpa();
		pfsCustom.setMaxResults(maxCount);
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

		return properties;
	}

	protected PrintWriter prepareOutputFile(String filePrefix, String outputDescription)
			throws FileNotFoundException, UnsupportedEncodingException {
		File fd = new File(userFolder.getPath() + File.separator + filePrefix + "-" + month + timestamp + ".xls");
		getLog().info("Creating " + outputDescription + " file (" + filePrefix + ") at: " + fd.getAbsolutePath());

		final FileOutputStream fos = new FileOutputStream(fd);
		final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		PrintWriter pw = new PrintWriter(osw);

		return pw;

	}

	protected PrintWriter prepareRelOutputFile(String filePrefix, String outputDescription)
			throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter pw = prepareOutputFile(filePrefix, outputDescription);

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

	protected SctNeoplasmConcept populateSctConcept(SearchResult result) throws Exception {
		SctNeoplasmConcept con = new SctNeoplasmConcept(result.getTerminologyId(), result.getValue());
		populateRelationships(con);
		populateDescriptions(con);

		return con;
	}

	protected Set<SctRelationship> getDestRels(SctNeoplasmConcept con, String relType) {
		Set<SctRelationship> targets = new HashSet<>();

		for (SctRelationship rel : con.getRels()) {
			if (rel.getRelationshipType().equals(relType)) {
				targets.add(rel);
			}
		}

		return targets;
	}

	protected Map<String, SctNeoplasmConcept> processEclQuery(SearchResultList eclResults) throws Exception {
		Map<String, SctNeoplasmConcept> concepts = new HashMap<>();
		setupDescParser();

		for (SearchResult result : eclResults.getObjects()) {

			// Get Desc
			Concept clientConcept = client.getConcept(result.getId(), null, authToken);
			SctNeoplasmConcept con = new SctNeoplasmConcept(result.getTerminologyId(), result.getValue());

			for (Atom atom : clientConcept.getAtoms()) {
				if (isValidDescription(atom)) {
					SctNeoplasmDescription desc = descParser.parse(atom.getName());
					con.getDescs().add(desc);
				}
			}

			// Get Associated Rels
			populateRelationships(con);
			concepts.put(result.getTerminologyId(), con);
		}

		return concepts;
	}

	protected void populateRelationships(SctNeoplasmConcept con) throws Exception {
		RelationshipList relsList = client.findConceptRelationships(con.getConceptId(), sourceTerminology,
				sourceVersion, null, new PfsParameterJpa(), authToken);

		for (final Relationship<?, ?> relResult : relsList.getObjects()) {
			SctRelationship rel = relParser.parse(con.getName(), relResult);
			if (rel != null) {
				con.getRels().add(rel);
			}
		}

	}

	protected void populateDescriptions(SctNeoplasmConcept con) throws Exception {
		Concept fullCon = client.getConcept(con.getConceptId(), sourceTerminology, sourceVersion, null, authToken);

		for (final Atom atom : fullCon.getAtoms()) {
			if (isValidDescription(atom)) {
				SctNeoplasmDescription desc = new SctNeoplasmDescription();
				desc.setDescription(atom.getName());
				con.getDescs().add(desc);
			}
		}

	}

	protected SctNeoplasmConcept getSctConceptFromDesc(String desc) throws Exception {
		if (!conceptsFromDescs.containsKey(desc)) {
			final SearchResultList possibleMatches = client.findConcepts(sourceTerminology, sourceVersion,
					"\"" + desc + "\"", pfsMinimal, authToken);

			for (SearchResult result : possibleMatches.getObjects()) {
				if (!result.isObsolete()) {
					SctNeoplasmConcept retConcept = populateSctConcept(result);
					conceptsFromDescs.put(desc, retConcept);
				}
			}
		}

		return conceptsFromDescs.get(desc);
	}

	protected boolean isValidDescription(Atom atom) {
		return (!atom.isObsolete() && !atom.getTermType().equals("Fully specified name")
				&& !atom.getTermType().equals("Definition"));
	}

	protected void generateStringOutput(Map<String, Integer> lowestDepthMap, Map<String, String> matchMap,
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

		// System.out.println("\n\nBut actually outputing:");
		for (String s : lowestDepthStrings) {
			// System.out.println(s);
			str.append(s);
		}
	}

	protected Set<SctNeoplasmConcept> identifyPotentialFSConcepts(Set<String> findingSites) throws Exception {
		Set<SctNeoplasmConcept> retConcepts = new HashSet<>();

		for (String site : findingSites) {
			// Get the finding site as a concept
			SctNeoplasmConcept fsConcept = getSctConceptFromDesc(site);
			retConcepts.add(fsConcept);

			if (findingSitePotentialTermsMapCache.containsKey(fsConcept.getConceptId())) {
				return retConcepts;
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
					return null;
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
						String normalizedStr = atom.getName().toLowerCase();
						for (String s : nonFindingSiteStrings) {
							normalizedStr = normalizedStr.replaceAll("\\b" + s + "s" + "\\b", " ").trim();
							normalizedStr = normalizedStr.replaceAll("\\b" + s + "\\b", " ").trim();
						}

						normalizedStr = normalizedStr.replaceAll(" {2,}", " ").trim();

						if (!potentialFSConTerms.get(testCon).contains(normalizedStr)) {
							potentialFSConTerms.get(testCon).add(normalizedStr);
						}
					}
				}
			}
		}

		return retConcepts;
	}

	protected Set<String> identifyValidFindingSites(SctNeoplasmConcept sctCon) {
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

	protected Set<String> splitTokens(String str) {
		String[] splitString = FieldedStringTokenizer.split(str.toLowerCase(), " \t-({[)}]_!@#%&*\\:;\"',.?/~+=|<>$`^");
		Set<String> retStrings = new HashSet<>();

		for (int i = 0; i < splitString.length; i++) {
			if (!splitString[i].trim().isEmpty() && splitString[i].trim().length() != 1) {
				retStrings.add(splitString[i].trim());
			}
		}

		return retStrings;
	}

}
