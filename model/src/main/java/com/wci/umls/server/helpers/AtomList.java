/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.content.Atom;

/**
 * Represents a sortable list of {@link Atom}
 */
public interface AtomList extends ResultList<Atom> {
  // nothing extra, a simple wrapper for easy serialization
}
