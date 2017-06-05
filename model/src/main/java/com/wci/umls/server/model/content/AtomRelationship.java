/*
 *    Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.content;

/**
 * Represents a relationship between two {@link Atom}s.
 */
public interface AtomRelationship extends Relationship<Atom, Atom> {
  // nothing extra, connects two atoms
  
  /**
   * Indicates whether or not short form long form is the case.
   *
   * @return <code>true</code> if so, <code>false</code> otherwise
   */
  public boolean isShortFormLongForm();
}
