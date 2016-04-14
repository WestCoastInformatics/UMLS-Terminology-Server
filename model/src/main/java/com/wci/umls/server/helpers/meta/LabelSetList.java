/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.LabelSet;

/**
 * Represents a sortable list of {@link LabelSet}
 */
public interface LabelSetList extends ResultList<LabelSet> {
  // nothing extra, a simple wrapper for easy serialization
}
