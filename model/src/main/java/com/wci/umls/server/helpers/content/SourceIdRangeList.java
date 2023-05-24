/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.inversion.SourceIdRange;

/**
 * Represents a sortable list of {@link SourceIdRange}
 */
public interface SourceIdRangeList extends ResultList<SourceIdRange> {
  // nothing extra, a simple wrapper for easy serialization
}
