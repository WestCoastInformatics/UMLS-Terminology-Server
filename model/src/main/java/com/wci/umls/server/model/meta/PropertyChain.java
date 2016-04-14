/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import java.util.List;

/**
 * Represents a property chain (of {@link AdditionalRelationshipType}s).
 */
public interface PropertyChain extends Abbreviation {

  /**
   * Returns the chain.
   *
   * @return the chain
   */
  public List<AdditionalRelationshipType> getChain();

  /**
   * Sets the chain.
   *
   * @param chain the chain
   */
  public void setChain(List<AdditionalRelationshipType> chain);

  /**
   * Returns the result.
   *
   * @return the result
   */
  public AdditionalRelationshipType getResult();

  /**
   * Sets the result.
   *
   * @param result the result
   */
  public void setResult(AdditionalRelationshipType result);

}
