/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PfsParameter;
import com.wci.umls.server.helpers.SearchResultList;
import com.wci.umls.server.helpers.content.RelationshipList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.helpers.PfsParameterJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Relationship;

/**
 * In effort to reduce deleted_cuis, create bequeathals to the live parent 
 * or grandparent concept.
 */
public class CreateDeepAncestorBequeathalAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  /**
   * Instantiates an empty {@link CreateDeepAncestorBequeathalAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public CreateDeepAncestorBequeathalAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("CREATEDEEPANCESTORBEQUEATH");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Create ancestor bequeath requires a project to be set");
    }

    // Check the input directories

    final String srcFullPath =
        ConfigUtility.getConfigProperties().getProperty("source.data.dir")
            + File.separator + getProcess().getInputPath();

    setSrcDirFile(new File(srcFullPath));
    if (!getSrcDirFile().exists()) {
      throw new Exception("Specified input directory does not exist");
    }

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    int addedCount = 0;
    
    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    try {

      Set<Concept> deletedCuis = new HashSet<>();
      File srcDir = getSrcDirFile();
      File maintDir = new File(srcDir, "maint");
      if (! maintDir.exists()){
        maintDir.mkdir();
      }
      logInfo("maint dir:" + maintDir);
      BufferedWriter out = new BufferedWriter(new FileWriter(new File(maintDir, "bequeathal.deep.ancestor.relationships.src")));
      
      Query query = getEntityManager().createNativeQuery(
          "SELECT   DISTINCT c.id conceptId FROM   concepts c,   "
          + "concepts_atoms ca,   atoms a WHERE   c.terminology = 'NCIMTH'   "
          + "AND c.id != c.terminologyId   AND c.id = ca.concepts_id   AND "
          + "ca.atoms_id = a.id   "
          + "AND a.publishable = FALSE   AND NOT c.id IN ("
          +   "SELECT       DISTINCT c.id conceptId     " 
          +   " FROM       concepts c,       concepts_atoms ca,       atoms a     "
          +   " WHERE       c.terminology = 'NCIMTH'       AND c.id = ca.concepts_id   "
          +   " AND ca.atoms_id = a.id       AND a.publishable = TRUE   )   " 
          +   " AND NOT c.id IN (     "
          +   "   SELECT       DISTINCT c.id conceptId     "
          +   "    FROM       concepts c,       concept_relationships cr     "
          +   "    WHERE       c.terminology = 'NCIMTH'       AND c.id = cr.from_id       "
          +   "    AND cr.relationshipType like 'B%'   )   AND NOT c.id IN (     "
          +   "      SELECT       c.id conceptId     " 
          +   "        FROM       concepts c,       concepts_atoms ca     "
          +   "        WHERE       c.terminology = 'NCIMTH'       "
          +   "        AND c.id = ca.concepts_id       AND ca.concepts_id IN (         "
          +   "      SELECT           ca.concepts_id         FROM           concepts_atoms ca,           atoms a         " 
          +   "        WHERE           ca.atoms_id = a.id           "
          +   "        AND a.terminology IN (:terminology, 'NCIMTH')           " 
          +   "        AND a.termType = 'PN'       )     GROUP BY       ca.concepts_id     "
          +   "        HAVING       COUNT(DISTINCT ca.atoms_id) = 1   )"
          +   " AND NOT c.id IN (   "
          +   "   SELECT  " 
          +   "     ca.concepts_id conceptId  "
          +   "   FROM  "
          +   "     mrcui mr,  "
          +  "      atomjpa_conceptterminologyids ac,  "
          +  "      concepts_atoms ca,  "
          +  "      concepts cpt  "
          + "     WHERE  "
          +  "      mr.cui1 = ac.conceptTerminologyIds  "
          +  "      AND ca.atoms_id = ac.AtomJpa_id  "
          +  "      AND cpt.id = ca.concepts_id  "
          +  "      AND cpt.terminology = 'NCIMTH'  "
          +  "      AND ac.conceptTerminologyIds_KEY = 'NCIMTH'  "
          +  "      AND mr.rel = 'DEL'  )"
        
          
          );
      
      query.setParameter("terminology",
              getProcess().getTerminology());
      List<Object> list = query.getResultList();
      setSteps(list.size());
     
      int index = 1;
      for (final Object entry : list) {
        final Long id = Long.valueOf(entry.toString());
        Concept c = getConcept(id);
        deletedCuis.add(c);
        c.getAtoms().size();
        c.getRelationships().size();
      }
      for (Concept c : deletedCuis) {
        index++;
        List<String> potentialParentBequeathals = new ArrayList<>();
        RelationshipList relList = findConceptDeepRelationships(c.getTerminologyId(),
            "NCIMTH", "latest", Branch.ROOT, null, true,
            true, false, false, new PfsParameterJpa());
        for (Relationship<? extends ComponentInfo, ? extends ComponentInfo> rel : relList.getObjects()) {
            ConceptRelationship cptRel = (ConceptRelationship)rel;
            if (cptRel.getRelationshipType().equals("PAR")) {
                Concept ncimthParentConcept = cptRel.getFrom();
                if (noXRRel(c, ncimthParentConcept) && conceptPublishable(ncimthParentConcept)) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("").append("|");
                    sb.append("C").append("|");
                    sb.append(c.getTerminologyId()).append("|");
                    sb.append("BBT").append("|").append("|");
                    sb.append(ncimthParentConcept.getTerminologyId()).append("|");
                    sb.append("NCIMTH|NCIMTH|R|n|N|N|SOURCE_CUI|NCIMTH|SOURCE_CUI|NCIMTH|||").append("\n");
                    potentialParentBequeathals.add(sb.toString());
                }
            }
        }

		for (String line : potentialParentBequeathals) {
			out.write(line);
			addedCount++;
			logInfo("[CreateBequeathal parent] " + line);
		}
        updateProgress();
        if (index % 100 == 0) {
          out.flush();
        }
      }

      out.close();



      commitClearBegin();

      logInfo("  added count = " + addedCount);

      logInfo("Finished " + getName());

    } catch (

    Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  private boolean noXRRel(Concept a, Concept b) {
    for (ConceptRelationship cr : a.getRelationships()) {
      if (cr.getRelationshipType().equals("XR") && (cr.getFrom().getId() == b.getId() || cr.getTo().getId() == b.getId())) {
          System.out.println("found XR rel: " + a.getId() + " " + b.getId());
          return false;
      }
    }
    return true;
  }
  
  /* see superclass */
  @Override
  public void reset() throws Exception {
    logInfo("Starting RESET " + getName());
    // n/a - No reset
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
  public List<AlgorithmParameter> getParameters() throws Exception {
    final List<AlgorithmParameter> params = super.getParameters();

    return params;
  }

  @Override
  public String getDescription() {
    return "Bequeaths deleted cuis to their closest published ancestor";
  }

  private boolean conceptPublishable(Concept cpt) {
	  return cpt.getAtoms().stream().filter(a -> a.isPublishable() && !a.getTerminology().equals("NCIMTH")
			  && !a.getTerminology().startsWith("MTH_")).count() > 0;
  }
  
}