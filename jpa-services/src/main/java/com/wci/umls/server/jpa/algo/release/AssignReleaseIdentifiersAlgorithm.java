/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.release;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.PrecedenceList;
import com.wci.umls.server.helpers.QueryType;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
import com.wci.umls.server.jpa.content.ConceptRelationshipJpa;
import com.wci.umls.server.jpa.services.handlers.RrfComputePreferredNameHandler;
import com.wci.umls.server.jpa.services.handlers.UmlsIdentifierAssignmentHandler;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.meta.AdditionalRelationshipType;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.services.RootService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Algorithm for assigning release identifiers.
 */
public class AssignReleaseIdentifiersAlgorithm extends AbstractAlgorithm {

  /** The previous progress. */
  private int previousProgress;

  /** The steps. */
  private int steps;

  /** The steps completed. */
  private int stepsCompleted;

  /**
   * Instantiates an empty {@link AssignReleaseIdentifiersAlgorithm}.
   *
   * @throws Exception the exception
   */
  public AssignReleaseIdentifiersAlgorithm() throws Exception {
    super();
    setActivityId(UUID.randomUUID().toString());
    setWorkId("ASSIGNRELEASEIDS");
  }

  /* see superclass */
  @Override
  public ValidationResult checkPreconditions() throws Exception {
    return new ValidationResultJpa();
  }

  /* see superclass */
  @Override
  public void compute() throws Exception {
    logInfo("Starting assign release identifiers");
    fireProgressEvent(0, "Starting");
    this.setMolecularActionFlag(false);

    // TODO Save cui/rui/atui max values for "reset"
    // need an accessible properties object for this - the release info??

    steps = 4;
    previousProgress = 0;
    stepsCompleted = 0;

    assignCuis();

    assignRuis();

    assignAtuis();

    fireProgressEvent(100, "Finished");
    logInfo("Finished assign release identifiers");
  }

  /**
   * Assign cuis.
   *
   * @throws Exception the exception
   */
  private void assignCuis() throws Exception {   
    
    // Get a UMLS identity handler
    final UmlsIdentifierAssignmentHandler handler =
        (UmlsIdentifierAssignmentHandler) getIdentifierAssignmentHandler(
            getProject().getTerminology());

    // Assign CUIs:
    // Rank all atoms

    // 1. Rank all atoms in (project) precedence order and iterate through
    final Map<Long, String> atomRankMap = new HashMap<>(20000);
    final Map<Long, Long> atomConceptMap = new HashMap<>(20000);
    // Get conceptId/atomId - unpublishable concepts need CUI assignments also
    final javax.persistence.Query query = manager
        .createQuery("select c.id, a.id from ConceptJpa c join c.atoms a "
            + "where c.terminology = :terminology "
            + "  and c.version = :version");
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> ids = query.getResultList();

    fireProgressEvent(10, "Assign CUIs");

    // NOTE: this assumes RRF preferred name handler
    final RrfComputePreferredNameHandler prefHandler =
        new RrfComputePreferredNameHandler();
    final PrecedenceList list = getPrecedenceList(getProject().getTerminology(),
        getProject().getVersion());
    prefHandler.cacheList(list);
    int ct = 0;
    final List<Long> atomIds = new ArrayList<>(20000);
    for (final Object[] result : ids) {
      final Atom atom = getAtom(Long.valueOf(result[1].toString()));
      final Long conceptId = Long.valueOf(result[0].toString());
      final String rank = new String(prefHandler.getRank(atom, list));
      final Long id = new Long(atom.getId());
      atomRankMap.put(id, rank);
      atomConceptMap.put(atom.getId(), conceptId);
      atomIds.add(atom.getId());
      logAndCommit(++ct, RootService.logCt, RootService.commitCt);
    }

    // Sort all atoms
    Collections.sort(atomIds,
        (a1, a2) -> atomRankMap.get(a1).compareTo(atomRankMap.get(a2)));
    // Clear memory.
    atomRankMap.clear();
    updateProgress();

    // Iterate through sorted atom ids
    int objectCt = 0;
    int prevProgress = 0;
    int totalCt = ids.size();
    final Set<Long> assignedConcepts = new HashSet<>(20000);
    for (final Long id : atomIds) {
      objectCt++;

      // If the concept for this atom already has an assignment, move on
      if (assignedConcepts.contains(atomConceptMap.get(id))) {
        continue;
      }

      final Atom atom = getAtom(id);
      final String cui =
          atom.getConceptTerminologyIds().get(getProject().getTerminology());

      // If the CUI is set, assign it to the concept and move on
      if (cui != null) {
        final Concept concept = getConcept(atomConceptMap.get(id));
        assignedConcepts.add(concept.getId());

        // Assign CUI if not the current CUI
        if (!concept.getTerminologyId().equals(cui)) {
          // Otherwise assign it
          concept.setTerminologyId(cui);
          updateConcept(concept);
        }
      }

      // otherwise, create a new CUI, assign it ,etc.
      else {
        // no assignment -> deal with new concepts later
      }

      // log, commit, check cancel, advance progress
      int progress = (int) (objectCt * 100.0 / totalCt);
      if (progress != prevProgress) {
        checkCancel();
        this.fireAdjustedProgressEvent(progress, stepsCompleted, steps,
            "Assigning CUIs");
        prevProgress = progress;
      }

      logAndCommit(objectCt, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();

    // Go through concepts without assignments and assign new concepts
    for (final Long conceptId : new HashSet<>(atomConceptMap.values())) {
      if (assignedConcepts.contains(conceptId)) {
        continue;
      }
      final Concept concept = getConcept(conceptId);

      // skip unpublishable concepts (should never happen)
      if (!concept.isPublishable()) {
        continue;
      }
      // prompt for new assignment
      concept.setTerminologyId("");
      concept.setTerminologyId(handler.getTerminologyId(concept));
      updateConcept(concept);
    }
    updateProgress();

  }

  /**
   * Assign ruis.
   *
   * @throws Exception the exception
   */
  private void assignRuis() throws Exception {
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());

    // Assign RUIs to concept relationships

    // Cache metadata
    final Map<String, String> relToInverseMap = new HashMap<>();
    for (final RelationshipType relType : getRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }
    for (final AdditionalRelationshipType relType : getAdditionalRelationshipTypes(
        getProject().getTerminology(), getProject().getVersion())
            .getObjects()) {
      relToInverseMap.put(relType.getAbbreviation(),
          relType.getInverse().getAbbreviation());
    }

    // Get all concept rels
    final Map<String, String> params = new HashMap<>();
    params.put("terminology", getProject().getTerminology());
    params.put("version", getProject().getVersion());
    // Normalization is only for English
    final List<Long> relIds = executeSingleComponentIdQuery(
        "select r.id from ConceptRelationshipJpa r WHERE r.publishable = true "
            + "and r.terminology = :terminology",
        QueryType.JQL, params, ConceptRelationshipJpa.class);
    commitClearBegin();

    int objectCt = 0;
    int prevProgress = 0;
    int totalCt = relIds.size();
    for (final Long relId : relIds) {
      final ConceptRelationship rel =
          (ConceptRelationship) getRelationship(relId,
              ConceptRelationshipJpa.class);

      final String origRui = rel.getTerminologyId();
      rel.setTerminologyId("");

      final String rui = handler.getTerminologyId(rel,
          relToInverseMap.get(rel.getRelationshipType()),
          relToInverseMap.get(rel.getAdditionalRelationshipType()));
      if (!origRui.equals(rui)) {
        rel.setTerminologyId(rui);
        updateRelationship(rel);
      }

      // log, commit, check cancel, advance progress
      int progress = (int) (objectCt * 100.0 / totalCt);
      if (progress != prevProgress) {
        checkCancel();
        this.fireAdjustedProgressEvent(progress, stepsCompleted, steps,
            "Assigning RUIs");
        prevProgress = progress;
      }
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    updateProgress();
  }

  /**
   * Assign atuis.
   *
   * @throws Exception the exception
   */
  private void assignAtuis() throws Exception {

    // get handler
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());

    // Assign ATUIs for semantic types
    final javax.persistence.Query query = manager
        .createQuery("select c.id, s.id from ConceptJpa c join c.semanticTypes s "
            + "where c.terminology = :terminology "
            + "  and c.version = :version and c.publishable = true ");
    query.setParameter("terminology", getProject().getTerminology());
    query.setParameter("version", getProject().getVersion());
    @SuppressWarnings("unchecked")
    final List<Object[]> ids = query.getResultList();

    int objectCt = 0;
    int prevProgress = 0;
    int totalCt = ids.size();
    for (final Object[] result : ids) {
      final Concept c = getConcept(Long.valueOf(result[0].toString()));
      final SemanticTypeComponent sty =
          getSemanticTypeComponent(Long.valueOf(result[1].toString()));

      if (sty == null) {
        logInfo("sty is null " + result.toString() + " " + c.toString());
      }
      // For each semantic type component (e.g. concept.getSemanticTypes())
      final String origAtui = sty.getTerminologyId();
      sty.setTerminologyId("");

      final String atui = handler.getTerminologyId(sty, c);
      if (!origAtui.equals(atui)) {
        sty.setTerminologyId(atui);
        updateSemanticTypeComponent(sty, c);
      }

      // log, commit, check cancel, advance progress
      int progress = (int) (objectCt * 100.0 / totalCt);
      if (progress != prevProgress) {
        checkCancel();
        this.fireAdjustedProgressEvent(progress, stepsCompleted, steps,
            "Assigning ATUIs");
        prevProgress = progress;
      }
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    updateProgress();
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

  /**
   * Update progress.
   *
   * @throws Exception the exception
   */
  public void updateProgress() throws Exception {
    stepsCompleted++;
    int currentProgress = (int) ((100.0 * stepsCompleted / steps));
    if (currentProgress > previousProgress) {
      checkCancel();
      fireProgressEvent(currentProgress,
          "ASSIGN RELEASE IDS progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }

  /* see superclass */
  @Override
  public String getDescription() {
    return ConfigUtility.getNameFromClass(getClass());
  }
}
