/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
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
import com.wci.umls.server.services.handlers.IdentifierAssignmentHandler;

/**
 * Default implementation of {@link IdentifierAssignmentHandler}. This supports
 * "application-managed" identifier assignment.
 * 
 * If a component already has an SCTID, it keeps it.
 */
public class UuidHashIdentifierAssignmentHandler implements
    IdentifierAssignmentHandler {

  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public String getTerminologyId(Concept concept) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Descriptor descriptor) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Code code) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(StringClass stringClass) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Atom atom) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Attribute attribute) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Definition definition) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(Subset subset) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes> subsetMember) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean allowIdChangeOnUpdate() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean allowConceptIdChangeOnUpdate() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent)
    throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

}
