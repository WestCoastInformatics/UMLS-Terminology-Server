/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.HasName;

/**
 * Represents the name of an attribute.
 */
public interface AttributeIdentity extends HasId, HasName {

  /**
   * Sets the owner type.
   *
   * @param ownerType the new owner type
   */
  public void setOwnerType(IdType ownerType);

  /**
   * Gets the owner type.
   *
   * @return the owner type
   */
  public IdType getOwnerType();

  /**
   * Sets the owner id.
   *
   * @param ownerId the new owner id
   */
  public void setOwnerId(String ownerId);

  /**
   * Gets the owner id.
   *
   * @return the owner id
   */
  public String getOwnerId();

  /**
   * Sets the owner qualifier.
   *
   * @param qualifier the new owner qualifier
   */
  public void setOwnerQualifier(String qualifier);

  /**
   * Gets the owner qualifier.
   *
   * @return the owner qualifier
   */
  public String getOwnerQualifier();

  /**
   * Gets the hash code.
   *
   * @return the hash code
   */
  public String getHashCode();

  /**
   * Sets the hash code.
   *
   * @param hashCode the new hash code
   */
  public void setHashCode(String hashCode);

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public String getTerminology();

  /**
   * Sets the terminology.
   *
   * @param terminology the new terminology
   */
  public void setTerminology(String terminology);
  

  /**
   * Gets the terminology.
   *
   * @return the terminology
   */
  public String getTerminologyId();

  /**
   * Sets the terminology.
   *
   * @param terminologyId the new terminology id
   */
  public void setTerminologyId(String terminologyId);

}
