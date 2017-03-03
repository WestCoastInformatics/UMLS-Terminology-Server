/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

import com.wci.umls.server.helpers.ComponentInfo;
import com.wci.umls.server.jpa.AbstractConfigurable;
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
 */
public class DefaultIdentifierAssignmentHandler extends AbstractConfigurable
    implements IdentifierAssignmentHandler {

  /* see superclass */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    return concept.getTerminologyId() == null ? "" : concept.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Descriptor descriptor) throws Exception {
    return descriptor.getTerminologyId() == null ? ""
        : descriptor.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Code code) throws Exception {
    return code.getTerminologyId() == null ? "" : code.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(StringClass stringClass) throws Exception {
    return stringClass.getTerminologyId() == null ? ""
        : stringClass.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {
    return lexicalClass.getTerminologyId() == null ? ""
        : lexicalClass.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Atom atom) throws Exception {
    return atom.getTerminologyId() == null ? "" : atom.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Attribute attribute, ComponentInfo component)
    throws Exception {
    return attribute.getTerminologyId() == null ? ""
        : attribute.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    return definition.getTerminologyId() == null ? ""
        : definition.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship,
    String inverseRelType, String inverseAdditionalRelType) throws Exception {
    return relationship.getTerminologyId() == null ? ""
        : relationship.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getInverseTerminologyId(
    Relationship<? extends ComponentInfo, ? extends ComponentInfo> relationship,
    String inverseRelType, String inverseAdditionalRelType) throws Exception {
    return relationship.getTerminologyId() == null ? ""
        : relationship.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception {
    return relationship.getTerminologyId() == null ? ""
        : relationship.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    return treepos.getTerminologyId() == null ? "" : treepos.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Subset subset) throws Exception {
    return subset.getTerminologyId() == null ? "" : subset.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> member)
    throws Exception {
    return member.getTerminologyId() == null ? "" : member.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent,
    Concept concept) throws Exception {
    return semanticTypeComponent.getTerminologyId() == null ? ""
        : semanticTypeComponent.getTerminologyId();
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
    return "Default Id Assignment Algorithm";
  }

  /* see superclass */
  @Override
  public String getTerminologyId(Mapping mapping) throws Exception {
    return mapping.getTerminologyId();
  }

  /* see superclass */
  @Override
  public String getTerminologyId(MapSet mapSet) throws Exception {
    return mapSet.getTerminologyId();
  }

  /* see superclass */
  @Override
  public boolean getTransactionPerOperation() throws Exception {
    // N/A
    return false;
  }

  /* see superclass */
  @Override
  public void setTransactionPerOperation(boolean transactionPerOperation)
    throws Exception {
    // N/A

  }

  /* see superclass */
  @Override
  public void commit() throws Exception {
    // N/A

  }

  /* see superclass */
  @Override
  public void rollback() throws Exception {
    // N/A

  }

  /* see superclass */
  @Override
  public void beginTransaction() throws Exception {
    // N/A
  }

  /* see superclass */
  @Override
  public void close() throws Exception {
    // N/A
  }

  /* see superclass */
  @Override
  public void clear() throws Exception {
    // N/A
  }

  /* see superclass */
  @Override
  public void commitClearBegin() throws Exception {
    // N/A

  }

  /* see superclass */
  @Override
  public void logAndCommit(int objectCt, int logCt, int commitCt)
    throws Exception {
    // N/A

  }

  /* see superclass */
  @Override
  public void silentIntervalCommit(int objectCt, int logCt, int commitCt)
    throws Exception {
    // N/A

  }

}
