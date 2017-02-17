/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.persistence.Query;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.AtomJpa;
import com.wci.umls.server.jpa.meta.LexicalClassIdentityJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
import com.wci.umls.server.jpa.services.handlers.RrfComputePreferredNameHandler;
import com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.UmlsIdentityService;

/**
 * Implementation of the LUI reassignment algorithm.
 */
public class LexicalClassAssignmentAlgorithm extends AbstractAlgorithm {

  /**
   * Instantiates an empty {@link LexicalClassAssignmentAlgorithm}.
   * @throws Exception if anything goes wrong
   */
  public LexicalClassAssignmentAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("LEXICALCLASSASSIGNMENT");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("LUI assignment requires a project to be set");
    }

    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting " + getName());

    setMolecularActionFlag(false);

    final UmlsIdentifierAssignmentHandler handler =
        (UmlsIdentifierAssignmentHandler) getIdentifierAssignmentHandler(
            getProject().getTerminology());
    final UmlsIdentityService service = new UmlsIdentityServiceJpa();

    try {
      fireProgressEvent(0, "Starting, look up LUI assignments");
      // Assume this is configured to be a umls identifier handler properly
      // configured

      // Track changes
      final Map<String, Long> postLexicalClassLuiMap = new HashMap<>(20000);

      // Algorithm
      // 0. Cache all lexical class/normal form in memory
      final Map<String, Long> preLexicalClassLuiMap = new HashMap<>(20000);

      final Query query = getEntityManager()
          .createQuery("select l.id from LexicalClassIdentityJpa l");
      @SuppressWarnings("unchecked")
      final List<Long> ids = query.getResultList();
      int ct = 0;
      for (final Long id : ids) {
        final LexicalClassIdentity lui = service.getLexicalClassIdentity(id);
        preLexicalClassLuiMap.put(lui.getNormalizedName(), lui.getId());
        if (++ct % 1000 == 0) {
          checkCancel();
        }
      }
      fireProgressEvent(10, "Collect atoms");

      // 1. Rank all atoms in (project) precedence order and iterate through
      final Map<Long, String> atomRankMap = new HashMap<>(20000);
      final Map<String, String> params = new HashMap<>();
      params.put("terminology", getProject().getTerminology());
      params.put("version", getProject().getVersion());
      // Normalization is only for English
      final List<Long> atomIds = executeSingleComponentIdQuery(
          "select a.id from ConceptJpa c join c.atoms a where c.terminology = :terminology "
              + "  and c.version = :version and a.language='ENG'",
          QueryType.JQL, params, AtomJpa.class);
      commitClearBegin();

      fireProgressEvent(11, "Get and rank atoms");
      // NOTE: this assumes RRF preferred name handler
      final RrfComputePreferredNameHandler prefHandler =
          new RrfComputePreferredNameHandler();
      final PrecedenceList list = getPrecedenceList(
          getProject().getTerminology(), getProject().getVersion());
      prefHandler.cacheList(list);
      ct = 0;
      for (final Long atomId : atomIds) {
        final Atom atom = getAtom(atomId);
        final String rank = new String(prefHandler.getRank(atom, list));
        Long id = new Long(atom.getId());
        atomRankMap.put(id, rank);
        logAndCommit(++ct, RootService.logCt, RootService.commitCt);
      }

      fireProgressEvent(20, "Assign LUIs");

      // Sort all atoms
      Collections.sort(atomIds,
          (a1, a2) -> atomRankMap.get(a1).compareTo(atomRankMap.get(a2)));

      // Clear memory.
      atomRankMap.clear();

      // Iterate through sorted atom ids
      int updatedLuis = 0;
      int newLuis = 0;
      int objectCt = 0;
      int startProgress = 20;
      int prevProgress = 0;
      int totalCt = atomIds.size();
      for (final Long id : atomIds) {
        final Atom atom = getAtom(id);

        // 2. Generate normal form
        final String normalForm = getNormalizedString(atom.getName());

        // 3. Check whether this normal form is already associated with a LUI
        // 3a. if so, assign that LUI to this atom and record it
        if (preLexicalClassLuiMap.containsKey(normalForm)) {
          final Long lui = preLexicalClassLuiMap.get(normalForm);
          final String lexicalClassId = handler.convertId(lui, "LUI");
          // Change LUI if needed
          if (!atom.getLexicalClassId().equals(lexicalClassId)) {
            atom.setLexicalClassId(lexicalClassId);
            updateAtom(atom);
            updatedLuis++;
          }
          postLexicalClassLuiMap.put(normalForm, lui);
          preLexicalClassLuiMap.remove(normalForm);

        }

        // 4. Check if this normal form has been assigned a LUI already, reuse
        // it
        else if (postLexicalClassLuiMap.containsKey(normalForm)) {
          final Long lui = postLexicalClassLuiMap.get(normalForm);
          final String lexicalClassId = handler.convertId(lui, "LUI");
          // Change LUI if needed
          if (!atom.getLexicalClassId().equals(lexicalClassId)) {
            atom.setLexicalClassId(lexicalClassId);
            updateAtom(atom);
            updatedLuis++;
          }
        }

        // 3b. create a new LUI and record it
        else {
          final Long lui = service.getNextLexicalClassId();
          final LexicalClassIdentity lexicalClassId =
              new LexicalClassIdentityJpa();
          lexicalClassId.setId(lui);
          lexicalClassId.setLanguage(atom.getLanguage());
          lexicalClassId.setNormalizedName(normalForm);
          service.addLexicalClassIdentity(lexicalClassId);
          newLuis++;
          atom.setLexicalClassId(handler.convertId(lui, "LUI"));
          updateAtom(atom);
          postLexicalClassLuiMap.put(normalForm, lui);
        }

        // Log, commit, progress, cancel
        int progress = (int) (startProgress + (80.0 * objectCt / totalCt));
        if (progress != prevProgress) {
          checkCancel();
          prevProgress = progress;
          fireProgressEvent(progress, "Continuing to assign LUIs.");
        }
        logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
      }
      // commit when finished
      commitClearBegin();

      fireProgressEvent(100, "Finished");

      logInfo("  updated LUIs ct = " + updatedLuis);
      logInfo("  new LUIs ct = " + newLuis);
      logInfo("Finished " + getName());

    } catch (

    Exception e) {
      e.printStackTrace();
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    } finally {
      service.close();
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
    return super.getParameters();
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Reassign lexical classes following an update or change in the string normalizer.";
  }
}
