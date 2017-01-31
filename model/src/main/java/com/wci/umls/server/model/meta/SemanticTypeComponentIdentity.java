/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.meta;

import com.wci.umls.server.helpers.HasId;
import com.wci.umls.server.helpers.Identity;
import com.wci.umls.server.model.content.SemanticTypeComponent;

/**
 * Represents identity for a {@link SemanticTypeComponent}.
 */
public interface SemanticTypeComponentIdentity extends HasId, Identity {

  /**
   * Sets the concept terminology id.
   *
   * @param conceptTerminologyId the concept terminology id
   */
  public void setConceptTerminologyId(String conceptTerminologyId);

  /**
   * Returns the concept terminology id.
   *
   * @return the concept terminology id
   */
  public String getConceptTerminologyId();

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

}
