/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.RelationshipType;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Default implementation of {@link GraphResolutionHandler}. This connects
 * graphs at the level at which CascadeType.ALL is used in the data model.
 */
public class UmlsGraphResolutionHandler implements GraphResolutionHandler {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // do nothing
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Concept, java.util.Set)
   */
  @Override
  public void resolve(Concept concept, List<RelationshipType> isaRelTypeIds)
    throws Exception {
    if (concept != null) {
      boolean nullId = concept.getId() == null;

      // Attributes
      resolveAttributes(concept, nullId);

      // Definitions
      for (Definition def : concept.getDefinitions()) {
        if (nullId) {
          def.setId(null);
        }
        def.getValue();
      }

      // Semantic type components
      for (SemanticTypeComponent sty : concept.getSemanticTypes()) {
        if (nullId) {
          sty.setId(null);
        }
        sty.getSemanticType();
        resolve(sty);
      }

      // Atoms
      for (Atom atom : concept.getAtoms()) {
        // if the concept is "new", then the atom must be too
        if (nullId) {
          atom.setId(null);
        }
        resolve(atom);
      }

      // skip relationships (for UMLS concept)
      concept.setRelationships(new ArrayList<ConceptRelationship>());

    } else if (concept == null) {
      throw new Exception("Cannot resolve a null concept.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolveEmpty
   * (com.wci.umls.server.model.content.Concept)
   */
  @Override
  public void resolveEmpty(Concept concept) {
    concept.setAtoms(new ArrayList<Atom>());
    concept.setSemanticTypes(new ArrayList<SemanticTypeComponent>());
    concept.setDefinitions(new ArrayList<Definition>());
    concept.setAttributes(new ArrayList<Attribute>());
    concept.setRelationships(new ArrayList<ConceptRelationship>());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Atom)
   */
  @Override
  public void resolve(Atom atom) throws Exception {
    if (atom != null) {
      boolean nullId = atom.getId() == null;

      atom.getTerm();
      atom.getConceptTerminologyIds().keySet();
      atom.getAlternateTerminologyIds().keySet();

      // Attributes
      resolveAttributes(atom, nullId);

      // Definitions
      for (Definition def : atom.getDefinitions()) {
        if (nullId) {
          def.setId(null);
        }
        def.getValue();
      }

      atom.setRelationships(new ArrayList<AtomRelationship>());

    } else if (atom == null) {
      throw new Exception("Cannot resolve a null atom.");
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Relationship)
   */
  @Override
  public void resolve(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception {
    if (relationship != null) {
      relationship.getFrom().getTerminology();
      relationship.getTo().getTerminology();
      resolveAttributes(relationship, relationship.getId() == null);
    } else if (relationship == null) {
      throw new Exception("Cannot resolve a null relationship.");
    }
  }

  @Override
  public void resolve(SemanticTypeComponent sty) {
    sty.getSemanticType();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Descriptor, java.util.Set)
   */
  @Override
  public void resolve(Descriptor descriptor, Set<String> isaRelTypeIds) {
    // n/a - there are no "UMLS" descriptors
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Code, java.util.Set)
   */
  @Override
  public void resolve(Code code, Set<String> isaRelTypeIds) {
    // n/a - there are no "UMLS" codes
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.LexicalClass)
   */
  @Override
  public void resolve(LexicalClass lexicalClass) throws Exception {
    if (lexicalClass != null && lexicalClass.getId() != null) {

      // Attributes
      resolveAttributes(lexicalClass, false);

      // Atoms but none of the members
      for (Atom atom : lexicalClass.getAtoms()) {
        // if the concept is "new", then the atom must be too
        atom.getTerm();
        atom.getConceptTerminologyIds().keySet();
        atom.getAlternateTerminologyIds().keySet();

        atom.setAttributes(new ArrayList<Attribute>());
        atom.setDefinitions(new ArrayList<Definition>());
        atom.setRelationships(new ArrayList<AtomRelationship>());
      }

    } else if (lexicalClass == null) {
      throw new Exception("Cannot resolve a null lexical class.");

    } else if (lexicalClass.getId() == null) {
      throw new Exception("Cannot resolve a lexical class with a null id.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.StringClass)
   */
  @Override
  public void resolve(StringClass stringClass) throws Exception {
    if (stringClass != null && stringClass.getId() != null) {

      // Attributes
      resolveAttributes(stringClass, false);

      // Atoms but none of the members
      for (Atom atom : stringClass.getAtoms()) {
        // if the concept is "new", then the atom must be too
        atom.getTerm();
        atom.getConceptTerminologyIds().keySet();
        atom.getAlternateTerminologyIds().keySet();

        atom.setAttributes(new ArrayList<Attribute>());
        atom.setDefinitions(new ArrayList<Definition>());
        atom.setRelationships(new ArrayList<AtomRelationship>());
      }

    } else if (stringClass == null) {
      throw new Exception("Cannot resolve a null string class.");

    } else if (stringClass.getId() == null) {
      throw new Exception("Cannot resolve a string class with a null id.");
    }
  }

  /**
   * Resolve attributes.
   *
   * @param component the component
   * @param nullId the null id
   */
  @SuppressWarnings("static-method")
  private void resolveAttributes(ComponentHasAttributes component,
    boolean nullId) {
    for (Attribute att : component.getAttributes()) {
      att.getName();
      if (nullId) {
        att.setId(null);
      }
    }
  }

}
