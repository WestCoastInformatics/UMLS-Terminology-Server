/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.ComponentHistory;

/**
 * Represents a sortable list of {@link ComponentHistoryList}
 */
public interface ComponentHistoryList extends ResultList<ComponentHistory> {
  // nothing extra, a simple wrapper for easy serialization
}
