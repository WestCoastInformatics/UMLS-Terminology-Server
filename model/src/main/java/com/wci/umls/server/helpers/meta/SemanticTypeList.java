/**
 * Copyright 2016 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.SemanticType;

/**
 * Represents a sortable list of {@link SemanticType}
 */
public interface SemanticTypeList extends ResultList<SemanticType> {
  // nothing extra, a simple wrapper for easy serialization
}
