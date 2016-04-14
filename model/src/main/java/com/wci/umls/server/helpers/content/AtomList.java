/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Atom;

/**
 * Represents a sortable list of {@link Atom}
 */
public interface AtomList extends ResultList<Atom> {
  // nothing extra, a simple wrapper for easy serialization
}
