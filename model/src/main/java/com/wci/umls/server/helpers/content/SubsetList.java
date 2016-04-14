/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.content;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.content.Subset;

/**
 * Represents a sortable list of {@link Subset}
 */
public interface SubsetList extends ResultList<Subset> {
  // nothing extra, a simple wrapper for easy serialization
}
