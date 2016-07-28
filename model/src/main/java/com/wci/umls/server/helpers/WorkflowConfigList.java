/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.WorkflowConfig;

/**
 * Represents a sortable list of {@link WorkflowConfig}
 */
public interface WorkflowConfigList extends ResultList<WorkflowConfig> {
  // nothing extra, a simple wrapper for easy serialization
}
