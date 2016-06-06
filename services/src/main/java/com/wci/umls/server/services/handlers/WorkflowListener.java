/*
 *    Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.HasTerminologyId;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Attribute;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.ComponentHistory;
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

/**
 * Generically represents a validation check on a concept.
 */
public interface WorkflowListener extends Configurable {

  /**
   * Represents change actions on components.
   */
  public enum Action {

    /** The add. */
    ADD,
    /** The remove. */
    REMOVE,
    /** The update. */
    UPDATE
  }

  /**
   * Notification of transaction starting.
   *
   * @throws Exception the exception
   */
  public void beginTransaction() throws Exception;

  /**
   * Notification pre-commit.
   *
   * @throws Exception the exception
   */
  public void preCommit() throws Exception;

  /**
   * Notification post-commit.
   *
   * @throws Exception the exception
   */
  public void postCommit() throws Exception;

  /**
   * Classification started.
   *
   * @throws Exception the exception
   */
  public void classificationStarted() throws Exception;

  /**
   * Classification finished.
   *
   * @throws Exception the exception
   */
  public void classificationFinished() throws Exception;

  /**
   * Pre classification started.
   *
   * @throws Exception the exception
   */
  public void preClassificationStarted() throws Exception;

  /**
   * Pre classification finished.
   *
   * @throws Exception the exception
   */
  public void preClassificationFinished() throws Exception;

  /**
   * Notification of concept added.
   *
   * @param concept the concept
   * @param action the action
   * @throws Exception the exception
   */
  public void conceptChanged(Concept concept, Action action) throws Exception;


  /**
   * Component history changed.
   *
   * @param componentHistory the component history
   * @param action the action
   * @throws Exception the exception
   */
  public void componentHistoryChanged(ComponentHistory componentHistory, Action action) throws Exception;

  /**
   * Descriptor of atom changed.
   *
   * @param descriptor the descriptor
   * @param action the action
   * @throws Exception the exception
   */
  public void descriptorChanged(Descriptor descriptor, Action action)
    throws Exception;

  /**
   * Code changed.
   *
   * @param code the code
   * @param action the action
   * @throws Exception the exception
   */
  public void codeChanged(Code code, Action action) throws Exception;

  /**
   * String class changed.
   *
   * @param stringClass the string class
   * @param action the action
   * @throws Exception the exception
   */
  public void stringClassChanged(StringClass stringClass, Action action)
    throws Exception;

  /**
   * Lexical class changed.
   *
   * @param lexicalClass the lexical class
   * @param action the action
   * @throws Exception the exception
   */
  public void lexicalClassChanged(LexicalClass lexicalClass, Action action)
    throws Exception;

  /**
   * Atom changed.
   *
   * @param atom the atom
   * @param action the action
   * @throws Exception the exception
   */
  public void atomChanged(Atom atom, Action action) throws Exception;

  /**
   * Attribute changed.
   *
   * @param attribute the attribute
   * @param action the action
   * @throws Exception the exception
   */
  public void attributeChanged(Attribute attribute, Action action)
    throws Exception;

  /**
   * Definition changed.
   *
   * @param definition the definition
   * @param action the action
   * @throws Exception the exception
   */
  public void definitionChanged(Definition definition, Action action)
    throws Exception;

  /**
   * Relationship changed.
   *
   * @param relationship the relationship
   * @param action the action
   * @throws Exception the exception
   */
  public void relationshipChanged(
    Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> relationship,
    Action action) throws Exception;

  /**
   * Semantic type changed.
   *
   * @param sty the sty
   * @param action the action
   * @throws Exception the exception
   */
  public void semanticTypeChanged(SemanticTypeComponent sty, Action action)
    throws Exception;

  /**
   * Subset changed.
   *
   * @param subset the subset
   * @param action the action
   */
  public void subsetChanged(Subset subset, Action action);
  
  /**
   * Mapping changed.
   *
   * @param mapping the mapping
   * @param action the action
   */
  public void mappingChanged(Mapping mapping, Action action);
  
  /**
   * Map set changed.
   *
   * @param mapSet the map set
   * @param action the action
   */
  public void mapSetChanged(MapSet mapSet, Action action);

  /**
   * Subset member changed.
   *
   * @param subsetMember the subset member
   * @param action the action
   */
  public void subsetMemberChanged(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> subsetMember,
    Action action);

  /**
   * Metadata changed.
   */
  public void metadataChanged();

  /**
   * Notification of a cancelled operation.
   */
  public void cancel();
}
