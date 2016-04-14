/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.SourceData;

/**
 * Represents a sortable list of {@link SearchResult} objects.
 */
public interface SourceDataList extends ResultList<SourceData> {
  // nothing extra, a simple wrapper for easy serialization
}
