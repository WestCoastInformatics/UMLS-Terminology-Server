/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
/*
 * 
 */
package com.wci.umls.server.services;

import com.wci.umls.server.model.meta.AttributeIdentity;
import com.wci.umls.server.model.meta.SemanticTypeComponentIdentity;

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
   * Gets the attribute identity.
   *
   * @param hashCode the hash code
   * @return the attribute identity
   * @throws Exception the exception
   */
  public AttributeIdentity getAttributeIdentity(String hashCode)
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
   * Gets the semantic type component identity.
   *
   * @param hashCode the hash code
   * @return the semantic type component identity
   * @throws Exception the exception
   */
  public SemanticTypeComponentIdentity getSemanticTypeComponentIdentity(
    String hashCode) throws Exception;

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

}