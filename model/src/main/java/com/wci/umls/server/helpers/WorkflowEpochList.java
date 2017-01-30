/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.WorkflowEpoch;

/**
 * Represents a sortable list of {@link WorkflowEpoch}
 */
public interface WorkflowEpochList extends ResultList<WorkflowEpoch> {
  // nothing extra, a simple wrapper for easy serialization
}
