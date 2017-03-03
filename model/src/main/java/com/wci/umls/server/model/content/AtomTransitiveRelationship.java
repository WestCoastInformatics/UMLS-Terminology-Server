/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Atom}s.
 */
public interface AtomTransitiveRelationship extends
    TransitiveRelationship<Atom> {
  // nothing extra, connects two atoms
}
