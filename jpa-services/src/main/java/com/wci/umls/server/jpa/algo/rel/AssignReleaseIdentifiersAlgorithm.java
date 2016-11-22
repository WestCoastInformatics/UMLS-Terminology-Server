/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.algo.rel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.AbstractAlgorithm;
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

    logInfo("Starting Assign release identifiers");
    
    steps = 3;
    previousProgress = 0;
    stepsCompleted = 0;


    // Assign CUIs:
    // • TODO: we need to come back and do a better job here.
    // atom.getConceptTerminologyIds().get(project.getTerminology()) -> “last
    // release cui”
    // Consider writing out the preferred atom of each data structure (CUI,
    // SCUI, SDUI, CODE, RUI) -> for later use
    final IdentifierAssignmentHandler handler =
        getIdentifierAssignmentHandler(getProject().getTerminology());

    javax.persistence.Query query =
        manager.createQuery("select c from ConceptJpa c "
            + "where c.terminologyId = c.id and c.publishable = true and terminology = :terminology");

    query.setParameter("terminology", getProject().getTerminology());
    @SuppressWarnings("unchecked")
    List<Concept> newConcepts = query.getResultList();

    setMolecularActionFlag(false);
    for (final Concept concept : newConcepts) {
      concept.setTerminologyId(handler.getTerminologyId(concept));
      updateConcept(concept);
    }
    updateProgress();

    // Assign RUIs to concept relationships
    // First create map of rel and rela inverses
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
    // Iterate through all concept relationships
    int objectCt = 0;
    // NOTE: Hibernate-specific to support iterating
    final Session session = manager.unwrap(Session.class);
    org.hibernate.Query hQuery = session.createQuery(
        "select a from ConceptRelationshipJpa a WHERE a.publishable = true "
            + "and terminology = :terminology");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    ScrollableResults results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final ConceptRelationship rel = (ConceptRelationship) results.get()[0];

      final String origRui = rel.getTerminologyId();
      rel.setTerminologyId("");
      
      final String rui = handler.getTerminologyId(rel,
          relToInverseMap.get(rel.getRelationshipType()),
          relToInverseMap.get(rel.getAdditionalRelationshipType()));
      if (!origRui.equals(rui)) {
        rel.setTerminologyId(rui);
        updateRelationship(rel);
      }
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }
    commitClearBegin();
    updateProgress();

    // Assign ATUIs for semantic types
    objectCt = 0;
    // TODO: join concepts and semantic types here into one
    hQuery = session
        .createQuery("select a, b from ConceptJpa a join a.semanticTypes b "
            + "where a.publishable = true and b.publishable = true "
            + "and a.terminology = :terminology");
    hQuery.setParameter("terminology", getProject().getTerminology());
    hQuery.setReadOnly(true).setFetchSize(2000).setCacheable(false);
    results = hQuery.scroll(ScrollMode.FORWARD_ONLY);
    while (results.next()) {
      final Concept c = (Concept) results.get()[0];
      final SemanticTypeComponent sty =
          (SemanticTypeComponent) results.get()[1];

      // For each semantic type component (e.g. concept.getSemanticTypes())
      final String origAtui = sty.getTerminologyId();
      sty.setTerminologyId("");
      
      final String atui = handler.getTerminologyId(sty, c);
      if (!origAtui.equals(atui)) {
        sty.setTerminologyId(atui);
        updateSemanticTypeComponent(sty, c);
      }
      logAndCommit(++objectCt, RootService.logCt, RootService.commitCt);
    }

    logInfo("Finished Assign release identifiers");
    updateProgress();
  }

  /* see superclass */
  @Override
  public void reset() throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    checkRequiredProperties(new String[] {
        ""
    }, p);
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
          "ASSIGN RELEASE IDS progress: " + currentProgress + "%");
      previousProgress = currentProgress;
    }
  }
}
