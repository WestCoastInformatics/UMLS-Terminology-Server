/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.ProcessExecution;

/**
 * Represents a sortable list of {@link ProcessExecution}
 */
public interface ProcessExecutionList extends ResultList<ProcessExecution> {
  // nothing extra, a simple wrapper for easy serialization
}
