/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import com.wci.umls.server.helpers.HasDefinitions;
import com.wci.umls.server.helpers.HasRelationships;

/**
 * Represents a conceptual meaning. This can be a concept in a terminology (like
 * SNOMED CT or ICD10CM), or it could be a concept in a metathesaurus (like a
 * CUI, or an RXCUI).
 */
public interface Concept extends AtomClass, HasDefinitions,
    HasRelationships<ConceptRelationship> {

  /**
   * Indicates whether or not the concept is fully defined. This is always false
   * for non-ontological terminologies.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFullyDefined();

  /**
   * Sets the fully defined flag.
   */
  public void setFullyDefined();

}