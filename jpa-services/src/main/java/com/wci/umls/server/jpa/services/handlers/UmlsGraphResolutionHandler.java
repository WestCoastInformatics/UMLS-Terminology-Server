/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Default implementation of {@link GraphResolutionHandler}. This connects
 * graphs at the level at which CascadeType.ALL is used in the data model.
 */
public class UmlsGraphResolutionHandler extends DefaultGraphResolutionHandler {

  @Override
  public void resolve(Concept concept) throws Exception {
    if (concept != null) {
      boolean nullId = concept.getId() == null;
      concept.setMembers(new ArrayList<ConceptSubsetMember>());


      // Attributes
      resolveAttributes(concept, nullId);

      // Definitions
      for (final Definition def : concept.getDefinitions()) {
        resolveDefinition(def, nullId);
      }

      // Semantic type components
      for (final SemanticTypeComponent sty : concept.getSemanticTypes()) {
        if (nullId) {
          sty.setId(null);
        }
        sty.getSemanticType();
        resolve(sty);
      }

      // Atoms
      for (final Atom atom : concept.getAtoms()) {
        // if the concept is "new", then the atom must be too
        if (nullId) {
          atom.setId(null);
        }
        resolve(atom);
      }

      // Relationships
      // Default behavior -- do not return relationships, require paging calls
      concept.setRelationships(new ArrayList<ConceptRelationship>());

      // lazy initialization of user annotations
      concept.getNotes().size();
      concept.getLabels();

      // lazy initialization of component history
      concept.getComponentHistory().size();

    } else if (concept == null) {
      throw new Exception("Cannot resolve a null concept.");
    }
  }

  @Override
  public void resolve(Atom atom) throws Exception {
    if (atom != null) {
      boolean nullId = atom.getId() == null;
      atom.setMembers(new ArrayList<AtomSubsetMember>());

      atom.getName();
      atom.getConceptTerminologyIds().keySet();
      atom.getAlternateTerminologyIds().keySet();
      atom.getNotes().size();

      // Attributes
      resolveAttributes(atom, nullId);

      // Definitions
      for (final Definition def : atom.getDefinitions()) {
        resolveDefinition(def, nullId);
      }

      // for UMLS view dont read atom subset members
      atom.setMembers(new ArrayList<>());

      // for UMLS view don't read relationship as
      // these are terminology-specific rels
      // they can show when browsing that terminology
      atom.setRelationships(new ArrayList<>());
      atom.setTreePositions(new ArrayList<>());

      // resolve component history
      resolveComponentHistory(atom, nullId);

    } else if (atom == null) {
      throw new Exception("Cannot resolve a null atom.");
    }

  }

}
