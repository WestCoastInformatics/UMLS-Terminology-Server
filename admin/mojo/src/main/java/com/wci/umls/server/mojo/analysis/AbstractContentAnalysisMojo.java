package com.wci.umls.server.mojo.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.jpa.services.SecurityServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.mojo.analysis.matching.AbstractICD11MatchingRule;
import com.wci.umls.server.mojo.analysis.matching.ICD11MatcherConstants;
import com.wci.umls.server.mojo.model.ICD11MatcherRelationship;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.ICD11MatcherConceptSearcher;
import com.wci.umls.server.mojo.processes.SctNeoplasmDescriptionParser;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;
import com.wci.umls.server.services.SecurityService;

/**
 * The Class AbstractMatchingAnalysisMojo.
 *
 * Base class used to search the term-server database for concepts with a
 * matching string.
 * 
 * Uses matcher/pom.xml for invocation.
 *
 * @author Jesse Efron
 */
abstract public class AbstractContentAnalysisMojo extends AbstractMojo {

  /** The source terminology. */
  protected String sourceTerminology;

  /** The source version. */
  protected String sourceVersion;

  /** The target terminology. */
  protected String targetTerminology;

  /** The target version. */
  protected String targetVersion;

  /** The user folder. */
  protected File userFolder;

  /** The timestamp. */
  protected String timestamp;

  /** The month. */
  protected String month;

  /** The user name. */
  @Parameter
  protected String userName;

  /** The user password. */
  @Parameter
  protected String userPassword;

  /** The user name. */
  @Parameter
  protected boolean productionExecutionType;

  /** The partial df. */
  protected final DateTimeFormatter partialDf = DateTimeFormatter.ofPattern("_dd_HH-mm");

  /** The Constant relParser. */
  final static protected SctRelationshipParser relParser = new SctRelationshipParser();

  /** The desc parser. */
  static protected SctNeoplasmDescriptionParser descParser = null;

  /** The client. */
  protected ContentClientRest client;

  /** The auth token. */
  protected String authToken;

  /** The pfs ecl. */
  protected PfsParameterJpa pfsEcl = new PfsParameterJpa();

  /** The pfs limitless. */
  protected PfsParameterJpa pfsLimitless = new PfsParameterJpa();

  /** The pfs minimal. */
  protected PfsParameterJpa pfsMinimal = new PfsParameterJpa();

  /** The pfs custom. */
  protected PfsParameterJpa pfsCustom = null;

  /** The run config file path. */
  @Parameter
  protected String runConfig;

  protected ICD11MatcherConceptSearcher conceptSearcher = new ICD11MatcherConceptSearcher();

  /**
   * Base setup method.
   *
   * @param folderName the folder name
   * @param st the st
   * @param sv the sv
   * @param tt the tt
   * @param tv the tv
   * @throws Exception the exception
   */
  protected void setup(String folderName, String st, String sv, String tt, String tv)
    throws Exception {
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

    userFolder = new File("results" + File.separator + folderName);
    userFolder.mkdirs();

    Properties properties = setupProperties();

    client = new ContentClientRest(properties);
    final SecurityService service = new SecurityServiceJpa();
    authToken = service.authenticate(userName, userPassword).getAuthToken();
    service.close();

    pfsMinimal.setStartIndex(0);
    pfsMinimal.setMaxResults(5);
  }

  /**
   * Setup method when want custom pfs in addition to the pre-made ones.
   *
   * @param folderName the folder name
   * @param st the st
   * @param sv the sv
   * @param tt the tt
   * @param tv the tv
   * @param maxCount the max count
   * @throws Exception the exception
   */
  protected void setup(String folderName, String st, String sv, String tt, String tv, int maxCount)
    throws Exception {
    sourceTerminology = st;
    sourceVersion = sv;
    targetTerminology = tt;
    targetVersion = tv;

    setup(folderName, st, sv, tt, tv);
    pfsCustom = new PfsParameterJpa();
    pfsCustom.setMaxResults(maxCount);
  }

  /**
   * Setup properties.
   *
   * @return the properties
   * @throws Exception the exception
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

  /**
   * Create an output file.
   *
   * @param filePrefix the file prefix
   * @param outputDescription the output description
   * @return the prints the writer
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  protected PrintWriter prepareOutputFile(String filePrefix, String outputDescription)
    throws FileNotFoundException, UnsupportedEncodingException {
    File fd = new File(
        userFolder.getPath() + File.separator + filePrefix + "-" + month + timestamp + ".xls");
    getLog().info(
        "Creating " + outputDescription + " file (" + filePrefix + ") at: " + fd.getAbsolutePath());

    final FileOutputStream fos = new FileOutputStream(fd);
    final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    PrintWriter pw = new PrintWriter(osw);

    return pw;

  }

  /**
   * Create an output file.
   *
   * @param printWriterType the file prefix
   * @param outputDescription the output description
   * @return the prints the writer
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  protected PrintWriter prepareResultsFile(AbstractICD11MatchingRule rule, int printWriterType,
    String outputDescription) throws FileNotFoundException, UnsupportedEncodingException {
    File dir = new File(userFolder.getPath() + File.separator + rule.getRuleId());
    dir.mkdirs();

    String printWriterString;
    if (printWriterType == ICD11MatcherConstants.PRINT_WRITER_DEV_TYPE) {
      printWriterString = "allResults";
    } else {
      printWriterString = "singelResult";
    }

    File fd;
    if (!productionExecutionType) {
      fd = new File(dir.getAbsolutePath() + File.separator + rule.getRuleName() + "-"
          + printWriterString + "-" + month + timestamp + ".xls");
    } else {
      fd = new File(dir.getAbsolutePath() + File.separator + rule.getRuleName() + "-"
          + printWriterString + ".xls");
    }

    getLog().info("Creating " + outputDescription + " file (" + rule.getRuleName() + "-"
        + printWriterString + ") at: " + fd.getAbsolutePath());

    final FileOutputStream fos = new FileOutputStream(fd);
    final OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
    PrintWriter pw = new PrintWriter(osw);

    return pw;

  }

  /**
   * Print the relationship output file's header.
   *
   * @param filePrefix the file prefix
   * @param outputDescription the output description
   * @return the prints the writer
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
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

  /**
   * Print out the relationship to a file.
   *
   * @param rel the rel
   * @param conId the con id
   * @param outputFile the output file
   * @throws Exception the exception
   */
  protected void exportRels(ICD11MatcherRelationship rel, String conId, PrintWriter outputFile)
    throws Exception {
    if (rel != null) {
      outputFile.print(conId);
      outputFile.print("\t");
      outputFile.print(rel.printForExcel());

      outputFile.println();
    }
  }

  /**
   * Execute an ecl query and populate the neoplasm concept with the results.
   *
   * @param eclResults the ecl results
   * @return the map
   * @throws Exception the exception
   */
  protected Map<String, ICD11MatcherSctConcept> processEclQuery(SearchResultList eclResults)
    throws Exception {
    Map<String, ICD11MatcherSctConcept> concepts = new HashMap<>();
    setupDescParser();

    for (SearchResult result : eclResults.getObjects()) {

      // Get Desc
      Concept clientConcept = client.getConcept(result.getId(), null, authToken);
      ICD11MatcherSctConcept con =
          new ICD11MatcherSctConcept(result.getTerminologyId(), result.getValue());

      for (Atom atom : clientConcept.getAtoms()) {
        if (conceptSearcher.isValidDescription(atom)) {
          SctNeoplasmDescription desc = descParser.parse(atom.getName(), true);
          con.getDescs().add(desc);
        }
      }

      // Get Associated Rels
      conceptSearcher.populateRelationships(con, con.getDescs().iterator().next().getDescription());
      concepts.put(result.getTerminologyId(), con);
    }

    return concepts;
  }

  /**
   * Execute an ecl query and populate the neoplasm concept with the results.
   *
   * @param eclResults the ecl results
   * @return the map
   * @throws Exception the exception
   */
  protected Map<String, ICD11MatcherSctConcept> processEclQueryFromFiles(
    AbstractICD11MatchingRule rule) throws Exception {
    Map<String, ICD11MatcherSctConcept> concepts = new HashMap<>();

    final SearchResultList eclResults =
        client.findConcepts(sourceTerminology, sourceVersion, null, pfsEcl, authToken);
    getLog().info("With SCT ECL, have: " + eclResults.getObjects().size());

    for (SearchResult result : eclResults.getObjects()) {
      // Get Desc
      ICD11MatcherSctConcept con =
          new ICD11MatcherSctConcept(result.getTerminologyId(), result.getValue());

      con.setDescs(descParser.getDescriptions(con));
      con.setRels(relParser.getRelationships(con));

      concepts.put(result.getTerminologyId(), con);
    }

    return concepts;
  }

  protected Map<String, ICD11MatcherSctConcept> populateTestConcept(List<String> conIdList) {
    Map<String, ICD11MatcherSctConcept> concepts = new HashMap<>();

    for (String conId : conIdList) {
      ICD11MatcherSctConcept con = new ICD11MatcherSctConcept(conId, null);

      con.setDescs(descParser.getDescriptions(con));
      con.setRels(relParser.getRelationships(con));

      if (con.getDescs() != null) {
        con.setName(con.getDescs().iterator().next().getDescription());
      }

      concepts.put(conId, con);
    }

    return concepts;
  }

  /*
   * Instantiate the neoplasm description parser
   */
  protected void setupDescParser() {
    if (descParser == null) {
      descParser = new SctNeoplasmDescriptionParser();
    }
  }
}
