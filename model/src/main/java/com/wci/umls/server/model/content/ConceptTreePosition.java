/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree of concepts. The ancestor path
 * will be a delimiter-separated value of concept terminology ids.
 */
public interface ConceptTreePosition extends TreePosition<Concept> {
  // n/a
}