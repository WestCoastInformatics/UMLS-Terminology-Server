/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubset;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptRelationship;
import com.wci.umls.server.model.content.ConceptSubset;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Default implementation of {@link GraphResolutionHandler}. This connects
 * graphs at the level at which CascadeType.ALL is used in the data model.
 */
public class DefaultGraphResolutionHandler implements GraphResolutionHandler {

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
  public void resolve(Concept concept, Set<String> hierarchicalRelTypeIds)
    throws Exception {
    if (concept != null) {
      boolean nullId = concept.getId() == null;
      concept.setMembers(new ArrayList<ConceptSubsetMember>());

      // Attributes
      resolveAttributes(concept, nullId);

      // Definitions
      for (Definition def : concept.getDefinitions()) {
        resolveDefinition(def, nullId);
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

      // Relationships
      for (ConceptRelationship rel : concept.getRelationships()) {
        if (nullId) {
          rel.setId(null);
        }
        resolve(rel);
      }
      // TODO: skip rels
      //concept.setRelationships(new ArrayList<ConceptRelationship>());

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
    concept.setMembers(new ArrayList<ConceptSubsetMember>());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolveEmpty
   * (com.wci.umls.server.model.content.Descriptor)
   */
  @Override
  public void resolveEmpty(Descriptor descriptor) {
    descriptor.setAtoms(new ArrayList<Atom>());
    descriptor.setDefinitions(new ArrayList<Definition>());
    descriptor.setAttributes(new ArrayList<Attribute>());
    descriptor.setRelationships(new ArrayList<DescriptorRelationship>());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolveEmpty
   * (com.wci.umls.server.model.content.Code)
   */
  @Override
  public void resolveEmpty(Code code) {
    code.setAtoms(new ArrayList<Atom>());
    code.setAttributes(new ArrayList<Attribute>());
    code.setRelationships(new ArrayList<CodeRelationship>());
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

      atom.getName();
      atom.getConceptTerminologyIds().keySet();
      atom.getAlternateTerminologyIds().keySet();
      atom.setMembers(new ArrayList<AtomSubsetMember>());

      // Attributes
      resolveAttributes(atom, nullId);

      // Definitions
      for (Definition def : atom.getDefinitions()) {
        resolveDefinition(def, nullId);
      }

      // Relationships
      for (AtomRelationship rel : atom.getRelationships()) {
        if (nullId) {
          rel.setId(null);
        }
        resolve(rel);
      }

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
      relationship.getAlternateTerminologyIds().keySet();
      resolveAttributes(relationship, relationship.getId() == null);
    } else if (relationship == null) {
      throw new Exception("Cannot resolve a null relationship.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.SemanticTypeComponent)
   */
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
  public void resolve(Descriptor descriptor, Set<String> isaRelTypeIds)
    throws Exception {
    if (descriptor != null) {
      boolean nullId = descriptor.getId() == null;

      // Attributes
      resolveAttributes(descriptor, nullId);

      // Definitions
      for (Definition def : descriptor.getDefinitions()) {
        resolveDefinition(def, nullId);
      }

      // Atoms
      for (Atom atom : descriptor.getAtoms()) {
        // if the concept is "new", then the atom must be too
        if (nullId) {
          atom.setId(null);
        }
        resolve(atom);
      }

      // Relationships
      for (DescriptorRelationship rel : descriptor.getRelationships()) {
        if (nullId) {
          rel.setId(null);
        }
        resolve(rel);
      }

    } else if (descriptor == null) {
      throw new Exception("Cannot resolve a null descriptor.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com
   * .wci.umls.server.model.content.Code, java.util.Set)
   */
  @Override
  public void resolve(Code code, Set<String> isaRelTypeIds) throws Exception {
    if (code != null) {
      boolean nullId = code.getId() == null;

      // Attributes
      resolveAttributes(code, nullId);

      // Atoms
      for (Atom atom : code.getAtoms()) {
        // if the concept is "new", then the atom must be too
        if (nullId) {
          atom.setId(null);
        }
        resolve(atom);
      }

      // Relationships
      for (CodeRelationship rel : code.getRelationships()) {
        if (nullId) {
          rel.setId(null);
        }
        resolve(rel);
      }

    } else if (code == null) {
      throw new Exception("Cannot resolve a null code.");
    }
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
        atom.getName();
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
        atom.getName();
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

  @Override
  public void resolve(Subset subset) throws Exception {
    if (subset != null) {
      boolean nullId = subset.getId() == null;
      subset.getName();

      // Attributes
      resolveAttributes(subset, nullId);

      // skip members
      subset.clearMembers();

    } else if (subset == null) {
      throw new Exception("Cannot resolve a null subset.");
    }
  }

  @Override
  public void resolve(
    SubsetMember<? extends ComponentHasAttributesAndName> member)
    throws Exception {
    if (member != null) {
      boolean nullId = member.getId() == null;
      member.getTerminology();

      // Attributes
      resolveAttributes(member, nullId);

      // Resolve underlying entity
      member.getMember().getName();

    } else if (member == null) {
      throw new Exception("Cannot resolve a null member.");
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
      att.getAlternateTerminologyIds().keySet();
      if (nullId) {
        att.setId(null);
      }
    }
  }

  /**
   * Resolve atom subset members.
   *
   * @param subset the subset
   * @param nullId the null id
   */
  @SuppressWarnings("static-method")
  private void resolveAtomSubsetMembers(AtomSubset subset,
    boolean nullId) {
    for (AtomSubsetMember member : subset.getMembers()) {
      member.getMember();
      if (member.getAttributes() != null)
        member.getAttributes().size();
      if (nullId) {
        member.setId(null);
      }
    }
  }
  
  /**
   * Resolve concept subset members.
   *
   * @param subset the subset
   * @param nullId the null id
   */
  @SuppressWarnings("static-method")
  private void resolveConceptSubsetMembers(ConceptSubset subset,
    boolean nullId) {
    for (ConceptSubsetMember member : subset.getMembers()) {
      member.getMember();
      if (member.getAttributes() != null)
        member.getAttributes().size();
      if (nullId) {
        member.setId(null);
      }
    }
  }  

  /**
   * Resolve definition.
   *
   * @param definition the definition
   * @param nullId the null id
   */
  private void resolveDefinition(Definition definition, boolean nullId) {
    definition.getValue();
    definition.getAlternateTerminologyIds().keySet();
    if (nullId) {
      definition.setId(null);
    }

    resolveAttributes(definition, nullId);
  }

  /* (non-Javadoc)
   * @see com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com.wci.umls.server.model.content.AtomSubset)
   */
  @Override
  public void resolve(AtomSubset subset) throws Exception {
    if (subset != null) {
      boolean nullId = subset.getId() == null;

      subset.getName();
      
      // Attributes
      resolveAttributes(subset, nullId);
      
      // AtomSubsetMembers
      resolveAtomSubsetMembers(subset, nullId);


    } else if (subset == null) {
      throw new Exception("Cannot resolve a null subset.");
    }

  }
  
  /* (non-Javadoc)
   * @see com.wci.umls.server.services.handlers.GraphResolutionHandler#resolve(com.wci.umls.server.model.content.ConceptSubset)
   */
  @Override
  public void resolve(ConceptSubset subset) throws Exception {
    if (subset != null) {
      boolean nullId = subset.getId() == null;

      subset.getName();
      
      // Attributes
      resolveAttributes(subset, nullId);
      
      // ConceptSubsetMembers
      resolveConceptSubsetMembers(subset, nullId);


    } else if (subset == null) {
      throw new Exception("Cannot resolve a null subset.");
    }

  }
  


}
