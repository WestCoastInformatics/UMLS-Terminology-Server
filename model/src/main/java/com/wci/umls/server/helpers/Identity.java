/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

/**
 * Represents an identity.
 */
public interface Identity {

  /**
   * Returns the identity code.
   *
   * @return the identity code
   */
  public String getIdentityCode();

  /**
   * Sets the identity code 
   * NOTE: this is just a placeholder so XML/JSON
   * serialization is kept happy. It will never actually do anything.
   *
   * @param identityCode the identity code
   */
  public void setIdentityCode(String identityCode);

}
