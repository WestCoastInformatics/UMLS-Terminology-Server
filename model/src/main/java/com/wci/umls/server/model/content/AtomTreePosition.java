/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a position in a hierarchical tree of atoms. The ancestor path
 * will be a delimiter-separated value of atom terminology ids.
 */
public interface AtomTreePosition extends TreePosition<Atom> {
  // n/a
}