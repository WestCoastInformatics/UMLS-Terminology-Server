/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import com.wci.umls.server.model.meta.AtomIdentity;
import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.LexicalClassIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;
import com.wci.umls.server.model.meta.StringClassIdentity;

/**
 * Represents a service used by Metathesaurus editing for assigning identifiers.
 */
public interface UmlsIdentityService extends RootService {

  /**
   * Gets the attribute identity.
   *
   * @param id the id
   * @return the attribute identity
   * @throws Exception the exception
   */

  public AttributeIdentity getAttributeIdentity(Long id) throws Exception;

  /**
   * Returns the next attribute id.
   *
   * @return the next attribute id
   * @throws Exception the exception
   */
  public Long getNextAttributeId() throws Exception;

  /**
   * Gets the attribute identity, returning an object with an assigned id.
   *
   * @param identity the identity
   * @return true, if successful
   * @throws Exception the exception
   */
  public AttributeIdentity getAttributeIdentity(AttributeIdentity identity)
    throws Exception;

  /**
   * Add attribute identity.
   *
   * @param attributeIdentity the attribute identity
   * @return the attribute identity
   * @throws Exception the exception
   */

  public AttributeIdentity addAttributeIdentity(
    AttributeIdentity attributeIdentity) throws Exception;

  /**
   * Update attribute identity.
   *
   * @param attributeIdentity the attribute identity
   * @throws Exception the exception
   */

  public void updateAttributeIdentity(AttributeIdentity attributeIdentity)
    throws Exception;

  /**
   * Remove attribute identity.
   *
   * @param attributeIdentityId the attribute identity id
   * @throws Exception the exception
   */

  public void removeAttributeIdentity(Long attributeIdentityId)
    throws Exception;

  /**
   * Returns the semantic type component identity.
   *
   * @param id the id
   * @return the semantic type component identity
   * @throws Exception the exception
   */

  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(Long id)
    throws Exception;

  /**
   * Returns the next semantict type component id.
   *
   * @return the next semantic type component id
   * @throws Exception the exception
   */
  public Long getNextSemanticTypeComponentId() throws Exception;

  /**
   * Returns the semantic type component identity.
   *
   * @param identity the identity
   * @return the semantic type component identity
   * @throws Exception the exception
   */
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity identity) throws Exception;

  /**
   * Add semantic type component identity.
   *
   * @param semanticTypeComponentIdentity the semantic type component identity
   * @return the semantic type component identity
   * @throws Exception the exception
   */

  public SemanticTypeComponentIdentity addSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity semanticTypeComponentIdentity)
    throws Exception;

  /**
   * Update semantic type component identity.
   *
   * @param semanticTypeComponentIdentity the semantic type component identity
   * @throws Exception the exception
   */

  public void updateSemanticTypeComponentIdentity(
    SemanticTypeComponentIdentity semanticTypeComponentIdentity)
    throws Exception;

  /**
   * Remove semantic type component identity.
   *
   * @param semanticTypeComponentIdentityId the semantic type component identity
   *          id
   * @throws Exception the exception
   */

  public void removeSemanticTypeComponentIdentity(
    Long semanticTypeComponentIdentityId) throws Exception;

  /**
   * Returns the atom identity.
   *
   * @param id the id
   * @return the atom identity
   * @throws Exception the exception
   */
  public AtomIdentity getAtomIdentity(Long id) throws Exception;

  /**
   * Returns the next atom id.
   *
   * @return the next atom id
   * @throws Exception the exception
   */
  public Long getNextAtomId() throws Exception;

  /**
   * Returns the atom identity.
   *
   * @param identity the identity
   * @return the atom identity
   * @throws Exception the exception
   */
  public AtomIdentity getAtomIdentity(AtomIdentity identity) throws Exception;

  /**
   * Adds the atom identity.
   *
   * @param atomIdentity the atom identity
   * @return the atom identity
   * @throws Exception the exception
   */
  public AtomIdentity addAtomIdentity(AtomIdentity atomIdentity)
    throws Exception;

  /**
   * Update atom identity.
   *
   * @param atomIdentity the atom identity
   * @throws Exception the exception
   */
  public void updateAtomIdentity(AtomIdentity atomIdentity) throws Exception;

  /**
   * Removes the atom identity.
   *
   * @param atomIdentityId the atom identity id
   * @throws Exception the exception
   */
  public void removeAtomIdentity(Long atomIdentityId) throws Exception;

  /**
   * Returns the string identity.
   *
   * @param id the id
   * @return the string identity
   * @throws Exception the exception
   */
  public StringClassIdentity getStringClassIdentity(Long id) throws Exception;

  /**
   * Returns the next string id.
   *
   * @return the next string id
   * @throws Exception the exception
   */
  public Long getNextStringClassId() throws Exception;

  /**
   * Returns the string identity.
   *
   * @param identity the identity
   * @return the string identity
   * @throws Exception the exception
   */
  public StringClassIdentity getStringClassIdentity(StringClassIdentity identity)
    throws Exception;

  /**
   * Adds the string identity.
   *
   * @param stringIdentity the string identity
   * @return the string identity
   * @throws Exception the exception
   */
  public StringClassIdentity addStringClassIdentity(StringClassIdentity stringIdentity)
    throws Exception;

  /**
   * Update string identity.
   *
   * @param stringIdentity the string identity
   * @throws Exception the exception
   */
  public void updateStringClassIdentity(StringClassIdentity stringIdentity)
    throws Exception;

  /**
   * Removes the string identity.
   *
   * @param stringIdentityId the string identity id
   * @throws Exception the exception
   */
  public void removeStringClassIdentity(Long stringIdentityId) throws Exception;

  /**
   * Returns the lexical class identity.
   *
   * @param id the id
   * @return the lexical class identity
   * @throws Exception the exception
   */
  public LexicalClassIdentity getLexicalClassIdentity(Long id) throws Exception;

  /**
   * Returns the next lexical class id.
   *
   * @return the next lexical class id
   * @throws Exception the exception
   */
  public Long getNextLexicalClassId() throws Exception;

  /**
   * Returns the lexical class identity.
   *
   * @param identity the identity
   * @return the lexical class identity
   * @throws Exception the exception
   */
  public LexicalClassIdentity getLexicalClassIdentity(
    LexicalClassIdentity identity) throws Exception;

  /**
   * Adds the lexical class identity.
   *
   * @param lexicalClassIdentity the lexical class identity
   * @return the lexical class identity
   * @throws Exception the exception
   */
  public LexicalClassIdentity addLexicalClassIdentity(
    LexicalClassIdentity lexicalClassIdentity) throws Exception;

  /**
   * Update lexical class identity.
   *
   * @param lexicalClassIdentity the lexical class identity
   * @throws Exception the exception
   */
  public void updateLexicalClassIdentity(
    LexicalClassIdentity lexicalClassIdentity) throws Exception;

  /**
   * Removes the lexical class identity.
   *
   * @param lexicalClassIdentityId the lexical class identity id
   * @throws Exception the exception
   */
  public void removeLexicalClassIdentity(Long lexicalClassIdentityId)
    throws Exception;

}