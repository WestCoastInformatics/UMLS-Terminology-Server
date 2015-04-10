/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Descriptor}s.
 */
public interface DescriptorTransitiveRelationship extends
    TransitiveRelationship<Descriptor> {
  // nothing extra, connects two concepts
}
