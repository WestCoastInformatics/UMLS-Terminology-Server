/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.MarkerSet;

/**
 * Represents a sortable list of {@link MarkerSet}
 */
public interface MarkerSetList extends ResultList<MarkerSet> {
  // nothing extra, a simple wrapper for easy serialization
}
