/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.google.common.io.Files;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.algo.FileSorter;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.services.handlers.NormalizedStringHandler;

/**
 * Algorithm to write the RRF index files.
 */
public class WriteRrfIndexFilesAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;
  
  /** The writer map. */
  private Map<String, PrintWriter> writerMap = new HashMap<>();
  
  private File dir = null;
  

  /**
   * Instantiates an empty {@link WriteRrfIndexFilesAlgorithm}.
   *
   * @throws Exception the exception
   */
  public WriteRrfIndexFilesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("WRITERRFINDEXES");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting Write RRF Indexes");
    
    openWriters();

    previousProgress = 0;
    stepsCompleted = 0;
    HashSet<String> seen = new HashSet<>();

    // initialize progress monitoring
    javax.persistence.Query query =
        manager.createQuery("select count(*) from ConceptJpa c "
            + "where c.publishable = true and terminology = :terminology");
    query.setParameter("terminology", getProject().getTerminology());
    steps = Integer.parseInt(query.getSingleResult().toString());

    NormalizedStringHandler handler =
        getNormalizedStringHandler();

    // process one concept at a time
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select a from ConceptJpa a WHERE a.publishable = true and terminology = :terminology order by a.terminologyId");

    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(1000);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Concept c = (Concept) results.get()[0];

      for (final Atom atom : c.getAtoms()) {
        if (atom.isPublishable()) {

          // MRXNS_ENG.RRF

          // 0 LAT Abbreviation of language of the string (always ENG in this
          // edition of the Metathesaurus)
          // 1 NSTR Normalized string in lowercase
          // 2 CUI Concept identifier
          // 3 LUI Term identifier
          // 4 SUI String identifier
          final String normalizedString =
              handler.getNormalizedString(atom.getName());

          if (atom.getLanguage().equals("ENG")
              && !seen.contains("MRXNS" + atom.getStringClassId())
              && !ConfigUtility.isEmpty(normalizedString)) {

            StringBuilder sb = new StringBuilder();
            sb.append("ENG").append("|"); // 0 LAT
            sb.append(normalizedString).append("|"); // 1 NSTR
            sb.append(c.getTerminologyId()).append("|"); // 2 CUI
            sb.append(atom.getLexicalClassId()).append("|"); // 3 LUI
            sb.append(atom.getStringClassId()).append("|"); // 4 SUI
            sb.append("\n");
            writerMap.get("MRXNS_ENG.RRF").write(sb.toString());
            seen.add("MRXNS" + atom.getStringClassId());

            // MRXNW_ENG.RRF
            for (final String word : FieldedStringTokenizer
                .split(normalizedString, ConfigUtility.PUNCTUATION)) {
              sb = new StringBuilder();
              sb.append("ENG").append("|"); // 0 LAT
              sb.append(word).append("|"); // 1 WORD
              sb.append(c.getTerminologyId()).append("|"); // 2 CUI
              sb.append(atom.getLexicalClassId()).append("|"); // 3 LUI
              sb.append(atom.getStringClassId()).append("|"); // 4 SUI
              sb.append("\n");
              writerMap.get("MRXNW_ENG.RRF").write(sb.toString());
            }
          }

          if (!seen.contains("MRXW" + atom.getStringClassId())) {
            // for all languages write MRXW_<language>.RRF
            for (final String word : FieldedStringTokenizer
                .split(atom.getName(), ConfigUtility.PUNCTUATION)) {
              StringBuilder sb = new StringBuilder();
              sb.append(atom.getLanguage()).append("|"); // 0 LAT
              sb.append(word).append("|"); // 1 WORD
              sb.append(c.getTerminologyId()).append("|"); // 2 CUI
              sb.append(atom.getLexicalClassId()).append("|"); // 3 LUI
              sb.append(atom.getStringClassId()).append("|"); // 4 SUI
              sb.append("\n");
              writerMap.get("MRXW_" + atom.getLanguage() + ".RRF").write(sb.toString());
            }
            seen.add("MRXW" + atom.getStringClassId());
          }
        }
      }
      updateProgress();
    }
    closeWriters();
    logInfo("Finishing Write RRF Indexes");
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  private void openWriters() throws Exception {
    dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");

    writerMap.put("MRXNS_ENG.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRXNS_ENG.RRF"))));

    writerMap.put("MRXNW_ENG.RRF",
        new PrintWriter(new FileWriter(new File(dir, "MRXNW_ENG.RRF"))));
    for (Language lat : getLanguages(getProject().getTerminology(), getProject().getVersion()).getObjects()) {
      writerMap.put("MRXW_" + lat.getAbbreviation() + ".RRF",
          new PrintWriter(new FileWriter(new File(dir, "MRXW_" + lat.getAbbreviation() + ".RRF"))));
    }
  }
  
  private void closeWriters() throws Exception {
    // close writers
    for (PrintWriter writer : writerMap.values()) {
      writer.close();
    }
    
    // sort files
    for (String writerName : writerMap.keySet()) {
      File inputFile = new File(dir, writerName);
      File outputFile = new File(dir, writerName + ".sorted");
      FileSorter.sortFile(inputFile.getAbsolutePath(), outputFile.getAbsolutePath(), (a1, a2) -> a2.compareTo(a1));
    }
    
    // move sorted files into orig files
    // TODO sorted files have wrong line termination
    // TODO review comparator choice
    for (String writerName : writerMap.keySet()) {

      File inputFile = new File(dir, writerName);
      File outputFile = new File(dir, writerName + ".sorted");
      inputFile.delete();
      Files.move(outputFile.getAbsoluteFile(), inputFile.getAbsoluteFile() );
    }

  }
  
  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      fireProgressEvent(currentProgress,
          "WRITE RRF INDEXES progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }
}
