/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.model.workflow.WorkflowBinDefinition;

/**
 * Represents a sortable list of {@link WorkflowBinDefinition}
 */
public interface WorkflowBinDefinitionList extends ResultList<WorkflowBinDefinition> {
  // nothing extra, a simple wrapper for easy serialization
}
