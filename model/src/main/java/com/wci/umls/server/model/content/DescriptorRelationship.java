/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Descriptor}s.
 */
public interface DescriptorRelationship extends
    Relationship<Descriptor, Descriptor> {
  // nothing extra, connects two concepts
}
