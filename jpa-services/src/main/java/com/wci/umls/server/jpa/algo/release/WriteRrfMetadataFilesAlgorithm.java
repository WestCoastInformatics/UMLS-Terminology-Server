/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.AttributeName;
import com.wci.umls.server.model.meta.CodeVariantType;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;
import com.wci.umls.server.model.meta.Language;
import com.wci.umls.server.model.meta.NameVariantType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.model.meta.TermType;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Algorithm to write the RRF metadata files.
 */
public class WriteRrfMetadataFilesAlgorithm
    extends AbstractInsertMaintReleaseAlgorithm {

  /**
   * Instantiates an empty {@link WriteRrfMetadataFilesAlgorithm}.
   *
   * @throws Exception the exception
   */
  public WriteRrfMetadataFilesAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("RRFMETADATA");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    fireProgressEvent(0, "Starting");
    setSteps(4);

    writeMrdoc();
    updateProgress();

    writeMrsab();
    updateProgress();

    writeMrrank();
    updateProgress();

    writeMrcolsMrfiles();
    updateProgress();

    fireProgressEvent(100, "Finished");
    logInfo("Finished " + getName());
  }

  /**
   * Write mrrank.
   *
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  private void writeMrrank() throws Exception {
    logInfo("  Write MRRANK data");

    // Make sure termgroups written out exist only for atoms.
    final String queryStr = "select distinct a.terminology, a.termType "
        + "from ConceptJpa c join c.atoms a where c.terminology = :projectTerminology "
        + " and a.publishable = true";
    final javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("projectTerminology", getProject().getTerminology());
    final List<Object[]> results = query.getResultList();
    final Set<String> sabTty = new HashSet<>();
    for (final Object[] result : results) {
      sabTty.add("" + result[0] + result[1]);
    }
    logInfo("  valid terminology/tty = " + sabTty.size());
    final File dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");
    final File outputFile = new File(dir, "MRRANK.RRF");
    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    try {
      final PrecedenceList precList = getPrecedenceList(
          getProject().getTerminology(), getProject().getVersion());
      int index = precList.getPrecedence().getKeyValuePairs().size();
      for (final KeyValuePair pair : precList.getPrecedence()
          .getKeyValuePairs()) {
        // Skip entries that are not represnted in atoms
        if (!sabTty.contains(pair.getKey() + pair.getValue())) {
          continue;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("%04d", index--)).append("|");
        sb.append(pair.getKey()).append("|");
        sb.append(pair.getValue()).append("|");
        final TermType tty = this.getTermType(pair.getValue(),
            getProject().getTerminology(), getProject().getVersion());
        sb.append(tty.isSuppressible() ? "Y" : "N").append("|");
        out.print(sb.toString() + "\n");
      }
    } catch (

    Exception e) {
      throw e;
    } finally {
      out.close();
    }
  }

  /**
   * Write mrcols mrfiles.
   *
   * @throws Exception the exception
   */
  private void writeMrcolsMrfiles() throws Exception {
    logInfo("  Write MRCOLS/MRFILES data");
    // This just copies template MRCOLS/MRFILES files because
    // MetamorphoSys will ultimately compute the correct
    // files anyway.
    Path source = Paths.get(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/META/MRFILES.RRF");
    Path destination = Paths.get(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion()
        + "/META/MRFILES.RRF");
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

    source = Paths.get(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/META/MRCOLS.RRF");
    destination = Paths.get(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion()
        + "/META/MRCOLS.RRF");
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
  }

  /**
   * Write mrsab.
   *
   * @throws Exception the exception
   */
  private void writeMrsab() throws Exception {
    logInfo("  Write MRSAB data");
    final File dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");
    final File outputFile = new File(dir, "MRSAB.RRF");
    final List<String> outputLines = new ArrayList<>();

    // progress monitoring
    for (final Terminology term : getCurrentTerminologies().getObjects()) {

      // Field Description
      // 0 VCUI
      // 1 RCUI
      // 2 VSAB
      // 3 RSAB
      // 4 SON
      // 5 SF
      // 6 SVER
      // 7 VSTART
      // 8 VEND
      // 9 IMETA
      // 10 RMETA
      // 11 SLC
      // 12 SCC
      // 13 SRL
      // 14 TFR
      // 15 CFR
      // 16 CXTY
      // 17 TTYL
      // 18 ATNL
      // 19 LAT
      // 20 CENC
      // 21 CURVER
      // 22 SABIN
      // 23 SSN
      // 24 SCIT
      //
      // e.g.
      // C3847853|C1140284|RXNORM_14AA_140902F|RXNORM|RxNorm Vocabulary,
      // 14AA_140902F|RXNORM|14AA_140902F|||2014AB||John Kilbourne, M.D. ;Head,
      // MeSH Section;National Library of Medicine;6701 Democracy Blvd.;Suite
      // 202 MSC 4879;Bethesda;Maryland;United
      // States;20892-4879;kilbourj@mail.nlm.nih.gov|John Kilbourne, M.D.;Head,
      // MeSH Section;National Library of Medicine;6701 Democracy Blvd.;Suite
      // 202 MSC 4879;Bethesda;Maryland;United
      // States;20892-4879;kilbourj@mail.nlm.nih.gov|0|1969|278||BN,BPCK,DF,GPCK,IN,MIN,OCD,PIN,PSN,SBD,SBDC,SBDF,SCD,SCDC,SCDF,SCDG,SY,TMSY|AMBIGUITY_FLAG,NDC,ORIG_AMBIGUITY_FLAG,ORIG_CODE,ORIG_SOURCE,ORIG_TTY,ORIG_VSAB,RXAUI,RXCUI,RXN_ACTIVATED,RXN_AVAILABLE_STRENGTH,RXN_BN_CARDINALITY,RXN_HUMAN_DRUG,RXN_OBSOLETED,RXN_QUANTITY,RXN_STRENGTH,RXTERM_FORM|ENG|UTF-8|Y|Y|RXNORM|RxNorm;META2014AA
      // Full Update 2014_09_02;Bethesda, MD;National Library of Medicine|

      // Get VCUI/RCUI first
      StringBuilder sb = new StringBuilder();
      String vcui = "";
      String rcui = "";
      SearchResultList results = findConceptSearchResults(
          getProject().getTerminology(), getProject().getVersion(),
          getProject().getBranch(), " atoms.codeId:V-" + term.getTerminology()
              + " AND atoms.terminology:SRC AND atoms.termType:RPT",
          null);
      Concept rootTerminologyConcept = null;
      if (results.size() > 0) {
        rootTerminologyConcept =
            getConcept(results.getObjects().get(0).getId());
        rcui = rootTerminologyConcept.getTerminologyId();

        // Look up versioned concept
        results = findConceptSearchResults(getProject().getTerminology(),
            getProject().getVersion(), getProject().getBranch(),
            " atoms.codeId:V-" + term.getTerminology() + "_" + term.getVersion()
                + " AND atoms.terminology:SRC AND atoms.termType:VPT",
            null);
        if (results.size() > 0) {
          vcui = getConcept(results.getObjects().get(0).getId())
              .getTerminologyId();
        }
        // not everything has a VCUI (e.g. "SRC" and "MTH").
      } else {
        // everything should have an RCUI
        logWarn("Unexpected missing RCUI concept " + term.getTerminology());
        continue;
      }

      // 0 VCUI
      sb.append(vcui).append("|");
      // 1 RCUI
      sb.append(rcui).append("|");
      // 2 VSAB
      sb.append(term.getTerminology() + "_" + term.getVersion()).append("|");
      // 3 RSAB
      sb.append(term.getRootTerminology().getTerminology()).append("|");
      // 4 SON
      sb.append(term.getPreferredName()).append("|");
      // 5 SF
      sb.append(term.getRootTerminology().getFamily()).append("|");
      // 6 VER
      sb.append(term.getVersion()).append("|");
      // 7 SDATE
      sb.append(term.getStartDate() != null ? term.getStartDate() : "")
          .append("|"); // 7
      // 8 EDATE
      sb.append(term.getEndDate() != null ? term.getEndDate() : "").append("|");

      final String imeta =
          term.getFirstReleases().get(getProject().getTerminology());
      final String rmeta =
          term.getLastReleases().get(getProject().getTerminology());
      // 9 IMETA
      sb.append(imeta == null ? "" : imeta).append("|");
      // 10 RMETA
      sb.append(rmeta == null ? "" : rmeta).append("|");

      // 11 SLC
      sb.append(term.getRootTerminology().getLicenseContact()).append("|");
      // 12 SCC
      sb.append(term.getRootTerminology().getContentContact()).append("|");
      // 13 SRL
      sb.append(term.getRootTerminology().getRestrictionLevel()).append("|");
      // 14 TFR
      sb.append(getTfr(term.getRootTerminology().getTerminology())).append("|");
      // 15 CFR
      sb.append(getCfr(term.getRootTerminology().getTerminology())).append("|");
      // 16 CXTY FULL, NOSIB, IGNORE-RELA, MULTIPLE
      if (rootTerminologyConcept.getAtoms().stream()
          .filter(a -> a.isPublishable() && a.getTermType().equals("RHT"))
          .collect(Collectors.toList()).size() > 0) {
        sb.append("FULL");
        if (!term.isIncludeSiblings()) {
          sb.append("-NOSIB");
        }
        if (term.getRootTerminology().isPolyhierarchy()) {
          sb.append("-MULTIPLE");
        }
        if (!term.getRootTerminology().isHierarchyComputable()) {
          sb.append("-IGNORE-RELA");
        }
      }
      sb.append("|");

      // 17 TTYL
      sb.append(getTtyl(term.getRootTerminology().getTerminology()))
          .append("|");
      // 18 ATNL
      sb.append(getAtnl(term.getRootTerminology().getTerminology()))
          .append("|");
      // 19 LAT
      sb.append(term.getRootTerminology().getLanguage() != null
          ? term.getRootTerminology().getLanguage() : "").append("|");
      // 20 CENC
      sb.append("UTF-8").append("|");
      // 21 CURVER
      sb.append(term.isCurrent() ? "Y" : "N").append("|");
      // 22 SABIN
      sb.append("Y").append("|");

      // 23 SSN
      for (final Atom atom : rootTerminologyConcept.getAtoms()) {
        if (atom.getTermType().equals("SSN") && atom.isPublishable()) {
          sb.append(atom.getName());
          break;
        }
      }
      sb.append("|");
      // 24 SCIT
      sb.append(term.getCitation()).append("|");

      outputLines.add(sb.toString());

    }

    // sort and write to file
    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    try {
      Collections.sort(outputLines);
      for (final String line : outputLines) {
        out.print(line + "\n");
      }
    } catch (Exception e) {
      throw e;
    } finally {
      out.close();
    }
  }

  /**
   * Returns the tfr.
   *
   * @param terminology the terminology
   * @return the tfr
   */
  private String getTfr(String terminology) {
    String queryStr = "select count(*) " + "from AtomJpa a " + "where "
        + "a.terminology = :terminology and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    return query.getSingleResult().toString();
  }

  /**
   * Returns the cfr.
   *
   * @param terminology the terminology
   * @return the cfr
   */
  private String getCfr(String terminology) {
    String queryStr = "select count(distinct c.terminologyId) "
        + "from ConceptJpa c join c.atoms a where a.terminology = :terminology and c.terminology = :projectTerminology"
        + " and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    query.setParameter("projectTerminology", getProject().getTerminology());
    return query.getSingleResult().toString();
  }

  /**
   * Returns the ttyl.
   *
   * @param terminology the terminology
   * @return the ttyl
   */
  private String getTtyl(String terminology) {
    String queryStr = "select distinct termType "
        + "from AtomJpa a where a.terminology = :terminology"
        + " and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    @SuppressWarnings("unchecked")
    List<String> list = query.getResultList();
    Collections.sort(list);
    StringBuilder sb = new StringBuilder();
    for (final String tty : list) {
      sb.append(tty).append(",");
    }
    if (sb.toString().endsWith(",")) {
      return sb.toString().substring(0, sb.toString().length() - 1);
    }
    return "";
  }

  /**
   * Returns the atnl.
   *
   * @param terminology the terminology
   * @return the atnl
   */
  private String getAtnl(String terminology) {
    String queryStr = "select distinct name "
        + "from AttributeJpa a where a.terminology = :terminology"
        + " and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    @SuppressWarnings("unchecked")
    List<String> list = query.getResultList();
    Collections.sort(list);
    StringBuilder sb = new StringBuilder();
    for (final String atn : list) {
      sb.append(atn).append(",");
    }
    if (sb.toString().endsWith(",")) {
      return sb.toString().substring(0, sb.toString().length() - 1);
    }
    return "";
  }

  /**
   * Write MRDOC.
   *
   * @throws Exception the exception
   */
  private void writeMrdoc() throws Exception {
    logInfo("  Write MRDOC ");

    // Set up the file
    final File dir = new File(config.getProperty("source.data.dir") + "/"
        + getProcess().getInputPath() + "/" + getProcess().getVersion() + "/"
        + "META");
    final File outputFile = new File(dir, "MRDOC.RRF");
    final List<String> outputLines = new ArrayList<>();

    // Field Description DOCKEY,VALUE,TYPE,EXPL
    // 0 DOCKEY
    // 1 VALUE
    // 2 TYPE
    // 3 EXPL

    // Handle AttributeNames
    // e.g. ATN|ACCEPTABILITYID|expanded_form|Acceptability Id|
    for (final AttributeName atn : getAttributeNames(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      StringBuilder sb = new StringBuilder();
      sb.append("ATN").append("|");
      sb.append(atn.getAbbreviation()).append("|");
      sb.append("expanded_form").append("|");
      sb.append(atn.getExpandedForm()).append("|");
      outputLines.add(sb.toString());
    }

    // Handle Languages
    for (final Language lat : getLanguages(getProject().getTerminology(),
        getProject().getVersion()).getObjects()) {
      StringBuilder sb = new StringBuilder();
      sb.append("LAT").append("|");
      sb.append(lat.getAbbreviation()).append("|");
      sb.append("expanded_form").append("|");
      sb.append(lat.getExpandedForm()).append("|");
      outputLines.add(sb.toString());
    }

    // Handle AdditionalRelationshipLabel
    for (final AdditionalRelationshipType rela : getAdditionalRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      StringBuilder sb = new StringBuilder();
      sb.append("RELA").append("|");
      sb.append(rela.getAbbreviation()).append("|");
      sb.append("expanded_form").append("|");
      sb.append(rela.getExpandedForm()).append("|");
      outputLines.add(sb.toString());
      sb = new StringBuilder();
      sb.append("RELA").append("|");
      sb.append(rela.getAbbreviation()).append("|");
      sb.append("rela_inverse").append("|");
      sb.append(rela.getInverse().getAbbreviation()).append("|");
      outputLines.add(sb.toString());
    }

    // Handle RelationshipLabel
    for (final RelationshipType rela : getRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      StringBuilder sb = new StringBuilder();
      sb.append("REL").append("|");
      sb.append(rela.getAbbreviation()).append("|");
      sb.append("expanded_form").append("|");
      sb.append(rela.getExpandedForm()).append("|");
      outputLines.add(sb.toString());
      sb = new StringBuilder();
      sb.append("REL").append("|");
      sb.append(rela.getAbbreviation()).append("|");
      sb.append("rel_inverse").append("|");
      sb.append(rela.getInverse().getAbbreviation()).append("|");
      outputLines.add(sb.toString());
    }

    // Handle term types
    for (final TermType tty : getTermTypes(getProject().getTerminology(),
        getProject().getVersion()).getObjects()) {
      StringBuilder sb = new StringBuilder();
      sb.append("TTY").append("|");
      sb.append(tty.getAbbreviation()).append("|");
      sb.append("expanded_form").append("|");
      sb.append(tty.getExpandedForm()).append("|");
      outputLines.add(sb.toString());

      if (tty.getCodeVariantType() == CodeVariantType.PET) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("entry_term").append("|");
        outputLines.add(sb.toString());
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("preferred").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getCodeVariantType() == CodeVariantType.PN) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("preferred").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getCodeVariantType() == CodeVariantType.ET) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("entry_term").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getCodeVariantType() == CodeVariantType.ATTRIBUTE) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("attribute").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getCodeVariantType() == CodeVariantType.SY) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("synonym").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getNameVariantType() == NameVariantType.AB) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("abbreviation").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.isHierarchicalType()) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("hierarchical").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.isObsolete()) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("obsolete").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getNameVariantType() == NameVariantType.EXPANDED) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("expanded").append("|");
        outputLines.add(sb.toString());
      }
      if (tty.getCodeVariantType() == CodeVariantType.UNDEFINED) {
        sb = new StringBuilder();
        sb.append("TTY").append("|");
        sb.append(tty.getAbbreviation()).append("|");
        sb.append("tty_class").append("|");
        sb.append("other").append("|");
        outputLines.add(sb.toString());
      }
    }

    // General metadata entries (skip MAPATN)
    for (final GeneralMetadataEntry entry : getGeneralMetadataEntries(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      StringBuilder sb = new StringBuilder();
      sb.append(entry.getKey()).append("|");
      sb.append(entry.getAbbreviation()).append("|");
      sb.append(entry.getType()).append("|");
      sb.append(entry.getExpandedForm()).append("|");
      outputLines.add(sb.toString());
    }

    // A few need to be hardcoded
    outputLines.add("MAPATN|ACTIVE|expanded_form|Active|");
    outputLines.add("MAPATN||expanded_form|Empty attribute name|");
    outputLines.add("REL|DEL|expanded_form|Deleted concept|");
    outputLines.add("REL||expanded_form|Empty relationship|");

    // sort and write to file
    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    Collections.sort(outputLines);
    for (final String line : outputLines) {
      out.print(line + "\n");
    }
    out.close();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a
    logInfo("Finished RESET " + getName());

  }

  /* see superclass */
  @Override
  public void checkProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
