/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.jpa.AbstractConfigurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.AtomRelationship;
import com.wci.umls.server.model.content.AtomSubsetMember;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.CodeRelationship;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentHistory;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.ConceptSubsetMember;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.DescriptorRelationship;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.model.meta.RootTerminology;
import com.wci.umls.server.model.meta.Terminology;
import com.wci.umls.server.services.handlers.GraphResolutionHandler;

/**
 * Default implementation of {@link GraphResolutionHandler}. This connects
 * graphs at the level at which CascadeType.ALL is used in the data model.
 */
public class DefaultGraphResolutionHandler extends AbstractConfigurable
    implements GraphResolutionHandler {

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // do nothing
  }

  /* see superclass */
  @Override
  public void resolve(Concept concept) throws Exception {
    if (concept != null) {
      boolean nullId = concept.getId() == null;

      // subset members
      for (final ConceptSubsetMember member : concept.getMembers()) {
        member.getTerminology();
        resolveAttributes(member, nullId);
      }

      // Attributes
      resolveAttributes(concept, nullId);

      // Component History
      resolveComponentHistory(concept, nullId);

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

      // Subsets
      for (final ConceptSubsetMember subset : concept.getMembers()) {
        if (nullId) {
          subset.setId(null);
        }
        resolve(subset);
      }

      // Relationships
      // default behavior -- require paging of relationships
      concept.setRelationships(new ArrayList<>(0));
      concept.setTreePositions(new ArrayList<>(0));

      // user annotations -- lazy initialize
      concept.getNotes().size();
      concept.getLabels().size();

    } else if (concept == null) {
      throw new Exception("Cannot resolve a null concept.");
    }
  }

  /* see superclass */
  @Override
  public void resolveEmpty(Concept concept) {
    concept.setAtoms(new ArrayList<>(0));
    concept.setSemanticTypes(new ArrayList<>(0));
    concept.setDefinitions(new ArrayList<>(0));
    concept.setAttributes(new ArrayList<>(0));
    concept.setRelationships(new ArrayList<>(0));
    concept.setMembers(new ArrayList<>(0));
    concept.setNotes(new ArrayList<>(0));
    concept.setComponentHistory(new ArrayList<>(0));
  }

  /* see superclass */
  @Override
  public void resolveEmpty(Descriptor descriptor) {
    descriptor.setAtoms(new ArrayList<>(0));
    descriptor.setDefinitions(new ArrayList<>(0));
    descriptor.setAttributes(new ArrayList<>(0));
    descriptor.setRelationships(new ArrayList<>(0));
    descriptor.setNotes(new ArrayList<>(0));
  }

  /* see superclass */
  @Override
  public void resolveEmpty(Code code) {
    code.setAtoms(new ArrayList<>(0));
    code.setAttributes(new ArrayList<>(0));
    code.setRelationships(new ArrayList<>(0));
    code.setNotes(new ArrayList<>(0));
  }

  /* see superclass */
  @Override
  public void resolve(Atom atom) throws Exception {
    if (atom != null) {
      boolean nullId = atom.getId() == null;

      atom.getName();
      // atom.getConceptTerminologyIds().keySet();
      // atom.getAlternateTerminologyIds().keySet();
      atom.setConceptTerminologyIds(new HashMap<>(0));
      atom.setAlternateTerminologyIds(new HashMap<>(0));

      atom.setMembers(new ArrayList<AtomSubsetMember>(0));

      // Attributes
      resolveAttributes(atom, nullId);

      // Component History
      resolveComponentHistory(atom, nullId);

      // Definitions
      for (final Definition def : atom.getDefinitions()) {
        resolveDefinition(def, nullId);
      }

      for (final AtomSubsetMember member : atom.getMembers()) {
        resolve(member);
      }

      // Relationships
      for (final AtomRelationship rel : atom.getRelationships()) {
        if (nullId) {
          rel.setId(null);
        }
        resolve(rel);
      }

      atom.setTreePositions(new ArrayList<>(0));
      atom.getNotes().size();

    } else if (atom == null) {
      throw new Exception("Cannot resolve a null atom.");
    }

  }

  /* see superclass */
  @Override
  public void resolve(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception {
    if (relationship != null) {
      if (relationship.getFrom() != null) {
        relationship.getFrom().getTerminology();
      }
      if (relationship.getTo() != null) {
        relationship.getTo().getTerminology();
      }
      // relationship.getAlternateTerminologyIds().keySet();
      relationship.setAlternateTerminologyIds(new HashMap<>(0));
      resolveAttributes(relationship, relationship.getId() == null);
    } else if (relationship == null) {
      throw new Exception("Cannot resolve a null relationship.");
    }
  }

  /* see superclass */
  @Override
  public void resolve(TreePosition<?> treepos) throws Exception {
    if (treepos != null) {
      treepos.getAncestorPath();

      // Tree positions don't have attributes yet.
      treepos.setAttributes(new ArrayList<Attribute>(0));
    }

  }

  /* see superclass */
  @Override
  public void resolve(SemanticTypeComponent sty) {
    sty.getSemanticType();
  }

  /* see superclass */
  @Override
  public void resolve(Descriptor descriptor) throws Exception {
    if (descriptor != null) {
      boolean nullId = descriptor.getId() == null;

      // Attributes
      resolveAttributes(descriptor, nullId);

      // Definitions
      for (final Definition def : descriptor.getDefinitions()) {
        resolveDefinition(def, nullId);
      }

      // Atoms
      for (final Atom atom : descriptor.getAtoms()) {
        // if the concept is "new", then the atom must be too
        if (nullId) {
          atom.setId(null);
        }
        resolve(atom);
      }

      // Relationships
      // default behavior -- require paging of relationships
      descriptor.setRelationships(new ArrayList<DescriptorRelationship>(0));
      descriptor.setTreePositions(new ArrayList<>(0));

      // user annotations -- lazy initialize
      descriptor.getNotes().size();
      descriptor.getLabels().size();

    } else if (descriptor == null) {
      throw new Exception("Cannot resolve a null descriptor.");
    }
  }

  /* see superclass */
  @Override
  public void resolve(Code code) throws Exception {
    if (code != null) {
      boolean nullId = code.getId() == null;

      // Attributes
      resolveAttributes(code, nullId);

      // Atoms
      for (final Atom atom : code.getAtoms()) {
        // if the concept is "new", then the atom must be too
        if (nullId) {
          atom.setId(null);
        }
        resolve(atom);
      }

      // Relationships
      // default behavior -- require paging of relationships
      code.setRelationships(new ArrayList<CodeRelationship>(0));
      code.setTreePositions(new ArrayList<>(0));
      // user annotations -- lazy initialize
      code.getNotes().size();
      code.getLabels().size();

    } else if (code == null) {
      throw new Exception("Cannot resolve a null code.");
    }
  }

  /* see superclass */
  @Override
  public void resolve(LexicalClass lexicalClass) throws Exception {
    if (lexicalClass != null && lexicalClass.getId() != null) {

      // Attributes
      resolveAttributes(lexicalClass, false);

      // Atoms but none of the members
      for (final Atom atom : lexicalClass.getAtoms()) {
        // if the concept is "new", then the atom must be too
        atom.getName();
        // atom.getConceptTerminologyIds().keySet();
        // atom.getAlternateTerminologyIds().keySet();
        atom.setConceptTerminologyIds(new HashMap<>(0));
        atom.setAlternateTerminologyIds(new HashMap<>(0));

        atom.setAttributes(new ArrayList<Attribute>(0));
        atom.setDefinitions(new ArrayList<Definition>(0));
        atom.setRelationships(new ArrayList<AtomRelationship>(0));
      }

    } else if (lexicalClass == null) {
      throw new Exception("Cannot resolve a null lexical class.");

    }
  }

  /* see superclass */
  @Override
  public void resolve(StringClass stringClass) throws Exception {
    if (stringClass != null && stringClass.getId() != null) {

      // Attributes
      resolveAttributes(stringClass, false);

      // Atoms but none of the members
      for (final Atom atom : stringClass.getAtoms()) {
        // if the concept is "new", then the atom must be too
        atom.getName();
        // atom.getConceptTerminologyIds().keySet();
        // atom.getAlternateTerminologyIds().keySet();
        atom.setConceptTerminologyIds(new HashMap<>(0));
        atom.setAlternateTerminologyIds(new HashMap<>(0));

        atom.setAttributes(new ArrayList<Attribute>(0));
        atom.setDefinitions(new ArrayList<Definition>(0));
        atom.setRelationships(new ArrayList<AtomRelationship>(0));
      }

    } else if (stringClass == null) {
      throw new Exception("Cannot resolve a null string class.");
    }
  }

  /* see superclass */
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

  /* see superclass */
  @Override
  public void resolve(MapSet mapSet) throws Exception {
    if (mapSet != null) {
      boolean nullId = mapSet.getId() == null;
      mapSet.getName();
      // Attributes
      resolveAttributes(mapSet, nullId);

      // skip mappings
      mapSet.clearMappings();
      // clear ids
      mapSet.setAlternateTerminologyIds(new HashMap<>(0));

    } else if (mapSet == null) {
      throw new Exception("Cannot resolve a null mapSet.");
    }
  }

  /* see superclass */
  @Override
  public void resolve(Mapping mapping) throws Exception {
    if (mapping != null) {
      boolean nullId = mapping.getId() == null;
      // Attributes
      resolveAttributes(mapping, nullId);
      mapping.setAlternateTerminologyIds(new HashMap<>(0));

    } else if (mapping == null) {
      throw new Exception("Cannot resolve a null mapping.");
    }
  }

  /* see superclass */
  @Override
  public void resolve(
    SubsetMember<? extends ComponentHasAttributesAndName, ? extends Subset> member)
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
   * @throws Exception
   */
  @SuppressWarnings("static-method")
  protected void resolveAttributes(ComponentHasAttributes component,
    boolean nullId) throws Exception {
    component.getAttributes().size();
    for (final Attribute att : component.getAttributes()) {
      att.getName();
      // no ATUI
      att.setAlternateTerminologyIds(new HashMap<>(0));
      if (nullId) {
        att.setId(null);
      }
    }
  }

  /**
   * Resolve component history.
   *
   * @param component the component
   * @param nullId the null id
   */
  @SuppressWarnings("static-method")
  protected void resolveComponentHistory(Atom component, boolean nullId) {
    component.getComponentHistory().size();
    for (final ComponentHistory history : component.getComponentHistory()) {
      history.getReferencedTerminologyId();
      if (nullId) {
        history.setId(null);
      }
    }
  }

  /**
   * Resolve component history.
   *
   * @param component the component
   * @param nullId the null id
   */
  @SuppressWarnings("static-method")
  protected void resolveComponentHistory(Concept component, boolean nullId) {
    component.getComponentHistory().size();
    for (final ComponentHistory history : component.getComponentHistory()) {
      history.getReferencedTerminologyId();
      if (nullId) {
        history.setId(null);
      }
    }
  }

  /**
   * Resolve definition.
   *
   * @param definition the definition
   * @param nullId the null id
   * @throws Exception the exception
   */
  protected void resolveDefinition(Definition definition, boolean nullId)
    throws Exception {
    definition.getValue();
    // no ATUI
    definition.setAlternateTerminologyIds(new HashMap<>(0));
    if (nullId) {
      definition.setId(null);
    }

    resolveAttributes(definition, nullId);
  }

  /* see superclass */
  @Override
  public void resolve(Terminology terminology) {
    if (terminology != null) {
      terminology.getSynonymousNames().size();
      terminology.getRootTerminology().getTerminology();
      terminology.getRelatedTerminologies().size();
    }
  }

  /* see superclass */
  @Override
  public void resolve(RootTerminology rootTerminology) {
    rootTerminology.getSynonymousNames().size();
  }

  /* see superclass */
  @Override
  public String getName() {
    return "Default Graph Resolver";
  }

}
