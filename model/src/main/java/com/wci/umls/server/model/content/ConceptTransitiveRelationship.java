package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Concept}s.
 */
public interface ConceptTransitiveRelationship extends
    TransitiveRelationship<Concept> {
  // nothing extra, connects two concepts
}
