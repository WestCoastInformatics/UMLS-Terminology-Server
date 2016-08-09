/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.ProcessConfig;

/**
 * Represents a sortable list of {@link ProcessConfig}
 */
public interface ProcessConfigList extends ResultList<ProcessConfig> {
  // nothing extra, a simple wrapper for easy serialization
}
