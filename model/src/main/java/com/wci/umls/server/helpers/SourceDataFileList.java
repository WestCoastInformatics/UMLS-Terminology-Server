/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.SourceDataFile;

/**
 * Represents a sortable list of {@link SourceDataFile} objects.
 */
public interface SourceDataFileList extends ResultList<SourceDataFile> {
  // nothing extra, a simple wrapper for easy serialization
}
