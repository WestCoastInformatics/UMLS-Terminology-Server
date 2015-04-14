/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers;

import com.wci.umls.server.ReleaseProperty;

/**
 * Represents a sortable list of {@link ReleaseProperty}
 */
public interface ReleasePropertyList extends ResultList<ReleaseProperty> {
  // nothing extra, a simple wrapper for easy serialization
}
