package com.wci.umls.server.mojo.analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import com.wci.umls.server.helpers.SearchResult;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.mojo.model.ICD11MatcherRelationship;
import com.wci.umls.server.mojo.model.ICD11MatcherSctConcept;
import com.wci.umls.server.mojo.model.SctNeoplasmDescription;
import com.wci.umls.server.mojo.processes.SctRelationshipParser;
import com.wci.umls.server.rest.client.ContentClientRest;

public class SctConceptsContentAnalyzer {
  SctRelationshipParser relParser = new SctRelationshipParser();

  private int counter = 0;

  private boolean testing = false;

  /** The partial df. */
  protected final DateTimeFormatter partialDf = DateTimeFormatter.ofPattern("_dd_HH-mm");

  private PrintWriter outputDescFile;

  private PrintWriter outputRelFile;

  private String matcherName;

  private String matcherType;


  public SctConceptsContentAnalyzer(String matcher, String rule) {
    this.matcherName = matcher;
    this.matcherType = rule;
  }

  public void analyze(Collection<ICD11MatcherSctConcept> sctConcepts, String targetTerminology,
    String targetVersion, ContentClientRest client, String authToken) throws FileNotFoundException, UnsupportedEncodingException {
    
    prepareAnalysis();
    
    for (ICD11MatcherSctConcept con : sctConcepts) {
      for (SctNeoplasmDescription desc : con.getDescs()) {
        writeDesc(con.getConceptId(),desc.getDescription());
      }
      
      for (ICD11MatcherRelationship rel : con.getRels()) {
        writeRel(con.getConceptId(), rel);
      }


      if (!clearCache(outputDescFile, outputRelFile)) {
        break;
      }
    }
    
    outputDescFile.close();
    outputRelFile.close();
  }

  public void analyze(SearchResultList results, String terminology, String version,
    ContentClientRest client, String authToken) throws Exception {

    prepareAnalysis();
    
    for (SearchResult result : results.getObjects()) {
      Concept con = client.getConcept(result.getId(), null, authToken);

      for (Atom atom : con.getAtoms()) {
        // Only process active & non-FSN & non-Definition descs
        if (!atom.isObsolete() && !atom.getTermType().equals("Fully specified name")
            && !atom.getTermType().equals("Definition")) {
          String desc = atom.getName();
          writeDesc(con.getTerminologyId(),desc);
        }
      }

      RelationshipList relsList = client.findConceptRelationships(con.getTerminologyId(),
          terminology, version, null, new PfsParameterJpa(), authToken);

      for (final Relationship<?, ?> relResult : relsList.getObjects()) {
        ICD11MatcherRelationship rel = relParser.parse(con.getName(), relResult);
        writeRel(con.getTerminologyId(), rel);
      }

      if (!clearCache(outputDescFile, outputRelFile)) {
        break;
      }
    }
  }

  private void writeRel(String conId, ICD11MatcherRelationship rel) {
    if (rel != null) {
      outputRelFile.print(conId);
      outputRelFile.print("\t");
      outputRelFile.print(rel.printForExcel());

      outputRelFile.println();
    }
  }

  private void writeDesc(String conId, String desc) {
    outputDescFile.write(conId);
    outputDescFile.write("\t");
    outputDescFile.println(desc.trim());
  }

  private void prepareAnalysis() throws FileNotFoundException, UnsupportedEncodingException {
    outputDescFile = prepareDescOutputFile();
    outputRelFile = prepareRelOutputFile();
    outputDescFile.flush();
    outputRelFile.flush();
  }

  private boolean clearCache(PrintWriter outputDescFile, PrintWriter outputRelFile) {
    if (counter++ % 50 == 0) {
      outputDescFile.flush();
      outputRelFile.flush();
    } else if (counter % 500 == 0) {
      System.out.println("Have processed " + counter + " concepts");
    }

    if (testing && counter > 4) { // result.getTerminologyId().equals("109830000"))
                                  // {
      return false;
    }

    return true;
  }

  private PrintWriter prepareDescOutputFile()
    throws FileNotFoundException, UnsupportedEncodingException {

    File userFolder = new File("results" + File.separator + matcherName + File.separator + matcherType);
    userFolder.mkdirs();

    // Setup Description File
    File fd = new File(userFolder.getPath() + File.separator + "analyzer-Descriptions.txt");
    String outputDescFilePath = fd.getAbsolutePath();
    System.out.println("Creating file at: " + outputDescFilePath);

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
   * Prepare relationship output file.
   *
   * @return the prints the writer
   * @throws FileNotFoundException the file not found exception
   * @throws UnsupportedEncodingException the unsupported encoding exception
   */
  private PrintWriter prepareRelOutputFile()
    throws FileNotFoundException, UnsupportedEncodingException {

    File userFolder = new File("results" + File.separator + matcherName + File.separator + matcherType);
    userFolder.mkdirs();

    // Setup Relationship File
    File fd = new File(userFolder.getPath() + File.separator + "analyzer-Relationships.txt");

    String outputRelFilePath = fd.getAbsolutePath();
    System.out.println("Creating file at: " + outputRelFilePath);

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
}
