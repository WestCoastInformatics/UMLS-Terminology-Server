/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.insert;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.FieldedStringTokenizer;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractInsertMaintReleaseAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.content.CodeJpa;
import com.wci.umls.server.jpa.content.ConceptJpa;
import com.wci.umls.server.jpa.content.DescriptorJpa;
import com.wci.umls.server.jpa.content.LexicalClassJpa;
import com.wci.umls.server.jpa.content.StringClassJpa;
import com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Algorithm responsible for combining newly created atoms by UMLS-CUI.
 * Consolidate all new atoms by UMLS-CUI into a single concept, IFF that
 * UMLS-CUI is not split across different NCIMTH concepts.
 * This is an efficiency measure to ensure large influxes of new atoms
 * don't clog up the merge steps
 */
public class ConsolideByCUIAlgorithm extends AbstractInsertMaintReleaseAlgorithm {

  /** The MTH cui update count. */
  private int CUIConsolidatedCount = 0;
  
  private int splitCUINotConsolidatedCount = 0;

  /**
   * Instantiates an empty {@link ConsolideByCUIAlgorithm}.
   *
   * @throws Exception the exception
   */
  public ConsolideByCUIAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("CONSOLIDATEBYCUI");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    ValidationResult validationResult = new ValidationResultJpa();

    if (getProject() == null) {
      throw new Exception("Consolidate by CUI requires a project to be set");
    }

    return validationResult;
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());
    System.out.println(getLastModifiedBy());

    // No molecular actions will be generated by this algorithm
    setMolecularActionFlag(false);

    try {

      commitClearBegin();

      // Track all new atoms by umlscui
      Map<String, Set<Long>> umlscuiToNewAtomIds = new HashMap<>();

      // Also track which concepts are with each atom, to avoid having to do
      // individual lookups later
      Map<Long, Long> newAtomIdConceptId = new HashMap<>();

      Query query = getEntityManager().createNativeQuery("SELECT "
          + "cid.conceptTerminologyIds CUI, " + "a.id atomId, " + "c.id conceptId " + "FROM " + "concepts c, "
          + "concepts_atoms ca, " + "atoms a, " + "AtomJpa_conceptTerminologyIds cid " + "WHERE "
          + "c.terminology = 'NCIMTH' " + "AND c.id = ca.concepts_id " + "AND ca.atoms_Id = a.id "
          + "AND a.id = cid.AtomJpa_id " + "AND a.publishable = TRUE "
          + "AND a.id>:maxAtomIdPreInsertion");

      query.setParameter("maxAtomIdPreInsertion",
          getProcess().getExecutionInfo().get("maxAtomIdPreInsertion"));
      List<Object[]> list = query.getResultList();
      for (final Object[] entry : list) {
        final String cui = entry[0].toString();
        final Long atomId = Long.parseLong(entry[1].toString());
        final Long conceptId = Long.parseLong(entry[2].toString());

        if (umlscuiToNewAtomIds.get(cui) == null) {
          Set<Long> atomIds = new HashSet<>();
          umlscuiToNewAtomIds.put(cui, atomIds);
        }
        Set<Long> atomIds = umlscuiToNewAtomIds.get(cui);
        atomIds.add(atomId);
        umlscuiToNewAtomIds.put(cui, atomIds);

        newAtomIdConceptId.put(atomId, conceptId);
      }

      logInfo("  " + umlscuiToNewAtomIds.keySet().size() + " total CUIs to process");      
      
      // Set the number of steps to the number of CUIs
      setSteps(umlscuiToNewAtomIds.keySet().size());

        // Identify which UMLS-CUIs are split into different NCIMTH concepts
        Set<String> splitCuis = new HashSet<>();

        String previousVersion = getPreviousVersion(getProcess().getTerminology());
        if (previousVersion == null) {
          throw new Exception("WARNING - previous version not found for terminology = "
              + getProcess().getTerminology());
        }

        String previousTerminologyVersion = getProcess().getTerminology() + previousVersion;

        Query query2 = getEntityManager().createNativeQuery("SELECT CUI " + "From "
            + "  (   SELECT DISTINCT " + "      cid.conceptTerminologyIds CUI, " + "      c.id "
            + "    FROM " + "      concepts c, " + "      concepts_atoms ca, " + "      atoms a, "
            + "      AtomJpa_conceptTerminologyIds cid " + "    WHERE "
            + "      c.terminology = 'NCIMTH' " + "      AND c.id = ca.concepts_id "
            + "      AND ca.atoms_Id = a.id " + "      AND a.id = cid.AtomJpa_id "
            + "      AND a.publishable = TRUE "
            + "      AND cid.conceptTerminologyIds_KEY = :previousTerminologyVersion " + "    ) a1 "
            + "    group by CUI " + "    having count(*)>1");

        query2.setParameter("previousTerminologyVersion", previousTerminologyVersion);
        List<Object> list2 = query2.getResultList();
        for (final Object entry : list2) {
          final String splitCui = entry.toString();
          splitCuis.add(splitCui);
        }

        // Now go through each CUI and consolidate its new atoms where appropriate
        for (final String umlscui : umlscuiToNewAtomIds.keySet()) {
          
          // Check for a cancelled call once every 100 CUIs
          if (getStepsCompleted() % 100 == 0) {
            checkCancel();
          }          
          
          // If these atoms' UMLS-CUI is split, leave them in separate concepts
          if (splitCuis.contains(umlscui)) {
            splitCUINotConsolidatedCount++;
            updateProgress();
            continue;
          }
          
          // If their UMLS-CUI is not split, consolidate all new atoms into a
          // single concept
          else {
            // Find lowest atom id, and the corresponding concept
            List<Long> atomIdsToConsolidate = new ArrayList<>();
            atomIdsToConsolidate.addAll(umlscuiToNewAtomIds.get(umlscui));
            atomIdsToConsolidate.sort(null);
            final Long conceptIdToConsolidateInto =
                newAtomIdConceptId.get(atomIdsToConsolidate.get(0));
            final Concept conceptToConsolidateInto = getConcept(conceptIdToConsolidateInto);

            if(conceptToConsolidateInto == null) {
              logWarn("Could not find concept for id="+conceptIdToConsolidateInto);
              continue;
            }
            
            for (Long atomId : atomIdsToConsolidate) {
              final Concept concept = getConcept(newAtomIdConceptId.get(atomId));
              if(concept == null) {
                logWarn("Could not find concept for id="+concept);
                continue;
              }
              
              // Don't consolidate a concept into itself
              if (concept.getId().equals(conceptToConsolidateInto.getId())) {
                continue;
              }
              // Add atom to the consolidate concept, and clear out and remove the
              // other concept
              else {
                final Atom atom = getAtom(atomId);
                concept.getAtoms().clear();
                conceptToConsolidateInto.getAtoms().add(atom);
                removeConcept(concept.getId());
              }
            }
            // Update the consolidated concept to finalize the atom additions
            updateConcept(conceptToConsolidateInto);
            CUIConsolidatedCount++;
          }

          updateProgress();
        }

        logInfo("  CUIs consolidated = " + CUIConsolidatedCount);
        logInfo("  split-CUIs not consolidated = " + splitCUINotConsolidatedCount);
        
        commitClearBegin();
        
      } catch (

      Exception e) {
        logError("Unexpected problem - " + e.getMessage());
        throw e;
      }        

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

  /* see superclass */
  @Override
  public String getDescription() {
    return "Combines new atoms by UMLS-CUI, if that CUI is not split across multiple NCIMTH concepts";
  }
}