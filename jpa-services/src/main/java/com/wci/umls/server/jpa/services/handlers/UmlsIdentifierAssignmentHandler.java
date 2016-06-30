/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.helpers.ConfigUtility;
import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.jpa.meta.AttributeIdentityJpa;
import com.wci.umls.server.jpa.meta.SemanticTypeComponentIdentityJpa;
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
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
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
    // TODO:
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {
    // TODO:
    return "";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Atom atom) throws Exception {
    // TODO:
    return "";
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
    Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> relationship)
    throws Exception {
    // TODO
    return "";
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

    //TODO (? - the below may not be correct)
    
    if (!semanticTypeComponent.isPublishable()) {
      return "";
    }

    final UmlsIdentityService service = new UmlsIdentityServiceJpa();
    try {
      // Create semanticTypeIdentity and populate from the semanticType.
      final SemanticTypeComponentIdentity identity = new SemanticTypeComponentIdentityJpa();
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
