/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.wci.umls.server.Project;
import com.wci.umls.server.ValidationResult;
import com.wci.umls.server.algo.action.MolecularActionAlgorithm;
import com.wci.umls.server.helpers.Branch;
import com.wci.umls.server.helpers.TypeKeyValue;
import com.wci.umls.server.jpa.ValidationResultJpa;
import com.wci.umls.server.jpa.algo.action.AbstractMolecularAction;
import com.wci.umls.server.jpa.algo.action.MergeMolecularAction;
import com.wci.umls.server.jpa.algo.action.MoveMolecularAction;
import com.wci.umls.server.jpa.helpers.TypeKeyValueJpa;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.services.ContentService;

/**
 * Validates merging between two {@link Concept}s. It is connected by an
 * approved, publishable, non RQ, SY, (maybe SFO/LFO), (non-weak)
 * {@link Relationship} whose source is not <code>MSH</code> and is not listed
 * in <code>ic_single</code>. *
 *
 */
public class MGV_E extends AbstractValidationCheck {

  /* see superclass */
  @Override
  public void setProperties(Properties p) {
    // n/a
  }

  /* see superclass */
  @Override
  public ValidationResult validateAction(MolecularActionAlgorithm action) {
    ValidationResult result = new ValidationResultJpa();

    // Only run this check on merge and move actions
    if (!(action instanceof MergeMolecularAction
        || action instanceof MoveMolecularAction)) {
      return result;
    }

    final Project project = action.getProject();
    final ContentService service = (AbstractMolecularAction) action;
    final Concept source = (action instanceof MergeMolecularAction
        ? action.getConcept2() : action.getConcept());
    final Concept target = (action instanceof MergeMolecularAction
        ? action.getConcept() : action.getConcept2());
    final List<Atom> source_atoms = (action instanceof MoveMolecularAction
        ? ((MoveMolecularAction) action).getMoveAtoms() : source.getAtoms());

    //
    // Get source data
    // Note, unlike other checks, this one will run on all terminologies NOT
    // explicitly listed in the validation data
    //
    final List<TypeKeyValue> sources = project.getValidationDataFor(getName());

    final List<String> terminologies =
        sources.stream().map(TypeKeyValue::getKey).collect(Collectors.toList());

    // This check will also not run on "MSH" atoms
    terminologies.add("MSH");

    //
    // Get publishable atoms that are NOT in specified list of sources
    //
    final List<Atom> target_atoms = target.getAtoms().stream().filter(
        a -> !terminologies.contains(a.getTerminology()) && a.isPublishable())
        .collect(Collectors.toList());

    final List<Atom> l_source_atoms = source_atoms.stream().filter(
        a -> !terminologies.contains(a.getTerminology()) && a.isPublishable())
        .collect(Collectors.toList());

    //
    // Go through all relationship-types (atom, concept, descriptor, and code),
    // and collect all unique atoms associated with the other end of those
    // relationships. If any of them are in the target concept, it violates this
    // check.
    //
    Set<Atom> relatedAtoms = new HashSet<>();

    // Go through all the publishable atoms in source, and collect
    // all the unique descriptor, concept, and code IDs.
    // TypeKeyValue: [Descriptor/Concept/Code], Terminology|Version,
    // [DescriptorId/ConceptId/CodeId]
    // Also collect atom relationships.
    Set<TypeKeyValue> ids = new HashSet<>();
    @SuppressWarnings("rawtypes")
    Set<Relationship> rels = new HashSet<>();
    for (Atom a : l_source_atoms) {
      if (a.isPublishable()) {
        ids.add(new TypeKeyValueJpa("Descriptor",
            a.getTerminology() + "|" + a.getVersion(), a.getDescriptorId()));
        ids.add(new TypeKeyValueJpa("Code",
            a.getTerminology() + "|" + a.getVersion(), a.getCodeId()));
        ids.add(new TypeKeyValueJpa("Concept",
            a.getTerminology() + "|" + a.getVersion(), a.getConceptId()));

        rels.addAll(a.getRelationships());

      }
    }

    // Cache previously loaded objects
    Map<TypeKeyValue, Object> objectMap = new HashMap<>();

    // Load each object associated with an id, and get all that object's
    // relationships.
    for (TypeKeyValue id : ids) {
      Object obj = null;

      if (objectMap.get(id) != null) {
        obj = objectMap.get(id);
      } else {
        try {
          // Use the type to create the getter
          final Method m = service.getClass().getMethod("get" + id.getType(),
              String.class, String.class, String.class, String.class);
          final String terminology = id.getKey().split("\\|")[0];
          final String version = id.getKey().split("\\|")[1];
          obj = m.invoke(service, id.getValue(), terminology, version,
              Branch.ROOT);

          obj = m.invoke(service, id.getValue(), terminology, version,
              Branch.ROOT);

          if (obj != null) {
            objectMap.put(id, obj);
            if (obj instanceof Descriptor) {
              rels.addAll(((Descriptor) obj).getRelationships());
            } else if (obj instanceof Code) {
              rels.addAll(((Code) obj).getRelationships());
            } else if (obj instanceof Concept) {
              rels.addAll(((Concept) obj).getRelationships());
            }
          }

        } catch (Exception e) {
          e.printStackTrace();
          result.getErrors()
              .add(getName() + ": Error loading " + id.getType() + " with id "
                  + id.getValue() + " and terminology " + id.getKey());
          continue;
        }
      }
    }

    // Get all objects on other side of relationships
    // Only look at publishable, non-SY or RQ relationships.
    List<Object> toObjects = new ArrayList<>();
    for (Relationship<?, ?> rel : rels) {
      if (rel.isPublishable() && !rel.getRelationshipType().equals("SY")
          && !rel.getRelationshipType().equals("RQ")) {
        toObjects.add(rel.getTo());
      }
    }

    // Get all of the publishable atoms associated with those objects
    // and add them to the atom set.
    for (Object o : toObjects) {
      if (o instanceof Descriptor) {
        for (Atom toAtom : ((Descriptor) o).getAtoms()) {
          if (toAtom.isPublishable()) {
            relatedAtoms.add(toAtom);
          }
        }
      }
      if (o instanceof Concept) {
        for (Atom toAtom : ((Concept) o).getAtoms()) {
          if (toAtom.isPublishable()) {
            relatedAtoms.add(toAtom);
          }
        }
      }
      if (o instanceof Code) {
        for (Atom toAtom : ((Code) o).getAtoms()) {
          if (toAtom.isPublishable()) {
            relatedAtoms.add(toAtom);
          }
        }
      }
      if (o instanceof Atom) {
        if (((Atom) o).isPublishable()) {
          relatedAtoms.add((Atom) o);
        }
      }
    }

    // Finally, if any of the related atoms are in target concept, fail.
    for (Atom toA : relatedAtoms) {
      if (target_atoms.contains(toA)) {
        result.getErrors().add(getName()
            + ": Concepts are connected by a publishable relationship");
        return result;
      }
    }
    return result;
  }

  /* see superclass */
  @Override
  public String getName() {
    return this.getClass().getSimpleName();
  }

}
