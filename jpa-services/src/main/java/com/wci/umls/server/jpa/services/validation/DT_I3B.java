/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.NoResultException;

import com.google.common.collect.Sets;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.workflow.WorkflowStatus;
import com.wci.umls.server.services.ContentService;

/**
 * Validates those {@link Concept}s which contain at least one demoted
 * {@link AtomRelationship} without a matching publishable
 * {@link ConceptRelationship}
 *
 */
public class DT_I3B extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult validate(Concept source) {
    ValidationResult result = new ValidationResultJpa();

    if (source == null) {
      return result;
    }

    //
    // Get demotions
    //
    List<AtomRelationship> demotions = new ArrayList<AtomRelationship>();
    for (Atom atom : source.getAtoms()) {
      for (AtomRelationship atomRel : atom.getRelationships()) {
        if (atomRel.getWorkflowStatus().equals(WorkflowStatus.DEMOTION)) {
          demotions.add(atomRel);
        }
      }
    }

    //
    // Assume no violation
    //
    boolean matchFound = true;

    //
    // Scan for violations
    //

    for (AtomRelationship demotion : demotions) {
      matchFound = false;
      for (ConceptRelationship rel : source.getRelationships()) {
        Concept toConcept = rel.getTo();
        for (Atom toAtom : toConcept.getAtoms()) {
          if (toAtom.getId().equals(demotion.getTo().getId())) {
            matchFound = true;
          }
        }
      }
      //
      // If we did not find a matching Concept relationship, VIOLATION!
      //
      if (!matchFound) {
        result.getErrors().add(getName()
            + ": Concept contains at least one demoted relationship without a matching publishable relationship");
        return result;
      }
    }

    return result;
  }

  /* see superclass */
  @SuppressWarnings("unchecked")
  @Override
  public Set<Long> validateConcepts(Set<Long> conceptIds, String terminology,
    String version, ContentService contentService) throws Exception {

    // Query to find all concepts without a semantic type.
    // Step 1 = query to find all demoted relationships
    Set<Long> demotedRelIds = null;
    final javax.persistence.Query query =
        ((ContentServiceJpa) contentService).getEntityManager()
            .createQuery("select c2.id "
                + "from AtomRelationshipJpa a, ConceptJpa c2 join c2.atoms ca "
                + "where c2.terminology = :terminology and c2.version = :version and "
                + "a.from.id in (ca.id) and a.workflowStatus='DEMOTION'");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      demotedRelIds = new HashSet<Long>(query.getResultList());
    } catch (NoResultException e) {
      demotedRelIds = new HashSet<>();
    }
    
    if (demotedRelIds.isEmpty()) {
      return new HashSet<>();
    }

    // Step 2 = query to find all publishable concept relationships
    Set<Long> cRelIds = new HashSet<>();
    if (demotedRelIds.size() > 0 ){
    final javax.persistence.Query query2 =
        ((ContentServiceJpa) contentService).getEntityManager()
            .createQuery("select a.from.id " + "from ConceptRelationshipJpa a "
                + "where terminology = :terminology and version = :version"
                + " and publishable = 1 and a.from.id in (:conceptIds)");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query2.setParameter("terminology", terminology);
      query2.setParameter("version", version);
      query2.setParameter("conceptIds", demotedRelIds);
      cRelIds = new HashSet<Long>(query2.getResultList());
    } catch (Exception e) {
      cRelIds = new HashSet<>();
    }
    }
    // Get the intersection of ids passed in with
    // the (demoted MINUS c level rels)
    return Sets.intersection(Sets.difference(demotedRelIds, cRelIds),
        conceptIds);

  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
