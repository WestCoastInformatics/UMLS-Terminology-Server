/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.MapSet;
import com.wci.umls.server.model.content.Mapping;

/**
 * Represents a sortable list of {@link Mapping}
 */
public interface MapSetList extends ResultList<MapSet> {
  // nothing extra, a simple wrapper for easy serialization
}
