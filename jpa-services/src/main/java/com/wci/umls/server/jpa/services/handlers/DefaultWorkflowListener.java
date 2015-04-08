package com.wci.umls.server.jpa.services.handlers;

import java.util.Properties;

import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.Component;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Definition;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.services.handlers.WorkflowListener;

/**
 * A sample validation check for a new concept meeting the minimum qualifying
 * criteria.
 */
public class DefaultWorkflowListener implements WorkflowListener {

  @Override
  public void setProperties(Properties p) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void beginTransaction() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void preCommit() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void postCommit() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void classificationStarted() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void classificationFinished() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void preClassificationStarted() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void preClassificationFinished() throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void conceptChanged(Concept concept, Action action) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void descriptorChanged(Descriptor descriptor, Action action)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void codeChanged(Code code, Action action) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void stringClassChanged(StringClass stringClass, Action action)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void lexicalClassChanged(LexicalClass lexicalClass, Action action)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void atomChanged(Atom atom, Action action) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void attributeChanged(Attribute attribute, Action action)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void definitionChanged(Definition definition, Action action)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void relationshipChanged(
    Relationship<? extends Component, ? extends Component> relationship,
    Action action) throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void semanticTypeChanged(SemanticTypeComponent sty, Action action)
    throws Exception {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void subsetChanged(Subset subset, Action action) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void subsetMemberChanged(SubsetMember subsetMember, Action action) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void cancel() {
    // TODO Auto-generated method stub
    
  }

}
