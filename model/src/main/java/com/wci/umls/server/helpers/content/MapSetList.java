/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.MapSet;

/**
 * Represents a sortable list of {@link MapSet}
 */
public interface MapSetList extends ResultList<MapSet> {
  // nothing extra, a simple wrapper for easy serialization
}
