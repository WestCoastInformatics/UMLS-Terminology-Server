/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.User;

/**
 * Represents a sortable list of {@link User}.
 */
public interface ComponentInfoList extends ResultList<ComponentInfo> {
  // nothing extra, a simple wrapper for easy serialization
}
