/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.AlgorithmExecution;

/**
 * Represents a sortable list of {@link AlgorithmExecution}
 */
public interface AlgorithmExecutionList extends ResultList<AlgorithmExecution> {
  // nothing extra, a simple wrapper for easy serialization
}
