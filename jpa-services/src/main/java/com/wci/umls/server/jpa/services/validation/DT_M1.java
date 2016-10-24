/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.NoResultException;

import com.google.common.collect.Sets;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.services.ContentServiceJpa;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.services.ContentService;

/**
 * Validates those {@link Concept}s lacking a publishable
 * {@link SemanticTypeComponent}.
 *
 */
public class DT_M1 extends AbstractValidationCheck {

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
    // Get semantic types
    //
    List<SemanticTypeComponent> stys = source.getSemanticTypes();

    //
    // Violation if there are none
    //
    if (stys.isEmpty()) {
      result.getErrors()
          .add(getName() + ": Concept contains no semantic type components");
      return result;
    }

    //
    // Check that there is a publishable STY
    //
    boolean hasPublishableSty = false;
    for (SemanticTypeComponent sty : stys) {
      if (sty.isPublishable()) {
        hasPublishableSty = true;
        break;
      }
    }

    if (!hasPublishableSty) {
      result.getErrors().add(getName()
          + ": Concept contains no publishable semantic type components");
      return result;
    }

    return result;
  }

  /* see superclass */
  @SuppressWarnings({
      "unchecked", "cast"
  })
  @Override
  public Set<Long> validateConcepts(Set<Long> conceptIds, String terminology,
    String version, ContentService contentService) throws Exception {

    // Query to find all concepts without a semantic type.
    // Step 1 - query to find all concept ids from terminology/version with a
    // semantic type
    Set<Long> idsWithSty = null;
    final javax.persistence.Query query =
        ((ContentServiceJpa) contentService).getEntityManager().createQuery(
            "select c.id from ConceptJpa c join c.semanticTypes s where "
                + "c.version = :version and c.terminology = :terminology");
    // Try to retrieve the single expected result If zero or more than one
    // result are returned, log error and set result to null
    try {
      query.setParameter("terminology", terminology);
      query.setParameter("version", version);
      idsWithSty = new HashSet<Long>((List<Long>) query.getResultList());
    } catch (NoResultException e) {
      // n/a
    }

    return Sets.difference(conceptIds, idsWithSty);

  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
