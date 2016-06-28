/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.WorkflowBin;

/**
 * Represents a sortable list of {@link WorkflowBin}
 */
public interface WorkflowBinList extends ResultList<WorkflowBin> {
  // nothing extra, a simple wrapper for easy serialization
}
