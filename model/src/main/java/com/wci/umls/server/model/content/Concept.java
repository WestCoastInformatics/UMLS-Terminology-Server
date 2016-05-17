/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

import java.util.List;

import com.wci.umls.server.helpers.HasMembers;
import com.wci.umls.server.helpers.HasNotes;

/**
 * Represents a conceptual meaning. This can be a concept in a terminology (like
 * SNOMED CT or ICD10CM), or it could be a concept in a metathesaurus (like a
 * CUI, or an RXCUI).
 */
public interface Concept extends AtomClass, ComponentHasDefinitions,
    ComponentHasRelationships<ConceptRelationship>,
    HasMembers<ConceptSubsetMember>, HasNotes {

  /**
   * Indicates whether or not the concept is fully defined. This is always false
   * for non-ontological terminologies.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isFullyDefined();

  /**
   * Sets the fully defined flag.
   *
   * @param fullyDefined the fully defined
   */
  public void setFullyDefined(boolean fullyDefined);

  /**
   * Indicates whether or not this is an anonymous concept.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isAnonymous();

  /**
   * Sets the anonymous flag.
   *
   * @param anonymous the anonymous
   */
  public void setAnonymous(boolean anonymous);

  /**
   * Returns the semantic types.
   *
   * @return the semantic types
   */
  public List<SemanticTypeComponent> getSemanticTypes();

  /**
   * Sets the semantic types.
   *
   * @param semanticTypes the semantic types
   */
  public void setSemanticTypes(List<SemanticTypeComponent> semanticTypes);

  /**
   * Adds the semantic type.
   *
   * @param semanticType the semantic type
   */
  public void addSemanticType(SemanticTypeComponent semanticType);

  /**
   * Removes the semantic type.
   *
   * @param semanticType the semantic type
   */
  public void removeSemanticType(SemanticTypeComponent semanticType);

  /**
   * Returns the uses relationship intersection.
   *
   * @return the uses relationship intersection
   */
  public boolean getUsesRelationshipIntersection();

  /**
   * Sets the uses relationship intersection.
   *
   * @param flag the flag
   */
  public void setUsesRelationshipIntersection(boolean flag);

  /**
   * Returns the uses relationship union.
   *
   * @return the uses relationship union
   */
  public boolean getUsesRelationshipUnion();

  /**
   * Sets the uses relationship union.
   *
   * @param flag the flag
   */
  public void setUsesRelationshipUnion(boolean flag);

}
