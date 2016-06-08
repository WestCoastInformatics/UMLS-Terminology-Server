/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.helpers.HasTerminologyId;
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
import com.wci.umls.server.model.content.WorkflowEpoch;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.content.Subset;
import com.wci.umls.server.model.content.SubsetMember;
import com.wci.umls.server.model.content.TransitiveRelationship;
import com.wci.umls.server.model.content.TreePosition;

/**
 * Represents an algorithm for assigning identifiers.
 */
public interface IdentifierAssignmentHandler extends Configurable {

  /**
   * Returns the terminology id.
   *
   * @param concept the concept
   * @return the id
   * @throws Exception the exception
   */
  public String getTerminologyId(Concept concept) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param descriptor the descriptor
   * @return the id
   * @throws Exception the exception
   */
  public String getTerminologyId(Descriptor descriptor) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param code the code
   * @return the id
   * @throws Exception the exception
   */
  public String getTerminologyId(Code code) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param stringClass the string class
   * @return the id
   * @throws Exception the exception
   */
  public String getTerminologyId(StringClass stringClass) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param lexicalClass the lexical class
   * @return the id
   * @throws Exception the exception
   */
  public String getTerminologyId(LexicalClass lexicalClass) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param atom the atom
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(Atom atom) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param attribute the attribute
   * @param component the component
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(Attribute attribute,
    ComponentHasAttributes component) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param definition the definition
   * @param component the component
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(Definition definition,
    ComponentHasDefinitions component) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param relationship the relationship
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(
    Relationship<? extends HasTerminologyId, ? extends HasTerminologyId> relationship)
    throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param relationship the relationship
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(
    TransitiveRelationship<? extends ComponentHasAttributes> relationship)
    throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param treepos the treepos
   * @return the terminology id
   * @throws Exception the exception
   */
  public String getTerminologyId(
    TreePosition<? extends ComponentHasAttributesAndName> treepos)
    throws Exception;

  // editing of maps is not currently supported

  /**
   * Returns the terminology id.
   *
   * @param subset the subset
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(Subset subset) throws Exception;
  
  /**
   * Gets the terminology id.
   *
   * @param mapping the mapping
   * @return the terminology id
   * @throws Exception the exception
   */
  public String getTerminologyId(WorkflowEpoch mapping) throws Exception;
  
  /**
   * Gets the terminology id.
   *
   * @param mapSet the map set
   * @return the terminology id
   * @throws Exception the exception
   */
  public String getTerminologyId(MapSet mapSet) throws Exception;

  /**
   * Returns the terminology id.
   *
   * @param subsetMember the subset member
   * @return the string
   * @throws Exception the exception
   */
  public String getTerminologyId(
    SubsetMember<? extends ComponentHasAttributes, ? extends Subset> subsetMember)
    throws Exception;

  /**
   * Gets the terminology id.
   *
   * @param semanticTypeComponent the semantic type component
   * @param concept the concept
   * @return the terminology id
   * @throws Exception the exception
   */
  public String getTerminologyId(SemanticTypeComponent semanticTypeComponent,
    Concept concept) throws Exception;

  /**
   * Indicates whether this algorithm allows identifiers to change on an update.
   * That is a computation of the id before and after should produce the same
   * identifier. For example UUID-hash based ID assignment should not change -
   * otherwise it means identity fields are changing - which means that this is
   * actually a different object.
   *
   * @return true, if successful
   */
  public boolean allowIdChangeOnUpdate();

  /**
   * Indicates whether this algorithm allows identifiers to change on an update
   * of a concept. Concept identifier assignment can be tricky and in some cases
   * the identifier should be allowed to change. In particular if hashing-based
   * IDs are used the concept id assignment must be based on its contents.
   *
   * @return true, if successful
   */
  public boolean allowConceptIdChangeOnUpdate();

}
