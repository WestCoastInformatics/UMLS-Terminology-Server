/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Code}s.
 */
public interface CodeRelationship extends Relationship<Code, Code> {
  // nothing extra, connects two concepts
}
