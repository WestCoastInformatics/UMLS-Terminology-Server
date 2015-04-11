/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;


/**
 * A semantic type {@link ComponentHasAttributes}.
 */
public interface SemanticTypeComponent extends ComponentHasAttributes {

  /**
   * Returns the semantic type.
   *
   * @return the semantic type
   */
  public String getSemanticType();

  /**
   * Sets the semantic type.
   *
   * @param semanticType the semantic type
   */
  public void setSemanticType(String semanticType);

}