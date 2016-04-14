/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;

/**
 * Represents a sortable list of {@link Tree}
 */
public interface TreeList extends ResultList<Tree> {
  // nothing extra, a simple wrapper for easy serialization
}
