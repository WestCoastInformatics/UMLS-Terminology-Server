/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Code}s.
 */
public interface CodeTransitiveRelationship extends
    TransitiveRelationship<Code> {
  // nothing extra, connects two codes
}
