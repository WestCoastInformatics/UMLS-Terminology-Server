/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

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
public class DefaultIdentifierAssignmentHandler implements
    IdentifierAssignmentHandler {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.wci.umls.server.helpers.Configurable#setProperties(java.util.Properties
   * )
   */
  @Override
  public void setProperties(Properties p) throws Exception {
    // n/a
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.Concept)
   */
  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    return concept.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.Descriptor)
   */
  @Override
  public String getTerminologyId(Descriptor descriptor) throws Exception {
    return descriptor.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.Code)
   */
  @Override
  public String getTerminologyId(Code code) throws Exception {
    return code.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.StringClass)
   */
  @Override
  public String getTerminologyId(StringClass stringClass) throws Exception {
    return stringClass.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.LexicalClass)
   */
  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {
    return lexicalClass.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.Atom)
   */
  @Override
  public String getTerminologyId(Atom atom) throws Exception {
    return atom.getTerminologyId();
  }

  @Override
  public String getTerminologyId(Attribute attribute,
    ComponentHasAttributes component) throws Exception {
    return attribute.getTerminologyId();
  }

  @Override
  public String getTerminologyId(Definition definition,
    ComponentHasDefinitions component) throws Exception {
    return definition.getTerminologyId();
  }

  @Override
  public String getTerminologyId(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception {
    return relationship.getTerminologyId();
  }

  @Override
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception {
    return relationship.getTerminologyId();
  }

  @Override
  public String getTerminologyId(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception {
    return treepos.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.Subset)
   */
  @Override
  public String getTerminologyId(Subset subset) throws Exception {
    return subset.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * getTerminologyId(com.wci.umls.server.model.content.SubsetMember)
   */
  @Override
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> member)
    throws Exception {
    return member.getTerminologyId();
  }

  @Override
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent,
    Concept concept) throws Exception {
    return semanticTypeComponent.getTerminologyId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * allowIdChangeOnUpdate()
   */
  @Override
  public boolean allowIdChangeOnUpdate() {
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wci.umls.server.services.handlers.IdentifierAssignmentHandler#
   * allowConceptIdChangeOnUpdate()
   */
  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    return false;
  }

}
