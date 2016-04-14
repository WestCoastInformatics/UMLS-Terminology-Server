/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.Terminology;

/**
 * Represents a sortable list of {@link Terminology}
 */
public interface TerminologyList extends ResultList<Terminology> {
  // nothing extra, a simple wrapper for easy serialization
}
