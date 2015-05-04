/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.GeneralMetadataEntry;

/**
 * Represents a sortable list of {@link GeneralMetadataEntry}
 */
public interface GeneralMetadataEntryList extends
    ResultList<GeneralMetadataEntry> {
  // nothing extra, a simple wrapper for easy serialization
}
