/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import java.util.List;

/**
 * Represents a property chain (of {@link RelationshipType}s).
 */
public interface PropertyChain extends Abbreviation {

  /**
   * Returns the chain.
   *
   * @return the chain
   */
  public List<RelationshipType> getChain();

  /**
   * Sets the chain.
   *
   * @param chain the chain
   */
  public void setChain(List<RelationshipType> chain);

  /**
   * Returns the result.
   *
   * @return the result
   */
  public RelationshipType getResult();

  /**
   * Sets the result.
   *
   * @param result the result
   */
  public void setResult(RelationshipType result);

}
