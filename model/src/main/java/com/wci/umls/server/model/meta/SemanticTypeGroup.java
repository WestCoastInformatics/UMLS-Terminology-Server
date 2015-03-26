package com.wci.umls.server.model.meta;

import java.util.List;

/**
 * Represents a group of {@link SemanticType}s.
 */
public interface SemanticTypeGroup extends Abbreviation {

  /**
   * Returns the semantic types.
   * 
   * @return the semantic types
   */
  public List<SemanticType> getSemanticTypes();

  /**
   * Sets the semantic types.
   * 
   * @param semanticTypes the semantic types
   */
  public void setSemanticTypes(List<SemanticType> semanticTypes);

  /**
   * Adds the semantic types.
   * 
   * @param semanticType the semantic type
   */
  public void addSemanticType(SemanticType semanticType);
}
