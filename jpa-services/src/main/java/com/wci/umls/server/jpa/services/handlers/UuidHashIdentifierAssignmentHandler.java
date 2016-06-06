/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.jpa.services.helper.TerminologyUtility;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHasAttributesAndName;
import com.wci.umls.server.model.content.ComponentHasDefinitions;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Default implementation of {@link IdentifierAssignmentHandler}. This supports
 * "application-managed" identifier assignment.
 * 
 * If a component already has an SCTID, it keeps it.
 */
public class UuidHashIdentifierAssignmentHandler implements
    IdentifierAssignmentHandler {

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    // Based on the concept name and the terminology ids
    // of the active stated isa relationships
    StringBuilder hashKey = new StringBuilder().append(concept.getName());
    for (Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> rel : concept
        .getRelationships()) {
      if (rel.isHierarchical() && !rel.isObsolete() && !rel.isSuppressible()
          && rel.isStated()) {
        hashKey.append(rel.getTerminologyId());
      }
    }
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Descriptor descriptor) throws Exception {
    // Based on the descriptor name and the terminology ids
    // of the active stated isa relationships
    StringBuilder hashKey = new StringBuilder().append(descriptor.getName());
    for (Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> rel : descriptor
        .getRelationships()) {
      if (rel.isHierarchical() && !rel.isObsolete() && !rel.isSuppressible()
          && rel.isStated()) {
        hashKey.append(rel.getTerminologyId());
      }
    }
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Code code) throws Exception {
    // Based on the code name and the terminology ids
    // of the active stated isa relationships
    StringBuilder hashKey = new StringBuilder().append(code.getName());
    for (Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> rel : code
        .getRelationships()) {
      if (rel.isHierarchical() && !rel.isObsolete() && !rel.isSuppressible()
          && rel.isStated()) {
        hashKey.append(rel.getTerminologyId());
      }
    }
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(StringClass stringClass) throws Exception {
    return TerminologyUtility.getUuid(stringClass.getName().toString())
        .toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {
    return TerminologyUtility.getUuid(
        lexicalClass.getNormalizedName().toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Atom atom) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, sui, codeId, descriptorId, conceptId,
    // termType
    hashKey.append(atom.getTerminology()).append(atom.getTerminologyId())
        .append(atom.getStringClassId()).append(atom.getConceptId())
        .append(atom.getDescriptorId()).append(atom.getCodeId())
        .append(atom.getTermType());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Attribute attribute,
    ComponentHasAttributes component) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, name, value, component.terminologyId
    hashKey.append(attribute.getTerminology())
        .append(attribute.getTerminologyId()).append(attribute.getName())
        .append(attribute.getValue()).append(component.getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, name, value, component.terminologyId
    hashKey.append(definition.getTerminology())
        .append(definition.getTerminologyId()).append(definition.getValue())
        .append(component.getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> relationship)
    throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, relType, additionalRelType, group,
    // component.terminologyId
    hashKey.append(relationship.getTerminology())
        .append(relationship.getTerminologyId())
        .append(relationship.getRelationshipType())
        .append(relationship.getAdditionalRelationshipType())
        .append(relationship.getGroup())
        .append(relationship.getFrom().getTerminologyId())
        .append(relationship.getTo().getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, superType, subType
    hashKey.append(relationship.getTerminology())
        .append(relationship.getTerminologyId())
        .append(relationship.getSuperType().getTerminologyId())
        .append(relationship.getSubType().getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Subset subset) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, name
    hashKey.append(subset.getTerminology()).append(subset.getTerminologyId())
        .append(subset.getName());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> member)
    throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, member, subset
    hashKey.append(member.getTerminology()).append(member.getTerminologyId())
        .append(member.getMember().getTerminologyId())
        .append(member.getSubset().getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent,
    Concept concept) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // value, concept
    hashKey.append(semanticTypeComponent.getSemanticType()).append(
        concept.getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, superType, subType
    hashKey.append(treepos.getTerminology()).append(treepos.getTerminologyId())
        .append(treepos.getAncestorPath())
        .append(treepos.getNode().getTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  /* see superclass */
  @Override
  public boolean allowIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    return true;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "UUID Hash Identifier Assignment Handler";
  }

  @Override
  public String getTerminologyId(Mapping mapping) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, fromTerminologyId, toTerminologyId, name
    hashKey.append(mapping.getFromTerminologyId())
        .append(mapping.getTerminologyId())
        .append(mapping.getToTerminologyId());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }

  @Override
  public String getTerminologyId(MapSet mapSet) throws Exception {
    StringBuilder hashKey = new StringBuilder();
    // terminologyId, terminology, name
    hashKey.append(mapSet.getTerminology()).append(mapSet.getTerminologyId())
        .append(mapSet.getName());
    return TerminologyUtility.getUuid(hashKey.toString()).toString();
  }
}
