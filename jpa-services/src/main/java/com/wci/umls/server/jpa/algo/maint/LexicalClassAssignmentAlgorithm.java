/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.maint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.AlgorithmParameter;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.meta.LexicalClassIdentityJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
import com.wci.umls.server.jpa.services.handlers.RrfComputePreferredNameHandler;
import com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
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
    setWorkId("LUI_REASSIGNMENT");
    setLastModifiedBy("admin");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {

    if (getProject() == null) {
      throw new Exception("Stamping requires a project to be set");
    }

    // n/a - NO preconditions
    return new ValidationResultJpa();
  }

  /* see superclass */
  @SuppressWarnings("unused")
  @Override
  public void compute() throws Exception {
    logInfo("Starting lexical class assignment");
    logInfo("  project = " + getProject().getId());
    logInfo("  workId = " + getWorkId());
    logInfo("  activityId = " + getActivityId());
    logInfo("  user  = " + getLastModifiedBy());

    // TODO: log/commit, progress/cancel

    try {
      // Assume this is configured to be a umls identifier handler properly
      // configured
      final UmlsIdentifierAssignmentHandler handler =
          (UmlsIdentifierAssignmentHandler) getIdentifierAssignmentHandler(
              getProject().getTerminology());
      final UmlsIdentityService service = new UmlsIdentityServiceJpa();

      // Track changes
      final Map<Long, String> postLuiLexicalClassMap = new HashMap<>(20000);
      final Map<String, Long> postLexicalClassLuiMap = new HashMap<>(20000);

      // Algorithm
      // 0. Cache all lexical class/normal form in memory
      final Map<Long, String> preLuiLexicalClassMap = new HashMap<>(20000);
      final Map<String, Long> preLexicalClassLuiMap = new HashMap<>(20000);

      Session session = manager.unwrap(Session.class);
      org.hibernate.Query hQuery =
          session.createQuery("select l from LexicalClassIdentityJpa l");
      hQuery.setReadOnly(true).setFetchSize(5000).setCacheable(false);
      ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
      int ct = 0;
      while (results.next()) {
        final LexicalClassIdentity lui =
            (LexicalClassIdentity) results.get()[0];
        preLuiLexicalClassMap.put(lui.getId(), lui.getNormalizedName());
        preLexicalClassLuiMap.put(lui.getNormalizedName(), lui.getId());
      }
      results.close();

      // 1. Rank all atoms in (project) precedence order and iterate through
      final Map<Long, String> atomRankMap = new HashMap<>(20000);
      session = manager.unwrap(Session.class);
      hQuery = session.createQuery(
          "select distinct c from ConceptJpa c join c.atoms a where c.terminology = :terminology "
              + "  and c.version = :version");
      hQuery.setParameter("terminology", getTerminology());
      hQuery.setParameter("version", getVersion());
      hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
      results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
      // NOTE: this assumes RRF preferred name handler
      final RrfComputePreferredNameHandler prefHandler =
          new RrfComputePreferredNameHandler();
      final PrecedenceList list = getProject().getPrecedenceList();
      while (results.next()) {
        final Concept concept = (Concept) results.get()[0];
        for (final Atom atom : concept.getAtoms()) {
          atomRankMap.put(atom.getId(), prefHandler.getRank(atom, list));
        }
      }
      results.close();

      // Sort all atoms -?? no idea how long this takes for full set
      final List<Long> atomIds = new ArrayList<>(atomRankMap.keySet());
      Collections.sort(atomIds,
          (a1, a2) -> atomRankMap.get(a1).compareTo(atomRankMap.get(a2)));

      // Iterate through sorted atom ids
      int updatedLuis = 0;
      int newLuis = 0;
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
          postLuiLexicalClassMap.put(lui, normalForm);
          preLexicalClassLuiMap.remove(normalForm);
          preLuiLexicalClassMap.remove(lui);

        } else if (postLexicalClassLuiMap.containsKey(normalForm)) {
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
          final LexicalClassIdentity lci = new LexicalClassIdentityJpa();
          lci.setId(lui);
          lci.setLanguage(atom.getLanguage());
          lci.setNormalizedName(normalForm);
          service.addLexicalClassIdentity(lci);
          atom.setLexicalClassId(handler.convertId(lui, "LUI"));
          updateAtom(atom);
          postLexicalClassLuiMap.put(normalForm, lui);
          postLuiLexicalClassMap.put(lui, normalForm);
        }
      }

      logInfo("Finished lexical class assignment");

    } catch (Exception e) {
      logError("Unexpected problem - " + e.getMessage());
      throw e;
    }

  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a - No reset
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
  public List<AlgorithmParameter> getParameters() {
    return super.getParameters();
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return "Reassign lexical classes following an update or change in the string normalizer.";
  }
}
