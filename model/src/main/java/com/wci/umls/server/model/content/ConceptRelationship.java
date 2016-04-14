/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Concept}s.
 */
public interface ConceptRelationship extends Relationship<Concept, Concept> {
  // nothing extra, connects two concepts
}
