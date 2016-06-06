/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.model.workflow;

import com.wci.umls.server.helpers.ResultList;

/**
 * Represents a sortable list of {@link TrackingRecord}
 */
public interface TrackingRecordList extends ResultList<TrackingRecord> {
  // nothing extra, a simple wrapper for easy serialization
}
