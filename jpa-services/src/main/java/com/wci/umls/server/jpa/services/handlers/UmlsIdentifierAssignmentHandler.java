/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.jpa.meta.AtomIdentityJpa;
import com.wci.umls.server.jpa.meta.AttributeIdentityJpa;
import com.wci.umls.server.jpa.meta.LexicalClassIdentityJpa;
import com.wci.umls.server.jpa.meta.RelationshipIdentityJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeComponentIdentityJpa;
import com.wci.umls.server.jpa.meta.StringClassIdentityJpa;
import com.wci.umls.server.jpa.services.UmlsIdentityServiceJpa;
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
import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.RelationshipIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringClassIdentity;
import com.wci.umls.server.services.UmlsIdentityService;
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Default implementation of {@link IdentifierAssignmentHandler}. This supports
 * "application-managed" identifier assignment.
 */
public class UmlsIdentifierAssignmentHandler
    implements IdentifierAssignmentHandler {

  /** The ui prefixes. */
  private Map<String, String> prefixMap = new HashMap<>();

  /** The ui lengths. */
  private Map<String, Integer> lengthMap = new HashMap<>();

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {

    if (p != null) {
      if (p.containsKey("aui.length")) {
        lengthMap.put("AUI", Integer.valueOf(p.getProperty("aui.length")));
      }
      if (p.containsKey("aui.prefix")) {
        prefixMap.put("AUI", p.getProperty("aui.prefix"));
      }
      if (p.containsKey("lui.length")) {
        lengthMap.put("LUI", Integer.valueOf(p.getProperty("lui.length")));
      }
      if (p.containsKey("lui.prefix")) {
        prefixMap.put("LUI", p.getProperty("lui.prefix"));
      }
      if (p.containsKey("atui.length")) {
        lengthMap.put("ATUI", Integer.valueOf(p.getProperty("atui.length")));
      }
      if (p.containsKey("atui.prefix")) {
        prefixMap.put("ATUI", p.getProperty("atui.prefix"));
      }
      if (p.containsKey("sui.length")) {
        lengthMap.put("SUI", Integer.valueOf(p.getProperty("sui.length")));
      }
      if (p.containsKey("sui.prefix")) {
        prefixMap.put("SUI", p.getProperty("sui.prefix"));
      }
      if (p.containsKey("rui.length")) {
        lengthMap.put("RUI", Integer.valueOf(p.getProperty("rui.length")));
      }
      if (p.containsKey("rui.prefix")) {
        prefixMap.put("RUI", p.getProperty("rui.prefix"));
      }
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    // TODO
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Descriptor descriptor) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Code code) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(StringClass stringClass) throws Exception {

    if (!stringClass.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create StringClassIdentity and populate from the stringClass.
      final StringClassIdentity identity = new StringClassIdentityJpa();
      identity.setName(stringClass.getName());
      identity.setLanguage(stringClass.getLanguage());

      final StringClassIdentity identity2 = service.getStringClassIdentity(identity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "SUI");
      }
      // else generate a new one and add it
      else {
        // Block between getting next id and saving the id value
        synchronized (this) {
          // Get next id
          final Long nextId = service.getNextStringClassId();
          // Add new identity object
          identity.setId(nextId);
          service.addStringClassIdentity(identity);
          return convertId(nextId, "SUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }

  }

  /* see superclass */
  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {

    if (!lexicalClass.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create LexicalClassIdentity and populate from the lexicalClass.
      final LexicalClassIdentity identity = new LexicalClassIdentityJpa();
      identity.setNormalizedName(lexicalClass.getNormalizedName());

      final LexicalClassIdentity identity2 =
          service.getLexicalClassIdentity(identity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "LUI");
      }
      // else generate a new one and add it
      else {
        // Block between getting next id and saving the id value
        synchronized (this) {
          // Get next id
          final Long nextId = service.getNextLexicalClassId();
          // Add new identity object
          identity.setId(nextId);
          service.addLexicalClassIdentity(identity);
          return convertId(nextId, "LUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Atom atom) throws Exception {

    if (!atom.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create AtomIdentity and populate from the atom.
      final AtomIdentity identity = new AtomIdentityJpa();
      identity.setCodeId(atom.getCodeId());
      identity.setConceptId(atom.getConceptId());
      identity.setDescriptorId(atom.getDescriptorId());
      identity.setStringClassId(atom.getStringClassId());
      identity.setTerminology(atom.getTerminology());
      identity.setTerminologyId(atom.getTerminologyId());
      identity.setTermType(atom.getTermType());

      final AtomIdentity identity2 =
          service.getAtomIdentity(identity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "AUI");
      }
      // else generate a new one and add it
      else {
        // Block between getting next id and saving the id value
        synchronized (this) {
          // Get next id
          final Long nextId = service.getNextAtomId();
          // Add new identity object
          identity.setId(nextId);
          service.addAtomIdentity(identity);
          return convertId(nextId, "AUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Attribute attribute, ComponentInfo component)
    throws Exception {

    if (!attribute.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create AttributeIdentity and populate from the attribute.
      final AttributeIdentity identity = new AttributeIdentityJpa();
      identity.setHashCode(ConfigUtility.getMd5(attribute.getValue()));
      identity.setName(attribute.getName());
      identity.setComponentId(component.getTerminologyId());
      identity.setComponentTerminology(component.getTerminology());
      identity.setComponentType(component.getType());
      identity.setTerminology(attribute.getTerminology());
      identity.setTerminologyId(attribute.getTerminologyId());

      final AttributeIdentity identity2 =
          service.getAttributeIdentity(identity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "ATUI");
      }
      // else generate a new one and add it
      else {
        // Block between getting next id and saving the id value
        synchronized (this) {
          // Get next id
          final Long nextId = service.getNextAttributeId();
          // Add new identity object
          identity.setId(nextId);
          service.addAttributeIdentity(identity);
          return convertId(nextId, "ATUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    // TODO:
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception {

    if (!relationship.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create RelationshipIdentity and populate from the relationship.
      final RelationshipIdentity identity = new RelationshipIdentityJpa();
      identity.setId(relationship.getId());
      identity.setTerminology(relationship.getTerminology());
      identity.setTerminologyId(relationship.getTerminologyId());
      identity.setRelationshipType(relationship.getRelationshipType());
      identity.setAdditionalRelationshipType(relationship.getAdditionalRelationshipType());
      identity.setFromId(relationship.getFrom().getTerminologyId());
      identity.setFromTerminology(relationship.getFrom().getTerminology());
      identity.setFromType(relationship.getFrom().getType());
      identity.setToId(relationship.getTo().getTerminologyId());
      identity.setToTerminology(relationship.getTo().getTerminology());
      identity.setToType(relationship.getTo().getType());

      final RelationshipIdentity identity2 =
          service.getRelationshipIdentity(identity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "RUI");
      }
      // else generate a new one and add it
      else {
        
        final RelationshipIdentity inverseIdentity = service.createInverseRelationshipIdentity(identity);
         // Block between getting next id and saving the id value
        synchronized (this) {
          // Get next id and inverse ID
          final Long nextId = service.getNextRelationshipId();
          //TODO confirm this gives different number.  If not, add 1 to nextId;
          final Long nextIdInverse = service.getNextRelationshipId();
          
          //Set ID and inverse IDs for both relationship and its inverse
          identity.setId(nextId);
          identity.setInverseId(nextIdInverse);

          inverseIdentity.setId(nextIdInverse);
          inverseIdentity.setInverseId(nextId);

          // Add new identity objects
          service.addRelationshipIdentity(inverseIdentity);
          service.addRelationshipIdentity(identity);
          
          // return ID for called relationship (inverse can get called later)
          return convertId(nextId, "RUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }
  
  /* see superclass */
  @Override
  public String getInverseTerminologyId(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship)
    throws Exception {

    if (!relationship.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create RelationshipIdentity and populate from the relationship.
      final RelationshipIdentity identity = new RelationshipIdentityJpa();
      identity.setId(relationship.getId());
      identity.setTerminology(relationship.getTerminology());
      identity.setTerminologyId(relationship.getTerminologyId());
      identity.setRelationshipType(relationship.getRelationshipType());
      identity.setAdditionalRelationshipType(relationship.getAdditionalRelationshipType());
      identity.setFromId(relationship.getFrom().getTerminologyId());
      identity.setFromTerminology(relationship.getFrom().getTerminology());
      identity.setFromType(relationship.getFrom().getType());
      identity.setToId(relationship.getTo().getTerminologyId());
      identity.setToTerminology(relationship.getTo().getTerminology());
      identity.setToType(relationship.getTo().getType());

      final RelationshipIdentity inverseIdentity = service.createInverseRelationshipIdentity(identity);
      
      final RelationshipIdentity identity2 =
          service.getRelationshipIdentity(inverseIdentity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "RUI");
      }
      // else generate a new one and add it
      else {
        throw new Exception ("Unexpected missing inverse of relationship " + relationship);
      }
    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }  

  /* see superclass */
  @Override
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Subset subset) throws Exception {
    throw new UnsupportedOperationException();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> member)
    throws Exception {
    // TODO
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent,
    Concept concept) throws Exception {

    // TODO (? - the below may not be correct)

    if (!semanticTypeComponent.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create semanticTypeIdentity and populate from the semanticType.
      final SemanticTypeComponentIdentity identity =
          new SemanticTypeComponentIdentityJpa();
      identity.setConceptTerminologyId(concept.getTerminologyId());
      identity.setSemanticType(semanticTypeComponent.getSemanticType());
      identity.setTerminology(semanticTypeComponent.getTerminology());

      final SemanticTypeComponentIdentity identity2 =
          service.getSemanticTypeComponentIdentity(identity);

      // Reuse existing id
      if (identity2 != null) {
        return convertId(identity2.getId(), "ATUI");
      }
      // else generate a new one and add it
      else {
        // Block between getting next id and saving the id value
        synchronized (this) {
          // Get next id
          final Long nextId = service.getNextSemanticTypeComponentId();
          // Add new identity object
          identity.setId(nextId);
          service.addSemanticTypeComponentIdentity(identity);
          return convertId(nextId, "ATUI");
        }
      }

    } catch (Exception e) {
      throw e;
    } finally {
      service.close();
    }
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Mapping mapping) throws Exception {
    // TODO
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(MapSet mapSet) throws Exception {
    // TODO
    return "";
  }

  /* see superclass */
  @Override
  public boolean allowIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    return false;
  }

  /* see superclass */
  @Override
  public String getName() {
    return "UMLS Id Assignment Algorithm";
  }

  /**
   * Convert id.
   *
   * @param id the id
   * @param type the type
   * @return the string
   * @throws Exception the exception
   */
  private String convertId(Long id, String type) throws Exception {
    if (!prefixMap.containsKey(type) && !lengthMap.containsKey(type)) {
      throw new Exception("Identifier type " + type + " is not configured");
    }
    final int length = lengthMap.get(type);
    final String idStr = id.toString();
    final int startIndex = idStr.length() + 19 - length;
    return prefixMap.get(type)
        + ("00000000000000000000" + idStr).substring(startIndex);
  }
}
