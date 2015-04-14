/**
 * Copyright 2015 West Coast Informatics, LLC
 */
package com.wci.umls.server.helpers.meta;

import com.wci.umls.server.helpers.ResultList;
import com.wci.umls.server.model.meta.TermType;

/**
 * Represents a sortable list of {@link TermType}
 */
public interface TermTypeList extends ResultList<TermType> {
  // nothing extra, a simple wrapper for easy serialization
}
