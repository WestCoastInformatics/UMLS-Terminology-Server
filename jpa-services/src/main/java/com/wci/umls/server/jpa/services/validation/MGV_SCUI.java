/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merges between two {@link Concept}s that both contain releasable
 * {@link Atom}s from the same {@link Terminology} but different source concept
 * identifier and that {@link Terminology} is listed in <code>ic_single</code>.
 *
 */
public class MGV_SCUI extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /**
   * Validate.
   *
   * @param project the project
   * @param service the service
   * @param source the source
   * @param target the target
   * @param source_atoms the source atoms
   * @return the validation result
   */
  public ValidationResult validate(Project project, ContentService service,
    Concept source, Concept target, List<Atom> source_atoms) {
    ValidationResult result = new ValidationResultJpa();

    //
    // Get sources list
    //
    List<TypeKeyValue> sources = project.getValidationDataFor(getName());

    List<String> terminologies =
        sources.stream().map(TypeKeyValue::getKey).collect(Collectors.toList());

    //
    // Get target atoms from specified list of sources
    //
    List<Atom> target_atoms = target.getAtoms().stream()
        .filter(a -> terminologies.contains(a.getTerminology()))
        .collect(Collectors.toList());

    List<Atom> l_source_atoms = source_atoms.stream()
        .filter(a -> terminologies.contains(a.getTerminology()))
        .collect(Collectors.toList());

    //
    // Find cases of merges where the sources are the same but SCUI are
    // different.
    //
    for (Atom sourceAtom : l_source_atoms) {
      if (sourceAtom.isPublishable()) {
        for (Atom targetAtom : target_atoms) {
          if (targetAtom.isPublishable()
              && targetAtom.getTerminology().equals(sourceAtom.getTerminology())
              && targetAtom.getConceptId() != null
              && !targetAtom.getConceptId().equals(sourceAtom.getConceptId())) {
            result.getErrors().add(
                getName() + ": Publishable atom in source concept has same Terminology but different conceptId as publishable atom in target concept.");
            return result;
          }
        }
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "MGV_SCUI";
  }

}
