/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.RootTerminology;

/**
 * Represents a sortable list of {@link RootTerminology}
 */
public interface RootTerminologyList extends ResultList<RootTerminology> {
  // nothing extra, a simple wrapper for easy serialization
}
