/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Mapping;

/**
 * Represents a sortable list of {@link Mapping}
 */
public interface MappingList extends ResultList<Mapping> {
  // nothing extra, a simple wrapper for easy serialization
}
