/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.services.handlers;

import java.util.List;
import java.util.Set;

import com.wci.umls.server.helpers.Configurable;
import com.wci.umls.server.model.content.Atom;
import com.wci.umls.server.model.content.Code;
import com.wci.umls.server.model.content.ComponentHasAttributes;
import com.wci.umls.server.model.content.Concept;
import com.wci.umls.server.model.content.Descriptor;
import com.wci.umls.server.model.content.LexicalClass;
import com.wci.umls.server.model.content.Relationship;
import com.wci.umls.server.model.content.SemanticTypeComponent;
import com.wci.umls.server.model.content.StringClass;
import com.wci.umls.server.model.meta.RelationshipType;

/**
 * Generically represents an algorithm for reading objects to a certain depth
 * before sending them across the wire. It also handles wiring objects together
 * that have been sent in from across the wire. Thus the "depth" of the graph is
 * controlled by the implementation of this algortihm
 */
public interface GraphResolutionHandler extends Configurable {

  /**
   * Resolve concepts.
   *
   * @param concept the concept
   * @param isaRelTypeIds the isa rel type ids
   * @throws Exception the exception
   */
  public void resolve(Concept concept, List<RelationshipType> isaRelTypeIds)
    throws Exception;

  /**
   * Resolve a concept to simply the concept element and none of the graph,
   * ready for JAXB serialization.
   *
   * @param concept the concept
   * @throws Exception the exception
   */
  public void resolveEmpty(Concept concept) throws Exception;

  /**
   * Resolve.
   *
   * @param descriptor the descriptor
   * @param isaRelTypeIds the isa rel type ids
   * @throws Exception the exception
   */
  public void resolve(Descriptor descriptor, Set<String> isaRelTypeIds)
    throws Exception;

  /**
   * Resolve.
   *
   * @param descriptor the descriptor
   * @param isaRelTypeIds the isa rel type ids
   * @throws Exception the exception
   */
  public void resolve(Code descriptor, Set<String> isaRelTypeIds)
    throws Exception;

  /**
   * Resolve.
   *
   * @param lexicalClass the lexical class
   * @throws Exception the exception
   */
  public void resolve(LexicalClass lexicalClass) throws Exception;

  /**
   * Resolve.
   *
   * @param stringClass the string class
   * @throws Exception the exception
   */
  public void resolve(StringClass stringClass) throws Exception;

  /**
   * Resolve atoms.
   *
   * @param atom the atom
   * @throws Exception the exception
   */
  public void resolve(Atom atom) throws Exception;

  /**
   * Resolve relationships.
   *
   * @param relationship the relationship
   * @throws Exception the exception
   */
  public void resolve(
    Relationship<? extends ComponentHasAttributes, ? extends ComponentHasAttributes> relationship)
    throws Exception;

  /**
   * Resolve.
   *
   * @param sty the sty
   * @throws Exception the exception
   */
  public void resolve(SemanticTypeComponent sty) throws Exception;

  // at some point, other data structures may be useful here
}
