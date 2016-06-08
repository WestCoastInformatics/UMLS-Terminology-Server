/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.WorkflowEpoch;

/**
 * Represents a sortable list of {@link WorkflowEpoch}
 */
public interface MappingList extends ResultList<WorkflowEpoch> {
  // nothing extra, a simple wrapper for easy serialization
}
