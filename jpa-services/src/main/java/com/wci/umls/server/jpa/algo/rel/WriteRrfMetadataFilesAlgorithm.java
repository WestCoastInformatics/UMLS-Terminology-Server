/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.KeyValuePair;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;
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
public class WriteRrfMetadataFilesAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;
    
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
    logInfo("Starting Write RRF metadata files");
    
    writeMrdoc();
    writeMrsab();
    writeMrrank();
    writeMrcolsMrfiles();

    logInfo("Finished Write RRF metadata files");

  }

  private void writeMrrank() throws Exception {
    logInfo("  Write MRRANK data");
    final File dir = new File(config.getProperty("source.data.dir") + "/" +  
      getProcess().getInputPath() + "/" + getProcess().getVersion() + "/" + "META");
    final File outputFile = new File(dir, "MRRANK.RRF");

    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    PrecedenceList precList = getPrecedenceList(getProject().getTerminology(), getProject().getVersion());
    int index = precList.getPrecedence().getKeyValuePairs().size();    
    for (KeyValuePair pair : precList.getPrecedence().getKeyValuePairs()) {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%04d", index--)).append("|");
      sb.append(pair.getKey()).append("|");
      sb.append(pair.getValue()).append("|");
      TermType tty = this.getTermType(pair.getValue(), getProject().getTerminology(), getProject().getVersion());
      String suppress = "";
      if (tty.isSuppressible()) {
        suppress = "Y";
      } else {
        suppress = "N";
      }
      sb.append(suppress).append("|");
      out.print(sb.toString() + "\n");
    }
    
    out.close();
  }
  
  private void writeMrcolsMrfiles() throws Exception {
    logInfo("  Write MRCOLS/MRFILES data");
    Path source = Paths.get(config.getProperty("source.data.dir") + "/" +  
      getProcess().getInputPath() + "/META/MRFILES.RRF");
    Path destination = Paths.get(config.getProperty("source.data.dir") + "/" +  
        getProcess().getInputPath() + "/" + getProcess().getVersion() + "/META/MRFILES.RRF");
 
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
    
    source = Paths.get(config.getProperty("source.data.dir") + "/" +  
        getProcess().getInputPath() + "/META/MRCOLS.RRF");
    destination = Paths.get(config.getProperty("source.data.dir") + "/" +  
          getProcess().getInputPath() + "/" + getProcess().getVersion() + "/META/MRCOLS.RRF");
   
    Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
  }
  
  private void writeMrsab() throws Exception {
    logInfo("  Write MRSAB data");
    final File dir = new File(config.getProperty("source.data.dir") + "/" +  
      getProcess().getInputPath() + "/" + getProcess().getVersion() + "/" + "META");
    final File outputFile = new File(dir, "MRSAB.RRF");

    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    
    // progress monitoring
    steps = getCurrentTerminologies().getObjects().size();
    
    for (Terminology term : getCurrentTerminologies().getObjects()) {
    
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
      PfsParameter pfs = new PfsParameterJpa();
      SearchResultList results = findConcepts(getProject().getTerminology(), getProject().getVersion(),
          getProject().getBranch(), " atoms.codeId:V-" + term.getTerminology() + " AND atoms.terminology:SRC AND atoms.termType:RPT", pfs);
      Concept rootTerminologyConcept = null;
      if (results.getTotalCount() > 0) {
        rootTerminologyConcept = getConcept(results.getObjects().get(0).getId());
        rcui = rootTerminologyConcept.getTerminologyId();
        // TODO VCUIs still not showing up
        for (Relationship rel : this.findConceptDeepRelationships(rootTerminologyConcept.getTerminologyId(), 
            getProject().getTerminology(), getProject().getVersion(), Branch.ROOT, "", false, false, false, false, pfs).getObjects()) {
          for (Atom atom : ((Concept)rel.getTo()).getAtoms()) {
            if (atom.getTerminology().equals("SRC") && atom.getTermType().equals("VPT")) {
              vcui =  rel.getTo().getTerminologyId();
            }
          }
        }
      } /*// TODO fails on CCS_10 else {
        throw new Exception("Unable to find root terminology - " + term.getTerminology());
      }*/
      
      
      sb.append(vcui).append("|"); // 0
      sb.append(rcui).append("|"); // 1
      sb.append(term.getTerminology()).append("|");  //2
      sb.append(term.getRootTerminology().getTerminology()).append("|");  //3
      sb.append(term.getPreferredName()).append("|");   //4
      sb.append(term.getRootTerminology().getFamily()).append("|");  // 5  
      sb.append(term.getVersion()).append("|");    //6
      sb.append(term.getStartDate() != null ? term.getStartDate() : "").append("|");    // 7
      sb.append(term.getEndDate() != null ? term.getEndDate() : "").append("|");    // 8
      // 9 IMETA  // TODO Version of the Metathesaurus that a source was added
      // 10 RMETA  // Version of the Metathesaurus where a version is removed
      sb.append("||"); // for IMETA/RMETA
      sb.append(term.getRootTerminology().getLicenseContact()).append("|"); // 11
      sb.append(term.getRootTerminology().getContentContact()).append("|"); // 12
      sb.append(term.getRootTerminology().getRestrictionLevel()).append("|"); // 13
      sb.append(getTfr(term.getRootTerminology().getTerminology())).append("|"); // 14 TFR  
      sb.append(getCfr(term.getRootTerminology().getTerminology())).append("|"); // 15 CFR 
      sb.append(term.getRootTerminology().isPolyhierarchy()).append("|"); // 16 CXTY      
      sb.append(getTtyl(term.getRootTerminology().getTerminology())).append("|");// 17 TTYL 
      sb.append(getAtnl(term.getRootTerminology().getTerminology())).append("|"); // 18 ATNL 
      sb.append(term.getRootTerminology().getLanguage() != null ? term.getRootTerminology().getLanguage() : "").append("|"); // 19 LAT
      sb.append("UTF-8").append("|"); // 20 CENC 
      sb.append("Y").append("|");  // 21
      sb.append(term.isCurrent()).append("|"); // 22 SABIN
      
      if (rootTerminologyConcept != null) { // TODO remove
        for (Atom atom : rootTerminologyConcept.getAtoms()) {// 23 SSN
          if (atom.getTermType().equals("SSN") && atom.isPublishable()) {
            sb.append(atom.getName()).append("|");
          }
        }
      }
      sb.append(term.getCitation()).append("|"); // 24 SCIT
      
      out.print(sb.toString() + "\n");
      updateProgress();
    }
    out.close();
  }
  
  private String getTfr(String terminology) {
    String queryStr = "select count(*) "
      + "from AtomJpa a " + "where " +
      "a.terminology = :terminology and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    return query.getSingleResult().toString();
  }
  
  private String getCfr(String terminology) {
    String queryStr = "select count(*) "
      + "from ConceptJpa c join c.atoms a where a.terminology = :terminology and c.terminology = :projectTerminology" +
      " and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    query.setParameter("projectTerminology", getProject().getTerminology());
    return query.getSingleResult().toString();
  }
  
  private String getTtyl(String terminology) {
    String queryStr = "select distinct termType "
      + "from AtomJpa a where a.terminology = :terminology" +
      " and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    List<String> list = query.getResultList();
    Collections.sort(list);
    StringBuilder sb = new StringBuilder();
    for (String tty : list) {
      sb.append(tty).append(",");
    }
    if (sb.toString().endsWith(",")) {
      return sb.toString().substring(0, sb.toString().length() -1);
    }
    return "";
  }
  
  private String getAtnl(String terminology) {
    String queryStr = "select distinct name "
      + "from AttributeJpa a where a.terminology = :terminology" +
      " and a.publishable = true";
    javax.persistence.Query query = manager.createQuery(queryStr);
    query.setParameter("terminology", terminology);
    List<String> list = query.getResultList();
    Collections.sort(list);
    StringBuilder sb = new StringBuilder();
    for (String atn : list) {
      sb.append(atn).append(",");
    }
    if (sb.toString().endsWith(",")) {
      return sb.toString().substring(0, sb.toString().length() -1);
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

    final File dir = new File(config.getProperty("source.data.dir") + "/" +  
      getProcess().getInputPath() + "/" + getProcess().getVersion() + "/" + "META");
    final File outputFile = new File(dir, "MRDOC.RRF");

    final PrintWriter out = new PrintWriter(new FileWriter(outputFile));
    List<String> outputLines = new ArrayList<>();
 
    // Field Description DOCKEY,VALUE,TYPE,EXPL
    // 0 DOCKEY
    // 1 VALUE
    // 2 TYPE
    // 3 EXPL

    // Handle AttributeNames
    // e.g.
    // ATN|ACCEPTABILITYID|expanded_form|Acceptability Id|
    for (AttributeName atn : getAttributeNames(getProject().getTerminology(), getProject().getVersion()).getObjects()) {      
      StringBuilder sb = new StringBuilder();
      sb.append("ATN").append("|");
      sb.append(atn.getAbbreviation()).append("|");
      sb.append("expanded_form").append("|");
      sb.append(atn.getExpandedForm()).append("|");
      outputLines.add(sb.toString());
      }

      // Handle Languages
     for (Language lat : getLanguages(getProject().getTerminology(), getProject().getVersion()).getObjects()) {
       StringBuilder sb = new StringBuilder();
       sb.append("LAT").append("|");
       sb.append(lat.getAbbreviation()).append("|");
       sb.append("expanded_form").append("|");
       sb.append(lat.getExpandedForm()).append("|");
       outputLines.add(sb.toString());
      }

      // Handle AdditionalRelationshipLabel
      for(AdditionalRelationshipType rela : getAdditionalRelationshipTypes(getProject().getTerminology(), getProject().getVersion()).getObjects()) {
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
      for(RelationshipType rela : getRelationshipTypes(getProject().getTerminology(), getProject().getVersion()).getObjects()) {
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


      for(TermType tty : getTermTypes(getProject().getTerminology(), getProject().getVersion()).getObjects()) {
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
              
      }

      // General metadata entries (skip MAPATN)
      for(GeneralMetadataEntry entry : getGeneralMetadataEntries(getProject().getTerminology(), getProject().getVersion()).getObjects()) {
          StringBuilder sb = new StringBuilder();
          sb.append(entry.getKey()).append("|");
          sb.append(entry.getAbbreviation()).append("|");
          sb.append(entry.getType()).append("|");
          sb.append(entry.getExpandedForm()).append("|"); 
          outputLines.add(sb.toString());
      }
      
      // sort and write to file
      Collections.sort(outputLines);
      for (String line : outputLines) {
        out.print(line + "\n");
      }
      out.close();
  }
  
  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a

  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

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
          "RRF METADATA progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }
}
